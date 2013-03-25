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
import java.util.*;
import gnu.trove.*;

/**
 * A parent class for modal operators, such as &lt;P&gt;p, [F]q, and
 * &lt;&gt;(p ^ q).
 *
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.6 $, $Date: 2009/12/21 03:27:19 $
 **/
public abstract class ModalOp extends HyloFormula {

	private static final long serialVersionUID = 1L;
	
	protected Mode _mode;
    protected LF _arg;

    @SuppressWarnings("unchecked")
	protected ModalOp(Element e) {
        String atomLabel = e.getAttributeValue("mode");
        if (atomLabel == null) atomLabel = e.getAttributeValue("m");
        if (atomLabel != null) {
            _mode = new ModeLabel(atomLabel);
            _arg = HyloHelper.getLF_FromChildren(e);
        } else {
            List<Element> children = e.getChildren();
            _mode = (Mode)HyloHelper.getLF((Element)children.get(0));
            _arg = HyloHelper.getLF((Element)children.get(1));
        }
    }
    
    protected ModalOp(Mode mode, LF arg) {
        _mode = mode;
        _arg = arg;
    }

    public Mode getMode() { return _mode; }
    public void setMode(Mode mode) { _mode = mode; }

    public LF getArg() { return _arg; }
    public void setArg(LF arg) { _arg = arg; }
    
    public void deepMap(ModFcn mf) {
        _arg.deepMap(mf);
        mf.modify(this);
    }

    public boolean occurs(Variable var) {
        return _mode.occurs(var) || _arg.occurs(var);
    }

    protected boolean equals(ModalOp mo) {
        if (_mode.equals(mo._mode) && _arg.equals(mo._arg)) {
            return true;
        } else {
            return false;
        }
    }

    protected void unifyCheck(ModalOp mo) throws UnifyFailure {
        _mode.unifyCheck(mo._mode);
        _arg.unifyCheck(mo._arg);
    }

    /** Returns a hash code based on the mode and arg. */
    public int hashCode() { return _mode.hashCode() + _arg.hashCode(); }
    
    /**
     * Returns a hash code using the given map from vars to ints.
     */
    public int hashCode(TObjectIntHashMap varMap) { 
        return _mode.hashCode(varMap) + _arg.hashCode(varMap); 
    }
        
    /**
     * Returns whether this modal op equals the given object  
     * up to variable names, using the given maps from vars to ints.
     */
    public boolean equals(Object obj, TObjectIntHashMap varMap, TObjectIntHashMap varMap2) {
        if (obj.getClass() != this.getClass()) { return false; }
        ModalOp mo = (ModalOp) obj;
        return _mode.equals(mo._mode, varMap, varMap2) && 
               _arg.equals(mo._arg, varMap, varMap2); 
    }

    /** Returns the string form of this modal op, without the arg. */
    abstract public String modalOpString();
    
    public String toString() {
        return new StringBuffer().append(modalOpString()).append(_arg.toString()).toString();
    }

    /**
     * Returns a pretty-printed string of this LF, with the given indent.
     */
    public String prettyPrint(String indent) {
        // calc new indent
        StringBuffer ibuf = new StringBuffer();
        ibuf.append(indent).append(' ');
        String modalOpString = modalOpString(); 
        for (int i = 0; i < modalOpString.length(); i++) {
            ibuf.append(' ');
        }
        String newIndent = ibuf.toString();
        // calc string
        StringBuffer sb = new StringBuffer();
        sb.append('\n').append(indent).append(modalOpString);
        sb.append(_arg.prettyPrint(newIndent));
        // done
        return sb.toString();
    }
}
