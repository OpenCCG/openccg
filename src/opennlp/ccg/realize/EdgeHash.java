///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2005 Michael White
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

package opennlp.ccg.realize;

import gnu.trove.*;
import java.util.*;

/**
 * A set of edges, unique up to surface words.
 * Edges with higher scores or whose signs have lower derivational complexity are kept during insertion.
 *
 * @author      Michael White
 * @version     $Revision: 1.2 $, $Date: 2010/01/14 22:52:01 $
 */
public class EdgeHash extends THashSet {

	private static final long serialVersionUID = 1L;
	
	/** Hashing strategy that uses Edge's surfaceWordHashCode and surfaceWordEquals methods. */
    protected static TObjectHashingStrategy surfaceWordHashingStrategy = new TObjectHashingStrategy() {
		private static final long serialVersionUID = 1L;
		public int computeHashCode(java.lang.Object o) {
            return ((Edge)o).surfaceWordHashCode();
        }
        public boolean equals(java.lang.Object o1, java.lang.Object o2) {
            return ((Edge)o1).surfaceWordEquals((Edge)o2);
        }
    };

    /** Default constructor. */
    public EdgeHash() { super(surfaceWordHashingStrategy); }

    /**
     * Returns this as a set of edges.
     */
    @SuppressWarnings("unchecked")
	public Set<Edge> asEdgeSet() { return (Set<Edge>) this; }

    /**
     * Adds an edge, keeping the one with a higher score or whose sign has lower derivational complexity 
     * if there is an equivalent one there already; returns the old
     * edge if it was displaced, the new edge if there was no equivalent 
     * old edge, or null if the edge was not actually added.
     */
    public Edge insert(Edge edge) {
        int pos = index(edge);
        // equiv edge
        if (pos >= 0) {
            Edge oldEdge = (Edge) _set[pos];
            // already there?
            if (oldEdge == edge) return null;
            // check score
            if (edge.score > oldEdge.score) { 
            	_set[pos] = edge; return oldEdge;
            }
            // check complexity
            int complexity = edge.sign.getDerivationHistory().complexity();
            int oldComplexity = oldEdge.sign.getDerivationHistory().complexity();
            if (complexity < oldComplexity) {
            	_set[pos] = edge; return oldEdge;
            }
            // otherwise toss
            else return null;
        }
        // add new
        else {
        	add(edge); return edge;
        }
    }
}
