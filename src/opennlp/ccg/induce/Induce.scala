package opennlp.ccg.induce

import scala.collection.generic.Sorted
import scala.collection.JavaConversions._
import scala.collection.mutable._
import java.io._
import java.util.BitSet
import org.jdom._
import opennlp.ccg.hylo._
import opennlp.ccg.synsem._
import opennlp.ccg.unify._
import opennlp.ccg.grammar._
import opennlp.ccg.lexicon._
import opennlp.ccg.test._
import opennlp.ccg.realize._
import plugins.MyGenSynScorer
import opennlp.ccgbank.extract.Testbed

object Induce extends App {

  val openccghome = "/Users/mwhite/dev/github/openccg"
  val working_dir = "/Users/mwhite/dev/scala/convert_deps/out"
    
  val partition = "dev"
//  val partition = "train"
//  val partition = "test"
//  val partition = "others"
//  val partition = "24"

  val sects = partition match {
    case "dev" => Array("00")
    case "train" => Array("02", "03", "04", "05", "06", 
    		"07", "08", "09", "10", "11", 
    		"12", "13", "14", "15", "16", 
    		"17", "18", "19", "20", "21")
    case "test" => Array("23")
    case "others" => Array("01","22")
    case _ => Array("24")
  }

  val firstPass = true
//  val firstPass = false

  // NB: for some reason, nbest option finding very few alt derivations??
//  val nbest = true
  val nbest = false
  
  val log_fn = working_dir + "/logs/inducecats." + partition + ".log"
  println("logging to " + log_fn)
  val out = new PrintWriter(new FileWriter(log_fn))
  
  val grammarsdir = working_dir + "/grammars/" + partition
  out.println("writing grammar to " + grammarsdir)
  new File(grammarsdir).mkdirs()
  out.println()
  
  // merge morph files, starting with init morph
  val morphinit = working_dir + "/init/morph.xml"
  val morphout = working_dir + "/morph.xml"
  val xsltmerge = openccghome + "/ccgbank/templates/mergeMorph.xsl"
  val initargs = Array("-IN",morphinit,"-XSL",xsltmerge,"-PARAM","newmorphfile",morphinit,"-OUT",morphout)
  org.apache.xalan.xslt.Process.main(initargs)
  val morphoutfile = new File(morphout)
  val morphtmp = working_dir + "/morph.tmp.xml"
  val morphtmpfile = new File(morphtmp)
  for (sect <- sects) {
    morphoutfile.renameTo(morphtmpfile)
	val morphmerge = working_dir + "/grammars/" + sect + "/morph.xml"
    out.println("merging morphs from " + morphmerge)
    val cmdargs = Array("-IN",morphtmp,"-XSL",xsltmerge,"-PARAM","newmorphfile",morphmerge,"-OUT",morphout)
    org.apache.xalan.xslt.Process.main(cmdargs)
    morphtmpfile.delete()
  }
  out.println()
  
  out.println("loading initial grammar")
  val grammar_fn = working_dir + "/grammar.xml"
  val grammar = new Grammar(grammar_fn)
  out.println()

  val signScorer = if (firstPass) SignScorer.complexityScorer else {
    val gensyn_dir = working_dir + "/induced/gensyn"
    System.setProperty("gensyn.model.dir", gensyn_dir)
    out.println("loading generative syntactic model from " + gensyn_dir)
    val gensyn = new MyGenSynScorer()
    out.println()
    gensyn
  }
  
  out.println("finding general rules")
  val generalRules = new RuleGroup(grammar)
  out.print("binary rules: ")
  for (rule <- grammar.rules.getBinaryRules) {
    generalRules.addRule(rule)
    out.print(rule.name + " ")
  }
  out.println()
  // add type raising rules, and type changing ones with no semantics too
  out.print("unary rules: ")
  for (rule <- grammar.rules.getUnaryRules) {
    if (!rule.isInstanceOf[TypeChangingRule] ||
        rule.asInstanceOf[TypeChangingRule].getResult.getLF == null) 
    {
        generalRules.addRule(rule)
        out.print(rule.name + " ")
    }
  }
  out.println()
  out.println()

  // map to track unique unary rules
  val ruleMap = HashMap[(Category,Category),TypeChangingRule]()

  // type changing rules from best derivations
  val bestRules = HashSet[String]()
  def getTCRs(sign:Sign):Unit = {
    val hist = sign.getDerivationHistory
    if (!hist.isEmpty) {
      val rule = hist.getRule
	  if (rule.isInstanceOf[TypeChangingRule]) bestRules += rule.name
	  for (child <- hist.getInputs) getTCRs(child)
    }
  }

  def makeBestRuleGroup() = {
    val updatedRules = new RuleGroup(grammar)
    for (rule <- grammar.rules.getBinaryRules) updatedRules.addRule(rule)
    for (rule <- grammar.rules.getUnaryRules if !rule.isInstanceOf[TypeChangingRule]) updatedRules.addRule(rule)
    for (rule <- grammar.rules.getUnaryRules if rule.isInstanceOf[TypeChangingRule]) updatedRules.addRule(rule)
    for (rule <- ruleMap.values if bestRules.contains(rule.name)) updatedRules.addRule(rule)
    updatedRules
  }
  
  // cats and words from best derivations
  val bestLexCats = HashSet[(Category,String)]()
  val bestWords = HashSet[Word]()
  def getLexItems(sign:Sign):Unit = {
    val hist = sign.getDerivationHistory
    if (hist.isEmpty) {
      val cat = sign.getCategory.copy()
      val word = sign.getWords.get(0)
      val morph = Word.createFullWord(word, word.getStem, word.getPOS, null, word.getSemClass) // remove supertag
      UnifyControl.abstractNominals(cat, word.getStem)
      bestLexCats += Pair(cat, word.getPOS)      
      bestWords += morph
    }
    else {
	  for (child <- hist.getInputs) getLexItems(child)
    }
  }
  
  def saveLexicon(lexicon_fn:String) = {
    val cats = ListBuffer[Category]()
    val tags = ListBuffer[String]()
    for ((cat,pos) <- bestLexCats) {
      cats += cat
      tags += pos
    }
    grammar.toLexiconXml(cats, tags, lexicon_fn)
  }
  
  def saveMorph(morph_fn:String) = {
    val wordList = ListBuffer[Word]() ++ bestWords
    grammar.toMorphXml(wordList, morph_fn)
  }
  
  // stats
  var totalBest = 0
  var totalComplete = 0
  var totalNBestComplete = 0
  var numErrors = 0
  var totalItems = 0
  var totalComplexity = 0
  var totalLogScore = 0.0
  var totalZeros = 0
  
  // induce derivations for file
  def induceFile(fileid:String, srcdir:String, outdir:String,
		  textPW:PrintWriter, factorsPW:PrintWriter, 
		  combosPW:PrintWriter, combos:HashSet[String], 
		  predsPW:PrintWriter) = {
    
    def updateRulesCatsWords(sign:Sign):Unit = {
      // update best rules, cats and words
      getTCRs(sign)
      getLexItems(sign)
      // update factors, combos, preds
      factorsPW.println(grammar.lexicon.tokenizer.format(sign.getWords))
      val newcombos = ListBuffer[String]()
      Testbed.newCombos(sign, newcombos, combos)
      for (combo <- newcombos) { combosPW.println(combo) }
      predsPW.println(Testbed.getPredInfo(sign.getCategory.getLF))
  }
  
    // load testbed
    val tb_fn = srcdir + "/" + fileid + ".xml"
    out.println("***** loading testbed " + fileid)
    print('.')
    val rinfo = new RegressionInfo(grammar, new File(tb_fn))
    out.println("read " + rinfo.numberOfItems() + " items")
    out.println()
  
    // derive edges for each item, retaining complete signs and their ids
    val bestEdges = new ListBuffer[Edge]()
    val completeSigns = new ListBuffer[Sign]()
    val allCompleteSigns = new ListBuffer[Sign]()
    val itemIds = new ListBuffer[String]()
    for (itemno <- 0 until rinfo.numberOfItems) {
      textPW.println(rinfo.getItem(itemno).sentence)
      try {
        val inducer = new DerivationInducer(grammar, generalRules, ruleMap, rinfo, itemno, signScorer, out)
        val edge = inducer.getResult
        bestEdges += edge
        if (edge.complete) {
          val sign = edge.getSign
          completeSigns += sign
          totalComplexity += sign.getDerivationHistory.complexity
          if (edge.score == 0) 
            totalZeros += 1 
          else 
            totalLogScore += Math.log10(edge.score)
          if (nbest) {
            for ((nbEdge,i) <- inducer.getResults.zipWithIndex) {
              val nbSign = nbEdge.getSign
              allCompleteSigns += nbSign
              val nbId = inducer.getId + "_" + i
              itemIds += nbId
              updateRulesCatsWords(nbSign)
            }
          }
          else {
            allCompleteSigns += sign
            itemIds += inducer.getId
            updateRulesCatsWords(sign)
          }
        }
      } catch {
        case exc:Exception => {
          numErrors += 1
          out.println("** error **, derivation failed! skipping item " + itemno)
          exc.printStackTrace(out)
          out.println()
        }
      }
    }
  
    totalBest += bestEdges.size
    totalComplete += completeSigns.size
    totalNBestComplete += allCompleteSigns.size
    totalItems += rinfo.numberOfItems
    out.print("derived " + bestEdges.size + " edges with " + completeSigns.size + " complete signs ")
    if (nbest) out.print("and " + allCompleteSigns.size + " n-best complete signs ")
    out.println("out of a total of " + rinfo.numberOfItems + " items")
    out.println()

    val testbed_fn = outdir + "/" + fileid + ".xml"
    out.println("saving complete signs to " + testbed_fn)
    RegressionInfo.writeTestbed(grammar, allCompleteSigns, itemIds, testbed_fn)
    out.println()
  }
  
  for (sect <- sects) {
    val srcdir = working_dir + "/converted/" + sect
    out.println("loading files from " + srcdir)
    val outdir = working_dir + "/induced/" + sect
    out.println("writing files to " + outdir)
    new File(outdir).mkdirs()
    out.println()
    val textfn = working_dir + "/induced/info/text-" + sect + "-all"
    out.println("writing text to " + textfn)
    val textfile = new File(textfn)
    textfile.getParentFile.mkdirs()
    val textPW = new PrintWriter(new BufferedWriter(new FileWriter(textfile)))
    val factorsfn = working_dir + "/induced/info/factors-" + sect + "-all"
    out.println("writing factors to " + factorsfn)
    val factorsPW = new PrintWriter(new BufferedWriter(new FileWriter(factorsfn)))
    val combosfn = working_dir + "/induced/info/combos-" + sect + "-all"
    out.println("writing combos to " + combosfn)
    val combosPW = new PrintWriter(new BufferedWriter(new FileWriter(combosfn)))
    val combos = new HashSet[String]()
    val predsfn = working_dir + "/induced/info/preds-" + sect + "-all"
    out.println("writing preds to " + predsfn)
    val predsPW = new PrintWriter(new BufferedWriter(new FileWriter(predsfn)))
    out.println()
    val inputnames = new File(srcdir).listFiles.map(_.getName).filter(_.endsWith(".xml"))
    val inputids = inputnames.map(fn => fn.substring(0, fn.lastIndexOf(".")))
    for (fileid <- inputids) {
      induceFile(fileid, srcdir, outdir, textPW, factorsPW, combosPW, combos, predsPW)
    }
//    induceFile(inputids(0), srcdir, outdir, textPW, factorsPW, combosPW, combos, predsPW)
//    induceFile("wsj_2402", srcdir, outdir, textPW, factorsPW, combosPW, combos, predsPW)
    textPW.flush(); textPW.close()
    factorsPW.flush(); factorsPW.close()
    combosPW.flush(); combosPW.close()
    predsPW.flush(); predsPW.close()
    println()
    out.println()
  }

  out.println("***** finished derivations")
  out.print("in total, derived " + totalBest + " edges with " + totalComplete + " complete signs ")
  if (nbest) out.print("and " + totalNBestComplete + " n-best complete signs ")
  out.println("out of a total of " + totalItems + " items")
  out.println("with " + numErrors + " errors in total")
  val avgComplexity = 1.0 * totalComplexity / totalComplete
  out.println("total complexity for complete signs: " + totalComplexity)
  out.println("avg complexity: " + avgComplexity)
  if (!firstPass) {
    val avgLogScore = 1.0 * totalLogScore / (totalComplete - totalZeros)
	out.println("total log score for non-zero complete signs: " + totalLogScore)
	out.println("number of complete signs with zero scores: " + totalZeros)
	out.println("avg log score: " + avgLogScore)
  }
  out.println()
  
  val rules_fn = grammarsdir + "/rules.xml"
  out.println("saving rules to " + rules_fn)
  print('.')
  val updatedRules = makeBestRuleGroup() 
  val numRules = updatedRules.getBinaryRules.size + updatedRules.getUnaryRules.size 
  updatedRules.toXml(rules_fn)
  out.println("saved " + numRules + " rules")
  
  val lexicon_fn = grammarsdir + "/lexicon.xml"
  out.println("saving categories to " + lexicon_fn)
  print('.')
  saveLexicon(lexicon_fn)
  out.println("saved " + bestLexCats.size + " cats")

  // TODO compare to converted words??
  val morph_fn = grammarsdir + "/morph2.xml" 
  out.println("saving words to " + morph_fn)
  print('.')
  saveMorph(morph_fn)
  out.println("saved " + bestWords.size + " words")
  out.println()
  println()

  out.flush()
  out.close()
  println("done")
}