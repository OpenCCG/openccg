///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2009 Dennis N. Mehay
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
package opennlp.ccg.parse.supertagger.util;

//import util.Pair;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import opennlp.ccg.util.Pair;

/**
 * @author Dennis N. Mehay
 * @version $Revision: 1.2 $, $Date: 2010/09/21 04:12:41 $
 */
public class STTaggerPOSDictionary implements STTaggerDictionary, Serializable {

	private static final long serialVersionUID = -4814356608876054823L;
	
	/**
     * This object represents our dictionary.  The String is the
     * POS we want to look up, and the Collection<String> holds all of 
     * of the CCG lex. cat's seen with that POS.
    */
    private Map<String, Collection<String>> dict = null;

   
    /**
     * This constructor does not create the dictionary; that must
     * be done using a <code>DictionaryMaker</code>.    
     */
    public STTaggerPOSDictionary(Map<String, Collection<String>> dict) { 
    	this.dict = dict;
    } 

    /** 
     * This method implements the <code>interface</code> STTaggerDictionary
     * by delegating to <code>getEntry(String, int)</code> (see below).
    */
    public Collection<String> getEntry(String POS) {
	return this.dict.get(POS);
    }

    /**
     * A method that returns the contents of the mapping embodied in this dictionary.
     * @return An <code>Iterator</code> of <code>supertagger.util.Pair</code>s
     * that represent the pos ->  { ... supertags ...} mappings in the dictionary.
     */
    public Iterator<Pair<String, Collection<String>>> getMappings() {
        Iterator<String> keyset = this.dict.keySet().iterator();
        ArrayList<Pair<String, Collection<String>>> preRes = 
                new ArrayList<Pair<String, Collection<String>>>();
        String tempS = null;
        while(keyset.hasNext()) {
            tempS = keyset.next();
            preRes.add(new Pair<String, Collection<String>>(tempS, this.dict.get(tempS)));
        }
        return preRes.iterator();
    }
    
    /**
     * A method to test whether this <code>STTaggerDictionary</code> contains 
     * an entry for a particular <code>String</code> representing a POS tag.  
     *
     * @param key A <code>String</code> representing a particular
     *            POS tag.
     * @return A <code>boolean</code> value of <code>true</code> or 
     *           <code>false</code> answering the question of whether this
     *           dictionary contains an entry for the specified POS tag.
     */
    public boolean containsEntry(String POS) { return this.dict.containsKey(POS); }
    
} // End class STTaggerPOSDictionary


