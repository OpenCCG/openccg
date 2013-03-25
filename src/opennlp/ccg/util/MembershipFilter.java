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

import java.util.Set;

/**
 * A filter that allows elements based on their membership in a set specified at creation. Later modifications
 * to this set will be reflected in the behavior of this filter's {@link #allows(Object)} method because
 * the set is not copied at creation.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class MembershipFilter<E> implements Filter<E> {

	/**
	 * The set to test for membership.
	 */
	protected Set<E> members;
	
	/**
	 * Creates a new membership filter based on the specified set of members. This set can later be modified
	 * and have its new membership reflected by this filter's {@link #allows(Object)} method because the
	 * specified set is not copied by this filter.
	 * 
	 * @param members The set to test for membership.
	 * @throws IllegalArgumentException If <tt>members</tt> is <tt>null</tt>.
	 */
	public MembershipFilter(Set<E> members) {
		if(members == null) {
			throw new IllegalArgumentException("members is null");
		}
		
		this.members = members;
	}

	/**
	 * Tests whether this membership filter allows the specified element by testing whether the membership
	 * set contains the element.
	 * 
	 * @param e The element to test membership for. 
	 * @return <tt>true</tt> if the set of members specified at creation contains <tt>e</tt>.
	 * @see #MembershipFilter(Set)
	 */
	@Override
	public boolean allows(E e) {
		return members.contains(e);
	}

}
