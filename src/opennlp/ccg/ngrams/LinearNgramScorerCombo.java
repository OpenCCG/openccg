///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-5 University of Edinburgh (Michael White)
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

import java.util.*;

import opennlp.ccg.perceptron.*;
import opennlp.ccg.synsem.Sign;
import opennlp.ccg.lexicon.Word;

/**
 * Linear combination of n-gram probability models, 
 * interpolated at the word level; can also be used 
 * as a feature extractor in a perceptron model.
 * The models must have the same direction.
 *
 * @author      Michael White
 * @version     $Revision: 1.17 $, $Date: 2009/06/22 04:32:47 $
 */
public class LinearNgramScorerCombo extends NgramScorer implements FeatureExtractor
{
    /** The component models. */
    protected NgramScorer[] models;
    
    /** The weights. */
    protected double weights[];
    
    /**
     * Creates a new linear combo model with the given component models 
     * and with the combination weights determined by the rank order 
     * centroid method.  The models are assumed to be ordered from 
     * most to least important.
     */
    public LinearNgramScorerCombo(NgramScorer[] models) { 
        this(models, rankOrderCentroidWeights(models.length));
    }
    
    /**
     * Creates a new linear combo model with the given component models 
     * and combination weights.  The weights are assumed to sum to 1, 
     * and the number of weights is assumed to match the number of models.
     * The wordsToScore list is shared across the component models.
     */
    public LinearNgramScorerCombo(NgramScorer[] models, double[] weights) {
        this.models = models;
        this.weights = weights;
        for (int i = 0; i < models.length; i++) {
            models[i].shareWordsToScore(wordsToScore);
            order = Math.max(order, models[i].order);
        }
    }
    
    /** Set reverse flag, and propagate to component models. */
    public void setReverse(boolean reverse) { 
        super.setReverse(reverse);
        for (int i = 0; i < models.length; i++) {
            models[i].setReverse(reverse);
        }
    }
    
    /** Sets wordsToScore to the given list, for sharing purposes. */
    protected void shareWordsToScore(List<Word> wordsToScore) {
        this.wordsToScore = wordsToScore;
        for (int i = 0; i < models.length; i++) {
            models[i].shareWordsToScore(wordsToScore);
        }
    }
    
    /** Does further preparation before scoring words for each component model. */
    protected void prepareToScoreWords() {
        for (int i = 0; i < models.length; i++) {
            models[i].prepareToScoreWords();
        }
    }
    
    /** Returns the log prob of the ngram starting at the given index 
        in wordsToScore and with the given order, with backoff.
        In particular, returns the linear combination using the established weights 
        of the probabilities given by the component models,
        converted back to a log prob (base 10). */
    protected float logProbFromNgram(int i, int order) {
        double prob = 0;
        for (int j = 0; j < models.length; j++) {
            prob += convertToProb(models[j].logProbFromNgram(i, order)) * weights[j];
        }
        return (float) convertToLogProb(prob);
    }
    
    
	/**
	 * Increments ngram counts for the ngrams starting at the given index in
	 * wordsToScore and with the given order.  The implementation 
	 * delegates to the component models.
	 */
	protected void incNgrams(FeatureMap featmap, int i, int order) {
        for (int j = 0; j < models.length; j++) {
        	if (models[j].useNgramFeatures) models[j].incNgrams(featmap, i, order);
        }
	}
	
	
	/** Flag for including the interpolated log prob as a feature. */
	protected boolean useInterpLogProb = true;
	
	/** Sets the flag for including interpolated log prob as a feature. */
	public void setInterpLogProb(boolean useInterpLogProb) { 
		this.useInterpLogProb = useInterpLogProb; 
	}
	
    /**
     * Returns a feature vector with the log prob features  
     * for the given sign and completeness flag.
	 * In particular, returns the log prob from each model as 
	 * the value of a feature named '$ngramN'.
	 * The interpolated log prob is also returned as a feature '$ngram' if 
	 * the flag for including the interpolated log prob as a feature is set (the default).
     */
    public FeatureVector extractLogProbs(Sign sign, boolean complete) {
		FeatureList retval = new FeatureList(models.length+1);
		if (useInterpLogProb) {
	        Alphabet.Feature f = alphabet.index("$ngram");
			if (f != null) retval.add(f, (float)logprob(sign, complete));
		}
		for (int i=0; i < models.length; i++) {
	        Alphabet.Feature f = alphabet.index("$ngram"+i);
			if (f != null) retval.add(f, (float)models[i].logprob(sign, complete));
		}
		return retval;
    }
    
	/** Sets the alphabet. */
	public void setAlphabet(Alphabet alphabet) {
		super.setAlphabet(alphabet);
		for (NgramScorer scorer : models) {
			scorer.setAlphabet(alphabet);
		}
	}
}

