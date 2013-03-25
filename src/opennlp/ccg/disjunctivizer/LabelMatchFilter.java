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
package opennlp.ccg.disjunctivizer;

import opennlp.ccg.hylo.graph.LFEdge;
import opennlp.ccg.hylo.graph.LFEdgeLabel;
import opennlp.ccg.util.Filter;


/**
 * A filter for edges based on a comparison of their {@linkplain LFEdge#getLabel() labels}. Instances of
 * this class compare a specified {@linkplain #getBasis() basis edge label}, so that the 
 * {@link #allows(LFEdge)} method returns <tt>true</tt> if it is
 * {@linkplain LFEdgeLabel#equals(Object) equivalent to} the specified edge's label. 
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class LabelMatchFilter implements Filter<LFEdge> {
	
	LFEdgeLabel basis;
	
	/**
	 * Creates a new label match filter that will compare edge labels to the specified label.
	 * @param basis The label to be used for comparison.
	 * @throws IllegalArgumentException if <tt>basis</tt> is <tt>null</tt>.
	 */
	public LabelMatchFilter(LFEdgeLabel basis) {
		checkBasis(basis);
		this.basis = basis;
	}

	private void checkBasis(LFEdgeLabel basis) {
		if(basis == null) {
			throw new IllegalArgumentException("basis is null");
		}
	}
	
	/**
	 * Gets the label used as the basis for comparison in the {@link #allows(LFEdge)} method.
	 * @return The edge label specified at creation.
	 * 
	 * @see #LabelMatchFilter(LFEdgeLabel)
	 */
	public LFEdgeLabel getBasis() {
		return basis;
	}

	/**
	 * Sets the edge label used as the basis for comparison.
	 * @throws IllegalArgumentException if <tt>basis</tt> is <tt>null</tt>.
	 */
	public void setBasis(LFEdgeLabel basis) {
		checkBasis(basis);
		this.basis = basis;
	}

	/**
	 * Tests whether the specified edge's label is equivalent to this filter's {@linkplain #getBasis() basis
	 * edge}.
	 * @param edge The edge to test.
	 * @return <tt>true</tt> if the basis edge label is equivalent to <tt>edge.getLabel()</tt> based on a
	 * comparison via their {@link LFEdgeLabel#equals(Object)} method.
	 */
	@Override
	public boolean allows(LFEdge edge) {
		return basis.equals(edge.getLabel());
	}

}
