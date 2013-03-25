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

import static opennlp.ccg.alignment.PhrasePosition.*;

/**
 * A mapping from an index in the {@linkplain PhrasePosition#A A} phrase position to an index in the
 * {@linkplain PhrasePosition#B B} phrase position. Mappings are interpreted as the individual pairs
 * that make up an {@link Alignment} from one {@link Phrase} to another. This class implements
 * {@link Comparable} so that mappings can be easily sorted (see the {@link #compareTo(Mapping)} method).
 * <p>
 * Although different
 * {@linkplain EncodingScheme encoding schemes} may use different {@linkplain IndexBase index bases}, all
 * mappings share a common one, namely {@link Alignments#DEFAULT_INDEX_BASE}. As such, no index can be 
 * specified that is less than the {@linkplain IndexBase#nullValue null value} of that index base. Similarly,
 * the phrase number specified (if any) must be in the range of the
 * {@linkplain Alignments#DEFAULT_PHRASE_NUMBER_BASE default phrase number base},
 * even though different encoding schemes may have different phrase number bases.
 * <p>
 * Since some {@linkplain EncodingScheme encoding schemes} do not require an
 * {@linkplain #getPhraseNumber() phrase number}
 * to be specified, the phrase number field may be <code>null</code>. However, none of the other fields may be
 * <code>null</code>. To specify that the mapping is to the special null word value, the
 * {@linkplain Alignments#DEFAULT_INDEX_BASE default index base}'s
 * {@linkplain IndexBase#nullValue null value} is used.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 * @see PhrasePosition
 * @see EncodingScheme
 * @see IndexBase
 * @see Alignments#DEFAULT_PHRASE_NUMBER_BASE
 * @see Alignments#DEFAULT_INDEX_BASE
 * @see Alignments#DEFAULT_STATUS
 * @see Alignments#DEFAULT_CONFIDENCE
 */
public class Mapping implements Comparable<Mapping> {

	final Integer phraseNumber, a, b;
	Status status;
	Double confidence;
	
	/**
	 * Creates a mapping between the two specified indices with a <code>null</code> phrase number.
	 * @see #Mapping(Integer, Integer, Integer)
	 */
	public Mapping(Integer a, Integer b) {
		this(null, a, b);
	}
	
	/**
	 * Creates a mapping between the two specified indices with a <code>null</code> phrase number and the
	 * {@linkplain Alignments#DEFAULT_STATUS default status}.
	 * @see #Mapping(Integer, Integer, Integer, Status)
	 */
	public Mapping(Integer phraseNumber, Integer a, Integer b) {
		this(phraseNumber, a, b, Alignments.DEFAULT_STATUS);
	}
	
	/**
	 * Creates a mapping between the two specified indices with the specified phrase number and status, with the
	 * {@linkplain Alignments#DEFAULT_CONFIDENCE default confidence}.
	 * @see #Mapping(Integer, Integer, Integer, Status, Double)
	 */
	public Mapping(Integer phraseNumber, Integer a, Integer b, Status status) {
		this(phraseNumber, a, b, status, Alignments.DEFAULT_CONFIDENCE);
	}
	
	/**
	 * Creates a mapping between the two specified indices.
	 * @param phraseNumber The phrase number of the corresponding mapping.
	 * @param a The A index, corresponding to {@link PhrasePosition#A}.
	 * @param b The B index, corresponding to {@link PhrasePosition#B}.
	 * @param status The status of this mapping.
	 * @param confidence This mapping's confidence value.
	 * 
	 * @throws IndexOutOfBoundsException If any of <code>phraseNumber</code>, <code>a</code>, or
	 * <code>b</code> is out of bounds according to the corresponding default.
	 * @throws IllegalArgumentException If either index is <tt>null</tt>, or if <code>status</code> or
	 * <code>confidence</code> is <code>null</code>.
	 * 
	 * @see Alignments#DEFAULT_PHRASE_NUMBER_BASE
	 * @see Alignments#DEFAULT_INDEX_BASE
	 */
	public Mapping(Integer phraseNumber, Integer a, Integer b, Status status, Double confidence) {
		checkPhraseNumber(phraseNumber);
		checkIndex(a);
		checkIndex(b);
		checkField(status, "status");
		checkField(confidence, "confidence");
		
		this.phraseNumber = phraseNumber;
		this.a = a;
		this.b = b;
		this.status = status;
		this.confidence = confidence;
	}
	
	/**
	 * Gets a copy of this mapping with its {@linkplain #getPhraseNumber() phrase number}
	 * set to the specified phrase number. This method is a convenience for the {@link MappingReader} class,
	 * which, for certain encoding scheme like {@link MosesEncodingScheme}, may not be able to
	 * parse the phrase number from the input string.
	 * @param phraseNumber The phrase number the returned mapping should have.
	 * @return This mapping if its {@linkplain #getPhraseNumber() phrase number} is <tt>null</tt> and the
	 * specified phrase number is <tt>null</tt>, or if this mapping's phrase number is
	 * {@linkplain Integer#equals(Object) equivalent to} the specified phrase number. Otherwise, a new 
	 * mapping is returned with all the same field values as this mapping, but with its phrase number set
	 * to <tt>phraseNumber</tt>.
	 * 
	 * @see #Mapping(Integer, Integer, Integer, Status, Double)
	 */
	public Mapping copyWithPhraseNumber(Integer phraseNumber) {
		if((this.phraseNumber == null && phraseNumber == null)
				|| (this.phraseNumber != null && this.phraseNumber.equals(phraseNumber))) {
			return this;
		}
		
		return new Mapping(phraseNumber, a, b, status, confidence);
	}
	
	/**
	 * Convenience method for creating mappings when the phrase position may possibly be 
	 * {@link PhrasePosition#B}.
	 * @see #mappingByPosition(Integer, Integer, Integer, Status, Double, PhrasePosition)
	 */
	public static Mapping mappingByPosition(Integer phraseNumber, Integer a, Integer b,
			PhrasePosition firstPosition) {
		return mappingByPosition(phraseNumber, a, b, Alignments.DEFAULT_STATUS,
				Alignments.DEFAULT_CONFIDENCE, firstPosition);
	}

	/**
	 * Convenience method for creating mappings in case which index should be {@linkplain #getA() A} and
	 * which should be {@linkplain #getB() B} depends on the phrase position.
	 * 
	 * @param phraseNumber The phrase number to use.
	 * @param a The index to use either for the A (if the first position is
	 * {@link PhrasePosition#A}) or B index (if the first position is {@link PhrasePosition#B}).
	 * @param b The index to use either for the B (if the first position is
	 * {@link PhrasePosition#A}) or A index (if the first position is {@link PhrasePosition#B}).
	 * @param status The status to use.
	 * @param confidence The confidence to use.
	 * @param firstPosition Which phrase position the new mapping should reflect. If this argument is
	 * {@link PhrasePosition#A}, the <code>a</code> argument will be the A index and the
	 * <code>b</code> argument the B. If it is {@link PhrasePosition#B}, these are reversed.
	 * @return A new mapping with its indices configured per the specified <code>firstPosition</code>.
	 * @see Mapping#Mapping(Integer, Integer, Integer, Status, Double)
	 */
	public static Mapping mappingByPosition(Integer phraseNumber, Integer a, Integer b, Status status,
			Double confidence, PhrasePosition firstPosition) {
		return new Mapping(phraseNumber,
				(firstPosition == A) ? a : b, (firstPosition == B) ? a : b, status, confidence);
	}
	
	/**
	 * Gets a new mapping just like this one except that the indices in the {@link PhrasePosition#A}
	 * and {@link PhrasePosition#B} positions are swapped. The original status and confidence are
	 * unchanged.
	 * @return A mapping with indices reversed.
	 * @see Mapping#mappingByPosition(Integer, Integer, Integer, Status, Double, PhrasePosition)
	 */
	public Mapping reverse() {
		return mappingByPosition(phraseNumber, a, b, status, confidence, B);
	}
	
	/**
	 * Gets this mapping's phrase number.
	 */
	public Integer getPhraseNumber() {
		return phraseNumber;
	}

	/**
	 * Gets this mapping's index in the {@linkplain PhrasePosition#A A-position}.
	 * @return The value of {@link #get(PhrasePosition)} for {@link PhrasePosition#A}.
	 */
	public Integer getA() {
		return get(A);
	}

	/**
	 * Gets this mapping's index in the {@linkplain PhrasePosition#B B-position}.
	 * @return The value of {@link #get(PhrasePosition)} for {@link PhrasePosition#B}.
	 */
	public Integer getB() {
		return get(B);
	}

	/**
	 * Gets this mapping's index at the specified phrase position.
	 * @param pos The phrase position at which to return the corresponding index.
	 * @return If <code>pos</code> is {@link PhrasePosition#A}, the A index; otherwise the B index.
	 */
	public Integer get(PhrasePosition pos) {
		return (pos == A) ? a : b;
	}
	
	/**
	 * Gets this mapping's status.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Sets this mapping's status to the specified value.
	 */
	public void setStatus(Status status) {
		checkField(status, "status");
		this.status = status;
	}

	/**
	 * Gets this mapping's confidence.
	 */
	public Double getConfidence() {
		return confidence;
	}

	/**
	 * Sets this mapping's confidence to the supplied value.
	 * @param confidence May be <code>null</code>. No bounds checking is performed on this value even if it
	 * is non-null.
	 */
	public void setConfidence(Double confidence) {
		checkField(confidence, "confidence");
		this.confidence = confidence;
	}

	/**
	 * Compares this mapping to another according to their natural ordering. The natural ordering of
	 * mappings is that first their IDs are compared, then their A indices, then their B indices,
	 * and finally their status and confidence value (in that order).
	 * <p>
	 * For the ID field, which may be null, the comparison is performed as follows. If both this mapping's 
	 * ID and the other's are null, they are considered equivalent. If this mapping's ID is non-null, it is
	 * compared to the (possibly null) other mapping's ID via {@link Integer#compareTo(Integer)}.
	 */
	@Override
	public int compareTo(Mapping o) {
		int i = (phraseNumber == null && o.phraseNumber == null) ? 0 : phraseNumber.compareTo(o.phraseNumber);
		
		if(i == 0) {
			i = a.compareTo(o.a);
		}
		if(i == 0) {
			i = b.compareTo(o.b);
		}
		if(i == 0) {
			i = status.compareTo(o.status);
		}
		if(i == 0) {
			i = confidence.compareTo(o.confidence);
		}
		
		return i;
	}

	/**
	 * Computes a hash code based on the ID, and A and B indices. The status and confidence fields are not
	 * used for hash code computation because they are mutable. Because of this, two mappings may have
	 * identical hash codes but not be equivalent according to {@link #equals(Object)}.
	 */
	@Override
	public int hashCode() {
		int h = 37 * 1 + a + b;
		
		return (phraseNumber == null) ? h : h + phraseNumber;
	}

	/**
	 * Tests whether this mapping is equal to another.
	 * @return true If this mapping's fields match the other according to the corresponding <code>equals</code>
	 * methods. For the phrase number field, they are considered equal if both <code>null</code>
	 * or if their corresponding <code>equals</code> method returns <code>true</code>, unequal otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Mapping) {
			Mapping m = (Mapping)obj;
			return ((phraseNumber == null && m.phraseNumber == null) || phraseNumber.equals(m.phraseNumber))
				&& a.equals(m.a) && b.equals(m.b) && status.equals(m.status)
				&& confidence.equals(m.confidence);
		}
		
		return false;
	}

	void checkPhraseNumber(Integer phraseNumber) throws IndexOutOfBoundsException {
		if(phraseNumber != null && !Alignments.DEFAULT_PHRASE_NUMBER_BASE.isValidIndex(phraseNumber)) {
			throw new IndexOutOfBoundsException("invalid phrase number: " + phraseNumber);
		}
	}
	
	void checkIndex(Integer index) throws IndexOutOfBoundsException {
		if(index == null) {
			throw new IllegalArgumentException("null index");
		}
		
		if(!Alignments.DEFAULT_INDEX_BASE.isValidIndex(index)) {
			throw new IndexOutOfBoundsException("invalid index: " + index);
		}
	}
	
	void checkField(Object obj, String name) throws IllegalArgumentException {
		if(obj == null) {
			throw new IllegalArgumentException("null " + name);
		}
	}

	/**
	 * A string representation of this mapping's indices.
	 * @return For a mapping with a {@linkplain #getA() A} index of <code>3</code> and a 
	 * {@linkplain #getB() B} index of <code>6</code>, this method gives the string
	 * &quot;<code>3 <-> 6</code>&quot;.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(a.toString());
		sb.append(" <-> ");
		sb.append(b.toString());
		
		return sb.toString();
	}
}
