///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2011 Michael White
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

import gnu.trove.THashSet;
import gnu.trove.TObjectIdentityHashingStrategy;

import java.util.*;

/**
 * A diversity pruning strategy that defines signs to be 
 * notCompellinglyDifferent if the set of open class stems 
 * is the same.  The POS classes of interest are set in the 
 * constructor.
 *
 * @author      Michael White
 * @version     $Revision: 1.1 $, $Date: 2011/04/02 16:32:17 $
 */
public class LexicalDiversityPruningStrategy extends DiversityPruningStrategy
{
    /** The interned POS values to use for relevant open class stems. */
    @SuppressWarnings("unchecked")
	protected Set<String> posValsToUse = new THashSet(new TObjectIdentityHashingStrategy());
    
    /** Reusable set of observed interned stems for comparison purposes. */
    @SuppressWarnings("unchecked")
	protected Set<String> stemsSeen = new THashSet(new TObjectIdentityHashingStrategy());
    
    /** Constructor, which sets POS classes of interest. */
    public LexicalDiversityPruningStrategy() {
    	String[] poslist = {
    		"JJ", "JJR", "JJS",
    		"NN", "NNP", "NNS", "NNPS",
    		"RB", "RBR", "RBS",
    		"VB", "VBD", "VBG", "VBN", "VBP", "VBZ"
    	};
    	for (String pos : poslist) posValsToUse.add(pos);
    }
    
    /** Returns true iff the given signs are not compellingly different.
        In particular, returns true iff the set of relevant open class stems are the same. */
    public boolean notCompellinglyDifferent(Sign sign1, Sign sign2) {
    	stemsSeen.clear();
    	for (Word w : sign1.getWords()) {
    		if (posValsToUse.contains(w.getPOS())) stemsSeen.add(w.getStem());
    	}
    	for (Word w : sign2.getWords()) {
    		if (posValsToUse.contains(w.getPOS()) && !stemsSeen.contains(w.getStem()))
    			return false;
    	}
        return true;
    }
}
