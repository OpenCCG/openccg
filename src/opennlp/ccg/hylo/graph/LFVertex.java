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
package opennlp.ccg.hylo.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import opennlp.ccg.hylo.Mode;
import opennlp.ccg.hylo.Nominal;
import opennlp.ccg.hylo.Proposition;
import opennlp.ccg.hylo.SatOp;

/**
 * A vertex in an {@link LFGraph}. Vertices are based on {@link SatOp}s, encapsulating their
 * {@linkplain SatOp#getNominal() nominal} and {@linkplain SatOp#getArg() proposition argument} (or
 * <tt>null</tt> if there is no associated proposition).
 * Vertices also maintain a list of attribute/value pairs representing their associated attribute/value
 * predications.
 * <p>
 * The {@linkplain #getIndex() index} and {@linkplain #getType() type} of an LF vertex are determined by
 * the specified nominal's {@linkplain Nominal#getName() name}. For example, if a vertex's 
 * {@linkplain #getName() name} is <tt>w12</tt>, then {@link #getIndex()} returns <tt>12</tt> and
 * {@link #getType()} returns {@link LFVertexType#WORD}.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 * @see LFGraph
 * @see LFVertexType
 * @see SatOp
 */
public class LFVertex {

	final Nominal nominal;
	final Proposition proposition;
	
	/**
	 * This vertex's attribute map.
	 */
	protected Map<Mode,Proposition> attributes;
	
	private Integer index;
	private LFVertexType type;
	
	/**
	 * Creates a new LF vertex based on the specified SatOp.
	 * @param satOp The SatOp to use for creating a new LF vertex, using its nominal and argument.
	 * @see #LFVertex(Nominal, Proposition)
	 */
	public LFVertex(SatOp satOp) {
		this(satOp.getNominal(), (Proposition)satOp.getArg());
	}
	
	/**
	 * Creates a new LF vertex based on the specified nominal, with a <tt>null</tt> proposition.
	 * @see #LFVertex(Nominal, Proposition) 
	 */
	public LFVertex(Nominal nominal) {
		this(nominal, null);
	}
	
	/**
	 * Creates a new LF vertex based on the specified nominal and proposition.
	 * @see #LFVertex(Nominal, Proposition, Map)
	 */
	public LFVertex(Nominal nominal, Proposition proposition) {
		this(nominal, proposition, null);
	}
	
	/**
	 * Creates a new LF vertex based on the specified nominal and proposition, with the specified attribute
	 * map (which can be <tt>null</tt>).
	 * @throws IllegalArgumentException if <tt>nominal</tt> is <tt>null</tt>.
	 */
	public LFVertex(Nominal nominal, Proposition proposition, Map<Mode,Proposition> attributes) {
		if(nominal == null) {
			throw new IllegalArgumentException("nominal is null");
		}
		
		this.nominal = nominal;
		this.proposition = proposition;
		this.attributes = attributes;
	}

	/**
	 * Gets this LF vertex's name, determined by the name of its {@linkplain #getNominal() nominal}.
	 * @return The value of <tt>getNominal().getName()</tt>.
	 */
	public String getName() {
		return getNominal().getName();
	}
	
	/**
	 * Gets this LF vertex's associated predicate, the name of its {@linkplain #getProposition() proposition}.
	 * @return The value of <tt>getProposition().getName()</tt>, if this vertex's proposition is non-null,
	 * and <tt>null</tt> otherwise.
	 */
	public String getPredicate() {
		Proposition p = getProposition();
		return (p == null) ? null : p.getName();
	}
	
	/**
	 * Gets this LF vertex's associated nominal.
	 */
	public Nominal getNominal() {
		return nominal;
	}

	/**
	 * Gets this LF vertex's associated proposition.
	 * @return Possibly <tt>null</tt>, if no proposition was provided at creation.
	 * @see #LFVertex(Nominal)
	 */
	public Proposition getProposition() {
		return proposition;
	}
	
	/**
	 * Gets the type of this LF vertex, as determined by the prefix of its {@linkplain #getNominal() nominal}'s
	 * name. For example, if this vertex's {@linkplain Nominal#getName() name} is <tt>x3</tt>, then
	 * this method returns
	 * {@link LFVertexType#fromPrefix(String) LFVertexType.fromPrefix}<tt>('x') == </tt>{@link LFVertexType#NONWORD}.
	 * @return The value of {@link LFVertexType#fromPrefix(String)} for this vertex's 
	 * {@linkplain #getNominal() nominal}'s {@linkplain Nominal#getName() name}.
	 * @see LFVertexType
	 */
	public LFVertexType getType() {
		return (type == null) ? (type = LFVertexType.fromPrefix(nominal.getName())) : type;
	}

	/**
	 * Gets the word index associated with this LF vertex, as determined by {@link #parseVertexIndex(Nominal)}.
	 */
	public Integer getIndex() {
		return (index == null) ? (index = parseVertexIndex(nominal)) : index;
	}
	
	/**
	 * Parses the word index associated with the specified nominal, obtained by parsing its name. For
	 * example, if the specified nominal's name is <tt>x9</tt>, this method returns the integer 
	 * <tt>9</tt>.
	 * @param nominal The nominal to find the word index for.
	 * @return The integer index corresponding to the specified nominal, determined by parsing its
	 * {@linkplain Nominal#getName() name}.
	 */
	public static Integer parseVertexIndex(Nominal nominal) {
		String nm = nominal.getName();
		int colidx = nm.indexOf(':');
		String s = (colidx == -1) ? nm : nm.substring(0, colidx);
		
		return Integer.parseInt(s.substring(1));
	}
	
	/**
	 * Gets the attribute map associated with this LF vertex. Note that the returned map is
	 * not modifiable; to modify a vertex's attributes, the methods
	 * {@link #setAttribute(Mode, Proposition)} and {@link #removeAttribute(Mode)}
	 * should be used.
	 * @return An unmodifiable copy of the attribute map encapsulated by this vertex, or
	 * {@link Collections#EMPTY_MAP} if no attributes are present.
	 * @see Collections#unmodifiableMap(Map)
	 */
	@SuppressWarnings("unchecked")
	public Map<Mode, Proposition> getAttributeMap() {
		return (attributes == null) ? Collections.EMPTY_MAP : Collections.unmodifiableMap(attributes);
	}
	
	/**
	 * Gets the names of this LF vertex's attributes. Note that the returned set is not modifiable;
	 * the methods {@link #setAttribute(Mode, Proposition)} and {@link #removeAttribute(Mode)} should
	 * be used to modify this vertex's attribute/value pairs.
	 * @return The value of <tt>getAttributeMap().keySet()</tt>, or {@link Collections#EMPTY_SET} if
	 * no attributes are present.
	 * @see Collections#unmodifiableSet(Set)
	 */
	@SuppressWarnings("unchecked")
	public Set<Mode> attributeNames() {
		return (attributes == null) ? Collections.EMPTY_SET : Collections.unmodifiableSet(attributes.keySet());
	}
	
	/**
	 * Tests whether this vertex contains an attribute with the associated attribute name.
	 * @param attributeName The name of the attribute to test for.
	 * @return <tt>true</tt> if this vertex has an attribute named <tt>attributeName</tt>.
	 * @see #attributeNames()
	 */
	public boolean containsAttribute(Mode attributeName) {
		return (attributes != null && attributes.containsKey(attributeName));
	}
	
	/**
	 * Gets the value of the attribute with the specified mode name.
	 * @param attributeName The attribute name to retrieve a value for.
	 * @return The associated attribute value, or <tt>null</tt> if none is present.
	 */
	public Proposition getAttributeValue(Mode attributeName) {
		return (attributes == null) ? null : attributes.get(attributeName);
	}
	
	/**
	 * Adds an attribute/value to this vertex's attributes. 
	 * @param attributeName The name of the new attribute.
	 * @param value The value of the new attribute.
	 * @return True if this vertex's attribute/value map changed as a result of the call because
	 * either (1) no attribute named <tt>attributeName</tt> was present or (2)
	 * the value associated with <tt>attributeName</tt> changed (was different from <tt>value</tt>).
	 * @see #setAttribute(Mode, Proposition)
	 */
	public boolean addAttribute(Mode attributeName, Proposition value) {
		// works even when setAttribute() returns null
		return !value.equals(setAttribute(attributeName, value)); 
	}
	
	/**
	 * Sets the attribute associated with the specified mode name to the specified proposition value.
	 * @param attributeName The key to set the value for.
	 * @param value The value that will be associated with <tt>attributeName</tt>.
	 * @return The value previously associated with <tt>attributeName</tt>, or <tt>null</tt> if no value
	 * was previously associated.
	 * @see Map#put(Object, Object)
	 */
	public Proposition setAttribute(Mode attributeName, Proposition value) {
		if(attributes == null) {
			attributes = new HashMap<Mode, Proposition>();
		}
		
		return attributes.put(attributeName, value);
	}
	
	/**
	 * Removes and returns the value associated with the specified attribute name.
	 * @param attributeName The name to remove the value for.
	 * @return The value previously associated with <tt>attributeName</tt>, or <tt>null</tt> if no value
	 * was associated with it.
	 */
	public Proposition removeAttribute(Mode attributeName) {
		return (attributes == null) ? null : attributes.remove(attributeName); 
	}

	/**
	 * Gets a hash code for this vertex based on its nominal and proposition, if the proposition is
	 * non-null.
	 */
	@Override
	public int hashCode() {
		int i = 37 * nominal.hashCode();
		
		if(proposition != null) {
			i += proposition.hashCode();
		}
		
		// Don't include attributes in hash code calculation. This could cause problems if a vertex is
		// added to a collection that relies on hashing, and then the attributes are later modified.
		
		return i;
	}

	/**
	 * Tests whether this LF vertex is equal to another by comparing their nominals and possibly also their
	 * propositions and attributes, if they are non-null/non-empty.
	 * @see #getNominal()
	 * @see #getProposition()
	 * @see #getAttributeMap()
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof LFVertex) {
			LFVertex v = (LFVertex)obj;
			return nominal.equals(v.nominal) 
					&& ((proposition == null) ? v.proposition == null : proposition.equals(v.proposition))
					&& ((attributes == null) ? v.attributes == null : attributes.equals(v.attributes));
		}
		
		return false;
	}

	/**
	 * Gets a string representation of this LF vertex. For example, if this vertex's name is <tt>w9</tt>,
	 * its proposition's name is <tt>walk</tt>, and its attribute map contains <tt>num=sg</tt> and
	 * <tt>det=nil</tt>, this method returns <tt>w9@walk {num=sg, det=nil}</tt>.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(nominal.getName());
		
		if(proposition != null) {
			sb.append('@');
			sb.append(proposition.getName());
		}
		
		if(attributes != null && !attributes.isEmpty()) {
			sb.append(' ');
			sb.append(attributes.toString());
		}
			
		return sb.toString();
	}
}
