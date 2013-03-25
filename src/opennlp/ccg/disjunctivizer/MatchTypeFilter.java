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

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

import opennlp.ccg.hylo.graph.LFEdge;
import opennlp.ccg.util.Filter;

/**
 * Abstract class for filters that allow {@link LFEdge}s based on criteria indicated by the set of
 * {@linkplain #getMatchTypes() match types} they use. 
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 *
 */
public abstract class MatchTypeFilter implements Filter<LFEdge> {
	
	/**
	 * The set of match types used as criteria by this filter.
	 */
	protected final EnumSet<MatchType> matchTypes;

	/**
	 * Creates a new match type filter based on the specified match types.
	 * @see #MatchTypeFilter(Collection)
	 */
	protected MatchTypeFilter(MatchType... matchTypes) {
		this(Arrays.asList(matchTypes));
	}
	
	/**
	 * Creates a new match type filter based on the specified match types.
	 * @param matchTypes The collection of match types to use. The specified collection is 
	 * copied via {@link EnumSet#copyOf(Collection)}.
	 */
	protected MatchTypeFilter(Collection<MatchType> matchTypes) {
		this.matchTypes = EnumSet.copyOf(matchTypes);
	}

	/**
	 * Gets the match types used by this match type filter.
	 */
	public EnumSet<MatchType> getMatchTypes() {
		return matchTypes;
	}

}
