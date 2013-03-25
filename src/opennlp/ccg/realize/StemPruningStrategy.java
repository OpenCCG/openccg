///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2012 Michael White
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

import opennlp.ccg.lexicon.Word;
import opennlp.ccg.synsem.Sign;

import java.util.*;

/**
 * A diversity pruning strategy that defines signs to be 
 * notCompellinglyDifferent if they have the same sequence of
 * stems. 
 * The empty constructor defaults the singleBestPerGroup flag 
 * to true.
 *
 * @author      Michael White
 * @version     $Revision: 1.1 $, $Date: 2011/04/02 16:32:17 $
 */
public class StemPruningStrategy extends DiversityPruningStrategy
{
    /** Constructor, defaults singleBestPerGroup to true. */
    public StemPruningStrategy() { this(true); }

    /** Full constructor. */
    public StemPruningStrategy(boolean singleBestPerGroup) { 
        this.singleBestPerGroup = singleBestPerGroup;
    }
    
    /** Returns true iff the given signs are not compellingly different.
        In particular, returns true iff the signs have the same
        sequence of stems. */
    public boolean notCompellinglyDifferent(Sign sign1, Sign sign2) {
	List<Word> words1 = sign1.getWords();
	List<Word> words2 = sign2.getWords();
	if (words1.size() != words2.size()) return false;
    	for (int i=0; i < words1.size(); i++) {
	    if (words1.get(i).getStem() != words2.get(i).getStem())
		return false;
    	}
        return true;
    }
}
