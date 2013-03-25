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
 * A feature vector represented using a <Alphabet.Feature,Float> map. 
 * 
 * @author Michael White
 * @version     $Revision: 1.9 $, $Date: 2011/10/11 03:20:06 $
 */ 
public class FeatureMap implements FeatureVector {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = -5054109887068464041L;

	// the map
	private HashMap<Alphabet.Feature,Float> map;
	
	/** Default constructor. */
	public FeatureMap() { map = new LinkedHashMap<Alphabet.Feature,Float>(); } 
	
	/** Constructor with size. */
	public FeatureMap(int size) { map = new LinkedHashMap<Alphabet.Feature,Float>(size*2); }
	
	/** Constructor from feature vector. */
	public FeatureMap(FeatureVector fv) { 
		this(fv.size()*4);
		for (Iterator it = fv.iterator(); it.hasNext(); ) 
			map.put(it.nextFeature(), it.nextValue());
	}
	
	/** Constructor from two feature vectors. */
	public FeatureMap(FeatureVector fv1, FeatureVector fv2) { 
		this((fv1.size()+fv2.size())*3);
		for (Iterator it = fv1.iterator(); it.hasNext(); ) 
			map.put(it.nextFeature(), it.nextValue());
		add(fv2);
	}
	
	
	/** Increments a feature count. */
	public void inc(Alphabet.Feature feature) {
		float count = 1;
		Float val = map.get(feature);
		if (val != null) count = Math.round(val) + 1;
		map.put(feature, count);
	}
	
	/** Adds to a feature's value (starting with zero). */
	public void add(Alphabet.Feature feature, Float value) {
		Float val = map.get(feature);
		if (val != null) map.put(feature, val + value);
		else map.put(feature, value);
	}
	
	/** Adds a feature vector. */
	public void add(FeatureVector fv) {
		for (Iterator it = fv.iterator(); it.hasNext(); ) 
			add(it.nextFeature(), it.nextValue());
	}
	
	/** Returns the feature's value (zero if not present). */
	public float get(Alphabet.Feature feature) {
		Float retval = map.get(feature);
		return (retval != null) ? retval : 0;
	}
	
	/** Clears the map. */
	public void clear() { map.clear(); }
	
	/** Size. */
	public int size() { return map.size(); }
	
	/** Returns an iterator over the entries. */
	public Iterator iterator() {
		return new Iterator() {
			java.util.Iterator<Map.Entry<Alphabet.Feature,Float>> it = map.entrySet().iterator();
			Map.Entry<Alphabet.Feature,Float> entry = null;
			public boolean hasNext() { return it.hasNext(); }
			public Alphabet.Feature nextFeature() { entry = it.next(); return entry.getKey(); }
			public Float nextValue() { return entry.getValue(); }
		};
	}
}
