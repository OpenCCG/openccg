///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2011-3 Michael White
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

package opennlp.ccg.hylo;

import java.util.*;

import opennlp.ccg.lexicon.Word;
import opennlp.ccg.perceptron.*;
import opennlp.ccg.synsem.*;
import opennlp.ccg.util.TrieMap;

/** 
 * A class for extracting lexical dependency features.  This class
 * implements the features in White and Rajkumar's EMNLP-12 paper 
 * <a href="http://aclweb.org/anthology-new/D/D12/D12-1023.pdf">
 * Minimal Dependency Length in Realization Ranking</a>.
 * 
 * Features are extracted lazily for efficiency, using a prefix of "ld" 
 * for "lexical dependency".  
 * 
 * Features potentially involve the word, POS tag, word class, definiteness, syntactic 
 * complexity, short-long order, and total dependency length, where the latter 
 * three can be controlled by the appropriate include* flags; of
 * these, only dependency length is included by default.
 *
 * Word class is based on the semantic class, a check for color terms, common suffixes, 
 * and the presence of a hyphen or capitalization.
 * 
 * The checks for definite NPs, color terms and common suffixes are done by methods with 
 * defaults for the English CCGbank, which can be overridden in subclasses or 
 * reconfigured in the case of color terms and suffixes.
 * 
 * @author Michael White
 * @version     $Revision: 1.4 $, $Date: 2011/11/10 15:12:53 $
 */ 
public class LexDepFeatureExtractor implements FeatureExtractor {

	/** Feature prefix constant: "ld". */
	public static final String PREFIX = "ld";
	
	/** Head precedes dep constant: "hpd". */
	public static final String HEAD_PRECEDES_DEP = "hpd";
	
	/** Dep precedes head constant: "dph". */
	public static final String DEP_PRECEDES_HEAD = "dph";
	
	/** Left of head sibs precedence constant: "lsp". */
	public static final String LEFT_SIBS_PRECEDENCE = "lsp";
	
	/** Right of head sibs precedence constant: "rsp". */
	public static final String RIGHT_SIBS_PRECEDENCE = "rsp";
	
	/** Returns the appropriate siblings precedence constant for the head-dep order constant. */
	public static String sibPrecedenceForDep(String depConst) {
		return (depConst == DEP_PRECEDES_HEAD) ? LEFT_SIBS_PRECEDENCE : RIGHT_SIBS_PRECEDENCE;
	}
	
	/** Definiteness constant: "def1". */
	public static final String DEF = "def1";
	
	/** Indefiniteness constant: "def0". */
	public static final String INDEF = "def0";
	
	/** Class constant PRO. */
	public static final String CLASS_PRO = "PRO";
	
	/** Class constant COLOR. */
	public static final String CLASS_COLOR = "COLOR";
	
	/** Class constant HYPH. */
	public static final String CLASS_HYPH = "HYPH";
	
	/** Class constant CAP. */
	public static final String CLASS_CAP = "CAP";
	
	/** Class constant NIL. */
	public static final String CLASS_NIL = "NIL";
	
	/** Flag for whether to include syntactic complexity ordering features (defaults to false). */
	public boolean includeComplexityFeats = false;
	
	/** Complexity ordering constant for verb presence: "1v". */
	public static final String HAS_V = "1v";
	
	/** Complexity ordering constant for verb presence: "0v". */
	public static final String NO_V = "0v";
	
	/** Complexity ordering constant for punct presence: "1p". */
	public static final String HAS_P = "1p";
	
	/** Complexity ordering constant for punct absence: "0p". */
	public static final String NO_P = "0p";
	
	/** Flag for whether to include short-long features (defaults to false). */
	public boolean includeShortLong = false;

	/** Short-long order constant: "sl". */
	public static final String SHORT_LONG_ORDER = "sl";
	
	/** Long-short order constant: "ls". */
	public static final String LONG_SHORT_ORDER = "ls";
	
	/** Flag for whether to include global dependency length features (defaults to true). */
	public boolean includeDepLen = true;

	/** Global dependency length feature constant: "$deplen". */
	public static final String DEPLEN = "$deplen";
	
	/** Conditional lazy extractor, for lazily extracting a feature subject to a test. */
	public static abstract class ConditionalLazyExtractor {
		abstract boolean test();
		List<TrieMap.KeyExtractor<String>> lazyExtractor = new ArrayList<TrieMap.KeyExtractor<String>>(5);
	}
	
	/** Conditional lazy evaluator, for lazily extracting a feature and its value, subject to a test. */
	public static abstract class ConditionalLazyEvaluator extends ConditionalLazyExtractor {
		abstract float eval();
	}
	
	/** Feature map wrapper, for unique retrieval from a sign's data objects. */
	public static class FeatureMapWrapper {
		public FeatureMap featureMap;
		public FeatureMapWrapper(FeatureMap featureMap) { this.featureMap = featureMap; }
	}
	
	/** The alphabet. */
	protected Alphabet alphabet = null;

	/** Current feature map. */
	protected FeatureMap currentMap = null;
	
	/** Current sign (for extracting features). */
	protected Sign currentSign = null;
	
	/** Current input signs (for extracting features). */
	protected Sign[] currentInputs = null;
	
	/** Current dependency (for extracting features). */
	protected LexDependency currentDep = null;

	/** Current head index. */
	protected int currentHeadIndex = -1;
	
	/** Current dependent index. */
	protected int currentDepIndex = -1;
	
	/** Current sibling dependency (for extracting features). */
	protected LexDependency currentSib = null;
	
	/** Current sibling dependent index. */
	protected int currentSibIndex = -1;
	
	/** Current head broad POS (for extracting features). */
	protected String currentHeadBroadPOS = null;

	/** Current head-dependent order (for extracting features). */
	protected String currentHeadDepOrder = null;

	/** Current siblings precedence relation (for extracting features). */
	protected String currentSibsPrecedence = null;

	/** Current dep prececes sib flag (for extracting features). */
	protected boolean currentDepPrecedesSib = false;

	/** Current dep sign (for extracting features). */
	protected Sign currentDepSign = null;
	
	/** Current sib sign (for extracting features). */
	protected Sign currentSibSign = null;
	
	/** Current dep phrase lengths (for extracting features). */
	protected PhraseLengths currentDepLengths = null;
	
	/** Current sib phrase lengths (for extracting features). */
	protected PhraseLengths currentSibLengths = null;
	
	/** Current difference in lengths between second and first siblings (for extracting features). */
	protected PhraseLengths currentLengthsDiff = null;
	
	///** Lexical feature extractors. */
	//protected List<List<TrieMap.KeyExtractor<String>>> lexExtractors = new ArrayList<List<TrieMap.KeyExtractor<String>>>();
	
	/** Dependency order feature extractors. */
	protected List<List<TrieMap.KeyExtractor<String>>> depOrderExtractors = new ArrayList<List<TrieMap.KeyExtractor<String>>>();
	
	/** Siblings order feature extractors. */
	protected List<List<TrieMap.KeyExtractor<String>>> sibsOrderExtractors = new ArrayList<List<TrieMap.KeyExtractor<String>>>();
	
	/** Siblings order conditional feature extractors. */
	protected List<ConditionalLazyExtractor> sibsOrderCondExtractors = new ArrayList<ConditionalLazyExtractor>();
	
	/** Siblings complexity order conditional feature extractors. */
	protected List<ConditionalLazyExtractor> sibsComplexityOrderCondExtractors = new ArrayList<ConditionalLazyExtractor>();
	
	/** Short-long conditional feature evaluators. */
	protected List<ConditionalLazyEvaluator> shortLongCondEvaluators = new ArrayList<ConditionalLazyEvaluator>();
	
	/** Global dependency length keys. */
	protected List<String> depLenKeys = new ArrayList<String>(1);
	
	/** Global dependency length feature. */
	protected Alphabet.Feature depLenFeat = null;
	
	/** Constructor. */
	public LexDepFeatureExtractor() {
		// init lazy feature extractors
		depOrderExtractors.add(head_dep_order_words());
		depOrderExtractors.add(head_dep_order_pos());
		depOrderExtractors.add(head_dep_order_word_pos());
		depOrderExtractors.add(head_dep_order_pos_word());
		sibsOrderExtractors.add(sibs_precedence_words());
		sibsOrderExtractors.add(sibs_precedence_word_pos());
		sibsOrderExtractors.add(sibs_precedence_pos_word());
		sibsOrderCondExtractors.add(sibs_precedence_pos());
		sibsOrderExtractors.add(sibs_precedence_word_class());
		sibsOrderExtractors.add(sibs_precedence_class_word());
		sibsOrderCondExtractors.add(sibs_precedence_class());
		sibsOrderCondExtractors.add(sibs_precedence_rels());
		sibsOrderCondExtractors.add(sibs_precedence_defs());
		// init complexity feature extractors
		sibsComplexityOrderCondExtractors.add(sibs_precedence_verbs());
		sibsComplexityOrderCondExtractors.add(sibs_precedence_puncts());
		// init short-long feature evaluators
		shortLongCondEvaluators.add(short_long_words());
		// init dep len keys
		depLenKeys.add(DEPLEN);
	}
	
	/** Sets the alphabet. */
	public void setAlphabet(Alphabet alphabet) {
		this.alphabet = alphabet;
	}
	
	
	/** Returns the features for the given sign and completeness flag. */
	public FeatureVector extractFeatures(Sign sign, boolean complete) {
		addFeatures(sign, complete);
		return getFeatureMap(sign);
	}
	
	/** Recursively adds features to the feature map for the given sign, if not already present. */
	protected void addFeatures(Sign sign, boolean complete) {
		// check for existing map, otherwise make one
		if (getFeatureMap(sign) != null) return;
		// lex case
		if (sign.isLexical()) {
			currentSign = sign;
			currentMap = new FeatureMap(0);
			//inc(lexExtractors);
		}
		// non-terminal
		else {
			Sign[] inputs = sign.getDerivationHistory().getInputs();
			// first recurse
			for (Sign child : inputs) addFeatures(child, false);
			// use input maps in making current map
			currentSign = sign;
			currentInputs = inputs;
			if (inputs.length == 1) {
				currentMap = new FeatureMap(getFeatureMap(inputs[0]));
			}
			else if (inputs.length == 2) {
				currentMap = new FeatureMap(getFeatureMap(inputs[0]), getFeatureMap(inputs[1]));
			}
			// do each newly filled dep
			for (LexDependency dep : sign.getFilledDeps()) {
				currentDep = dep;
				currentHeadBroadPOS = getHeadBroadPOS(dep);
				setDepIndexes(dep);
				currentHeadDepOrder = getHeadDepOrder();
				inc(depOrderExtractors);
				// do dep len
				if (includeDepLen) {
					Alphabet.Feature f = getDepLenFeat();
					if (f != null) currentMap.add(f, (float)depLen());					
				}
				// do order for each sib on the same side of the same head
				for (LexDependency sib : sign.getSiblingFilledDeps()) {
					currentSib = sib;
					if (dep.lexHead != sib.lexHead) continue;
					setSibIndex(sib);
					if (currentHeadDepOrder != getHeadSibOrder()) continue;
					currentSibsPrecedence = sibPrecedenceForDep(currentHeadDepOrder);
					currentDepPrecedesSib = depPrecedesSib();
					inc(sibsOrderExtractors);
					incCond(sibsOrderCondExtractors);
					// do complexity, length feats
					if (includeComplexityFeats || includeShortLong) {
						setLengthsDiff();
						if (currentLengthsDiff != null) {
							if (includeComplexityFeats) incCond(sibsComplexityOrderCondExtractors);
							if (includeShortLong) addCond(shortLongCondEvaluators);
						}
					}
				}
			}
		}
		// store it
		storeFeatureMap(sign);
	}
	
	/** Stores the current feature map as a data object in the given sign. */
	protected void storeFeatureMap(Sign sign) {
		sign.addData(new FeatureMapWrapper(currentMap));
	}
	
	/** Returns the feature map for this extractor from the given sign (null if none). */
	protected FeatureMap getFeatureMap(Sign sign) {
		FeatureMapWrapper fmw = (FeatureMapWrapper)sign.getData(FeatureMapWrapper.class);
		return (fmw != null) ? fmw.featureMap : null;
	}
	
	/**
	 * Increments the count of the given features, if relevant.
	 */
	protected void inc(List<List<TrieMap.KeyExtractor<String>>> extractors) {
		for (List<TrieMap.KeyExtractor<String>> lazyExtractor : extractors) {
			Alphabet.Feature f = alphabet.indexLazy(lazyExtractor);
			if (f != null) currentMap.inc(f);
		}
	}

	/**
	 * Increments the count of the given conditional features, if relevant.
	 */
	protected void incCond(List<ConditionalLazyExtractor> condExtractors) {
		for (ConditionalLazyExtractor condExtractor : condExtractors) {
			if (condExtractor.test()) {
				Alphabet.Feature f = alphabet.indexLazy(condExtractor.lazyExtractor);
				if (f != null) currentMap.inc(f);
			}
		}
	}

	/**
	 * Adds to the values of the given conditional features, if relevant.
	 */
	protected void addCond(List<ConditionalLazyEvaluator> condEvaluators) {
		for (ConditionalLazyEvaluator condEvaluator : condEvaluators) {
			if (condEvaluator.test()) {
				Alphabet.Feature f = alphabet.indexLazy(condEvaluator.lazyExtractor);
				if (f != null) currentMap.add(f, condEvaluator.eval());
			}
		}
	}

	
	//------------------------------------
	// utility functions
	
	// returns up to the first two chars of the head POS
	private String getHeadBroadPOS(LexDependency dep) {
		String pos = dep.lexHead.getPOS();
		String retval = pos;
		if (pos.length() > 2) retval = pos.substring(0, 2).intern();
		return retval;
		
	}
	
	// sets the current head and dep indexes, and the dep sign
	private void setDepIndexes(LexDependency dep) {
		currentHeadIndex = currentSign.wordIndex(dep.lexHead);
		currentDepIndex = currentSign.wordIndex(dep.lexDep);
		currentDepSign = currentSign.getSignHeadedByDep(dep);
	}

	// returns the head-dependent order
	private String getHeadDepOrder() {
		return (currentHeadIndex < currentDepIndex) ? HEAD_PRECEDES_DEP : DEP_PRECEDES_HEAD;
	}
	
	// sets the current sib index and the sib sign
	private void setSibIndex(LexDependency sib) {
		currentSibIndex = currentSign.wordIndex(sib.lexDep);	
		currentSibSign = currentSign.getSignHeadedByDep(sib);
	}

	// returns the head--sibling dependent order
	private String getHeadSibOrder() {
		return (currentHeadIndex < currentSibIndex) ? HEAD_PRECEDES_DEP : DEP_PRECEDES_HEAD;
	}
	
	// returns whether the dep precedes the sib
	private boolean depPrecedesSib() {
		return currentDepIndex < currentSibIndex;
	}

	
	//------------------------------------
	// definiteness functions
	
	/** Class for storing whether a sign is or immediately contains a definite NP. */
	public static class DefiniteNP {
		/** Definiteness value; null means not an NP (or NP parent). */
		public Boolean def;
		public DefiniteNP(Boolean def) { this.def = def; }
		public String toString() { return "defNP: " + def; }
	}
	
	/** Returns the definite NP status for a sign, caching it in the sign. */
	public DefiniteNP getDefiniteNP(Sign sign) {
		// check cached
		DefiniteNP defNP = (DefiniteNP)sign.getData(DefiniteNP.class);
		if (defNP != null) return defNP;
		// determine def NP status
		Boolean def = null;
		// check for NP
		Sign npSign = getSignOrChildSignAsNP(sign);
		if (npSign != null) {
			// set status to definite by default; check for indef
			def = Boolean.TRUE;
			// get sem feats
			Nominal npNom = npSign.getCategory().getIndexNominal();
			List<SatOp> semFeats = HyloHelper.getSemFeatsForHead(npNom, npSign.getCategory().getLF());
			// check for <det>nil
			for (SatOp feat : semFeats) {
				if (isIndefFeat(feat)) {
					def = Boolean.FALSE; break;
				}
			}
			// otherwise check deps
			if (def) {
				// get all deps
				List<LexDependency> allDeps = new ArrayList<LexDependency>(5);
				allDeps.addAll(npSign.getFilledDeps());
				allDeps.addAll(npSign.getSiblingFilledDeps());
				// check for <Det>a|an|some|any
				for (LexDependency dep : allDeps) {
					if (isIndefDep(dep)) {
						def = Boolean.FALSE; break;
					}
				}
			}
		} 
		// store result and return
		defNP = new DefiniteNP(def);
		sign.addData(defNP);
		return defNP;
	}
	
	/**
	 * Returns the given sign if it's an NP sign;
	 * otherwise returns the first child sign that's an NP sign;
	 * otherwise returns null.
	 */
	protected Sign getSignOrChildSignAsNP(Sign sign) {
		if (isNP(sign)) return sign;
		if (sign.isLexical()) return null;
		Sign[] inputs = sign.getDerivationHistory().getInputs();
        for (int i = 0; i < inputs.length; i++) {
        	if (isNP(inputs[i])) return inputs[i];
        }
		return null;
	}
	
	/**
	 * Returns whether the given sign is an NP.
	 * The default implementation tests for a category type of "np".
	 */
	protected boolean isNP(Sign sign) {
		Category cat = sign.getCategory();
		if (!(cat instanceof AtomCat)) return false;
		AtomCat ac = (AtomCat) cat;
		return (ac.getType().equals("np"));
	}
	
	/** 
	 * Returns whether the given semantic features signals indefiniteness.
	 * The default implementation tests for &lt:det&gt;nil.
	 */
	protected boolean isIndefFeat(SatOp feat) {
		return HyloHelper.getRel(feat).equals("det") && "nil".equals(HyloHelper.getVal(feat));
	}
	
	/**
	 * Returns whether the given lexical dependency signals indefiniteness.
	 * The default implementation checks for &lt;Det&gt;a|an|any|some.
	 */
	protected boolean isIndefDep(LexDependency dep) {
		if (dep.rel.equalsIgnoreCase("Det")) {
			String form = dep.lexDep.getWordForm();
			if (form=="a" || form=="an" || form=="any" || form=="some")
				return true;
		}
		return false;
	}
	
	/** Returns whether two signs differ in definiteness. */
	public boolean defDifference(Sign sign1, Sign sign2) {
		DefiniteNP defNP1 = getDefiniteNP(sign1);
		if (defNP1.def == null) return false;
		DefiniteNP defNP2 = getDefiniteNP(sign2);
		if (defNP2.def == null) return false;
		return defNP1.def != defNP2.def;
	}
	
	/** Returns the appropriate definiteness/indefiniteness constant. */
	public String defConstant(DefiniteNP defNP) {
		return (defNP.def) ? DEF : INDEF;
	}
	
	
	//------------------------------------
	// phrase length functions

	/** 
	 * Class for storing length of phrase in words, puncts and (finite) verbs, for unique retrieval from a sign's data objects. 
	 */
	public static class PhraseLengths {
		public int wordlen, punctlen, verblen;
		public PhraseLengths(int wordlen, int punctlen, int verblen) {
			this.wordlen = wordlen; this.punctlen = punctlen; this.verblen = verblen;
		}
		public String toString() { return " wordlen: " + wordlen + " punctlen: " + punctlen + " verblen: " + verblen; }
	}
	
	/** Returns the phrase lengths for a sign, caching them in the sign. */
	public PhraseLengths getPhraseLengths(Sign sign) {
		// check cached
		PhraseLengths lengths = (PhraseLengths)sign.getData(PhraseLengths.class);
		if (lengths != null) return lengths;
		int wordlen = 0, punctlen = 0, verblen = 0;
		// lex case
		if (sign.isLexical()) {
			for (Word w: sign.getWords()) {
				wordlen++;
				if (isPunct(w)) punctlen++;
				if (isVerb(w)) verblen++;
			}
		}
		// non-lex: add child lengths
		else {
			Sign[] inputs = sign.getDerivationHistory().getInputs();
	        for (int i = 0; i < inputs.length; i++) {
	        	PhraseLengths lengthsI = getPhraseLengths(inputs[i]);
	        	wordlen += lengthsI.wordlen; punctlen += lengthsI.punctlen; verblen += lengthsI.punctlen;
	        }
		}
		// store result and return
		lengths = new PhraseLengths(wordlen, punctlen, verblen);
		sign.addData(lengths);
		return lengths;
	}
	
	/**
	 * Returns whether a word is a punctuation mark that typically signals sentence-internal complexity.
	 * The default implementation tests for commas, dashes (--), semi-colons and colons.
	 */
	protected boolean isPunct(Word word) {
		// NB: in principle could use POS, but sometimes punctuation marks seem to end up with IN as the POS tag
		String form = word.getForm();
		return (form == "," || form == "--" || form == ";" || form == ":");
	}
	
	/**
	 * Returns whether a word is a verb that indicates a substantial clause.
	 * The default implementation tests for the finite verb POS tags VBD, VBP and VBZ.
	 */
	protected boolean isVerb(Word word) {
		String pos = word.getPOS();
		return (pos == "VBD" || pos == "VBP" || pos == "VBZ");
	}
	
	/** 
	 * Sets the differences in length between the signs headed by the current dep and sib, or null if none; 
	 * also sets the current dep and sib lengths.  The lengths are set to the lengths of the second sign 
	 * minus those of the first sign.
	 */
	protected void setLengthsDiff() {
		// reset
		currentLengthsDiff = null; currentDepLengths = null; currentSibLengths = null;
		// ensure both there
		if (currentDepSign == null || currentSibSign == null) return;
		// get phrase lengths
		currentDepLengths = getPhraseLengths(currentDepSign);
		currentSibLengths = getPhraseLengths(currentSibSign);
		// get 1st and 2nd phrase lengths
		PhraseLengths pl1 = (currentDepPrecedesSib) ? currentDepLengths : currentSibLengths;
		PhraseLengths pl2 = (currentDepPrecedesSib) ? currentSibLengths : currentDepLengths;
		// set diff to 2nd - 1st
		currentLengthsDiff = new PhraseLengths(pl2.wordlen-pl1.wordlen, pl2.punctlen-pl1.punctlen, pl2.verblen-pl1.verblen);
	}
	

	//------------------------------------
	// dep len functions
	
	/** Returns the dep len feature if not already set. */
	protected Alphabet.Feature getDepLenFeat() {
		if (depLenFeat == null) 
			depLenFeat = alphabet.index(depLenKeys);
		return depLenFeat;
	}
	
	/**
	 * Returns the dependency length between the current head and the current dependent.
	 * The default implementation returns the number of intervening words excluding 
	 * punctuation (as determined by isPunct), and doesn't count each word in a collapsed NE separately.  
	 */ 
	protected int depLen() {
		List<Word> words = currentSign.getWords();
		int min = Math.min(currentHeadIndex, currentDepIndex);
		int max = Math.max(currentHeadIndex, currentDepIndex);
		int count = 0;
		for (int i=min+1; i < max; i++) {
			Word w = words.get(i); 
			if (!isPunct(w)) count++;
		}
		return count;
	}
	
	
	//------------------------------------
	// word class functions
	
	/**
	 * Returns a class for the word, or CLASS_NIL if none.
	 * The default implementation returns one of the following, in this order:
	 * the semantic class of the word; 
	 * CLASS_PRO, if a pronoun; 
	 * CLASS_COLOR, if a color word; 
	 * the suffix, if getSuffix returns a value; 
	 * CLASS_HYPH, if the word is hyphenated; 
	 * CLASS_CAP, if capitalized; 
	 * or CLASS_NIL, otherwise. 
	 * The word class is cached using cachedWordClasses. 
	 */
	protected String getWordClass(Word word) {
		String retval = cachedWordClasses.get(word);
		if (retval != null) return retval;
		String wClass = word.getSemClass();
		if (wClass != null) return updateCachedWordClasses(word, wClass);
		if (isPro(word)) return updateCachedWordClasses(word, CLASS_PRO);
		String form = word.getForm();
		if (colors.contains(form)) return updateCachedWordClasses(word, CLASS_COLOR);
		String suffix = getSuffix(form);
		if (suffix != null) return updateCachedWordClasses(word, suffix);
		if (form.indexOf('-') >= 0) return updateCachedWordClasses(word, CLASS_HYPH); 
		if (Character.isUpperCase(form.charAt(0))) return updateCachedWordClasses(word, CLASS_CAP);
		return updateCachedWordClasses(word, CLASS_NIL);
	}
	
	/**
	 * Returns whether a word is a pronoun.
	 * The default implementation returns whether the POS tag starts with "PR".
	 */
	protected boolean isPro(Word word) { return word.getPOS().startsWith("PR"); }
	
	/**
	 * The set of color words to check for in determining the word class.
	 */
	protected Set<String> colors = defaultColors();
	
	/**
	 * Sets the set of color words.
	 */
	public void setColorWords(Set<String> colorWords) { colors = colorWords; }
	
	/**
	 * Returns the default set of color words: 11 common English colors, with two spellings of gray/grey.
	 */
	protected Set<String> defaultColors() {
		String[] colors = { 
				"black", "blue", "brown", "gray", "grey", "green", 
				"orange", "pink", "purple", "red", "white", "yellow"
			};
		return new HashSet<String>(Arrays.asList(colors));
	}

	/**
	 * A sequence of suffixes to check for in determining the word class, ordered by specificity.
	 */
	protected String[] suffixClasses = defaultSuffixClasses();
	
	/**
	 * Returns the default suffix classes: 61 common English suffixes from various lists on the web.
	 */
	protected String[] defaultSuffixClasses() {
		return new String[] { 
				"ancy", "aphy", "arch", "crat", "gram", "less", "logy", "ness", "nomy", "ship", "some", "sque", "tude",
				"ade", "age", "ant", "aph", "ary", "ast", "ate", "ble", "dom", "ent", "est", "ful", 
				"ian", "ile", "ion", "ing", "ish", "ism", "ist", "ise", "ite", "ium", "ive", "ize", 
				"nce", "oid", "ory", "ose", "ote", "ous", "sig", "ure",
				"ac", "al", "an", "cy", "ed", "en", "er", "fy", "ic", "le", "ly", "or", "se", "sy", "ty", 
				"y"
			};
	}
	
	/**
	 * Sets the suffix classes, which are assumed to be interned.
	 */
	public void setSuffixClasses(String[] suffixes) { suffixClasses = suffixes; }
	
	/**
	 * Returns a matching suffix class, or null if none.
	 */
	protected String getSuffix(String form) {
		for (int i=0; i < suffixClasses.length; i++) {
			String suff = suffixClasses[i];
			if (form.length() > suff.length() && form.endsWith(suff)) return suff;
		}
		return null;
	}
	
	/**
	 * Cache of word classes, using a weak hash map.
	 */
	protected WeakHashMap<Word,String> cachedWordClasses = new WeakHashMap<Word,String>();
	
	/**
	 * Updates the cached word classes with the given word and word class, and returns the word class.
	 */
	protected String updateCachedWordClasses(Word word, String wordClass) {
		cachedWordClasses.put(word, wordClass); return wordClass;
	}
	
	
	//------------------------------------
	// shared feature extractor elements

	// prefix: "ld" + head broad POS
	private void add_prefix(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return PREFIX; }});
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentHeadBroadPOS; }});
	}
	
	// head-dep order
	private void add_head_dep_order(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentHeadDepOrder; }});
	}
	
	// rel
	private void add_rel(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentDep.rel.intern(); }});
	}
	
	// common head-dep order elements
	private void add_head_dep_order_common(List<TrieMap.KeyExtractor<String>> retval) {
		add_prefix(retval);
		add_head_dep_order(retval);
		add_rel(retval);
	}
	
	// head word
	private void add_head_word(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentDep.lexHead.getWordForm(); }});
	}
	
	// head pos
	private void add_head_pos(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentDep.lexHead.getPOS(); }});
	}
	
	// dep word
	private void add_dep_word(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentDep.lexDep.getWordForm(); }});
	}
	
	// dep pos
	private void add_dep_pos(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentDep.lexDep.getPOS(); }});
	}
	
	// sibs precedence
	private void add_sibs_precedence(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentSibsPrecedence; }});
	}
	
	// common sibs precedence elements
	private void add_sibs_precedence_common(List<TrieMap.KeyExtractor<String>> retval) {
		add_prefix(retval);
		add_sibs_precedence(retval);
	}
	
	// sibs word1
	private void add_sibs_word1(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ 
			return (currentDepPrecedesSib) ? currentDep.lexDep.getWordForm() : currentSib.lexDep.getWordForm(); 
		}});
	}
	
	// sibs word2
	private void add_sibs_word2(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ 
			return (currentDepPrecedesSib) ? currentSib.lexDep.getWordForm() : currentDep.lexDep.getWordForm(); 
		}});
	}
	
	// sibs pos1
	private void add_sibs_pos1(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ 
			return (currentDepPrecedesSib) ? currentDep.lexDep.getPOS() : currentSib.lexDep.getPOS(); 
		}});
	}
	
	// sibs pos2
	private void add_sibs_pos2(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ 
			return (currentDepPrecedesSib) ? currentSib.lexDep.getPOS() : currentDep.lexDep.getPOS(); 
		}});
	}
	
	// sibs class1
	private void add_sibs_class1(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){
			Sign first = (currentDepPrecedesSib) ? currentDep.lexDep : currentSib.lexDep;
			return getWordClass(first.getWords().get(0));
		}});
	}
	
	// sibs class2
	private void add_sibs_class2(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){
			Sign second = (currentDepPrecedesSib) ? currentSib.lexDep : currentDep.lexDep;
			return getWordClass(second.getWords().get(0));
		}});
	}
	
	// sibs rel1
	private void add_sibs_rel1(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ 
			return (currentDepPrecedesSib) ? currentDep.rel.intern() : currentSib.rel.intern(); 
		}});
	}
	
	// sibs rel2
	private void add_sibs_rel2(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ 
			return (currentDepPrecedesSib) ? currentSib.rel.intern() : currentDep.rel.intern(); 
		}});
	}
	

	//-------------------------------
	// feature extractors
	
	// head-dep order words
	private List<TrieMap.KeyExtractor<String>> head_dep_order_words() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_head_dep_order_common(retval);
		add_head_word(retval);
		add_dep_word(retval);
		return retval;
	}
	
	// head-dep order pos
	private List<TrieMap.KeyExtractor<String>> head_dep_order_pos() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_head_dep_order_common(retval);
		add_head_pos(retval);
		add_dep_pos(retval);
		return retval;
	}

	// head-dep order word/pos
	private List<TrieMap.KeyExtractor<String>> head_dep_order_word_pos() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_head_dep_order_common(retval);
		add_head_word(retval);
		add_dep_pos(retval);
		return retval;
	}
	
	// head-dep order pos/word
	private List<TrieMap.KeyExtractor<String>> head_dep_order_pos_word() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_head_dep_order_common(retval);
		add_head_pos(retval);
		add_dep_word(retval);
		return retval;
	}
	
	// sibs precedence words
	private List<TrieMap.KeyExtractor<String>> sibs_precedence_words() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_sibs_precedence_common(retval);
		add_sibs_word1(retval);
		add_sibs_word2(retval);
		return retval;
	}
	
	// sibs precedence word pos
	private List<TrieMap.KeyExtractor<String>> sibs_precedence_word_pos() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_sibs_precedence_common(retval);
		add_sibs_word1(retval);
		add_sibs_pos2(retval);
		return retval;
	}
	
	// sibs precedence pos word
	private List<TrieMap.KeyExtractor<String>> sibs_precedence_pos_word() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_sibs_precedence_common(retval);
		add_sibs_pos1(retval);
		add_sibs_word2(retval);
		return retval;
	}
	
	// sibs precedence pos
	private ConditionalLazyExtractor sibs_precedence_pos() {
		ConditionalLazyExtractor retval = new ConditionalLazyExtractor() {
			boolean test() { return currentDep.lexDep.getPOS() != currentSib.lexDep.getPOS(); }
		};
		add_sibs_precedence_common(retval.lazyExtractor);
		add_sibs_pos1(retval.lazyExtractor);
		add_sibs_pos2(retval.lazyExtractor);
		return retval;
	}
	
	// sibs precedence word / class
	private List<TrieMap.KeyExtractor<String>> sibs_precedence_word_class() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_sibs_precedence_common(retval);
		add_sibs_word1(retval);
		add_sibs_class2(retval);
		return retval;
	}
	
	// sibs precedence class / word
	private List<TrieMap.KeyExtractor<String>> sibs_precedence_class_word() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_sibs_precedence_common(retval);
		add_sibs_class1(retval);
		add_sibs_word2(retval);
		return retval;
	}
	
	// sibs precedence class
	private ConditionalLazyExtractor sibs_precedence_class() {
		ConditionalLazyExtractor retval = new ConditionalLazyExtractor() {
			boolean test() { 
				return getWordClass(currentDep.lexDep.getWords().get(0)) != getWordClass(currentSib.lexDep.getWords().get(0)); 
			}
		};
		add_sibs_precedence_common(retval.lazyExtractor);
		add_sibs_class1(retval.lazyExtractor);
		add_sibs_class2(retval.lazyExtractor);
		return retval;
	}
	
	// sibs precedence rels
	private ConditionalLazyExtractor sibs_precedence_rels() {
		ConditionalLazyExtractor retval = new ConditionalLazyExtractor() {
			boolean test() { return !currentDep.rel.equals(currentSib.rel); }
		};
		add_sibs_precedence_common(retval.lazyExtractor);
		add_sibs_rel1(retval.lazyExtractor);
		add_sibs_rel2(retval.lazyExtractor);
		return retval;
	}
	
	// sibs precedence defs
	private ConditionalLazyExtractor sibs_precedence_defs() {
		ConditionalLazyExtractor retval = new ConditionalLazyExtractor() {
			boolean test() {
				if (currentDepSign == null || currentSibSign == null) return false;
				return defDifference(currentDepSign, currentSibSign); 
			}
		};
		add_sibs_precedence_common(retval.lazyExtractor);
		retval.lazyExtractor.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ 
			return (currentDepPrecedesSib) ? defConstant(getDefiniteNP(currentDepSign)) : defConstant(getDefiniteNP(currentSibSign));
		}});
		retval.lazyExtractor.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ 
			return (currentDepPrecedesSib) ? defConstant(getDefiniteNP(currentSibSign)) : defConstant(getDefiniteNP(currentDepSign));
		}});
		return retval;
	}
	
	// sibs precedence verbs
	private ConditionalLazyExtractor sibs_precedence_verbs() {
		ConditionalLazyExtractor retval = new ConditionalLazyExtractor() {
			boolean test() { 
				return currentLengthsDiff.verblen != 0 && 
						(currentDepLengths.verblen == 0 || currentSibLengths.verblen == 0); 
			}
		};
		add_sibs_precedence_common(retval.lazyExtractor);
		retval.lazyExtractor.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ 
			return (currentLengthsDiff.verblen > 0) ? NO_V : HAS_V;
		}});
		retval.lazyExtractor.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ 
			return (currentLengthsDiff.verblen > 0) ? HAS_V : NO_V;
		}});
		return retval;
	}
	
	// sibs precedence puncts
	private ConditionalLazyExtractor sibs_precedence_puncts() {
		ConditionalLazyExtractor retval = new ConditionalLazyExtractor() {
			boolean test() { 
				return currentLengthsDiff.punctlen != 0 && 
						(currentDepLengths.punctlen == 0 || currentSibLengths.punctlen == 0); 
			}
		};
		add_sibs_precedence_common(retval.lazyExtractor);
		retval.lazyExtractor.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ 
			return (currentLengthsDiff.punctlen > 0) ? NO_P : HAS_P;
		}});
		retval.lazyExtractor.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ 
			return (currentLengthsDiff.punctlen > 0) ? HAS_P : NO_P;
		}});
		return retval;
	}

	// short-long words
	private ConditionalLazyEvaluator short_long_words() {
		ConditionalLazyEvaluator retval = new ConditionalLazyEvaluator() {
			boolean test() { return currentLengthsDiff.wordlen != 0; }
			float eval() { return (float) Math.abs(currentLengthsDiff.wordlen); }
		};
		add_sibs_precedence_common(retval.lazyExtractor);
		retval.lazyExtractor.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ 
			return (currentLengthsDiff.wordlen > 0) ? SHORT_LONG_ORDER : LONG_SHORT_ORDER;
		}});
		return retval;
	}
}
