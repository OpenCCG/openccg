///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002-4 Jason Baldridge, Gann Bierner and 
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
package opennlp.ccg.synsem;

import org.jdom.*;

/**
 * Utility class to build categories.
 *
 * @author      Jason Baldridge
 * @author      Gann Bierner
 * @author      Michael White
 * @version     $Revision: 1.4 $, $Date: 2005/10/18 22:20:15 $
 */
public class CatReader {
    
    public static Category getCat(Element catel) {
        Category cat = null;
        String catType = catel.getName();
        
        if (catType.equals("atomcat") || catType.equals("ac")) {    
            cat =  new AtomCat(catel);
        }
        
        else if (catType.equals("complexcat") || catType.equals("cc")) {    
            cat =  new ComplexCat(catel);
        }

        return cat;
    }
}
