///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2008-9 Michael White
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

package opennlp.ccg.realize;

import opennlp.ccg.lexicon.*;
import opennlp.ccg.hylo.*;
import java.util.*;

/**
 * A hypertagger is a realization supertagger.  It must extend the 
 * SupertaggerAdapter interface for plugging a supertagger into the 
 * lexicon.
 * 
 * @author      Michael White
 * @version     $Revision: 1.5 $, $Date: 2009/08/24 01:56:14 $
 */
public interface Hypertagger extends SupertaggerAdapter {
	
	/**
	 * Maps the given elementary predications to their predicted categories, 
	 * so that the beta-best categories can be returned by calls to setPred
	 * and getSupertags.
	 */
	public void mapPreds(List<SatOp> preds);
	
	/**
	 * Sets the current elementary predication to the one with the given index, 
	 * so that the beta-best categories for it can be returned by a call to 
	 * getSupertags.
	 */
	public void setPred(int index);
	
	/** 
	 * Stores the gold standard pred info, for use in discriminative training.
	 * The string consists of space delimited tokens, where each token 
	 * is a colon-separated list of fields, with the first field containing 
	 * the nominal id, and the second field the gold supertag.
	 */
	public void storeGoldStdPredInfo(String goldStdPredInfo);
}
