///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002-3 Jason Baldridge and University of Edinburgh (Michael White)
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
 * Super class for substitution rules.
 *
 * @author  Jason Baldridge
 * @author  Michael White
 * @version $Revision: 1.5 $, $Date: 2009/12/21 03:27:18 $
 */
public abstract class AbstractSubstitutionRule extends AbstractApplicationRule {

	private static final long serialVersionUID = 1L;
	
	protected boolean _isHarmonic;
    protected Slash _argSlash;

    /** Returns an XML element representing the rule. */
    public Element toXml(String dir) {
    	Element retval = new Element("substitution");
    	retval.setAttribute("dir", dir);
    	retval.setAttribute("harmonic", Boolean.toString(_isHarmonic));
    	return retval;
    }

    protected List<Category> apply (Category xyzCat, Category yzCat)
        throws UnifyFailure {
        
        if (xyzCat instanceof ComplexCat && yzCat instanceof ComplexCat) {
            ComplexCat xyzCC = (ComplexCat)xyzCat;
            ComplexCat yzCC = (ComplexCat)yzCat;

            if (xyzCC.arity() < 2
                || xyzCC.containsDollarArg()
                || xyzCC.containsSetArg()
                || yzCC.containsSetArg()
                || yzCC.containsDollarArg()) {
                throw new UnifyFailure();
            }

            ArgStack primaryStack = xyzCC.getArgStack();
            int size = primaryStack.size();
            
            BasicArg primaryArgY = (BasicArg)primaryStack.get(size-2);
            primaryArgY.unifySlash(_functorSlash);
            BasicArg primaryArgZ = (BasicArg)primaryStack.get(size-1);
            primaryArgZ.unifySlash(_argSlash);

            BasicArg secondaryArgZ = (BasicArg)yzCC.getOuterArg();
            secondaryArgZ.unifySlash(_argSlash);
            Category secondaryY = yzCC.getResult();

            GSubstitution sub = new GSubstitution();

            GUnifier.unify(primaryArgZ.getCat(), secondaryArgZ.getCat(), sub);
            GUnifier.unify(primaryArgY.getCat(), secondaryY, sub);         
            
            Category result =
                new ComplexCat(xyzCC.getTarget(), primaryStack.copyWithout(size-2));
            ((GSubstitution)sub).condense();
            result = (Category)result.fill(sub);
            ((ComplexCat)result).getOuterArg().setSlashModifier(false);
            
            appendLFs(xyzCat, yzCat, result, sub);
            
            List<Category> results = new ArrayList<Category>(1);
            _headCats.clear();
            results.add(result);
            _headCats.add(primaryArgY.getSlash().isModifier() ? yzCat : xyzCat); 
            return results;
        } else {
            throw new UnifyFailure();
        }

    }
}

