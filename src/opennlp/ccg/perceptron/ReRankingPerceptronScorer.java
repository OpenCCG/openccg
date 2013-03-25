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

package opennlp.ccg.perceptron;

import opennlp.ccg.synsem.*;

/**
 * A re-ranking sign scorer for a perceptron model.
 * Note that at present, n-best re-ranking has been found to work better 
 * for parsing, but not for realization, where forest re-ranking (ie using 
 * the perceptron scorer throughout) seems to work better.
 * 
 * @author Michael White
 * @version     $Revision: 1.1 $, $Date: 2011/03/21 20:46:32 $
 */
public abstract class ReRankingPerceptronScorer extends PerceptronScorer implements ReRankingScorer {

	/** Flag for whether to use the full model. */
    protected boolean useFullModel = false;

    /** Sets the full model flag. */
    public void setFullModel(boolean on) { useFullModel = on; }

	/** The base scorer, for use when the full model is turned off. */
    protected SignScorer baseScorer;

    /** Returns the base scorer, using the given feature extractor if desired. */
    abstract protected SignScorer getBaseScorer(FeatureExtractor featureExtractor);

    /** Constructor that configures the base scorer using getBaseScorer. */
	public ReRankingPerceptronScorer(FeatureExtractor featureExtractor, Model model) {
		super(featureExtractor, model);
		baseScorer = getBaseScorer(featureExtractor);
	}

    /** Scores the sign with the full or base model, according to the full model flag. */
    public double score(Sign sign, boolean complete) {
		if (useFullModel) return super.score(sign, complete);
		else return baseScorer.score(sign, complete);
    }
}
