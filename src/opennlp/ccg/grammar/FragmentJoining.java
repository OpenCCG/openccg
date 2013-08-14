///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2007 Michael White
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

import opennlp.ccg.synsem.*;
import opennlp.ccg.unify.*;

import java.util.*;

import org.jdom.Element;


/**
 * Rule for joining fragments that don't fit together, when all else fails.
 *
 * @author Michael White
 * @version $Revision: 1.4 $, $Date: 2009/07/17 04:23:30 $
 */
public class FragmentJoining extends AbstractRule {

	private static final long serialVersionUID = 7451163798607652012L;

	/** Constructor. */
	public FragmentJoining() { _name = "*"; }
	
    /** Returns an XML element representing the rule (not supported). */
    public Element toXml() { throw new RuntimeException("toXml not supported for FragmentJoining rules"); }

	/**
	 *  Returns the result of applying this rule to two input signs.
	 */
	public Sign applyRule(Sign sign1, Sign sign2) {
		List<Sign> results = new ArrayList<Sign>(1);
		Sign[] inputs = new Sign[] { sign1, sign2 };
		applyRule(inputs, results);
		return results.get(0);
	}
	
    /**
     * Apply this rule to two input categories.  
     * Returns a copy of the first cat with the LFs appended.
     **/
    public List<Category> applyRule(Category[] inputs) throws UnifyFailure {
		if (inputs.length != 2) { throw new UnifyFailure(); }
		List<Category> results = new ArrayList<Category>(1);
        _headCats.clear();
		Category result = inputs[0].shallowCopy();
		try {
			appendLFs(inputs[0], inputs[1], result, new EmptySubstitution());
		}
		catch (UnifyFailure uf) { // not expected
			// System.err.println("Unexpected unify failure in appending LFs when joining fragments:");
			// System.err.println("cat0: " + inputs[0] + " lf: " + inputs[0].getLF());
			// System.err.println("cat1: " + inputs[1] + " lf: " + inputs[1].getLF());
		}
		results.add(result);
		_headCats.add(inputs[0]);
		return results;
    }

    /**
     * The number of arguments this rule takes.
     **/
    public int arity() { return 2; }

    /** Returns a string for this rule. */
	public String toString() {
		return "X Y *=> X";
	}
}

