//////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2012 Scott Martin
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

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A map whose keys must be allowed by a specified {@linkplain #getKeyFilter() key filter}.
 * The filtered mappings are kept in the same order they occurred in in the
 * {@linkplain #getOriginalMap() original map}.
 * 
 * @see Filter
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class FilteredMap<K,V> extends AbstractMap<K,V> {

	Map<? extends K,? extends V> originalMap;
	Filter<? super K> keyFilter;
	
	private final Map<K,V> map = new LinkedHashMap<K,V>();
	
	/**
	 * Creates a new filtered map including only the elements in <tt>originalMap</tt> whose
	 * keys are {@linkplain Filter#allows(Object) allowed} by the specified
	 * <tt>keyFilter</tt>.
	 * <p>
	 * Filtered maps maintain an {@linkplain #entrySet() entry set} whose entries occur in
	 * the same order as they occurred in the original map.
	 * 
	 * @param originalMap The map to draw this map's elements from.
	 * @param keyFilter The filter that decides which keys from the original map should be
	 * represented in this filtered map.
	 * @throws IllegalArgumentException If <tt>keyFilter</tt> is <tt>null</tt>.
	 * @see Map#putAll(Map)
	 */
	public FilteredMap(Map<? extends K,? extends V> originalMap, Filter<? super K> keyFilter) {
		if(keyFilter == null) {
			throw new IllegalArgumentException("keyFilter is null");
		}
		
		this.originalMap = originalMap;
		this.keyFilter = keyFilter;
		
		putAll(originalMap);
	}

	/**
	 * Gets the map that this map draws its elements from.
	 * @return The map specified at creation
	 * @see #FilteredMap(Map, Filter)
	 */
	public Map<? extends K,? extends V> getOriginalMap() {
		return originalMap;
	}
	
	/**
	 * Gets this map's key filter.
	 */
	public Filter<? super K> getKeyFilter() {
		return keyFilter;
	}	

	/**
	 * Gets the entry set for this map. Each entry's key is
	 * guaranteed to be allowable according to this map's {@linkplain #getKeyFilter() key 
	 * filter}.
	 * 
	 * @return The subset of the {@linkplain #getOriginalMap() original map}'s entries that
	 * are allowable by the key filter.
	 * 
	 * @see Map#entrySet()
	 */
	@Override
	public Set<Entry<K,V>> entrySet() {
		return map.entrySet();
	}

	/**
	 * Provides the ability to put new mappings into this filtered map, provided the specified
	 * key is {@linkplain Filter#allows(Object) allowed} by this map's
	 * {@linkplain #getKeyFilter() key filter}.
	 * 
	 * @return the element previously associated with <tt>key</tt> if the specified
	 * <tt>key</tt> is allowed by the key filter (and <tt>null</tt> if none was associated).
	 * This method always returns <tt>null</tt> for key/value pairs in which the specified
	 * key is <em>not</em> allowed by the key filter in effect. 
	 */
	@Override
	public V put(K key, V value) {
		if(keyFilter.allows(key)) {
			return map.put(key, value);
		}
		
		return null;
	}
}
