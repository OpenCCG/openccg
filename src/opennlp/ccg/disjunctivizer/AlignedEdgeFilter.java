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

import static opennlp.ccg.disjunctivizer.MatchType.SOURCE_ALIGNED;
import static opennlp.ccg.disjunctivizer.MatchType.SOURCE_UNALIGNED;
import static opennlp.ccg.disjunctivizer.MatchType.TARGET_ALIGNED;
import static opennlp.ccg.disjunctivizer.MatchType.TARGET_UNALIGNED;

import java.util.Collection;
import java.util.Set;

import opennlp.ccg.hylo.graph.LFEdge;
import opennlp.ccg.hylo.graph.LFVertex;


/**
 * A filter for edges that tests whether they are aligned based on a specified set of 
 * {@linkplain #getAlignmentIndices() alignment indices}. Whether the source or target vertices
 * (or both) is considered depends on the match type criteria in effect. For example, if the match
 * type criteria contains {@link MatchType#SOURCE_ALIGNED}, this filter's {@link #allows(LFEdge)} method
 * will check whether argument edges have a {@linkplain LFVertex#getIndex() source index} that is 
 * contained in the set of alignment indices. 
 * <p>
 * Instances of this class use the following match type criteria: {@link MatchType#SOURCE_ALIGNED},
 * {@link MatchType#SOURCE_UNALIGNED}, {@link MatchType#TARGET_ALIGNED}, 
 * and {@link MatchType#TARGET_UNALIGNED}. If the set of alignment indices is modified after an instance
 * of this class is created, the filter will reflect the changes because the set is not copied at
 * creation.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class AlignedEdgeFilter extends MatchTypeFilter {

	Set<Integer> alignmentIndices;
		
	/**
	 * Creates a new aligned edge filter based on the specified alignment indices for the specified
	 * match type criteria.
	 * @param alignmentIndices The set of indices to check for alignment.
	 * @param matchTypes The match type criteria to use.
	 * @throws IllegalArgumentException If <tt>alignmentIndices</tt> is <tt>null</tt>.
	 */
	public AlignedEdgeFilter(Set<Integer> alignmentIndices, MatchType... matchTypes) {
		super(matchTypes);
		
		checkAlignmentIndices(alignmentIndices);
		this.alignmentIndices = alignmentIndices;
	}

	/**
	 * Creates a new aligned edge filter based on the specified alignment indices for the specified
	 * match type criteria.
	 * @param alignmentIndices The set of indices to check for alignment.
	 * @param matchTypes The match type criteria to use.
	 * @throws IllegalArgumentException If <tt>alignmentIndices</tt> is <tt>null</tt>.
	 */
	public AlignedEdgeFilter(Set<Integer> alignmentIndices, Collection<MatchType> matchTypes) {
		super(matchTypes);
		
		checkAlignmentIndices(alignmentIndices);
		this.alignmentIndices = alignmentIndices;
	}
	
	private void checkAlignmentIndices(Set<Integer> alignmentIndices) {
		if(alignmentIndices == null) {
			throw new IllegalArgumentException("alignmentIndices is null");
		}
	}

	/**
	 * Gets the alignment indices used by this filter.
	 */
	public Set<Integer> getAlignmentIndices() {
		return alignmentIndices;
	}

	/**
	 * Sets the alignment indices used by this filter.
	 * @throws IllegalArgumentException If <tt>alignmentIndices</tt> is <tt>null</tt>.
	 */
	public void setAlignmentIndices(Set<Integer> alignmentIndices) {
		checkAlignmentIndices(alignmentIndices);	
		this.alignmentIndices = alignmentIndices;
	}

	/**
	 * Tests whether this filter allows the specified LF edge.
	 * @return <tt>false</tt> if {@link #getMatchTypes()} contains
	 * <ul>
	 * 	<li>{@link MatchType#SOURCE_ALIGNED}, but the alignment indices does not contain the edge's
	 * 		source vertex's index,</li>
	 * 	<li>{@link MatchType#SOURCE_UNALIGNED}, but the alignment indices contains the edge's
	 * 		source vertex's index,</li>
	 * 	<li>{@link MatchType#TARGET_ALIGNED}, but the alignment indices does not contain the edge's
	 * 		target vertex's index,</li>
	 * 	<li>{@link MatchType#TARGET_UNALIGNED}, but the alignment indices contains the edge's
	 * 		target vertex's index,</li>
	 * </ul>
	 * and <tt>true</tt> otherwise.
	 * 
	 * @see #getAlignmentIndices()
	 * @see LFEdge#getSource()
	 * @see LFEdge#getTarget()
	 * @see LFVertex#getIndex()
	 */
	@Override
	public boolean allows(LFEdge edge) {
		for(MatchType t : matchTypes) {
			if(t == SOURCE_ALIGNED && !alignmentIndices.contains(edge.getSource().getIndex())) {
				return false;
			}
			else if(t == SOURCE_UNALIGNED && alignmentIndices.contains(edge.getSource().getIndex())) {
				return false;
			}
			else if(t == TARGET_ALIGNED && !alignmentIndices.contains(edge.getTarget().getIndex())) {
				return false;
			}
			else if(t == TARGET_UNALIGNED && alignmentIndices.contains(edge.getTarget().getIndex())) {
				return false;
			}
		}
		
		return true;
	}

}
