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
package opennlp.ccg.alignment;

/**
 * A set of {@link Enum} constants representing the two common indexing bases used in representing alignments.
 * The constants are ordered so that their {@link Enum#ordinal()} method returns a number corresponding to 
 * their {@linkplain #getStart() start index}.
 * <p>
 * An index base is characterized by its {@linkplain IndexBase#getStart() starting point} and its 
 * {@linkplain #getNullValue() null value}, which is derived from its starting point by decrementing by one.
 * One index base can translate an integer in that base into another base via the
 * {@link #translate(Integer, IndexBase)} method.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 * @see EncodingScheme
 */
public enum IndexBase {
	
	/**
	 * The index base that starts with <tt>0</tt>.
	 */
	ZERO,
	
	/**
	 * The index base starting with <tt>1</tt>.
	 */
	ONE;
	
	final Integer start, nullValue;
	
	private IndexBase() {
		this.start = Integer.valueOf(ordinal());
		this.nullValue = start - 1;
	}
	
	/**
	 * Gets the starting point of this index base.
	 */
	public Integer getStart() {
		return start;
	}

	/**
	 * This index base's special null value, the value of {@link #getStart()}<code> - 1</code>.
	 */
	public Integer getNullValue() {
		return nullValue;
	}

	/**
	 * Tests whether the specified index is valid in this index base.
	 * @param index The index to test.
	 * @return true If index is non-null and not less than {@link #getNullValue()}.
	 */
	public boolean isValidIndex(Integer index) {
		return index != null && nullValue <= index;
	}
	
	/**
	 * Translates an index in this base to another base. For example, <code>ZERO.translate(2, ONE)</code> yields
	 * <code>3</code> and <code>ONE.translate(1, ZERO)</code> yields <code>0</code>. Note that supplying the same
	 * index base as the target has no effect, so that if <code>b</code> is an index base constant, then
	 * <code>b.translate(n, b)</code> returns <code>n</code> for every {@link Integer} <code>n</code> as long
	 * as <code>n</code> is {@linkplain #isValidIndex(Integer) valid} (throwing an exception otherwise).
	 * @param index The index to translate.
	 * @param target The target index base to translate <code>index</code> into.
	 * @return The value of <code>index</code> as it is represented in the index base <code>target</code>.
	 * @throws IllegalArgumentException If <code>index</code> is invalid for this index base.
	 * @see #isValidIndex(Integer) 
	 */
	public Integer translate(Integer index, IndexBase target) {
		if(!isValidIndex(index)) {
			throw new IllegalArgumentException("invalid index for index base " + name() + ": " + index);
		}
		
		if(target == this) {
			return index;
		}
		
		return Integer.valueOf(index + (target.start - start));
	}
}