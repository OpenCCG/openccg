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

import java.io.Serializable;
import java.util.*;

/**
 * An unmodifiable list formed by sequencing two sublists. 
 * The sublists are assumed to remain unchanged as well.
 * Equality is checked using == on the list elements, which 
 * are assumed to be canonical.
 *
 * @author      Michael White
 * @version     $Revision: 1.7 $, $Date: 2009/12/21 03:27:18 $
 */
public class StructureSharingList<T> extends AbstractList<T> implements Serializable {
    
	private static final long serialVersionUID = 6692080357319326492L;

	/** The first sublist. */
    public final List<T> first; 

    /** The second sublist. */
    public final List<T> second;
    
    // cached hashcode
    private int hashcode = -1;

    // size
    private final int size;
    
    /** Constructor. */
    public StructureSharingList(List<T> first, List<T> second) {
        this.first = first; this.second = second;
        this.size = first.size() + second.size();
    }
    
    /** Returns the size of this list. */
    public int size() { return size; }
    
    /** Returns the ith element of the list. */
    public T get(int i) {
        if (i < first.size()) { 
            return first.get(i); 
        }
        else {
            return second.get(i - first.size());
        }
    }
    
    /** Returns a hash code for this list, using identity hash codes of the list elements. */
    public int hashCode() {
        // check whether already cached
        if (hashcode != -1) return hashcode;
        int hc = 1;
        for (int i = 0; i < size(); i++) {
            hc = 31*hc + System.identityHashCode(get(i));
        }
        // cache then return
        hashcode = hc;
        return hc;
    }
    
    /** Returns whether this list equals the given object, using identity tests on the list elements. */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof List<?>)) return false;
        List<?> list = (List<?>) obj;
        if (list instanceof StructureSharingList<?>) {
            StructureSharingList<?> ssl = (StructureSharingList<?>) list;
            if (first == ssl.first && second == ssl.second) return true;
        }
        if (size() != list.size()) return false;
        for (int i = 0; i < size(); i++) {
            if (get(i) != list.get(i)) return false;
        }
        return true; 
    }
}
