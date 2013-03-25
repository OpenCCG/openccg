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

import java.util.List;
import java.util.Set;

/**
 * An encoding scheme for mappings. Some examples are
 * the {@linkplain MosesEncodingScheme Moses scheme} and the {@linkplain NAACLEncodingScheme NAACL scheme}.
 * <p>
 * Implementers keep track of the {@link IndexBase}s corresponding to an encoding scheme (for both phrase 
 * numbers and indices), which characters it uses to delimit mappings, groups of mappings, and fields within
 * mappings. It also captures which fields occur
 * in which {@linkplain #getOrder() order} in an encoding scheme, along with which ones are
 * {@linkplain #getRequired() required}, and which are used by {@linkplain #getDefaults() default}.
 * <p>
 * Some convenience methods are provided for determining whether a given
 * character is the field, mapping, or group delimiter for this encoding scheme. These methods mainly allow
 * comparison when one character is a line separator that may be different from the one on the current platform.
 * <p>
 * Encoding schemes are used in the {@link MappingFormat} class, as well as in
 * the readers and writers for {@link Mapping}s and {@link Alignment}s. 
 * 
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @see AbstractEncodingScheme
 * @see MappingFormat
 * @see <a href="http://www.statmt.org/moses/?n=FactoredTraining.AlignWords">Moses alignment format</a>
 * @see <a href="http://www.cse.unt.edu/~rada/wpt05/WordAlignment.Guidelines.txt">NAACL shared task alignment format</a>
 */
public interface EncodingScheme {
	
	/**
	 * Tests whether the supplied character counts as a field delimiter according to this encoding scheme.
	 * @return true if <code>c</code> is equal to {@link #getFieldDelimiter()} or both <code>c</code> and 
	 * field delimiters are line separators.
	 */
	public boolean isFieldDelimiter(Character c);
	
	/**
	 * Tests whether the supplied character counts as a mapping delimiter according to this encoding scheme.
	 * @return true if <code>c</code> is equal to {@link #getMappingDelimiter()} or both <code>c</code> and 
	 * mapping delimiters are line separators.
	 */
	public boolean isMappingDelimiter(Character c);
	
	/**
	 * Tests whether the supplied character counts as a group delimiter according to this encoding scheme.
	 * @return true if <code>c</code> is equal to {@link #getGroupDelimiter()} or both <code>c</code> and 
	 * group delimiters are line separators.
	 */
	public boolean isGroupDelimiter(Character c);

	/**
	 * Gets the delimiter for fields.
	 */
	public Character getFieldDelimiter();

	/**
	 * Gets the delimiter for mappings.
	 */
	public Character getMappingDelimiter();

	/**
	 * Gets the delimiter for groups.
	 */
	public Character getGroupDelimiter();

	/**
	 * Gets the numbering base used for phrases.
	 */
	public IndexBase getPhraseNumberBase();

	/**
	 * Gets the numbering base used for mapping indices.
	 */
	public IndexBase getIndexBase();

	/**
	 * Gets the order in which fields occur in this encoding scheme.
	 */
	public List<MappingFormat.Field> getOrder();

	/**
	 * Gets the required (non-optional) fields in this scheme.
	 */
	public Set<MappingFormat.Field> getRequired();
	
	/**
	 * Gets the default fields in this scheme.
	 */
	public Set<MappingFormat.Field> getDefaults();
}
