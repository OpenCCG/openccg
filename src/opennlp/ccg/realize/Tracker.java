///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-5 University of Edinburgh (Michael White)
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

import opennlp.ccg.hylo.*;
import java.util.*;

/**
 * A tracker is a wrapper for either a sign (ie, an edge) or 
 * a type changing rule (ie, a rule instance) which has bitsets for 
 * representing the coverage of the input predicates and the semantic indices used.
 * It also has a list of active LF alts.
 * Trackers are created by an EdgeFactory.
 * The design follows the Singleton pattern.
 *
 * @author      Michael White
 * @version     $Revision: 1.9 $, $Date: 2005/11/24 03:15:26 $
 */
public class Tracker
{
    /** The coverage bitset. */
    public final BitSet bitset;
    
    /** The indices bitset. */
    public final BitSet indices;
    
    /** The active LF alts. */
    public final List<List<Alt>> activeLfAlts;
    
    /** Constructor. */
    public Tracker(BitSet bitset, BitSet indices, List<List<Alt>> activeLfAlts) {
        this.bitset = bitset; this.indices = indices; this.activeLfAlts = activeLfAlts;
    }
    
    /** 
     * Returns whether the coverage bitset of this tracker intersects with the 
     * coverage bitset of the given one.
     */
    public boolean intersects(Tracker tracker) {
        return bitset.intersects(tracker.bitset);
    }

    /** 
     * Returns whether the indices bitset of this tracker intersects with the 
     * indices bitset of the given one, if both non-empty; otherwise, returns 
     * true (if either this tracker or the given one has no indices).
     */
    public boolean indicesIntersect(Tracker tracker) {
        return indices.isEmpty() || tracker.indices.isEmpty() || 
            indices.intersects(tracker.indices);
    }
}
