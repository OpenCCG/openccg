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
 * Abstract class for filters that delegates to another filter that tests elements that do not
 * necessarily apply to the same type of elements as this filter. This filter's
 * {@link #allows(Object)} method returns the value of the 
 * {@linkplain #getDelegateFilter() delegate filter}'s <tt>allows(...)</tt> method
 * for the value returned by {@link #delegateValueFor(Object)} for the specified
 * element.
 * <p>
 * Concrete subclasses need to specify (1) the delegate filter (via their constructor), and
 * (2) a way to determine which element of type <tt>D</tt> the delegate filter should use
 * based on a specified element of type <tt>E</tt> by implementing {@link #delegateValueFor(Object)}.
 * A typical example is the case when a filter is desired that compares elements of type <tt>E</tt>, but
 * the comparison needs to take place on some type-<tt>D</tt> object somehow derived from instances of
 * <tt>E</tt>, e.g. by an accessor method.
 * 
 * @param <E> The type of elements that this filter applies to.
 * @param <D> The type of elements that the delegated filter applies to.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public abstract class DelegatedFilter<E, D> implements Filter<E> {

	Filter<? super D> delegateFilter;
	
	/**
	 * Creates a delegated filter with the specified filter to delegate to. The delegated filter will be
	 * used in the test for {@link #allows(Object)}, through the {@link #delegateValueFor(Object)}.
	 */
	protected DelegatedFilter(Filter<? super D> delegateFilter) {
		this.delegateFilter = delegateFilter;
	}

	/**
	 * Gets the filter that this filter delegates to.
	 * @return The filter specified at creation.
	 * @see #DelegatedFilter(Filter)
	 */
	public Filter<? super D> getDelegateFilter() {
		return delegateFilter;
	}

	/**
	 * Tests whether this filter allows the specified element by testing whether its
	 * {@linkplain #getDelegateFilter() delegate filter} allows the value of {@link #delegateValueFor(Object)}
	 * for the argument <tt>e</tt>.
	 * @return <tt>true</tt> if the delegate filter's {@link Filter#allows(Object)} method returns <tt>true</tt>
	 * for the element returned by <tt>delegateValueFor(e)</tt>.
	 * @see #delegateValueFor(Object)
	 */
	@Override
	public boolean allows(E e) {
		return delegateFilter.allows(delegateValueFor(e));
	}

	/**
	 * Gets the element of type <tt>D</tt> that the delegated filter should use in its
	 * {@link Filter#allows(Object)} comparisons, given the specified type-<tt>E</tt> element.
	 * @param e The element to obtain a type-<tt>D</tt> element for.
	 * @return The element that the delegated filter should use for comparison, based on <tt>e</tt>.
	 */
	public abstract D delegateValueFor(E e);
}
