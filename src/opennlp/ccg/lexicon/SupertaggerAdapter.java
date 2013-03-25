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

package opennlp.ccg.lexicon;

import java.util.*;

/**
 * The SupertaggerAdapter interface is for plugging a supertagger into the 
 * lexicon in order to return only the desired, high probability categories 
 * during lexical lookup.  Once the supertagger has been plugged in, 
 * using Lexicon.setSupertagger, the supertagger will be consulted during 
 * each lexical lookup for the desired categories, using getSupertags. 
 * Note that this entails that the supertagger must update its state between lexical 
 * lookup calls; in this way, identical words in a sentence can have different 
 * predicted categories.
 * 
 * The supertagger returns beta-best categories for lexical lookup 
 * according to a sequence of beta settings it maintains internally.
 * Associated probabilities for each supertag are also returned.
 * The 'include gold' option controls whether gold standard tags are included 
 * during training.
 * 
 * At present, the lexicon must contain appropriate morph items for all words. 
 * However, the supertags assigned to a word need not be limited to those explicitly 
 * listed in the lexicon.  When there is an explicit entry, it will be used, 
 * as doing so allows the specification of a 'pred' which differs from the stem, 
 * as well as macros that can affect the supertag.  Otherwise, when using a supertagger, 
 * it is no longer necessary to list stems with categories in the lexicon, as the 
 * supertagger becomes responsible for this mapping.
 *
 * Note also that at present, only one supertagger (for either parsing or realization) 
 * may be plugged in to the lexicon at a time.
 *  
 * @author      Michael White
 * @version     $Revision: 1.11 $, $Date: 2010/12/08 15:24:26 $
 */
public interface SupertaggerAdapter {
	
	/**
	 * Class for caching a lexical item's log prob in a sign.
	 */
	public static class LexLogProb {
		/** The log prob. */
		public final float logprob;
		/** Constructor. */
		public LexLogProb(float logprob) { this.logprob = logprob; }
	}
	
	/**
	 * Returns the supertags of the desired categories for the current lexical lookup 
	 * as a map from supertags to contextual probabilities (or null to accept all). 
	 */
	public Map<String,Double> getSupertags();
	
	/**
	 * Sets the flag for whether to include gold tags.
	 */
	public void setIncludeGold(boolean includeGold);
	
	/**
	 * Resets beta to the most restrictive value.
	 */
	public void resetBeta();

	/**
	 * Resets beta to the least restrictive value.
	 */
	public void resetBetaToMax();

	/**
	 * Advances beta to the next most restrictive setting.
	 */
	public void nextBeta();
	
	/**
	 * Advances beta to the next less restrictive setting.
	 */
	public void previousBeta();
	
	/**
	 * Returns whether there are any less restrictive beta settings
	 * remaining in the sequence.
	 */
	public boolean hasMoreBetas();

	/**
	 * Returns whether there are any more restrictive beta settings
	 * remaining in the sequence.
	 */
	public boolean hasLessBetas();

	/** Returns all the beta values. */
	public double[] getBetas();
	
	/** Sets the beta values. */
	public void setBetas(double[] betas);
	
	/** Returns the current beta value. */
	public double getCurrentBetaValue();
}
