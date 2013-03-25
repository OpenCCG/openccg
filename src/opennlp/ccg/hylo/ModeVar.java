///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002-4 Jason Baldridge and University of Edinburgh (Michael White)
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
 * A class for variables over ModeLabel objects.
 * Mode vars are not really supported at present, and 
 * type unification is not implemented.
 *
 * @author      Jason Baldridge
 * @author		<a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 * @version     $Revision: 1.5 $, $Date: 2009/07/17 04:23:30 $
 **/
public class ModeVar extends HyloVar implements Mode {
    
	private static final long serialVersionUID = -6872985893931836901L;

	public ModeVar(String name) {
        super(name);
    }

    protected ModeVar(String name, int index, SimpleType st) {
        super(name, index, st);
    }
    
    /**
     * Gets the name of this mode variable.
     * @return This method just delegates to the {@link #name()} method.
     */
    @Override
	public String getName() {
		return name();
	}

	public LF copy() {
        return new ModeVar(_name, _index, type);
    }

    
    public boolean equals(Object o) {
        if (!(o instanceof ModeVar)) return false;
        return super.equals(o);
    }
    
    public Object unify(Object u, Substitution sub) throws UnifyFailure {
        if (u instanceof ModeLabel) {
            return sub.makeSubstitution(this, u);
        } else if (u instanceof ModeVar) {
            ModeVar u_nv = (ModeVar)u;
            if (equals(u_nv)) return this;
            // substitute according to comparison order, 
            // so that the direction of unification doesn't matter
            if (compareTo(u_nv) >= 0) {
                return sub.makeSubstitution(this, u_nv);
            } else {
                return sub.makeSubstitution(u_nv, this);
            }
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

    
    /**
     * Returns an XML representation of this LF (not currently supported).
     * Throws a runtime exception.
     */
    public Element toXml() {
        throw new RuntimeException("toXml() not currently supported for ModeVar.");
    }
}
