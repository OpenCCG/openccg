///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-4 Jason Baldridge, Gann Bierner and 
//                      University of Edinburgh (Michael White)
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
package opennlp.ccg.lexicon;

import opennlp.ccg.unify.*;
import opennlp.ccg.synsem.*;

/**
 * Data structure for storing information about morphological macros.
 *
 * @author Jason Baldridge
 * @author Gann Bierner
 * @author Michael White
 * @author Daniel Couto-Vale
 * @version $Revision: 1.5 $, $Date: 2009/12/21 03:27:18 $
 */
public class MacroItem {
    private final String name;
    private final FeatureStructure[] featStrucs;
    private final LF[] preds;
    
    public MacroItem() {
    	name = null;
    	featStrucs = null;
    	preds = null;
    };

    public MacroItem (String name, FeatureStructure[] featStrucs, LF[] preds) {
		this.name = name;
		this.featStrucs = featStrucs;
		this.preds = preds;
    }

    public String getName() {
        return name;
    }

    public FeatureStructure[] getFeatureStructures() {
        return featStrucs;
    }
    
    public LF[] getPreds() {
        return preds;
    }

}
