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

//import java.util.ArrayList;
import java.util.Collection;

/**
 * An interface for supertagger "dictionaries" as described
 * in, e.g., Clark (2002) _Supertagging for CCG_.
 * 
 * @author Dennis N. Mehay
 * @version $Revision: 1.1 $, $Date: 2009/08/21 17:20:20 $
 */
public interface STTaggerDictionary {

    /**
     * A method for getting the dictionary entry for a particular
     * <code>String</code> key.  
     * The key will usually be a word, lemma or a part 
     * of speech, but you may have other interesting grammatical things 
     * to associate with supertags.
     * 
     * @param key A <code>String</code> representing a particular
     *            grammatical type.
     * @return A <code>Collection<String></code> containing supertags (CCG lexical 
     *         categories) seen with the particular grammatical type `key'.
     *         Returns <code>null</code> if that word was not seen in the
     *         corpus or (if the implementing class has a frequency cut-off)
     *         if that type's token frequency was not high enough.
     */
    public Collection<String> getEntry(String key);
    
    /**
     * A method to test whether this <code>STTaggerDictionary</code> contains 
     * an entry for a particular <code>String</code> key.  
     * The key will usually be a word, lemma or a part of speech, but 
     * you may have other interesting grammatical things to associate with 
     * supertags.
     * 
     * @param key A <code>String</code> representing a particular
     *            grammatical type.
     * @return A <code>boolean</code> value of <code>true</code> or 
     *           <code>false</code> answering the question of whether this
     *           dictionary contains an entry for the specified key.
     */
    public boolean containsEntry(String key);
}

