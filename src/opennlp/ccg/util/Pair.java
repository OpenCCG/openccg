///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003 Jason Baldridge, Gann Bierner and 
//                    University of Edinburgh (Michael White)
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

package opennlp.ccg.util;

import java.io.Serializable;

/**
 * Dinky class to package pairs of things.
 *
 * @author      Jason Baldridge
 * @author      Gann Bierner
 * @author      Michael White
 * @version     $Revision: 1.7 $, $Date: 2009/12/21 03:27:18 $
 */
public final class Pair<TypeA,TypeB> implements Serializable {
    
	private static final long serialVersionUID = 3626104184233533389L;

	/** The first element of the pair. */
    public final TypeA a; 

    /** The second element of the pair. */
    public final TypeB b;

    /** Constructor. */
    public Pair(TypeA a, TypeB b) {
        this.a = a; this.b = b; 
    }
    
    /** Returns a hash code constructed from those of a and b. */
    public int hashCode() { return a.hashCode() - b.hashCode(); }
    
    /** Returns true if the given object pairs the same elements. */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Pair<?, ?>)) return false;
        Pair<?, ?> p = (Pair<?, ?>) obj;
        return a.equals(p.a) && b.equals(p.b);
    }

    /** Returns "[a/b]". */
    public String toString() { return "["+a+"/"+b+"]"; }
}
