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
 * Forward composition, e.g. X/Y Y/Z => X/Z
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.3 $, $Date: 2009/07/17 04:23:30 $
 */
public class ForwardComposition extends AbstractCompositionRule {

	private static final long serialVersionUID = -5029901211362928251L;

	public ForwardComposition() {
		this(true);
	}

	public ForwardComposition(boolean isHarmonic) {
		_isHarmonic = isHarmonic;
		if (isHarmonic) {
			_name = ">B";
			_functorSlash = new Slash('/', "^");
			_argSlash = new Slash('/', "^");
		} else {
			_name = ">Bx";
			_functorSlash = new Slash('/', "x");
			_argSlash = new Slash('\\', "x");
		}
		_functorSlash.setAbility("active");
	}

    /** Returns an XML element representing the rule. */
    public Element toXml() { return super.toXml("forward"); }

	public List<Category> applyRule(Category[] inputs) throws UnifyFailure {
		if (inputs.length != 2) {
			throw new UnifyFailure();
		}

		return apply(inputs[0], inputs[1]);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("X").append(_functorSlash.toString()).append("Y Y").append(
				_argSlash.toString()).append("Z => X").append(
				_argSlash.toString()).append("Z");
		return sb.toString();
	}

}
