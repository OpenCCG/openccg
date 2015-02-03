package opennlp.ccg.lexicon;

import java.util.ArrayList;
import java.util.List;

import opennlp.ccg.util.Interner;
import opennlp.ccg.util.Pair;

/**
 * A factory of "full words".
 * 
 * @author Daniel Couto-Vale
 */
public class FullWordFactory implements WordFactory {

	// reusable word, for looking up already interned ones
	private FullWord w = new FullWord(null, null, null, null, null, null, null);

	// sets the form and factors of the reusable word w
	private void setW(String form, String pitchAccent, List<Pair<String, String>> attrValPairs,
			String stem, String POS, String supertag, String semClass) {
		w.form = form;
		w.pitchAccent = pitchAccent;
		w.attrValPairs = attrValPairs;
		w.stem = stem;
		w.POS = POS;
		w.supertag = supertag;
		w.semClass = semClass;
	}

	// looks up the word equivalent to w, or if none, returns a new one
	// based on it
	private Word getOrCreateFromW() {
		Word retval = (Word) Interner.getGlobalInterned(w);
		if (retval != null)
			return retval;
		if (w.isSurfaceWord() && w.attrValPairs == null) {
			if (w.pitchAccent == null)
				retval = new SimpleWord(w.form);
			else
				retval = new WordWithPitchAccent(w.form, w.pitchAccent);
		} else
			retval = new FullWord(w.form, w.pitchAccent, w.attrValPairs, w.stem, w.POS, w.supertag,
					w.semClass);
		return (Word) Interner.globalIntern(retval);
	}

	/** Creates a surface word with the given interned form. */
	public synchronized Word create(String form) {
		return create(form, null, null, null, null, null, null);
	}

	/**
	 * Creates a (surface or full) word with the given normalized attribute name
	 * and value. The attribute names Tokenizer.WORD_ATTR, ...,
	 * Tokenizer.SEM_CLASS_ATTR may be used for the form, ..., semantic class.
	 */
	public synchronized Word create(String attributeName, String attributeValue) {
		String form = null;
		String pitchAccent = null;
		List<Pair<String, String>> attrValPairs = null;
		String stem = null;
		String POS = null;
		String supertag = null;
		String semClass = null;
		if (attributeName == Tokenizer.WORD_ASSOCIATE) {
			form = attributeValue;
		} else if (attributeName == Tokenizer.TONE_ASSOCIATE) {
			pitchAccent = attributeValue;
		} else if (attributeName == Tokenizer.TERM_ASSOCIATE) {
			stem = attributeValue;
		} else if (attributeName == Tokenizer.FUNCTIONS_ASSOCIATE) {
			POS = attributeValue;
		} else if (attributeName == Tokenizer.SUPERTAG_ASSOCIATE) {
			supertag = attributeValue;
		} else if (attributeName == Tokenizer.ENTITY_CLASS_ASSOCIATE) {
			semClass = attributeValue;
		} else {
			attrValPairs = new ArrayList<Pair<String, String>>(1);
			attrValPairs.add(new Pair<String, String>(attributeName, attributeValue));
		}
		return create(form, pitchAccent, attrValPairs, stem, POS, supertag, semClass);
	}

	/** Creates a (surface or full) word from the given canonical factors. */
	public synchronized Word create(String form, String pitchAccent,
			List<Pair<String, String>> attrValPairs, String stem, String POS, String supertag,
			String semClass) {
		setW(form, pitchAccent, attrValPairs, stem, POS, supertag, semClass);
		return getOrCreateFromW();
	}
}
