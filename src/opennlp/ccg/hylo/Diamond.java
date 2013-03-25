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

import opennlp.ccg.synsem.*;
import opennlp.ccg.unify.*;
import org.jdom.*;

/**
 * A modal diamond operator, such as &lt;P&gt;p.
 *
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.6 $, $Date: 2009/07/17 04:23:30 $
 **/
public final class Diamond extends ModalOp {

	private static final long serialVersionUID = 543211908001651361L;

	public Diamond(Element e) {
        super(e);
    }

    public Diamond(Mode mode, LF arg) {
        super(mode, arg);
    }

    public LF copy() {
        return new Diamond ((Mode)_mode.copy(), _arg.copy());
    }
    
    public boolean equals(Object o) {
        if (o instanceof Diamond) {
            return super.equals((Diamond)o);
        } else {
            return false;
        }
    }

    public void unifyCheck(Object u) throws UnifyFailure {
        if (u instanceof Diamond) {
            super.unifyCheck((Diamond)u);
        } else {
            throw new UnifyFailure();
        }
    }

    public Object unify(Object u, Substitution sub) throws UnifyFailure {
        if (u instanceof HyloFormula) {
            if (u instanceof Diamond) {
                Mode $mode = (Mode) Unifier.unify(_mode, ((Diamond)u)._mode, sub);
                LF $arg = (LF) Unifier.unify(_arg,((Diamond)u)._arg, sub);
                return new Diamond($mode, $arg);
            }
            else return super.unify(u,sub);
        } else {
            throw new UnifyFailure();
        }
    }

    public Object fill(Substitution sub) throws UnifyFailure {
        return new Diamond((Mode)_mode.fill(sub), (LF)_arg.fill(sub));
    }
    
    /** Returns the string form of this modal op, without the arg. */
    public String modalOpString() {
        return new StringBuffer().append('<').append(_mode.toString()).append('>').toString();
    }
    
    /**
     * Returns an XML representation of this LF.
     */
    public Element toXml() {
        Element retval = new Element("diamond");
        retval.setAttribute("mode", _mode.toString());
        Element argElt = _arg.toXml();
        retval.addContent(argElt);
        return retval;
    }
}
