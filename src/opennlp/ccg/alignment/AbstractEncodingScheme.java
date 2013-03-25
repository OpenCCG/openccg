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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Abstract class for implementing encoding schemes. This base class provides a constructor taking all the
 * necessary arguments for implementing {@link EncodingScheme}, and gives getter methods for all of them,
 * as required.
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @see EncodingScheme
 */
public abstract class AbstractEncodingScheme implements EncodingScheme {
	
	/**
	 * Delimits a field within a mapping.
	 */
	protected Character fieldDelimiter;
	
	/**
	 * Delimits a mapping.
	 */
	protected Character mappingDelimiter;
	
	/**
	 * Delimits a group of mappings.
	 */
	protected Character groupDelimiter;
	
	/**
	 * The index base for IDs.
	 */
	protected IndexBase phraseNumberBase;
	
	/**
	 * The index base for indices.
	 */
	protected IndexBase indexBase;
	
	/**
	 * The order of the fields in mappings corresponding to this encoding scheme.
	 */
	protected List<MappingFormat.Field> order;
	
	/**
	 * The set of required fields in this encoding scheme.
	 */
	protected Set<MappingFormat.Field> required;
	
	/**
	 * The fields that this encoding scheme uses by default.
	 */
	protected Set<MappingFormat.Field> defaults;
	
	final boolean fieldDelimSep, mappingDelimSep, groupDelimSep;	
	
	protected AbstractEncodingScheme(Character fieldDelimiter, Character mappingDelimiter,
			Character groupDelimiter, IndexBase phraseNumberBase, IndexBase indexBase,
			Set<MappingFormat.Field> defaults, Set<MappingFormat.Field> required,
			MappingFormat.Field... order) {
		this.fieldDelimiter = fieldDelimiter;
		this.mappingDelimiter = mappingDelimiter;
		this.groupDelimiter = groupDelimiter;
		this.phraseNumberBase = phraseNumberBase;
		this.indexBase = indexBase;
		
		fieldDelimSep = isLineSeparator(fieldDelimiter);
		mappingDelimSep = isLineSeparator(mappingDelimiter);
		groupDelimSep = isLineSeparator(groupDelimiter);
		
		this.defaults = Collections.unmodifiableSet(defaults);
		this.required = Collections.unmodifiableSet(required);
		this.order = Collections.unmodifiableList(Arrays.asList(order));
	}
	
	static boolean isLineSeparator(Character c) {
		// TODO why doesn't Character.getType(c) == Character.LINE_SEPARATOR work?
		return c == '\r' || c == '\n';
	}
	
	/**
	 * Tests whether the supplied character counts as a field delimiter according to this encoding scheme.
	 * @return true if <code>c</code> is equal to {@link #fieldDelimiter} or both <code>c</code> and 
	 * {@link #fieldDelimiter} are line separators.
	 */
	public boolean isFieldDelimiter(Character c) {
		return fieldDelimiter.equals(c) || (fieldDelimSep && isLineSeparator(c)); 
	}
	
	/**
	 * Tests whether the supplied character counts as a mapping delimiter according to this encoding scheme.
	 * @return true if <code>c</code> is equal to {@link #mappingDelimiter} or both <code>c</code> and 
	 * {@link #mappingDelimiter} are line separators.
	 */
	public boolean isMappingDelimiter(Character c) {
		return mappingDelimiter.equals(c) || (mappingDelimSep && isLineSeparator(c)); 
	}
	
	/**
	 * Tests whether the supplied character counts as a group delimiter according to this encoding scheme.
	 * @return true if <code>c</code> is equal to {@link #groupDelimiter} or both <code>c</code> and 
	 * {@link #groupDelimiter} are line separators.
	 */
	public boolean isGroupDelimiter(Character c) {
		return groupDelimiter.equals(c) || (groupDelimSep && isLineSeparator(c)); 
	}

	/**
	 * Gets the delimiter for fields.
	 */
	public Character getFieldDelimiter() {
		return fieldDelimiter;
	}

	/**
	 * Gets the delimiter for mappings.
	 */
	public Character getMappingDelimiter() {
		return mappingDelimiter;
	}

	/**
	 * Gets the delimiter for groups.
	 */
	public Character getGroupDelimiter() {
		return groupDelimiter;
	}

	/**
	 * Gets the numbering base used for phrases.
	 */
	public IndexBase getPhraseNumberBase() {
		return phraseNumberBase;
	}

	/**
	 * Gets the numbering base used for mapping indices.
	 */
	public IndexBase getIndexBase() {
		return indexBase;
	}

	/**
	 * Gets the order in which fields occur in this encoding scheme.
	 */
	public List<MappingFormat.Field> getOrder() {
		return order;
	}

	/**
	 * Gets the required (non-optional) fields in this scheme.
	 */
	public Set<MappingFormat.Field> getRequired() {
		return required;
	}

	/**
	 * @return Gets the default fields used by this scheme.
	 */
	public Set<MappingFormat.Field> getDefaults() {
		return defaults;
	}

	/**
	 * Gets a hash code for this encoding scheme based on its delimiters, index bases, and fields.
	 */
	@Override
	public int hashCode() {
		return 37 * fieldDelimiter.hashCode() + groupDelimiter.hashCode() + mappingDelimiter.hashCode()
				+ indexBase.hashCode() + phraseNumberBase.hashCode() + order.hashCode()
				+ required.hashCode() + defaults.hashCode();
	}

	/**
	 * Tests whether this encoding scheme is equal to another based on its delimiters, index bases, and fields.
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AbstractEncodingScheme) {
			AbstractEncodingScheme e = (AbstractEncodingScheme)obj;
			
			return fieldDelimiter.equals(e.fieldDelimiter) && groupDelimiter.equals(e.groupDelimiter)
					&& mappingDelimiter.equals(e.mappingDelimiter) && indexBase.equals(e.indexBase)
					&& phraseNumberBase.equals(e.phraseNumberBase) && order.equals(e.order)
					&& required.equals(e.required) && defaults.equals(e.defaults);
		}
		
		return false;
	}

}
