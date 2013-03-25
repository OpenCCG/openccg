///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-6 Jason Baldridge, Gann Bierner and 
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

package opennlp.ccg.util;

import gnu.trove.*;

import java.io.Serializable;
import java.util.*;

/**
 * A map where putting a value does not replace an old value but is rather
 * included in a set of values for that key.
 * The map may use identity equals on keys. 
 *
 * @author      Jason Baldridge
 * @author      Gann Bierner
 * @author      Michael White
 * @version     $Revision: 1.9 $, $Date: 2009/07/17 04:23:30 $
 */
public class GroupMap<KeyType,ValType> implements Serializable {
    
	private static final long serialVersionUID = -2995356057195571222L;
	
	// the underlying map
	private THashMap map;
	
	/** Default constructor. */
	public GroupMap() { this(false); }
	
	/** Constructor with flag for whether to use identity instead of <code>equals</code> on keys. */
	public GroupMap(boolean useIdentityEquals) {
		if (useIdentityEquals) map = new THashMap(new TObjectIdentityHashingStrategy());
		else map = new THashMap();
	}
	
    /** Adds the given key-value pair to the map, and returns null. */
	@SuppressWarnings("unchecked")
	public Object put(KeyType key, ValType value) {
        // get current val
        Object currentVal = map.get(key);
        // if none, add value to map
        if (currentVal == null) { 
        	map.put(key, value);
        }
        // if already a set, add value to set
        else if (currentVal instanceof Set) {
            Set<ValType> set = (Set<ValType>) currentVal;
            set.add(value);
        }
        // otherwise replace with a set including both values
        else {
            Set<ValType> set = new THashSet();
            set.add((ValType)currentVal);
            set.add(value);
            map.put(key, set);
        }
        // return null, since we're not really replacing the old val
        return null;
    }

    /** Returns the set of values for the given key (or null). */
    @SuppressWarnings("unchecked")
	public Set<ValType> get(KeyType key) {
        // get val
        Object val = map.get(key);
        // return if null or already a set
        if (val == null || val instanceof Set) {
            return (Set<ValType>) val;
        }
        // otherwise replace val with a set and return it
        Set<ValType> set = new THashSet();
        set.add((ValType)val);
        map.put(key, set);
        return set;
    }
    
    /** Adds a key-value pair to the map for all the given vals. */
    public void putAll(KeyType key, Collection<ValType> vals) {
    	for (ValType val : vals) put(key, val);
    }
    
    
    /** Returns the size of the underlying map. */
    public int size() { return map.size(); }

    /** Returns the keys. */
    @SuppressWarnings("unchecked")
	public Set<KeyType> keySet() {
    	return (Set<KeyType>) map.keySet();
    }
    
    /** Returns whether the keys contain the given one. */
    public boolean containsKey(KeyType key) {
    	return map.containsKey(key);
    }
    
    /** Removes the given key, returning its previous value (if any). */
    Set<ValType> remove(KeyType key) {
    	Set<ValType> retval = get(key);
    	map.remove(key);
    	return retval;
    }
}
