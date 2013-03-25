///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2009 Michael White
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

import java.util.*;

import opennlp.ccg.perceptron.*;
import opennlp.ccg.lexicon.SupertaggerAdapter;

/** 
 * A class for extracting total lexical log probabilities from a supertagger 
 * as a feature.  The class may also be used as a sign scorer.
 * 
 * @author 	Michael White
 * @version $Revision: 1.3 $, $Date: 2009/11/01 22:26:29 $
 */ 
public class LexLogProbFeatureExtractor implements FeatureExtractor, SignScorer {

	/** Feature key. */
	public static String lexlogprobkey = "lexlogprob";
	
	/** The alphabet. */
	protected Alphabet alphabet = null;

	/** Lexical logprob feature. */
	protected Alphabet.Feature lexlogprobFeature = null;
	
	/** Sets the alphabet. */
	public void setAlphabet(Alphabet alphabet) {
		this.alphabet = alphabet;
		List<String> keys = new ArrayList<String>(1);
		keys.add(lexlogprobkey);
		lexlogprobFeature = alphabet.closed() ? alphabet.index(keys) : alphabet.add(keys);
	}
	
	/** Returns the features for the given sign and completeness flag. */
	public FeatureVector extractFeatures(Sign sign, boolean complete) {
		return lexLogProbVector(getLexLogProb(sign, complete));
	}
	
	/** Recursively gets lex log prob total for the given sign, if not already present. */
	protected float getLexLogProb(Sign sign, boolean complete) {
		// check for stored log prob 
		SupertaggerAdapter.LexLogProb lexlogprob = 
			(SupertaggerAdapter.LexLogProb) sign.getData(SupertaggerAdapter.LexLogProb.class);
		if (lexlogprob != null) return lexlogprob.logprob;
		// otherwise calculate and store one
		float logprob = 0;
		// lex case
		if (sign.isLexical()) {
			// just use zero if not already there
		}
		// non-terminal
		else {
			// use input totals to calculate current one
			Sign[] inputs = sign.getDerivationHistory().getInputs();
			if (inputs.length == 1) 
				logprob = getLexLogProb(inputs[0], false);
			else if (inputs.length == 2) 
				logprob = getLexLogProb(inputs[0], false) + getLexLogProb(inputs[1], false);
		}
		// store it and return
		sign.addData(new SupertaggerAdapter.LexLogProb(logprob));
		return logprob;
	}
	
	/** Returns a feature vector with the given lex log prob total. */
	protected FeatureVector lexLogProbVector(float logprob) {
		FeatureList retval = new FeatureList(1);
		if (lexlogprobFeature != null) retval.add(lexlogprobFeature, logprob);
		return retval;
	}
	
    /** 
     * Returns a score for the given sign and completeness flag; 
     * specifically, returns the lex log prob total for the sign.
     */
    public double score(Sign sign, boolean complete) {
    	return getLexLogProb(sign, complete);
    }
}
