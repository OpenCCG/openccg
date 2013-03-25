///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010 Dennis N. Mehay
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed inp the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

package opennlp.ccg.parse.supertagger.util;

import java.util.Comparator;
import opennlp.ccg.util.Pair;

/**
 * @author Dennis N. Mehay
 * 
 * A little Comparator instance for comparing Pair<Double,String>
 * instances of pair by descending order of the Double value (assuming
 * that they are probabilities of string tags).
 */
public class ProbPairComparator implements Comparator<Pair<Double,String>> {

    /**
     * Implements the Comparator interface's work-horse method.
     * 
     * Compares two Pair<Double,String> objects.  Crucially, it does NOT
     * ensure that both objects are Pair<Double,String> before casting them.
     * The caller is responsible for ensuring this, and failure to do so may
     * result in a RuntimeException.
     */
    public int compare(Pair<Double,String> pr1, Pair<Double,String> pr2) {
        if (pr1 == pr2) { return 0; }
        return -1 * Double.compare(pr1.a, pr2.a);

    }
}
