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

package opennlp.ccg.perceptron;

import java.util.ArrayList;

import opennlp.ccg.synsem.*;

/** 
 * Class for composing feature extractors. 
 * Features from the component feature extractors are assumed to be independent.
 * 
 * @author Michael White
 * @version $Revision: 1.5 $, $Date: 2011/01/15 17:52:59 $
 */ 
public class ComposedFeatureExtractor implements FeatureExtractor {

	/** The feature extractors. */
	public final FeatureExtractor[] featureExtractors;
	
	/** Constructor. */
	public ComposedFeatureExtractor(FeatureExtractor[] featureExtractors) {
		this.featureExtractors = featureExtractors;
	}
	
	/** Binary constructor. */
	public ComposedFeatureExtractor(FeatureExtractor featureExtractor1, FeatureExtractor featureExtractor2) {
		this.featureExtractors = new FeatureExtractor[]{ featureExtractor1, featureExtractor2 };
	}
	
	/** Constructor for sign scorers, some of which may be feature extractors. */
	public ComposedFeatureExtractor(SignScorer[] models) { 
    	ArrayList<FeatureExtractor> feList = new ArrayList<FeatureExtractor>(models.length);
        for (int i = 0; i < models.length; i++) {
        	if (models[i] instanceof FeatureExtractor) feList.add((FeatureExtractor)models[i]);
        }
        this.featureExtractors = feList.toArray(new FeatureExtractor[feList.size()]);
	}
	
	/** Returns the features for the given sign and completeness flag. */
	public FeatureVector extractFeatures(Sign sign, boolean complete) {
		FeatureVector[] featureVectors = new FeatureVector[featureExtractors.length];
		for (int i=0; i < featureExtractors.length; i++)
			featureVectors[i] = featureExtractors[i].extractFeatures(sign, complete);
		return new ComposedFeatureVector(featureVectors);
	}
	
	/** Sets the alphabet. */
	public void setAlphabet(Alphabet alphabet) {
		for (FeatureExtractor fe : featureExtractors) {
			fe.setAlphabet(alphabet);
		}
	}
}
