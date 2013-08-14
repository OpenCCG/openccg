///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2011 Michael White
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

import java.util.*;

import org.jdom.Element;

import opennlp.ccg.synsem.*;
import opennlp.ccg.unify.*;

/**
 * Implements a glue rule for combining a sequence of fragments.
 * The rule is frag|cat cat => frag, allowing only the first input 
 * to itself be a fragment, unless the second input has the 
 * frag completion flag set, meaning that it completes a chunk/alt.
 * 
 * @author  Michael White
 * @version $Revision: 1.3 $, $Date: 2011/06/07 05:12:01 $
 */
public class GlueRule extends AbstractRule {

	private static final long serialVersionUID = 4867141181941895272L;

	// empty subst for combining LFs
	private static final Substitution emptySubst = new SimpleSubstitution();
	
	/** Fragment result type. */
	public static final String resultType = "frag";
	
	/** Constructor. */
	public GlueRule() { _name = "glue"; }
	
    /** Returns an XML element representing the rule (not supported). */
    public Element toXml() { throw new RuntimeException("toXml not supported for GlueRule rules"); }

	/** Arity. */
	public int arity() { return 2; }

	/** Glues cats into fragments. */
	public List<Category> applyRule(Category[] inputs) throws UnifyFailure {
		// check num inputs
		if (inputs.length != 2) {
			throw new UnifyFailure();
		}
		// check for frag as second input with completion false
		if (inputs[1] instanceof AtomCat) {
			AtomCat ac2 = (AtomCat) inputs[1];
			if (ac2.isFragment() && !ac2.fragCompletion) 
				throw new UnifyFailure();
		}
		// make result cat
        List<Category> results = new ArrayList<Category>(1);
        _headCats.clear();
        AtomCat ac = new AtomCat(resultType);
        appendLFs(inputs[0], inputs[1], ac, emptySubst);
        results.add(ac);
        // guess head, with left as default
        boolean leftHead = true;
        boolean leftMod = isModifier(inputs[0]);
        boolean rightMod = isModifier(inputs[1]);
        if ((inputs[0] instanceof AtomCat && inputs[1] instanceof ComplexCat && !rightMod) ||
        	(leftMod && !rightMod)) 
        {
        	leftHead = false;
        }
		// return result cat with guessed head
        _headCats.add(leftHead ? inputs[0] : inputs[1]);
		return results;
	}
	
	// modifier check
	private static boolean isModifier(Category cat) {
		if (cat instanceof ComplexCat) {
			ComplexCat xyCat = (ComplexCat) cat;
			Arg arg = xyCat.getOuterArg();
			if (arg instanceof BasicArg) {
				return ((BasicArg)arg).getSlash().isModifier();
			}
		}
		return false;
	}

	/** toString. */
	public String toString() {
		return "frag|cat cat => frag";
	}
}
