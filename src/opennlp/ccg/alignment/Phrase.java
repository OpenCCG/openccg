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

import java.util.AbstractList;
import java.util.List;

/**
 * A phrase, i.e., a sequence of words with an associated {@linkplain #getNumber() phrase number}.
 * Phrases may additionally have a {@linkplain #getId() string ID}.
 * To save access time and space, this class is backed by an array of
 * {@link String}s, but extends {@link AbstractList} so that phrases can be iterated over and have 
 * all of the usual convenience methods.
 * <p>
 * Phrases are immutable once created, so calling any of the {@link List#add(Object)},
 * {@link List#remove(int)}, or {@link List#set(int, Object)}
 * methods will throw an {@link UnsupportedOperationException}. Similarly, the
 * {@linkplain #iterator() iterator's} <tt>remove()</tt> method also throws an
 * {@link UnsupportedOperationException}. 
 * <p>
 * For convenience, this class implements the {@link Comparable} interface, comparing
 * phrases by their {@linkplain #getNumber() numbers}.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class Phrase extends AbstractList<String> implements Comparable<Phrase> {

	final Integer number;
	final String id;
	final String[] words;
	
	/**
	 * Creates a new phrase with the given number and list of words.
	 * @see Phrase#Phrase(String, Integer, String...)
	 */
	public Phrase(Integer number, List<String> words) {
		this(number, words.toArray(new String[words.size()]));
	}
	
	/**
	 * Creates a new phrase with the specified number, made up of the given words.
	 * @see Phrase#Phrase(String, Integer, String...)
	 */
	public Phrase(Integer number, String... words) {
		this(null, number, words);
	}
	
	/**
	 * Creates a new phrase with the given number, ID, and list of words.
	 * @see Phrase#Phrase(String, Integer, String...)
	 */
	public Phrase(String id, Integer number, List<String> words) {
		this(id, number, words.toArray(new String[words.size()]));
	}
	
	/**
	 * Creates a new phrase with the specified number and id, made up of the given words.
	 * The ID may be <tt>null</tt>, but the cannot be <tt>null</tt>. The list of words cannot be
	 * <tt>null</tt> or contain <tt>null</tt> members, although it can be empty.
	 * @throws IllegalArgumentException If <code>number</code>, <code>words</code> is <code>null</code>,
	 * or one of the strings in <tt>words</tt> is <tt>null</tt>.
	 */
	public Phrase(String id, Integer number, String... words) {
		checkObject(number, "number");
		checkObject(words, "words");
		
		for(int i = 0; i < words.length; i++) {
			checkObject(words[i], "word " + i);
		}
		
		this.number = number;
		this.id = id;
		this.words = words;
	}

	void checkObject(Object obj, String name) {
		if(obj == null) {
			throw new IllegalArgumentException(name + " is null");
		}
	}
	
	/**
	 * Gets this phrase's ID, if any was specified.
	 * @return The ID of this phrase, possibly <code>null</code>.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return This phrase's number.
	 */
	public Integer getNumber() {
		return number;
	}

	/**
	 * Returns the word in this phrase at the supplied index.
	 */
	@Override
	public String get(int index) {
		return words[index];
	}

	/**
	 * @return The number of words in this phrase.
	 */
	@Override
	public int size() {
		return words.length;
	}

	/**
	 * Compares this phrase to another by comparing their {@linkplain #getNumber() numbers}.
	 * @return The value of <tt>getNumber().compareTo(o.getNumber())</tt>.
	 * @param o The phrase to compare to.
	 * @see Integer#compareTo(Integer)
	 */
	@Override
	public int compareTo(Phrase o) {
		return getNumber().compareTo(o.getNumber());
	}

	/**
	 * Tests whether this phrase is equal to another by first calling the superclass method 
	 * {@link AbstractList#equals(Object)}, then comparing this phrase's number and id to the other.
	 */
	@Override
	public boolean equals(Object o) {
		if(o instanceof Phrase && super.equals(o)) {
			Phrase p = (Phrase)o;
			return number.equals(p.number) && ((id == null && p.id == null) || id.equals(p.id));
		}
		
		return false;
	}

	/**
	 * Generates a hash code for this phrase based on the superclass hash code, its number, and its ID
	 * (if any).
	 */
	@Override
	public int hashCode() {
		int h = 31 * super.hashCode() + number.hashCode();
		
		return (id == null) ? h : h + id.hashCode();
	}

	/**
	 * Gets a string representation of this phrase.
	 * @return For a phrase with number <code>37</code>, ID <code>phrase 3</code>, and words
	 * &quot;Test phrase&quot;, prepends <code>37 (phrase 3): </code> to the result of calling the
	 * superclass method {@link AbstractList#toString()}; 
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(number.toString());
		
		if(id != null) {
			sb.append(" (");
			sb.append(id);
			sb.append(')');
		}
		
		sb.append(": ");
		sb.append(super.toString());
		
		return sb.toString();
	}

}
