///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004 University of Edinburgh (Michael White)
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

import opennlp.ccg.TextCCG;

import java.util.*;
import java.util.prefs.*;

/**
 * Default, n-best edge pruning strategy.
 *
 * @author      Michael White
 * @version     $Revision: 1.9 $, $Date: 2011/03/27 14:45:32 $
 */
public class NBestPruningStrategy implements PruningStrategy
{
    /** The current pruning val. */
    protected int CAT_PRUNE_VAL;
    
    /** Reusable return list. */
    protected List<Edge> retval = new ArrayList<Edge>();
    
    /** Constructor with pruning val. */
    public NBestPruningStrategy(int pruningVal) {
        CAT_PRUNE_VAL = pruningVal;
    }
    
    /** Default constructor retrieves pruning val from preferences. */
    public NBestPruningStrategy() {
        Preferences prefs = Preferences.userNodeForPackage(TextCCG.class);
        CAT_PRUNE_VAL = prefs.getInt(Chart.PRUNING_VALUE, Chart.NO_PRUNING);
    }
    
    /**
     * Returns a (possibly empty) list of edges pruned 
     * from the given ones, which should be sorted by score, 
     * from highest to lowest. 
     * In particular, prunes and returns the edges that follow the N-best 
     * ones in the given list.
     */
    public List<Edge> pruneEdges(List<Edge> catEdges) {
        // clear reusable return list
        retval.clear();
        // ensure pruning enabled
        if (CAT_PRUNE_VAL == Chart.NO_PRUNING) return retval;
        // nb: could add an option to prune all egdes with zero score
        /*
        for (Iterator it = catEdges.iterator(); it.hasNext(); ) {
            Edge edge = it.next();
            if (edge.score == 0) {
                retval.add(edge);
                it.remove();
            }
        }
        */
        // return edges at bottom of list, starting with CAT_PRUNE_VAL (if any)
        while (CAT_PRUNE_VAL < catEdges.size()) {
            retval.add(catEdges.remove(CAT_PRUNE_VAL));
        }
        // done
        return retval;
    }
}

