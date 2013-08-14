///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002 Jason Baldridge
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
 * Forward application: X/Y Y => X
 *
 * @author  Jason Baldridge
 * @version $Revision: 1.3 $, $Date: 2009/07/17 04:23:30 $
 */
public class BackwardApplication extends AbstractApplicationRule {

	private static final long serialVersionUID = 6981288425455531650L;

	public BackwardApplication () {
		_name = "<";
		_functorSlash = new Slash('\\');
		_functorSlash.setAbility("active");
    }
    
    /** Returns an XML element representing the rule. */
    public Element toXml() { return super.toXml("backward"); }
    
    public List<Category> applyRule(Category[] inputs) throws UnifyFailure {
		if (inputs.length != 2) {
		    throw new UnifyFailure();
		}
		return apply(inputs[1], inputs[0]);
    }

    public String toString() {
		return "Y X\\Y => X";
    }
}

