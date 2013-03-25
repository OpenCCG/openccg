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

import java.util.*;

/** 
 * A feature vector represented by a list of features and a list of values.
 * Features are assumed to be unique. 
 * 
 * @author	Michael White
 * @version	$Revision: 1.2 $, $Date: 2011/10/11 03:20:05 $
 */ 
public class FeatureList implements FeatureVector {
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 325935806787566283L;
	
	// features
	private ArrayList<Alphabet.Feature> features;
	// values
	private ArrayList<Float> values;
	
	
	/** Default constructor. */
	public FeatureList() {
		features = new ArrayList<Alphabet.Feature>();
		values = new ArrayList<Float>();
	}
	
	/** Constructor with size. */
	public FeatureList(int size) { 
		features = new ArrayList<Alphabet.Feature>(size);
		values = new ArrayList<Float>(size);
	}
	
	/** Constructor from feature vector. */
	public FeatureList(FeatureVector fv) {
		for (Iterator it = fv.iterator(); it.hasNext(); ) 
			add(it.nextFeature(), it.nextValue());
	}

	
	/** Add feature-value pair. */
	public void add(Alphabet.Feature feature, Float value) {
		features.add(feature); values.add(value);
	}
	
	/** Add feature vector (features assumed distinct). */
	public void add(FeatureVector fv) {
		features.ensureCapacity(size() + fv.size());
		values.ensureCapacity(size() + fv.size());
		for (Iterator it = fv.iterator(); it.hasNext(); ) 
			add(it.nextFeature(), it.nextValue());
	}
	
	/** Get feature at index. */
	public Alphabet.Feature getFeature(int index) { return features.get(index); }
	
	/** Get value at index. */
	public Float getValue(int index) { return values.get(index); }

	
	/** Size. */
	public int size() { return features.size(); }
	
	/** Iterator. */
	public Iterator iterator() {
		return new Iterator() {
			java.util.Iterator<Alphabet.Feature> itF = features.iterator();
			java.util.Iterator<Float> itV = values.iterator();
			public boolean hasNext() { return itF.hasNext(); }
			public Alphabet.Feature nextFeature() { return itF.next(); }
			public Float nextValue() { return itV.next(); }
		};
	}
	
	
	/** toString. */
	public String toString() {
		String retval = "features: ";
		for (int i=0; i < features.size(); i++)
			retval += "<" + getFeature(i).name() + "," + getValue(i) + "> "; 
		return retval;
	}
}
