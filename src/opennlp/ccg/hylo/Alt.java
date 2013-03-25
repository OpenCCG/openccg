///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2005-6 Michael White (University of Edinburgh, The Ohio State University)
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

package opennlp.ccg.hylo;

import java.io.Serializable;
import java.util.BitSet;

/**
 * Class for representing alts.
 * LF alts are used during realization to represent 
 * exclusive disjunctions in the input.
 * The alts are represented as pairs of ints, 
 * one for the alt set and one for the alt within the set.
 * The alts are numbered starting with 0.
 * An alt has a bitset for the elementary predications 
 * within the alt.
 *
 * @author      Michael White
 * @version     $Revision: 1.6 $, $Date: 2009/07/17 04:23:30 $
 */
public final class Alt implements Comparable<Alt>, Serializable {
    
	private static final long serialVersionUID = 7241395629445814238L;
	
	/** The alt set number. */
    public final int altSet;
    /** The alt within the set. */
    public final int numInSet;
    /** The bitset. */
    public final BitSet bitset = new BitSet();
    
    /** Constructor. */
    public Alt(int altSet, int numInSet) { 
        this.altSet = altSet; this.numInSet = numInSet;
    }
    
    /** Equals. */
    public boolean equals(Object o) {
        if (!(o instanceof Alt)) return false;
        Alt a = (Alt) o;
        return altSet == a.altSet && numInSet == a.numInSet;
    }
    /** Comparison. */
    public int compareTo(Alt a) {
        if (altSet < a.altSet) return -1;
        if (altSet == a.altSet && numInSet < a.numInSet) return -1;
        if (altSet == a.altSet && numInSet == a.numInSet) return 0;
        return 1;
    }
}
