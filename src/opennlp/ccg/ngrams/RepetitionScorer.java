///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004 University of Edinburgh (Michael White)
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

import opennlp.ccg.synsem.Sign;
import opennlp.ccg.synsem.SignScorer;
import opennlp.ccg.lexicon.Word;

import java.util.*;
import gnu.trove.*;

/**
 * Scores a sign according to how repetitive its words are given the 
 * observed context.  Relevant repeated items (eg stems) are counted, 
 * with full counts given to items in the previous words or recent context, 
 * and fractional counts to older items.  The score is then assigned according 
 * to the number of repeated items and the configured penalty, as 
 * 10 to the minus (penalty times repeated items).
 *
 * @author      Michael White
 * @version     $Revision: 1.6 $, $Date: 2011/03/20 20:11:58 $
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class RepetitionScorer implements SignScorer
{
    /** The repetition penalty (defaults to 1.0). */
    public double penalty = 1.0;

    /** The fractional count for the older items (defaults to 0.5). */
    public double olderCount = 0.5;
    
    /** The fractional count for the even older items (defaults to 0.25). */
    public double evenOlderCount = 0.25;
    
    /** The fractional count for the oldest items (defaults to 0.125). */
    public double oldestCount = 0.125;
    
    /** The interned POS values to use for repetition scoring purposes. */
    protected Set posValsToUse = new THashSet(new TObjectIdentityHashingStrategy());
    
    /** The interned stems to ignore for repetition scoring purposes. */
    protected Set stemsToIgnore = new THashSet(new TObjectIdentityHashingStrategy());
    
    /** The interned items (eg stems) seen in the previous words. */
    protected Set previousItems = new THashSet(new TObjectIdentityHashingStrategy());
    
    /** The interned items (eg stems) seen in the recent context. */
    protected Set contextItems = new THashSet(new TObjectIdentityHashingStrategy());
    
    /** The interned items (eg stems) seen in the older context. */
    protected Set olderContextItems = new THashSet(new TObjectIdentityHashingStrategy());
    
    /** The interned items (eg stems) seen in the even older context. */
    protected Set evenOlderContextItems = new THashSet(new TObjectIdentityHashingStrategy());
    
    /** The interned items (eg stems) seen in the oldest context. */
    protected Set oldestContextItems = new THashSet(new TObjectIdentityHashingStrategy());
    
    /**
     * Default constructor.
     * Adds "NNP", "N", "V", "Adj" and "Adv" to posValsToUse, 
     * and "do" and "not" to stemsToIgnore.
     */
    public RepetitionScorer() {
        String[] posVals = { "NNP", "N", "V", "Adj", "Adv" };
        posValsToUse.addAll(Arrays.asList(posVals));
        String[] stems = { "do", "not" };
        stemsToIgnore.addAll(Arrays.asList(stems));
    }
    
    /** Resets all the context items. */
    public void resetContext() { 
        contextItems.clear(); olderContextItems.clear(); 
        evenOlderContextItems.clear(); oldestContextItems.clear();
    }
    
    /** Ages the context items, clearing the recent ones. */
	public void ageContext() { 
        oldestContextItems.clear(); oldestContextItems.addAll(evenOlderContextItems);
        evenOlderContextItems.clear(); evenOlderContextItems.addAll(olderContextItems);
        olderContextItems.clear(); olderContextItems.addAll(contextItems);
        contextItems.clear(); 
    }
    
    /** Adds the items (eg stems) from the given sign's words to the context items. */
    public void updateContext(Sign sign) {
        List words = sign.getWords(); 
        if (words == null) return;
        for (int i = 0; i < words.size(); i++) {
            Word word = (Word) words.get(i);
            updateItems(word, contextItems);
        }
    }
    
    /** 
     * Adds the items (eg stems) from the given word to the given set.
     * By default, adds the relevant stems, per the relevantStem method.
     */
    protected void updateItems(Word word, Set set) {
        String stem = relevantStem(word);
        if (stem != null) set.add(stem);
    }

    /**
     * Returns the stem of the given word if its POS is in posValsToUse, 
     * unless the stem is in stemsToIgnore; otherwise returns null.
     */
    protected String relevantStem(Word word) {
        if (!(posValsToUse.contains(word.getPOS()))) return null;
        String stem = word.getStem();
        if (!(stemsToIgnore.contains(stem))) return stem;
        return null;
    }
    
    /** 
     * Returns a score between 0 (worst) and 1 (best) for the given sign 
     * and completeness flag, according to how repetitive its word are compared to 
     * the observed context.  
     * In particular, returns 10 to the minus (penalty times repeated items), 
     * or zero if there are no words.
     */
    public double score(Sign sign, boolean complete) {
        List words = sign.getWords(); 
        if (words == null) return 0;
        return Math.pow(10, -1 * penalty * repeatedItems(words));
    }
    
    /** 
     * Returns the number of repeated items (eg stems) in the given word list, 
     * using fractional counts for repetitions of older items.
     * The previous items set is cleared, and then the repeated items 
     * are summed for each word, updating the previous items along the way.
     */
    protected double repeatedItems(List words) {
        previousItems.clear();
        double retval = 0;
        for (int i = 0; i < words.size(); i++) {
            Word word = (Word) words.get(i);
            retval += repeatedItems(word);
            updateItems(word, previousItems);
        }
        return retval;
    }
    
    /** 
     * Returns the number of repeated items (eg stems) in the given word, 
     * using fractional counts for repetitions of older items.
     * By default, returns 1 (or a fractional count) if the stem is relevant, 
     * per the relevantStem method. 
     */
    protected double repeatedItems(Word word) {
        String stem = relevantStem(word);
        if (stem == null) return 0;
        if (contextItems.contains(stem) || previousItems.contains(stem)) return 1;
        if (olderContextItems.contains(stem)) return olderCount;
        if (evenOlderContextItems.contains(stem)) return evenOlderCount;
        if (oldestContextItems.contains(stem)) return oldestCount;
        return 0;
    }
}

