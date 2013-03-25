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
import opennlp.ccg.hylo.HyloHelper;

import org.jdom.*;
import java.util.*;

/**
 * Data structure for storing information about morphological macros.
 *
 * @author      Jason Baldridge
 * @author      Gann Bierner
 * @author      Michael White
 * @version $Revision: 1.5 $, $Date: 2009/12/21 03:27:18 $
 */
public class MacroItem {
    private String name;
    private FeatureStructure[] featStrucs;
    private LF[] preds;
    
    public MacroItem() {};

    @SuppressWarnings("unchecked")
	public MacroItem (Element e) {
        name = e.getAttributeValue("name");
        if (name == null) { name = e.getAttributeValue("n"); }
        List<Element> fsEls = e.getChildren("fs");
        featStrucs = new FeatureStructure[fsEls.size()];
        for (int i=0; i<featStrucs.length; i++) {
            featStrucs[i] = new GFeatStruc(fsEls.get(i));
        }
        Element lfElt = e.getChild("lf");
        if (lfElt == null) preds = new LF[0];
        else {
            List<Element> predElts = lfElt.getChildren();
            preds = new LF[predElts.size()];
            for (int i=0; i < predElts.size(); i++) {
                preds[i] = HyloHelper.getLF(predElts.get(i));
            }
        }
    }
    
    public void setName(String s) {
        name=s;
    }

    //public void setSpecs(ArrayList al) {specs = al; }

    public String getName() {
        return name;
    }

    public FeatureStructure[] getFeatureStructures() {
        return featStrucs;
    }
    
    public LF[] getPreds() {
        return preds;
    }

    //public void addSpec(String s) { specs.add(s); }
    //public void removeSpec(String s) {
    //  specs.remove(specs.indexOf(s));
    //}
    
}
