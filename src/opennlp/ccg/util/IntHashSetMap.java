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

import gnu.trove.*;

/**
 * A map from ints to sets which allows objects with the same key to be
 * added without overriding previous puts.
 *
 * @author      Jason Baldridge
 * @author      Gann Bierner
 * @author      Michael White
 * @version     $Revision: 1.6 $, $Date: 2005/10/13 20:33:49 $
 */
public class IntHashSetMap extends TIntObjectHashMap {

	private static final long serialVersionUID = 1L;

	/** Adds the given key-value pair to the map. */
    public Object put(int key, Object value) {
    	THashSet val = (THashSet) get(key);
        if (val==null) {
            val = new THashSet();
            val.add(value);
            super.put(key, val); 
        } else {
            val.add(value);
        }
        return val;
    }
}
