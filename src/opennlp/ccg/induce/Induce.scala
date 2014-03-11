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

object Induce extends App {
  
  val sect = "24"
  val firstPass = true
//  val firstPass = false
    
  val log_fn = "/Users/mwhite/dev/hmmm/scala/convert_deps/out/logs/inducecats." + sect + ".log"
  println("logging to " + log_fn)
  val out = new PrintWriter(new FileWriter(log_fn))
  
  val working_dir = "/Users/mwhite/dev/hmmm/scala/convert_deps/out"

  out.println("loading initial grammar")
  val grammar_fn = working_dir + "/grammar.xml"
  val grammar = new Grammar(grammar_fn)
  out.println()

  val srcdir = working_dir + "/converted/" + sect
  out.println("loading files from " + srcdir)
  val outdir = working_dir + "/induced/" + sect
  out.println("writing files to " + outdir)
  new File(outdir).mkdirs()
  val grammarsdir = working_dir + "/grammars/" + sect
  out.println("writing grammars to " + grammarsdir)
  new File(grammarsdir).mkdirs()
  out.println()
  
  val signScorer = if (firstPass) SignScorer.complexityScorer else {
    val gensyn_dir = "/Users/mwhite/dev/hmmm/scala/convert_deps/out/induced/gensyn"
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
	  if (rule.isInstanceOf[TypeChangingRule]) bestRules += rule.name()
	  for (child <- hist.getInputs) getTCRs(child)
    }
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
  
  // stats
  var totalBest = 0
  var totalComplete = 0
  var numErrors = 0
  var totalItems = 0
  var totalComplexity = 0
  var totalLogScore = 0.0
  var totalZeros = 0
  
  // induce derivations for file
  def induceFile(fileid:String) = {
    
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
    val itemIds = new ListBuffer[String]()
    for (itemno <- 0 until rinfo.numberOfItems) {
      try {
        val inducer = new DerivationInducer(grammar, generalRules, ruleMap, rinfo, itemno, signScorer, out)
        val edge = inducer.getResult
        bestEdges += edge
        if (edge.complete) {
          completeSigns += edge.getSign
          itemIds += inducer.getId
          totalComplexity += edge.getSign.getDerivationHistory.complexity
          if (edge.score == 0) 
            totalZeros += 1 
          else 
            totalLogScore += Math.log10(edge.score)
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
    totalItems += rinfo.numberOfItems
    out.print("derived " + bestEdges.size + " edges with " + completeSigns.size + " complete signs ")
    out.println("out of a total of " + rinfo.numberOfItems + " items")
    out.println()

    val testbed_fn = outdir + "/" + fileid + ".xml"
    out.println("saving complete signs to " + testbed_fn)
    RegressionInfo.writeTestbed(grammar, completeSigns, itemIds, testbed_fn)
    out.println()
    
    // update best rules, cats and words
    for (sign <- completeSigns) getTCRs(sign)
    for (sign <- completeSigns) getLexItems(sign)
  }
  
  val inputnames = new File(srcdir).listFiles.map(_.getName).filter(_.endsWith(".xml"))
  val inputids = inputnames.map(fn => fn.substring(0, fn.lastIndexOf(".")))
  for (fileid <- inputids) {
    induceFile(fileid)
  }
//  induceFile(inputids(0))
  println()

  out.println("***** finished derivations")
  out.print("in total, derived " + totalBest + " edges with " + totalComplete + " complete signs ")
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
  val updatedRules = new RuleGroup(grammar)
  for (rule <- grammar.rules.getBinaryRules) updatedRules.addRule(rule)
  for (rule <- grammar.rules.getUnaryRules if !rule.isInstanceOf[TypeChangingRule]) updatedRules.addRule(rule)
  for (rule <- grammar.rules.getUnaryRules if rule.isInstanceOf[TypeChangingRule]) updatedRules.addRule(rule)
  val numExistingUnaries = updatedRules.getUnaryRules.size
  for (rule <- ruleMap.values if bestRules.contains(rule.name)) updatedRules.addRule(rule)
  val numRules = updatedRules.getBinaryRules.size + updatedRules.getUnaryRules.size 
  updatedRules.toXml(rules_fn)
  out.println("saved " + numRules + " rules")
  
  val lexicon_fn = grammarsdir + "/lexicon.xml"
  out.println("saving categories to " + lexicon_fn)
  print('.')
  val cats = ListBuffer[Category]()
  val tags = ListBuffer[String]()
  for ((cat,pos) <- bestLexCats) {
    cats += cat
    tags += pos
  }
  grammar.toLexiconXml(cats, tags, lexicon_fn)
  out.println("saved " + cats.size + " cats")

  // TODO compare to converted words??
  val morph_fn = grammarsdir + "/morph2.xml" 
  out.println("saving words to " + morph_fn)
  print('.')
  val wordList = ListBuffer[Word]() ++ bestWords
  grammar.toMorphXml(wordList, morph_fn)
  out.println("saved " + wordList.size + " words")
  out.println()
  println()

  out.flush()
  out.close()
  println("done")
}