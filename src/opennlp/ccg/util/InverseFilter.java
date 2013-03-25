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

/**
 * A filter that wraps another filter an inverts its effects. Specifically, for each argument that
 * the wrapped filter's {@link Filter#allows(Object)} method returns <tt>true</tt>, this filter
 * returns <tt>false</tt>, and vice versa.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class InverseFilter<E> implements Filter<E> {

	Filter<? super E> originalFilter;
	
	/**
	 * Creates a new filter based on the specified filter, inverting its effects. The specified filter can
	 * apply to any superclass of this filter's type parameter.
	 * @param originalFilter The filter to invert.
	 * @throws IllegalArgumentException If <tt>originalFilter</tt> is <tt>null</tt>.
	 */
	public InverseFilter(Filter<? super E> originalFilter) {
		if(originalFilter == null) {
			throw new IllegalArgumentException("originalFilter is null");
		}
		
		this.originalFilter = originalFilter;
	}
	
	/**
	 * Gets the original, non-inverted filter that this inverse filter wraps.
	 * @return The filter specified at creation.
	 * @see #InverseFilter(Filter)
	 */
	public Filter<? super E> getOriginalFilter() {
		return originalFilter;
	}

	/**
	 * Tests whether this filter allows a specified element by calling the original filter's
	 * {@link Filter#allows(Object)} method and reversing its boolean value.
	 * 
	 * @param e The element to test.
	 * @return A value equivalent to calling <tt>!getOriginalFilter().allows(e)</tt>.
	 */
	@Override
	public boolean allows(E e) {
		return !originalFilter.allows(e);
	}

}
