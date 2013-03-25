///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-4 University of Edinburgh (Michael White)
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

import java.util.*;

/**
 * An array list where equality is checked using == on the list elements, which 
 * are assumed to be canonical.
 * The hashCode method is compatible with SingletonList and StructureSharingList.
 *
 * @author      Michael White
 * @version     $Revision: 1.4 $, $Date: 2009/12/21 03:27:18 $
 */
public class ArrayListWithIdentityEquals<T> extends ArrayList<T> {
    
	private static final long serialVersionUID = 1L;

	/** Default constructor. */
    public ArrayListWithIdentityEquals() {}
    
    /** Constructor with initial collection. */
    public ArrayListWithIdentityEquals(Collection<T> c) { super(c); }
    
    /** Constructor with initial capacity. */
    public ArrayListWithIdentityEquals(int initialCapacity) { super(initialCapacity); }
    
    
    /** Returns a hash code for this list, using identity hash codes of the list elements. */
    public int hashCode() {
        int hc = 1;
        for (int i = 0; i < size(); i++) {
            hc = 31*hc + System.identityHashCode(get(i));
        }
        return hc;
    }
    
    /** Returns whether this list equals the given object, using identity tests on the list elements. */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof List<?>)) return false;
        List<?> list = (List<?>) obj;
        if (size() != list.size()) return false;
        for (int i = 0; i < size(); i++) {
            if (get(i) != list.get(i)) return false;
        }
        return true; 
    }
}
