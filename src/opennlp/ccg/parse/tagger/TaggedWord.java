///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010 Dennis N. Mehay
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed inp the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

/**
 * A wrapper around {@code Word}s that can hold multitaggings (for POSs and for
 * supertags).
 */

package opennlp.ccg.parse.tagger;

import java.util.List;
import opennlp.ccg.lexicon.Word;
import opennlp.ccg.util.Pair;

/**
 * @author Dennis N. Mehay
 */
public class TaggedWord {
    // multitaggings for POSs and supertags (resp).
    private List<Pair<Double,String>> postagging;    
    private List<Pair<Double,String>> stagging;    
    
    // old-timey Word that holds the word form (and potentially gold POS and supertag).
    private Word oldWord;
    
    /** Decorators for the core functionality of the underlying word. */
    public String getSupertag() { return oldWord.getSupertag(); }
    public String getForm() { return oldWord.getForm(); }
    public String getPOS() { return oldWord.getPOS(); }
    
    /** Accessor for the underlying vanilla Word. */
    public Word getWord() { return oldWord; }
    
    /** Constructor with a Word. */
    public TaggedWord(Word wd) { 
        oldWord = Word.createFullWord(wd, wd.getForm(), wd.getPOS(), wd.getSupertag(), wd.getSemClass()); 
    }
        
    /** This does the obvious thing. */
    public void setSupertagging(List<Pair<Double,String>> stagging) { this.stagging = stagging; }
    
    /** 
     * Set the multi-POS tagging.  
     * Also replace the underlying single-best tagging with the
     * first tag of the multitag list.
     */
    public void setPOSTagging(List<Pair<Double,String>> postagging) { 
        this.postagging = postagging; 
        oldWord = Word.createFullWord(oldWord, oldWord.getForm(), this.postagging.get(0).b, oldWord.getSupertag(), oldWord.getSemClass());            
    }
    
    
    /** This does the obvious thing. */
    public List<Pair<Double,String>> getSupertagging() { return stagging; }
    
    /** This does the obvious thing. */
    public List<Pair<Double,String>> getPOSTagging() { return postagging; }
    
    /** Gets the gold-standard supertag. */
    public String getGoldSuper() { return oldWord.getSupertag(); }
    
    /** Gets the gold-standard POS tag. */
    public String getGoldPOS() { return oldWord.getPOS(); }
    
}
