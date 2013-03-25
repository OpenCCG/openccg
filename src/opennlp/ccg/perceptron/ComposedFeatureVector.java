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

/** 
 * Class for composing feature vectors. 
 * Features from the component feature vectors are assumed to be independent.
 * 
 * @author Michael White
 * @version $Revision: 1.2 $, $Date: 2011/01/15 17:52:59 $
 */ 
public class ComposedFeatureVector implements FeatureVector {

	/** The feature vectors. */
	public final FeatureVector[] featureVectors;
	
	/** Constructor. */
	public ComposedFeatureVector(FeatureVector[] featureVectors) {
		this.featureVectors = featureVectors;
	}
	
	/** Binary constructor. */
	public ComposedFeatureVector(FeatureVector featureVector1, FeatureVector featureVector2) {
		this.featureVectors = new FeatureVector[]{ featureVector1, featureVector2 };
	}
	
	/** Size. */
	public int size() {
		int retval = 0;
		for (FeatureVector fv : featureVectors) retval += fv.size();
		return retval;
	}
	
	/** Returns an iterator over the entries. */
	public Iterator iterator() {
		if (featureVectors.length == 0) return EMPTY_ITERATOR;
		return new Iterator() {
			int i = 0;
			Iterator it = featureVectors[0].iterator();
			public boolean hasNext() { 
				if (it.hasNext()) return true;
				if (i == featureVectors.length-1) return false;
				it = featureVectors[++i].iterator();
				return hasNext(); 
			}
			public Alphabet.Feature nextFeature() { return it.nextFeature(); }
			public Float nextValue() { return it.nextValue(); }
		};
	}
}
