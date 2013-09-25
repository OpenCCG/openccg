///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-9 Jason Baldridge and Michael White (University of Edinburgh / The Ohio State University)
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

import java.io.*;
import java.net.URL;
import java.text.*;
import java.util.*;
import java.util.prefs.Preferences;

import opennlp.ccg.TextCCG;
import opennlp.ccg.grammar.Grammar;
import opennlp.ccg.hylo.*;
import opennlp.ccg.lexicon.Tokenizer;
import opennlp.ccg.lexicon.Word;
import opennlp.ccg.ngrams.*;
import opennlp.ccg.parse.ParseException;
import opennlp.ccg.parse.Parser;
import opennlp.ccg.parse.Supertagger;
import opennlp.ccg.parse.supertagger.WordAndPOSDictionaryLabellingStrategy;
import opennlp.ccg.realize.*;
import opennlp.ccg.realize.hypertagger.ZLMaxentHypertagger;
import opennlp.ccg.synsem.*;
import opennlp.ccg.util.Pair;
import opennlp.ccg.util.SingletonList;
import opennlp.ccg.perceptron.*;

import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Automates the testing of CCG grammars.
 *
 * @author  Jason Baldridge
 * @author  Michael White
 * @version $Revision: 1.151 $, $Date: 2011/12/11 16:51:58 $
 */
public class Regression {

    /** Flag for whether to do parsing. */
    public boolean doParsing = true;
    
    /** Flag for whether to do realization. */
    public boolean doRealization = true;

    /** Flag for whether to just do even items. */
    public boolean evenOnly = false;

    /** Flag for whether to just do odd items. */
    public boolean oddOnly = false;
    
    /** Flag for whether to do garbage collection before each iteration. */
    public boolean doGC = false;
    
    /** File to write events to (if any). */
    public String eventfile = null;
    
    /** Flag for whether to include the gold sign when generating events. */
    public boolean includeGoldInEvents = false;
    
    /** Directory for writing APML files (if any). */
    public String apmldir = null;
    
    /** Flag for whether to show realization stats. */
    public boolean showStats = true;
    
    /** Flag for whether to show parsing stats. */
    public boolean showParseStats = false;
    
    /** File to dump realizer stats to (if any). */
    public String statsfile = null;
    
    /** File prefix to write bleu test files to (if any). */
    public String bleufileprefix = null;

    /** File to write n-best realizations to (if any). */
    public String nbestrealfile = null;
    
    /** Flag for whether to normalize strings as for BLEU scoring in n-best output. */
    public boolean nbestnormbleu = false;
    
    /** Directory to save best realization serializations to (if any). */
    public String realserdir = null;
    
    /** File to write rescored sign scores to (if any). */
    public String rescorefile = null;
    
    /** Map from info keys to best realization signs for serialization (if any). */
    public Map<String,Sign> bestRealMap = null;
    
    /** Flag for whether to include LFs in n-best output. */
    public boolean nbestincludelfs = false;
    
    /** File to write n-best parses to (if any). */
    public String nbestparsefile = null;
    
    /** The grammar to use for testing. */
    public Grammar grammar = null;
    
    /** The parser to use for testing. */
    public Parser parser = null;
    
    /** The realizer to use for testing. */
    public Realizer realizer = null;
    
    /** The scorer to use for realizer testing (or null, for default). */
    public SignScorer scorer = null;
    
    /** The scorer to use for parser testing. */
    public SignScorer parseScorer = null;
    
    /** Flag for whether to only allow exact matches with the default scorer. */
    public boolean exactMatches = false;
    
    /** The n-gram order to use with the default scorer (or 0, for default). */
    public int ngramOrder = 0; 

    /** The feature extractor to use in extracting events. */
    public FeatureExtractor featureExtractor = null;
    
    //
    // the various totals
    //

    public int pCount = 0;
    public int pBadCount = 0;
    public int pFailedCount = 0;
    public int pExactCount = 0;
    public double totalF = 0.0;
    public double totalRecall = 0.0;
    public double totalPrecision = 0.0;
    public double totalDepsF = 0.0;
    public double totalDepsRecall = 0.0;
    public double totalDepsPrecision = 0.0;
    public double totalUnlabeledDepsF = 0.0;
    public double totalUnlabeledDepsRecall = 0.0;
    public double totalUnlabeledDepsPrecision = 0.0;
    public double totalFComplete = 0.0;
    public double totalRecallComplete = 0.0;
    public double totalPrecisionComplete = 0.0;
    public double totalDepsFComplete = 0.0;
    public double totalDepsRecallComplete = 0.0;
    public double totalDepsPrecisionComplete = 0.0;
    public double totalUnlabeledDepsFComplete = 0.0;
    public double totalUnlabeledDepsRecallComplete = 0.0;
    public double totalUnlabeledDepsPrecisionComplete = 0.0;
    public int pTotalEdges = 0;
    public int pTotalEdgesGood = 0;
    public int pMaxEdges = 0;
    public int pMaxEdgesGood = 0;
    public int pTotalUnpackingEdges = 0;
    public int pMaxUnpackingEdges = 0;
    public int pTotalCellMax = 0;
    public int pTotalCellMaxGood = 0;
    public int pMaxCellMax = 0;
    public int pMaxCellMaxGood = 0;
    public int pTotalLexTime = 0;
    public int pTotalParseTime = 0;
    public int pTotalChartTime = 0;
    public int pTotalUnpackingTime = 0;
    public int pMaxLexTime = 0;
    public int pMaxParseTime = 0;
    public int pMaxChartTime = 0;
    public int pMaxUnpackingTime = 0;
    public Map<Double,Integer> pBetaTallies = null;
    
    public int rCount = 0;
    public int rDoneCount = 0;
    public int rBadCount = 0;
    public int rExactCount = 0;
    public double totalScore = 0.0;
    public double totalScoreComplete = 0.0;
    public double totalReciprocalRank = 0.0;
    public int totalNominals = 0; 
    public int totalTokens = 0;
    public int minTokens = 0;
    public int maxTokens = 0;
    public int totalRuleApps = 0;
    public int totalEdges = 0;
    public int totalEdgesCreated = 0;
    public int totalUnprunedEdges = 0;
    public int totalPrunedRemoved = 0;
    public int totalPrunedNeverAdded = 0;
    public int totalCellMax = 0;
    public int totalNewBest = 0;
    public int totalLex = 0;
    public int totalFirst = 0;
    public int totalBest = 0;
    public int totalPacked = 0;
    public int totalStoppedOrDone = 0;
    public int maxLex = 0;
    public int maxFirst = 0;
    public int maxBest = 0;
    public int maxNewBest = 0;
    public int maxPacked = 0;
    public int maxStoppedOrDone = 0;
    public int oracleBetter = 0;
    public int goldMissing = 0;
    public String maxLexStr = null;
    public String maxFirstStr = null;
    public String maxBestStr = null;
    public String maxNewBestStr = null;
    public String maxPackedStr = null;
    public String maxStoppedOrDoneStr = null;
    public List<Double> bestEstimatedScores = null;
    public List<Double> bestActualScores = null;
    public List<Integer> itemRanks = null;
    public TimingMap lexMap = null;
    public TimingMap firstMap = null;
    public TimingMap bestMap = null; 
    public TimingMap allMap = null; 
    
    private PrintWriter events = null;
    private PrintWriter bleuGen = null;
    private PrintWriter bleuRef = null;
    private PrintWriter bleuSrc = null;
    private PrintWriter nbestrealPW = null;
    private PrintWriter rescorePW = null;
    private PrintWriter nbestparsePW = null;
    
    private XMLOutputter xmlOutputter = new XMLOutputter(); // for xml-escaping strings
    
    /** Constructor. */
    public Regression() {
        // init
        resetTotals();
    }
    
    /** Resets the various totals. */
    public void resetTotals() {
    	// parser
        pCount = 0; pBadCount = 0; pFailedCount = 0; pExactCount = 0;
        totalF = 0.0; totalRecall = 0.0; totalPrecision = 0.0;
        totalDepsF = 0.0; totalDepsRecall = 0.0; totalDepsPrecision = 0.0;
        totalUnlabeledDepsF = 0.0; totalUnlabeledDepsRecall = 0.0; totalUnlabeledDepsPrecision = 0.0;
        totalFComplete = 0.0; totalRecallComplete = 0.0; totalPrecisionComplete = 0.0;
        totalDepsFComplete = 0.0; totalDepsRecallComplete = 0.0; totalDepsPrecisionComplete = 0.0;
        totalUnlabeledDepsFComplete = 0.0; totalUnlabeledDepsRecallComplete = 0.0; totalUnlabeledDepsPrecisionComplete = 0.0;
        pTotalEdges = 0; pTotalEdgesGood = 0; pMaxEdges = 0; pMaxEdgesGood = 0;
        pTotalUnpackingEdges = 0; pMaxUnpackingEdges = 0;
        pTotalCellMax = 0; pTotalCellMaxGood = 0; pMaxCellMax = 0; pMaxCellMaxGood = 0;
        pTotalLexTime = 0; pTotalParseTime = 0; pTotalChartTime = 0; pTotalUnpackingTime = 0;
        pMaxLexTime = 0; pMaxParseTime = 0; pMaxChartTime = 0; pMaxUnpackingTime = 0;
        if (doParsing) {
        	pBetaTallies = new TreeMap<Double,Integer>();
        }
        // realizer
        rCount = 0; rDoneCount = 0; rBadCount = 0; rExactCount = 0;
        totalScore = 0.0; totalScoreComplete = 0.0;
        totalReciprocalRank = 0.0;
        totalNominals = 0; 
        totalTokens = 0; minTokens = 0; maxTokens = 0;
        totalRuleApps = 0;
        totalEdges = 0; totalEdgesCreated = 0; totalUnprunedEdges = 0; totalPrunedRemoved = 0; totalPrunedNeverAdded = 0; totalCellMax = 0;
        totalNewBest = 0; totalLex = 0; totalFirst = 0; totalBest = 0; totalPacked = 0; totalStoppedOrDone = 0;
        maxLex = 0; maxFirst = 0; maxBest = 0; maxNewBest = 0; maxPacked = 0; maxStoppedOrDone = 0;
        oracleBetter = 0; goldMissing = 0;
        maxLexStr = null; maxFirstStr = null; maxBestStr = null; maxNewBestStr = null; maxPackedStr = null; maxStoppedOrDoneStr = null;
        if (doRealization) {
            bestActualScores = new ArrayList<Double>(); bestEstimatedScores = new ArrayList<Double>(); itemRanks = new ArrayList<Integer>();
            lexMap = new TimingMap("lex"); firstMap = new TimingMap("first"); bestMap = new TimingMap("best"); allMap = new TimingMap("all");
        }
    }

    
    // sets up bleu output
    private void bleuSetup() throws IOException {
    	// setup bleu files, if apropos
    	if (bleufileprefix != null && doRealization) {
    		bleuGen = new PrintWriter(new BufferedWriter(new FileWriter(bleufileprefix + "-gen.sgm")));
    		bleuRef = new PrintWriter(new BufferedWriter(new FileWriter(bleufileprefix + "-ref.sgm")));
    		bleuSrc = new PrintWriter(new BufferedWriter(new FileWriter(bleufileprefix + "-src.sgm")));
    		bleuGen.println("<tstset setid=\"ccg-test\" srclang=\"en\" trglang=\"en\">");
    		bleuRef.println("<refset setid=\"ccg-test\" srclang=\"en\" trglang=\"en\">");
    		bleuSrc.println("<srcset setid=\"ccg-test\" srclang=\"en\">");
    	}
    }
    
    // sets up n-best realization output
    private void nbestrealSetup() throws IOException {
        // set up file to write ref & n-best realizations (if any)
        if (nbestrealfile != null && doRealization) {
            nbestrealPW = new PrintWriter(new BufferedWriter(new FileWriter(nbestrealfile)));
            nbestrealPW.println("<nbest>");
        }
    }
    
    // sets up rescored sign score output
    private void rescoreSetup() throws IOException {
        // set up rescoring file
        if (rescorefile != null) {
            rescorePW = new PrintWriter(new BufferedWriter(new FileWriter(rescorefile)));
            rescorePW.println("<rescored>");
        }
    }
    
    // sets up n-best parsing output
    private void nbestparseSetup() throws IOException {
        // set up file to write sentence & n-best parses (if any)
        if (nbestparsefile != null && doParsing) {
            nbestparsePW = new PrintWriter(new BufferedWriter(new FileWriter(nbestparsefile)));
            nbestparsePW.println("<nbest>");
        }
    }
    
    // starts a doc
    private void bleuStartDoc(String id) {
    	if (bleufileprefix != null && doRealization) {
			bleuGen.println("<doc docid=\"" + id + "\" sysid=\"openccg\">");
			bleuRef.println("<doc docid=\"" + id + "\" sysid=\"ref\">");
			bleuSrc.println("<doc docid=\"" + id + "\">");
    	}
    }

    // ends a doc
    private void bleuEndDoc() {
    	if (bleufileprefix != null && doRealization) {
    		bleuGen.println("</doc>"); 
    		bleuRef.println("</doc>"); 
    		bleuSrc.println("</doc>"); 
    	}
    }

    // finishes bleu output
    private void bleuFinish() throws IOException {
    	// finish bleu files, if apropos
    	if (bleufileprefix != null && doRealization) {
    		bleuGen.println("</tstset>");
    		bleuRef.println("</refset>");
    		bleuSrc.println("</srcset>");
    		bleuGen.flush(); bleuGen.close();
    		bleuRef.flush(); bleuRef.close();
    		bleuSrc.flush(); bleuSrc.close();
    	}
    }

    // finishes n-best realization output
    private void nbestrealFinish() throws IOException {
    	// finish n-best real file, if apropos
    	if (nbestrealfile != null && doRealization) {
    		nbestrealPW.println("</nbest>");
    		nbestrealPW.flush(); nbestrealPW.close();
    	}
    }

    // finishes rescored sign output
    private void rescoreFinish() throws IOException {
    	// finish rescoring file
    	if (rescorefile != null) {
    		rescorePW.println("</rescored>");
    		rescorePW.flush(); rescorePW.close();
    	}
    }

    // finishes n-best parsing output
    private void nbestparseFinish() throws IOException {
    	// finish n-best real file, if apropos
    	if (nbestparsefile != null && doParsing) {
    		nbestparsePW.println("</nbest>");
    		nbestparsePW.flush(); nbestparsePW.close();
    	}
    }

    // resets bestRealMap
    private void realserStartDoc() {
    	if (realserdir != null && doRealization) {
    		bestRealMap = new HashMap<String,Sign>();
    	}
    }
    
    // serializes bestRealMap
    private void realserEndDoc(String testName) throws IOException {
    	if (realserdir != null && doRealization) {
	    	File serFile = new File(new File(realserdir), testName + ".ser");
	    	ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serFile));
	    	oos.writeObject(bestRealMap);
	    	oos.close();
    	}
    }
    
    // escapes string for xml output
    private String xmlEscape(String s) { return xmlOutputter.outputString(new Text(s)); }
    
    // normalizes realizations for BLEU scoring
    // at present, this means replacing underscores with spaces and escaping for xml output
    private String norm_bleu(String s) { return xmlEscape(s.replace('_', ' ')); }
    
    
    /** Runs the test on the items in the given file or directory of files. */
    public void runTest(File regressionFile) throws IOException {
    	// set up event file (if any)
    	if (eventfile != null) events = EventFile.openWriter(new File(eventfile));
        // set up bleu output, n-best realizations, rescoring, n-best parses (if apropos)
    	bleuSetup(); nbestrealSetup(); rescoreSetup(); nbestparseSetup();
    	// do each file or files
    	for (File f : getXMLFiles(regressionFile))
    		runSingleTest(f);
    	// finish bleu, n-best realization output, rescoring, n-best parses (if apropos)
    	bleuFinish(); nbestrealFinish(); rescoreFinish(); nbestparseFinish();
    	// close event file (if any)
    	if (events != null) { events.flush(); events.close(); }
    	// show stats (if apropos)
    	if (rescorefile != null) return;
    	if (doParsing && showParseStats) showParseStats();
        if (doRealization && showStats) showStats();
    }
    	 
    /** Returns a list of xml files from the given file or directory. */
    public static List<File> getXMLFiles(File file) {
    	if (!file.isDirectory()) return new SingletonList<File>(file);
    	List<File> retval = new ArrayList<File>();
		File[] files = file.listFiles();
		Arrays.sort(files);
		for (int i = 0; i < files.length; i++) {
			if (!files[i].isDirectory() && files[i].getName().endsWith(".xml"))
				retval.add(files[i]);
		}
    	return retval;
    }
    	
    /** Runs the test on the items in the given file. */
    private void runSingleTest(File regressionFile) throws IOException {
    	
    	String testName = regressionFile.getName();
    	int lastDot = testName.lastIndexOf('.');
    	if (lastDot > 0) testName = testName.substring(0, lastDot);
    	
        // load testfile
    	System.out.println("Loading: " + testName);
    	System.out.println();
        RegressionInfo rinfo = new RegressionInfo(grammar, regressionFile);

        // start bleu doc (if apropos)
        bleuStartDoc(testName);
        // start storing best realizations for serialization (if apropos) 
        realserStartDoc();
        
        // do each test
        int numItems = rinfo.numberOfItems();
        System.out.println("Parse\tRealize\tString");
        System.out.println("-----\t-------\t------");
        
        for (int i=0; i < numItems; i++) {

            // check even/odd only
            if (i % 2 == 1 && evenOnly) continue;
            if (i % 2 == 0 && oddOnly) continue;
            
            RegressionInfo.TestItem testItem = rinfo.getItem(i);
            if (doGC) System.gc();
            
            // short circuit for sign rescoring; realization only at the moment
            if (rescorefile != null) {
                String id = testItem.info;
                double score = scorer.score(testItem.sign, true);
            	rescorePW.println("<seg id=\"" + id + "\" score=\"" + score + "\"/>");
                showOutcome("-", nfE.format(score), "", testItem.sign.getOrthography());
            	continue;
            }
            
            List<Sign> parses = null;
            List<Double> parseScores = null;
            LF parsedLF = null;
            LF compactedLF = null;
            LF transformedParsedLF = null;
            boolean parsed = false;
            boolean parsedComplete = false;
            if (doParsing) {
                try {
            		// use full-words or words from stored sign if possible
            		List<Word> words = null;
            		if (testItem.fullWords != null) {
                		words = grammar.lexicon.tokenizer.tokenize(testItem.fullWords, true);
                		// strip <s> and </s>
                		if (words.get(0).getForm() == "<s>") words.remove(0);
                		if (words.get(words.size()-1).getForm() == "</s>") words.remove(words.size()-1);
            		}
            		else if (testItem.sign != null) {
            			words = testItem.sign.getWords();
            		}
            		if (words != null) {
	            		// parse 'em
	            		parser.parse(words);
            		}
                	else { 
                		parser.parse(testItem.sentence);
                	}
                	// retrieve results
                    parses = parser.getResult();
                    parseScores = parser.getScores();
                    parsed = true;
                    parsedComplete = !parses.get(0).getCategory().isFragment();
                    // get LF of best parse, if needed
                    if (showParseStats || (doRealization && testItem.lfElt == null && testItem.sign == null)) {
	                    Sign sign = parses.get(0);
	                    Category cat = sign.getCategory().copy();
	                    Nominal index = cat.getIndexNominal();
	                    parsedLF = cat.getLF();
	                    index = HyloHelper.convertNominals(parsedLF, sign, index);
	                    compactedLF = HyloHelper.compact(parsedLF, index);
	                    // get transformed version if needed
	                    if (testItem.sign == null) {
		                    transformedParsedLF = grammar.transformLF(compactedLF); 
	                    }
                    }
                } catch (ParseException e) {
                	parses = Collections.emptyList();
                    parsed = false;
                } catch (Exception e) {
                	parses = Collections.emptyList();
                    parsed = false;
                    System.err.println("Uncaught exception in parsing: " + testItem.sentence);
                    e.printStackTrace(System.err);
                }
                  
                // update parse stats
                int count = parser.edgeCount();
                pTotalEdges += count;
                if (count > pMaxEdges) pMaxEdges = count;
                if (parsedComplete) {
                	pTotalEdgesGood += count;
                	if (count > pMaxEdgesGood) pMaxEdgesGood = count;
                }
                count = parser.unpackingEdgeCount();
                pTotalUnpackingEdges += count;
                if (count > pMaxUnpackingEdges) pMaxUnpackingEdges = count;
                int cellMax = parser.maxCellSize();
                pTotalCellMax += cellMax;
                if (cellMax > pMaxCellMax) pMaxCellMax = cellMax;
                if (parsedComplete) {
                	pTotalCellMaxGood += cellMax;
                    if (cellMax > pMaxCellMaxGood) pMaxCellMaxGood = cellMax;
                }
                int time = parser.getLexTime();
                pTotalLexTime += time;
                if (time > pMaxLexTime) pMaxLexTime = time;
                time = parser.getParseTime();
                pTotalParseTime += time;
                if (time > pMaxParseTime) pMaxParseTime = time;
                time = parser.getChartTime();
                pTotalChartTime += time;
                if (time > pMaxChartTime) pMaxChartTime = time;
                time = parser.getUnpackingTime();
                pTotalUnpackingTime += time;
                if (time > pMaxUnpackingTime) pMaxUnpackingTime = time;
                double beta = parser.getSupertaggerBeta();
                Integer betaTally = pBetaTallies.get(beta);
                pBetaTallies.put(beta, (betaTally != null) ? ++betaTally : 1);
            }
            
            // get test item LF, if needed
            LF testItemLF = null;
            if (testItem.lfElt != null && (doRealization || (showParseStats && parsed && testItem.sign == null))) {
                Element lfElt = testItem.lfElt;
                Document doc = new Document();
                lfElt.detach();
                doc.setRootElement(lfElt);
                testItemLF = grammar.loadLF(doc);
            }
            
            // compare EPs
            EPsScorer.Results parseScore = null;
            LF goldLF = null;
            if (showParseStats && parsedLF != null) {
                // get LF to score, gold LF
            	LF lfToScore = null;
                if (testItem.sign != null) {
                	// use LF from stored sign if available
                	lfToScore = parsedLF;
                    Category cat = testItem.sign.getCategory().copy();
                    Nominal index = cat.getIndexNominal();
                    goldLF = cat.getLF();
                    index = HyloHelper.convertNominals(goldLF, testItem.sign, index);
                }
                else {
                	// otherwise use test item LF
                	lfToScore = transformedParsedLF;
                	goldLF = testItemLF;
                }
        		if (goldLF == null) {
        			throw new RuntimeException(
        				"Can't score parse: " + testItem.sentence + "!\n" +
        				"No gold LF."
    				);
        		}
                // score parse
            	parseScore = EPsScorer.score(lfToScore, goldLF);
            }
            
            // update parsing results
            if (showParseStats && testItem.numOfParses > 0) {
            	pCount++;
            	if (parses.size() == 0 || !parsedComplete) pBadCount++;
            	if (parses.size() == 0) pFailedCount++;
            	if (parseScore != null) {
            		if (parseScore.fscore == 1.0) pExactCount++;
            		totalF += parseScore.fscore;
            		totalRecall += parseScore.recall;
            		totalPrecision += parseScore.precision;
            		totalDepsF += parseScore.depsFscore;
            		totalDepsRecall += parseScore.depsRecall;
            		totalDepsPrecision += parseScore.depsPrecision;
            		totalUnlabeledDepsF += parseScore.unlabeledDepsFscore;
            		totalUnlabeledDepsRecall += parseScore.unlabeledDepsRecall;
            		totalUnlabeledDepsPrecision += parseScore.unlabeledDepsPrecision;
            		if (parsedComplete) {
                		totalFComplete += parseScore.fscore;
                		totalRecallComplete += parseScore.recall;
                		totalPrecisionComplete += parseScore.precision;
                		totalDepsFComplete += parseScore.depsFscore;
                		totalDepsRecallComplete += parseScore.depsRecall;
                		totalDepsPrecisionComplete += parseScore.depsPrecision;
                		totalUnlabeledDepsFComplete += parseScore.unlabeledDepsFscore;
                		totalUnlabeledDepsRecallComplete += parseScore.unlabeledDepsRecall;
                		totalUnlabeledDepsPrecisionComplete += parseScore.unlabeledDepsPrecision;
            		}
            	}
            }
            
            // events output
            if (events != null && doParsing) {
            	// nb: only dealing with complete parses at the moment
            	// nb: gold LF must come from saved sign
            	if (parses.size() > 0 && testItem.sign != null) {
            		List<Sign> bestSigns = new ArrayList<Sign>(parses);
            		Sign best = parses.get(0);
            		// update best if not exact match
            		if (parseScore.fscore != 1.0) {
            			// check oracle best
            			Pair<Sign,Boolean> bestPair = parser.oracleBest(goldLF); 
            			if (bestPair.a != null) oracleBetter++;
            			if (bestPair.b) {
            				best = bestPair.a;
            				if (!bestSigns.contains(best)) bestSigns.add(best);
            			}
            			// add gold if missing, if apropos
            			else {
            				goldMissing++;
            				if (includeGoldInEvents) {
	                			best = testItem.sign;
	                    		//parser.addSupertaggerLogProbs(best); 
	                    		bestSigns.add(best);
            				}
            			}
            		}
        			EventFile.writeEvents(events, bestSigns, best, featureExtractor);
            	}
            }
            
            // n-best parses output
            if (nbestparsePW != null) {
                XMLOutputter outputter = new XMLOutputter();
                outputter.setFormat(Format.getPrettyFormat());
            	// header for item
                String extras = "";
                if (parsedComplete) extras += " complete=\"true\"";
                String id = testItem.info;
                if (id == null) id = "" + i;
                nbestparsePW.println("<seg id=\"" + id + "\" str=\"" + xmlEscape(testItem.sentence) + "\"" + extras + ">");
            	String tagend = (nbestincludelfs) ? ">" : "/>";
            	// add best parse
            	if (parseScore != null) { 
            		double edgeScore = parseScores.get(0);
                	String scores = "score=\"" + nf.format(parseScore.fscore) + "\" edge-score=\"" + nfE.format(edgeScore) + "\"";
                	nbestparsePW.println("<best " + scores + tagend);
                	if (nbestincludelfs) {
                    	Element lfElt = grammar.makeLfElt(compactedLF);
                    	nbestparsePW.println(outputter.outputString(lfElt));
                    	nbestparsePW.println("</best>");                		
                	}
            	}
            	// add remaining n-best 
            	for (int k=1; k < parses.size(); k++) {
                    Sign sign = parses.get(k);
            		double edgeScore = parseScores.get(k);
                    Category cat = sign.getCategory().copy();
                    Nominal index = cat.getIndexNominal();
                    LF parsedLFk = cat.getLF();
                    index = HyloHelper.convertNominals(parsedLFk, sign, index);
                    LF compactedLFk = HyloHelper.compact(parsedLFk, index);
                    LF lfToScore = parsedLFk;
                    if (testItem.sign != null) {
                        lfToScore = grammar.transformLF(compactedLFk); 
                    }
                    EPsScorer.Results parseScoreK = EPsScorer.score(lfToScore, goldLF);
                	String scores = "score=\"" + nf.format(parseScoreK.fscore) + "\" edge-score=\"" + nfE.format(edgeScore) + "\"";
                	nbestparsePW.println("<next " + scores + tagend);
                	if (nbestincludelfs) {
                    	Element lfElt = grammar.makeLfElt(compactedLFk);
                    	nbestparsePW.println(outputter.outputString(lfElt));
                    	nbestparsePW.println("</next>");
                	}
            	}
                // close item
                nbestparsePW.println("</seg>");
            }
        
            // determine string to show for parse result
            String starForBadSentence = "";
            if (testItem.numOfParses == 0) starForBadSentence = "*";
            
            String parseResult;
            if (!doParsing) {
                parseResult = "-";
            } else if (parseScore != null) {
                parseResult = nf.format(parseScore.fscore);
                if (!parsedComplete) parseResult = "[" + parseResult + "]";
            } else if (testItem.numOfParses == parses.size()) {
                parseResult = "ok";
            } else if (testItem.numOfParses > 0 && parses.size() > 0) {
                // show num parses, if not the number expected
                parseResult = "(" + parses.size() + ")";
            } else if (testItem.knownFailure) {
                parseResult = "(known)";
            } else {
                parseResult = "FAILED";
            }
                
            if (!doRealization || (doParsing && !parsed) || testItem.numOfParses == 0) {
                showOutcome(parseResult, "-", starForBadSentence, testItem.sentence);
                continue;
            }
            
            LF inputLF = null;
            // use given LF
            if (testItemLF != null) inputLF = testItemLF;
            // or LF from stored sign
            else if (testItem.sign != null) {
                Sign sign = testItem.sign;
                Category cat = sign.getCategory().copy();
                Nominal index = cat.getIndexNominal();
                LF convertedLF = HyloHelper.compactAndConvertNominals(cat.getLF(), index, sign);
                inputLF = grammar.transformLF(convertedLF);
            }
            // otherwise use first parse
            else if (transformedParsedLF != null) inputLF = transformedParsedLF;
            // otherwise give up
            else {
                String suggestion = (!doParsing) ? "Try leaving off -noparsing option." : "";
                throw new RuntimeException("No LF to realize! " + suggestion);
            }
            
            // set up n-gram precision scorer for default scoring and/or scoring results
            String[] targets = (testItem.alt == null) 
            	? new String[] { testItem.sentence }
            	: new String[] { testItem.sentence, testItem.alt };
            NgramPrecisionModel defaultNgramScorer = new NgramPrecisionModel(targets);
            SignScorer scorerToUse = scorer;
            if (scorerToUse == null) {
                if (ngramOrder > 0 || exactMatches) {
                	if (ngramOrder > 0) scorerToUse = new NgramPrecisionModel(targets, ngramOrder);
                	else scorerToUse = new NgramPrecisionModel(targets);
                	((NgramPrecisionModel)scorerToUse).setExactMatches(exactMatches);
                }
                else scorerToUse = defaultNgramScorer;
            }
            // set targets for self-paraphrase biasing
            else if (scorerToUse instanceof SelfParaphraseBiaser) {
            	((SelfParaphraseBiaser)scorerToUse).setTargets(targets);
            }
            
            if (doGC) System.gc(); 
            try {
            	//Add gold std pred supertag mapping to the hypertagger class
            	if (events != null && realizer.hypertagger != null && testItem.predInfo != null) {
                	realizer.hypertagger.storeGoldStdPredInfo(testItem.predInfo);
                }
            	realizer.realize(inputLF, scorerToUse);
            }
            catch (Throwable thrwbl) {
                System.out.println("Unable to process: " + testItem.sentence);
                thrwbl.printStackTrace(System.out);
                continue;
            }
            opennlp.ccg.realize.Chart chart = realizer.getChart();
            String realizeResult = "ok"; boolean gramcomplete = true; boolean joined = false;
            Edge bestEdge = chart.bestEdge;
            if (!bestEdge.complete() || bestEdge.getSign().getCategory().isFragment()) {
                realizeResult = "[ok]"; gramcomplete = false;
                rBadCount++;
                if (chart.joinFragments) { 
                	bestEdge = chart.bestJoinedEdge;
                	joined = true;
                }
            }
            String bestRealization = bestEdge.getSign().getOrthography();
            double score = defaultNgramScorer.score(bestEdge.getSign(), false); // nb: use default n-gram precision score for reporting 
            
            // events output
            if (events != null) {
            	List<Edge> bestEdges = chart.bestEdges();
            	// nb: only dealing with complete realizations at the moment
            	if (bestEdges.size() > 0) {
            		Pair<Edge,Boolean> bestPair = chart.oracleBest(testItem.sentence); 
            		Edge oracleBest = bestPair.a;
            		if (oracleBest != null) {
                    	Sign best = oracleBest.getSign();
                    	List<Sign> bestSigns = new ArrayList<Sign>(bestEdges.size()+1);
                    	for (Edge e : bestEdges) bestSigns.add(e.getSign());
            			if (bestEdge != oracleBest) oracleBetter++;
            			if (!bestPair.b) {
            				goldMissing++;
            				if (includeGoldInEvents) {
            					best = testItem.sign;
            				}
            			}
            			if (!bestSigns.contains(best)) bestSigns.add(best);
            			EventFile.writeEvents(events, bestSigns, best, featureExtractor);
            		}
            	}
            }
            
            // bleu output
            if (bleufileprefix != null) {
                String extras = " time=\"";
                if (chart.done) extras += chart.timeTilDone;
                else extras += chart.timeTilStopped;
                extras += "\"";
                extras += " score=\"" + nf.format(score) + "\"";
                if (gramcomplete) extras += " complete=\"true\"";
                if (joined) extras += " joined=\"true\"";
                String id = testItem.info;
                if (id == null) id = "" + i;
            	bleuGen.println("<seg id=\"" + id + "\"" + extras + ">" + norm_bleu(bestRealization) + "</seg>");
            	String sent = norm_bleu(testItem.sentence);
            	bleuRef.println("<seg id=\"" + id + "\">" + sent + "</seg>");
            	bleuSrc.println("<seg id=\"" + id + "\">" + sent + "</seg>");
            }
            
            // n-best realization output
            if (nbestrealPW != null) {
                XMLOutputter outputter = new XMLOutputter();
                outputter.setFormat(Format.getPrettyFormat());
            	// header for item
                String extras = "";
                if (gramcomplete) extras += " complete=\"true\"";
                if (joined) extras += " joined=\"true\"";
                String id = testItem.info;
                if (id == null) id = "" + i;
            	nbestrealPW.println("<seg id=\"" + id + "\"" + extras + ">");
            	// add ref sentence
            	String ref = (nbestnormbleu) ? norm_bleu(testItem.sentence) : xmlEscape(testItem.sentence); 
            	nbestrealPW.println("<ref>" + ref + "</ref>");
            	// add best realization
            	String scores = "score=\"" + nf.format(score) + "\" edge-score=\"" + nfE.format(bestEdge.score) + "\"";
            	String best = (nbestnormbleu) ? norm_bleu(bestRealization) : xmlEscape(bestRealization);
            	if (!nbestincludelfs)
            		nbestrealPW.println("<best " + scores + ">" + best + "</best>");
            	else {
            		nbestrealPW.println("<best " + scores + ">");
            		nbestrealPW.println("<str>" + best + "</str>");
                	Sign sign = bestEdge.getSign();
                    Category cat = sign.getCategory().copy();
                    Nominal index = cat.getIndexNominal();
                    LF lf = cat.getLF();
                	index = HyloHelper.convertNominalsToVars(lf, index);
                    index = HyloHelper.convertNominals(lf, sign, index);
                    LF lfc = HyloHelper.compact(lf, index);
                	Element lfElt = grammar.makeLfElt(lfc);
                	nbestrealPW.println(outputter.outputString(lfElt));
            		nbestrealPW.println("</best>");
            		
            	}
                // if complete, add remaining n-best
                if (bestEdge.complete()) {
                    List<Edge> bestEdges = chart.bestEdges();
                    for (int j=1; j < bestEdges.size(); j++) {
                    	Edge e = bestEdges.get(j);
                        String eSent = e.getSign().getOrthography();
                        double eScore = defaultNgramScorer.score(e.getSign(), false); // nb: use default n-gram precision score for reporting
                    	String eScores = " score=\"" + nf.format(eScore) + "\" edge-score=\"" + nfE.format(e.score) + "\"";
                    	// add next realization
                    	String next = (nbestnormbleu) ? norm_bleu(eSent) : xmlEscape(eSent);
                    	if (!nbestincludelfs)
                    		nbestrealPW.println("<next" + eScores + ">" + next + "</next>");
                    	else {
                    		nbestrealPW.println("<next" + eScores + ">");
                    		nbestrealPW.println("<str>" + next + "</str>");
                        	Sign sign = e.getSign();
                            Category cat = sign.getCategory().copy();
                            Nominal index = cat.getIndexNominal();
                            LF lf = cat.getLF();
                        	index = HyloHelper.convertNominalsToVars(lf, index);
                            index = HyloHelper.convertNominals(lf, sign, index);
                            LF lfc = HyloHelper.compact(lf, index);
                        	Element lfElt = grammar.makeLfElt(lfc);
                        	nbestrealPW.println(outputter.outputString(lfElt));
                    		nbestrealPW.println("</next>");
                    	}
                    }
                }
                // close item
            	nbestrealPW.println("</seg>");
            }
        
        	// if apmldir non-null, output APML as apmldir/ex(i+1).apml
            if (apmldir != null) {
                String apmlfn = apmldir + "/ex" + (i+1) + ".apml";
                grammar.saveToApml(bestEdge.getSign(), apmlfn);
            }
            
            // store best realization, if apropos and grammatically complete, keyed by info string or item position
            if (realserdir != null && gramcomplete) {
            	String id = testItem.info;
            	if (id == null) id = "i" + i;
            	bestRealMap.put(testItem.info, bestEdge.getSign());
            }
            
            // compute stats, show outcome
            rCount++;
            totalScore += score; 
            if (gramcomplete) totalScoreComplete += score;
            int itemRank = 1;
            Tokenizer tokenizer = grammar.lexicon.tokenizer;
            String itemOrth = tokenizer.getOrthography(tokenizer.tokenize(testItem.sentence));
            if (!bestRealization.equals(itemOrth)) {
                itemRank = 0;
                List<Edge> bestEdges = chart.bestEdges();
                for (int j = 0; j < bestEdges.size(); j++) {
                    Edge edge = bestEdges.get(j);
                    String str = edge.getSign().getOrthography();
                    if (str.equals(itemOrth)) {
                        itemRank = j+1; break;
                    }
                }
                if (itemRank > 0) totalReciprocalRank += (1.0 / itemRank);
            	if (gramcomplete) {
	                realizeResult = nf.format(score);
	                if (itemRank > 0 && itemRank < 10) realizeResult += " ";
	                if (itemRank > 0 && itemRank < 100) realizeResult += "#" + itemRank;
            	}
            	else {
            		realizeResult = "[" + nf.format(score) + "]";
            		if (joined) realizeResult += "j";
            	}
                showOutcome(parseResult, realizeResult, starForBadSentence, testItem.sentence, bestRealization);
            }
            else {
                rExactCount++;
                totalReciprocalRank += 1.0;
                showOutcome(parseResult, realizeResult, starForBadSentence, testItem.sentence);
            }
            
            totalNominals += chart.numNominals;
            int tokens = testItem.sentence.split("\\s+").length;
            totalTokens += tokens;
            if (tokens < minTokens || minTokens == 0) minTokens = tokens;
            if (tokens > maxTokens) maxTokens = tokens;
            totalRuleApps += chart.edgeFactory.ruleApps();
            totalEdges += chart.numEdgesInChart();
            totalEdgesCreated += chart.numEdges;
            totalUnprunedEdges += chart.numUnprunedEdges();
            totalPrunedRemoved += chart.numPrunedRemoved;
            totalPrunedNeverAdded += chart.numPrunedNeverAdded;
            totalCellMax += chart.cellMax;
            totalNewBest += chart.newBest;
            
            bestActualScores.add(new Double(score));
            bestEstimatedScores.add(new Double(bestEdge.score));
            itemRanks.add(new Integer(itemRank));
            
            totalLex += chart.timeTilLex;
            if (chart.timeTilLex > maxLex) {
                maxLex = chart.timeTilLex;
                maxLexStr = testItem.sentence;
            }
            lexMap.add(chart.numNominals, chart.timeTilLex);
            
            totalFirst += chart.timeTilFirst;
            if (chart.timeTilFirst > maxFirst) {
                maxFirst = chart.timeTilFirst;
                maxFirstStr = testItem.sentence;
            }
            firstMap.add(chart.numNominals, chart.timeTilFirst);
            
            totalBest += chart.timeTilBest;
            if (chart.timeTilBest > maxBest) {
                maxBest = chart.timeTilBest;
                maxBestStr = testItem.sentence;
            }
            bestMap.add(chart.numNominals, chart.timeTilBest);
            
            if (chart.newBest > 0 && (chart.timeTilBest - chart.timeTilFirst) >= maxNewBest) {
                maxNewBest = chart.timeTilBest - chart.timeTilFirst;
                maxNewBestStr = testItem.sentence;
            }
            
            totalPacked += chart.timeTilPacked;
            if (chart.timeTilPacked > maxPacked) {
                maxPacked = chart.timeTilPacked;
                maxPackedStr = testItem.sentence;
            }
            
            if (chart.done) {
                rDoneCount++;
                totalStoppedOrDone += chart.timeTilDone;
                if (chart.timeTilDone > maxStoppedOrDone) {
                    maxStoppedOrDone = chart.timeTilDone;
                    maxStoppedOrDoneStr = testItem.sentence;
                }
                allMap.add(chart.numNominals, chart.timeTilDone);
            }
            else {
                totalStoppedOrDone += chart.timeTilStopped;
                if (chart.timeTilStopped > maxStoppedOrDone) {
                    maxStoppedOrDone = chart.timeTilStopped;
                    maxStoppedOrDoneStr = testItem.sentence;
                }
                allMap.add(chart.numNominals, chart.timeTilStopped);
            }
        }
        
        // end bleu doc (if apropos)
        bleuEndDoc();
        System.out.println();
        
        // serialize best realizations (if apropos)
        realserEndDoc(testName);
    }

    /** Shows the various parsing totals. */
    public void showParseStats() {
    	int pCompleteCount = pCount - pBadCount;
    	int pFragCount = pBadCount - pFailedCount;
        String pComplete = "" + pCompleteCount;
        String pCompletePct = "" + nf.format(100.0 * pCompleteCount / pCount) + "%";
        System.out.println("Strings parsed completely (in fragments, failed): " + pComplete + " (" + pFragCount + ", " + pFailedCount + ") " + pCompletePct);
        String pInexact = "" + (pCount - pExactCount);
        String pExactPct = "" + nf.format(100.0 * pExactCount / pCount) + "%";
        System.out.println("Strings parsed exactly (inexactly): " + pExactCount + " (" + pInexact + ") " + pExactPct);
        String avgEdges = nf.format(1.0 * pTotalEdges / pCount);
        System.out.println("Average edge count (before unpacking): " + avgEdges);
        String avgEdgesGood = nf.format(1.0 * pTotalEdgesGood / pCompleteCount);
        System.out.println("Average edge count (before unpacking) for complete parses: " + avgEdgesGood);
        System.out.println("Max edge count: " + pMaxEdges);
        System.out.println("Max edge count for complete parses: " + pMaxEdgesGood);
        String avgUnpacked = nf.format(1.0 * pTotalUnpackingEdges / pCount);
        System.out.println("Average edges unpacked (created while unpacking): " + avgUnpacked);
        System.out.println("Max unpacked edges: " + pMaxUnpackingEdges);
        String avgLexTime = nf.format(1.0 * pTotalLexTime / pCount);
        String avgCellMax = nf.format(1.0 * pTotalCellMax / pCount);
        System.out.println("Average max cell size (before unpacking): " + avgCellMax);
        String avgCellMaxGood = nf.format(1.0 * pTotalCellMaxGood / pCompleteCount);
        System.out.println("Average max cell size (before unpacking) for complete parses: " + avgCellMaxGood);
        System.out.println("Max max cell size: " + pMaxCellMax);
        System.out.println("Max max cell size for complete parses: " + pMaxCellMaxGood);
        System.out.println("Average lex lookup time: " + avgLexTime);
        String avgParseTime = nf.format(1.0 * pTotalParseTime / pCount);
        System.out.println("Max lex lookup time: " + pMaxLexTime);
        System.out.println("Average parse time: " + avgParseTime);
        System.out.println("Max parse time: " + pMaxParseTime);
        String avgChartTime = nf.format(1.0 * pTotalChartTime / pCount);
        System.out.println("Average chart construction time: " + avgChartTime);
        System.out.println("Max chart construction time: " + pMaxChartTime);
        String avgUnpackingTime = nf.format(1.0 * pTotalUnpackingTime / pCount);
        System.out.println("Average unpacking time: " + avgUnpackingTime);
        System.out.println("Max unpacking time: " + pMaxUnpackingTime);
        System.out.println("Supertagger beta tallies:");
        Set<Double> betas = pBetaTallies.keySet();
        for (double beta : betas) {
        	int tally = pBetaTallies.get(beta);
            System.out.println(beta + "\t" + tally);
        }
        String avgF = nf.format(100.0 * totalF / pCount);
        System.out.println("Labeled f-score: " + avgF);
        String avgRecall = nf.format(100.0 * totalRecall / pCount);
        System.out.println("Labeled recall: " + avgRecall);
        String avgPrecision = nf.format(100.0 * totalPrecision / pCount);
        System.out.println("Labeled precision: " + avgPrecision);
        String avgDepsF = nf.format(100.0 * totalDepsF / pCount);
        System.out.println("Labeled f-score deps only: " + avgDepsF);
        String avgDepsRecall = nf.format(100.0 * totalDepsRecall / pCount);
        System.out.println("Labeled recall deps only: " + avgDepsRecall);
        String avgDepsPrecision = nf.format(100.0 * totalDepsPrecision / pCount);
        System.out.println("Labeled precision deps only: " + avgDepsPrecision);
        String avgUnlabeledDepsF = nf.format(100.0 * totalUnlabeledDepsF / pCount);
        System.out.println("Unlabeled deps f-score: " + avgUnlabeledDepsF);
        String avgUnlabeledDepsRecall = nf.format(100.0 * totalUnlabeledDepsRecall / pCount);
        System.out.println("Unlabeled deps recall: " + avgUnlabeledDepsRecall);
        String avgUnlabeledDepsPrecision = nf.format(100.0 * totalUnlabeledDepsPrecision / pCount);
        System.out.println("Unlabeled deps precision: " + avgUnlabeledDepsPrecision);
        String avgFComplete = nf.format(100.0 * totalFComplete / pCompleteCount);
        System.out.println("Labeled f-score for complete parses: " + avgFComplete);
        String avgRecallComplete = nf.format(100.0 * totalRecallComplete / pCompleteCount);
        System.out.println("Labeled recall for complete parses: " + avgRecallComplete);
        String avgPrecisionComplete = nf.format(100.0 * totalPrecisionComplete / pCompleteCount);
        System.out.println("Labeled precision for complete parses: " + avgPrecisionComplete);
        String avgDepsFComplete = nf.format(100.0 * totalDepsFComplete / pCompleteCount);
        System.out.println("Labeled f-score deps only for complete parses: " + avgDepsFComplete);
        String avgDepsRecallComplete = nf.format(100.0 * totalDepsRecallComplete / pCompleteCount);
        System.out.println("Labeled recall deps only for complete parses: " + avgDepsRecallComplete);
        String avgDepsPrecisionComplete = nf.format(100.0 * totalDepsPrecisionComplete / pCompleteCount);
        System.out.println("Labeled precision deps only for complete parses: " + avgDepsPrecisionComplete);
        String avgUnlabeledDepsFComplete = nf.format(100.0 * totalUnlabeledDepsFComplete / pCompleteCount);
        System.out.println("Unlabeled deps f-score for complete parses: " + avgUnlabeledDepsFComplete);
        String avgUnlabeledDepsRecallComplete = nf.format(100.0 * totalUnlabeledDepsRecallComplete / pCompleteCount);
        System.out.println("Unlabeled deps recall for complete parses: " + avgUnlabeledDepsRecallComplete);
        String avgUnlabeledDepsPrecisionComplete = nf.format(100.0 * totalUnlabeledDepsPrecisionComplete / pCompleteCount);
        System.out.println("Unlabeled deps precision for complete parses: " + avgUnlabeledDepsPrecisionComplete);
        if (oracleBetter > 0) System.out.println("Oracle better: " + oracleBetter);
        if (goldMissing > 0) System.out.println("Gold missing: " + goldMissing);
        System.out.println();
    }
    
    /** Shows the various realization totals. */
    public void showStats() {
    	int rCompleteCount = rCount - rBadCount;
        String rComplete = "" + rCompleteCount;
        String rCompletePct = "" + nf.format(100.0 * rCompleteCount / rCount) + "%";
        System.out.println("Strings realized completely (in fragments): " + rComplete + " (" + rBadCount + ") " + rCompletePct);
        String rInexact = "" + (rCount - rExactCount);
        String rExactPct = "" + nf.format(100.0 * rExactCount / rCount) + "%";
        System.out.println("Strings realized exactly (inexactly): " + rExactCount + " (" + rInexact + ") " + rExactPct);
        System.out.println("Strings where realization finished: " + rDoneCount);
        String avgScore = nf.format(totalScore / rCount);
        System.out.println("Avg score: " + avgScore);
        String avgScoreComplete = nf.format(totalScoreComplete / rCompleteCount);
        System.out.println("Avg score for complete realizations: " + avgScoreComplete);
        String meanReciprocalRank = nf.format(totalReciprocalRank / rCount);
        System.out.println("Mean reciprocal rank: " + meanReciprocalRank);
        String residualMRR = (rCount == rExactCount) 
            ? "n/a" 
            : nf.format((totalReciprocalRank - rExactCount) / (rCount - rExactCount));
        System.out.println("Residual mean reciprocal rank: " + residualMRR);
        String avgNodes = nf.format(totalNominals * 1.0 / rCount); 
        String avgTokens = nf.format(totalTokens * 1.0 / rCount); 
        System.out.println("Avg num nodes, words: " + avgNodes + ", " + avgTokens);
        System.out.println("Num words (min-max): " + minTokens + "-" + maxTokens);
        String avgRuleApps = nf.format(totalRuleApps * 1.0 / rCount);
        System.out.println("Avg num rule apps: " + avgRuleApps);
        String avgEdges = nf.format(totalEdges * 1.0 / rCount);
        String avgEdgesCreated = nf.format(totalEdgesCreated * 1.0 / rCount);
        String avgUnprunedEdges = nf.format(totalUnprunedEdges * 1.0 / rCount);
        System.out.println("Avg num edges in chart: " + avgEdges);
        System.out.println("Avg num edges created: " + avgEdgesCreated);
        System.out.println("Avg num unpruned edges: " + avgUnprunedEdges);
        String avgRemoved = nf.format(totalPrunedRemoved * 1.0 / rCount);
        String avgNeverAdded = nf.format(totalPrunedNeverAdded * 1.0 / rCount);
        System.out.println("Avg num pruned edges removed, never added: "  + avgRemoved + ", " + avgNeverAdded);
        String avgCellMax = nf.format(totalCellMax * 1.0 / rCount);
        System.out.println("Avg cell max: " + avgCellMax);
        String avgNewBest = nf.format(totalNewBest * 1.0 / rCount);
        System.out.println("Total, avg num new best realizations: " + totalNewBest + ", " + avgNewBest);
        String avgLex = nf.format(lexMap.mean());
        String stdLex = nf.format(lexMap.sigma());
        System.out.println("Avg (std) time 'til lex lookup finished: " + avgLex + " (" + stdLex + ")");
        System.out.println("Max time 'til lex lookup finished: " + maxLex + " (" + maxLexStr + ")");
        String avgFirst = nf.format(firstMap.mean());
        String stdFirst = nf.format(firstMap.sigma());
        System.out.println("Avg (std) time 'til first realization: " + avgFirst + " (" + stdFirst + ")");
        System.out.println("Max time 'til first realization: " + maxFirst + " (" + maxFirstStr + ")");
        String avgBest = nf.format(bestMap.mean());
        String stdBest = nf.format(bestMap.sigma());
        System.out.println("Avg (std) time 'til best realization: " + avgBest + " (" + stdBest + ")");
        System.out.println("Max time 'til best realization: " + maxBest + " (" + maxBestStr +")");
        System.out.println("Max time 'til new best realization: " + maxNewBest + " (" + maxNewBestStr +")");
        String avgPacked = nf.format(totalPacked / rCount);
        System.out.println("Avg time 'til done packing: " + avgPacked);
        System.out.println("Max time 'til done packing: " + maxPacked + " (" + maxPackedStr +")");
        String avgStoppedOrDone = nf.format(allMap.mean());
        String stdStoppedOrDone = nf.format(allMap.sigma());
        System.out.println("Avg (std) time 'til stopped/done with realizations: " + avgStoppedOrDone + " (" + stdStoppedOrDone + ")");
        System.out.println("Max time 'til stopped/done with realizations: " + maxStoppedOrDone + " (" + maxStoppedOrDoneStr +")");
        if (oracleBetter > 0) System.out.println("Oracle better: " + oracleBetter);
        if (goldMissing > 0) System.out.println("Gold missing: " + goldMissing);
        if (statsfile != null) {
            Document doc = new Document();
            Element root = new Element("rstats");
            doc.setRootElement(root);
            Element counts = new Element("counts");
            root.addContent(counts);
            counts.setAttribute("realized", "" + rCount);
            counts.setAttribute("complete", "" + rComplete);
            counts.setAttribute("fragments", "" + rBadCount);
            counts.setAttribute("exact", "" + rExactCount);
            counts.setAttribute("inexact", rInexact);
            counts.setAttribute("finished", "" + rDoneCount);
            Element overall = new Element("overall");
            root.addContent(overall);
            overall.setAttribute("avg-score", avgScore);
            overall.setAttribute("mean-reciprocal-rank", meanReciprocalRank);
            overall.setAttribute("residual-mrr", residualMRR);
            overall.setAttribute("avg-nodes", avgNodes);
            overall.setAttribute("avg-words", avgTokens);
            overall.setAttribute("min-words", "" + minTokens);
            overall.setAttribute("max-words", "" + maxTokens);
            Element rules = new Element("rules");
            root.addContent(rules);
            rules.setAttribute("avg-apps", avgRuleApps);
            Element edges = new Element("edges");
            root.addContent(edges);
            edges.setAttribute("avg", avgEdges);
            edges.setAttribute("avg-unpruned", avgUnprunedEdges);
            edges.setAttribute("avg-removed", avgRemoved);
            edges.setAttribute("avg-never-added", avgNeverAdded);
            edges.setAttribute("avg-cell-max", avgCellMax);
            if (oracleBetter > 0) edges.setAttribute("oracle-better", "" + oracleBetter);
            if (goldMissing > 0) edges.setAttribute("gold-missing", "" + goldMissing);
            Element newBest = new Element("new-best");
            root.addContent(newBest);
            newBest.setAttribute("total", "" + totalNewBest);
            newBest.setAttribute("avg", avgNewBest);
            Element times = new Element("times-summary");
            root.addContent(times);
            times.setAttribute("avg-lex", avgLex);
            times.setAttribute("std-lex", stdLex);
            times.setAttribute("avg-first", avgFirst);
            times.setAttribute("std-first", stdFirst);
            times.setAttribute("max-first", "" + maxFirst);
            times.setAttribute("avg-best", avgBest);
            times.setAttribute("std-best", stdBest);
            times.setAttribute("max-best", "" + maxBest);
            times.setAttribute("max-new-best", "" + maxNewBest);
            times.setAttribute("avg-packed", avgPacked);
            times.setAttribute("max-packed", "" + maxPacked);
            times.setAttribute("avg-stopped-or-done", avgStoppedOrDone);
            times.setAttribute("std-stopped-or-done", stdStoppedOrDone);
            times.setAttribute("max-stopped-or-done", "" + maxStoppedOrDone);
            Element strings = new Element("max-strings");
            root.addContent(strings);
            Element lex = new Element("lex");
            strings.addContent(lex);
            lex.addContent(maxLexStr);
            Element first = new Element("first");
            strings.addContent(first);
            first.addContent(maxFirstStr);
            Element best = new Element("best");
            strings.addContent(best);
            best.addContent(maxBestStr);
            Element newBest2 = new Element("new-best");
            strings.addContent(newBest2);
            newBest2.addContent(maxNewBestStr);
            Element packed = new Element("packed");
            strings.addContent(packed);
            packed.addContent(maxPackedStr);
            Element stoppedOrDone = new Element("stopped-or-done");
            strings.addContent(stoppedOrDone);
            stoppedOrDone.addContent(maxStoppedOrDoneStr);
            Element scores = new Element("scores");
            root.addContent(scores);
            for (int i = 0; i < bestActualScores.size(); i++) {
                Element score = new Element("score");
                scores.addContent(score);
                score.setAttribute("val", bestActualScores.get(i).toString());
                score.setAttribute("est", bestEstimatedScores.get(i).toString());
                score.setAttribute("rank", itemRanks.get(i).toString());
            }
            firstMap.saveTimes(root);
            bestMap.saveTimes(root);
            allMap.saveTimes(root);
            try {
                FileOutputStream out = new FileOutputStream(statsfile);
                grammar.serializeXml(doc, out);
                out.flush();
            }
            catch (IOException exc) {
                System.out.println("Unable to write stats to: " + statsfile + " (" + exc + ")");
            }
        }
    }
    
    // show outcome, with wrapping
    private static void showOutcome(String parseResult, String realizeResult, String starForBadSentence, String str) {
        showOutcome(parseResult, realizeResult, starForBadSentence, str, null);
    }
    
    // show outcome including best realization
    private static void showOutcome(String parseResult, String realizeResult, String starForBadSentence, 
                                    String str, String bestRealization) 
    {
        System.out.print(parseResult + "\t" + realizeResult + "\t");
        simpleWrap(starForBadSentence + str);
        if (bestRealization != null) {
            System.out.print("\t\t");
            simpleWrap("(best: " + bestRealization + ")");
        }
    }
    
    // does simple wrapping at TEXTWIDTH
    private static void simpleWrap(String str) {
        int TEXTWIDTH = 60;
        for (int i = 0; i <= (str.length()-1)/TEXTWIDTH; i++) {
            if (i != 0) {
                System.out.print("\t\t");
            }
            System.out.println(str.substring(i*TEXTWIDTH, Math.min(i*TEXTWIDTH + TEXTWIDTH, str.length())));
        }
    }
    
    // formats to three decimal places
    private static final NumberFormat nf = initNF();
    private static NumberFormat initNF() { 
        NumberFormat f = NumberFormat.getInstance();
        f.setMinimumIntegerDigits(1);
        f.setMinimumFractionDigits(1);
        f.setMaximumFractionDigits(2);
        return f;
    }
    
    // formats to three decimal places in scientific notation
    private static final NumberFormat nfE = initNFE();
    private static NumberFormat initNFE() {
    	DecimalFormat f = new DecimalFormat("0.###E0");
        return f;
    }
    
    /** Shows realizer settings for current test. */
    static void showRealizerSettings() {
        // get, show prefs
        Preferences prefs = Preferences.userNodeForPackage(TextCCG.class);
        boolean useIndexing = prefs.getBoolean(EdgeFactory.USE_INDEXING, true);
        boolean useChunks = prefs.getBoolean(EdgeFactory.USE_CHUNKS, true);
        boolean useLicensing = prefs.getBoolean(EdgeFactory.USE_FEATURE_LICENSING, true);
        boolean useCombos = prefs.getBoolean(opennlp.ccg.realize.Chart.USE_COMBOS, true);
        boolean usePacking = prefs.getBoolean(opennlp.ccg.realize.Chart.USE_PACKING, false);
        int timeLimit = prefs.getInt(opennlp.ccg.realize.Chart.TIME_LIMIT, opennlp.ccg.realize.Chart.NO_TIME_LIMIT);
        double nbTimeLimit = prefs.getDouble(opennlp.ccg.realize.Chart.NEW_BEST_TIME_LIMIT, opennlp.ccg.realize.Chart.NO_TIME_LIMIT);
        int pruningVal = prefs.getInt(opennlp.ccg.realize.Chart.PRUNING_VALUE, opennlp.ccg.realize.Chart.NO_PRUNING);
        int cellPruningVal = prefs.getInt(opennlp.ccg.realize.Chart.CELL_PRUNING_VALUE, opennlp.ccg.realize.Chart.NO_PRUNING);
        String msg = "Timing realization with index filtering " + ((useIndexing) ? "on" : "off") + ", "; 
        msg += "chunks " + ((useChunks) ? "on" : "off") + ", "; 
        msg += "licensing " + ((useLicensing) ? "on" : "off") + ", ";
        if (usePacking) msg += "packing on, ";
        else {
            msg += "combos " + ((useCombos) ? "on" : "off") + ", ";
            if (timeLimit == opennlp.ccg.realize.Chart.NO_TIME_LIMIT) msg += "no time limit, ";
            else msg += "a time limit of " + timeLimit + " ms, ";
            if (nbTimeLimit == opennlp.ccg.realize.Chart.NO_TIME_LIMIT) msg += "no new best time limit, ";
            else {
                msg += "a new best time limit of ";
                if (nbTimeLimit >= 1) msg += ((int)nbTimeLimit) + " ms, ";
                else msg += nbTimeLimit + " of first, ";
            }
        }
        if (pruningVal == opennlp.ccg.realize.Chart.NO_PRUNING) msg += "no pruning, ";
        else msg += "a pruning value of " + pruningVal + ", ";
        msg += "and ";
        if (cellPruningVal == opennlp.ccg.realize.Chart.NO_PRUNING) msg += "no cell pruning";
        else msg += "a cell pruning value of " + cellPruningVal;
        System.out.println(msg);
        System.out.println();
    }
    

    /** 
     * Writes the target strings from the given testbed to the given textfile.
     */
    public void writeTargets(File tbFile, String textfile) throws IOException {
        writeTargets(tbFile, textfile, false, false, false);
    }
    
    /** 
     * Writes the target strings with semantic class replacement 
     * from the given testbed to the given textfile. 
     */
    public void writeTargetsSC(File tbFile, String textfile) throws IOException {
        writeTargets(tbFile, textfile, true, false, false);
    }
    
    /** 
     * Writes the target strings with all associated factors 
     * from the given testbed to the given textfile. 
     */
    public void writeTargetsF(File tbFile, String textfile) throws IOException {
        writeTargets(tbFile, textfile, false, true, false);
    }
    
    /** 
     * Writes the target strings with all associated factors with semantic class replacement  
     * from the given testbed to the given textfile. 
     */
    public void writeTargetsFSC(File tbFile, String textfile) throws IOException {
        writeTargets(tbFile, textfile, true, true, false);
    }
    
    // writes targets, optionally with sem class replacement or factors, 
    // and optionally reversing the words; ungrammatical options are filtered out 
    private void writeTargets(
    		File tbFile, String filename, 
    		boolean semClassReplacement, boolean withFactors, 
    		boolean reverse
    ) throws IOException {
        // open text file
        String option = "";
        if (withFactors) option = " with factors";
        if (semClassReplacement) option += " with semantic class replacement";
        if (reverse) option += ", reversed";
        System.out.println("Writing text file" + option + ": " + filename);
        System.out.println();
        PrintWriter tOut = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
        HashSet<String> unique = new HashSet<String>(); 
        Tokenizer tokenizer = grammar.lexicon.tokenizer;
        // loop through files
        for (File f : getXMLFiles(tbFile)) {
	        // load testbed
	        System.out.println("Loading testbed from: " + f);
	        RegressionInfo tbInfo = new RegressionInfo(grammar, f);
	        int numItems = tbInfo.numberOfItems();
	        // do each test item
	        for (int i = 0; i < numItems; i++) {
	            // check even/odd only
	            if (i % 2 == 1 && evenOnly) continue;
	            if (i % 2 == 0 && oddOnly) continue;
	            RegressionInfo.TestItem testItem = tbInfo.getItem(i); 
	        	// check grammatical
	        	if (testItem.numOfParses == 0) continue;
	            String s = testItem.sentence;
	            // get parsed words if doing more than just text
	            List<Word> words = null;
	            if (semClassReplacement || withFactors) {
	                // use words from sign or pre-parsed full words if available
	            	if (testItem.sign != null) 
	            		words = testItem.sign.getWords();
	            	else if (testItem.fullWords != null) 
	                    words = tokenizer.tokenize(testItem.fullWords, true);
	                // otherwise parse
	                else words = grammar.getParsedWords(s);
	            }
	            else words = tokenizer.tokenize(s);
	            // reverse, if apropos
	            if (reverse) {
	                List<Word> tmp = words;
	                words = new ArrayList<Word>(words.size());
	                words.add(Word.createWord("<s>"));
	                for (int j = tmp.size()-1; j >= 0; j--) {
	                    Word w = tmp.get(j);
	                    if (w.getForm() == "<s>" || w.getForm() == "</s>") continue; // skip <s> or </s>
	                    words.add(w);
	                }
	                words.add(Word.createWord("</s>"));
	            }
	            // write str, add to unique set
	            String str = (!withFactors)
	                ? tokenizer.getOrthography(words, semClassReplacement)
	                : tokenizer.format(words, semClassReplacement);
	            tOut.println(str);
	            unique.add(str);
	            System.out.print("."); // indicate progress
	        }
	        System.out.println();
        }
        tOut.flush(); tOut.close();
        System.out.println();
        System.out.println("Unique strings: " + unique.size());
        System.out.println();
    }
    
    private void writeDerivationFactors(File tbFile, String filename) throws IOException {
        // open text file
        System.out.println("Writing derivation factors file: " + filename);
        System.out.println();
        PrintWriter tOut = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
        Tokenizer tokenizer = grammar.lexicon.tokenizer;
        // loop through files
        for (File f : getXMLFiles(tbFile)) {
	        // load testbed
	        System.out.println("Loading testbed from: " + f);
	        RegressionInfo tbInfo = new RegressionInfo(grammar, f);
	        int numItems = tbInfo.numberOfItems();
	        // do each test item, using the saved sign
	        for (int i = 0; i < numItems; i++) {
	            RegressionInfo.TestItem testItem = tbInfo.getItem(i); 
	        	if (testItem.numOfParses == 0) continue; // check grammatical
	        	Sign sign = testItem.sign;
	        	List<Word> factors = GenerativeSyntacticModel.getFactors(sign);
	        	for (Word w : factors) {
	        		tOut.print(tokenizer.format(w));
	        		tOut.print(" ");
	        	}
	        	tOut.println();
	            System.out.print("."); // indicate progress
	        }
	        System.out.println();
        }
        tOut.flush(); tOut.close();
        System.out.println();
    }
    
    
    /** Command-line routine for regression testing. */
    public static void main(String[] args) throws IOException { 

        String usage = "java opennlp.ccg.test.Regression \n" + 
                       "  (-noparsing) (-norealization) (-even|-odd) (-gc) \n" + 
                       "  (-nullscorer) (-randomscorer) \n" + 
                       "  (-depthfirst) (-exactmatches) (-aanfilter (<excfile>)) \n" +
                       "  (-scorer <scorerclass>) \n" +
                       "  (-parsescorer <scorerclass>) \n" +
                       "  (-extractor <extractorclass>) \n" +
                       "  (-ngrampruningstrategy) (-pruningstrategy <pruningstrategyclass>) \n" +
                       "  (-hypertagger <hypertaggerclass> | -htconfig <configfile>) (-htgold) \n" +
                       "  (-supertagger <supertaggerclass> | -stconfig <configfile>) \n" +
                       "  (-ngramorder N) (-lm|-lmsc <lmfile>) \n" + 
                       "  (-srilm " + Arrays.toString(SRILMNgramModelType.values()) + ")\n"+
                       "  (-flm|-flmsc <flmfile>) \n" + 
                       "  (-text|-textsc|-textf|-textfsc <textfile>) (-reverse) \n" +
                       "  (-derivf <derivfactorsfile>) \n" +
                       "  (-2events <eventfile>) (-includegoldinevents) \n" +
                       "  (-2apml <apmldir>) (-bleu <bleufileprefix>) \n" +
                       "  (-nbestrealfile <nbestrealfile>) (-nbestnormbleu) (-realserdir <realserdir>) \n" + 
                       "  (-nbestincludelfs) \n" +
                       "  (-rescorefile <rescorefile>) \n" + 
                       "  (-nbestparsefile <nbestparsefile>) \n" + 
                       "  (-g <grammarfile>) (-s <statsfile>) (<regressionfile>|<regressiondir>)";
                       
        if (args.length > 0 && args[0].equals("-h")) {
            System.out.println("Usage: \n\n" + usage);
            System.exit(0);
        }
        
        // setup Regression tester
        Regression tester = new Regression();

        // args
        String grammarfile = "grammar.xml";
        String regressionfile = "testbed.xml";
        boolean depthFirst = false;
        boolean aanfilter = false;
        String excfile = null;
        String scorerClass = null;
        String parseScorerClass = null;
        String extractorClass = null;
        boolean ngrampruningstrategy = false;
        String pruningStrategyClass = null; 
        String hypertaggerClass = null, htconfig = null; 
        boolean htgold = false;
        String supertaggerClass = null, stconfig = null;
        String lmfile = null;
        String flmfile = null;
        boolean useSemClasses = false;
        boolean withFactors = false;
        boolean reverse = false;
        String textfile = null;
        String derivfactorsfile = null;
        boolean srilm = false;
        SRILMNgramModelType srilmModelType = SRILMNgramModelType.STANDARD;
        
        for (int i = 0; i < args.length; i++) {
        	if (args[i].startsWith("-D")) {
        		String prop = args[i].substring(2); int equalpos = prop.indexOf("=");
        		String key = prop.substring(0, equalpos); String val = prop.substring(equalpos+1);
        		System.setProperty(key, val); continue;
        	}
            if (args[i].equals("-noparsing")) { tester.doParsing = false; continue; }
            if (args[i].equals("-norealization")) { tester.doRealization = false; continue; }
            if (args[i].equals("-even")) { tester.evenOnly = true; continue; }
            if (args[i].equals("-odd")) { tester.oddOnly = true; continue; }
            if (args[i].equals("-gc")) { tester.doGC = true; continue; }
            if (args[i].equals("-nullscorer")) { 
            	tester.scorer = SignScorer.nullScorer; tester.parseScorer = SignScorer.nullScorer; continue; 
            }
            if (args[i].equals("-randomscorer")) { 
            	tester.scorer = SignScorer.randomScorer; tester.parseScorer = SignScorer.randomScorer; continue; 
            }
            if (args[i].equals("-depthfirst")) { depthFirst = true; continue; }
            if (args[i].equals("-exactmatches")) { tester.exactMatches = true; continue; }
            if (args[i].equals("-aanfilter")) {
                aanfilter = true; 
                if (i < args.length-1 && args[i+1].charAt(0) != '-') excfile = args[++i]; 
                continue;
            }
            if (args[i].equals("-scorer")) { scorerClass = args[++i]; continue; }
            if (args[i].equals("-parsescorer")) { parseScorerClass = args[++i]; continue; }
            if (args[i].equals("-extractor")) { extractorClass = args[++i]; continue; }
            if (args[i].equals("-ngrampruningstrategy")) { ngrampruningstrategy = true; continue; }
            if (args[i].equals("-pruningstrategy")) { pruningStrategyClass = args[++i]; continue; }
            if (args[i].equals("-hypertagger")) { hypertaggerClass = args[++i]; continue; }
            if (args[i].equals("-htconfig")) { htconfig = args[++i]; continue; }
            if (args[i].equals("-htgold")) { htgold = true; continue; }
            if (args[i].equals("-supertagger")) { supertaggerClass = args[++i]; continue; }
            if (args[i].equals("-stconfig")) { stconfig = args[++i]; continue; }
            if (args[i].equals("-ngramorder")) { tester.ngramOrder = Integer.parseInt(args[++i]); continue; }
            if (args[i].equals("-lm")) { lmfile = args[++i]; continue; }
            if (args[i].equals("-lmsc")) { lmfile = args[++i]; useSemClasses = true; continue; }
            if (args[i].equals("-flm")) { flmfile = args[++i]; continue; }
            if (args[i].equals("-flmsc")) { flmfile = args[++i]; useSemClasses = true; continue; }
            if (args[i].equals("-reverse")) { reverse = true; continue; }
            if (args[i].equals("-text")) { textfile = args[++i]; continue; }
            if (args[i].equals("-textsc")) { textfile = args[++i]; useSemClasses = true; continue; }
            if (args[i].equals("-textf")) { textfile = args[++i]; withFactors = true; continue; }
            if (args[i].equals("-textfsc")) { textfile = args[++i]; useSemClasses = true; withFactors = true; continue; }
            if (args[i].equals("-derivf")) { derivfactorsfile = args[++i]; continue; }
            if (args[i].equals("-2events")) { tester.eventfile = args[++i]; continue; }
            if (args[i].equals("-includegoldinevents")) { tester.includeGoldInEvents = true; continue; }
            if (args[i].equals("-2apml")) { tester.apmldir = args[++i]; continue; }
            if (args[i].equals("-bleu")) { tester.bleufileprefix = args[++i]; continue; }
            if (args[i].equals("-nbestrealfile")) { tester.nbestrealfile = args[++i]; continue; }
            if (args[i].equals("-nbestnormbleu")) { tester.nbestnormbleu = true; continue; }
            if (args[i].equals("-realserdir")) { tester.realserdir = args[++i]; continue; }
            if (args[i].equals("-nbestincludelfs")) { tester.nbestincludelfs = true; continue; }
            if (args[i].equals("-rescorefile")) { tester.rescorefile = args[++i]; continue; }
            if (args[i].equals("-nbestparsefile")) { tester.nbestparsefile = args[++i]; continue; }
            if (args[i].equals("-g")) { grammarfile = args[++i]; continue; }
            if (args[i].equals("-s")) { tester.statsfile = args[++i]; continue; }
            if (args[i].equals("-srilm")) { 
            	srilm = true;
            	if(i < (args.length - 1)) {
	            	String type = args[i + 1];
	            	try {
	            		srilmModelType = SRILMNgramModelType.valueOf(type);
	            		i++;
	            	}
	            	catch(IllegalArgumentException iae) {
	            		srilmModelType = SRILMNgramModelType.STANDARD;
	            		System.err.println(
	            			"Warning: unknown SRILM n-gram model type " + type
	            				+ " specified, using default ("
	            				+ srilmModelType + ")");
	            	}
            	}
            	continue;
            }
            regressionfile = args[i];
        }
        
        // load grammar
        URL grammarURL = new File(grammarfile).toURI().toURL();
        System.out.println("Loading grammar from URL: " + grammarURL);
        tester.grammar = new Grammar(grammarURL);
        System.out.println();
        
        // with -aanfilter (<excfile) option, instantiate AAnFilter
        AAnFilter aanFilter = null;
        if (aanfilter) {
            if (excfile != null) System.out.println("Loading a/an exceptions from file: " + excfile);
            aanFilter = (excfile != null) ? new AAnFilter(excfile) : new AAnFilter();
        }
        
        // instantiate scorer, if any
        if (scorerClass != null) {
            try {
                System.out.println("Instantiating sign scorer from class: " + scorerClass);
                SignScorer scorer = (SignScorer) Class.forName(scorerClass).newInstance();
            	if (scorer instanceof NgramScorer) {
                    NgramScorer lmScorer = (NgramScorer) scorer;
                    if (aanfilter) lmScorer.addFilter(aanFilter);
                    tester.ngramOrder = lmScorer.getOrder();
                }
                tester.scorer = scorer;
                System.out.println();
            } catch (Exception exc) {
                throw (RuntimeException) new RuntimeException().initCause(exc);
            }
        }
        
        // with -lm|-lmsc options, load n-gram model
        if (lmfile != null) {
            int order = (tester.ngramOrder > 0) ? tester.ngramOrder : 3;
            String reversedStr = (reverse) ? "reversed " : "";
            System.out.println("Loading " + reversedStr + order
            		+ "-gram model from file: " + lmfile);
            NgramScorer lmScorer = (srilm)
            	? new SRILMNgramModel(order, new File(lmfile), useSemClasses, 
            			srilmModelType)
            	: new StandardNgramModel(order, lmfile, useSemClasses);
            if (reverse) lmScorer.setReverse(true);
            if (aanfilter) lmScorer.addFilter(aanFilter);
            tester.scorer = lmScorer;
            System.out.println();
        }

        // with -flm|-flmsc options, load factored n-gram model family
        if (flmfile != null) {
            String reversedStr = (reverse) ? "reversed " : "";
            System.out.println("Loading " + reversedStr + "factored n-gram model family from file: " + flmfile);
            NgramScorer flmScorer = new FactoredNgramModelFamily(flmfile, useSemClasses);
            if (reverse) flmScorer.setReverse(true);
            if (aanfilter) flmScorer.addFilter(aanFilter);
            tester.scorer = flmScorer;
            tester.ngramOrder = flmScorer.getOrder();
            System.out.println();
        }

        // with -text|-textsc|-textf|-textfsc options, just write text file and exit
        if (textfile != null) {
        	File tbFile = new File(regressionfile);
            tester.writeTargets(tbFile, textfile, useSemClasses, withFactors, reverse);
            System.exit(0);
        }
        
        // with -derivf option, just write derivation factors file and exit
        if (derivfactorsfile != null) {
        	File tbFile = new File(regressionfile);
        	tester.writeDerivationFactors(tbFile, derivfactorsfile);
            System.exit(0);
        }
        
        // setup parser
        if (tester.doParsing) {
            tester.parser = new Parser(tester.grammar);
            // instantiate scorer, if any
            if (parseScorerClass != null) {
                try {
                    System.out.println("Instantiating parsing sign scorer from class: " + parseScorerClass);
                    tester.parseScorer = (SignScorer) Class.forName(parseScorerClass).newInstance();
                    tester.showParseStats = true; // turn parsing stats on
                    System.out.println();
                } catch (Exception exc) {
                    throw (RuntimeException) new RuntimeException().initCause(exc);
                }
            }
            // set parser scorer, if any
            if (tester.parseScorer != null) tester.parser.setSignScorer(tester.parseScorer);
            // also turn on parse stats if doing n-best output
            if (tester.nbestparsefile != null) tester.showParseStats = true;
            // instantiate supertagger, if any
            if (supertaggerClass != null || stconfig != null) {
                try {
                	Supertagger supertagger;
                	if (supertaggerClass != null) {
	                    System.out.println("Instantiating supertagger from class: " + supertaggerClass);
	                    supertagger = (Supertagger) Class.forName(supertaggerClass).newInstance();
                	}
                	else {
                		System.out.println("Instantiating supertagger from config file: " + stconfig);
                		supertagger = WordAndPOSDictionaryLabellingStrategy.supertaggerFactory(stconfig);
                	}
                    tester.parser.setSupertagger(supertagger);
                    if (tester.eventfile != null) {
                    	supertagger.setIncludeGold(true); // use gold tags during training
                    	tester.parser.setSupertaggerMostToLeastRestrictiveDirection(false); // reverse direction to start with least restrictive setting
                    }
                    System.out.println();
                } catch (Exception exc) {
                    throw (RuntimeException) new RuntimeException().initCause(exc);
                }
            }
        }
        
        // setup realizer, show settings
        if (tester.doRealization) {
            tester.realizer = new Realizer(tester.grammar);
            tester.realizer.depthFirst = depthFirst;
            // instantiate pruning strategy, if any
            if (ngrampruningstrategy) {
                int order = (tester.ngramOrder > 0) ? tester.ngramOrder : 3;
                System.out.println("Instantiating n-gram diversity pruning strategy with order " + order);
                tester.realizer.pruningStrategy = new NgramDiversityPruningStrategy(order);
                System.out.println();
            }
            if (pruningStrategyClass != null) {
                try {
                    System.out.println("Instantiating pruning strategy from class: " + pruningStrategyClass);
                    tester.realizer.pruningStrategy = (PruningStrategy) Class.forName(pruningStrategyClass).newInstance();
                    System.out.println();
                } catch (Exception exc) {
                    throw (RuntimeException) new RuntimeException().initCause(exc);
                }
            }
            if (hypertaggerClass != null || htconfig != null) {
                try {
                	Hypertagger hypertagger;
                	if (hypertaggerClass != null) {
                        System.out.println("Instantiating hypertagger from class: " + hypertaggerClass);
                        hypertagger = (Hypertagger) Class.forName(hypertaggerClass).newInstance();
                	}
                	else {
                		System.out.println("Instantiating hypertagger from config file: " + htconfig);
                		hypertagger = ZLMaxentHypertagger.ZLMaxentHypertaggerFactory(htconfig);
                	}
                    tester.realizer.hypertagger = hypertagger;
                    if (tester.eventfile != null) {
                    	hypertagger.setIncludeGold(true); // use gold tags during training
                        // NB: could investigate most-to-least-restrictive direction
                    }
                    if (htgold) hypertagger.setIncludeGold(true); // use gold tags, eg for paraphrasing
                    System.out.println();
                } catch (Exception exc) {
                    throw (RuntimeException) new RuntimeException().initCause(exc);
                }
            }
            showRealizerSettings();
        }
        
        // ensure dir for event file exists; 
        // set up feature extractor
        if (tester.eventfile != null) {
    		File tmp = new File(tester.eventfile); 
    		File tmpParent = tmp.getParentFile(); 
    		if (tmpParent != null) tmpParent.mkdirs(); 
            System.out.println("Writing event file to: " + tester.eventfile);
            System.out.println();
        }
        
        // instantiate feature extractor, if generating events 
        if (tester.eventfile != null) {
        	// ensure just doing parsing or realization
        	if (tester.doParsing && tester.doRealization) {
        		throw new RuntimeException("Events can't be generated for parsing and realization at the same time.");
        	}
        	FeatureExtractor extractor = null;
        	if (extractorClass != null) {
	            try {
	                System.out.println("Instantiating feature extractor from class: " + extractorClass);
	                extractor = (FeatureExtractor) Class.forName(extractorClass).newInstance();
	                tester.featureExtractor = extractor;
	                System.out.println();
	            } catch (Exception exc) {
	                throw (RuntimeException) new RuntimeException().initCause(exc);
	            }
        	}
        	// use or combine with scorer, if it's also a feature extractor
            if (tester.scorer instanceof FeatureExtractor) {
            	if (extractor != null)
            		tester.featureExtractor = new ComposedFeatureExtractor(
            					new FeatureExtractor[] {
            							(FeatureExtractor)tester.scorer, extractor
            					}
            				);
            	else tester.featureExtractor = (FeatureExtractor) tester.scorer;
            }
            else if (tester.parseScorer instanceof FeatureExtractor) {
            	if (extractor != null)
            		tester.featureExtractor = new ComposedFeatureExtractor(
            					new FeatureExtractor[] {
            							(FeatureExtractor)tester.parseScorer, extractor
            					}
            				);
            	else tester.featureExtractor = (FeatureExtractor) tester.parseScorer;
            }
            // otherwise use an n-gram precision model 
            if (tester.featureExtractor == null)
            	tester.featureExtractor = new NgramPrecisionModel(new String[]{""}, true);
            // set new alphabet
            tester.featureExtractor.setAlphabet(new Alphabet(10000));
        }
        
        // ensure apmldir exists
        if (tester.apmldir != null) {
            File apmlDir = new File(tester.apmldir);
            if (!apmlDir.exists()) { apmlDir.mkdirs(); }
            System.out.println("Writing APML files to dir: " + tester.apmldir);
            System.out.println();
        }

        // ensure dir for bleu files exists
        if (tester.bleufileprefix != null) {
    		File tmp = new File(tester.bleufileprefix + "-gen.sgm");  
    		File tmpParent = tmp.getParentFile(); 
    		if (tmpParent != null) tmpParent.mkdirs(); 
            System.out.println("Writing BLEU files to: " + tester.bleufileprefix + "-*.sgm");
            System.out.println();
        }
        
        // ensure dir for nbestrealfile exists
        if (tester.nbestrealfile != null) {
    		File tmp = new File(tester.nbestrealfile);
    		File tmpParent = tmp.getParentFile(); 
    		if (tmpParent != null) tmpParent.mkdirs(); 
            System.out.println("Writing N-best realizations to: " + tester.nbestrealfile);
            System.out.println();
        }
        
        // ensure realserdir exists
        if (tester.realserdir != null) {
            File realserDir = new File(tester.realserdir);
            if (!realserDir.exists()) { realserDir.mkdirs(); }
            System.out.println("Writing best realization serialization files to dir: " + tester.realserdir);
            System.out.println();
        }

        // ensure dir for rescorefile exists
        if (tester.rescorefile != null) {
    		File tmp = new File(tester.rescorefile);
    		File tmpParent = tmp.getParentFile(); 
    		if (tmpParent != null) tmpParent.mkdirs(); 
            System.out.println("Writing rescored sign scores to: " + tester.rescorefile);
            System.out.println();
        }
                
        // run test
        tester.runTest(new File(regressionfile));
    }
}
