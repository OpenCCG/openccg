///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-5 Jason Baldridge, Gann Bierner and 
//                      University of Edinburgh (Michael White)
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

import gnu.trove.*;
import java.util.*;

import opennlp.ccg.lexicon.Word;

/**
 * A set of signs, unique up to surface words.
 * 
 * WARNING: Signs with lower derivational complexity are kept during insertion.
 *
 * @author Jason Baldridge
 * @author Gann Bierner
 * @author Michael White
 * @author Daniel Couto-Vale
 * @version $Revision: 1.13 $, $Date: 2009/12/21 02:15:44 $
 */
public class SymbolHash extends THashSet {
	
	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = -6570863529126436679L;

	/** Hashing strategy that uses Sign's surfaceWordHashCode and surfaceWordEquals methods. */
    protected static TObjectHashingStrategy surfaceWordHashingStrategy = new TObjectHashingStrategy() {
		private static final long serialVersionUID = 1L;
		public int computeHashCode(java.lang.Object o) {
            return ((Symbol)o).surfaceWordHashCode();
        }
        public boolean equals(java.lang.Object o1, java.lang.Object o2) {
            return ((Symbol)o1).surfaceWordEquals((Symbol)o2);
        }
    };

    /** Default constructor. */
    public SymbolHash() { super(surfaceWordHashingStrategy); }

    /**
     * Constructor which adds one sign.
     */
    public SymbolHash(Symbol sign) {
        this(); insert(sign);
    }

    /**
     * Constructor which adds a collection of signs.
     */
    public SymbolHash(Collection<Symbol> c) {
        this();
        for (Symbol s : c) insert(s);
    }
    
    /**
     * Returns this as a set of signs.
     */
    @SuppressWarnings("unchecked")
	public Set<Symbol> asSymbolSet() { return (Set<Symbol>) this; }

    /**
     * Adds a sign, keeping the one with lower derivational complexity 
     * if there is an equivalent one there already; returns the old 
     * sign if it was displaced, the new sign if there was no equivalent 
     * old sign, or null if the sign was not actually added.
     */
    public Symbol insert(Symbol sign) {
        int pos = index(sign);
        if (pos >= 0) {
            Symbol oldSign = (Symbol) _set[pos];
            if (oldSign == sign) return null;
            if (sign.getDerivationHistory().compareTo(oldSign.getDerivationHistory()) < 0) {
            	_set[pos] = sign; return oldSign;
            }
            else return null;
        }
        else {
        	add(sign); return sign;
        }
    }
    
    /** Returns the signs sorted by their words lexicographically. */
    public List<Symbol> getSignsSorted() {
    	ArrayList<Symbol> retval = new ArrayList<Symbol>(asSymbolSet());
    	Collections.sort(retval, signComparator); 
    	return retval;
    }
    
    /** Comparator for signs to provide a persistent ordering. */
    public static final Comparator<Symbol> signComparator = new Comparator<Symbol>() {
		public int compare(Symbol sign1, Symbol sign2) {
			return compareTo(sign1, sign2);
		}
    };

    /** Compares signs by their derivation complexity, lists of words, then (somewhat desperately) cat hash codes. */
    public static int compareTo(Symbol sign1, Symbol sign2) {
    	int cmp = 0;
    	cmp = sign1.getDerivationHistory().compareTo(sign2.getDerivationHistory());
    	if (cmp != 0) return cmp;
    	List<Word> words1 = sign1.getWords(); 
    	List<Word> words2 = sign2.getWords();
    	cmp = compareTo(words1, words2);
    	if (cmp != 0) return cmp;
    	// TODO: implement compareTo method on categories
    	int h1 = sign1.getCategory().hashCode();
    	int h2 = sign2.getCategory().hashCode();
    	if (h1 < h2) return -1;
    	if (h1 > h2) return 1;
    	return 0;
    }
    
    /** Compares lists of words lexicographically. */
    public static int compareTo(List<Word> words1, List<Word> words2) {
    	int i=0;
    	while (i < words1.size() || i < words2.size()) {
    		if (i == words1.size()) return -1;
    		if (i == words2.size()) return 1;
    		Word w1 = words1.get(i); Word w2 = words2.get(i);
    		int cmp = w1.compareTo(w2);
    		if (cmp != 0) return cmp;
    		i++;
    	}
    	return 0;
    }
}
