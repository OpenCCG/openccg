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

package opennlp.ccg.util;

import java.io.Serializable;
import java.util.*;

/**
 * An unmodifiable list of one element.
 * Equality is checked using == on the list element, which 
 * is assumed to be canonical.
 *
 * @author      Michael White
 * @version     $Revision: 1.5 $, $Date: 2009/12/21 03:27:18 $
 */
public class SingletonList<T> extends AbstractList<T> implements Serializable {
    
	private static final long serialVersionUID = -4340168177098319085L;
	
	/** The single list element. */
    public final T elt;

    /** Constructor. */
    public SingletonList(T elt) {
        this.elt = elt;
    }
    
    /** Returns the size of this list. */
    public int size() { return 1; }
    
    /** Returns the ith element of the list. */
    public T get(int i) {
        if (i == 0) return elt;
        else throw new IndexOutOfBoundsException("No element with index: " + i);
    }
    
    /** Returns a hash code for this list, using the identity hash code of the list element. */
    public int hashCode() {
        return 31 + System.identityHashCode(elt);
    }
    
    /** Returns whether this list equals the given object, using identity tests on the list element. */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof List<?>)) return false;
        List<?> list = (List<?>) obj;
        if (size() != list.size()) return false;
        return (get(0) == list.get(0));
    }
}
