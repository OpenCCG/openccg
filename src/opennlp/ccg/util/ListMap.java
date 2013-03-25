///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2006 Michael White (The Ohio State University)
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
import java.util.*;

/**
 * A map where putting a value does not replace an old value 
 * but is instead included in a list of values for that key.
 * The map may use identity equals on keys. 
 * (NB: A ListMap is essentially a GroupMap that uses lists instead of sets.)
 *
 * @author      Michael White
 * @version     $Revision: 1.1 $, $Date: 2006/08/15 18:21:31 $
 */
public class ListMap<KeyType,ValType> {
    
	// the underlying map
	private THashMap map;
	
	/** Default constructor. */
	public ListMap() { this(false); }
	
	/** Constructor with flag for whether to use identity instead of <code>equals</code> on keys. */
	public ListMap(boolean useIdentityEquals) {
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
        // if already a list, add value to list
        else if (currentVal instanceof List) {
            List<ValType> list = (List<ValType>) currentVal;
            list.add(value);
        }
        // otherwise replace with a list including both values
        else {
            List<ValType> list = new ArrayList<ValType>(3);
            list.add((ValType)currentVal);
            list.add(value);
            map.put(key, list);
        }
        // return null, since we're not really replacing the old val
        return null;
    }

    /** Returns the list of values for the given key (or null). */
    @SuppressWarnings("unchecked")
	public List<ValType> get(KeyType key) {
        // get val
        Object val = map.get(key);
        // return if null or already a list
        if (val == null || val instanceof List) {
            return (List<ValType>) val;
        }
        // otherwise replace val with a list and return it
        List<ValType> list = new ArrayList<ValType>(1);
        list.add((ValType)val);
        map.put(key, list);
        return list;
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
    List<ValType> remove(KeyType key) {
    	List<ValType> retval = get(key);
    	map.remove(key);
    	return retval;
    }
}
