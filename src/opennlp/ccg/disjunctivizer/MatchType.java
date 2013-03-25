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
import opennlp.ccg.hylo.graph.LFVertex;

/**
 * A set of enum constants for identifying the matching criteria used by a {@link MatchTypeFilter}.
 * Not all match type filters will use all of the match type criteria contained in this enum.
 * 
 * @see LFVertex
 * @see LFEdge
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public enum MatchType {
	/**
	 * Matching source vertices.
	 */
	SOURCE_MATCH,
	
	/**
	 * Matching target vertices.
	 */
	TARGET_MATCH,
	
	/**
	 * Matching edge labels.
	 */
	LABEL_MATCH,
	
	/**
	 * Matching predicates for source vertices.
	 */
	SOURCE_PREDICATE_MATCH,
	
	/**
	 * Matching predicates for target vertices.
	 */
	TARGET_PREDICATE_MATCH,
	
	/**
	 * Mismatching source vertices.
	 */
	SOURCE_MISMATCH,
	
	/**
	 * Mismatching target vertices.
	 */
	TARGET_MISMATCH,
	
	/**
	 * Mismatching edge labels.
	 */
	LABEL_MISMATCH,
	
	/**
	 * Mismatching source vertex predicates.
	 */
	SOURCE_PREDICATE_MISMATCH,
	
	/**
	 * Mismatching target vertex predicates.
	 */
	TARGET_PREDICATE_MISMATCH,
	
	/**
	 * Source vertex is aligned.
	 */
	SOURCE_ALIGNED,
	
	/**
	 * Source vertex is not aligned.
	 */
	SOURCE_UNALIGNED,
	
	/**
	 * Target vertex is aligned.
	 */
	TARGET_ALIGNED,
	
	/**
	 * Target vertex is unaligned.
	 */
	TARGET_UNALIGNED
}
