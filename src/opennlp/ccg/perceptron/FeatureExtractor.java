///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2008 Michael White
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

package opennlp.ccg.perceptron;

import opennlp.ccg.synsem.*;

/** 
 * Interface for mappings signs to features. 
 * 
 * @author Michael White
 * @version     $Revision: 1.5 $, $Date: 2009/06/22 04:32:50 $
 */ 
public interface FeatureExtractor {
	
	/** Returns the features for the given sign and completeness flag. */
	public FeatureVector extractFeatures(Sign sign, boolean complete);
	
	/** Sets the alphabet. */
	public void setAlphabet(Alphabet alphabet);
}
