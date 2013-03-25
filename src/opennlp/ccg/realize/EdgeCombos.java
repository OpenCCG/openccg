///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2005 University of Edinburgh (Michael White)
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

import java.util.*;

/**
 * EdgeCombos is a simple data structure that allows a representative edge 
 * to collect the info about successful combinations of the edge (with 
 * other edges or unary rules) that is needed to create analogous new result edges 
 * for edges that share the same category, without having to invoke 
 * the combinatory rules.
 *
 * @author      Michael White
 * @version     $Revision: 1.3 $, $Date: 2005/10/13 18:20:30 $
 */
public class EdgeCombos
{
    
    /** Info for a collected category combination. */
    public class CatCombo {
        /** The (other) input edge for the category combo. */
        public final Edge inputEdge;
        /** The result edge, to use in making alternative edges. */
        public final Edge resultEdge;
        /** Constructor */
        public CatCombo(Edge inputEdge, Edge resultEdge) {
            this.inputEdge = inputEdge; this.resultEdge = resultEdge; 
        }
    }
    
    /** The rightward combos. */
    public final List<CatCombo> rightwardCombos = new ArrayList<CatCombo>(5);
    
    /** The leftward combos. */
    public final List<CatCombo> leftwardCombos = new ArrayList<CatCombo>(5);
    
    /** The unary results. */
    public final List<Edge> unaryResults = new ArrayList<Edge>(3);
    
    /** The optional results, ie with optional parts marked as completed. */
    public final List<Edge> optionalResults = new ArrayList<Edge>(3);
    
    
    /** Adds a rightward combo. */
    public void addRightwardCombo(Edge inputEdge, Edge resultEdge) {
        rightwardCombos.add(new CatCombo(inputEdge, resultEdge));
    }
    
    /** Adds a leftward combo. */
    public void addLeftwardCombo(Edge inputEdge, Edge resultEdge) {
        leftwardCombos.add(new CatCombo(inputEdge, resultEdge));
    }
}

