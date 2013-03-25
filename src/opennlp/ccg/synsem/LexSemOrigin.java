///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2007 Michael White
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

/**
 * An interface for items which introduce lexical semantics, covering 
 * (lexical) signs and unary type changing rules.
 *
 * @author      Michael White
 * @version     $Revision: 1.2 $, $Date: 2008/01/03 21:30:12 $
 */
public interface LexSemOrigin {
	
    /**
     * Returns the supertag.
     */
    public String getSupertag();
    
    /**
     * Returns the POS tag. 
     * For unary type changing rules, the constant TypeChangingRule.POS_STRING is 
     * always returned.
     */
    public String getPOS();
    
    /**
     * Sets the origin of the elementary predications.
     */
    public void setOrigin();
}
