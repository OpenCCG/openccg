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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import opennlp.ccg.hylo.Flattener;
import opennlp.ccg.hylo.Nominal;
import opennlp.ccg.synsem.LF;

import org.jgrapht.graph.DirectedMultigraph;

/**
 * A graph representation of an {@link LF}. By default, LF graphs are empty. But populated LF graphs can
 * be easily created using the {@link LFGraphFactory} class.
 * <p>
 * This class extends the 
 * {@link DirectedMultigraph} class from the <tt>org.jgrapht.graph</tt> package that provides some
 * specialized methods for dealing with OpenCCG LFs. It provides some flexibility for edge creation
 * by allowing an {@link LFEdgeFactory} to be specified at creation. The original LF structure is also
 * reflected in the {@link #getLFAncestry(LFVertex)}, {@link #highestLFAncestors()}, and
 * {@link #highestLFAncestorOf(LFVertex)} methods.
 * <p>
 * As a convenience, the method {@link #findVertexByNominal(Nominal)} provides access to the vertex
 * corresponding to a given nominal. This is implemented by a hash map so that the lookup takes place in 
 * constant time. 
 * 
 * @see <a href="http://jgrapht.org/">JGraphT website</a>
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class LFGraph extends DirectedMultigraph<LFVertex,LFEdge> {

	private static final long serialVersionUID = 1L; //TODO make this really serializable?
	
	/**
	 * The map reflecting LF ancestry by giving the highest ancestor for a specified
	 * vertex, or <tt>null</tt> if it is the highest ancestor.
	 */
	protected Map<LFVertex, LFVertex> highestAncestorMap = new HashMap<LFVertex, LFVertex>();
	
	
	/**
	 * Convenience cache of nominals mapped to vertices.
	 * @see #findVertexByNominal(Nominal)
	 */
	protected Map<Nominal, LFVertex> nominalVertexMap = new HashMap<Nominal, LFVertex>();
	
	/**
	 * Creates a new, empty LF graph that uses an implementation of {@link LFEdgeFactory}
	 * as its edge factory.
	 * @see #LFGraph(LFEdgeFactory)
	 */
	public LFGraph() {
		this(LFGraphFactory.DEFAULT_EDGE_FACTORY);
	}
	
	/**
	 * Creates a new, empty LF graph using the specified edge factory.
	 * @param lfEdgeFactory The edge factory to use for creating edges.
	 * 
	 * @see LFGraphFactory
	 */
	public LFGraph(LFEdgeFactory lfEdgeFactory) {
		super(lfEdgeFactory);
	}
	
	/**
	 * Gets the {@link LFEdgeFactory} used to create this LF graph.
	 * @return The value of {@link #getEdgeFactory()}, cast to an 
	 * {@link LFEdgeFactory}.
	 */
	public LFEdgeFactory getLFEdgeFactory() {
		return (LFEdgeFactory)getEdgeFactory();
	}

	/**
	 * Adds a new labeled edge based on the specified source and target vertices and edge label by calling
	 * <tt>getLFEdgeFactory().createLabeledEdge(source, target, label)</tt>. The new
	 * edge is added to this graph's {@linkplain #edgeSet() edge set}.
	 * @param source The source vertex of the new edge.
	 * @param target The target vertex of the new edge.
	 * @param label The label of the new edge.
	 * @return The newly created edge if it was successfully added to this graph, <tt>null</tt> otherwise.
	 */
	public LFEdge addLabeledEdge(LFVertex source, LFVertex target, LFEdgeLabel label) {
		LFEdge e = getLFEdgeFactory().createLabeledEdge(source, target, label);
		return addEdge(source, target, e) ? e : null;
	}
	
	/**
	 * Finds a vertex by its nominal. This method does a lookup on a dictionary mapping each vertex's 
	 * {@linkplain LFVertex#getNominal() nominal} to members of the{@link #vertexSet()}. 
	 * @param nominal The nominal to test for.
	 * @return A vertex whose nominal is equivalent to the one specified, or <tt>null</tt> if none is present.
	 */
	public LFVertex findVertexByNominal(Nominal nominal) { 
		return nominalVertexMap.get(nominal);
	}

	/**
	 * Overrides the superclass method to add a mapping from the vertex's {@linkplain LFVertex#getNominal() nominal}
	 * to the vertex itself, for later retrieval via {@link #findVertexByNominal(Nominal)}.
	 */
	@Override
	public boolean addVertex(LFVertex v) {
		boolean b = super.addVertex(v); // give this a chance first
		
		if(b) {
			nominalVertexMap.put(v.nominal, v);
		}
		
		return b;
	}

	/**
	 * Overrides the superclass method to remove any existing mapping from some nominal to the specified vertex.
	 * @see #addVertex(LFVertex)
	 * @see #findVertexByNominal(Nominal)
	 */
	@Override
	public boolean removeVertex(LFVertex v) {
		boolean b = super.removeVertex(v); // invoke this first
	
		if(b) {
			nominalVertexMap.values().remove(v);
		}
		
		return b;
	}

	/**
	 * Gets the highest LF ancestor of the specified vertex, as determined by the LF structure.
	 * @param vertex The vertex to get the highest ancestor for.
	 * @return The highest ancestor of the specified vertex, or <tt>null</tt> if it is the highest in its
	 * ancestry.
	 * @see Flattener#getHighestParentMap()
	 */
	public LFVertex highestLFAncestorOf(LFVertex vertex) {
		return highestAncestorMap.get(vertex);
	}
	
	/**
	 * Gets the LF ancestry corresponding to the specified vertex.
	 * 
	 * @return The parents of the specified vertex in the LF ancestry.
	 * @see #highestLFAncestorOf(LFVertex) 
	 * @see Flattener#getHighestParentMap()
	 */
	public Set<LFVertex> getLFAncestry(LFVertex vertex) {
		LFVertex a = highestLFAncestorOf(vertex);
		Set<LFVertex> as = new LinkedHashSet<LFVertex>();
		
		for(LFVertex v : vertexSet()) {
			if(!v.equals(vertex) && highestLFAncestorOf(v).equals(a)) {
				as.add(v);
			}
		}
		
		return as;
	}
	
	/**
	 * Gets the vertex or vertices that are at the top of the LF ancestry hierarchy. 
	 * @return The set of vertices <tt>v</tt> for which {@link #highestLFAncestorOf(LFVertex)} returns
	 * <tt>null</tt>.
	 * @see Flattener#getHighestParentMap()
	 */
	public Set<LFVertex> highestLFAncestors() {
		Set<LFVertex> ps = new LinkedHashSet<LFVertex>();
		
		for(LFVertex v : vertexSet()) {
			if(highestLFAncestorOf(v) == null) {
				ps.add(v);
			}
		}
		
		return ps;
	}
}
