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

/** 
 * A sparse feature vector, with size and an iterator over feature-value pairs. 
 * 
 * @author 	Michael White
 * @version	$Revision: 1.6 $, $Date: 2011/01/15 17:52:59 $
 */ 
public interface FeatureVector {
	
	/**
	 * Iterator over feature-value pairs.
	 * Features and values must be retrieved using a pair of calls 
	 * to nextFeature and nextValue, otherwise the behavior is not defined.
	 */
	public interface Iterator {
		
		/** Whether any items remain. */
		public boolean hasNext();
		
		/** Returns the next feature. */
		public Alphabet.Feature nextFeature();
		
		/** Returns the next value. */
		public Float nextValue();
	}
	
	/** Size. */
	public int size();

	/** Iterator over feature-value pairs. */
	public Iterator iterator();
	
	/** Empty iterator. */
	public static Iterator EMPTY_ITERATOR = new Iterator() {
		public boolean hasNext() { return false; }
		public Alphabet.Feature nextFeature() { return null; }
		public Float nextValue() { return null; }
	};
}
