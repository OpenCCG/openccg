///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002 Jason Baldridge and Gann Bierner
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

import org.jdom.*;

/**
 * Data structure for storing information about a lexical entry.  Specifically
 * used by LMR grammars.
 *
 * @author      Jason Baldridge
 * @version $Revision: 1.2 $, $Date: 2007/12/17 20:02:23 $
 */
public class DataItem {
    private String stem = "";
    private String pred = "";
    
    public DataItem() {}
    public DataItem (String s, String p) {
		stem = s;
		pred = p;
    }

    
    public DataItem(Element datael) {
		stem = datael.getAttributeValue("stem");	
	
		pred = datael.getAttributeValue("pred");	
		if (null == pred) {
		    pred = stem;
		}
    }

    public void setStem(String s) { stem = s; }
    public void setPred(String s) { pred = s; }

    public String getStem() { return stem; }
    public String getPred() { return pred; }
}
