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

object Induce extends App {
  
  val log_fn = "/Users/mwhite/dev/hmmm/scala/convert_deps/out/instcats_log.txt"
  println("logging to " + log_fn)
  val out = new PrintWriter(new FileWriter(log_fn))
  
  out.println("loading initial grammar")
  val grammar_fn = "/Users/mwhite/dev/hmmm/scala/convert_deps/out/grammar.xml"
  val grammar = new Grammar(grammar_fn)
  out.println()

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

  val tb_fn = "/Users/mwhite/dev/hmmm/scala/convert_deps/out/testbed.xml"
  out.println("loading testbed")
  val rinfo = new RegressionInfo(grammar, new File(tb_fn))
  out.println("read " + rinfo.numberOfItems() + " items")
  
  val item0 = rinfo.getItem(0)
//  val item0 = rinfo.getItem(5) // 14
  val fullwords = item0.fullWords;
  out.println("full words for first item:")
  out.println(fullwords)
  val words = grammar.lexicon.tokenizer.tokenize(item0.fullWords, true)
  // strip <s> and </s>
  if (words.get(0).getForm == "<s>") words.remove(0);
  if (words.get(words.size()-1).getForm() == "</s>") words.remove(words.size()-1);
  out.println("tagged words:")
  for (word <- words) {
    out.print(word.getForm + "/" + word.getPOS + " ")
  }
  out.println()
  
  out.println("lf for first item:")
  val doc = new Document()
  val lfElt = item0.lfElt
  lfElt.detach()
  doc.setRootElement(lfElt)
  val lf = grammar.loadLF(doc)
  out.println(lf.prettyPrint("  "))
  out.println()

  out.println("EPs for first item:")
  val flattener = new Flattener()
  val preds = flattener.flatten(lf)
  HyloHelper.sort(preds)
  val edgeFactory = new EdgeFactory(grammar, preds, SignScorer.nullScorer)
  for ((pred,i) <- preds.zipWithIndex) out.println(i + ": " + pred)
  out.println()

  out.println("highest parents for first item:")
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
  
  // nb: perhaps better as a real class?
  case class TreeNode(id:Int, deps:List[(String,Int)], kids:List[TreeNode])
  
  def makeTreeNode(id:Int):TreeNode = {
    val kidIds = parentIds.indices.filter(parentIds(_) == id) 
    val deps = relPredIdxs(id).keys.toList.sortBy(_._2)
    val kids = (kidIds.map(makeTreeNode(_))).toList.sortBy(_.id)
    TreeNode(id, deps, kids)
  }
  
  def printTree(node:TreeNode, indent:String="", rel:String="root"):Unit = {
    out.println(indent + "-" + rel + "-> " + node.id + " " + words.get(node.id))
    for (dep <- node.deps if !node.kids.exists(kid => kid.id == dep._2)) 
      out.println(indent + "  -" + dep._1 + "-> " + dep._2)
    for (kid <- node.kids) 
      printTree(kid, indent+"  ", node.deps.find(dep => dep._2 == kid.id).get._1)
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
    
  // nb: could allow for some optional argrels, eg prp?
  val argrels = List("sbj","obj","vc","prd","oprd","im","prp","pmod")
  val defaultcat = new AtomCat("np")
  
  case class EdgeSpan(edge:Edge, span:(Int,Int))
  
  // adds arg cats to back or front of arg stack depending on rightward flag
  // if arg cat has arity > 1, also uses result of arg cat to allow 
  // for use of composition, eg 'to' s[to]\np/(s[b]\np) for arg 'see' s[b]\np/np 
  // TODO special treatment for coord to enforce like categories
  // nb: special treatment of balanced punctuation may be needed?
  def extendCat(cat:Category, argCat:Category, dep:(String,Int), rightward:Boolean) = {
    val (rel, id) = dep
    val catCopy = cat.copy()
    val args = if (catCopy.isInstanceOf[ComplexCat]) 
      catCopy.asInstanceOf[ComplexCat].getArgStack() 
      else new ArgStack()
    val argCatCopy = argCat.copy()
    argCatCopy.setLF(null)
    argCatCopy.getTarget().getFeatureStructure().setFeature("index", new NominalAtom("w"+id))
    if (rightward) args.add(new BasicArg(new Slash('/',">"), argCatCopy))
    else args.addFront(new BasicArg(new Slash('\\',"<"), argCatCopy))
    val headNom = catCopy.getIndexNominal().copy().asInstanceOf[Nominal]
    val depRel = new Diamond(new ModeLabel(rel), new NominalAtom("w"+id))
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
      val args2 = retcat2.getArgStack()
      val argCat2 = argCatCopy.asInstanceOf[ComplexCat].getResult()
      if (rightward) args2.setLast(new BasicArg(new Slash('/',">"), argCat2))
      else args2.set(0, new BasicArg(new Slash('\\',"<"), argCat2))
      (retcat, Some(retcat2))
    }
    else (retcat, None)
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
                if (argArgCat.isInstanceOf[AtomCat] && !hasArgWithTargetIndex(args, argArgCat))
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

  // map to track unique unary rules
  val ruleMap = HashMap[(Category,Category),TypeChangingRule]()
  
  def getRuleName(arg:Category, result:Category) = {
	val argA = arg.copy()
	UnifyControl.abstractNominals(argA)
	val resA = result.copy()
	UnifyControl.abstractNominals(resA)
	val numOpt = ruleMap.get((argA,resA))
	if (numOpt == None) {
	  val num = ruleMap.size
	  val name = "tc"+num
	  val lf = HyloHelper.firstEP(resA.getLF)
	  ruleMap.put((argA,resA), new TypeChangingRule(argA, resA, name, lf))
	  name
	}
	else numOpt.get.name
  }
  
  def makeRuleInst(headCat:Category, modCat:Category, relPredIdx:Int, rightward:Boolean) = {
	val modCatCopy = modCat.copy()
	modCatCopy.setLF(null)
	val headCatCopy = headCat.copy()
	headCatCopy.setLF(null)
	UnifyControl.addIndices(headCatCopy)
	UnifyControl.removeFeatsExcept(headCatCopy, List("index"))
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
	  val headCat2 = headCatCopy.getTarget().copy()
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
  
  def makeAndAddEdges(cats:(Category,Option[Category]), sign:Sign, bitset:BitSet, buf:ListBuffer[Edge]) = {
    val (cat, catOpt) = cats
    val edge = edgeFactory.makeEdge(new Sign(sign.getWords, cat), bitset.clone().asInstanceOf[BitSet])
    buf += edge
    if (catOpt != None) {
      val edge2 = edgeFactory.makeEdge(new Sign(sign.getWords, catOpt.get), bitset.clone().asInstanceOf[BitSet])
	  buf += edge2
    }
  }
  
  def combineEdges(node:TreeNode):Unit = {
    
    // recurse
//    out.println("starting node: " + node)
    for (kid <- node.kids) combineEdges(kid)
//    out.println("continuing node: " + node)
    
    // TODO consider treating all secondary deps as args, in order to handle eg "in which" in 2402
    val argDeps = for (dep <- node.deps if argrels.contains(dep._1)) yield dep
    val modDeps = for (dep <- node.deps if !argrels.contains(dep._1)) yield dep
    
    if (!argDeps.isEmpty) {
      val withArgs = new ListBuffer[Edge]()
      for (edge <- edgesById(node.id)) withArgs += edge
      for (dep <- argDeps) {
        val rightward = dep._2 > node.id
        val withNext = ListBuffer[Edge]()
        val nextKid = node.kids.find(kid => kid.id == dep._2)
        val relPredIdx = relPredIdxs(node.id).get(dep).get
        for (edge <- withArgs if !edge.bitset.get(relPredIdx)) {
          val nextBitSet = edge.bitset.clone().asInstanceOf[BitSet]
          nextBitSet.set(relPredIdx)
          val sign = edge.getSign
          if (nextKid.isEmpty) {
            // nb: don't actually know whether this should be right or left! (trying both)
            val nextCatLs = extendCat(sign.getCategory, defaultcat, dep, false) 
            makeAndAddEdges(nextCatLs, sign, nextBitSet, withNext)
            val nextCatRs = extendCat(sign.getCategory, defaultcat, dep, true) 
            makeAndAddEdges(nextCatRs, sign, nextBitSet, withNext)
          }
          else {
            for (next <- edgesById(nextKid.get.id) if !next.getSign.getDerivationHistory.ruleIsTypeRaising) {
              val nextCats = extendCat(sign.getCategory, next.getSign.getCategory, dep, rightward)
    		  makeAndAddEdges(nextCats, sign, nextBitSet, withNext)
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
    val agenda = new ListBuffer[EdgeSpan]()
    
    val ids = (node.id :: node.kids.map(_.id)).sorted
    def adjacent(id1:Int, id2:Int) = { 
      val idx = ids.indexOf(id1); 
      idx >= 0 && idx < ids.length-1 && ids(idx+1) == id2 
    }
    
    agenda ++= edgesById(node.id).map(EdgeSpan(_, (node.id,node.id)))
    for (kid <- node.kids) agenda ++= edgesById(kid.id).map(EdgeSpan(_, (kid.id,kid.id)))

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
		agenda.prepend(EdgeSpan(newEdge, (next.span._1,next.span._2)))
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
          agenda.prepend(EdgeSpan(newEdge, next.span))          
        }
        // existing rule instances
        for { ruleInst <- edgeFactory.ruleInstances
        	  if !ruleInst.intersects(next.edge)
    	  	  if ruleInst.indicesIntersect(next.edge) 
    	  	  if next.edge.meetsLfChunkConstraints(ruleInst) }
          applyRuleInst(ruleInst, next)
      }
      // do binary rules and modifier rule instances
      for { current <- chart 
    	  	if !(current eq next)
    	  	if adjacent(current.span._2, next.span._1) || adjacent(next.span._2, current.span._1)
    	  	if !current.edge.intersects(next.edge) 
    	  	if current.edge.meetsLfChunkConstraints(next.edge)
    	  	if next.edge.meetsLfChunkConstraints(current.edge) 
      } {
        val currentFirst = (current.span._2 < next.span._1)
        if (current.edge.indicesIntersect(next.edge)) {
          val results = if  (currentFirst)
            generalRules.applyBinaryRules(current.edge.getSign, next.edge.getSign)
          else
        	generalRules.applyBinaryRules(next.edge.getSign, current.edge.getSign)
//          out.println("Trying: \n" + next.edge + " + \n" + current.edge)
          for (result <- results) {
            val bitset = current.edge.bitset.clone().asInstanceOf[BitSet]
            bitset.or(next.edge.bitset)
            val newEdge = edgeFactory.makeEdge(result,bitset) 
            out.println("derived: " + newEdge)
            val span = if (currentFirst) (current.span._1,next.span._2) else (next.span._1,current.span._2)
            agenda.prepend(EdgeSpan(newEdge, span))
          }
        }
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
            val relPredIdx = relPredIdxs(node.id).get(modDep.get).get
            if (!headES.edge.bitset.get(relPredIdx) && !modES.edge.bitset.get(relPredIdx) &&
                !headES.edge.getSign.getDerivationHistory.ruleIsTypeRaising &&
                !modES.edge.getSign.getDerivationHistory.ruleIsTypeRaising) 
            {
	          val modCat = modES.edge.getSign.getCategory
	          val headCat = headES.edge.getSign.getCategory
              val (ruleInst, ruleInst2Opt) = makeRuleInst(headCat, modCat, relPredIdx, rightward)
              applyRuleInst(ruleInst, modES)
//              out.println("new rule needed: " + ruleInst)
              if (ruleInst2Opt != None) {
                val ruleInst2 = ruleInst2Opt.get
                applyRuleInst(ruleInst2, modES)
                out.println("new rule2 needed: " + ruleInst2)
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
	  if (actuallyAdded) doEdgeCombos(next)
    }
    
    // filter chart to edges intersecting head edges
    val filteredChart = chart.filter(next => edgesById(node.id).exists(edge => edge.bitset.intersects(next.edge.bitset)))
    
    // nb: update node edges to chart edges with coverage within N (eg 2) of max, 
    // to allow for some optional bits (eg just det by itself instead of np/n, which has nmod rel)
    val maxcov = filteredChart.foldLeft(0)((m,entry) => m max entry.edge.bitset.cardinality)
    edgesById(node.id) = filteredChart.filter(entry => entry.edge.bitset.cardinality >= maxcov-2).map(_.edge)
  }

  out.println("resetting counters")
  UnifyControl.startUnifySequence()
  out.println()

  out.println("trying edge combos:")
  for (root <- primaryTrees)
    combineEdges(root)
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

//  out.println("chunks for first item:")
//  for (chunk <- edgeFactory.lfChunks) out.println(Edge.toString(chunk))

//  out.println("inferred rules: " + ruleMap.size)
//  out.println(ruleMap)
  
  // TODO abstract cats in best deriv lex items, save these and deriv's rules

  val rules_fn = "/Users/mwhite/dev/hmmm/scala/convert_deps/out/induced/rules.xml"
  out.println("saving rules to " + rules_fn)
  val updatedRules = new RuleGroup(grammar)
  for (rule <- grammar.rules.getBinaryRules) updatedRules.addRule(rule)
  for (rule <- grammar.rules.getUnaryRules if !rule.isInstanceOf[TypeChangingRule]) updatedRules.addRule(rule)
  for (rule <- grammar.rules.getUnaryRules if rule.isInstanceOf[TypeChangingRule]) updatedRules.addRule(rule)
  val numExistingUnaries = updatedRules.getUnaryRules.size
  val bestRules = HashSet[String]()
  def getTCRs(sign:Sign):Unit = {
    val hist = sign.getDerivationHistory
    if (!hist.isEmpty) {
      val rule = hist.getRule
	  if (rule.isInstanceOf[TypeChangingRule]) bestRules += rule.name()
	  for (child <- hist.getInputs) getTCRs(child)
    }
  }
  getTCRs(rzchart.bestEdge.getSign)
  for (rule <- ruleMap.values if bestRules.contains(rule.name)) updatedRules.addRule(rule)
  val numRules = updatedRules.getBinaryRules.size + updatedRules.getUnaryRules.size 
  updatedRules.toXml(rules_fn)
  out.println("saved " + numRules + " rules")
  
  val lexicon_fn = "/Users/mwhite/dev/hmmm/scala/convert_deps/out/induced/lexicon.xml"
  out.println("saving categories to " + lexicon_fn)
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
  getLexItems(rzchart.bestEdge.getSign)
  val cats = ListBuffer[Category]()
  val tags = ListBuffer[String]()
  for ((cat,pos) <- bestLexCats) {
    cats += cat
    tags += pos
  }
  grammar.toLexiconXml(cats, tags, lexicon_fn)
  out.println("saved " + cats.size + " cats")

  val morph_fn = "/Users/mwhite/dev/hmmm/scala/convert_deps/out/induced/morph.xml"
  out.println("saving words to " + morph_fn)
  val wordList = ListBuffer[Word]() ++ bestWords
  grammar.toMorphXml(wordList, morph_fn)
  out.println("saved " + wordList.size + " words")
  out.println()

  out.flush()
  out.close()
  println("done.")
}