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
 * Super class for application rules.
 *
 * @author  Jason Baldridge
 * @author  Michael White
 * @version $Revision: 1.8 $, $Date: 2009/12/21 03:27:18 $
 */
public abstract class AbstractApplicationRule extends AbstractRule {
	
	private static final long serialVersionUID = 1L;
	
	protected Slash _functorSlash;

    /** Returns an XML element representing the rule. */
    public Element toXml(String dir) {
    	Element retval = new Element("application");
    	retval.setAttribute("dir", dir);
    	return retval;
    }

    public int arity() {
        return 2;
    }

    protected List<Category> apply(Category xyCat, Category yCat) throws UnifyFailure {

        if (xyCat instanceof ComplexCat) {

            ComplexCat xyCurCat = (ComplexCat)xyCat;
            Arg xyOuter = xyCurCat.getOuterArg();

            List<Category> results;
            _headCats.clear();

            if (xyOuter instanceof BasicArg) {
                xyOuter.unifySlash(_functorSlash);
                Category xyOuterCat = ((BasicArg)xyOuter).getCat();
                Substitution sub = new GSubstitution();
                GUnifier.unify(xyOuterCat, yCat, sub);
                results = new ArrayList<Category>(1);
                ((GSubstitution)sub).condense();
                Category result = (Category) xyCurCat.getResult().fill(sub);
                appendLFs(xyCat, yCat, result, sub);
                results.add(result);
                Slash xyOuterSlash = ((BasicArg)xyOuter).getSlash();
                _headCats.add(xyOuterSlash.isModifier() ? yCat : xyCat); 
            } else if (xyOuter instanceof SetArg) {
                SetArg xyOuterSet = (SetArg)xyOuter;
                results = new ArrayList<Category>(xyOuterSet.size());
                for (int i=0; i<xyOuterSet.size(); i++) {
                    BasicArg argi = xyOuterSet.get(i);
                    try {
                        argi.unifySlash(_functorSlash);
                        Substitution sub = new GSubstitution();
                        GUnifier.unify(argi.getCat(), yCat, sub);
                        ComplexCat result = (ComplexCat)xyCurCat.copy();
                        result.setOuterArgument(xyOuterSet.copyWithout(i));
                        ((GSubstitution)sub).condense();
                        result = (ComplexCat)result.fill(sub);
                        appendLFs(xyCat, yCat, result, sub);
                        results.add(result);
                        Slash xyOuterSlash = argi.getSlash();
                        _headCats.add(xyOuterSlash.isModifier() ? yCat : xyCat); 
                    } catch (UnifyFailure uf) {}                
                }
            } else {
                throw new UnifyFailure();
            }

            if (results.size() == 0) {
                throw new UnifyFailure();
            }
            
            return results;
        } else {
            throw new UnifyFailure();
        }
    }
}

