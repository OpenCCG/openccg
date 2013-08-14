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
 * Backward composition, e.g. Y\Z X\Y => X\Z
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.3 $, $Date: 2009/07/17 04:23:30 $
 */
public class BackwardComposition extends AbstractCompositionRule {

	private static final long serialVersionUID = -937944882697380690L;

	public BackwardComposition() {
		this(true);
	}

	public BackwardComposition(boolean isHarmonic) {
		_isHarmonic = isHarmonic;
		if (isHarmonic) {
			_name = "<B";
			_functorSlash = new Slash('\\', "^");
			_argSlash = new Slash('\\', "^");
		} else {
			_name = "<Bx";
			_functorSlash = new Slash('\\', "x");
			_argSlash = new Slash('/', "x");
		}
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
		StringBuffer sb = new StringBuffer();
		sb.append("Y").append(_argSlash.toString()).append("Z ").append(
				"X\\Y => X").append(_argSlash.toString()).append("Z");
		return sb.toString();
	}

}
