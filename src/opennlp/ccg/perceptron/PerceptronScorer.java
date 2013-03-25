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
 * A sign scorer for a perceptron model.
 * 
 * @author Michael White
 * @version     $Revision: 1.7 $, $Date: 2011/03/21 20:46:43 $
 */
public class PerceptronScorer implements SignScorer {

	/** The feature extractor. */
	public final FeatureExtractor featureExtractor;
	
	/** The model. */
	public final Model model;
	
	/** Constructor. */
	public PerceptronScorer(FeatureExtractor featureExtractor, Model model) {
		this.featureExtractor = featureExtractor;
		this.model = model;
		featureExtractor.setAlphabet(model.getAlphabet());
	}
	
    /** 
     * Returns a score for the given sign and completeness flag, where higher 
     * numbers are better than lower numbers.
     * In particular, returns the score assigned by the model to the features 
     * extracted from the given sign with the given completeness flag.
     */
    public double score(Sign sign, boolean complete) {
    	return model.score(featureExtractor.extractFeatures(sign, complete));
    }
}
