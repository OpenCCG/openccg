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

object Config {
//  val argrels = List("sbj","obj","iobj","vc","prd","oprd","prt","pmod","sub") //,"prp")
//  val optrels = List("sub") //,"prp")
//  val lexonlyrels = List("im")
  val argrels = List("nsubj","nsubjpass","csubj","expl","dobj","iobj","xcomp","ccomp")
  val optrels = List("case") //,"prp")
  val invrels = List("aux","auxpass","cop","case","mark")
  val lexrels = List("mwe","compound:prt") // nb: in principle could require these to use cats like prt[up]
  val nonargcats = List("n")
  val defaultcat = new AtomCat("np")
  val firstConjRel = "conj1" // "coord1"
  val secondConjRel = "conj2" // "coord2"
//  val node_max = 8
//  val agenda_max = 12
  val node_max = 20
  val agenda_max = 30
//  val log_sort = true
  val log_sort = false
}

class DerivationInducer(grammar:Grammar, generalRules:RuleGroup, ruleMap:HashMap[(Category,Category),TypeChangingRule], 
		rinfo:RegressionInfo, itemno:Int, signScorer:SignScorer, out:PrintWriter) {
  
  out.println("*** starting item " + itemno)
  val item = rinfo.getItem(itemno)
  out.println("id: " + item.getId())
  out.println()
	
  val fullwords = item.fullWords;
  out.println("full words:")
  out.println(fullwords)
  val words = grammar.lexicon.tokenizer.tokenize(item.fullWords, true)
  // strip <s> and </s>
  if (words.get(0).getForm == "<s>") words.remove(0);
  if (words.get(words.size()-1).getForm() == "</s>") words.remove(words.size()-1);
  out.println("tagged words:")
  for (word <- words) {
	  out.print(word.getForm + "/" + word.getPOS + " ")
  }
  out.println()

  out.println("lf:")
  val doc = new Document()
  val lfElt = item.lfElt
  lfElt.detach()
  doc.setRootElement(lfElt)
  val lf = grammar.loadLF(doc)
  out.println(lf.prettyPrint("  "))
  out.println()

  out.println("EPs:")
  val flattener = new Flattener()
  val preds = flattener.flatten(lf)
  HyloHelper.sort(preds)
  val edgeFactory = new EdgeFactory(grammar, preds, signScorer) //SignScorer.complexityScorer
  for ((pred,i) <- preds.zipWithIndex) out.println(i + ": " + pred)
  out.println()

  out.println("highest parents:")
  val parentMap = flattener.getHighestParentMap()
  def nomId(nom:Nominal) = {
    if (nom == null) -1
    else nom.getName().substring(1).toInt
  }
  val noms = parentMap.keys.toList.sortWith(nomId(_) < nomId(_))
  for (nom <- noms) out.println(nom + " -> " + parentMap.get(nom))
  out.println()
  
  val parentIds = Array.fill(words.size)(-1)
  parentMap.foreach(item => parentIds(nomId(item._1)) = nomId(item._2))  
  val roots = parentIds.indices.filter(parentIds(_) == -1)

  // array from head word id to a map from (rel, child id) pairs to rel pred indices
  val relPredIdxs = Array.fill(words.size)(Map[(String,Int),Int]())
  for ((pred,i) <- preds.zipWithIndex if HyloHelper.isRelPred(pred)) {
    val headId = nomId(HyloHelper.getPrincipalNominal(pred))
    val rel = HyloHelper.getRel(pred)
    val depId = nomId(HyloHelper.getSecondaryNominal(pred))
    relPredIdxs(headId) += (rel,depId) -> i
  }

  out.println("primary tree(s):")
  val primaryTrees = (roots.map(makeTreeNode(_))).toList
  for (root <- primaryTrees) {
    printTree(root)
  }
  out.println()

  def firstPredNomId(edge:Edge) = {
	  val sign = edge.getSign()
	  val lexpred = HyloHelper.flatten(sign.getCategory().getLF()).find(HyloHelper.isLexPred(_))
	  val nom = lexpred.get.getNominal() // nb: assumes lex pred exists
	  nomId(nom)
  }
  
  def matchesWord(edge:Edge, id:Int) = {
    val sign = edge.getSign()
    val word = words.get(id)
    sign.getWordForm() == word.getForm() && sign.getPOS() == word.getPOS()
  }
  
  out.println("initial edges:")
  val allEdgesWithIds = edgeFactory.createInitialEdges().map(edge => (edge, firstPredNomId(edge)))
  val edgesWithIds = (allEdgesWithIds.filter(item => matchesWord(item._1, item._2))).toList

  val edgesById = Array.fill(words.size)(new ListBuffer[Edge]())
  for ((edge,id) <- edgesWithIds) edgesById(id) += edge
  for (i <- 0 until words.size) {
    for (edge <- edgesById(i)) out.println(i + " " + edge)
  }
  out.println()

  out.println("initial rule instances:")
  for (ruleInst <- edgeFactory.ruleInstances) {
    out.println(ruleInst)
  }
  out.println()

  out.println("resetting counters")
  UnifyControl.startUnifySequence()
  out.println()

  out.println("trying edge combos:")
  for (root <- primaryTrees) {
    combineEdges(root)
  }
  out.println()
  
  out.println("unpacking roots")
  val pruningStrategy = new NBestPruningStrategy()
  val rzchart = new Chart(edgeFactory, pruningStrategy)
  val rootEdges = ListBuffer[Edge]()
  for (root <- primaryTrees) rootEdges ++= edgesById(root.id)
  val foundComplete = rootEdges.foldLeft(false){ (foundOne, edge) => foundOne || edge.complete() }
  rzchart.doUnpacking(rootEdges, foundComplete)
  out.println()

  out.println("best root edge: " + rzchart.bestEdge)
  out.println("complete: " + rzchart.bestEdge.complete)
  out.println("complexity: " + rzchart.bestEdge.getSign.getDerivationHistory.complexity)
  out.println(rzchart.bestEdge.getSign.getDerivationHistory)
  out.println()

  /** Returns the best edge resulting from the derivation. */
  def getResult() = { rzchart.bestEdge }
  
  /** Returns the id for the current item. */
  def getId() = { item.getId }
  
  // nb: perhaps better as a real class?
  case class TreeNode(id:Int, deps:List[(String,Int)], kids:List[TreeNode])
  
  def makeTreeNode(id:Int):TreeNode = {
    val kidIds = parentIds.indices.filter(parentIds(_) == id) 
    val deps = relPredIdxs(id).keys.toList.sortBy(_._2)
    val kids = (kidIds.map(makeTreeNode(_))).toList.sortBy(_.id)
    TreeNode(id, deps, kids)
  }
  
  // returns the empty string for kids without a rel, ie nested nodes
  def getKidRel(node:TreeNode, kid:TreeNode) = {
    val dep = node.deps.find(dep => dep._2 == kid.id) getOrElse ("",-1)
    dep._1
  }
  
  def printTree(node:TreeNode, indent:String="", rel:String="root"):Unit = {
    out.println(indent + "-" + rel + "-> " + node.id + " " + words.get(node.id))
    for (dep <- node.deps if !node.kids.exists(kid => kid.id == dep._2)) 
      out.println(indent + "  -" + dep._1 + "-> " + dep._2)
    for (kid <- node.kids) 
      printTree(kid, indent+"  ", getKidRel(node, kid))
  }

  case class EdgeSpan(edge:Edge, span:(Int,Int))
  
  def nomid(id:Int) = { f"w$id%03d" }

  // adds arg cats to back or front of arg stack, depending on direction, 
  // and assuming args given in reverse order
  // if arg cat has arity > 1, also uses result of arg cat to allow 
  // for use of composition, eg 'to' s[to]\np/(s[b]\np) for arg 'see' s[b]\np/np 
  // nb: special treatment of balanced punctuation may be needed?
  def extendCat(cat:Category, argCat:Category, dep:(String,Int), rightward:Boolean) = {
    val (rel, id) = dep
    val catCopy = cat.copy()
    UnifyControl.reindex(catCopy)
    val args = if (catCopy.isInstanceOf[ComplexCat]) 
      catCopy.asInstanceOf[ComplexCat].getArgStack() 
      else new ArgStack()
    val argCatCopy = argCat.copy()
    argCatCopy.setLF(null)
    argCatCopy.getTarget().getFeatureStructure().setFeature("index", new NominalAtom(nomid(id)))
    UnifyControl.reindex(argCatCopy)
    if (rightward) args.add(new BasicArg(new Slash('/',">"), argCatCopy))
    else args.addFront(new BasicArg(new Slash('\\',"<"), argCatCopy))
    val headNom = catCopy.getIndexNominal().copy().asInstanceOf[Nominal]
    val depRel = new Diamond(new ModeLabel(rel), new NominalAtom(nomid(id)))
    val lf = HyloHelper.append(catCopy.getLF(), new SatOp(headNom, depRel))
    val retcat = if (catCopy.isInstanceOf[ComplexCat]) {
      catCopy.setLF(lf)
      catCopy.asInstanceOf[ComplexCat]      
    }
    else {
      catCopy.setLF(null)
      new ComplexCat(catCopy.asInstanceOf[TargetCat], args, lf)
    }
    if (argCat.isInstanceOf[ComplexCat] && argCat.asInstanceOf[ComplexCat].arity() > 1) {
      val retcat2 = retcat.copy().asInstanceOf[ComplexCat]
      UnifyControl.reindex(retcat2)
      val args2 = retcat2.getArgStack()
      val argCat2 = argCatCopy.asInstanceOf[ComplexCat].getResult()
      val argCat2Copy = argCat2.copy()
      UnifyControl.reindex(argCat2Copy)
      if (rightward) args2.setLast(new BasicArg(new Slash('/',">"), argCat2Copy))
      else args2.set(0, new BasicArg(new Slash('\\',"<"), argCat2Copy))
      (retcat, Some(retcat2))
    }
    else (retcat, None)
  }

  // makes an edge for a coordinating conjunction using type of coord1DepCat
  def makeConjCoordEdge(lexConjEdge:Edge, lexConjId:Int, coord1DepCat:Category, 
		  				coord1PredIdx:Int, coord2PredIdx:Int, 
		  				coord1Dep:(String,Int), coord2Dep:(String,Int)) = {
    val (coord1Rel, coord1Id) = coord1Dep
    val (coord2Rel, coord2Id) = coord2Dep
    val lexConjSign = lexConjEdge.getSign
    val lexConjCat = lexConjSign.getCategory
    val lf0 = lexConjCat.getLF.copy()
    val headNom = lexConjCat.getIndexNominal().copy().asInstanceOf[Nominal]
    val coord1DepRel = new Diamond(new ModeLabel(coord1Rel), new NominalAtom(nomid(coord1Id)))
    val coord2DepRel = new Diamond(new ModeLabel(coord2Rel), new NominalAtom(nomid(coord2Id)))
    val lf1 = HyloHelper.append(lf0, new SatOp(headNom, coord1DepRel))
    val lf = HyloHelper.append(lf1, new SatOp(headNom, coord2DepRel))
    val coord1DepCatCopy = coord1DepCat.copy()
    coord1DepCatCopy.setLF(null)
    UnifyControl.reindex(coord1DepCatCopy)
    val coord2DepCat = coord1DepCatCopy.copy()
    coord2DepCat.getTarget().getFeatureStructure().setFeature("index", new NominalAtom(nomid(coord2Id)))
    UnifyControl.reindex(coord2DepCat)
    val resultCat = coord2DepCat.copy()
    resultCat.getTarget().getFeatureStructure().setFeature("index", new NominalAtom(nomid(lexConjId)))
    UnifyControl.reindex(resultCat)
    val args = if (resultCat.isInstanceOf[ComplexCat]) 
      resultCat.asInstanceOf[ComplexCat].getArgStack() 
      else new ArgStack()
    args.add(new BasicArg(new Slash('\\',"*"), coord1DepCatCopy))
    args.add(new BasicArg(new Slash('/',"*"), coord2DepCat))
    val coordCat = if (resultCat.isInstanceOf[ComplexCat]) {
      resultCat.setLF(lf)
      resultCat.asInstanceOf[ComplexCat]      
    }
    else new ComplexCat(resultCat.asInstanceOf[TargetCat], args, lf)
    val coordBitSet = lexConjEdge.bitset.clone().asInstanceOf[BitSet]
    coordBitSet.set(coord1PredIdx)
    coordBitSet.set(coord2PredIdx)
    makeEdge(coordCat, lexConjSign, coordBitSet)
  }
  
  // adds coindexed args for any argument cats that have basic args 
  // whose cats are not already present, eg 'be' s[b]\np[X]/(s[pt]\np[X])
  def coindexCat(cat:Category) = {
    if (!cat.isInstanceOf[ComplexCat]) cat
    else {
      val args = cat.asInstanceOf[ComplexCat].getArgStack
      val coindexes = ListBuffer[BasicArg]()
      for (i <- 0 until args.size) {
        val arg = args.get(i)
        if (arg.isInstanceOf[BasicArg]) {
          val argCat = arg.asInstanceOf[BasicArg].getCat
          if (argCat.isInstanceOf[ComplexCat]) {
            val argArgs = argCat.asInstanceOf[ComplexCat].getArgStack
            for (j <- 0 until argArgs.size) {
              val argArg = argArgs.get(j)
              if (argArg.isInstanceOf[BasicArg]) {
                val argArgCat = argArg.asInstanceOf[BasicArg].getCat
                if (argArgCat.isInstanceOf[AtomCat] &&
                    cat.getIndexNominal() != argArgCat.getIndexNominal() &&
                    !hasArgWithTargetIndex(args, argArgCat))
                  coindexes += argArg.asInstanceOf[BasicArg]
              }
            }
          }
        }
      }
      if (coindexes.isEmpty) cat
      else {
        val resultCat = cat.copy().asInstanceOf[ComplexCat]
        val resultArgs = resultCat.getArgStack
        for (arg <- coindexes) {
          if (arg.getSlash.getDir.equals(Slash.L)) resultArgs.addFront(arg.copy())
          else resultArgs.add(arg.copy())
        }
        out.println("added coindexes to: " + cat)
        out.println("yielding: " + resultCat)
        resultCat
      }
    }
  }
  
  // returns whether there's an arg with a target cat with the same index nominal as the given one
  def hasArgWithTargetIndex(args:ArgStack, cat:Category) = {
    var retval = false
    for (i <- 0 until args.size) {
      val arg = args.get(i)
      if (arg.isInstanceOf[BasicArg]) {
        if (arg.asInstanceOf[BasicArg].getCat.getTarget.getIndexNominal == cat.getIndexNominal) 
          retval = true
      }
    }
    retval
  }

  def getRuleName(arg:Category, result:Category) = {
	val argA = arg.copy()
	UnifyControl.abstractNominals(argA)
	val resA = result.copy()
	UnifyControl.abstractNominals(resA)
	val numOpt = ruleMap.get((argA,resA))
	if (numOpt == None) {
	  // nb: could use a supertag-based name, but it would not be unique b/c of LF rels
//	  val name = argA.getSupertag + "=>" + resA.getSupertag
	  val num = ruleMap.size
	  val name = "tc"+num
	  val lf = HyloHelper.firstEP(resA.getLF)
	  ruleMap.put((argA,resA), new TypeChangingRule(argA, resA, name, lf))
	  name
	}
	else {
	  numOpt.get.name
	}
  }
  
  // TODO look into vp\vp cats for <Bx?
  def makeRuleInst(headCat:Category, modCat:Category, relPredIdx:Int, rightward:Boolean) = {
	val modCatCopy = modCat.copy()
	modCatCopy.setLF(null)
	UnifyControl.reindex(modCatCopy)
	val headCatCopy = headCat.copy()
	headCatCopy.setLF(null)
	UnifyControl.addIndices(headCatCopy)
	UnifyControl.removeFeatsExcept(headCatCopy, List("index"))
	UnifyControl.reindex(headCatCopy)
    val argCatCopy = headCatCopy.copy()
    val args = if (headCatCopy.isInstanceOf[ComplexCat]) 
      headCatCopy.asInstanceOf[ComplexCat].getArgStack() 
      else new ArgStack()
    val slash = if (rightward) new Slash('/',">") else new Slash('\\',"<")
    args.add(new BasicArg(slash, argCatCopy))
	val pred = preds(relPredIdx)
	val lf = pred.copy()
    val resultCat = if (headCatCopy.isInstanceOf[ComplexCat]) {
      headCatCopy.setLF(lf)
      headCatCopy.asInstanceOf[ComplexCat]      
    }
    else {
      new ComplexCat(headCatCopy.asInstanceOf[TargetCat], args, lf)
    }
	val name = getRuleName(modCatCopy, resultCat)
	val unaryRule = new TypeChangingRule(modCatCopy, resultCat, name, lf)
	val bitset = new BitSet(preds.size())
	bitset.set(relPredIdx)
	val retval = edgeFactory.makeRuleInstance(unaryRule, bitset)
	if (headCatCopy.isInstanceOf[ComplexCat]) {
	  val modCat2 = modCatCopy.copy()
	  UnifyControl.reindex(modCat2)
	  val headCat2 = headCatCopy.getTarget().copy()
	  UnifyControl.reindex(headCat2)
	  val argCat2 = headCat2.copy()
	  val args2 = new ArgStack()
	  val slash2 = slash.copy()
	  args2.add(new BasicArg(slash2, argCat2))
	  val lf2 = lf.copy()
	  val resultCat2 = new ComplexCat(headCat2.asInstanceOf[TargetCat], args2, lf2)
	  val name2 = getRuleName(modCat2, resultCat2)
	  val unaryRule2 = new TypeChangingRule(modCat2, resultCat2, name2, lf2)
	  val bitset2 = bitset.clone().asInstanceOf[BitSet]
	  val retval2 = edgeFactory.makeRuleInstance(unaryRule2, bitset2)
	  (retval, new Some(retval2))
	}
	else (retval, None)
  }
  
  // NB: in principle it would be better to add inheritsFrom to target cat
  // TODO add composition case
  def extendInvCat(headCat:Category, invCat:AtomCat, relPredIdx:Int, rightward:Boolean) = {
	val invCatCopy = invCat.copy()
	UnifyControl.reindex(invCatCopy)
	val lf = invCatCopy.getLF
	invCatCopy.setLF(null)
	val headCatCopy = headCat.copy()
	headCatCopy.setLF(null)
	UnifyControl.addIndices(headCatCopy)
	UnifyControl.reindex(headCatCopy)
    val args = new ArgStack()
	if (headCat.isInstanceOf[ComplexCat]) {
	  val headCatCopyArgs = headCatCopy.asInstanceOf[ComplexCat].getArgStack
	  args.add(headCatCopyArgs.copy)
	}
    if (rightward) args.add(new BasicArg(new Slash('/',"^"), headCatCopy))
    else args.add(new BasicArg(new Slash('\\',"^"), headCatCopy))
    invCatCopy.asInstanceOf[TargetCat].getFeatureStructure()
    val retcat = new ComplexCat(invCatCopy.asInstanceOf[TargetCat], args, lf)
	retcat
  }

  def makeEdge(cat:Category, sign:Sign, bitset:BitSet) = {
    // replace supertag in word 
    // nb: assuming a single word, as usual
    val word = sign.getWords.get(0)
    val stWord = Word.createFullWord(word, word.getStem, word.getPOS, cat.getSupertag, word.getSemClass) 
    // make new sign, updating lex sem origins
    val stSign = new Sign(stWord, cat)
    stSign.setOrigin()
    // make edge with new sign
    edgeFactory.makeEdge(stSign, bitset.clone().asInstanceOf[BitSet])
  }
  
  def makeAndAddEdges(cats:(Category,Option[Category]), sign:Sign, bitset:BitSet, buf:ListBuffer[Edge]) = {
    val (cat, catOpt) = cats
    val edge = makeEdge(cat, sign, bitset) 
    buf += edge
    if (catOpt != None) {
      val edge2 = makeEdge(catOpt.get, sign, bitset) 
	  buf += edge2
    }
  }
  
  def isArgRel(rel:String) = { Config.argrels.contains(rel) || Config.lexrels.contains(rel) }
  def isInvRel(rel:String) = { Config.invrels.contains(rel) }
  def isModRel(rel:String)  = {
    (!isArgRel(rel) && !isInvRel(rel)) || Config.optrels.contains(rel) 
  }
  
  def combineEdges(node:TreeNode):Unit = {
    
    // recurse
//    out.println("starting node: " + node)
    for (kid <- node.kids) combineEdges(kid)
//    out.println("continuing node: " + node)
    
    // TODO consider treating all secondary deps as args, in order to handle eg "in which" in 2402
    val argDeps = for (dep <- node.deps if isArgRel(dep._1)) yield dep 
    val invDeps = for (dep <- node.deps if isInvRel(dep._1)) yield dep 
    val modDeps = for (dep <- node.deps if isModRel(dep._1)) yield dep 
    val coordDeps = for (dep <- node.deps if dep._1 == Config.firstConjRel || dep._1 == Config.secondConjRel) yield dep
    if (coordDeps.size == 2 && argDeps.isEmpty) {
      val coord1Dep = coordDeps.get(0)
      val coord1DepKid = node.kids.find(kid => kid.id == coord1Dep._2).get
      val coord1PredIdx = relPredIdxs(node.id).get(coord1Dep).get
      val coord2Dep = coordDeps.get(1)
      val coord2PredIdx = relPredIdxs(node.id).get(coord2Dep).get
      // nb: assuming there is a lexical conj edge whose LF and bitset can be borrowed
      val lexConjEdge = edgesById(node.id).filter(edge => edge.getSign.getCategory.isInstanceOf[AtomCat]).get(0)
      for (coord1DepEdge <- edgesById(coord1DepKid.id) if !coord1DepEdge.getSign.isTypeRaised) {
        val coord1DepCat = coord1DepEdge.getSign.getCategory
        val coordEdge = makeConjCoordEdge(lexConjEdge, node.id, coord1DepCat, coord1PredIdx, coord2PredIdx, coord1Dep, coord2Dep)
        out.println("new coord cat needed: " + coordEdge)
        edgesById(node.id) += coordEdge
      }
    }
    else if (coordDeps.size > 0) {
      out.println("**warning**, unexpected number of coord or arg deps!")
      out.println("# coordDeps: " + coordDeps.size)
      out.println("# argDeps: " + argDeps.size)
    }
    
    if (!argDeps.isEmpty) {
      val withArgs = new ListBuffer[Edge]()
      for (edge <- edgesById(node.id)) withArgs += edge
      for (dep <- argDeps.reverse) {
        val rightward = dep._2 > node.id
        val withNext = ListBuffer[Edge]()
        if (Config.optrels.contains(dep._1)) withNext ++= withArgs // keep current ones if rel is optional
        val nextKid = node.kids.find(kid => kid.id == dep._2)
        val relPredIdx = relPredIdxs(node.id).get(dep).get
        for (edge <- withArgs if !edge.bitset.get(relPredIdx)) {
          val nextBitSet = edge.bitset.clone().asInstanceOf[BitSet]
          nextBitSet.set(relPredIdx)
          val sign = edge.getSign
          if (nextKid.isEmpty) {
            // nb: don't actually know whether this should be right or left! (trying both)
            val nextCatLs = extendCat(sign.getCategory, Config.defaultcat, dep, false) 
            makeAndAddEdges(nextCatLs, sign, nextBitSet, withNext)
            val nextCatRs = extendCat(sign.getCategory, Config.defaultcat, dep, true) 
            makeAndAddEdges(nextCatRs, sign, nextBitSet, withNext)
          }
          else {
            for (next <- edgesById(nextKid.get.id) if !next.getSign.getDerivationHistory.ruleIsTypeRaising) {
              val nextCat = next.getSign.getCategory
              val nextCatType = if (nextCat.isInstanceOf[AtomCat]) 
                nextCat.asInstanceOf[AtomCat].getType else null
              if (!Config.nonargcats.contains(nextCatType)) {
                val nextCats = extendCat(sign.getCategory, nextCat, dep, rightward)
                makeAndAddEdges(nextCats, sign, nextBitSet, withNext)
              }
		    }
		  }
		}
        withArgs.clear()
        withArgs ++= withNext
      }
      // add coindexes
      for ((withArg,i) <- withArgs.zipWithIndex) {
        val cat = withArg.getSign.getCategory
        val coCat = coindexCat(cat)
        if (!(cat eq coCat)) {
          val nextSign = new Sign(withArg.getSign.getWords, coCat)
          val nextEdge = edgeFactory.makeEdge(nextSign, withArg.bitset.clone().asInstanceOf[BitSet])
          withArgs(i) = nextEdge
        }
        out.println("new cat with args needed: " + withArgs(i))
      }
      // update edges
      edgesById(node.id) = withArgs
    }

    // combine edges
    val chart = new ListBuffer[EdgeSpan]()
    var agenda = new ListBuffer[EdgeSpan]()
    
    val ids = (node.id :: node.kids.map(_.id)).sorted
    def adjacent(id1:Int, id2:Int) = { 
      val idx = ids.indexOf(id1); 
      idx >= 0 && idx < ids.length-1 && ids(idx+1) == id2 
    }
    
    agenda ++= edgesById(node.id).map(EdgeSpan(_, (node.id,node.id)))
    for (kid <- node.kids) agenda ++= edgesById(kid.id).map(EdgeSpan(_, (kid.id,kid.id)))
    
    // TODO silly to not use log directly
    def orderAgendaBy(es:EdgeSpan) = {
      // check whether to use log
      val score = if (Config.log_sort) Math.log(es.edge.score) else es.edge.score;
      -1.0 * score / es.edge.completeness
    }
    agenda = agenda.sortBy(orderAgendaBy(_))
    
    // applies the rule instance and adds the results to the agenda
    def applyRuleInst(ruleInst:RuleInstance, next:EdgeSpan) = {
      val signs = Array(next.edge.getSign)
	  val results = new ListBuffer[Sign]()
	  ruleInst.rule.applyRule(signs, results)
	  for (result <- results) {
	    val bitset = ruleInst.bitset.clone().asInstanceOf[BitSet]
		bitset.or(next.edge.bitset)
		val newEdge = edgeFactory.makeEdge(result,bitset) 
		out.println("derived: " + newEdge)
		out.println("from rule inst: " + ruleInst)
//		out.println("and: " + next.edge)
		agenda += EdgeSpan(newEdge, (next.span._1,next.span._2))
	  }
    }
    
    // map for handling representative edges
    val catMap = Chart.createCatMap()
    
    // adds the edge span to the chart, returning true if it's actually been added, false if 
    // folded into a representative edge
    def addEdgeToChart(next:EdgeSpan) = {
      // get representative edge for this edge
      val edge = next.edge
      val repEdge = catMap.get(edge)
      // check for same edge already in chart; pretend it's been added
      // nb: not sure if this ever actually happens
      if (edge eq repEdge) {
        out.println("whoa, identical repEdge for: " + edge)
        true
      }
      // if none, make this edge into one, adding it to the chart
      else if (repEdge eq null) {
        // nb: alts can already be initialized from a recursive subchart
        if (!edge.isRepresentative()) edge.initAltEdges()
        catMap.put(edge, edge)
        chart += next
        true
      }
      // otherwise add edge to alts (nb: currently unsorted)
      else {
        out.println("folding: " + edge)
//        out.println("into: " + repEdge)
        repEdge.getAltEdges.add(edge)
        false
      }
    }
    
    // combines the given current and next edges, adding to the agenda
    def applyBinaryRules(current:EdgeSpan, next:EdgeSpan, currentFirst:Boolean) = {
      if (current.edge.indicesIntersect(next.edge)) {
        val results = if  (currentFirst)
          generalRules.applyBinaryRules(current.edge.getSign, next.edge.getSign)
		else
		  generalRules.applyBinaryRules(next.edge.getSign, current.edge.getSign)
//		out.println("Trying: \n" + next.edge + " + \n" + current.edge)
		for (result <- results) {
		  val bitset = current.edge.bitset.clone().asInstanceOf[BitSet]
		  bitset.or(next.edge.bitset)
		  val newEdge = edgeFactory.makeEdge(result,bitset) 
		  out.println("derived: " + newEdge)
		  // NB: only combine with type-raised arg if functor also type-raised;  
		  //     this should really be migrated to applyBinaryRules
		  val dh = newEdge.getSign.getDerivationHistory
		  val ruleIsForwards = dh.getRule.name.startsWith(">")
		  val args = dh.getInputs.asInstanceOf[Array[Sign]]
		  val argL = args(0)
		  val argR = args(1)
		  val onlyArgIsTypeRaised = 
		    ((ruleIsForwards && !argL.isTypeRaised && argR.isTypeRaised) ||
		     (!ruleIsForwards && argL.isTypeRaised && !argR.isTypeRaised)) 
		  if (onlyArgIsTypeRaised) {
//		    out.println("skipping, only the arg is type raised with " + dh.getRule.name + "!")
//		    out.println("first: " + argL)
//		    out.println("second: " + argR)
		  }
		  else {
		    val span = if (currentFirst) (current.span._1,next.span._2) else (next.span._1,current.span._2)
		    agenda += EdgeSpan(newEdge, span)		    
		  }
		}
      }
    }
    
    // combines the next edge with those in the chart and does unary rules, 
    // adding the results to the agenda
    def doEdgeCombos(next:EdgeSpan) = {
      val nextContainsHead = (next.span._1 <= node.id && node.id <= next.span._2)
      // do unary rules for head edges
      if (nextContainsHead) {
        // general unary rules
        val results = generalRules.applyUnaryRules(next.edge.getSign)
        for (result <- results if !result.getDerivationHistory.containsCycle) {
          val bitset = next.edge.bitset.clone().asInstanceOf[BitSet]
          val newEdge = edgeFactory.makeEdge(result,bitset)
          out.println("derived: " + newEdge)
          agenda += EdgeSpan(newEdge, next.span)          
        }
        // existing rule instances
        for { ruleInst <- edgeFactory.ruleInstances
        	  if !ruleInst.intersects(next.edge)
    	  	  if ruleInst.indicesIntersect(next.edge) 
    	  	  if next.edge.meetsLfChunkConstraints(ruleInst) }
          applyRuleInst(ruleInst, next)
      }
      // do binary rules, modifier rule instances and new inverted cats
      for { current <- chart 
    	  	if !(current eq next)
    	  	if adjacent(current.span._2, next.span._1) || adjacent(next.span._2, current.span._1)
    	  	if !current.edge.intersects(next.edge) 
    	  	if current.edge.meetsLfChunkConstraints(next.edge)
    	  	if next.edge.meetsLfChunkConstraints(current.edge) 
      } {
        val currentFirst = (current.span._2 < next.span._1)
        // first do binary rules with existing cats
        applyBinaryRules(current, next, currentFirst)
        // create and apply modifier unary rule instances
        val currentContainsHead = (current.span._1 <= node.id && node.id <= current.span._2)
        // ensure that we have a head edge and a mod edge
        if (nextContainsHead ^ currentContainsHead) {
          val headES = if (nextContainsHead) next else current
          val modES = if (nextContainsHead) current else next
          val rightward = (modES == current && currentFirst) || (modES == next && !currentFirst)
          // check whether mod rel already covered
          val modDep = modDeps.find(dep => dep._2 == modES.span._1 && dep._2 == modES.span._2)
          if (modDep != None) {
            val rel = modDep.get._1 
            val relPredIdx = relPredIdxs(node.id).get(modDep.get).get
            val headCat = headES.edge.getSign.getCategory
	        val modCat = modES.edge.getSign.getCategory
            if (!headES.edge.bitset.get(relPredIdx) && !modES.edge.bitset.get(relPredIdx) &&
                !headES.edge.getSign.getDerivationHistory.ruleIsTypeRaising &&
                !modES.edge.getSign.getDerivationHistory.ruleIsTypeRaising &&
                !Config.lexrels.contains(rel))
            {
              val (ruleInst, ruleInst2Opt) = makeRuleInst(headCat, modCat, relPredIdx, rightward)
              applyRuleInst(ruleInst, modES)
//              out.println("new rule needed: " + ruleInst)
              if (ruleInst2Opt != None) {
                val ruleInst2 = ruleInst2Opt.get
                applyRuleInst(ruleInst2, modES)
//                out.println("new rule2 needed: " + ruleInst2)
              }
            }
          }
        }
        // create and combine with cat that has an inverted relation:
        // ensure that we have a head edge and an inverted edge
        if (nextContainsHead ^ currentContainsHead) {
          val headES = if (nextContainsHead) next else current
          val invES = if (nextContainsHead) current else next
          val rightward = (invES == current && currentFirst) || (invES == next && !currentFirst)
          // find inv rel
          val invDep = invDeps.find(dep => dep._2 == invES.span._1 && dep._2 == invES.span._2)
          if (invDep != None) {
            val relPredIdx = relPredIdxs(node.id).get(invDep.get).get
            val headCat = headES.edge.getSign.getCategory
	        val invCat = invES.edge.getSign.getCategory
	        val headCatType = if (headCat.isInstanceOf[AtomCat])
	          headCat.asInstanceOf[AtomCat].getType else null
            if (!headES.edge.bitset.get(relPredIdx) && invES.edge.bitset.get(relPredIdx) &&
                !headES.edge.getSign.getDerivationHistory.ruleIsTypeRaising &&
                !invES.edge.getSign.getDerivationHistory.ruleIsTypeRaising &&
                invCat.isInstanceOf[AtomCat] && !Config.nonargcats.contains(headCatType))
            {
              // TODO - handle non-lexical cases involving MWEs
              if (invES.edge.getSign.isLexical) {
                val invCatAC = invCat.asInstanceOf[AtomCat]
        		val extendedCat = extendInvCat(headCat, invCatAC, relPredIdx, rightward)
//        		out.println("new inverted cat needed: " + extendedCat)
        		val newSign = new Sign(invES.edge.getSign.getWords, extendedCat)
                val bitset = invES.edge.bitset.clone().asInstanceOf[BitSet]
        		val newEdge = edgeFactory.makeEdge(newSign,bitset)
        		out.println("new inverted cat needed: " + newEdge)
        		val newES = EdgeSpan(newEdge, invES.span)
        		val actuallyAdded = addEdgeToChart(newES)
        		if (actuallyAdded) {
        		  applyBinaryRules(newES, headES, rightward)
        		}
              }
              else {
                out.println("*** warning, new inverted cat needed for non-lexical edge ***")
                out.println(invES.edge)
              }
            }
          }
        }    
      }
    }
    
    while (!agenda.isEmpty) {
      // take edge from agenda
      val next = agenda.remove(0)
      // add edge to chart
	  val actuallyAdded = addEdgeToChart(next)
	  // do combos unless folded into an existing edge 
	  if (actuallyAdded) {
	    doEdgeCombos(next)
	    // re-sort and prune
	    agenda = agenda.sortBy(orderAgendaBy(_))
	    if (agenda.size > Config.agenda_max) {
	      agenda = agenda.take(Config.agenda_max)
	    }
	  }
    }
    
    // filter chart to edges intersecting head edges
    val filteredChart = chart.filter(next => edgesById(node.id).exists(edge => edge.bitset.intersects(next.edge.bitset)))
    
    // nb: update node edges to chart edges with coverage within N (eg 2) of max, 
    // to allow for some optional bits (eg just det by itself instead of np/n, which has nmod rel)
    val maxcov = filteredChart.foldLeft(0)((m,entry) => m max entry.edge.bitset.cardinality)
    val filteredByCov = filteredChart.filter(entry => entry.edge.bitset.cardinality >= maxcov-2).map(_.edge)
    edgesById(node.id) = filteredByCov.sortBy(-1*_.score).take(Config.node_max)
  }
    
}
