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

package opennlp.ccg.grammar;

import opennlp.ccg.unify.*;
import opennlp.ccg.hylo.HyloHelper;
import opennlp.ccg.synsem.*;

import java.util.*;

import org.jdom.Element;

/**
 * A CCG unary type changing rule.
 *
 * @author      Jason Baldridge
 * @author      Michael White
 * @version $Revision: 1.12 $, $Date: 2009/11/30 20:36:16 $
 **/
public class TypeChangingRule extends AbstractRule implements LexSemOrigin {
	
	private static final long serialVersionUID = -2654945192870162776L;

	/**
	 * String used as the POS for all type changing rules, 
	 * to satisfy the LexSemOrigin interface.
	 * Defaults to "URULE".
	 */
	public static String POS_STRING = "URULE";
	

    /** The argument category. */   
    protected Category _arg; 

    /** The result category. */
    protected Category _result; 
    
    /** The first elementary predication in the result LF (or null), before sorting. */
    protected LF _firstEP;


    /** Constructor. */
    public TypeChangingRule(Category arg, Category result, String name, LF firstEP) {
        _arg = arg; _result = result; _name = name.intern(); _firstEP = firstEP;
        setOrigin();
    }

    /** Returns an XML element representing the rule. */
    public Element toXml() {
    	Element retval = new Element("typechanging");
    	retval.setAttribute("name", _name);
    	Element argElt = new Element("arg");
    	retval.addContent(argElt);
    	argElt.addContent(_arg.toXml());
    	Element resultElt = new Element("result");
    	retval.addContent(resultElt);
    	resultElt.addContent(_result.toXml());
    	return retval;
    }

    /** Returns 1. */
    public int arity() { return 1; }
    
    /** Returns the arg. */
    public Category getArg() { return _arg; }
    
    /** Returns the result. */
    public Category getResult() { return _result; }

    /** Returns the first elementary predication in the result LF (or null), before sorting. */
    public LF getFirstEP() { return _firstEP; }


    /** Applies this rule to the given inputs. */
    public List<Category> applyRule(Category[] inputs) throws UnifyFailure {
        // check arity
        if (inputs.length != 1) {
            throw new UnifyFailure();
        }
        return apply(inputs[0]);
    }

    /** Applies this rule to the given input. */
    protected List<Category> apply(Category input) throws UnifyFailure {

        // unify quick check
        _arg.unifyCheck(input);
        
        // copy arg and result
        Category arg = _arg.copy();
        Category result = _result.copy();
        
        // make variables unique
        UnifyControl.reindex(result, arg);

        // unify
        Substitution sub = new GSubstitution();
        GUnifier.unify(input, arg, sub);
        ((GSubstitution)sub).condense();

        // fill in result
        Category $result = (Category)result.fill(sub);
        appendLFs(input, result, $result, sub);

        // return
        List<Category> results = new ArrayList<Category>(1);
        _headCats.clear();
        results.add($result);
        _headCats.add(input);
        return results;
    }
    
    
    /** Returns 'name: arg => result'. */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(_name).append(": ");
        sb.append(_arg).append(' ');
        sb.append("=> ").append(_result);
        return sb.toString();
    }
    
    /** Returns 'arg_=>_result' as the supertag. */
    public String getSupertag() {
        StringBuffer sb = new StringBuffer();
        sb.append(_arg.getSupertag()).append("_=>_").append(_result.getSupertag());
        return sb.toString();
    }
    
    /**
     * Always returns POS_STRING. 
     */
    public String getPOS() { return POS_STRING; }
    
    /**
     * Sets the origin of the elementary predications.
     */
    public void setOrigin() { HyloHelper.setOrigin(_result.getLF(), this); }
}

