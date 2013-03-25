///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004-5 University of Edinburgh (Michael White)
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

import opennlp.ccg.util.*;

import java.util.*;

/**
 * A FullWord object is a word with all possible fields. 
 * The factory methods return interned objects.
 *
 * @author      Michael White
 * @version     $Revision: 1.6 $, $Date: 2009/07/17 04:23:30 $
 */
public class FullWord extends WordWithPitchAccent {
    
	private static final long serialVersionUID = -3115687437782457735L;

	/** List of attribute-value pairs, which must be strings. */
    protected List<Pair<String,String>> attrValPairs;
    
    /** The stem. */
    protected String stem;
    
    /** The part of speech. */
    protected String POS;
    
    /** The supertag. */
    protected String supertag;
    
    /** The semantic class (optional). */
    protected String semClass;

    
    /** Returns the list of extra attribute-value pairs. */
    protected List<Pair<String,String>> getAttrValPairsList() { return attrValPairs; }
    
    /** Returns the stem. */
    public String getStem() { return stem; }
    
    /** Returns the part of speech. */
    public String getPOS() { return POS; }
    
    /** Returns the supertag. */
    public String getSupertag() { return supertag; }
    
    /** Returns the semantic class (may be null). */
    public String getSemClass() { return semClass; }

    
    /** Constructor for full word. */
    protected FullWord(
        String form, String pitchAccent, List<Pair<String,String>> attrValPairs, 
        String stem, String POS, String supertag, String semClass 
    ) {
        super(form, pitchAccent);
        this.attrValPairs = attrValPairs; 
        this.stem = stem; this.POS = POS; this.supertag = supertag; this.semClass = semClass; 
    }
    
    /** Factory. */
    public static class Factory implements WordFactory {

        // reusable word, for looking up already interned ones
        private FullWord w = new FullWord(null, null, null, null, null, null, null);
    
        // sets the form and factors of the reusable word w 
        private void setW(
            String form, String pitchAccent, List<Pair<String,String>> attrValPairs, 
            String stem, String POS, String supertag, String semClass 
        ) {
            w.form = form; w.pitchAccent = pitchAccent;
            w.attrValPairs = attrValPairs;
            w.stem = stem; w.POS = POS; w.supertag = supertag; w.semClass = semClass;
        }
        
        // looks up the word equivalent to w, or if none, returns a new one based on it
        private Word getOrCreateFromW() {
            Word retval = (Word) Interner.getGlobalInterned(w);
            if (retval != null) return retval;
            if (w.isSurfaceWord() && w.attrValPairs == null) {
                if (w.pitchAccent == null) retval = new SimpleWord(w.form);
                else retval = new WordWithPitchAccent(w.form, w.pitchAccent);
            }
            else retval = new FullWord(w.form, w.pitchAccent, w.attrValPairs, w.stem, w.POS, w.supertag, w.semClass);
            return (Word) Interner.globalIntern(retval);
        }
        
        /** Creates a surface word with the given interned form. */
        public synchronized Word create(String form) {
            return create(form, null, null, null, null, null, null);
        }
        
        /** Creates a (surface or full) word with the given normalized attribute name and value.
            The attribute names Tokenizer.WORD_ATTR, ..., Tokenizer.SEM_CLASS_ATTR 
            may be used for the form, ..., semantic class. */
        public synchronized Word create(String attr, String val) {
            String form = null; String pitchAccent = null;
            List<Pair<String,String>> attrValPairs = null; 
            String stem = null; String POS = null; String supertag = null; String semClass = null;
            if (attr == Tokenizer.WORD_ATTR) form = val;
            else if (attr == Tokenizer.PITCH_ACCENT_ATTR) pitchAccent = val;
            else if (attr == Tokenizer.STEM_ATTR) stem = val;
            else if (attr == Tokenizer.POS_ATTR) POS = val;
            else if (attr == Tokenizer.SUPERTAG_ATTR) supertag = val;
            else if (attr == Tokenizer.SEM_CLASS_ATTR) semClass = val;
            else {
                attrValPairs = new ArrayList<Pair<String,String>>(1);
                attrValPairs.add(new Pair<String,String>(attr, val));
            }
            return create(form, pitchAccent, attrValPairs, stem, POS, supertag, semClass);
        }
        
        /** Creates a (surface or full) word from the given canonical factors. */
        public synchronized Word create(
            String form, String pitchAccent, List<Pair<String,String>> attrValPairs, 
            String stem, String POS, String supertag, String semClass 
        ) {
            setW(form, pitchAccent, attrValPairs, stem, POS, supertag, semClass);
            return getOrCreateFromW();
        }
    }
}

