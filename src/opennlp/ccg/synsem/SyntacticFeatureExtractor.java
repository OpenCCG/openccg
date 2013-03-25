///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2009 Michael White
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

import java.util.*;

import opennlp.ccg.perceptron.*;
import opennlp.ccg.util.TrieMap;
import opennlp.ccg.lexicon.Word;

/** 
 * A class for extracting generic features from derivations,
 * inspired by those in the C&C-07 normal form model.
 * 
 * Features are extracted lazily for efficiency.
 * 
 * @author Michael White
 * @version     $Revision: 1.10 $, $Date: 2011/11/08 14:58:15 $
 */ 
public class SyntacticFeatureExtractor implements FeatureExtractor {
	
	/** Feature prefix constant: "syn". */
	public static final String PREFIX = "syn";
	
	/** Feature map wrapper, for unique retrieval from a sign's data objects. */
	public static class FeatureMapWrapper {
		public FeatureMap featureMap;
		public FeatureMapWrapper(FeatureMap featureMap) { this.featureMap = featureMap; }
	}
	
	/** Flag for whether to include distance features (defaults to true). */
	public boolean includeDistFeats = true;

	/** The alphabet. */
	protected Alphabet alphabet = null;

	/** Current feature map. */
	protected FeatureMap currentMap = null;
	
	/** Current sign (for extracting features). */
	protected Sign currentSign = null;
	
	/** Current input signs (for extracting features). */
	protected Sign[] currentInputs = null;
	
	/** Current sibling (for extracting features). */
	protected Sign currentSibling = null;
	
	/** Current words (for extracting features). */
	protected List<Word> currentWords = null;
	
	/** Current head index (for extracting features). */
	protected int currentHeadIndex = -1;

	/** Current sibling head index (for extracting features). */
	protected int currentSibHeadIndex = -1;
	
	/** Current distance in words (for extracting features). */
	protected String currentDistW = null;

	/** Current distance in puncts (for extracting features). */
	protected String currentDistP = null;

	/** Current distance in verbs (for extracting features). */
	protected String currentDistV = null;

	/** Lexical feature extractors. */
	protected List<List<TrieMap.KeyExtractor<String>>> lexExtractors = new ArrayList<List<TrieMap.KeyExtractor<String>>>();
	
	/** Rule feature extractors. */
	protected List<List<TrieMap.KeyExtractor<String>>> unaryRuleExtractors = new ArrayList<List<TrieMap.KeyExtractor<String>>>();
	
	/** Binary rule feature extractors. */
	protected List<List<TrieMap.KeyExtractor<String>>> binaryRuleExtractors = new ArrayList<List<TrieMap.KeyExtractor<String>>>();
	
	/** Distance feature extractors. */
	protected List<List<TrieMap.KeyExtractor<String>>> distExtractors = new ArrayList<List<TrieMap.KeyExtractor<String>>>();
	
	
	/** Constructor. */
	public SyntacticFeatureExtractor() {
		// init lazy feature extractors
		lexExtractors.add(lexcat_word());
		lexExtractors.add(lexcat_pos());
		unaryRuleExtractors.add(unary_rule());
		unaryRuleExtractors.add(unary_rule_word());
		unaryRuleExtractors.add(unary_rule_pos());
		binaryRuleExtractors.add(binary_rule());
		binaryRuleExtractors.add(binary_rule_word());
		binaryRuleExtractors.add(binary_rule_pos());
		binaryRuleExtractors.add(rule_word_word());
		binaryRuleExtractors.add(rule_word_pos());
		binaryRuleExtractors.add(rule_pos_word());
		binaryRuleExtractors.add(rule_pos_pos());
		distExtractors.add(rule_word_dist());
		distExtractors.add(rule_pos_dist());
		distExtractors.add(rule_word_dist_puncts());
		distExtractors.add(rule_pos_dist_puncts());
		distExtractors.add(rule_word_dist_verbs());
		distExtractors.add(rule_pos_dist_verbs());
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
			currentMap = new FeatureMap();
			inc(lexExtractors);
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
				inc(unaryRuleExtractors);
			}
			else if (inputs.length == 2) {
				currentMap = new FeatureMap(getFeatureMap(inputs[0]), getFeatureMap(inputs[1]));
				currentSibling = sibling(sign, inputs);
				inc(binaryRuleExtractors);
				// dist feats
				if (includeDistFeats) {
					currentWords = null; // get words and head indices lazily
					currentDistW = null; currentDistP = null; currentDistV = null; // also reset current distances
					inc(distExtractors);
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

	/** Returns the sibling sign from among the two inputs. */
	protected Sign sibling(Sign sign, Sign[] inputs) {
		if (sign.getLexHead() == inputs[0].getLexHead()) return inputs[1];
		else return inputs[0];
	}
	
	/** Sets the current words, if null, along with head indices. */
	protected void setCurrentWords() {
		if (currentWords != null) return;
		currentWords = currentSign.getWords();
		Word head = currentSign.getLexHead().getWords().get(0);
		Word sibHead = currentSibling.getLexHead().getWords().get(0);
		currentHeadIndex = find(currentWords, head);
		currentSibHeadIndex = find(currentWords, sibHead);
	}
	
	/** Returns the index of the given word in the list, or -1 if not found. */
	protected int find(List<Word> words, Word word) {
		int len = words.size();
		for (int i=0; i < len; i++) {
			if (words.get(i) == word) return i;
		}
		return -1;
	}
	
	/** Returns the distance in intervening words as 0w, 1w, 2w or 3w (for 3 or more). */
	protected String distWords() {
		if (currentDistW != null) return currentDistW;
		setCurrentWords();
		int dist = Math.abs(currentHeadIndex - currentSibHeadIndex) - 1;
		switch (dist) {
			case 0: return currentDistW = "0w";
			case 1: return currentDistW = "1w";
			case 2: return currentDistW = "2w";
			default: return currentDistW = "3w";
		}
	}
	
	/** Returns the distance in intervening punctuation marks as 0p, 1p, 2p or 3p (for 3 or more). */
	protected String distPuncts() {
		if (currentDistP != null) return currentDistP;
		setCurrentWords();
		int min = Math.min(currentHeadIndex, currentSibHeadIndex);
		int max = Math.max(currentHeadIndex, currentSibHeadIndex);
		int count = 0;
		for (int i=min+1; i < max; i++) {
			Word w = currentWords.get(i); 
			if (isPunct(w)) count++;
		}
		switch (count) {
			case 0: return currentDistP = "0p";
			case 1: return currentDistP = "1p";
			case 2: return currentDistP = "2p";
			default: return currentDistP = "3p";
		}
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
	
	/** Returns the distance in intervening verbs as 0v, 1v, or 2v (for 2 or more). */
	protected String distVerbs() {
		if (currentDistV != null) return currentDistV;
		setCurrentWords();
		int min = Math.min(currentHeadIndex, currentSibHeadIndex);
		int max = Math.max(currentHeadIndex, currentSibHeadIndex);
		int count = 0;
		for (int i=min+1; i < max; i++) {
			Word w = currentWords.get(i); 
			if (isVerb(w)) count++;
		}
		switch (count) {
			case 0: return currentDistV = "0v";
			case 1: return currentDistV = "1v";
			default: return currentDistV = "2v";
		}
	}
	
	/**
	 * Returns whether a word is a verb.
	 * The default implementation tests for a POS tag beginning with V.
	 */
	protected boolean isVerb(Word word) {
		String pos = word.getPOS();
		return (pos.startsWith("V"));
	}
	
	
	// lex cat + word
	private List<TrieMap.KeyExtractor<String>> lexcat_word() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(2);
		add_supertag(retval);
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentSign.getWordForm(); }});
		return retval;
	}
	
	// add prefix + supertag
	private void add_supertag(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return PREFIX; }});
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentSign.getSupertag(); }});
	}
	
	// lex cat + pos
	private List<TrieMap.KeyExtractor<String>> lexcat_pos() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(2);
		add_supertag(retval);
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentSign.getPOS(); }});
		return retval;
	}
	
	// rule
	private List<TrieMap.KeyExtractor<String>> unary_rule() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(2);
		add_unary_rule(retval);
		return retval;
	}
	
	private void add_unary_rule(List<TrieMap.KeyExtractor<String>> retval) {
		add_supertag(retval);
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentInputs[0].getSupertag(); }});
	}
	
	private List<TrieMap.KeyExtractor<String>> binary_rule() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(3);
		add_binary_rule(retval);
		return retval;
	}
	
	private void add_binary_rule(List<TrieMap.KeyExtractor<String>> retval) {
		add_unary_rule(retval);
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentInputs[1].getSupertag(); }});
	}
	
	// rule + head word
	private List<TrieMap.KeyExtractor<String>> unary_rule_word() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(3);
		add_unary_rule_word(retval);
		return retval;
	}
	
	private void add_unary_rule_word(List<TrieMap.KeyExtractor<String>> retval) {
		add_unary_rule(retval);
		add_lex_word(retval);
	}
	
	private void add_lex_word(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentSign.getLexHead().getWordForm(); }});
	}
	
	private List<TrieMap.KeyExtractor<String>> binary_rule_word() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(4);
		add_binary_rule_word(retval);
		return retval;
	}
	
	private void add_binary_rule_word(List<TrieMap.KeyExtractor<String>> retval) {
		add_binary_rule(retval);
		add_lex_word(retval);
	}
	
	// rule + head pos
	private List<TrieMap.KeyExtractor<String>> unary_rule_pos() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(3);
		add_unary_rule_pos(retval);
		return retval;
	}
	
	private void add_unary_rule_pos(List<TrieMap.KeyExtractor<String>> retval) {
		add_unary_rule(retval);
		add_lex_pos(retval);
	}
	
	private void add_lex_pos(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentSign.getLexHead().getPOS(); }});
	}
	
	private List<TrieMap.KeyExtractor<String>> binary_rule_pos() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(4);
		add_binary_rule_pos(retval);
		return retval;
	}
	
	private void add_binary_rule_pos(List<TrieMap.KeyExtractor<String>> retval) {
		add_binary_rule(retval);
		add_lex_pos(retval);
	}
	
	// rule + head word + sibling word
	private List<TrieMap.KeyExtractor<String>> rule_word_word() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_binary_rule_word(retval);
		add_sibling_word(retval);
		return retval;
	}
	
	private void add_sibling_word(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentSibling.getLexHead().getWordForm(); }});
	}
	
	// rule + head word + sibling pos
	private List<TrieMap.KeyExtractor<String>> rule_word_pos() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_binary_rule_word(retval);
		add_sibling_pos(retval);
		return retval;
	}
	
	private void add_sibling_pos(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return currentSibling.getLexHead().getPOS(); }});
	}
	
	// rule + head pos + sibling word
	private List<TrieMap.KeyExtractor<String>> rule_pos_word() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_binary_rule_pos(retval);
		add_sibling_word(retval);
		return retval;
	}
	
	// rule + head pos + sibling pos
	private List<TrieMap.KeyExtractor<String>> rule_pos_pos() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_binary_rule_pos(retval);
		add_sibling_pos(retval);
		return retval;
	}
	
	// rule + head word + dist
	private List<TrieMap.KeyExtractor<String>> rule_word_dist() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_binary_rule_word(retval);
		add_dist_words(retval);
		return retval;
	}
	
	private void add_dist_words(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return distWords(); }});
	}
	
	// rule + head pos + dist
	private List<TrieMap.KeyExtractor<String>> rule_pos_dist() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_binary_rule_pos(retval);
		add_dist_words(retval);
		return retval;
	}
	
	// rule + head word + dist in puncts
	private List<TrieMap.KeyExtractor<String>> rule_word_dist_puncts() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_binary_rule_word(retval);
		add_dist_puncts(retval);
		return retval;
	}
	
	private void add_dist_puncts(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return distPuncts(); }});
	}
	
	// rule + head pos + dist in puncts
	private List<TrieMap.KeyExtractor<String>> rule_pos_dist_puncts() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_binary_rule_pos(retval);
		add_dist_puncts(retval);
		return retval;
	}
	
	// rule + head word + dist in verbs
	private List<TrieMap.KeyExtractor<String>> rule_word_dist_verbs() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_binary_rule_word(retval);
		add_dist_verbs(retval);
		return retval;
	}
	
	private void add_dist_verbs(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return distVerbs(); }});
	}
	
	// rule + head pos + dist in verbs
	private List<TrieMap.KeyExtractor<String>> rule_pos_dist_verbs() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(5);
		add_binary_rule_pos(retval);
		add_dist_verbs(retval);
		return retval;
	}
}
