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
 * Interface for filters that characterize a set by some membership criteria.
 * This interface uses a single method, {@link #allows(Object)}, to allow implementing
 * classes to say whether the specified element should be a member of the collection or not.
 * <p>
 * Filters can be thought of as characteristic functions for sets. The type parameter is 
 * used to signal what kind of elements a filter applies to.
 * 
 * @param <E> The type of elements that this filter applies to.
 * 
 * @see FilteredSet
 * @see FilteredMap
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public interface Filter<E> {

	/**
	 * Tests whether the specified element is allowed.
	 * @return <tt>true</tt> if the provided element should be allowed into the collection.
	 */
	boolean allows(E e);
}
