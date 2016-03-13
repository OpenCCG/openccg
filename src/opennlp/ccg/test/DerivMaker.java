///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2016 Michael White
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
package opennlp.ccg.test;

//import java.util.*;
import org.jdom.*;
import opennlp.ccg.synsem.*;

/**
 * Utility class for exporting derivations in xml.
 *
 * @author  Michael White
 * @version $Revision: 1.5 $, $Date: 2009/12/21 04:18:31 $
 */
public class DerivMaker {

	/** 
	 * Returns a derivation in the same format as the converted CCGbank, 
	 * but just with the lexemes, POS tags and supertags.
	 */
	public static Element makeDeriv(Sign sign) {
		Element retval;
		if (sign.isLexical()) {
			retval = new Element("Leafnode");
			retval.setAttribute("lexeme",sign.getOrthography());
			retval.setAttribute("pos",sign.getPOS());
		}
		else {
			retval = new Element("Treenode");
			for (Sign child: sign.getDerivationHistory().getInputs())
				retval.addContent(makeDeriv(child));
		}
		retval.setAttribute("stag",sign.getSupertag());
		return retval;
	}
}


