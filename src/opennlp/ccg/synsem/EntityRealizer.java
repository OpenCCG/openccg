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
 * An interface for grammatical units that realise rhetorico-semantic entities,
 * including both (overt) indexed signs and (covert) unary type changing rules.
 *
 * @author Michael White
 * @author Daniel Couto-Vale
 * @version $Revision: 1.2 $, $Date: 2008/01/03 21:30:12 $
 */
public interface EntityRealizer {

	/**
	 * A map of entity realizers
	 * 
	 * @author Daniel Couto-Vale
	 *
	 */
	public static interface EntityRealizerMap {
		public void setEntityRealizer(LF entity, EntityRealizer entityRealizer);
	}

	/**
	 * @return the supertag
	 */
	public String getSupertag();

	/**
	 * WARNING: For unary type changing rules, TypeChangingRule.POS_STRING is
	 * returned.
	 * 
	 * @return the POS tag.
	 */
	public String getPOS();

}
