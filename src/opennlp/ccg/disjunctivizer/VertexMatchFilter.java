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

import static opennlp.ccg.disjunctivizer.MatchType.SOURCE_MATCH;
import static opennlp.ccg.disjunctivizer.MatchType.SOURCE_MISMATCH;
import static opennlp.ccg.disjunctivizer.MatchType.SOURCE_PREDICATE_MATCH;
import static opennlp.ccg.disjunctivizer.MatchType.SOURCE_PREDICATE_MISMATCH;
import static opennlp.ccg.disjunctivizer.MatchType.TARGET_MATCH;
import static opennlp.ccg.disjunctivizer.MatchType.TARGET_MISMATCH;
import static opennlp.ccg.disjunctivizer.MatchType.TARGET_PREDICATE_MATCH;
import static opennlp.ccg.disjunctivizer.MatchType.TARGET_PREDICATE_MISMATCH;

import java.util.Collection;

import opennlp.ccg.hylo.graph.LFEdge;
import opennlp.ccg.hylo.graph.LFVertex;


/**
 * A filter that matches vertices based on a basis vertex and a set of match type criteria.
 * <p>
 * Instances of this class use the following match types: {@link MatchType#SOURCE_MATCH}, 
 * {@link MatchType#SOURCE_MISMATCH}, {@link MatchType#TARGET_MATCH}, {@link MatchType#TARGET_MISMATCH},
 * {@link MatchType#SOURCE_PREDICATE_MATCH}, {@link MatchType#SOURCE_PREDICATE_MISMATCH},
 * {@link MatchType#TARGET_PREDICATE_MATCH}, {@link MatchType#TARGET_PREDICATE_MISMATCH}.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class VertexMatchFilter extends MatchTypeFilter {

	LFVertex basis;
	
	/**
	 * Creates a new vertex match filter using the specified vertex as a basis for comparison and the
	 * specified match type criteria.
	 * @throws IllegalArgumentException if <tt>basis</tt> is <tt>null</tt>.
	 * @see #VertexMatchFilter(LFVertex, Collection)
	 */
	public VertexMatchFilter(LFVertex basis, MatchType... matchTypes) {
		super(matchTypes);
		
		checkBasis(basis);
		this.basis = basis;
	}
		
	/**
	 * Creates a new vertex match filter using the specified vertex as a basis for comparison and the
	 * specified match type criteria.
	 * @param basis The vertex to use as a basis for comparison.
	 * @param matchTypes The set of match type criteria to use.
	 * @throws IllegalArgumentException if <tt>basis</tt> is <tt>null</tt>.
	 */
	public VertexMatchFilter(LFVertex basis, Collection<MatchType> matchTypes) {
		super(matchTypes);
		
		checkBasis(basis);
		this.basis = basis;
	}

	private void checkBasis(LFVertex basis) {
		if(basis == null) {
			throw new IllegalArgumentException("basis is null");
		}
	}
	
	/**
	 * Gets the vertex that is the basis for comparison in this filter's {@link #allows(LFEdge)} method.
	 */
	public LFVertex getBasis() {
		return basis;
	}

	/**
	 * Sets the vertex used as a basis for comparison.
	 * @throws IllegalArgumentException if <tt>basis</tt> is <tt>null</tt>.
	 */
	public void setBasis(LFVertex basis) {
		checkBasis(basis);
		this.basis = basis;
	}

	/**
	 * Tests whether a specified edge is allowed based on the match type criteria in effect and the
	 * vertex used as a basis for comparison.
	 * @param edge The edge to test.
	 * @return <tt>false</tt> if {@link #getMatchTypes()} contains
	 * <ul>
	 * 	<li>{@link MatchType#SOURCE_MATCH}, but the basis edge does not equal the edge's source,</li>
	 * 	<li>{@link MatchType#SOURCE_MISMATCH}, but the basis edge is equal to the edge's source,</li>
	 * 	<li>{@link MatchType#TARGET_MATCH}, but the basis edge does not equal the edge's target,</li>
	 * 	<li>{@link MatchType#TARGET_MISMATCH}, but the basis edge is equal to the edge's target,</li>
	 * 	<li>{@link MatchType#SOURCE_PREDICATE_MATCH}, but the basis edge's predicate does not equal the 
	 * 		edge's source vertex's predicate,</li>
	 * 	<li>{@link MatchType#SOURCE_PREDICATE_MISMATCH}, but the basis edge's predicate is equal to the 
	 * 		edge's source vertex's predicate,</li>
	 * 	<li>{@link MatchType#TARGET_PREDICATE_MATCH}, but the basis edge's predicate does not equal the 
	 * 		edge's target vertex's predicate,</li>
	 * 	<li>{@link MatchType#TARGET_PREDICATE_MISMATCH}, but the basis edge's predicate is equal to the 
	 * 		edge's target vertex's predicate,</li>
	 * </ul>
	 * and <tt>true</tt> otherwise.
	 */
	@Override
	public boolean allows(LFEdge edge) {
		for(MatchType t : matchTypes) {
			if(t == SOURCE_MATCH && !basis.equals(edge.getSource())) {
				return false;
			}
			else if(t == SOURCE_MISMATCH && basis.equals(edge.getSource())) {
				return false;
			}
			else if(t == TARGET_MATCH && !basis.equals(edge.getTarget())) {
				return false;
			}
			else if(t == TARGET_MISMATCH && basis.equals(edge.getTarget())) {
				return false;
			}
			else if(basis.getPredicate() != null) {
				if(t == SOURCE_PREDICATE_MATCH 
						&& !basis.getPredicate().equals(edge.getSource().getPredicate())) {
					return false;
				}
				else if(t == SOURCE_PREDICATE_MISMATCH 
						&& basis.getPredicate().equals(edge.getSource().getPredicate())) {
					return false;
				}
				else if(t == TARGET_PREDICATE_MATCH 
						&& !basis.getPredicate().equals(edge.getTarget().getPredicate())) {
					return false;
				}
				else if(t == TARGET_PREDICATE_MISMATCH 
						&& basis.getPredicate().equals(edge.getTarget().getPredicate())) {
					return false;
				}
			}
		}
		
		return true;
	}

}
