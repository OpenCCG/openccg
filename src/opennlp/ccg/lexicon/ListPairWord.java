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

package opennlp.ccg.lexicon;

import java.util.*;

import opennlp.ccg.util.Pair;

/**
 * A ListPairWord represents a word via a list of pairs of interned 
 * attributes and values.  It is intended to be a simple wrapper of 
 * the list to make it act like a word, without requiring canonical 
 * instances. 
 *
 * @author      Michael White
 * @version     $Revision: 1.1 $, $Date: 2010/01/17 04:49:24 $
 */
public class ListPairWord extends Word {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The list of pairs of attributes and values.
	 */
	protected List<Pair<String,String>> pairsList;
	
	/** Constructor. */
	public ListPairWord(List<Pair<String,String>> pairsList) { this.pairsList = pairsList; }
	
    /** Returns the surface form. */
    public String getForm() { return getValFromInterned(Tokenizer.WORD_ATTR); }
    
    /** Returns the pitch accent. */
    public String getPitchAccent() { return getValFromInterned(Tokenizer.PITCH_ACCENT_ATTR); }
    
    /** Returns the list of extra attribute-value pairs. */
    protected List<Pair<String,String>> getAttrValPairsList() { 
        List<Pair<String,String>> retval = null; 
		for (Pair<String,String> pair : pairsList) {
            if (!isKnownAttr(pair.a)) {
                if (retval == null) retval = new ArrayList<Pair<String,String>>(5);
                retval.add(pair);
            }
		}
        return retval; 
    }
    
    /** Returns the stem. */
    public String getStem() { return getValFromInterned(Tokenizer.STEM_ATTR); }
    
    /** Returns the part of speech. */
    public String getPOS() { return getValFromInterned(Tokenizer.POS_ATTR); }
    
    /** Returns the supertag. */
    public String getSupertag() { return getValFromInterned(Tokenizer.SUPERTAG_ATTR); }
    
    /** Returns the semantic class. */
    public String getSemClass() { return getValFromInterned(Tokenizer.SEM_CLASS_ATTR); }


    /** Returns the value of the attribute with the given name, or null if none. 
    The attribute names Tokenizer.WORD_ATTR, ..., Tokenizer.SEM_CLASS_ATTR 
    may be used to retrieve the form, ..., semantic class. */
	public String getVal(String attr) {
		String internedAttr = attr.intern(); // use == on interned attr
		return getValFromInterned(internedAttr); 
	}

	/** Returns the value of the given interned attr, or null if none. */
	protected String getValFromInterned(String attr) {
		for (Pair<String,String> pair : pairsList) {
			if (pair.a == attr) return pair.b;
		}
	    return null;
	}
}
