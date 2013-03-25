///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002 Jason Baldridge
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

package opennlp.ccg.hylo;

import opennlp.ccg.synsem.*;

/**
 * A interface for hybrid logic nominals, to allow polymorphism for
 * both Modality labels and variables over Modality labels.
 *
 * @author      Jason Baldridge
 * @author 		<a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 * @version     $Revision: 1.2 $, $Date: 2005/10/19 21:27:15 $
 **/
public interface Mode extends LF {
	
	/**
	 * Gets the name of this mode.
	 */
	String getName();
}

