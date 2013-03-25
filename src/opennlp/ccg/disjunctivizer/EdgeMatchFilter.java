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

import static opennlp.ccg.disjunctivizer.MatchType.LABEL_MATCH;
import static opennlp.ccg.disjunctivizer.MatchType.LABEL_MISMATCH;
import static opennlp.ccg.disjunctivizer.MatchType.SOURCE_MATCH;
import static opennlp.ccg.disjunctivizer.MatchType.SOURCE_MISMATCH;
import static opennlp.ccg.disjunctivizer.MatchType.SOURCE_PREDICATE_MATCH;
import static opennlp.ccg.disjunctivizer.MatchType.SOURCE_PREDICATE_MISMATCH;
import static opennlp.ccg.disjunctivizer.MatchType.TARGET_MATCH;
import static opennlp.ccg.disjunctivizer.MatchType.TARGET_MISMATCH;
import static opennlp.ccg.disjunctivizer.MatchType.TARGET_PREDICATE_MATCH;
import static opennlp.ccg.disjunctivizer.MatchType.TARGET_PREDICATE_MISMATCH;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

import opennlp.ccg.hylo.graph.LFEdge;
import opennlp.ccg.util.CompositeFilter;
import opennlp.ccg.util.Filter;
import opennlp.ccg.util.InverseFilter;


/**
 * A filter for LF edges based on a set of {@linkplain #getMatchTypes() match type criteria}. Instances of
 * this class compare a specified {@linkplain #getBasis() basis edge} based on their match type criteria.
 * This class extends {@link CompositeFilter}, and the constructors add various filters as members depending on 
 * the criteria in effect.  
 * <p>
 * Edge match filters use the following match types as criteria in addition to the ones used by
 * {@link VertexMatchFilter}: {@link MatchType#LABEL_MATCH}, and {@link MatchType#LABEL_MISMATCH}. 
 * 
 * @see VertexMatchFilter
 * @see LabelMatchFilter
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class EdgeMatchFilter extends CompositeFilter<LFEdge> {
	
	LFEdge basis;
	final EnumSet<MatchType> matchTypes;
	
	/**
	 * Creates a new edge match filter based on the specified edge and criteria.
	 * @see #EdgeMatchFilter(LFEdge, Collection)
	 */
	public EdgeMatchFilter(LFEdge basis, MatchType... matchTypes) {
		this(basis, Arrays.asList(matchTypes));
	}
	
	/**
	 * Creates a new edge match filter based on the specified edge, using the specified
	 * match type criteria. Depending on the criteria, this constructor adds instances of
	 * {@link VertexMatchFilter} and {@link LabelMatchFilter} (or their inverses) to the
	 * set of filters composing it.
	 * 
	 * @param basis The LF edge to use for comparison.
	 * @param matchTypes The comparison criteria, used to populate this composite filter.
	 * 
	 * @throws IllegalArgumentException if <tt>basis</tt> is <tt>null</tt>.
	 */
	public EdgeMatchFilter(LFEdge basis, Collection<MatchType> matchTypes) {
		super();
		
		checkBasis(basis);
		this.basis = basis;
		this.matchTypes = EnumSet.copyOf(matchTypes);
				
		for(MatchType t : matchTypes) {
			Filter<LFEdge> f = null;
			
			if(t == LABEL_MATCH || t == LABEL_MISMATCH) {
				f = new LabelMatchFilter(basis.getLabel());
				if(t == LABEL_MISMATCH) {
					f = new InverseFilter<LFEdge>(f);
				}
			}
			else if(t == SOURCE_MATCH || t == SOURCE_MISMATCH
					|| t == SOURCE_PREDICATE_MATCH || t == SOURCE_PREDICATE_MISMATCH) {
				f = new VertexMatchFilter(basis.getSource(), t);
			}
			else if(t == TARGET_MATCH || t == TARGET_MISMATCH
					|| t == TARGET_PREDICATE_MATCH || t == TARGET_PREDICATE_MISMATCH) {
				f = new VertexMatchFilter(basis.getTarget(), t);
			}
			
			if(f != null) {
				addFilter(f);
			}
		}
	}
	
	private void checkBasis(LFEdge basis) {
		if(basis == null) {
			throw new IllegalArgumentException("basis is null");
		}
	}
	
	/**
	 * Gets the edge that comparisons are based on.
	 */
	public LFEdge getBasis() {
		return basis;
	}

	/**
	 * Sets the edge used for comparisons.
	 * @throws IllegalArgumentException if <tt>basis</tt> is <tt>null</tt>.
	 */
	public void setBasis(LFEdge basis) {
		checkBasis(basis);
		this.basis = basis;
	}

	/**
	 * Gets the match type criteria used by this edge match filter.
	 */
	public EnumSet<MatchType> getMatchTypes() {
		return matchTypes;
	}
}
