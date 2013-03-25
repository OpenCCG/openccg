///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004 University of Edinburgh (Michael White)
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////
package opennlp.ccg.test;

import opennlp.ccg.grammar.*;
import opennlp.ccg.realize.*;
import opennlp.ccg.synsem.SignScorer;
import opennlp.ccg.ngrams.*;

import java.io.*;
import java.net.*;
import java.util.*;
//import java.util.prefs.*;
//import java.text.NumberFormat;

import org.jdom.*;
import org.jdom.input.*;

/**
 * Runs cross-validation tests with the realizer.
 *
 * @author  Michael White
 * @version $Revision: 1.30 $, $Date: 2011/03/20 20:11:58 $
 */
public class CrossValidateRealizer implements ScorerMaker {

    /** The tester to use. */
    public Regression tester = null;

    /** The path to the tmp dir. */
    public String tmpdir = "tmp";
    
    // the actual tmp dir
    private File tmpDir = null;
    
    /** The number of cross-validation folds, either 1.x or an int of at least 2. */
    public double numFolds = 10;

    /** The scorer maker, for preparing and loading scoring models. */
    public ScorerMaker scorerMaker = this;
    
    /** The pruning strategy, if any. */
    public PruningStrategy pruningStrategy = null;
    
    
    /** Sets up the tester with the given grammar. */
    public CrossValidateRealizer(URL grammarURL) throws IOException {

        // init tester
        tester = new Regression();
        
        // load grammar
        System.out.println("Loading grammar from URL: " + grammarURL);
        tester.grammar = new Grammar(grammarURL);
        System.out.println();
    }

        
    /** Sets up the folds in tmpdir. */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setupInputs(URL testbedURL) throws IOException {
        
        // ensure tmpdir exists
        tmpDir = new File(tmpdir);
        if (!tmpDir.exists()) { tmpDir.mkdirs(); }
        
        try {
            // load items
            System.out.println("Loading testbed from URL: " + testbedURL);
            SAXBuilder builder = new SAXBuilder();
            Document inputDoc = builder.build(testbedURL.openStream());
            System.out.println();
            Element inputRoot = inputDoc.getRootElement();
            List inputItems = inputRoot.getChildren("item");
            
            System.out.println("Setting up inputs in tmpdir: " + tmpdir);
            
            // make, save shuffled doc
            Document shuffledDoc = new Document();
            Element shuffledRoot = new Element("regression");
            shuffledDoc.setRootElement(shuffledRoot);
            Random rand = new Random();
            while (inputItems.size() > 0) {
                Element rItem = (Element) inputItems.remove(rand.nextInt(inputItems.size()));
                shuffledRoot.addContent(rItem);
            }
            FileOutputStream shuffledOut = new FileOutputStream(new File(tmpDir, "shuffled.xml")); 
            tester.grammar.serializeXml(shuffledDoc, shuffledOut);
            shuffledOut.close();
            List shuffledItems = shuffledRoot.getChildren("item");
            int numItems = shuffledItems.size();
            Element[] shuffledItemsArray = new Element[numItems];
            shuffledItems.toArray(shuffledItemsArray); // need a non-live listing
            
            // get LF listing
            Element[] shuffledLFsArray = new Element[numItems];
            for (int i = 0; i < numItems; i++) {
                Element item = shuffledItemsArray[i];
                shuffledLFsArray[i] = item.getChild("lf");
                // reduce content to just full-words (if present)
                Element fullWords = item.getChild("full-words");
                item.setContent((List)null);
                if (fullWords != null) item.addContent(fullWords);
            }
            
            // make folds docs, leaving LFs out of training, and 
            // removing any exact duplicates with test items 
            int itemsPerFold = (int) Math.floor(numItems / (numFolds * 1.0));
            Set<String> testStrings = new HashSet<String>();
            int dups = 0;
            for (int i = 0; i < numFolds; i++) {
                testStrings.clear(); dups = 0;
                int foldStart = i * itemsPerFold;
                int foldLimit = (i < numFolds - 1) ? foldStart + itemsPerFold : numItems;
                Document testDoc = new Document();
                Element testRoot = new Element("regression");
                testDoc.setRootElement(testRoot);
                Document trainDoc = new Document();
                Element trainRoot = new Element("regression");
                trainDoc.setRootElement(trainRoot);
                // split items into train/test
                for (int j = 0; j < numItems; j++) {
                    Element item = shuffledItemsArray[j]; 
                    item.detach();
                    Element lf = shuffledLFsArray[j];
                    if (foldStart <= j && j < foldLimit) {
                        testRoot.addContent(item);
                        item.addContent(lf);
                        testStrings.add(item.getAttributeValue("string"));
                    }
                    else { 
                        // special case for 1.x folds: limit training data 
                        // to first numItems - itemsPerFold items
                        if (numFolds < 2 && i == 1 && j >= (numItems - itemsPerFold))
                            continue;
                        trainRoot.addContent(item);
                    }
                }
                // check for dups in training items
                List trainingItems = trainRoot.getChildren("item");
                for (Iterator it = trainingItems.iterator(); it.hasNext(); ) {
                    Element item = (Element) it.next();
                    if (testStrings.contains(item.getAttributeValue("string"))) {
                        it.remove(); dups++;
                    }
                }
                if (dups > 0) { 
                    System.out.println("Removing " + dups + " test item duplicate(s) from training, fold " + i);
                }
                FileOutputStream testOut = new FileOutputStream(new File(tmpDir, testFileName(i))); 
                tester.grammar.serializeXml(testDoc, testOut);
                testOut.close();
                FileOutputStream trainOut = new FileOutputStream(new File(tmpDir, trainingFileName(i))); 
                tester.grammar.serializeXml(trainDoc, trainOut);
                trainOut.close();
            }
            
            System.out.println();
        }
        catch (JDOMException exc) {
            throw (IOException) new IOException().initCause(exc);
        }
    }

    // training/test file names
    private String trainingFileName(int foldNum) { return "fold" + foldNum + "-train.xml"; }
    private String testFileName(int foldNum) { return "fold" + foldNum + "-test.xml"; }
    
    /** Does scorer prep (if any) on the folds already set-up in tmpdir. */
    public void prepScorers() throws IOException {
        // ensure tmpDir set
        if (tmpDir == null) tmpDir = new File(tmpdir);
        System.out.println("Preparing scorers in tmpdir: " + tmpdir);
        System.out.println();
        // do each fold
        for (int i = 0; i < numFolds; i++) {
            // make training/test files for fold
            File trainFile = new File(tmpDir, trainingFileName(i));
            File testFile = new File(tmpDir, testFileName(i));
            // prep scorer
            scorerMaker.prepScorer(tmpDir, i, trainFile, testFile);
        }
        // summary
        scorerMaker.prepScorersSummary(tmpDir);
    }

    
    /** Default, do-nothing implementation of ScorerMaker.setCVR. */
    public void setCVR(CrossValidateRealizer cvr) {}
    
    /** Default, do-nothing implementation of ScorerMaker.prepScorer. */
    public void prepScorer(File tmpDir, int foldNum, File trainFile, File testFile) throws IOException {}
    
    /** Default, do-nothing implementation of ScorerMaker.prepScorersSummary. */
    public void prepScorersSummary(File tmpDir) throws IOException {}
    
    /**
     * Default implementation of ScorerMaker.loadScorer.
     * Loads an n-gram precision model with semantic class replacement, 
     * using targets from the training data. 
     */
    public SignScorer loadScorer(File tmpDir, int foldNum, File trainFile) throws IOException {
        RegressionInfo trainingItems = new RegressionInfo(tester.grammar, trainFile);
        String[] targets = new String[trainingItems.numberOfItems()];
        for (int i=0; i < trainingItems.numberOfItems(); i++) {
            targets[i] = trainingItems.getItem(i).sentence;
        }
        NgramPrecisionModel retval = (tester.ngramOrder > 0) 
            ? new NgramPrecisionModel(targets, tester.ngramOrder, true) 
            : new NgramPrecisionModel(targets, true);
        return retval;
    }
    

    /** Run the cross-validation test on the folds already set-up in tmpdir. */
    public void runTest() throws IOException {

        // ensure tmpDir set
        if (tmpDir == null) tmpDir = new File(tmpdir);
        
        // turn-off parsing, stats
        tester.doParsing = false;
        tester.showStats = false;
        
        // setup realizer
        tester.realizer = new Realizer(tester.grammar);
        if (pruningStrategy != null) {
            tester.realizer.pruningStrategy = pruningStrategy;
        }

        // show realizer settings
        Regression.showRealizerSettings();
        
        // do each fold
        for (int i = 0; i < numFolds; i++) {
            // make files for fold
            File trainFile = new File(tmpDir, trainingFileName(i));
            File testFile = new File(tmpDir, testFileName(i));
            // load scorer
            tester.scorer = scorerMaker.loadScorer(tmpDir, i, trainFile);
            // run test
            tester.runTest(testFile);
        }
        
        // show stats
        tester.showStats();
    }
    
    
   
    /** Command-line routine for cross-validating realizer. */
    public static void main(String[] args) throws IOException { 
        
        String usage = "java opennlp.ccg.test.CrossValidateRealizer " + 
                       "(-folds N) (-tmp <tmpdir>) " +
                       "(-setuponly) (-skipsetup) " + 
                       "(-preponly) (-skipprep) " + 
                       "(-ngramorder N) " + 
                       "(-scorermaker <scorermakerclass>) " + 
                       "(-pruningstrategy <pruningstrategyclass>) " +
                       "(-g <grammarfile>) (-s <statsfile>) (<testbedfile>)";
                       
        if (args.length > 0 && args[0].equals("-h")) {
            System.out.println("Usage: " + usage);
            System.exit(0);
        }
        
        // args
        double numFolds = 0;
        String tmpdir = null;
        boolean setupOnly = false;
        boolean skipSetup = false;
        boolean prepOnly = false;
        boolean skipPrep = false;
        int ngramOrder = 0;
        String scorerMakerClass = null;
        String pruningStrategyClass = null; 
        String grammarfile = "grammar.xml";
        String testbedfile = "testbed.xml";
        String statsfile = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-folds")) { 
                numFolds = Double.parseDouble(args[++i]); 
                if (numFolds < 1 || (numFolds >= 2 && numFolds != Math.round(numFolds))) {
                    System.out.println("Error, folds must be 1.x or an int of at least 2");
                    System.exit(-1);
                }
                continue; 
            }
            if (args[i].equals("-tmp")) { tmpdir = args[++i]; continue; }
            if (args[i].equals("-setuponly")) { setupOnly = true; continue; }
            if (args[i].equals("-skipsetup")) { skipSetup = true; continue; }
            if (args[i].equals("-preponly")) { prepOnly = true; continue; }
            if (args[i].equals("-skipprep")) { skipPrep = true; continue; }
            if (args[i].equals("-ngramorder")) { ngramOrder = Integer.parseInt(args[++i]); continue; }
            if (args[i].equals("-scorermaker")) { scorerMakerClass = args[++i]; continue; }
            if (args[i].equals("-pruningstrategy")) { pruningStrategyClass = args[++i]; continue; }
            if (args[i].equals("-g")) { grammarfile = args[++i]; continue; }
            if (args[i].equals("-s")) { statsfile = args[++i]; continue; }
            testbedfile = args[i];
        }
        
        // make cross-validator
        URL grammarURL = new File(grammarfile).toURI().toURL();
        CrossValidateRealizer cvr = new CrossValidateRealizer(grammarURL);
        if (numFolds > 0) cvr.numFolds = numFolds;
        if (tmpdir != null) cvr.tmpdir = tmpdir;
        if (ngramOrder > 0) cvr.tester.ngramOrder = ngramOrder;
        if (scorerMakerClass != null) {
            try {
                cvr.scorerMaker = (ScorerMaker) Class.forName(scorerMakerClass).newInstance();
                cvr.scorerMaker.setCVR(cvr);
            } catch (Exception exc) {
                throw (RuntimeException) new RuntimeException().initCause(exc);
            }
        }
        if (pruningStrategyClass != null) {
            try {
                cvr.pruningStrategy = (PruningStrategy) Class.forName(pruningStrategyClass).newInstance();
            } catch (Exception exc) {
                throw (RuntimeException) new RuntimeException().initCause(exc);
            }
        }
        if (statsfile != null) cvr.tester.statsfile = statsfile;

        // set-up inputs
        URL testbedURL = new File(testbedfile).toURI().toURL();
        if (!skipSetup) { cvr.setupInputs(testbedURL); }
        if (setupOnly) { System.exit(0); }

        // prep scorers
        if (!skipPrep) { cvr.prepScorers(); }
        if (prepOnly) { System.exit(0); }
        
        // run test
        System.gc();
        cvr.runTest();
    }
}
