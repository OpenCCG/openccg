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

package opennlp.ccg.grammar;

import opennlp.ccg.unify.*;
import opennlp.ccg.synsem.*;

import java.util.*;

import org.jdom.Element;

/**
 * Type-raising, e.g. np => s/(s\np).
 *
 * @author  Jason Baldridge
 * @author  Michael White
 * @version $Revision: 1.9 $, $Date: 2009/12/21 03:27:18 $
 */
// NB: It might make sense to eventually make this a subclass of TypeChangingRule, 
//     but currently it's structured a bit differently.
public abstract class AbstractTypeRaisingRule extends AbstractRule {

	private static final long serialVersionUID = 1L;

	/** The upper slash, eg the first slash in s/(s\np). */
    protected Slash _upperSlash; 

    /** The embedded slash, eg the second slash in s/(s\np). */
    protected Slash _embeddedSlash;

    /** 
     * The result of the type raised category, eg the "s" in s/(s\np). Defaults to "s". 
     */
    protected Category _result;

    /** 
     * The argument of the ComplexCat argument of the type raised category,
     * eg the "np" in s/(s\np). Defaults to "np".
     */
    protected Category _arg;

    /**
     * Creates a new type raising rule with the given name; upper and lower slashes; 
     * use dollar switch; arg category; and result category.  Defaults are used 
     * for the arg and result categories if null.
     */
    protected AbstractTypeRaisingRule(
        String name, Slash uslash, Slash eslash, 
        boolean useDollar, Category arg, Category result
    ) {
        _name = name;
        _upperSlash = uslash;
        _upperSlash.setAbility("active");
        _upperSlash.setModifier(true);
        _embeddedSlash = eslash;
        _embeddedSlash.setAbility("active");

        if (arg != null) { _arg = arg; }
        else { _arg = new AtomCat("np", new GFeatStruc()); }

        if (result != null) { 
            _result = result;
            result.getFeatureStructure().setIndex(1);
        }
        else {
            GFeatStruc resfs = new GFeatStruc();
            resfs.setIndex(1);
            _result = new AtomCat("s", resfs);
        }
        
        if (useDollar) {
            Dollar dol = new Dollar("1");
            dol.setIndex(1);
            _result = new ComplexCat((AtomCat)_result, dol);
        }
        
    }

    /** Returns an XML element representing the rule. */
    public Element toXml(String dir) {
    	Element retval = new Element("typeraising");
    	retval.setAttribute("dir", dir);
    	boolean usesDollar = (_result instanceof ComplexCat) && ((ComplexCat)_result).containsDollarArg();
    	retval.setAttribute("useDollar", Boolean.toString(usesDollar));
    	if (!(_arg instanceof AtomCat) || !((AtomCat)_arg).getType().equals("np")) {
        	Element argElt = new Element("arg");
        	retval.addContent(argElt);
        	argElt.addContent(_arg.toXml());
    	}
    	if (!((AtomCat)_result.getTarget()).getType().equals("s")) {
        	Element resultElt = new Element("result");
        	retval.addContent(resultElt);
        	resultElt.addContent(_result.getTarget().toXml());
    	}
    	return retval;
    }

    /** Returns 1. */
    public int arity() {
        return 1;
    }

    /** Applies this rule to the given inputs. */
    public List<Category> applyRule(Category[] inputs) throws UnifyFailure {
        if (inputs.length != 1) {
            throw new UnifyFailure();
        }
        return apply(inputs[0]);
    }

    /** Applies this rule to the given input. */
    protected List<Category> apply(Category input) throws UnifyFailure {
        Substitution sub = new GSubstitution();
        Category arg = (Category)_arg.unify(input, sub);
        ((GSubstitution)sub).condense();
        
        Category result = _result.copy();
        ComplexCat range;
        UnifyControl.reindex(result);
        if (result instanceof ComplexCat) {
            range = (ComplexCat)result.copy();
            range.add(new BasicArg(_embeddedSlash, arg));
            ((ComplexCat)result).add(new BasicArg(_upperSlash, range));
        } else {
            range = new ComplexCat((TargetCat)result.copy(),
                                   new BasicArg(_embeddedSlash, arg));
            result = new ComplexCat((TargetCat)result.copy(),
                                    new BasicArg(_upperSlash, range));
        }
        
        // nb: with defined type changing rules, this step is done when the 
        //     rule is created; with type raising, it is done here, so that 
        //     the arg need not have its distributive features yet, and since 
        //     the full result category doesn't exist beforehand
        _ruleGroup.grammar.lexicon.propagateDistributiveAttrs(result);
        
        LF inputLF = input.getLF();
        if (inputLF != null) {
            result.setLF((LF)inputLF.copy());
        }
        
        List<Category> results = new ArrayList<Category>(1);
        _headCats.clear();
        results.add(result);
        _headCats.add(input);
        return results;
    }
}

