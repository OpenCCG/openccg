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

import static opennlp.ccg.alignment.PhrasePosition.A;
import static opennlp.ccg.alignment.PhrasePosition.B;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import opennlp.ccg.util.DelegatedFilter;
import opennlp.ccg.util.Filter;
import opennlp.ccg.util.FilteredSet;
import opennlp.ccg.util.VisitedFilter;

/**
 * An alignment consisting of a pair of phrases and a set of mappings between them.
 * <p>
 * This class is a flat representation of the mappings between indices in its 
 * {@linkplain #getA() A-position} phrase and its {@linkplain #getB() B-position} phrase in that it is
 * simply a set of mappings. More granularity is available by calling
 * {@link #getTargets(Integer, PhrasePosition)}, which returns all the indices a certain index is mapped to
 * from a specified position.
 * Alignments also allow their indices to be accessed when the phrase position is not necessarily known, 
 * via {@link #get(PhrasePosition)}, {@link #getIndices(PhrasePosition)}, and {@link #asMap(PhrasePosition)}.
 * <p>
 * A detached view of this alignment as a map whose keys are the indices in a
 * specified position and whose values are the sets of indices that index maps to can be obtained by calling
 * {@link #asMap(PhrasePosition)}. If only the set of indices mapped to by a certain index is required,
 * {@link #getTargets(Integer, PhrasePosition)} provides similar functionality. The static method
 * {@link #fromMap(Phrase, Phrase, Map)} allows an alignment to be reconstructed from a map of indices to
 * sets of indices. 
 * <p>
 * A version of this alignment with the phrase positions reversed and all the mappings
 * {@linkplain Mapping#reverse() reversed} can be obtained by calling {@link #reverse()}.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 * @see PhrasePosition
 * @see Phrase
 * @see Mapping
 */
public class Alignment extends AbstractSet<Mapping> implements Comparable<Alignment> {

	final Phrase a, b;
	final Set<Mapping> mappings;
	
	/**
	 * Creates a new alignment with the specified phrases and mappings between them. The specified set of
	 * mappings is copied in a way that preserves whatever ordering is present in the original set,
	 * via {@link LinkedHashSet}.
	 * 
	 * @param a The phrase to use for {@linkplain PhrasePosition#A the &quot;A&quot; position}.
	 * @param b The phrase to use for {@linkplain PhrasePosition#B the &quot;B&quot; position}.
	 * @param mappings The mappings between <tt>a</tt> and <tt>b</tt>, where the
	 * {@linkplain Mapping#getA() first index} is understood to belong to <tt>a</tt> and the
	 * {@linkplain Mapping#getB() second index} is understood to belong to <tt>b</tt>.
	 * 
	 * @throws IllegalArgumentException If either phrase is null, or if phrases <tt>a</tt> and <tt>b</tt> do not
	 * have matching {@linkplain Phrase#getNumber() numbers}, if <tt>mappings</tt> is <tt>null</tt>,
	 * or if any of the mappings have a non-null phrase number that is not equal to the phrases' numbers. 
	 * @throws IndexOutOfBoundsException If any of the mappings contains an index that does not
	 * exist in the phrase in the corresponding position.
	 * 
	 * @see LinkedHashSet
	 */
	public Alignment(Phrase a, Phrase b, Collection<? extends Mapping> mappings) {
		checkPhrases(a, b);
		
		if(mappings == null) {
			throw new IllegalArgumentException("mappings is null");
		}
		
		// have to set these first or checkMapping() throws exception
		this.a = a;
		this.b = b;
		
		for(Mapping m : mappings) {
			checkMapping(m);
		}
		
		
		this.mappings = new LinkedHashSet<Mapping>(mappings);
	}
	
	/**
	 * Creates a new alignment based on the specified phrases and map view of their mappings.
	 * @param a The {@linkplain PhrasePosition#A A-position} phrase.
	 * @param b The {@linkplain PhrasePosition#B B-position} phrase.
	 * @param map A map whose keys are the A-position indices and whose values are the B-position indices 
	 * that the corresponding key is mapped to.
	 * @return A new alignment with mappings created based on the specified <tt>map</tt>.
	 * 
	 * @see #asMap()
	 */
	public static Alignment fromMap(Phrase a, Phrase b, Map<Integer, Set<Integer>> map) {
		@SuppressWarnings("unchecked")
		Set<Mapping> ms = map.isEmpty() ? Collections.EMPTY_SET : new LinkedHashSet<Mapping>();
		
		for(Integer k : map.keySet()) {
			for(Integer v : map.get(k)) {
				ms.add(new Mapping(a.getNumber(), k, v));
			}
		}
		
		return new Alignment(a, b, ms);
	}
	
	/**
	 * Creates an alignment based on this one except that the phrases have
	 * switched positions and all of the mappings are reversed.
	 * 
	 * @return A new alignment with the phrases swapped and all the mappings'
	 * indices swapped.
	 * 
	 * @see Mapping#reverse()
	 */
	public Alignment reverse() {
		@SuppressWarnings("unchecked")
		Alignment r = new Alignment(getB(), getA(), Collections.EMPTY_SET);
		
		for(Mapping m : mappings) {
			r.add(m.reverse());
		}
		
		return r;
	}

	/**
	 * Gets this alignment's number.
	 * @return The value of the {@linkplain Phrase#getNumber() number} of the phrase in
	 * both {@linkplain PhrasePosition positions}.
	 */
	public Integer getNumber() {
		return a.number;
	}
	
	/**
	 * Gets the phrase in {@linkplain PhrasePosition#A A-position}.
	 */
	public Phrase getA() {
		return get(A);
	}

	/**
	 * Gets the phrase in {@linkplain PhrasePosition#B B-position}.
	 */
	public Phrase getB() {
		return get(B);
	}

	/**
	 * Gets the phrase in the specified position.
	 * @param pos The position in which to find the phrase.
	 * @return If <tt>pos</tt> is {@link PhrasePosition#notifyAll()}, the A-phrase; otherwise the B-phrase.
	 */
	public Phrase get(PhrasePosition pos) {
		return (pos == A) ? a : b;
	}
	
	/**
	 * Adds a new mapping to this alignment.
	 * @throws IndexOutOfBoundsException If either of the indices in <tt>m</tt> are out of bounds for the
	 * phrase in the corresponding {@linkplain PhrasePosition position}.
	 */
	@Override
	public boolean add(Mapping m) {
		checkMapping(m);
		return mappings.add(m);
	}

	/**
	 * Gets an iterator over the mappings in this alignment.
	 */
	@Override
	public Iterator<Mapping> iterator() {
		return mappings.iterator();
	}

	/**
	 * Gets the number of mappings in this alignment.
	 */
	@Override
	public int size() {
		return mappings.size();
	}
	
	/**
	 * Compares this alignment to another by comparing their {@linkplain #getNumber() numbers}.
	 * @param o The alignment to compare to.
	 * @return The value of <tt>getNumber().compareTo(o.getNumber())</tt>.
	 * @see Integer#compareTo(Integer)
	 */
	@Override
	public int compareTo(Alignment o) {
		return getNumber().compareTo(o.getNumber());
	}

	/**
	 * Tests whether this alignment is equal to another by comparing their mappings and their phrases.
	 * @see Phrase#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {
		if(o instanceof Alignment) {
			Alignment al = (Alignment)o;
			return super.equals(o) && a.equals(al.a) && b.equals(al.b); 
		}
		
		return false;
	}

	/**
	 * Generates a hash code for this alignment based on its mappings and phrases.
	 */
	@Override
	public int hashCode() {
		return 37 * super.hashCode() + a.hashCode() + b.hashCode();
	}

	/**
	 * Gets a string representation of this alignment with both phrases and the mappings between them.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(A.name());
		sb.append(": ");
		sb.append(a.toString());
		sb.append(", ");
		sb.append(B.name());
		sb.append(": ");
		sb.append(b.toString());
		sb.append(", mappings: ");
		sb.append(super.toString());
		
		return sb.toString();
	}
	
	// Views and conveniences
	
	/**
	 * Gets the indices mapped to from the specified <tt>source</tt>, assuming that the source is in the
	 * {@linkplain PhrasePosition#A A-position}.
	 * 
	 * @return the value of <tt>getTargets(source, PhrasePosition.A)</tt>
	 * @see #getTargets(Integer, PhrasePosition)
	 */
	public Set<Integer> getTargets(Integer source) {
		return getTargets(source, A);
	}
	
	/**
	 * Gets the indices mapped to by a specified index starting from the specified position. For example, if
	 * an alignment contains the following mappings:
	 * <blockquote><pre>
	 * 7 <-> 4
	 * 3 <-> 4
	 * 4 <-> 4
	 * ...</pre></blockquote>
	 * Then calling <tt>getTargets(4,</tt> {@link PhrasePosition#B}<tt>)</tt> returns a set containing 7, 3,
	 * and 4.
	 * <p>
	 * Calling this method is equivalent to calling
	 * {@link #asMap(PhrasePosition) asMap}<tt>(sourcePosition).get(source)</tt>, with the exception that if
	 * no mappings have <tt>source</tt> in the source position, the empty set is returned rather than
	 * <tt>null</tt>.
	 * 
	 * @param source The index to look for targets of.
	 * @param sourcePosition The phrase position to assume the <tt>source</tt> index belongs to.
	 *  
	 * @return A set of indices in the {@linkplain PhrasePosition#opposite() opposite position} that the
	 * specified <tt>source</tt> index maps to (the same as <tt>asMap(sourcePosition).get(source)</tt>),
	 * or the empty set if no such indices are present.
	 * 
	 * @see #add(Mapping)
	 * @see #asMap(PhrasePosition)
	 */
	public Set<Integer> getTargets(Integer source, PhrasePosition sourcePosition) {
		return new LinkedHashSet<Integer>(new ValueView(source, sourcePosition));
	}
	
	/**
	 * Gets the indices in a specified phrase position. Specifically, returns a set containing every
	 * integer <tt>i</tt> such that there exists a mapping in this alignment that returns <tt>i</tt>
	 * for the call {@link Mapping#get(PhrasePosition)} with the specified <tt>position</tt> as argument.
	 * @param position The position to get indices for.
	 * @return The same value as <tt>asMap(position).keySet()</tt>.
	 * @see #asMap(PhrasePosition)
	 */
	public Set<Integer> getIndices(PhrasePosition position) {
		return new LinkedHashSet<Integer>(new KeyView(position));
	}

	/**
	 * Gets a map view of this alignment from the {@linkplain Alignments#DEFAULT_PHRASE_POSITION 
	 * default phrase position}. 
	 * @see #asMap(PhrasePosition)
	 */
	public Map<Integer, Set<Integer>> asMap() {
		return asMap(Alignments.DEFAULT_PHRASE_POSITION);
	}
	
	/**
	 * Gets a map view of this alignment from the specified key position. The returned map's keys are drawn
	 * from the mappings by accessing the specified key position, while the values are aggregated together
	 * into sets from the indices at <tt>keyPosition</tt>'s {@linkplain PhrasePosition#opposite() opposite
	 * position}. The returned map contains key/value pairs that can be used to reconstruct the alignment
	 * it is based on via the {@link #fromMap(Phrase, Phrase, Map)} method. That is, calling
	 * <blockquote><pre>Alignment.fromMap(a.getA(), a.getB(), a.asMap(PhrasePosition.A))</pre></blockquote>
	 * for any alignment <tt>a</tt> always returns an alignment that is equivalent to <tt>a</tt> according
	 * to the {@link #equals(Object)} method.
	 * <p>
	 * For example, if this alignment contains the following mappings
	 * <blockquote><pre>
	 * 0 <-> 0
	 * 0 <-> 1
	 * 1 <-> 2
	 * 3 <-> 2</pre>
	 * </blockquote>
	 * then calling <code>asMap(PhrasePosition.A)</code> returns a map with the key/value pairings
	 * <blockquote><pre>
	 * 0=[0, 1]
	 * 1=[2]
	 * 3=[2]</pre>
	 * </blockquote>
	 * while calling <code>asMap(PhrasePosition.B)</code> gives the map view from the &quot;opposite
	 * direction&quot;, i.e.
	 * <blockquote><pre>
	 * 0=[0]
	 * 1=[0]
	 * 2=[1, 3]</pre>
	 * </blockquote>
	 * Note that the order of the keys and values reflects the ordering of the alignment's mappings via
	 * {@link LinkedHashMap} and {@link LinkedHashSet}, and is dependent
	 * on its {@linkplain #iterator() iterator}. Also, the behavior of
	 * the returned map is not specified if mappings are added are removed to this alignment after a call to
	 * <code>asMap()</code>.
	 * <p>
	 * The returned map is detached from (not backed by) this set of mappings, so keys can be removed from and 
	 * added to it without any effect on this alignment. Similarly, the sets of indices that are the values
	 * of its entry set can be modified without affecting this alignment. The
	 * {@link #fromMap(Phrase, Phrase, Map)} provides the ability to create an alignment based on a map of
	 * A indices to sets of B indices.
	 * 
	 * @param keyPosition The phrase position that the resulting maps keys should be taken from. 
	 * @return A map whose {@linkplain Map#keySet() keys} are from the phrase in the specified position, and
	 * whose values are sets of indices from the phrase in the {@linkplain PhrasePosition#opposite() opposite}
	 * position.
	 * 
	 * @see #fromMap(Phrase, Phrase, Map)
	 * @see LinkedHashMap
	 * @see LinkedHashSet
	 */
	public Map<Integer, Set<Integer>> asMap(PhrasePosition keyPosition) {
		return new LinkedHashMap<Integer, Set<Integer>>(new MapView(keyPosition));
	}
	
	void checkPhrases(Phrase ap, Phrase bp) {
		if(ap == null) {
			throw new IllegalArgumentException(A.name() + " phrase is null");
		}
		
		if(bp == null) {
			throw new IllegalArgumentException(B.name() + " phrase is null");
		}
		
		if(!ap.number.equals(bp.number)) {
			throw new IllegalArgumentException("phrases have different numbers");
		}
	}
	
	void checkMapping(Mapping m) {
		if(m == null) {
			throw new IllegalArgumentException("attempt to add null mapping");
		}
		
		if(m.phraseNumber != null && !m.phraseNumber.equals(a.number)) {
			throw new IllegalArgumentException("mapping's phrase number does not match: expected "
					+ a.number + ", but was " + m.phraseNumber);
		}
		
		for(PhrasePosition pos : PhrasePosition.values()) {
			checkIndex(m.get(pos), pos);
		}
	}
	
	void checkIndex(Integer index, PhrasePosition intendedPosition) {
		if(index == null) {
			throw new IllegalArgumentException("attempt to add null index in position "
				+ intendedPosition.name()); 
		}
		
		if(index < -1 || get(intendedPosition).size() <= index) {
			throw new IndexOutOfBoundsException(intendedPosition.name() + " index out of bounds: " + index);
		}
	}
	
	class MapView extends AbstractMap<Integer, Set<Integer>> {
		PhrasePosition keyPosition;
		
		MapView(PhrasePosition keyPosition) {
			this.keyPosition = keyPosition;
		}

		@Override
		public Set<Entry<Integer, Set<Integer>>> entrySet() {
			return new AbstractSet<Entry<Integer, Set<Integer>>>() {
				private Set<Integer> keys = new KeyView(keyPosition);
				
				@Override
				public int size() {
					return keys.size();
				}

				@Override
				public Iterator<Entry<Integer, Set<Integer>>> iterator() {
					return new Iterator<Entry<Integer,Set<Integer>>>() {
						private Iterator<Integer> i = keys.iterator();
						
						@Override
						public boolean hasNext() {
							return i.hasNext();
						}

						@Override
						public Entry<Integer, Set<Integer>> next() {
							final Integer key = i.next();
							
							// copy values because HashMap's constructor doesn't
							return new SimpleImmutableEntry<Integer, Set<Integer>>(
									key, new LinkedHashSet<Integer>(new ValueView(key, keyPosition)));
						}
						
						@Override
						public void remove() {
							i.remove(); // throws UnsupportedOperationException
						}
					};
				}
			};
		}
	}
	
	abstract class IndexView extends AbstractSet<Integer> {

		PhrasePosition indexPosition;
		Filter<Mapping> indexFilter;
	
		private Set<Mapping> indices;
		
		IndexView(PhrasePosition indexPosition, Filter<Mapping> indexFilter) {
			this.indexPosition = indexPosition;
			this.indexFilter = indexFilter;
		}
		
		Set<Mapping> indices() {
			return (indices == null)
					? (indices = new FilteredSet<Mapping>(Alignment.this.mappings, indexFilter))
					: indices;
		}
		
		@Override
		public int size() {
			return indices().size();
		}

		@Override
		public Iterator<Integer> iterator() {
			return new Iterator<Integer>() {
				private Iterator<Mapping> i = indices().iterator();

				@Override
				public boolean hasNext() {
					return i.hasNext();
				}

				@Override
				public Integer next() {
					return i.next().get(indexPosition);
				}
				
				@Override
				public void remove() {
					throw new UnsupportedOperationException(); // just in case
				}
			};
		}		
	}
	
	class KeyView extends IndexView {
		KeyView(final PhrasePosition keyPosition) {
			super(keyPosition, new DelegatedFilter<Mapping, Integer>(new VisitedFilter<Integer>()) {
				@Override
				public Integer delegateValueFor(Mapping e) {
					return e.get(keyPosition);
				}
			});
		}
	}
	
	class ValueView extends IndexView {
		ValueView(final Integer key, final PhrasePosition keyPosition) {
			super(keyPosition.opposite(), new Filter<Mapping>() {
				@Override
				public boolean allows(Mapping m) {
					return key.equals(m.get(keyPosition));
				}
			});
		}		
	}
}
