///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-4 Jason Baldridge and University of Edinburgh (Michael White)
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

import opennlp.ccg.grammar.*;
import opennlp.ccg.synsem.*;
import opennlp.ccg.unify.*;
import org.jdom.*;
import gnu.trove.*;

/**
 * A class for objects which can stand for any HyloFormula object.
 * Types are unified with other hylo vars, and with other hylo formulas when present.
 *
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.17 $, $Date: 2009/07/17 04:23:30 $
 **/
public class HyloVar extends HyloFormula implements Variable, Indexed {
    
	private static final long serialVersionUID = 3455577234911944031L;
	
	protected final String _name;
    protected int _index;
    protected int _hashCode;
    protected SimpleType type;
    
    public HyloVar(String name) {
        this(name, 0, null);
    }

    public HyloVar(String name, SimpleType st) {
        this(name, 0, st);
    }

    protected HyloVar(String name, int index, SimpleType st) {
        _name = name;
        _index = index;
        type = (st != null) ? st : Grammar.theGrammar.types.getSimpleType(Types.TOP_TYPE);
        _hashCode = _name.hashCode() + _index + type.getIndex();
    }
    
    public String name() {
        return _name;
    }

    public LF copy() {
        return new HyloVar(_name, _index, type);
    }


    public int getIndex() {
        return _index;
    }

    public void setIndex(int index) {
        _hashCode += index - _index;
        _index = index;
    }

    public SimpleType getType() {
        return type;
    }
    
    public boolean occurs(Variable var) {
        return equals(var);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HyloVar)) return false;
        HyloVar var = (HyloVar) o;
        return _index == var._index && _name.equals(var._name) && type.equals(var.type);
    }
    
    public int compareTo(HyloVar hv) {
        int retval = _name.compareTo(hv._name);
        if (retval == 0) {
            if (_index < hv._index) { retval = -1; }
            else if (_index > hv._index) { retval = 1; }
        }
        return retval;
    }
    
    public Object unify(Object u, Substitution sub) throws UnifyFailure {
        // with nominal vars, reverse direction of unification
        if (u instanceof NominalVar) return ((NominalVar)u).unify(this, sub);
        // check for equality with u
        if (equals(u)) return this; 
        // make sure u is an LF
        if (!(u instanceof LF)) throw new UnifyFailure();
        LF lf = (LF) u;
        // check type compatibility, if present
        SimpleType st = null;
        if (lf.getType() != null) st = (SimpleType) type.unify(lf.getType(), sub);
        // with hylo vars, substitute according to type specificity then comparison order, 
        // so that the direction of unification doesn't matter
        if (u instanceof HyloVar) {
            HyloVar u_hv = (HyloVar)u;
            // equal types, use comparison order
            if (type.equals(u_hv.getType())) {
                if (compareTo(u_hv) >= 0) return sub.makeSubstitution(this, u_hv); 
                else return sub.makeSubstitution(u_hv, this);
            }
            // unequal types, use most specific one
            if (type.equals(st)) return sub.makeSubstitution(u_hv, this);
            if (u_hv.getType().equals(st)) return sub.makeSubstitution(this, u_hv); 
            // otherwise make new hylo var with intersection type, 
            // name based on comparison order and index, and new index
            String name = (compareTo(u_hv) >= 0) ? (u_hv._name + u_hv._index) : (_name + this._index);
            HyloVar hv_st = new HyloVar(name, UnifyControl.getUniqueVarIndex(), st);
            // and subst both
            sub.makeSubstitution(u_hv, hv_st);
            return sub.makeSubstitution(this, hv_st); 
        }
        // with props, check for more specific type
        if (u instanceof Proposition) {
            Proposition prop = (Proposition) u;
            // if no or same type, just subst
            if (st == null || prop.getType().equals(st)) return sub.makeSubstitution(this, prop);
            // otherwise subst prop with name of type
            Proposition prop_st = new Proposition(st.getName(), st); 
            return sub.makeSubstitution(this, prop_st);
        }
        // otherwise, do occurs check ... 
        if (((LF)u).occurs(this)) throw new UnifyFailure(); 
        // and then go ahead and substitute
        return sub.makeSubstitution(this, u);
    }

    public Object fill(Substitution sub) throws UnifyFailure {
        Object val = sub.getValue(this);
        if (val != null) {
            return val;
        } else {
            return this;
        }
    }

    public String toString() { 
        String retval = _name+"_"+_index;
        if (!type.getName().equals(Types.TOP_TYPE)) retval += ":" + type.getName();
        return retval;
    }

    /** Returns the name with the type separated by a colon if the type is not the top type. */
    public String nameWithType() { 
        String retval = _name;
        if (!type.getName().equals(Types.TOP_TYPE)) retval += ":" + type.getName();
        return retval;
    }

    /**
     * Returns a pretty-printed string of this LF, with the given indent.
     */
    public String prettyPrint(String indent) {
        return toString();
    }
    
    /** Returns a hash code based on the name, index and type. */
    public int hashCode() {
        return _hashCode;
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
	 * (Note that the name and index may differ, but the types must be equal.)
	 */
    public boolean equals(Object obj, TObjectIntHashMap varMap, TObjectIntHashMap varMap2) {
        if (obj.getClass() != this.getClass()) { return false; }
        HyloVar hv = (HyloVar) obj;
        if (varMap.get(this) != varMap2.get(hv)) return false;
        if (!this.type.equals(hv.type)) return false;
        return true;
    }
    
    /**
     * Returns an XML representation of this LF.
     */
    public Element toXml() {
        Element retval = new Element("var");
        retval.setAttribute("name", nameWithType());
        return retval;
    }
}
