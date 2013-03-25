///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2005 University of Edinburgh (Michael White)
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

package opennlp.ccg.lexicon;

import opennlp.ccg.util.*;
// import java.util.*;

/** 
 * Factor key, with identity equals for the factor and value. 
 *
 * @author      Michael White
 * @version     $Revision: 1.1 $, $Date: 2005/03/22 20:59:32 $
 */
public class FactorKey {

    /** The factor. */
    public final String factor;

    /** The value. */
    public final String val;

    /** Constructor.  Assumes interned components. */
    private FactorKey(String factor, String val) { this.factor = factor; this.val = val; }

    /** Makes/retrieves an interned factor key for the given interned attr and val; 
        for the word form, the string itself is returned. 
        Null vals are replaced with &lt;NULL&gt;. */
    public static Object getKey(String attr, String val) {
        if (val == null) val = "<NULL>";
        if (attr == Tokenizer.WORD_ATTR) return val;
        else return Interner.globalIntern(new FactorKey(attr, val));
    }

    /** Returns a hash code constructed from the component identity hash codes. */
    public int hashCode() { 
        return System.identityHashCode(factor) - System.identityHashCode(val);
    }

    /** Returns true if the given factor key has identical components. */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FactorKey)) return false;
        FactorKey key = (FactorKey) obj;
        return factor == key.factor && val == key.val;
    }

    /** Returns "factor-val". */
    public String toString() { return factor + "-" + val; }
}    

