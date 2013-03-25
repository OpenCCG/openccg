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

import java.util.HashSet;

/**
 * A filter that tracks which elements have already been visited by some process or iteration, allowing only
 * those that have not yet been visited. An element is considered visited when this filter's 
 * {@link #allows(Object)} method has been called with it as an argument.
 * <p>
 * Internally, the visited elements are tracked by using a
 * {@link MembershipFilter} based on a {@link HashSet}.  
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class VisitedFilter<E> extends MembershipFilter<E> {

	/**
	 * Creates a new visited filter with an empty set of visited elements.
	 */
	public VisitedFilter() {
		super(new HashSet<E>());
	}
	
	/**
	 * Tests whether this filter allows <tt>e</tt> by testing whether or not it has been visited. An element
	 * has been visited if this method has been previously called with it as an argument.
	 * 
	 * @param e The element to test whether it has been visited or not.
	 * @return <tt>true</tt> if <tt>e</tt> has not yet been visited.
	 * 
	 * @see #hasVisited(Object)
	 */
	@Override
	public boolean allows(E e) {
		if(!hasVisited(e)) {
			members.add(e);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Tests whether the specified element has been visited or not.
	 * @param e The element to test for visitation.
	 * @return <tt>true</tt> if <tt>e</tt> is among the elements that have been previously visited.
	 * 
	 * @see #allows(Object)
	 * @see MembershipFilter#allows(Object)
	 */
	public boolean hasVisited(E e) {
		return super.allows(e);
	}
}
