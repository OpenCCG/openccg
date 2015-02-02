///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-9 Jason Baldridge, University of Edinburgh and Michael White
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

import opennlp.ccg.parse.*;
import opennlp.ccg.util.*;
import opennlp.ccg.lexicon.*;
import opennlp.ccg.grammar.*;
import opennlp.ccg.hylo.*;

import org.jdom.*;

import gnu.trove.*;

import java.io.*;
import java.util.*;

/**
 * A CCG sign, consisting of a list of words paired with a category.
 * Signs may contain arbitrary data objects which are ignored in equality checking.
 * Non-serializable data objects are filtered during serialization.
 *
 * @author Jason Baldridge
 * @author Michael White
 * @author Daniel Couto-Vale
 * @version $Revision: 1.44 $, $Date: 2011/08/27 19:27:01 $
 */
public class Sign implements LexSemOrigin, Serializable {

	private static final long serialVersionUID = 1072712272514007274L;

	/**
	 * The words
	 */
    private List<Word> words;

    /**
     * The grammatical category
     */
    private final Category category;

    /**
     * The derivation history
     */
    private DerivationHistory history;

    /**
     * The lexical head
     */
    private Sign lexHead;

    /**
     * List of transient data objects, for retrieval by class.
     */
    private LinkedList<Object> data = null;

    /**
     * Constructor
     * 
     * @param words the words
     * @param category the category
     * @param history the history
     * @param lexHead the lex head
     */
	@SuppressWarnings("unchecked")
	private Sign(List<Word> words, Category category, DerivationHistory history, Sign lexHead) {
		this.words = (List<Word>) Interner.globalIntern(words); 
        this.category = category;
        this.history = history != null ? history : new DerivationHistory(this);
        this.lexHead = lexHead != null ? lexHead : this;
    }

	/**
	 * Constructor (no history)
	 * 
	 * @param words the words
	 * @param category the category
	 */
    public Sign(List<Word> words, Category category) {
        this(words, category, null, null);
    }

    /**
     * Constructor with words and derivation history formed from the given inputs, rule and lex head.
     */
    private Sign(Category category, Sign[] inputs, Rule rule, Sign lexHead) {
        this(getRemainingWords(inputs, 0), category, null, lexHead);
        this.history = new DerivationHistory(inputs, this, rule);
    }

    // during deserialization, interns words
    @SuppressWarnings("unchecked")
	private final void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    	in.defaultReadObject();
        words = (List<Word>) Interner.globalIntern(words); 
    }
    
    // during serialization, skips non-serializable data objects
    private final void writeObject(java.io.ObjectOutputStream stream) throws IOException {
    	// save old data objects
    	LinkedList<Object> tmp = data;
    	// filter non-serializable ones
    	if (tmp != null) {
    		data = new LinkedList<Object>();
    		for (Object obj : tmp) {
    			if (obj instanceof Serializable) data.add(obj);
    		}
    		if (data.isEmpty()) data = null;
    	}
    	// serialize
    	stream.defaultWriteObject();
    	// restore old data objects
    	data = tmp;
    }

    /** Factory method for creating a sign from a lexical sign plus a coarticulation one. */
    public final static Sign createCoartSign(Category cat, Sign lexSign, Sign coartSign) {
        List<Word> words = lexSign.getWords();
        if (words.size() > 1) 
            throw new RuntimeException("Can't create coarticulation sign from multiple words.");
        Word word = words.get(0);
        Word coartWord = coartSign.getWords().get(0);
        Word wordPlus = Word.createWordWithAttrs(word, coartWord);
        Rule coartRule = new Rule() {
            public String name() { return "coart"; }
            public int arity() { return 1; }
            public List<Category> applyRule(Category[] inputs) { throw new RuntimeException("Not supported."); }
            public RuleGroup getRuleGroup() { throw new RuntimeException("Not supported."); }
            public void setRuleGroup(RuleGroup ruleGroup) { throw new RuntimeException("Not supported."); }
            public Element toXml() { throw new RuntimeException("Not supported."); }
        };
        Sign retval = new Sign(new SingletonList<Word>(wordPlus), cat, null, null);
        DerivationHistory history = new DerivationHistory(new Sign[]{lexSign,coartSign}, retval, coartRule);
        retval.history = history;
        return retval;
    }

    /** Factory method for creating derived signs with the given cat from the given inputs, rule and lex head. */
    public final static Sign createDerivedSign(Category cat, Sign[] inputs, Rule rule, Sign lexHead) {
        return new Sign(cat, inputs, rule, lexHead);
    }

    /** Factory method for creating derived signs from the given result cat, inputs, rule and lex head, 
        with a new LF constructed from the inputs.
        Note that unlike with rule applications, the result LF is constructed with 
        no var substitutions, so it is useful only for creating alternative signs during realization. */
    public final static Sign createDerivedSignWithNewLF(Category cat, Sign[] inputs, Rule rule, Sign lexHead) {
        Category copyCat = cat.shallowCopy();
        LF lf = null;
        for (int i = 0; i < inputs.length; i++) {
            lf = HyloHelper.append(lf, inputs[i].getCategory().getLF());
        }
        if (rule instanceof TypeChangingRule) {
            TypeChangingRule tcr = (TypeChangingRule) rule;
            lf = HyloHelper.append(lf, tcr.getResult().getLF());
        }
        if (lf != null) { HyloHelper.sort(lf); }
        copyCat.setLF(lf);
        return new Sign(copyCat, inputs, rule, lexHead);
    }
        

    
    // returns the remaining words in a structure sharing way
    private final static List<Word> getRemainingWords(Sign[] inputs, int index) {
        // if (inputs.length == 0) throw new RuntimeException("Error: can't make sign from zero inputs");
        if (index == (inputs.length - 1)) return inputs[index].words;
        return new StructureSharingList<Word>(
            inputs[index].words,
            getRemainingWords(inputs, index+1)
        );
    }
    
    /** Returns the words of the sign. */
    public final List<Word> getWords() {
        return words;
    }

    /** Returns the words as a string.  Delegates to the current tokenizer's getOrthography method. */
    public final String getOrthography() {
        return Grammar.theGrammar.lexicon.tokenizer.getOrthography(words);
    }

    /** Returns the sign's category. */
    public final Category getCategory() {
        return category;
    }

    /** Returns whether the sign is lexical. */
    public final boolean isLexical() { return history.isEmpty(); }
    
    /** Sets the derivation history. */
    public final void setDerivationHistory(DerivationHistory dh) {
        history = dh;
    }
    
    /** Returns the derivation history. */
    public final DerivationHistory getDerivationHistory() {
        return history;
    }

    /** Returns the lexical head. */
    public final Sign getLexHead() { return lexHead; }

    /** Returns a hash code for this sign. */ 
    public final int hashCode() {
        return System.identityHashCode(words) + category.hashCode();
    }

    /** Returns whether this sign equals the given object. */
    public final boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Sign)) return false;
        Sign sign = (Sign) obj;
        return words == sign.words && category.equals(sign.category);
    }

    
    /** 
     * Returns a hash code for this sign with the words restricted to surface words; 
     * with lexical signs, however, the original hash code is returned, so that 
     * words with signs that differ just in their pos tags can be distinguished 
     * (for robustness).
     */ 
    public final int surfaceWordHashCode() {
    	return surfaceWordHashCode(false);
    }
    
    /** 
     * Returns a hash code for this sign with the words restricted to surface words, 
     * and with the LF ignored according to the given flag; 
     * with lexical signs, however, the original hash code is returned, so that 
     * words with signs that differ just in their pos tags can be distinguished 
     * (for robustness).
     */ 
    public final int surfaceWordHashCode(boolean ignoreLF) {
        // original hash code for lex signs
        if (history.getInputs() == null) return hashCode();
        // otherwise use surface words
        int hc = 1;
        for (int i = 0; i < words.size(); i++) {
            Word word = words.get(i);
            hc = 31*hc + word.surfaceWordHashCode();
        }
        hc += (ignoreLF) ? category.hashCodeNoLF() : category.hashCode();
        return hc;
    }
    
    /** 
     * Returns whether this sign and the given object have equal categories and
     * restrictions to surface words; 
     * with lexical signs, however, the original equals result is returned, so that 
     * words with signs that differ just in their pos tags can be distinguished 
     * (for robustness).
     */
    public final boolean surfaceWordEquals(Object obj) {
    	return surfaceWordEquals(obj, false);
    }

    /** 
     * Returns whether this sign and the given object have equal categories and
     * restrictions to surface words,  
     * with the LF ignored according to the given flag; 
     * with lexical signs, however, the original equals result is returned, so that 
     * words with signs that differ just in their pos tags can be distinguished 
     * (for robustness).
     */
    public final boolean surfaceWordEquals(Object obj, boolean ignoreLF) {
        if (obj == this) return true;
        if (!(obj instanceof Sign)) return false;
        Sign sign = (Sign) obj;
        // original equals for lex signs
        if (history.getInputs() == null || sign.history.getInputs() == null) 
            return equals(sign);
        // otherwise use surface words
        if (words.size() != sign.words.size()) return false;
        for (int i = 0; i < words.size(); i++) {
            Word word = words.get(i); 
            Word signWord = (Word) sign.words.get(i);
            if (!word.surfaceWordEquals(signWord)) return false;
        }
        return (ignoreLF) ? category.equalsNoLF(sign.category) : category.equals(sign.category);
    }

    
    /** Returns 'orthography :- category'. */
    public final String toString() {
        return getOrthography() + " :- " + category.toString(); // for lex head: + " --> " + _lexHead.getWordForm();
    }
 
    
    /** 
     * Returns the words in an XML doc, with no labeled spans for nominals. 
     */
    public final Document getWordsInXml() {
    	Set<Nominal> emptySet = Collections.emptySet();
    	return getWordsInXml(emptySet); 
    }
    
    /** 
     * Returns the words in an XML doc, with labeled spans for the given nominals, 
     * and with pitch accents and boundary tones converted to elements. 
     * Each orthographic word appears in a separate element, 
     * with multiwords grouped under a multiword element.
     * Attribute-value pairs for the word (if any) appear on the word 
     * or multiword element.
     * Words are also expanded using the grammar's tokenizer.
     */
    public final Document getWordsInXml(Set<Nominal> nominals) {
        TObjectIntHashMap nominalsMap = new TObjectIntHashMap(); 
        setMaxOrthLengths(nominals, nominalsMap);
        Document doc = new Document();
        Element root = new Element("seg");
        doc.setRootElement(root);
        addWordsToXml(root, nominalsMap);
        return doc;
    }
    
    // finds the maximum orthography lengths for signs headed by the given nominals
    private final void setMaxOrthLengths(Set<Nominal> nominals, TObjectIntHashMap nominalsMap) {
        // update map
        Nominal index = category.getIndexNominal();
        if (index != null && nominals.contains(index)) {
            int orthLen = getOrthography().length();
            if (!nominalsMap.containsKey(index) || orthLen > nominalsMap.get(index)) {
                nominalsMap.put(index, orthLen);
            }
        }
        // recurse
        Sign[] inputs = history.getInputs();
        if (inputs == null) return;
        for (int i = 0; i < inputs.length; i++) {
            inputs[i].setMaxOrthLengths(nominals, nominalsMap); 
        }
    }
    
    // recursively adds orthographic words as XML to the given parent, 
    // using the nominals map to determine labeled spans
    private final void addWordsToXml(Element parent, TObjectIntHashMap nominalsMap) {
        // check for matching nominal as index of target cat; 
        // if found, update parent to labeled span element
        Nominal index = category.getIndexNominal();
        if (index != null && nominalsMap.containsKey(index) && 
            nominalsMap.get(index) == getOrthography().length()) 
        {
            // remove index key from map, to avoid duplicate spans with the same length
            nominalsMap.remove(index);
            // make span element, update parent
            Element span = new Element("span");
            span.setAttribute("label", index.toString());
            parent.addContent(span);
            parent = span;
        }
        // process inputs from derivation history
        Sign[] inputs = history.getInputs();
        if (inputs == null) {
            // in leaf case, word list must be a singleton
            Word word = words.get(0); 
            // check for boundary tone
            if (Grammar.isBoundaryTone(word.getForm())) {
                // add element for boundary tone
                Element boundary = new Element("boundary");
                boundary.setAttribute("type", word.getForm());
                parent.addContent(boundary);
                return;
            }
            // check for pitch accent
            if (word.getPitchAccent() != null) {
                // add pitchaccent element containing word(s) with corresponding accent
                Element pitchaccent = new Element("pitchaccent");
                pitchaccent.setAttribute("type", word.getPitchAccent());
                addWords(pitchaccent, word);
                parent.addContent(pitchaccent);
                return;
            }
            // otherwise add word(s)
            addWords(parent, word);
            return;
        }
        if (inputs.length == 1) {
            inputs[0].addWordsToXml(parent, nominalsMap);
            return;
        }
        for (int i = 0; i < inputs.length; i++) {
            inputs[i].addWordsToXml(parent, nominalsMap);
        }
    }
    
    // adds one or more word elements after expanding surface form; 
    // multiwords are enclosed within a multiword element; 
    // any attribute-value pairs are added to the word or multiword element
    private final void addWords(Element parent, Word word) {
        List<String> orthWords = Grammar.theGrammar.lexicon.tokenizer.expandWord(word);
        Element child;
        if (orthWords.size() == 1) {
            Element wordElt = new Element("word");
            wordElt.addContent(orthWords.get(0));
            child = wordElt;
        }
        else {
            Element multiwordElt = new Element("multiword");
            for (int i = 0; i < orthWords.size(); i++) {
                Element wordElt = new Element("word");
                wordElt.addContent(orthWords.get(i));
                multiwordElt.addContent(wordElt);
            }
            child = multiwordElt;
        }
        for (Iterator<Pair<String,String>> it = word.getAttrValPairs(); it.hasNext(); ) {
            Pair<String,String> p = it.next();
            String attr = p.a; String val = p.b;
            child.setAttribute(attr, val);
        }
        parent.addContent(child);
    }
    

    /**
     * Returns a string showing the bracketings implied by the derivation.
     * See DerivationHistory.toString to see the complete derivation in 
     * vertical list form.
     */
    public final String getBracketedString() {
        Sign[] inputs = history.getInputs();
        if (inputs == null) return getOrthography();
        if (inputs.length == 1) return inputs[0].getBracketedString();
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        for (int i = 0; i < inputs.length; i++) {
            sb.append(inputs[i].getBracketedString());
            if (i < (inputs.length - 1)) sb.append(" ");
        }
        sb.append(")");
        return sb.toString();
    }
    
    /**
     * Returns the category's supertag.
     */
    public final String getSupertag() { return category.getSupertag(); }
    
    /**
     * Returns the word form of the first word. 
     */
    public final String getWordForm() { return words.get(0).getForm(); }
    
    /**
     * Returns the POS tag of the first word.
     */
    public final String getPOS() { return words.get(0).getPOS(); }
    
    /**
     * Sets the origin of the elementary predications.
     */
    public final void setOrigin() { HyloHelper.setOrigin(category.getLF(), this); }
    
    /**
     * Returns the index of the first word of the given lex sign in this sign's 
     * list of words, or -1 if the given lex sign is not in this sign's derivation 
     * history.
     */
    public final int wordIndex(Sign lexSign) {
    	return wordIndex(lexSign, new int[]{0});
    }
    
    // returns word index relative to input offset
    private final int wordIndex(Sign lexSign, int[] offset) {
    	if (this == lexSign) return offset[0];
    	if (isLexical()) {
    		offset[0] += words.size();
    		return -1;
    	}
        Sign[] inputs = history.getInputs();
        for (int i = 0; i < inputs.length; i++) {
        	int retval = inputs[i].wordIndex(lexSign, offset);
        	if (retval >= 0) return retval;
        }
        return -1;
    }
    
    
    /** Adds a data object to the front of the list of data objects. */
    public final void addData(Object obj) {
    	if (data == null) data = new LinkedList<Object>();
    	data.addFirst(obj);
    }
    
    /** Returns the first data object with the given class, or null if none. */
    public final Object getData(Class<?> objClass) {
    	if (data == null) return null;
    	for (Object obj : data) {
    		if (obj.getClass() == objClass) return obj;
    	}
    	return null;
    }

    
	/** Unfilled dependencies wrapper, for unique retrieval from data objects. */
	public final static class UnfilledDeps {
		public List<LexDependency> unfilledDeps; 
		public UnfilledDeps(List<LexDependency> unfilledDeps) { this.unfilledDeps = unfilledDeps; }
	}
	
	/** Filled dependencies wrapper, for unique retrieval from data objects. */
	public final static class FilledDeps {
		public List<LexDependency> filledDeps; 
		public FilledDeps(List<LexDependency> filledDeps) { this.filledDeps = filledDeps; }
	}
	
	/** Returns the unfilled dependencies for this sign, with caching. */
	public final List<LexDependency> getUnfilledDeps() {
		// check cache
		UnfilledDeps udeps = (UnfilledDeps) getData(UnfilledDeps.class);
		if (udeps != null) return udeps.unfilledDeps;
		// lex case: calculate, store and return
		if (isLexical()) {
			List<LexDependency> unfilledDeps = HyloHelper.getUnfilledLexDeps(category.getLF());
			addData(new UnfilledDeps(unfilledDeps));
			return unfilledDeps;
		}
		// otherwise compute filled deps, with unfilled determined as a side effect, and return cached result
		getFilledDeps();
		udeps = (UnfilledDeps) getData(UnfilledDeps.class);
		return udeps.unfilledDeps;
	}
    
	/** Returns the filled dependencies for this sign, with caching. */
	public final List<LexDependency> getFilledDeps() {
		// skip lex case
		if (isLexical()) return Collections.emptyList();
		// check cache
		FilledDeps fdeps = (FilledDeps) getData(FilledDeps.class);
		if (fdeps != null) return fdeps.filledDeps;
		// otherwise get unfilled deps from children recursively
		List<LexDependency> unfilledDeps = new ArrayList<LexDependency>(5);
		Sign[] inputs = history.getInputs();
        for (int i = 0; i < inputs.length; i++) {
        	unfilledDeps.addAll(inputs[i].getUnfilledDeps());
        }
        // calculate filled deps
        List<LexDependency> filledDeps = HyloHelper.getFilledLexDeps(unfilledDeps, category.getLF());
        // store filled and unfilled, returning filled
        addData(new UnfilledDeps(unfilledDeps));
        addData(new FilledDeps(filledDeps));
    	return filledDeps;
	}

	/** 
	 * Returns the sibling filled dependencies for this sign by recursively 
	 * filtering the filled dependencies from the input signs for those with 
	 * the same head.
	 */
	public final List<LexDependency> getSiblingFilledDeps() {
		List<LexDependency> filledDeps = getFilledDeps();
		if (filledDeps.isEmpty()) return Collections.emptyList();
		List<LexDependency> retval = new ArrayList<LexDependency>(5);
		Sign[] inputs = history.getInputs();
        for (int i = 0; i < inputs.length; i++) {
        	inputs[i].addSiblingFilledDeps(retval, filledDeps);
        }
		return retval;
	}
	
	// recursively adds sibling filled deps until lex items reached or 
	// sibs with different heads found
	private final void addSiblingFilledDeps(List<LexDependency> retval, List<LexDependency> filledDeps) {
		if (isLexical()) return;
		List<LexDependency> candDeps = getFilledDeps();
		if (!candDeps.isEmpty()) {
			List<LexDependency> sibs = LexDependency.filterSameHead(candDeps, filledDeps);
			if (sibs.isEmpty()) return;
			retval.addAll(sibs);
			
		}
		Sign[] inputs = history.getInputs();
        for (int i = 0; i < inputs.length; i++) {
        	inputs[i].addSiblingFilledDeps(retval, filledDeps);
        }
	}

	/**
	 * Returns the descendant sign headed by the given dependent 
	 * by recursing through the input signs as long as the head remains 
	 * the same as the given head; otherwise returns null.
	 */
	public final Sign getSignHeadedByDep(LexDependency lexDependency) {
		// check same head
		if (!isLexical() && lexHead == lexDependency.lexHead) {
			Sign[] inputs = history.getInputs();
	        for (int i = 0; i < inputs.length; i++) {
	    		// check for match
	        	if (inputs[i].lexHead == lexDependency.lexDep) return inputs[i]; // found it
	        	// otherwise recurse
	        	Sign retval = inputs[i].getSignHeadedByDep(lexDependency);
	        	if (retval != null) return retval;
	        }
		}
		// otherwise not found
		return null;
	}


    /**
     * Tests serialization of simple types, including resolution.
     */
    public final void debugSerialization() throws IOException, ClassNotFoundException {
    	String filename = "tmp.ser";
    	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
    	System.out.println("Writing this: " + this);
    	System.out.println(this.getDerivationHistory());
    	out.writeObject(this);
    	out.close();
    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
    	System.out.print("Reading sign: ");
    	Sign sign = (Sign) in.readObject();
    	System.out.println(sign);
    	System.out.println(sign.getDerivationHistory());
    	in.close();
    	// test identity and equality
    	System.out.println("this == sign?: " + (this == sign));
    	System.out.println("this.equals(sign)?: " + (this.equals(sign)));
    }

}
