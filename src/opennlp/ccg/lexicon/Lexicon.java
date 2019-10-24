///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004-9 Jason Baldridge, Gann Bierner and 
//                      Michael White (University of Edinburgh, The Ohio State University)
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

package opennlp.ccg.lexicon;

import opennlp.ccg.grammar.*;
import opennlp.ccg.synsem.*;
import opennlp.ccg.unify.*;
import opennlp.ccg.util.*;
import opennlp.ccg.hylo.*;

import org.jdom.*;

import java.io.*;
import java.net.*;
import java.util.*;

import gnu.trove.*;


/**
 * Contains words and their associated categories and semantics.
 * Lookup can be filtered by plugging in a supertagger.
 * 
 *
 * @author      Gann Bierner
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.78 $, $Date: 2011/10/31 02:01:06 $
 */
public class Lexicon implements WithGrammar {
    
    /** Flag used to indicate a purely syntactic edge, with no associated semantics. */
    public static final String NO_SEM_FLAG = "*NoSem*";
    
    /** Constant used to signal the substitution of the stem or pred. */
    public static final String DEFAULT_VAL = "[*DEFAULT*]";
    
    // supertagger
    private SupertaggerAdapter _supertagger = null;
    
    // various maps
    private GroupMap<Word,MorphItem> _words;
    private GroupMap<String,Object> _stems;
    private GroupMap<String,FeatureStructure> _macros;
    private HashMap<String,MacroItem> _macroItems;

    private GroupMap<String,EntriesItem[]> _posToEntries;
    private GroupMap<String,EntriesItem> _stagToEntries;
    private GroupMap<String,Word> _predToWords;
    private GroupMap<String,String> _relsToPreds;
    private GroupMap<String,String> _coartRelsToPreds;
    
    // coarticulation attrs
    private Set<String> _coartAttrs;
    private Set<String> _indexedCoartAttrs;
    
    // attrs per atomic category type, across all entries
    private GroupMap<String,String> _catsToAttrs;
    private Set<String> _lfAttrs;
    
    // distributive attributes
    private String[] _distributiveAttrs = null;
    
    // licensing features
    private LicensingFeature[] _licensingFeatures = null;
    
    // relation sorting
    private HashMap<String,Integer> _relationIndexMap = new HashMap<String,Integer>();
    
    // interner for caching lex lookups during realization
    private Interner<Object> lookupCache = new Interner<Object>(true);
    
    /** The grammar that this lexicon is part of. */
    public Grammar grammar;
    
    /** The tokenizer.  (Defaults to DefaultTokenizer.) */
    public final Tokenizer tokenizer;
    
    /** Flag for whether the lexicon is open, ie complete lexical category mappings are not expected. (Defaults to false.) */
    public boolean openlex = false;
    
    /** Flag for whether to show warnings for failed sem class unification. (Defaults to false.) */
    public boolean debugSemClasses = false;
    

    /*************************************************************
     * Constructor
     *************************************************************/
    public Lexicon(){
        this.tokenizer = new DefaultTokenizer();
        Grammar.theGrammar.lexicon = this;
        loadLicensingFeatures(null);
        loadRelationSortOrder(null);
    }

    public Lexicon(Grammar grammar) {
        this.grammar = grammar;
        this.tokenizer = new DefaultTokenizer();
    }

    /** Constructor with tokenizer. */
    public Lexicon(Grammar grammar, Tokenizer tokenizer) {
        this.grammar = grammar;
        this.tokenizer = tokenizer;
    }

    //-------------------------------------------------------------
    
    /** Sets the supertagger (null if none). */
    public void setSupertagger(SupertaggerAdapter supertagger) { _supertagger = supertagger; }

    public void setGrammar(Grammar grammar){
        this.grammar = grammar;
    }
    
    public void init(List<Family> lexicon, List<MorphItem> morph, List<MacroItem> macroModel){
        // index words; also index stems to words, as default preds
        // store indexed coarticulation attrs too
        _words = new GroupMap<Word,MorphItem>();
        _predToWords = new GroupMap<String,Word>();
        _coartAttrs = new HashSet<String>();
        _indexedCoartAttrs = new HashSet<String>();
        for (MorphItem morphItem : morph) {
            Word surfaceWord = morphItem.getSurfaceWord();
            _words.put(surfaceWord, morphItem);
            _predToWords.put(morphItem.getWord().getStem(), surfaceWord);
            if (morphItem.isCoart()) {
                Word indexingWord = morphItem.getCoartIndexingWord();
                _words.put(indexingWord, morphItem);
                Pair<String,String> first = indexingWord.getSurfaceAttrValPairs().next();
                _indexedCoartAttrs.add(first.a);
                for (Iterator<Pair<String,String>> it = surfaceWord.getSurfaceAttrValPairs(); it.hasNext(); ) {
                    Pair<String,String>  p = it.next();
                    _coartAttrs.add(p.a);
                }
            }
        }

        // index entries based on stem+pos
        _stems = new GroupMap<String,Object>();
        _posToEntries = new GroupMap<String,EntriesItem[]>();
        // index entries by supertag+pos, for supertagging
        _stagToEntries = new GroupMap<String,EntriesItem>();
        // also index rels and coart rels to preds
        _relsToPreds = new GroupMap<String,String>();
        _coartRelsToPreds = new GroupMap<String,String>();
        // and gather list of attributes used per atomic category type
        _catsToAttrs = new GroupMap<String,String>();
        _lfAttrs = new HashSet<String>();
        // and remember family and ent, names, for checking excluded list on morph items
        HashSet<String> familyAndEntryNames = new HashSet<String>();

        // index each family
        for (Family family : lexicon) {

            familyAndEntryNames.add(family.getName());
            EntriesItem[] entries = family.getEntries();
            DataItem[] data = family.getData();

            // for generic use when we get an unknown stem
            // from the morphological analyzer
            if (!family.isClosed()) {
                _posToEntries.put(family.getPOS(), entries);
            }

            // scan through entries
            for (int j=0; j < entries.length; j++) {
                // index
                EntriesItem eItem = entries[j];
                _stagToEntries.put(eItem.getSupertag()+family.getPOS(), eItem);
                if (eItem.getStem().length() > 0) {
                    _stems.put(eItem.getStem()+family.getPOS(), eItem);
                }
                try {
                    // gather features
                    eItem.getCat().forall(gatherAttrs);
                    // record names
                    familyAndEntryNames.add(eItem.getName());
                    familyAndEntryNames.add(eItem.getQualifiedName());
                }
                catch (RuntimeException exc) {
                    System.err.println("exception for: " + family.getName() + ": " + exc);
                }
            }

            // scan through data
            for (int j=0; j < data.length; j++) {
                DataItem dItem = data[j];
                _stems.put(dItem.getStem()+family.getPOS(), new Pair<DataItem, EntriesItem[]>(dItem,entries));
                // index non-default preds to words
                if (!dItem.getStem().equals(dItem.getPred())) {
                    Collection<Word> words = (Collection<Word>) _predToWords.get(dItem.getStem());
                    if (words == null) {
                        if (!openlex) {
                            System.out.print("Warning: couldn't find words for pred '");
                            System.out.println(dItem.getPred() + "' with stem '" + dItem.getStem() + "'");
                        }
                    }
                    else {
                        for (Iterator<Word> it = words.iterator(); it.hasNext(); ) {
                            _predToWords.put(dItem.getPred(), it.next());
                        }
                    }
                }
            }

            // index rels to preds
            // nb: this covers relational (eg @x<GenRel>e) and featural (eg @e<tense>past)
            //     elementary predications
            List<String> indexRels = new ArrayList<String>(3);
            String familyIndexRel = family.getIndexRel();
            if (familyIndexRel.length() > 0) {
                indexRels.add(familyIndexRel);
            }
            for (int j=0; j < entries.length; j++) {
                EntriesItem eItem = entries[j];
                String indexRel = eItem.getIndexRel();
                if (indexRel.length() > 0 && !indexRel.equals(familyIndexRel)) {
                    indexRels.add(indexRel);
                }
            }
            for (Iterator<String> it = indexRels.iterator(); it.hasNext(); ) {
                String indexRel = it.next();
                // nb: not indexing on entries items, b/c some stems are still defaults
                for (int j=0; j < data.length; j++) {
                    DataItem dItem = data[j];
                    _relsToPreds.put(indexRel, dItem.getPred());
                }
            }

            // index coart rels (features, really) to preds
            String coartRel = family.getCoartRel();
            if (coartRel.length() > 0) {
                for (int j=0; j < data.length; j++) {
                    _coartRelsToPreds.put(coartRel, data[j].getPred());
                }
            }
        }

        // index the macros
        _macros = new GroupMap<String, FeatureStructure>();
        // nb: could just index MacroItem objects for feature structures too;
        //     this might be a bit cleaner, but life is short
        _macroItems = new HashMap<String, MacroItem>();
        for (MacroItem mi : macroModel) {
            String macName = mi.getName();
            FeatureStructure[] specs = mi.getFeatureStructures();
            for (int j=0; j < specs.length; j++) {
                _macros.put(macName, specs[j]);
            }
            // this is for handling LF part of macros
            _macroItems.put(macName, mi);
        }

        // with morph items, check POS, macro names, excluded list for xref
        for (MorphItem morphItem : morph) {
            Word w = morphItem.getWord();
            if (!openlex &&
                    !_stems.containsKey(w.getStem() + w.getPOS()) &&
                    !_posToEntries.containsKey(w.getPOS()))
            {
                System.err.println(
                        "Warning: no entries for stem '" + w.getStem() +
                                "' and POS '" + w.getPOS() +
                                "' found for word '" + w + "'"
                );
            }
            String[] macroNames = morphItem.getMacros();
            for (int j=0; j < macroNames.length; j++) {
                if (!_macroItems.containsKey(macroNames[j])) {
                    System.err.println("Warning: macro " + macroNames[j] +
                            " not found for word '" + morphItem.getWord() + "'");
                }
            }
            String[] excludedNames = morphItem.getExcluded();
            for (int j=0; j < excludedNames.length; j++) {
                if (!familyAndEntryNames.contains(excludedNames[j])) {
                    System.err.println("Warning: excluded family or entry '" + excludedNames[j] +
                            "' not found for word '" + morphItem.getWord() + "'");
                }
            }
        }
    }
    /** Loads the lexicon and morph files. */
    public void init(URL lexiconUrl, URL morphUrl) throws IOException {
        
    	List<Family> lexicon = null;
        List<MorphItem> morph = null;
        List<MacroItem> macroModel = null;

        // load category families (lexicon), morph forms and macros
        lexicon = getLexicon(lexiconUrl);
        Pair<List<MorphItem>,List<MacroItem>> morphInfo = getMorph(morphUrl);
        morph = morphInfo.a; macroModel = morphInfo.b;

        init(lexicon, morph, macroModel);
    }
    
    /** Expands inheritsFrom links to feature equations for those features not explicitly listed. */ 
    public void expandInheritsFrom(Category cat) {
        expandInheritsFrom(cat, null);
    }
    
    /** Expands inheritsFrom links to feature equations for those features not explicitly listed. */ 
    public void expandInheritsFrom(Category cat, Category cat2) {
        // index feature structures
        featStrucMap.clear();
        cat.forall(indexFeatStrucs);
        if (cat2 != null) { cat2.forall(indexFeatStrucs); }
        // add feature eqs 
        cat.forall(doInheritsFrom);
        if (cat2 != null) { cat2.forall(doInheritsFrom); }
    }
    
    // gathers attrs from a category
    private CategoryFcn gatherAttrs = new CategoryFcnAdapter() {
        public void forall(Category c) {
            if (!(c instanceof AtomCat)) return;
            String type = ((AtomCat)c).getType();
            FeatureStructure fs = c.getFeatureStructure();
            if (fs == null) return;
            for (Iterator<String> it = fs.getAttributes().iterator(); it.hasNext(); ) {
                String att = it.next();
                _catsToAttrs.put(type, att);
                if (fs.getValue(att) instanceof LF) {
                    _lfAttrs.add(att);
                }
            }
        }
    };

    // a map from indices to atomic categories, reset for each category
    private TIntObjectHashMap featStrucMap = new TIntObjectHashMap();
    
    // fills in featStrucMap for a category
    private CategoryFcn indexFeatStrucs = new CategoryFcnAdapter() {
        public void forall(Category c) {
            FeatureStructure fs = c.getFeatureStructure();
            if (fs != null && fs.getIndex() != 0)
                featStrucMap.put(fs.getIndex(), fs);
        }
    };

    // adds feature equations to percolate attributes from inheritsFrom feature 
    // structure, except for any attributes already present
    private CategoryFcn doInheritsFrom = new CategoryFcnAdapter() {
        public void forall(Category c) {
            // get feature structures
            if (!(c instanceof AtomCat)) return;
            String type = ((AtomCat)c).getType();
            FeatureStructure fs = c.getFeatureStructure();
            GFeatStruc gfs = (GFeatStruc) fs;
            if (gfs == null || gfs.getInheritsFrom() == 0) return;
            int inhf = gfs.getInheritsFrom();
            FeatureStructure inhfFS = (FeatureStructure) featStrucMap.get(inhf);
            if (inhfFS != null) {
                // copy values of features from inhfFS not already present
                for (Iterator<String> it = inhfFS.getAttributes().iterator(); it.hasNext(); ) {
                    String att = it.next(); 
                    if (gfs.hasAttribute(att)) continue;
                    gfs.setFeature(att, UnifyControl.copy(inhfFS.getValue(att)));
                }
                // for each possible attr used with this type and not already present, 
                // add feature equation
                Collection<String> attrs = (Collection<String>) _catsToAttrs.get(type);
                if (attrs == null) return;
                for (Iterator<String> it = attrs.iterator(); it.hasNext(); ) {
                    String att = it.next(); 
                    if (gfs.hasAttribute(att)) continue;
                    String varName = att.toUpperCase() + inhf;
                    if (_lfAttrs.contains(att)) {
                        gfs.setFeature(att, new HyloVar(varName));
                        inhfFS.setFeature(att, new HyloVar(varName));
                    }
                    else {
                        gfs.setFeature(att, new GFeatVar(varName));
                        inhfFS.setFeature(att, new GFeatVar(varName));
                    }
                }
            }
            else {
                System.err.println(
                    "Warning: no feature structure with inheritsFrom index of " + inhf + 
                    " found in category " + c
                );
            }
        }
    };

    
    /**
     * Returns the lexical signs indexed by the given rel, or null if none. 
     */
    public Collection<Sign> getSignsFromRel(String rel) {
        // check cache (if not doing supertagging)
    	if (_supertagger == null) {
	        RelLookup lookup = new RelLookup(rel);
	        RelLookup retLookup = (RelLookup) lookupCache.getInterned(lookup);
	        if (retLookup != null) return retLookup.signs;
    	}
        // lookup signs via preds
        Collection<String> preds = (Collection<String>) _relsToPreds.get(rel);
        if (preds == null) return null;
        Collection<Sign> retval = getSignsFromRelAndPreds(rel, preds);
        // cache non-null result (if not doing supertagging)
        if (_supertagger == null && retval != null) {
        	RelLookup lookup = new RelLookup(rel);
            lookup.signs = retval; lookupCache.intern(lookup);
        }
        return retval;
    }

    // get signs for rel via preds, or null if none
    private Collection<Sign> getSignsFromRelAndPreds(String rel, Collection<String> preds) {
        List<Sign> retval = new ArrayList<Sign>();
        for (Iterator<String> it = preds.iterator(); it.hasNext(); ) {
            String pred = it.next();
            Collection<Sign> signs = getSignsFromPredAndTargetRel(pred, rel);
            if (signs != null) retval.addAll(signs);
        }
        // return null if none survive filter
        if (retval.size() > 0) return retval;
        else return null;
    }

    /**
     * Returns the lexical signs indexed by the given pred.
     * If the pred is not listed in the lexicon, the tokenizer is 
     * consulted to see if it is a special token (date, time, etc.); 
     * otherwise, null is returned.
     * Coarticulations are applied for the given rels, if non-null.
     */
    public Collection<Sign> getSignsFromPred(String pred, List<String> coartRels) {
        // check cache (if not doing supertagging)
    	if (_supertagger == null) {
	        PredLookup lookup = new PredLookup(pred, coartRels);
	        PredLookup retLookup = (PredLookup) lookupCache.getInterned(lookup);
	        if (retLookup != null) return retLookup.signs;
    	}
        // lookup pred
        Collection<Sign> result = getSignsFromPredAndTargetRel(pred, null);
        if (result == null) return null;
        // apply coarts for rels
        if (coartRels != null) applyCoarts(coartRels, result);
        // cache result (if not doing supertagging)
    	if (_supertagger == null) {
	        PredLookup lookup = new PredLookup(pred, coartRels);
    		lookup.signs = result; lookupCache.intern(lookup);
    	}
        // and return
        return result;
    }
        
    // get signs using an additional arg for a target rel
    private Collection<Sign> getSignsFromPredAndTargetRel(String pred, String targetRel) {
        
        Collection<Word> words = (Collection<Word>) _predToWords.get(pred);
        String specialTokenConst = null;
        
        // for robustness, when using supertagger, add words for pred sans sense index
        int dotIndex = -1;
        if (_supertagger != null && !Character.isDigit(pred.charAt(0)) && // skip numbers 
        		(dotIndex = pred.lastIndexOf('.')) > 0 && pred.length() > dotIndex+1 &&
        		pred.charAt(dotIndex+1) != '_') // skip titles, eg Mr._Smith
        {
        	String barePred = pred.substring(0, dotIndex);
        	Collection<Word> barePredWords = (Collection<Word>) _predToWords.get(barePred);
        	if (words == null) words = barePredWords;
        	else if (barePredWords != null) {
        		Set<Word> unionWords = new HashSet<Word>(words);
        		unionWords.addAll(barePredWords);
        		words = unionWords;
        	}
        }
        
        if (words == null) {
            specialTokenConst = tokenizer.getSpecialTokenConstant(tokenizer.isSpecialToken(pred));
            if (specialTokenConst == null) return null;
            // lookup words with pred = special token const
            Collection<Word> specialTokenWords = (Collection<Word>) _predToWords.get(specialTokenConst);
            // replace special token const with pred
            if (specialTokenWords == null) return null;
            words = new ArrayList<Word>(specialTokenWords.size());
            for (Iterator<Word> it = specialTokenWords.iterator(); it.hasNext(); ) {
                Word stw = it.next();
                Word w = Word.createSurfaceWord(stw, pred);
                words.add(w);
            }
        }
        
        List<Sign> retval = new ArrayList<Sign>();
        for (Iterator<Word> it = words.iterator(); it.hasNext(); ) {
            Word w = it.next();
            try {
                SignHash signs = getSignsFromWord(w, specialTokenConst, pred, targetRel);
                retval.addAll(signs.asSignSet());
            }
            // shouldn't happen
            catch (LexException exc) {
                System.err.println("Unexpected lex exception for word " + w + ": " + exc);
            }
        }
        return retval;
    }
    
    // look up and apply coarts for given rels to each sign in result
    private void applyCoarts(List<String> coartRels, Collection<Sign> result) {
        List<Sign> inputSigns = new ArrayList<Sign>(result);
        result.clear();
        List<Sign> outputSigns = new ArrayList<Sign>(inputSigns.size());
        // for each rel, lookup coarts and apply to input signs, storing results in output signs
        for (Iterator<String> it = coartRels.iterator(); it.hasNext(); ) {
            String rel = it.next();
            Collection<String> preds = (Collection<String>) _coartRelsToPreds.get(rel);
            if (preds == null) continue; // not expected
            Collection<Sign> coartResult = getSignsFromRelAndPreds(rel, preds);
            if (coartResult == null) continue;
            for (Iterator<Sign> it2 = coartResult.iterator(); it2.hasNext(); ) {
                Sign coartSign = it2.next();
                // apply to each input
                for (int j = 0; j < inputSigns.size(); j++) {
                    Sign sign = inputSigns.get(j);
                    grammar.rules.applyCoart(sign, coartSign, outputSigns);
                }
            }
            // switch output to input for next iteration
            inputSigns.clear();
            inputSigns.addAll(outputSigns);
            outputSigns.clear();
        }
        // add results back
        result.addAll(inputSigns);
    }

    
    /**
     * For a string of 1 or more surface words, return all of the lexical
     * entries for each word as a list of sign hashes.
     * Tokenization is performed using the configured tokenizer.
     *
     * @param w the words in string format
     * @return a list of sign hashes
     * @exception LexException thrown if word not found
     */
    public List<SignHash> getEntriesFromWords(String s) throws LexException { 
        List<SignHash> entries = new ArrayList<SignHash>();
        List<Word> words = tokenizer.tokenize(s);
        for (Iterator<Word> it = words.iterator(); it.hasNext(); ) {
            Word w = it.next();
            SignHash signs = getSignsFromWord(w);
            if (signs.size() == 0) {
                throw new LexException("Word not in lexicon: \"" + w +"\"");
            }
            entries.add(signs);
        }
        return entries;
    }
    
    /**
     * For a given word, return all of its surface word's lexical entries.
     * If the word is not listed in the lexicon, the tokenizer is 
     * consulted to see if it is a special token (date, time, etc.); 
     * otherwise an exception is thrown.
     * If the word has coarticulations, all applicable coarticulation 
     * entries are applied to the base word, in an arbitrary order.
     *
     * @param w the word
     * @return a sign hash
     * @exception LexException thrown if word not found
     */
    public SignHash getSignsFromWord(Word w) throws LexException {
        // reduce word to its core, removing coart attrs if any
    	Word surfaceWord = Word.createSurfaceWord(w);
        Word coreWord = (surfaceWord.attrsIntersect(_coartAttrs)) 
            ? Word.createCoreSurfaceWord(surfaceWord, _coartAttrs) 
            : surfaceWord;
        // lookup core word
        SignHash result = getSignsFromWord(coreWord, null, null, null);
        if (result.size() == 0) {
            throw new LexException(coreWord + " not found in lexicon");
        }
        // return signs if no coart attrs
        if (coreWord == surfaceWord) return result; 
        // otherwise apply coarts for word
        applyCoarts(surfaceWord, result);
        return result; 
    }
    
    // look up and apply coarts for w to each sign in result
    @SuppressWarnings("unchecked")
	private void applyCoarts(Word w, SignHash result) throws LexException {
        List<Sign> inputSigns = new ArrayList<Sign>(result.asSignSet());
        result.clear();
        List<Sign> outputSigns = new ArrayList<Sign>(inputSigns.size());
        // for each surface attr, lookup coarts and apply to input signs, storing results in output signs
        for (Iterator<Pair<String,String>> it = w.getSurfaceAttrValPairs(); it.hasNext(); ) {
            Pair<String,String> p = it.next();
            String attr = (String) p.a;
            if (!_indexedCoartAttrs.contains(attr)) continue;
            String val = (String) p.b;
            Word coartWord = Word.createWord(attr, val);
            SignHash coartResult = getSignsFromWord(coartWord, null, null, null);
            for (Iterator<Sign> it2 = coartResult.iterator(); it2.hasNext(); ) {
                Sign coartSign = it2.next();
                // apply to each input
                for (int j = 0; j < inputSigns.size(); j++) {
                    Sign sign = inputSigns.get(j);
                    grammar.rules.applyCoart(sign, coartSign, outputSigns);
                }
            }
            // switch output to input for next iteration
            inputSigns.clear();
            inputSigns.addAll(outputSigns);
            outputSigns.clear();
        }
        // add results back
        result.addAll(inputSigns);
    }
    
    // get signs with additional args for a known special token const, target pred and target rel        
    private SignHash getSignsFromWord(Word w, String specialTokenConst, String targetPred, String targetRel) throws LexException {

        Collection<MorphItem> morphItems = (specialTokenConst == null)
            ? (Collection<MorphItem>) _words.get(w)
            : null;

        if (morphItems == null) {
            // check for special tokens
            if (specialTokenConst == null) {
                specialTokenConst = tokenizer.getSpecialTokenConstant(tokenizer.isSpecialToken(w.getForm()));
                targetPred = w.getForm();
            }
            if (specialTokenConst != null) {
                Word key = Word.createSurfaceWord(w, specialTokenConst);
                morphItems = (Collection<MorphItem>) _words.get(key);
            }
            // otherwise throw lex exception
            if (morphItems == null)
                throw new LexException(w + " not in lexicon");
        }

        SignHash result = new SignHash();

        for (Iterator<MorphItem> MI = morphItems.iterator(); MI.hasNext();) {
            getWithMorphItem(w, MI.next(), targetPred, targetRel, result);
        }

        return result;
    }


    // given MorphItem
    private void getWithMorphItem(Word w, MorphItem mi, String targetPred, String targetRel, SignHash result)
        throws LexException 
    {
    	// get supertags for filtering, if a supertagger is installed
    	Map<String,Double> supertags = null;
    	Set<String> supertagsFound = null;
    	if (_supertagger != null) {
    		supertags = _supertagger.getSupertags();
    		if (supertags != null) supertagsFound = new HashSet<String>(supertags.size());
    	}
    	
        // get macro adder
        MacroAdder macAdder = getMacAdder(mi);
        
        // if we have this stem in our lexicon
        String stem = mi.getWord().getStem();
        String pos = mi.getWord().getPOS();
        Set<EntriesItem[]> explicitEntries = null; // for storing entries from explicitly listed family members
        if (_stems.containsKey(stem+pos)) {
            explicitEntries = new HashSet<EntriesItem[]>();
            Collection<Object> stemItems = (Collection<Object>)_stems.get(stem+pos);
            for (Iterator<Object> I=stemItems.iterator(); I.hasNext();) {
                Object item = I.next();
                // see if it's an EntriesItem
                if (item instanceof EntriesItem) {
                    EntriesItem entry = (EntriesItem) item;
                    // do lookup
                    getWithEntriesItem(w, mi, stem, stem, targetPred, targetRel, entry, macAdder, supertags, supertagsFound, result);
                } 
                // otherwise it has to be a Pair containing a DataItem and 
                // an EntriesItem[]
                else {
                    @SuppressWarnings("rawtypes")
					DataItem dItem = (DataItem)((Pair)item).a;
                    @SuppressWarnings("rawtypes")
					EntriesItem[] entries = (EntriesItem[])((Pair)item).b;
                    // store entries
                    explicitEntries.add(entries);
                    // do lookup
                    getWithDataItem(w, mi, dItem, entries, targetPred, targetRel, macAdder, supertags, supertagsFound, result);
                }
            }
        }
        
        // for entries that are not explicitly in the lexicon file, we have to create
        // Signs from the open class entries with the appropriate part-of-speech
        Collection<EntriesItem[]> entrySets = (Collection<EntriesItem[]>)_posToEntries.get(pos);
        if (entrySets != null) {
	        for (Iterator<EntriesItem[]> E=entrySets.iterator(); E.hasNext(); ) {
	            EntriesItem[] entries = E.next();  
	            // skip if entries explicitly listed
	            if (explicitEntries != null && explicitEntries.contains(entries)) continue;
	            // otherwise get entries with pred = targetPred, or stem if null
	            String pred = (targetPred != null) ? targetPred : stem;
	            getWithDataItem(w, mi, new DataItem(stem, pred), entries, targetPred, targetRel, macAdder, supertags, supertagsFound, result);
	        }
        }
        
        // finally do entries for any remaining supertags
        if (supertags != null) {
        	for (String supertag : supertags.keySet()) {
        		if (supertagsFound.contains(supertag)) continue;
        		Set<EntriesItem> entries = _stagToEntries.get(supertag+pos);
        		if (entries == null) continue; // nb: could be a POS mismatch
	            // get entries with pred = targetPred, or stem if null
	            String pred = (targetPred != null) ? targetPred : stem;
        		for (EntriesItem entry : entries) {
        			if (!entry.getStem().equals(DEFAULT_VAL)) continue;
		            getWithEntriesItem(w, mi, stem, pred, targetPred, targetRel, entry, macAdder, supertags, supertagsFound, result);
        		}
        	}
        }
    }

    
    // given DataItem
    private void getWithDataItem(Word w, MorphItem mi,  
                                 DataItem item, EntriesItem[] entries, 
                                 String targetPred, String targetRel, 
                                 MacroAdder macAdder,
                                 Map<String,Double> supertags,
                                 Set<String> supertagsFound,
                                 SignHash result) 
    {
        for (int i=0; i < entries.length; i++) {
            EntriesItem entry = entries[i];
            if (entry.getStem().equals(DEFAULT_VAL)) {
                getWithEntriesItem(w, mi, item.getStem(), item.getPred(), targetPred, targetRel, entry, macAdder, supertags, supertagsFound, result);
            }
        }
    }

    // given EntriesItem
    private void getWithEntriesItem(Word w, MorphItem mi, 
                                    String stem, String pred, 
                                    String targetPred, String targetRel,
                                    EntriesItem item,
                                    MacroAdder macAdder,
                                    Map<String,Double> supertags,
                                    Set<String> supertagsFound,
                                    SignHash result) 
    {
        // ensure apropos
        if (targetPred != null && !targetPred.equals(pred)) return; 
        if (targetRel != null && !targetRel.equals(item.getIndexRel()) && !targetRel.equals(item.getCoartRel())) return; 
        if (!item.getActive().booleanValue()) return;
        if (mi.excluded(item)) return;
        
        try {
	        // copy and add macros
	        Category cat = item.getCat().copy();
	        macAdder.addMacros(cat);
	
	        // replace DEFAULT_VAL with pred, after first 
	        // unifying type of associated nom var(s) with sem class 
	        unifySemClass(cat, mi.getWord().getSemClass());
	        REPLACEMENT = pred; 
	        cat.deepMap(defaultReplacer);
	        
	        // check supertag
	        // TODO: think about earlier checks for efficiency, for grammars where macros and preds don't matter
	        //Double lexprob = null; // nb: skipping lex log probs, don't seem to be helpful
	        if (supertags != null) {
	        	// skip if not found
	        	String stag = cat.getSupertag();
	        	if (!supertags.containsKey(stag)) return;
	        	// otherwise update found supertags
	        	supertagsFound.add(stag);
	        	// get lex prob
	        	//lexprob = supertags.get(stag);
	        }
	        
	        // propagate types of nom vars
	        propagateTypes(cat);
	        
	        // handle distrib attrs and inherits-from
	        propagateDistributiveAttrs(cat);
	        expandInheritsFrom(cat);
	        
	        // merge stem, pos, sem class from morph item, plus supertag from cat
	        Word word = Word.createFullWord(w, mi.getWord(), cat.getSupertag());

	        // set origin and lexprob
	        Sign sign = new Sign(word, cat);
	        sign.setOrigin();
	        //if (lexprob != null) {
	        //	sign.addData(new SupertaggerAdapter.LexLogProb((float) Math.log10(lexprob)));
	        //}
	        // return sign
	        result.insert(sign);
        }
        catch (RuntimeException exc) {
        	System.err.println(
        			"Warning: ignoring entry: " + item.getName() + " of family: " + item.getFamilyName() + 
        			" for stem: " + stem + " b/c: " + exc.toString()
        	);
        }
    }

    // the sem class for defaultNomvarSetter
    private SimpleType SEMCLASS = null;
    
    // unify sem class with default nom var(s)
    private void unifySemClass(Category cat, String semClass) {
        if (semClass == null || cat.getLF() == null) return;
        SEMCLASS = grammar.types.getSimpleType(semClass);
        try {
            cat.getLF().deepMap(defaultNomvarUnifier);
        } catch (TypePropagationException tpe) {
        	if (debugSemClasses) {
	            System.err.println(
	                "Warning: unable to unify types '" + tpe.st1 + "' and '" + tpe.st2 + 
	                "' in unifying sem class in cat: \n" + cat
	            );
        	}
        }
    }
    
    // mod function to unify type of nom var for DEFAULT_VAL with SEMCLASS
    private ModFcn defaultNomvarUnifier = new ModFcn() {
        public void modify(Mutable m) {
            if (!(m instanceof SatOp)) return;
            SatOp satop = (SatOp) m;
            if (!(satop.getArg() instanceof Proposition)) return; 
            Proposition prop = (Proposition) satop.getArg();
            if (!prop.getName().equals(DEFAULT_VAL)) return;
            if (!(satop.getNominal() instanceof NominalVar)) return;
            NominalVar nv = (NominalVar) satop.getNominal();
            SimpleType st = nv.getType();
            // check equality
            if (st.equals(SEMCLASS)) return;
            // otherwise unify types, update nv
            try {
                SimpleType stU = (SimpleType) st.unify(SEMCLASS, null);
                nv.setType(stU);
            } catch (UnifyFailure uf) {
                throw new TypePropagationException(st, SEMCLASS);
            }
        }
    };

    // the replacement string for defaultReplacer
    private String REPLACEMENT = "";
    
    // mod function to replace DEFAULT_VAL with REPLACEMENT
    private ModFcn defaultReplacer = new ModFcn() {
        public void modify(Mutable m) {
            if (m instanceof Proposition) {
                Proposition prop = (Proposition) m; 
                if (prop.getName().equals(DEFAULT_VAL)) prop.setAtomName(REPLACEMENT);
            }
            else if (m instanceof FeatureStructure) {
                FeatureStructure fs = (FeatureStructure) m;
                for (Iterator<String> it = fs.getAttributes().iterator(); it.hasNext(); ) {
                    String attr = it.next();
                    Object val = fs.getValue(attr);
                    if (val instanceof SimpleType && 
                        ((SimpleType)val).getName().equals(DEFAULT_VAL))
                    {
                        fs.setFeature(attr, grammar.types.getSimpleType(REPLACEMENT));
                    }
                }
            }
        }
    };


    // a cache for macro adders
    private Map<MorphItem, MacroAdder> macAdderMap = new HashMap<MorphItem, MacroAdder>();
    
    // returns a macro adder for the given morph item
    private MacroAdder getMacAdder(MorphItem mi) {
        
        // check map
        MacroAdder retval = macAdderMap.get(mi);
        if (retval != null) return retval;
        
        // set up macro adder
        IntHashSetMap macrosFromLex = new IntHashSetMap();
        String[] newMacroNames = mi.getMacros();
        List<MacroItem> macroItems = new ArrayList<MacroItem>();
        for (int i=0; i < newMacroNames.length; i++) {
            Set<FeatureStructure> featStrucs = (Set<FeatureStructure>)_macros.get(newMacroNames[i]);
            if (featStrucs != null) {
                for (Iterator<FeatureStructure> fsIt = featStrucs.iterator(); fsIt.hasNext();) {
                    FeatureStructure fs = fsIt.next();
                    macrosFromLex.put(fs.getIndex(), fs);
                }
            }
            MacroItem macroItem = _macroItems.get(newMacroNames[i]);
            if (macroItem != null) { macroItems.add(macroItem); }
            else { 
                // should be checked earlier too
                System.err.println("Warning: macro " + newMacroNames[i] + 
                    " not found for word '" + mi.getWord() + "'");
            }
        }
        retval = new MacroAdder(macrosFromLex, macroItems);
        
        // update map and return
        macAdderMap.put(mi, retval);
        return retval; 
    }
        
    
    //
    // type propagation
    //

    /** Propagates types of nomvars in the given category. */
    public void propagateTypes(Category cat) {
        propagateTypes(cat, null);
    }        
    
    /** Propagates types of nomvars in the given categories. */
    public void propagateTypes(Category cat, Category cat2) {
        try {
            nomvarMap.clear();
            cat.deepMap(nomvarTypePropagater);
            if (cat2 != null) cat2.deepMap(nomvarTypePropagater);
            cat.deepMap(nomvarTypePropagater);
            if (cat2 != null) cat2.deepMap(nomvarTypePropagater);
        } catch (TypePropagationException tpe) {
        	if (debugSemClasses) {
	            System.err.println(
	                "Warning: unable to unify types '" + tpe.st1 + "' and '" + tpe.st2 + 
	                "' in cat: \n" + cat
	            );
	            if (cat2 != null) System.err.println("and cat: \n" + cat2);
        	}
        }
    }
        
    // a map from a cat's nomvars to types, 
    // just using the var's name for equality
    @SuppressWarnings("unchecked")
	private Map<NominalVar,SimpleType> nomvarMap = new THashMap(
        new TObjectHashingStrategy() {
			private static final long serialVersionUID = 1L;
			public int computeHashCode(Object o) {
                return ((NominalVar)o).getName().hashCode();
            }
            public boolean equals(Object o1, Object o2) {
                return ((NominalVar)o1).getName().equals(((NominalVar)o2).getName());
            }
        }
    );
    
    // exception for unification failures in propagating types
    private class TypePropagationException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		SimpleType st1; SimpleType st2;
        TypePropagationException(SimpleType st1, SimpleType st2) {
            this.st1 = st1; this.st2 = st2;
        }
    }
    
    // mod function to propagate nomvar types; 
    // needs to be called twice after clearing nomvarMap
    private ModFcn nomvarTypePropagater = new ModFcn() {
        public void modify(Mutable m) {
            if (m instanceof NominalVar) {
                NominalVar nv = (NominalVar) m;
                SimpleType st = nv.getType();
                SimpleType st0 = nomvarMap.get(nv);
                // add type to map if no type found
                if (st0 == null) { nomvarMap.put(nv, st); return; }
                // check equality
                if (st.equals(st0)) return;
                // otherwise unify types, update nv and map
                try {
                    SimpleType stU = (SimpleType) st.unify(st0, null);
                    nv.setType(stU);
                    nomvarMap.put(nv, stU);
                } catch (UnifyFailure uf) {
                    throw new TypePropagationException(st, st0);
                }
            }
        }
    };
    

    //
    // distributive attribute propagation
    //

    /**
     * Returns the list of distributive attributes, or null if none.
     */
    public String[] getDistributiveAttrs() { return _distributiveAttrs; }
    
    /**
     * Gathers and propagates the unique values of each 
     * distributive attribute.
     */
    public void propagateDistributiveAttrs(Category cat) {
        propagateDistributiveAttrs(cat, null);
    }
    
    /**
     * Gathers and propagates the unique values of each 
     * distributive attribute.
     */
    public void propagateDistributiveAttrs(Category cat, Category cat2) {
        if (_distributiveAttrs == null) return;
        resetDistrAttrVals();
        cat.forall(gatherDistrAttrVals);
        if (cat2 != null) { cat2.forall(gatherDistrAttrVals); }
        cat.forall(propagateUniqueDistrAttrVals);
        if (cat2 != null) { cat2.forall(propagateUniqueDistrAttrVals); }
    }
    
    // an array of lists, one for each distributive attr    
    @SuppressWarnings("rawtypes")
	private List[] distrAttrVals = null;
    @SuppressWarnings("rawtypes")
	private void resetDistrAttrVals() {
        if (distrAttrVals == null) { 
            distrAttrVals = new List[_distributiveAttrs.length];
            for (int i = 0; i < distrAttrVals.length; i++) {
                distrAttrVals[i] = new ArrayList(3);
            }
            return;
        }
        for (int i = 0; i < distrAttrVals.length; i++) {
            distrAttrVals[i].clear();
        }
    }
    
    // gathers distinct values for each distributive attr
    private CategoryFcn gatherDistrAttrVals = new CategoryFcnAdapter() {
        @SuppressWarnings("unchecked")
		public void forall(Category c) {
            if (!(c instanceof AtomCat)) return;
            FeatureStructure fs = c.getFeatureStructure();
            if (fs == null) return;
            for (int i = 0; i < _distributiveAttrs.length; i++) {
                String attr = _distributiveAttrs[i];
                Object val = fs.getValue(attr);
                if (val != null && !distrAttrVals[i].contains(val)) { 
                    distrAttrVals[i].add(val); 
                }
            }
        }
    };

    // propagates unique values for each distributive attr
    private CategoryFcn propagateUniqueDistrAttrVals = new CategoryFcnAdapter() {
        public void forall(Category c) {
            if (!(c instanceof AtomCat)) return;
            FeatureStructure fs = c.getFeatureStructure();
            if (fs == null) return;
            for (int i = 0; i < _distributiveAttrs.length; i++) {
                if (distrAttrVals[i].size() != 1) continue;
                Object distVal = distrAttrVals[i].get(0);
                String attr = _distributiveAttrs[i];
                Object val = fs.getValue(attr);
                if (val == null) {
                    fs.setFeature(attr, UnifyControl.copy(distVal));
                }
            }
        }
    };

    
    //
    // licensing features
    //

    /**
     * Returns the list of licensing features.
     */
    public LicensingFeature[] getLicensingFeatures() { return _licensingFeatures; }
    
    
    /**
     * Returns the index of the given relation in the relation sort order, 
     * or the index of "*" if the relation is not explicitly listed.
     */
    public Integer getRelationSortIndex(String rel) {
        Integer retval = _relationIndexMap.get(rel);
        if (retval != null) return retval;
        retval = _relationIndexMap.get("*");
        if (retval != null) return retval;
        return new Integer(-1);
    }
    
    
    //
    // access to maps (limited)
    //

    /** Returns whether the given rel (semantic feature, really) is one used to signal coarticulation. */
    public boolean isCoartRel(String rel) {
        return _coartRelsToPreds.containsKey(rel);
    }
    
    
    //
    // classes for caching lex lookups during realization
    //

    // a class for caching lookups of signs from rels
    // nb: equality is checked just on the rel, to check for a cached lookup
    private static class RelLookup {
        String rel; Collection<Sign> signs;
        RelLookup(String s) { rel = s; }
        public int hashCode() { return rel.hashCode(); }
        public boolean equals(Object obj) { 
            return (obj instanceof RelLookup) && rel.equals(((RelLookup)obj).rel);
        }
    }
    
    // a class for caching lookups of signs from preds and coart rels
    // nb: equality is checked just on the pred and coart rels, to check for a cached lookup
    private static class PredLookup {
        String pred; List<String> coartRels; Collection<Sign> signs;
        PredLookup(String s, List<String> l) { pred = s; coartRels = l; }
        public int hashCode() { 
            return pred.hashCode() + ((coartRels != null) ? coartRels.hashCode() : 0); 
        }
        public boolean equals(Object obj) { 
            if (!(obj instanceof PredLookup)) return false;
            PredLookup pLook = (PredLookup) obj;
            if (!pred.equals(pLook.pred)) return false;
            if (coartRels == null) return (pLook.coartRels == null);
            return coartRels.equals(pLook.coartRels);
        }
    }

    
    //
    // XML loading routines
    //
    
	private class MorphScanner extends XmlScanner {
    	List<MorphItem> morphItems = new ArrayList<MorphItem>();
    	List<MacroItem> macroItems = new ArrayList<MacroItem>();
    	public void handleElement(Element e) {
            // create morph item
			if (e.getName().equals("entry")) {
                try { morphItems.add(new MorphItem(e)); }
                catch (RuntimeException exc) {
                    System.err.println("Skipping morph item: " + e.getAttributeValue("word"));
                    System.err.println(exc.toString());
                }
            }
            // create macro item
			else if (e.getName().equals("macro")) {
                try { macroItems.add(new MacroItem(e)); }
                catch (RuntimeException exc) {
                    System.err.println("Skipping macro item: " + e.getAttributeValue("name"));
                    System.err.println(exc.toString());
                }
            }
		}
	};
	
    private Pair<List<MorphItem>,List<MacroItem>> getMorph(URL url) throws IOException {
    	// scan XML
    	MorphScanner morphScanner = new MorphScanner();
    	morphScanner.parse(url);
        // return morph and macro items
        return new Pair<List<MorphItem>,List<MacroItem>>(morphScanner.morphItems, morphScanner.macroItems);
    }
    
	private class LexiconScanner extends XmlScanner {
    	List<Family> lexicon = new ArrayList<Family>();
    	Element distrElt = null;
    	Element licensingElt = null;
    	Element relationSortingElt = null;
    	public void handleElement(Element e) {
            // create family
			if (e.getName().equals("family")) {
                try { lexicon.add(new Family(e)); }
                catch (RuntimeException exc) {
                    System.err.println("Skipping family: " + e.getAttributeValue("name"));
                    System.err.println(exc.toString());
                }
            }
            // save distributive attributes
			else if (e.getName().equals("distributive-features")) distrElt = e; 
            // save licensing features
			else if (e.getName().equals("licensing-features")) licensingElt = e; 
            // save relation sort order
			else if (e.getName().equals("relation-sorting")) relationSortingElt = e; 
		}
	};
	
    private List<Family> getLexicon(URL url) throws IOException {
    	// scan XML, creating families
    	LexiconScanner lexiconScanner = new LexiconScanner();
    	lexiconScanner.parse(url);
        // get distributive attributes, if any
        if (lexiconScanner.distrElt != null) {
            String distrAttrs = lexiconScanner.distrElt.getAttributeValue("attrs");
            _distributiveAttrs = distrAttrs.split("\\s+");
        }
        // load licensing features
        loadLicensingFeatures(lexiconScanner.licensingElt);
        // load relation sort order
        loadRelationSortOrder(lexiconScanner.relationSortingElt);
        // return families
        return lexiconScanner.lexicon;
    }
    
    // get licensing features, with appropriate defaults
    @SuppressWarnings("unchecked")
	private void loadLicensingFeatures(Element licensingElt) {
        List<LicensingFeature> licensingFeats = new ArrayList<LicensingFeature>();
        boolean containsLexFeat = false;
        if (licensingElt != null) {
            for (Iterator<Element> it = licensingElt.getChildren("feat").iterator(); it.hasNext(); ) {
                Element featElt = it.next();
                String attr = featElt.getAttributeValue("attr");
                if (attr.equals("lex")) containsLexFeat = true;
                String val = featElt.getAttributeValue("val");
                List<String> alsoLicensedBy = null;
                String alsoVals = featElt.getAttributeValue("also-licensed-by");
                if (alsoVals != null) {
                    alsoLicensedBy = Arrays.asList(alsoVals.split("\\s+"));
                }
                boolean licenseEmptyCats = true;
                boolean licenseMarkedCats = false;
                boolean instantiate = true; 
                byte loc = LicensingFeature.BOTH;
                String lmc = featElt.getAttributeValue("license-marked-cats");
                if (lmc != null) {
                    licenseMarkedCats = Boolean.valueOf(lmc).booleanValue();
                    // change defaults
                    licenseEmptyCats = false;
                    loc = LicensingFeature.TARGET_ONLY;
                    instantiate = false;
                }
                String lec = featElt.getAttributeValue("license-empty-cats");
                if (lec != null) {
                    licenseEmptyCats = Boolean.valueOf(lec).booleanValue();
                }
                String inst = featElt.getAttributeValue("instantiate");
                if (inst != null) {
                    instantiate = Boolean.valueOf(inst).booleanValue();
                }
                String locStr = featElt.getAttributeValue("location");
                if (locStr != null) {
                    if (locStr.equals("target-only")) loc = LicensingFeature.TARGET_ONLY;
                    if (locStr.equals("args-only")) loc = LicensingFeature.ARGS_ONLY;
                    if (locStr.equals("both")) loc = LicensingFeature.BOTH;
                }
                licensingFeats.add(
                    new LicensingFeature(
                        attr, val, alsoLicensedBy, 
                        licenseEmptyCats, licenseMarkedCats, instantiate, 
                        loc
                    )
                );
            }
        }
        if (!containsLexFeat) {
            licensingFeats.add(LicensingFeature.defaultLexFeature);
        }
        _licensingFeatures = new LicensingFeature[licensingFeats.size()];
        licensingFeats.toArray(_licensingFeatures);
    }
    
    
    // default relation sort order
    private static String[] defaultRelationSortOrder = {
        "BoundVar", "PairedWith", 
        "Restr", "Body", "Scope", 
        "*", 
        "GenRel", "Coord", "Append"
    };
    
    // get relation sort order, or use defaults
    private void loadRelationSortOrder(Element relationSortingElt) {
        // use defaults if no order specified
        if (relationSortingElt == null) {
            for (int i = 0; i < defaultRelationSortOrder.length; i++) {
                _relationIndexMap.put(defaultRelationSortOrder[i], new Integer(i));
            }
            return;
        }
        // otherwise load from 'order' attribute
        String orderAttr = relationSortingElt.getAttributeValue("order");
        String[] relSortOrder = orderAttr.split("\\s+");
        for (int i = 0; i < relSortOrder.length; i++) {
            _relationIndexMap.put(relSortOrder[i], new Integer(i));
        }
    }
		/*
		 * Accessor for words map
		 */
    public GroupMap<Word,MorphItem> getWords() {
			return _words;
		}

    public GroupMap<String,FeatureStructure> getMacros(){
        return _macros;
    }
}
