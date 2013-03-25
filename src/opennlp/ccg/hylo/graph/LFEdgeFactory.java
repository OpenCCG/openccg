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

import org.jgrapht.EdgeFactory;

/**
 * A factory for LF edges that creates edges from specified source and target vertices and an edge label.
 * This interface extends the {@link EdgeFactory} interface for the specialized case of
 * directed, labeled LF edges with LF vertices as their nodes. A default implementation
 * is provided in {@link DefaultLFEdgeFactory}.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public interface LFEdgeFactory extends EdgeFactory<LFVertex, LFEdge> {
	
	/**
	 * Creates a new labeled, directed edge from a specified vertex pair and edge label.
	 * @param sourceVertex The source vertex of the new edge.
	 * @param targetVertex The target vertex of the new edge.
	 * @param label The label of the new edge.
	 * @return An instance of {@link LFEdge} with the specified parameters.
	 * 
	 * @see LFEdge#LFEdge(LFVertex, LFVertex, LFEdgeLabel)
	 */
	public LFEdge createLabeledEdge(LFVertex sourceVertex, LFVertex targetVertex, LFEdgeLabel label);

}
