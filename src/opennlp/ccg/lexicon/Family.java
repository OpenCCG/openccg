///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002-9 Jason Baldridge, Gann Bierner and 
//                      University of Edinburgh / Michael White
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
import java.util.*;

/**
 * Lexicon category family.
 *
 * @author      Jason Baldridge
 * @author      Gann Bierner
 * @author      Michael White
 * @version     $Revision: 1.11 $, $Date: 2010/11/30 18:51:05 $
 */
public class Family {
	
    private String name = "";
    private Boolean closed = Boolean.FALSE;
    private String pos = "";
    private String indexRel = ""; 
    private String coartRel = ""; 
    private DataItem[] data;
    private EntriesItem[] entries;

    @SuppressWarnings("unchecked")
	public Family(Element famel) {
    	
        setName(famel.getAttributeValue("name"));
        pos = famel.getAttributeValue("pos");
        
        String isClosed = famel.getAttributeValue("closed");
        if (isClosed != null && isClosed.equals("true")) {
            setClosed(Boolean.TRUE);
        }

        String indexRelVal = famel.getAttributeValue("indexRel");
        if (indexRelVal != null) { indexRel = indexRelVal; }

        String coartRelVal = famel.getAttributeValue("coartRel");
        if (coartRelVal != null) { coartRel = coartRelVal; }

        List<Element> entriesList = famel.getChildren("entry");
        entries = new EntriesItem[entriesList.size()];
        for (int j=0; j < entriesList.size(); j++) {
            entries[j] = new EntriesItem(entriesList.get(j), this);
        }
        
        List<Element> members = famel.getChildren("member");
        data = new DataItem[members.size()];
        for (int j=0; j < members.size(); j++) {
            data[j] = new DataItem(members.get(j));
        }
    }

    public Family(String s) { setName(s); }

    public boolean isClosed() { return closed.booleanValue(); }
    
    public void setName(String s) { name = s; }
    public void setClosed(Boolean b) { closed = b; }
    public void setPOS(String s) { pos = s; }
    public void setIndexRel(String s) { indexRel = s; }
    public void setCoartRel(String s) { coartRel = s; }
    public void setData(DataItem[] dm) { data = dm; }
    public void setEntries(EntriesItem[] em) { entries = em; }

    public String getName() { return name; }
    /** Delegates to first entry. */
    public String getSupertag() { return entries[0].getSupertag(); } 
    public Boolean getClosed() { return closed; }
    public String getPOS() { return pos; }
    public String getIndexRel() { return indexRel; }
    public String getCoartRel() { return coartRel; }
    public DataItem[] getData() { return data; }
    public EntriesItem[] getEntries() { return entries; }
}
