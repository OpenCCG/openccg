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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Composes the effects of several {@linkplain Filter filters} into a single filter.
 * The component filters are interpreted conjunctively, so that they all must allow a
 * given element for the composite filter to allow it.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class CompositeFilter<E> implements Filter<E> {

	Set<Filter<? super E>> filters;
	
	/**
	 * Creates a new empty composite filter.
	 */
	public CompositeFilter() {
		this.filters = new HashSet<Filter<? super E>>();
	}
	
	/**
	 * Creates a new composite filter made up of the specified filters.
	 * @see #CompositeFilter(Collection)
	 */
	@SafeVarargs
	public CompositeFilter(Filter<? super E>... edgeFilters) {
		this(Arrays.asList(edgeFilters));
	}
	
	/**
	 * Creates a new composite filter made up of the specified filters.
	 * The specified filters can be filters on an superclass of this filter's
	 * type parameter.
	 */
	public CompositeFilter(Collection<? extends Filter<? super E>> filters) {
		this.filters = new HashSet<Filter<? super E>>(filters);
	}
	
	/**
	 * Gets the set of filters this composite filter is made up of. 
	 * @return An unmodifiable set view of the filters making up this composite filter.
	 */
	public Set<? extends Filter<? super E>> filters() {
		return Collections.unmodifiableSet(filters);
	}
	
	/**
	 * Tests whether this filter contains a given filter.
	 * @return <tt>true</tt> if the specified filter is one of the ones making up this 
	 * composite filter.
	 */
	public boolean containsFilter(Filter<? super E> filter) {
		return filters.contains(filter);
	}
	
	/**
	 * Adds a filter to this composite filter, if it is not already present.
	 * @param filter The filter to add.
	 * @return <tt>true</tt> if the filter was not already contained.
	 * @throws IllegalArgumentException If <tt>filter</tt> is <tt>null</tt>.
	 * 
	 * @see Collection#add(Object)
	 */
	public boolean addFilter(Filter<? super E> filter) {
		if(filter == null) {
			throw new IllegalArgumentException("filter is null");
		}
		
		return filters.add(filter);
	}
	
	/**
	 * Removes the specified filter.
	 * @param filter The filter to remove.
	 * @return <tt>true</tt> if the specified filter was removed from this composite filter.
	 */
	public boolean removeFilter(Filter<? super E> filter) {
		return filters.remove(filter);
	}

	/**
	 * Tests whether the given element is allowed by applying each of this composite filter's components
	 * to it one by one, calling each of their {@link Filter#allows(Object)} method exactly once wit the
	 * specified argument.
	 * 
	 * @return <tt>false</tt> if one of the filters making up this composite filters returns <tt>false</tt> from its
	 * {@link Filter#allows(Object)} method for the argument <tt>e</tt>, otherwise <tt>true</tt>. In particular,
	 * this means that an empty composite filter returns <tt>true</tt> for every argument.
	 */
	@Override
	public boolean allows(E e) {
		for(Filter<? super E> f : filters) {
			if(!f.allows(e)) {
				return false;
			}
		}
		
		return true;
	}

}
