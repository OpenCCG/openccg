///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2005-7 Michael White
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

package opennlp.ccg.parse;

import gnu.trove.*;
import java.util.*;

/**
 * A set of edges, unique up to surface words.
 * Edges whose signs have lower derivational complexity are kept during insertion.
 * NB: This is just like EdgeHash in the realize package, except that 
 *     it deals with parse edges.
 *
 * @author      Michael White
 * @version     $Revision: 1.1 $, $Date: 2007/12/20 05:51:10 $
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
     * Adds an edge, keeping the one whose sign has lower derivational complexity 
     * if there is an equivalent one there already; returns the old
     * edge if it was displaced, the new edge if there was no equivalent 
     * old edge, or null if the edge was not actually added.
     * iff the edge is actually inserted.
     */
    public Edge insert(Edge edge) {
        int pos = index(edge);
        if (pos >= 0) {
            Edge oldEdge = (Edge) _set[pos];
            if (oldEdge == edge) return null; 
            int complexity = edge.sign.getDerivationHistory().complexity();
            int oldComplexity = oldEdge.sign.getDerivationHistory().complexity();
            if (complexity < oldComplexity) {
            	_set[pos] = edge; return oldEdge;
            }
            else return null;
        }
        else {
        	add(edge); return edge;
        }
    }
}
