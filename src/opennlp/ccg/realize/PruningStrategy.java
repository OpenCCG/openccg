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

import java.util.*;

/**
 * Interface for edge pruning strategies.
 *
 * @author      Michael White
 * @version     $Revision: 1.4 $, $Date: 2011/03/27 14:45:32 $
 */
public interface PruningStrategy
{
    /**
     * Prunes and returns a (possibly empty) list of edges 
     * from the given ones, which should be sorted by score, 
     * from highest to lowest.
     */
    public List<Edge> pruneEdges(List<Edge> catEdges);
}

