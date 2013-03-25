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
package opennlp.ccg.hylo.graph;

/**
 * An edge in an {@linkplain LFGraph LF graph}. LF graph edges are directed, containing a 
 * {@linkplain #getSource() source} and {@linkplain #getTarget() target} vertex as well as
 * an {@linkplain #getLabel() edge label} representing the type of relation between the two
 * vertices.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class LFEdge {

	final LFVertex source, target;
	final LFEdgeLabel label;
	
	/**
	 * Convenience constructor for creating edges with a <tt>null</tt> edge label. This constructor just
	 * calls <tt>LFEdge(source, target, null)</tt>.
	 * @see #LFEdge(LFVertex, LFVertex, LFEdgeLabel)
	 */
	public LFEdge(LFVertex source, LFVertex target) {
		this(source, target, null);
	}
	
	/**
	 * Creates a new LF edge with the specified source and target vertices, and edge label.
	 * @param source The source vertex of the new edge.
	 * @param target The target vertex of the new edge.
	 * @param label The label of the new edge, possibly <tt>null</tt>.
	 * 
	 * @throws IllegalArgumentException If either <tt>source</tt> or <tt>target</tt> is <tt>null</tt>.
	 */
	public LFEdge(LFVertex source, LFVertex target, LFEdgeLabel label) {
		checkVertex(source, "source");
		checkVertex(target, "target");
		
		this.source = source;
		this.target = target;
		this.label = label;
	}
	
	void checkVertex(LFVertex v, String name) {
		if(v == null) {
			throw new IllegalArgumentException(name + " is null");
		}
	}

	/**
	 * Gets the edge label, which may be <tt>null</tt>.
	 */
	public LFEdgeLabel getLabel() {
		return label;
	}

	/**
	 * Gets the source vertex.
	 */
	public LFVertex getSource() {
		return source;
	}
	
	/**
	 * Gets the target vertex.
	 */
	public LFVertex getTarget() {
		return target;
	}

	/**
	 * Tests whether this edge is equal to another by comparing the source and target vertices by using their
	 * {@link LFVertex#equals(Object)} methods.
	 * If the label is <tt>null</tt>, it is considered equivalent to the other edge's label only if
	 * the other edge's label is also <tt>null</tt>. Otherwise, the labels are compared using their
	 * {@link LFEdgeLabel#equals(Object)} method.
	 * 
	 * @param o The edge to compare this edge to.
	 */
	@Override
	public boolean equals(Object o) {
		if(o instanceof LFEdge) {
			LFEdge e = (LFEdge)o;
			return source.equals(e.source) && target.equals(e.target) 
				&& (label != null) ? label.equals(e.label) : e.label == null;
		}
		
		return false;
	}

	/**
	 * Computes a hash code for this edge based on the hash codes of its vertices and label, assuming the
	 * label is non-null.
	 */
	@Override
	public int hashCode() {
		int h = 37 * source.hashCode() + target.hashCode();
		
		if(label != null) {
			h += label.hashCode();
		}
		
		return h;
	}
	
	/**
	 * Gets a string representation of this edge. For example, if the edge's source is <tt>w1@woman</tt>,
	 * its target is <tt>w0@the</tt>, and its label is <tt>Det</tt>, this method returns
	 * <tt>w1@woman --Det--> w0@the</tt>.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(source.toString());
		sb.append(" --");
		sb.append((label == null) ? "(no label)" : label.toString());
		sb.append("--> ");
		sb.append(target.toString());
		
		return sb.toString();
	}
}

