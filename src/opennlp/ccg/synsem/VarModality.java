///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002-7 Jason Baldridge and Michael White
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
package opennlp.ccg.synsem;

import java.io.Serializable;
import gnu.trove.TObjectIntHashMap;

import opennlp.ccg.unify.*;

/**
 * A class for variables which can stand for slash modalities.
 *
 * @author      Jason Baldridge
 * @author		Michael White
 * @version     $Revision: 1.4 $, $Date: 2009/07/17 04:23:30 $
 **/
public class VarModality implements Variable, Indexed, Mutable, Modality, Serializable {
    
	private static final long serialVersionUID = 7465785777802095802L;
	
	protected final String _name;
    protected int _index;
    protected int _hashCode;
    
    private static int UNIQUE_STAMP = 0;
    
    public VarModality() {
        this("VM"+UNIQUE_STAMP++);
    }
    
    public VarModality(String name) {
        this(name, 0);
    }

    protected VarModality(String name, int index) {
        _name = name;
        _index = index;
        _hashCode = _name.hashCode() + _index;
    }
    
    public String name() {
        return _name;
    }

    public Object copy() {
        return new VarModality(_name, _index);
    }
    
    public void deepMap(ModFcn mf) {
        mf.modify(this);
    }
    
    public int getIndex() {
        return _index;
    }

    public void setIndex(int index) {
        _hashCode += index - _index;
        _index = index;
    }

    public boolean occurs(Variable var) {
        return equals(var);
    }

    public int hashCode() {
        return _hashCode;
    }
    
    public boolean equals(Object o) {
    	if (this == o) return true;
        if (!(o instanceof VarModality)) return false;
        VarModality vm = (VarModality) o;
        return _index == vm._index && _name.equals(vm._name);
    }

    /**
	 * Returns a hash code using the given map from vars to ints.
	 */
	public int hashCode(TObjectIntHashMap varMap) {
		// see if this already in map
		if (varMap.containsKey(this))
			return varMap.get(this);
		// otherwise add it
		int next = varMap.size() + 1;
		varMap.put(this, next);
		return next;
	}
        
    /**
	 * Returns whether this var equals the given object up to variable names,
	 * using the given maps from vars to ints.
	 */
    public boolean equals(Object obj, TObjectIntHashMap varMap, TObjectIntHashMap varMap2) {
        if (this == obj) return true;
        if (obj.getClass() != this.getClass()) { return false; }
        VarModality vm = (VarModality) obj;
        if (varMap.get(this) != varMap2.get(vm)) return false;
        return true;
    }
    
    public void unifyCheck(Object o) throws UnifyFailure {
        if (!(o instanceof SlashMode || o instanceof VarModality)) {
            throw new UnifyFailure();
        }
    }
    
    public Object unify(Object u, Substitution sub) throws UnifyFailure {
        if (u instanceof SlashMode) {
            return sub.makeSubstitution(this, u);    
        } else if (u instanceof VarModality) {
            VarModality var2 = (VarModality)u;
            Variable $var = new VarModality(_name+var2._name,
                            UnifyControl.getUniqueVarIndex());
            
            sub.makeSubstitution(this, $var);
            sub.makeSubstitution(var2, $var);
            return $var;
        } else {
            throw new UnifyFailure();
        }
    }

    public Object fill(Substitution sub) throws UnifyFailure {
        Object val = sub.getValue(this);
        if (val != null) {
            return val;
        } else {
            return this;
        }
    }

    public byte getDirection() {
        return Slash.B;
    }
    
    public String toString(byte dir) { 
        return toString();
    }

    public String toString() { 
        return _name;
    }
    
    public String toTeX(byte dir) {    
        return toTeX();
    }

    public String toTeX() {    
        return  _name;
    }
}
