package opennlp.ccg.induce

import scala.collection.generic.Sorted
import scala.collection.JavaConversions._
import scala.collection.mutable._
import java.io._
import java.util.BitSet
import org.jdom._
import opennlp.ccg.hylo._
import opennlp.ccg.synsem._
import opennlp.ccg.grammar._
import opennlp.ccg.test._
import opennlp.ccg.realize._
import opennlp.ccg.perceptron._
import opennlp.ccg.ngrams._
//import opennlp.ccgbank.extract.Testbed


object TrainRealizationRanker extends App {

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

  val log_fn = working_dir + "/logs/train_rz_ranker." + partition + ".log"
  println("logging to " + log_fn)
  val out = new PrintWriter(new FileWriter(log_fn))

  val grammarsdir = working_dir + "/grammars" 
  out.println("loading " + partition + " grammar from " + grammarsdir)
  out.println("(assumes symlinks in place)")
  val grammar_fn = grammarsdir + "/grammar.xml"
  val grammar = new Grammar(grammar_fn)
  out.println()

  // set up model 
  // TODO add n-gram model
  val alphabet = new Alphabet(0)
  val model = new Model(alphabet)
  val featureExtractor = new ComposedFeatureExtractor(new SyntacticFeatureExtractor(), new LexDepFeatureExtractor())
  val perceptronScorer = new PerceptronScorer(featureExtractor, model)
  val scorers = Array(SignScorer.complexityScorer, perceptronScorer)
  val scorerWeights = Array(1.0, 1.0)
  val signScorer = new SignScorerInterpolation(scorers, scorerWeights)

  out.println("instantiating realizer")
  val realizer = new Realizer(grammar)
  realizer.signScorer = signScorer
  out.println()
  
  def getLF(item:RegressionInfo.TestItem) = {
    val lfElt = item.lfElt
    val doc = new Document()
    lfElt.detach()
    doc.setRootElement(lfElt)
    grammar.loadLF(doc)
  }
  
  def findBestGoldRealization(lf:LF, target:String) = {
    val targetScorer = new SignScorer.TargetScorer(target, signScorer)
    realizer.realize(lf, targetScorer)
    val chart = realizer.getChart
    chart.bestEdge
  }
  
  // update model on file
  def trainFile(fileid:String, srcdir:String, outdir:String) = {
    // load testbed
    val tb_fn = srcdir + "/" + fileid + ".xml"
    out.println("***** loading testbed " + fileid)
    print('.')
    val rinfo = new RegressionInfo(grammar, new File(tb_fn))
    out.println("read " + rinfo.numberOfItems() + " items")
    out.println()
    // do each item
    for (itemno <- 0 until rinfo.numberOfItems) {
      val item = rinfo.getItem(itemno)
      out.println("item " + itemno + ": " + item.sentence)
      out.println()
      try {
        val lf = getLF(item)
        // find gold
        val goldEdge = findBestGoldRealization(lf, item.sentence)
        out.println("gold best realization: " + goldEdge.getSign.getOrthography)
        out.println("complete: " + goldEdge.complete)
        out.println("complexity: " + goldEdge.getSign.getDerivationHistory.complexity)
        out.println("score: " + goldEdge.score)
        out.println(goldEdge.getSign.getDerivationHistory)
        val goldFeats = featureExtractor.extractFeatures(goldEdge.getSign, goldEdge.complete)
//        out.println(new FeatureList(goldFeats))
        out.println()
        // find model best
        // TODO - early update
        realizer.realize(lf)
        val chart = realizer.getChart
        val bestEdge = chart.bestEdge
        out.println("model best realization: " + bestEdge.getSign.getOrthography)
        out.println("complete: " + bestEdge.complete)
//        out.println(bestEdge)
        out.println("complexity: " + bestEdge.getSign.getDerivationHistory.complexity)
        out.println("score: " + bestEdge.score)
        out.println(bestEdge.getSign.getDerivationHistory)
        val bestFeats = featureExtractor.extractFeatures(bestEdge.getSign, bestEdge.complete)
//        out.println(new FeatureList(bestFeats))
        out.println()
        // TODO - loss-sensitive over n-best
        out.println("updating model")
        model.add(goldFeats)
        model.subtract(bestFeats)
        val model_fn = working_dir + "/logs/model.txt"
        out.println("saving model to " + model_fn)
        model.save(model_fn)
        out.println()
      }
      catch {
        case thrwbl:Throwable => thrwbl.printStackTrace(out)
      }
    }
    out.println()
  }
  
  // do each epoch
  for (epoch <- 0 to 2) {
    out.println("*** starting epoch " + epoch)
    out.println()
    // do each section
    for (sect <- sects) {
      val srcdir = working_dir + "/converted/" + sect
	  out.println("loading files from " + srcdir)
	  val outdir = working_dir + "/induced/" + sect
	  out.println("writing files to " + outdir)
	  new File(outdir).mkdirs()
	  out.println()
	  val inputnames = new File(srcdir).listFiles.map(_.getName).filter(_.endsWith(".xml"))
	  val inputids = inputnames.map(fn => fn.substring(0, fn.lastIndexOf(".")))
	  for (fileid <- inputids) {
	    trainFile(fileid, srcdir, outdir)
	  }
      println()
      out.println()
    }
  }
  
  out.flush()
  out.close()
  println("done")
}