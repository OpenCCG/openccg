///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2005 University of Edinburgh (Michael White)
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

package opennlp.ccg.ngrams;

import opennlp.ccg.lexicon.Word;
import opennlp.ccg.realize.*;
import opennlp.ccg.synsem.Sign;

import java.util.*;

/**
 * A diversity pruning strategy that defines signs to be 
 * notCompellinglyDifferent if the n-1 initial and final words 
 * are the same, where n is the n-gram order of interest.
 * The single arg constructor defaults the singleBestPerGroup flag 
 * to true, which can increase efficiency with no loss in quality 
 * when only the single best output is of interest (as long as the 
 * reduction in the search space outweighs the extra time necessary 
 * to check for the same initial and final words).
 *
 * @author      Michael White
 * @version     $Revision: 1.3 $, $Date: 2009/12/21 03:27:18 $
 */
public class NgramDiversityPruningStrategy extends DiversityPruningStrategy
{
    /** The n-gram order. */
    protected int order;
    
    /** Constructor that defaults singleBestPerGroup to true. */
    public NgramDiversityPruningStrategy(int order) { this(order, true); }
    
    /** Full constructor. */
    public NgramDiversityPruningStrategy(int order, boolean singleBestPerGroup) { 
        this.order = order; this.singleBestPerGroup = singleBestPerGroup;
    }
    
    /** Returns true iff the given signs are not compellingly different.
        In particular, returns true iff the n-1 initial and final words are the same. */
    public boolean notCompellinglyDifferent(Sign sign1, Sign sign2) {
        List<Word> words1 = sign1.getWords(); List<Word> words2 = sign2.getWords();
        int words1Len = words1.size(); int words2Len = words2.size();  
        for (int i = 0; i < order-1 && i < words1Len && i < words2Len; i++) {
            if (words1.get(i) != words2.get(i)) return false;
        }
        int wordsLenDiff = words1Len-words2Len;
        for (int i = words1Len-1; i > words1Len-order && i >= 0 && i >= wordsLenDiff; i--) {
            int j = i - wordsLenDiff;
            if (words1.get(i) != words2.get(j)) return false;
        }
        return true;
    }
}

