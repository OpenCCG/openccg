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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A set whose elements must be {@linkplain Filter#allows(Object) allowed} by a specified
 * {@link Filter}. Conceptually, instances of this class represent the sets described by the
 * characteristic function corresponding to their {@linkplain #getFilter() filter}.
 * <p>
 * The elements in the filtered set are kept in the same order they occur in according to the
 * {@linkplain #getOriginalCollection() original collection}'s iterator.
 * 
 * @see Filter 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a> 
 */
public class FilteredSet<E> extends AbstractSet<E> {

	Collection<? extends E> originalCollection;
	Filter<? super E> filter;
	
	private final Set<E> set = new LinkedHashSet<E>();
	
	/**
	 * Creates a new filtered set based on the specified collection and filter. The resulting set will
	 * contain all the members of the original collection for which the specified filter's
	 * {@link Filter#allows(Object)} method returns <tt>true</tt>.
	 * @param originalCollection The collection from which this filtered set will draw its elements.
	 * @param filter The filter that decides which of the members of <tt>originalCollection</tt> are
	 * allowable. The specified filter can apply to elements of any superclass of this filtered set's
	 * type parameter.
	 * @throws IllegalArgumentException If <tt>filter</tt> is <tt>null</tt>.
	 * 
	 * @see #addAll(Collection)
	 */
	public FilteredSet(Collection<? extends E> originalCollection, Filter<? super E> filter) {
		if(filter == null) {
			throw new IllegalArgumentException("filter is null");
		}
		
		this.filter = filter;
		this.originalCollection = originalCollection;
		
		addAll(originalCollection);
	}

	/**
	 * Gets the original collection from which this filtered set's elements are drawn.
	 * @return The collection specified at creation.
	 * @see #FilteredSet(Collection, Filter)
	 */
	public Collection<? extends E> getOriginalCollection() {
		return originalCollection;
	}

	/**
	 * Gets the filter used by this filtered set to determine which elements are allowed in it.
	 * @return The filter specified at creation.
	 * @see #FilteredSet(Collection, Filter)
	 */
	public Filter<? super E> getFilter() {
		return filter;
	}

	/**
	 * Gets an iterator over the elements in this filtered set.
	 */
	@Override
	public Iterator<E> iterator() {
		return set.iterator();
	}

	/**
	 * Gets the size of this filtered set (the number of elements it contains).
	 */
	@Override
	public int size() {
		return set.size();
	}

	/**
	 * Adds an element if it conforms to the {@linkplain #getFilter() filter in effect}, determined by
	 * consulting the filter's {@link Filter#allows(Object)} method using the supplied element. 
	 * @param e The element to add, after testing its allowability according to this filtered set's filter.
	 * @return <tt>true</tt> if the filter allows <tt>e</tt> and this set changed as a result of the addition
	 * (because the specified element <tt>e</tt> was not already contained).
	 */
	@Override
	public boolean add(E e) {
		return filter.allows(e) && set.add(e);
	}

}
