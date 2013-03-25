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
import opennlp.ccg.grammar.Grammar;
import org.jdom.*;
import java.util.*;
import gnu.trove.*;

/**
 * A generic operator, such as conjunction, disjunction, exclusive-or, 
 * negation or optionality (^, v, v_, ~, ?).
 *
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.17 $, $Date: 2009/12/21 03:27:19 $
 **/
public class Op extends HyloFormula {
    
	private static final long serialVersionUID = -7489598953770901195L;

	/** Conjunction constant. */
    public static final String CONJ = "conj";
    
    /** Disjunction constant. */
    public static final String DISJ = "disj";
    
    /** Exclusive-or constant. */
    public static final String XOR = "xor";
    
    /** Negation constant. */
    public static final String NEG = "neg";
    
    /** Optionality constant. */
    public static final String OPT = "opt";

    
    /** The name of the operator (ie its kind). */
    protected final String _name;
    
    /** The args. */
    protected List<LF> _args;
    
    /** Element constructor. */
    @SuppressWarnings("unchecked")
	public Op(Element e) {
        String name = e.getAttributeValue("name");
        if (name == null) name = e.getAttributeValue("n");
        _name = name;
        List<Element> argElements = e.getChildren();
        int argSize = argElements.size();
        List<LF> args = new ArrayList<LF>(argSize);
        for (int i=0; i<argSize; i++) {
            args.add(HyloHelper.getLF((Element)argElements.get(i)));
        }
        // add implicit CONJ op with NEG or OPT
        if (args.size() > 1 && (name.equals(NEG) || name.equals(OPT))) {
            _args = new ArrayList<LF>(1);
            _args.add(new Op(CONJ, args));
        }
        else _args = args;
    }

    /** Constructor. */
    public Op(String name, List<LF> args) {
        _name = name; _args = args; 
    }

    /** Two arg convenience constructor. */
    public Op(String name, LF first, LF second) {
        _name = name;
        _args = new ArrayList<LF>();
        _args.add(first); _args.add(second);
    }
    
	public String getName() { 
        return _name;
    }

    public List<LF> getArguments() {
        return _args;
    }
    
    public void addArgument(LF formula) {
        _args.add(formula);
    }
    
    /**
     * Appends the args if the given lf is a CONJ op,
     * otherwise just adds it.
     */
    public void appendArgs(LF lf) {
        if (lf instanceof Op && ((Op)lf).getName().equals(Op.CONJ)) 
            _args.addAll(((Op)lf).getArguments());
        else _args.add(lf);
    }

    public LF copy() {
        List<LF> $args = new ArrayList<LF>(_args.size());
        for (LF arg : _args) {
            $args.add(arg.copy());
        }
        return new Op(_name, $args);
    }

    public void deepMap(ModFcn mf) {
        for (Iterator<LF> argsIt = _args.iterator(); argsIt.hasNext(); ) {
            argsIt.next().deepMap(mf);
        }
        mf.modify(this);
    }

    public boolean occurs(Variable var) {
        for (Iterator<LF> argsIt = _args.iterator(); argsIt.hasNext(); ) {
            if (argsIt.next().occurs(var)) {
                return true;
            }
        }
        return false;
    }

    /** Returns true iff the given object equals this op. */
    public boolean equals(Object o) {
        if (!(o instanceof Op)) return false;
        Op op = (Op) o;
        if (_name != op._name) return false;
        List<LF> opArgs = op._args;
        if (_args.size() != opArgs.size()) return false;
        if (!opArgs.containsAll(_args)) return false;
        return true;
    }
    
    /** Unification is not attempted for Ops. */
    public void unifyCheck(Object u) throws UnifyFailure {
        throw new UnifyFailure();
    }

    /** Unification is not attempted for Ops. */
    public Object unify(Object u, Substitution s) throws UnifyFailure {
        throw new UnifyFailure();
    }
    
    public Object fill(Substitution sub) throws UnifyFailure {
        List<LF> $args = new ArrayList<LF>(_args.size());
        for (LF arg : _args) {
            $args.add((LF)arg.fill(sub));
        }
        return new Op(_name, $args);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String opString = printOp(_name);
        if (_args.size() == 1) {
            sb.append(opString);
            sb.append(_args.get(0).toString());
        } else {
            sb.append('(');
            Iterator<LF> argsIt = filteredArgs().iterator();
            for (; argsIt.hasNext(); ) {
                sb.append(argsIt.next().toString());
                if (argsIt.hasNext()) sb.append(' ').append(opString).append(' ');
            }
            sb.append(')');
        }
        return sb.toString();
    }
    
    /**
     * Returns a pretty-printed string of this LF, with the given indent.
     */
    public String prettyPrint(String indent) {
        StringBuffer sb = new StringBuffer();
        String opString = printOp(_name);
        if (_args.size() == 1) {
            sb.append(opString);
            sb.append(((LF)_args.get(0)).prettyPrint(indent));
        } else {
            sb.append('(');
            Iterator<LF> argsIt = filteredArgs().iterator();
            for (; argsIt.hasNext(); ) {
                sb.append(argsIt.next().prettyPrint(indent));
                if (argsIt.hasNext()) sb.append(' ').append(opString).append(' ');
            }
            sb.append(')');
        }
        return sb.toString();
    }
    
    public static String printOp(String o) {
        if (o.equals(CONJ))      return "^";
        else if (o.equals(DISJ)) return "v";
        else if (o.equals(XOR))  return "v_";
        else if (o.equals(NEG))  return "~";
        else if (o.equals(OPT))  return "?";
        else                     return o;
    }
    
    // filters out semantic features if apropos
    private List<LF> filteredArgs() {
        String featsToShow = Grammar.theGrammar.prefs.featsToShow;
        if (featsToShow.length() == 0) return _args;
        List<LF> retval = new ArrayList<LF>(_args.size());
        for (Iterator<LF> it = _args.iterator(); it.hasNext(); ) {
            LF arg = it.next();
            String attr = null;
            if (arg instanceof SatOp && HyloHelper.isAttrPred(arg)) 
                attr = HyloHelper.getRel(arg);
            else if (arg instanceof Diamond && HyloHelper.isAttr(arg))
                attr = ((Diamond)arg).getMode().toString();
            if (attr == null || featsToShow.indexOf(attr) != -1)
                retval.add(arg);
        }
        return retval;
    }

    /** Returns a hash code. */
    public int hashCode() {
        int retval = _name.hashCode();
        for (Iterator<LF> it = _args.iterator(); it.hasNext(); ) {
            retval += it.next().hashCode();
        }
        return retval;
    }
    
    /**
     * Returns a hash code using the given map from vars to ints.
     */
    public int hashCode(TObjectIntHashMap varMap) { 
        int retval = _name.hashCode();
        for (Iterator<LF> it = _args.iterator(); it.hasNext(); ) {
            LF arg = it.next();
            retval += arg.hashCode(varMap);
        }
        return retval;
    }
        
    /**
     * Returns whether this op equals the given object  
     * up to variable names, using the given maps from vars to ints
     * (where args must be in the same order).
     */
    public boolean equals(Object obj, TObjectIntHashMap varMap, TObjectIntHashMap varMap2) {
        if (obj.getClass() != this.getClass()) { return false; }
        Op op = (Op) obj;
        if (!_name.equals(op._name)) return false;
        if (_args.size() != op._args.size()) return false;
        for (int i = 0; i < _args.size(); i++) {
            LF arg = (LF) _args.get(i);
            LF arg2 = (LF) op._args.get(i);
            if (!arg.equals(arg2, varMap, varMap2)) return false;
        }
        return true;
    }
    
    /**
     * Returns an XML representation of this LF.
     */
    public Element toXml() {
        Element retval = new Element("op");
        retval.setAttribute("name", _name);
        for (int i = 0; i < _args.size(); i++) {
            LF arg = (LF) _args.get(i);
            Element argElt = arg.toXml();
            retval.addContent(argElt);
        }
        return retval;
    }
}
