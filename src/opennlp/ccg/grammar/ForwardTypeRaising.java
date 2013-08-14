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

import org.jdom.Element;

import opennlp.ccg.synsem.*;

/**
 * Forward type-raising: X => Y/(Y\X).
 *
 * @author  Jason Baldridge
 * @author  Michael White
 * @version $Revision: 1.5 $, $Date: 2009/07/17 04:23:30 $
 */
public class ForwardTypeRaising extends AbstractTypeRaisingRule {
    
	private static final long serialVersionUID = 1417585756957436141L;

	/** Creates a forward type raising rule with the given parameters. */
    public ForwardTypeRaising (boolean useDollar, Category arg, Category result) {
        super(">T", new Slash('/', new VarModality("i")), new Slash('\\', new VarModality("i")),
              useDollar, arg, result);
    }
    
    /** Returns an XML element representing the rule. */
    public Element toXml() {
    	return super.toXml("forward");
    }

    public String toString() {
        return "X => Y/(Y\\X)";
    }
}

