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
 * A factory for LF edges that creates edges from specified source and target vertices and an edge label.
 * This class provides a default implementation of the {@link LFEdgeFactory} interface for instantiating
 * {@link LFGraph}s.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class DefaultLFEdgeFactory implements LFEdgeFactory {
	
	/**
	 * Creates an edge from a specified source and target vertex.
	 * @return A new edge with a <tt>null</tt> label.
	 * @see #createLabeledEdge(LFVertex, LFVertex, LFEdgeLabel)
	 */
	@Override
	public LFEdge createEdge(LFVertex sourceVertex, LFVertex targetVertex) {
		return createLabeledEdge(sourceVertex, targetVertex, null);
	}

	/**
	 * Creates a new labeled, directed edge from a specified vertex pair and edge label.
	 * @param sourceVertex The source vertex of the new edge.
	 * @param targetVertex The target vertex of the new edge.
	 * @param label The label of the new edge.
	 * @return An instance of {@link LFEdge} with the specfied parameters.
	 * 
	 * @see LFEdge#LFEdge(LFVertex, LFVertex, LFEdgeLabel)
	 */
	@Override
	public LFEdge createLabeledEdge(LFVertex sourceVertex, LFVertex targetVertex, LFEdgeLabel label) {
		return new LFEdge(sourceVertex, targetVertex, label);
	}

}
