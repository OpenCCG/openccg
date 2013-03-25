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

import opennlp.ccg.grammar.*;
import opennlp.ccg.hylo.*;
import java.util.*;
//import java.util.prefs.*;
//import java.text.*;

/**
 * A rule instance is a tracker for an instantiated version of a type changing 
 * rule, ie a type changing rule with its semantics instantiated together with 
 * bitsets representing its coverage of the input predicates 
 * and the indices in its arg category, along with lists of the active LF alts.
 * Such rule instances are created and managed by an EdgeFactory.
 * The design follows the Singleton pattern.
 *
 * @author      Michael White
 * @version     $Revision: 1.7 $, $Date: 2005/11/24 03:22:08 $
 */
public class RuleInstance extends Tracker
{
    /** The instantiated type changing rule. */
    public final TypeChangingRule rule;
    
    /** Constructor. */
    public RuleInstance(TypeChangingRule rule, BitSet bitset, BitSet indices, List<List<Alt>> activeLfAlts) {
        super(bitset, indices, activeLfAlts);
        this.rule = rule;
    }

    /** Returns '{bitset} name: arg => result'. */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        //sb.append(indices + " ");
        sb.append(bitset + " ");
        sb.append(rule);
        return sb.toString();
    }
}
    
