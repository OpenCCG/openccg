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

import opennlp.ccg.synsem.*;
import opennlp.ccg.unify.*;
import org.jdom.*;

/**
 * A class for variables over NominalAtom objects.
 * Nominal vars take precedence over generic hylo vars.
 * Types are unified with other hylo vars and nominal atoms.
 *
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.13 $, $Date: 2009/07/17 04:23:30 $
 **/
public class NominalVar extends HyloVar implements Nominal {
    
	private static final long serialVersionUID = -2086362887254623273L;
	
	protected boolean shared = false;
    
    public NominalVar(String name) {
        super(name);
    }

    public NominalVar(String name, SimpleType st) {
        super(name, st);
    }

    public NominalVar(String name, SimpleType st, boolean shared) {
        super(name, st);
        this.shared = shared;
    }

    protected NominalVar(String name, int index, SimpleType st) {
        super(name, index, st);
    }
    
    protected NominalVar(String name, int index, SimpleType st, boolean shared) {
        super(name, index, st);
        this.shared = shared;
    }
    
    public String getName() { return _name; }
    
    public boolean isShared() { return shared; }

    public void setShared(boolean shared) { this.shared = shared; }
    
    public void setType(SimpleType st) { 
        _hashCode += st.getIndex() - type.getIndex();
        type = st; 
    }
    
    public LF copy() {
        return new NominalVar(_name, _index, type, shared);
    }

    
    public boolean equals(Object o) {
        if (!(o instanceof NominalVar)) return false;
        return super.equals(o);
    }
    
    public int compareTo(Nominal nom) {
        if (nom instanceof NominalVar) { 
            return super.compareTo((NominalVar)nom);
        }
        int retval = _name.compareTo(nom.getName());
        if (retval == 0) { retval = 1; } // atom precedes var if names equal
        return retval;
    }
    
    
    public Object unify(Object u, Substitution sub) throws UnifyFailure {
        // check for equality with u
        if (equals(u)) return this; 
        // make sure u is an LF
        if (!(u instanceof LF)) throw new UnifyFailure(); 
        // check type compatibility
        LF lf = (LF) u;
        if (lf.getType() == null) throw new UnifyFailure();
        SimpleType st = (SimpleType) type.unify(lf.getType(), sub);
        // with nominal atoms, go ahead and substitute
        if (u instanceof NominalAtom) return sub.makeSubstitution(this, u); 
        // with nominal vars, substitute according to type specificity then comparison order,  
        // so that the direction of unification doesn't matter
        if (u instanceof NominalVar) {
            NominalVar u_nv = (NominalVar) u;
            // equal types, use comparison order
            if (type.equals(u_nv.getType())) {
                if (super.compareTo(u_nv) >= 0) return sub.makeSubstitution(this, u_nv); 
                else return sub.makeSubstitution(u_nv, this);
            }
            // unequal types, use most specific one
            if (type.equals(st)) return sub.makeSubstitution(u_nv, this);
            if (u_nv.getType().equals(st)) return sub.makeSubstitution(this, u_nv); 
            // otherwise make new nom var with intersection type, 
            // name based on comparison order and index, and new index
            String name = (super.compareTo(u_nv) >= 0) ? (u_nv._name + u_nv._index) : (_name + _index);
            NominalVar nv_st = new NominalVar(name, UnifyControl.getUniqueVarIndex(), st);
            // and subst both
            sub.makeSubstitution(u_nv, nv_st);
            return sub.makeSubstitution(this, nv_st); 
        }
        // with hylo vars, substitute the hylo var for this 
        if (u instanceof HyloVar) { 
            HyloVar u_hv = (HyloVar) u;
            // check for same type
            if (type.equals(st)) return sub.makeSubstitution(u_hv, this); 
            // otherwise make new nom var with intersection type, 
            // same name, and new index
            NominalVar nv_st = new NominalVar(this._name, UnifyControl.getUniqueVarIndex(), st);
            // and subst both
            sub.makeSubstitution(u_hv, nv_st);
            return sub.makeSubstitution(this, nv_st); 
        }
        // otherwise give up
        throw new UnifyFailure();
    }

    public Object fill(Substitution sub) throws UnifyFailure {
        Object val = sub.getValue(this);
        if (val != null) {
            return val;
        } else {
            return this;
        }
    }
    
    /**
     * Returns an XML representation of this LF.
     */
    public Element toXml() {
        Element retval = new Element("nomvar");
        retval.setAttribute("name", nameWithType());
        return retval;
    }
}
