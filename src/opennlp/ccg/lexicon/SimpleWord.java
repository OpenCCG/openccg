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

package opennlp.ccg.lexicon;

import opennlp.ccg.util.*;
import java.util.*;

/**
 * A SimpleWord object is a surface word which stores just the word form.
 * SimpleWord serves as the base class for concrete instantiations of words.
 *
 * @author      Michael White
 * @version     $Revision: 1.4 $, $Date: 2009/07/17 04:23:30 $
 */
public class SimpleWord extends Word {
    
	private static final long serialVersionUID = 181491057498517717L;
	
	/** The surface form. */
    protected String form;
    
    /** Returns the surface form. */
    public String getForm() { return form; }
    
    /** Returns the pitch accent. */
    public String getPitchAccent() { return null; }
    
    /** Returns the list of extra attribute-value pairs. */
    protected List<Pair<String,String>> getAttrValPairsList() { return null; }
    
    /** Returns the stem. */
    public String getStem() { return null; }
    
    /** Returns the part of speech. */
    public String getPOS() { return null; }
    
    /** Returns the supertag. */
    public String getSupertag() { return null; }
    
    /** Returns the semantic class. */
    public String getSemClass() { return null; }

    
    /** Returns the value of the attribute with the given name, or null if none. 
        The attribute names Tokenizer.WORD_ATTR, ..., Tokenizer.SEM_CLASS_ATTR 
        may be used to retrieve the form, ..., semantic class. */
    public String getVal(String attr) {
        String internedAttr = attr.intern(); // use == on interned attr
        if (internedAttr == Tokenizer.WORD_ATTR) return getForm();
        if (internedAttr == Tokenizer.PITCH_ACCENT_ATTR) return getPitchAccent();
        if (internedAttr == Tokenizer.STEM_ATTR) return getStem();
        if (internedAttr == Tokenizer.POS_ATTR) return getPOS();
        if (internedAttr == Tokenizer.SUPERTAG_ATTR) return getSupertag();
        if (internedAttr == Tokenizer.SEM_CLASS_ATTR) return getSemClass();
        List<Pair<String,String>> pairs = getAttrValPairsList(); 
        if (pairs == null) return null;
        for (int i = 0; i < pairs.size(); i++) {
            Pair<String,String> p = pairs.get(i);
            if (p.a == internedAttr) return p.b;
        }
        return null; // not found
    }

    
    /** Constructor. */
    protected SimpleWord(String form) {
        this.form = form; 
    }
}

