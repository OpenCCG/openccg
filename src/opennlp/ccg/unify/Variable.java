///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002-7 Jason Baldridge, Gann Bierner and Michael White
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

package opennlp.ccg.unify;

import gnu.trove.TObjectIntHashMap;

/**
 * A variable that can stand for some class of Unifiable objects.
 *
 * @author      Gann Bierner
 * @author 		Michael White
 * @version     $Revision: 1.2 $, $Date: 2007/12/20 21:30:22 $
 **/
public interface Variable extends Unifiable {

    /**
     * Returns the name of this variable.
     *
     * @return the variable's name
     **/        
    public String name();

    /**
	 * Returns a hash code using the given map from vars to ints.
	 */
	public int hashCode(TObjectIntHashMap varMap);
        
    /**
	 * Returns whether this var equals the given object up to variable names,
	 * using the given maps from vars to ints.
	 */
    public boolean equals(Object obj, TObjectIntHashMap varMap, TObjectIntHashMap varMap2);
}
