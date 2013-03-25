//////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2012 Scott Martin
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
package opennlp.ccg.alignment;


/**
 * Class for representing groups of mappings during reading or writing.
 * Mapping groups encapsulate a phrase number (usually associated with a
 * phrase's line number) and a number of mappings in the group (its
 * {@linkplain #getLength() length}).
 * 
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 */
public class MappingGroup implements Comparable<MappingGroup> {

	Integer phraseNumber;
	int length;
	
	/**
	 * Creates a new mapping group with the given phrase number and length.
	 * @throws IllegalArgumentException if <tt>phraseNumber</tt> is <tt>null</tt>, or if
	 * <tt>length &lt; 0</tt>.
	 */
	public MappingGroup(Integer phraseNumber, int length) {
		if(phraseNumber == null) {
			throw new IllegalArgumentException("phraseNumber is null");
		}
		if(length < 0) {
			throw new IllegalArgumentException("length < 0: " + length);
		}
		
		this.phraseNumber = phraseNumber;
		this.length = length;
	}
	
	/**
	 * Gets this group's phrase number.
	 */
	public Integer getPhraseNumber() {
		return phraseNumber;
	}

	/**
	 * Gets the length of this mapping group.
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Tests whether this group is equal to another by comparing the two groups'
	 * phrase numbers and lengths.
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof MappingGroup) {
			MappingGroup ag = (MappingGroup)obj;
			return phraseNumber.equals(ag.phraseNumber) && length == ag.length;
		}
		
		return false;
	}

	/**
	 * Compares this mapping group to another by comparing their IDs and lengths.
	 */
	public int compareTo(MappingGroup ag) {
		int i = phraseNumber.compareTo(ag.phraseNumber);
		
		if(i == 0) {
			i = (length == ag.length) ? 0 : length < ag.length ? -1 : 1;
		}
		
		return i;
	}

	/**
	 * Computes a hash code for this mapping group based on its ID and length.
	 */
	@Override
	public int hashCode() {
		// brackets are to guard against 0
		return 37 * (1 + phraseNumber.intValue() + length);
	}

	/**
	 * Gets a string representation of this group.
	 * @return For a group with phrase number <code>37</code> and length 
	 * <code>12</code>, the string &quot;<code>Group 37 (12 mappings)</code>&quot;. 
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Group ");
		sb.append(phraseNumber);
		sb.append(" (");
		sb.append(length);
		sb.append(" mappings)");
		
		return sb.toString();
	}

}
