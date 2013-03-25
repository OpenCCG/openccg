///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003 Jason Baldridge and University of Edinburgh (Michael White)
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

package opennlp.ccg.hylo;

import opennlp.ccg.unify.*;
import gnu.trove.*;

/**
 * A logical atomic formula.
 * The type is optional, so by default, it is not considered in determining equality.
 *
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.9 $, $Date: 2009/12/21 03:27:19 $
 **/
public abstract class HyloAtom extends HyloFormula {
    
	private static final long serialVersionUID = 1L;
	
	protected String _name;
    protected SimpleType type;

    protected HyloAtom(String name) {
        this(name, null);
    }
    
    protected HyloAtom(String name, SimpleType st) {
        _name = name; type = st;
    }
    
    public void setAtomName(String name) {
        _name = name;
    }

    public String getName() { return _name; } 
    
    public SimpleType getType() { return type; }

    public boolean occurs(Variable var) {
        return false;
    }

    public String toString() { 
        return _name;
    }

    /**
     * Returns a pretty-printed string of this LF, with the given indent.
     */
    public String prettyPrint(String indent) {
        return toString();
    }
    
    public int compareTo(HyloAtom ha) {
        return _name.compareTo(ha._name);
    }

    /** Returns a hash code based on the atom name. */
    public int hashCode() { 
        return _name.hashCode();
    }

    /**
     * Returns whether this atom equals the given object  
     * based on the atom name.
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) { return false; }
        HyloAtom ha = (HyloAtom) obj;
        return _name.equals(ha._name);
    }
    
    /**
     * Returns a hash code using the given map from vars to ints.
     */
    public int hashCode(TObjectIntHashMap varMap) { return hashCode(); }
        
    /**
     * Returns whether this atom equals the given object  
     * up to variable names, using the given maps from vars to ints.
     */
    public boolean equals(Object obj, TObjectIntHashMap varMap, TObjectIntHashMap varMap2) {
        return equals(obj);
    }
}
