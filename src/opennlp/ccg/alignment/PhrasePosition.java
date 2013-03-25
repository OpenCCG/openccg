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
 * In an alignment, a phrase position indicates which of the two aligned phrases
 * is first (the &quot;A&quot; phrase) and which is second (the &quot;B&quot; phrase)
 * in terms of the alignment indices.
 *  
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public enum PhrasePosition {
	
	/**
	 * The &quot;A&quot; position.
	 */
	A,
	
	/**
	 * The &quot;B&quot; position.
	 */
	B;
	
	/**
	 * Gives the opposite of this phrase position.
	 * @return {@link #B} if this position is {@link #A}, otherwise 
	 * {@link #A}.
	 */
	public PhrasePosition opposite() {
		return (this == A) ? B : A;
	}
}
