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

import opennlp.ccg.synsem.*;
import org.jdom.*;

/**
 * Data structure for storing information about a category family entry.
 *
 * @author  Jason Baldridge
 * @author  Gann Bierner
 * @author  Michael White
 * @version $Revision: 1.10 $, $Date: 2009/10/17 20:46:20 $
 */
public class EntriesItem {
    
    private Family family;
    private Boolean active = Boolean.TRUE;
    private String name;
    private String stem;
    private String indexRel;
    private Category cat;

    public EntriesItem(Element el, Family family) {
        this.family = family;
        name = el.getAttributeValue("name");

        stem = el.getAttributeValue("stem");
        if (stem == null) stem = Lexicon.DEFAULT_VAL; 

        String isActive = el.getAttributeValue("active");
        if (isActive != null && isActive.equals("false"))
            active = Boolean.FALSE;

        String indexRelVal = el.getAttributeValue("indexRel");
        if (indexRelVal != null) indexRel = indexRelVal;
        else indexRel = family.getIndexRel();

        cat = CatReader.getCat((Element)el.getChildren().get(0));
    }

    public Boolean getActive() { return active; }
    /** Returns the name of this entry. */
    public String getName() { return name; }
    /** Returns the qualified name in the form familyName.name. */ 
    public String getQualifiedName() { return getFamilyName() + "." + name; }
    /** Returns the name of this entry's family. */
    public String getFamilyName() { return family.getName(); }
    /** Returns the supertag of this entry's category. */
    public String getSupertag() { return cat.getSupertag(); }
    public Family getFamily() { return family; }
    public String getStem() { return stem; }
    public String getIndexRel() { return indexRel; }
    /** Returns this entry's family's coart rel. */
    public String getCoartRel() { return family.getCoartRel(); }
    public Category getCat() { return cat; }
        
    public String toString () {
        return getQualifiedName() + ":" + stem + " :- " + cat;
    }
}
