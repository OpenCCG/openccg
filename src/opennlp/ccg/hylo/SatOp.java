///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-5 Jason Baldridge and University of Edinburgh (Michael White)
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
 * A hybrid logic satifaction operator, which tests whether a formula is true
 * a particular point named by a nominal.
 *
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.13 $, $Date: 2009/07/17 04:23:30 $
 **/
public class SatOp extends HyloFormula {
    
	private static final long serialVersionUID = -4953978442971984002L;
	
	/**
     * The LF alts to which this LF belongs.
     * Null represents no alts.
     */
    protected List<Alt> alts = null;
    
    /**
     * Gets the LF alts to which this LF belongs.
     */
    public List<Alt> getAlts() { return alts; }
    
    /**
     * The LF opts (optional parts) to which this LF belongs.
     * LF opts are used during realization to represent 
     * optional parts of the input.
     * The opts are numbered starting with 0, 
     * and null represents no opts.
     */
    protected TIntArrayList opts = null;
    
    /**
     * Gets the LF opts to which this LF belongs.
     */
    public TIntArrayList getOpts() { return opts; }
    
    /**
     * The sign or unary rule which introduced this predication.
     */
    protected LexSemOrigin _origin = null;
    
    /**
     * Gets the sign or unary rule which introduced this predication (or null if none).
     */
    public LexSemOrigin getOrigin() { return _origin; }
    
    /**
     * Sets the sign or unary rule which introduced this predication.
     */
    public void setOrigin(LexSemOrigin origin) { _origin = origin; }
    
    
    // the real contents of the satop
    protected Nominal _nominal;
    protected LF _arg;

    public SatOp(Element e) {
        boolean shared = "true".equals(e.getAttributeValue("shared"));
        String nom = e.getAttributeValue("nom");
        if (nom != null) {
            _nominal = new NominalAtom(HyloHelper.prefix(nom), HyloHelper.type(nom), shared);
        } else {
            nom = e.getAttributeValue("nomvar");
            if (nom != null) {
                _nominal = new NominalVar(HyloHelper.prefix(nom), HyloHelper.type(nom), shared);
            } else {
                throw new RuntimeException("Satop must have a nom or nomvar.");
            }
        }
        _arg = HyloHelper.getLF_FromChildren(e);
    }

    public SatOp(Nominal nom, LF arg) {
        _nominal = nom;
        _arg = arg;
    }

    public Nominal getNominal() { return _nominal; }
    public void setNominal(Nominal nominal) { _nominal = nominal; }

    public LF getArg() { return _arg; }
    public void setArg(LF arg) { _arg = arg; }
    
    public LF copy() {
        SatOp retval = new SatOp((Nominal)_nominal.copy(), _arg.copy());
        retval._origin = _origin;
        return retval;
    }

    public void deepMap(ModFcn mf) {
        _nominal.deepMap(mf);
        _arg.deepMap(mf);
        mf.modify(this);
    }

    public boolean occurs(Variable var) {
        return (_nominal.occurs(var) || _arg.occurs(var));
    }

    /** Returns true iff the nominal and arg are equal. */
    public boolean equals(Object o) {
        if (o instanceof SatOp
            && _nominal.equals(((SatOp)o)._nominal)
            && _arg.equals(((SatOp)o)._arg)) 
        {
            return true;
        } else {
            return false;
        }
    }
    
    public Object unify(Object u, Substitution sub) throws UnifyFailure {
        if (u instanceof HyloFormula) {
            if (u instanceof SatOp) {
                Nominal $nom = (Nominal) Unifier.unify(_nominal, ((SatOp)u)._nominal, sub);
                LF $arg = (LF)Unifier.unify(_arg,((SatOp)u)._arg, sub);
                SatOp retval = new SatOp($nom, $arg);
                retval._origin = _origin;
                return retval;
            }
            else return super.unify(u,sub);
        } else {
            throw new UnifyFailure();
        }
    }

    public Object fill(Substitution sub) throws UnifyFailure {
        SatOp retval = new SatOp((Nominal)_nominal.fill(sub), (LF)_arg.fill(sub));
        retval._origin = _origin;
        return retval;
    }

    public String toString() { 
        boolean includeParens = !(_arg instanceof Op);
        StringBuffer sbuf = new StringBuffer();
        sbuf.append('@').append(_nominal.toString());
        if (includeParens) { sbuf.append('('); }
        sbuf.append(_arg.toString());
        if (includeParens) { sbuf.append(')'); }
        return sbuf.toString();
    }

    /**
     * Returns a pretty-printed string of this LF, with the given indent.
     */
    public String prettyPrint(String indent) {
        // calc new indent
        StringBuffer ibuf = new StringBuffer();
        ibuf.append(indent).append("  ");
        String nomStr = _nominal.toString();
        for (int i = 0; i < nomStr.length(); i++) {
            ibuf.append(' ');
        }
        String newIndent = ibuf.toString();
        // calc string
        boolean includeParens = !(_arg instanceof Op);
        StringBuffer sbuf = new StringBuffer();
        sbuf.append('@').append(nomStr); 
        if (includeParens) { sbuf.append('('); }
        sbuf.append(_arg.prettyPrint(newIndent));
        if (includeParens) { sbuf.append(')'); }
        // done
        return sbuf.toString();
    }
    
    
    /** Returns a hash code using the nominal and arg. */
    public int hashCode() { return _nominal.hashCode() + _arg.hashCode(); }
    
    /**
     * Returns a hash code using the given map from vars to ints.
     */
    public int hashCode(TObjectIntHashMap varMap) { 
        return _nominal.hashCode(varMap) + _arg.hashCode(varMap); 
    }
        
    /**
     * Returns whether this sat op equals the given object  
     * up to variable names, using the given maps from vars to ints.
     */
    public boolean equals(Object obj, TObjectIntHashMap varMap, TObjectIntHashMap varMap2) {
        if (obj.getClass() != this.getClass()) { return false; }
        SatOp so = (SatOp) obj;
        return _nominal.equals(so._nominal, varMap, varMap2) && 
               _arg.equals(so._arg, varMap, varMap2); 
    }
    
    /**
     * Returns an XML representation of this LF.
     */
    public Element toXml() {
        Element retval = new Element("satop");
        if (_nominal instanceof NominalAtom) {
            retval.setAttribute("nom", _nominal.toString());
        } else {
            retval.setAttribute("nomvar", ((NominalVar)_nominal).nameWithType());
        }
        Element argElt = _arg.toXml();
        retval.addContent(argElt);
        return retval;
    }
}
