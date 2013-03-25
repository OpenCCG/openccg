///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2010 Michael White
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

package opennlp.ccg.synsem;

import java.io.*;
import java.net.URL;
import java.util.*;

import opennlp.ccg.grammar.Grammar;
import opennlp.ccg.lexicon.*;
import opennlp.ccg.ngrams.ConditionalProbabilityTable;
import opennlp.ccg.ngrams.NgramScorer;
import opennlp.ccg.perceptron.*;
import opennlp.ccg.test.Regression;
import opennlp.ccg.test.RegressionInfo;
import opennlp.ccg.test.RegressionInfo.TestItem;
import opennlp.ccg.util.Pair;

/** 
 * A class implementing a variant of Hockenmaier's HWDep generative syntactic model, 
 * with additional postag variables.
 * Note that the top step is only used with complete derivations.
 * Also note that for simplicity there is no special treatment of rare words, and thus 
 * a single unknown word is used in the model, rather than one for each POS tag.
 * 
 * @author 	Michael White
 * @version	$Revision: 1.12 $, $Date: 2010/03/07 03:23:01 $
 */ 
public class GenerativeSyntacticModel implements FeatureExtractor, SignScorer {

	/** Feature key. */
	public static String genlogprobkey = "genlogprob";
	
	/** Expansion string constant. */
	public static final String EXPANSION = "E";
	
	/** Left expansion string constant. */
	public static final String LEFT = "left";
	
	/** Right expansion string constant. */
	public static final String RIGHT = "right";
	
	/** Unary expansion string constant. */
	public static final String UNARY = "unary";
	
	/** Leaf expansion string constant. */
	public static final String LEAF = "leaf";

	/** Category of parent string constant. */
	public static final String PARENT = "P";

	/** Category of head string constant. */
	public static final String HEAD = "H";

	/** Category of sibling string constant. */
	public static final String SIBLING = "S";

	/** Lexical head category of parent string constant. */
	public static final String LEXCAT_PARENT = "CP";

	/** Head postag of parent string constant. */
	public static final String POS_PARENT = "T";

	/** Head word of parent string constant. */
	public static final String WORD_PARENT = "W";

	/** Lexical head category of sibling string constant. */
	public static final String LEXCAT_SIBLING = "CS";

	/** Head postag of sibling string constant. */
	public static final String POS_SIBLING = "TS";

	/** Head word of sibling string constant. */
	public static final String WORD_SIBLING = "WS";

	/** Lexical head category of top string constant. */
	public static final String LEXCAT_TOP = "CT";

	/** Head postag top string constant. */
	public static final String POS_TOP = "TT";

	/** Head word of top string constant. */
	public static final String WORD_TOP = "WT";

	/** Derivation top string constant. */
	public static final String TOP = "<top>";
	
	/**
	 * Class for caching the model's log prob in a sign.
	 */
	public static class GenLogProb {
		/** The log prob. */
		public final double logprob;
		/** Constructor. */
		public GenLogProb(double logprob) { this.logprob = logprob; }
	}
	
	/** The top step model. */
	protected ConditionalProbabilityTable topModel;
	/** The lexical step model. */
	protected ConditionalProbabilityTable leafModel;
	/** The unary step model. */
	protected ConditionalProbabilityTable unaryModel;
	/** The binary step model. */
	protected ConditionalProbabilityTable binaryModel;
	
	/** Constructor with file names. */
	public GenerativeSyntacticModel(String topModelFN, String leafModelFN, String unaryModelFN, String binaryModelFN) throws IOException {
		topModel = new ConditionalProbabilityTable(topModelFN);
		leafModel = new ConditionalProbabilityTable(leafModelFN);
		unaryModel = new ConditionalProbabilityTable(unaryModelFN);
		binaryModel = new ConditionalProbabilityTable(binaryModelFN);
	}

    /** Flag for whether to show scoring breakdown. */
    protected boolean debugScore = false;
    
    /** Sets the debug score flag, and propagates to component models. */
    public void setDebug(boolean debugScore) { 
    	this.debugScore = debugScore; 
    	topModel.setDebug(debugScore);
    	leafModel.setDebug(debugScore);
    	unaryModel.setDebug(debugScore);
    	binaryModel.setDebug(debugScore);
    } 
    
	/** The alphabet. */
	protected Alphabet alphabet = null;

	/** Generative logprob feature. */
	protected Alphabet.Feature genlogprobFeature = null;
	
	/** Sets the alphabet. */
	public void setAlphabet(Alphabet alphabet) {
		this.alphabet = alphabet;
		List<String> keys = new ArrayList<String>(1);
		keys.add(genlogprobkey);
		genlogprobFeature = alphabet.closed() ? alphabet.index(keys) : alphabet.add(keys);
	}
	
	/** Returns the features for the given sign and completeness flag. */
	public FeatureVector extractFeatures(Sign sign, boolean complete) {
		return genLogProbVector((float) logprob(sign, complete));
	}
	
	/** Returns a feature vector with the given gen log prob. */
	protected FeatureVector genLogProbVector(float logprob) {
		FeatureList retval = new FeatureList(1);
		if (genlogprobFeature != null) retval.add(genlogprobFeature, logprob);
		return retval;
	}
	
	/** Derivation handler for getting the log prob for each derivation step as a sum. */
	public class LogProbGetter extends DerivationHandler<Double> {
		// reusable list of pairs
		private List<Pair<String,String>> pairs = new ArrayList<Pair<String,String>>(); 
		private String listPairs() {
			StringBuffer sb = new StringBuffer();
			for (Pair<String,String> pair : pairs) sb.append(pair.a).append('-').append(pair.b).append(' ');
			return sb.toString();
		}
		/** Checks for cached value. */
		public Double checkCache(Sign sign) {
			GenLogProb glp = (GenLogProb) sign.getData(GenLogProb.class);
			return (glp == null) ? null : glp.logprob;
		}
		/** Caches the total. */
		public void cache(Sign sign, Double total) {
			sign.addData(new GenLogProb(total));
		}
		/** Top step. */
		public Double topStep(Sign sign) {
			pairs.clear(); addTopFactors(sign, pairs); 
			if (debugScore) System.out.println("[topStep] " + listPairs());
			return topModel.logprob(pairs) + handleDerivation(sign);
		}
		/** Lexical step. */
		public Double lexStep(Sign sign) {
			pairs.clear(); addLexFactors(sign, pairs); 
			if (debugScore) System.out.println("[lexStep] " + listPairs());
			return leafModel.logprob(pairs); 
		}
		/** Unary step. */
		public Double unaryStep(Sign sign, Sign headChild) {
			pairs.clear(); addUnaryFactors(sign, pairs, headChild);
			if (debugScore) System.out.println("[unaryStep] " + listPairs());
			return unaryModel.logprob(pairs) + handleDerivation(headChild);
		}
		/** Binary step. */
		public Double binaryStep(Sign sign, boolean left, Sign headChild, Sign siblingChild) {
			pairs.clear(); addBinaryFactors(sign, pairs, left, headChild, siblingChild); 
			if (debugScore) System.out.println("[binaryStep] " + listPairs());
			return binaryModel.logprob(pairs) + handleDerivation(headChild) + handleDerivation(siblingChild);
		}
	}
	
	/** Derivation handler for getting the factors for each derivation step as a list of words. */
	public static class FactorsGetter extends DerivationHandler<Void> {
		/** The factors. */
		public List<Word> factors = new ArrayList<Word>();
		// reusable list of pairs
		private List<Pair<String,String>> pairs = null;
		// new pairs
		private void newPairs() { pairs = new ArrayList<Pair<String,String>>(); }
		// adds new word for pairs to result
		private void addPairs() { factors.add(new ListPairWord(pairs)); }
		/** Top step. */
		public Void topStep(Sign sign) {
			newPairs(); addTopFactors(sign, pairs); addPairs(); 
			handleDerivation(sign); return null;
		}
		/** Lexical step. */
		public Void lexStep(Sign sign) {
			newPairs(); addLexFactors(sign, pairs); addPairs(); return null;
		}
		/** Unary step. */
		public Void unaryStep(Sign sign, Sign headChild) {
			newPairs(); addUnaryFactors(sign, pairs, headChild); addPairs(); 
			handleDerivation(headChild); return null;
		}
		/** Binary step. */
		public Void binaryStep(Sign sign, boolean left, Sign headChild, Sign siblingChild) {
			newPairs(); addBinaryFactors(sign, pairs, left, headChild, siblingChild); addPairs(); 
			handleDerivation(headChild); handleDerivation(siblingChild); return null;
		}
	}
	
	/** Returns the probability of the derivation according to the models. */
	public double score(Sign sign, boolean complete) {
		return NgramScorer.convertToProb(logprob(sign, complete));
	}
	
	/** Returns the log probability of the derivation according to the models. */
	public double logprob(Sign sign, boolean complete) {
		LogProbGetter lpgetter = new LogProbGetter();
		if (complete) return lpgetter.handleCompleteDerivation(sign);
		else return lpgetter.handleDerivation(sign);
	}
	
	/** Returns the factors from the derivation of the given sign (assumed to be complete). */
	public static List<Word> getFactors(Sign sign) {
		FactorsGetter fgetter = new FactorsGetter();
		fgetter.handleCompleteDerivation(sign);
		return fgetter.factors;
	}
	
	/** Adds the factors for the top step in the derivation of the given sign. */
	public static void addTopFactors(Sign sign, List<Pair<String,String>> pairs) {
		pairs.add(new Pair<String,String>(EXPANSION, TOP));
		pairs.add(new Pair<String,String>(PARENT, TOP));
		pairs.add(new Pair<String,String>(LEXCAT_PARENT, TOP));
		pairs.add(new Pair<String,String>(WORD_PARENT, TOP));
		pairs.add(new Pair<String,String>(HEAD, sign.getSupertag()));
		Sign lexHead = sign.getLexHead();
		pairs.add(new Pair<String,String>(LEXCAT_TOP, lexHead.getSupertag()));
		pairs.add(new Pair<String,String>(POS_TOP, lexHead.getPOS()));
		pairs.add(new Pair<String,String>(WORD_TOP, lexHead.getWordForm()));
	}
	
	/** Adds the factors for a lexical step in the derivation of the given sign. */
	public static void addLexFactors(Sign sign, List<Pair<String,String>> pairs) {
		pairs.add(new Pair<String,String>(EXPANSION, LEAF));
		addParentFactors(sign, pairs);
	}
	
	/** Adds the parent factors for a step in the derivation of the given sign. */
	public static void addParentFactors(Sign sign, List<Pair<String,String>> pairs) {
		pairs.add(new Pair<String,String>(PARENT, sign.getSupertag()));
		Sign lexHead = sign.getLexHead();
		pairs.add(new Pair<String,String>(LEXCAT_PARENT, lexHead.getSupertag()));
		pairs.add(new Pair<String,String>(POS_PARENT, lexHead.getPOS()));
		pairs.add(new Pair<String,String>(WORD_PARENT, lexHead.getWordForm()));
	}
	
	/** Returns the factors for a unary step in the derivation of the given sign. */
	public static void addUnaryFactors(Sign sign, List<Pair<String,String>> pairs, Sign headChild) {
		pairs.add(new Pair<String,String>(EXPANSION, UNARY));
		addParentFactors(sign, pairs);
		pairs.add(new Pair<String,String>(HEAD, headChild.getSupertag()));
	}
	
	/** Returns the factors for a binary step in the derivation of the given sign. */
	public static void addBinaryFactors(Sign sign, List<Pair<String,String>> pairs, boolean left, Sign headChild, Sign siblingChild) {
		pairs.add(new Pair<String,String>(EXPANSION, (left) ? LEFT : RIGHT));
		addParentFactors(sign, pairs);
		pairs.add(new Pair<String,String>(HEAD, headChild.getSupertag()));
		pairs.add(new Pair<String,String>(SIBLING, siblingChild.getSupertag()));
		Sign siblingLexHead = siblingChild.getLexHead();
		pairs.add(new Pair<String,String>(LEXCAT_SIBLING, siblingLexHead.getSupertag()));
		pairs.add(new Pair<String,String>(POS_SIBLING, siblingLexHead.getPOS()));
		pairs.add(new Pair<String,String>(WORD_SIBLING, siblingLexHead.getWordForm()));
	}
	
    /** Tests loading and scoring. */
    public static void main(String[] args) throws IOException {
        
    	String argstr = "(-dir <modeldir>) (-g <grammarfile>) (-t <testbedfile>) (-verbose)";
        String usage = "Usage: java opennlp.ccg.synsem.GenerativeSyntacticModel " + argstr;
        
        if (args.length > 0 && args[0].equals("-h")) {
            System.out.println(usage);
            System.exit(0);
        }
        
        String dir = ".", topfn = "top.flm", leaffn = "leaf.flm", unaryfn = "unary.flm", binaryfn = "binary.flm";
        String grammarfn = "grammar.xml", tbfn = "testbed.xml";
        boolean verbose = false;
        
        for (int i=0; i < args.length; i++) {
        	if (args[i].equals("-dir")) { dir = args[++i]; continue; }
        	if (args[i].equals("-g")) { grammarfn = args[++i]; continue; }
        	if (args[i].equals("-t")) { tbfn = args[++i]; continue; }
        	if (args[i].equals("-v") || args[i].equals("-verbose")) { verbose = true; continue; }
        	System.out.println("Unrecognized option: " + args[i]);
        }
        
        // load grammar
        URL grammarURL = new File(grammarfn).toURI().toURL();
        System.out.println("Loading grammar from URL: " + grammarURL);
        Grammar grammar = new Grammar(grammarURL);
        
        // load model
        System.out.println("Loading syntactic model from: " + dir);
        topfn = dir + "/" + topfn; leaffn = dir + "/" + leaffn; unaryfn = dir + "/" + unaryfn; binaryfn = dir + "/" + binaryfn;
        GenerativeSyntacticModel model = new GenerativeSyntacticModel(topfn, leaffn, unaryfn, binaryfn);
        if (verbose) model.setDebug(true);
        
    	// score saved signs
    	double logprobttotal = 0.0;
    	int numsents = 0;
    	for (File f : Regression.getXMLFiles(new File(tbfn))) {
            // load testfile
        	System.out.println("Loading: " + f.getName());
            RegressionInfo rinfo = new RegressionInfo(grammar, f);
            // do each item
	    	for (int i=0; i < rinfo.numberOfItems(); i++) {
	    		TestItem item = rinfo.getItem(i);
	    		if (item.numOfParses == 0) continue;
	    		numsents++;
	    		if (verbose) System.out.println("scoring: " + item.sentence);
	    		else System.out.print(".");
	    		Sign sign = item.sign;
	            double logprob = model.logprob(sign, true);
	            logprobttotal += logprob;
	            if (verbose) {
		    		System.out.println(sign.getDerivationHistory().toString());
		            System.out.println("logprob: " + logprob);
	            }
	    	}
	    	System.out.println();
    	}
    	
    	// totals
    	System.out.println("total logprob: " + logprobttotal);
    	System.out.println("logprob per sentence: " + (logprobttotal / numsents));
    }
}
