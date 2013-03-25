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

import static opennlp.ccg.alignment.Alignments.DEFAULT_CONFIDENCE;
import static opennlp.ccg.alignment.Alignments.DEFAULT_INDEX_BASE;
import static opennlp.ccg.alignment.Alignments.DEFAULT_PHRASE_NUMBER_BASE;
import static opennlp.ccg.alignment.Alignments.DEFAULT_STATUS;
import static opennlp.ccg.alignment.Alignments.DEFAULT_STRICTNESS;
import static opennlp.ccg.alignment.MappingFormat.Field.A_INDEX_FIELD;
import static opennlp.ccg.alignment.MappingFormat.Field.B_INDEX_FIELD;
import static opennlp.ccg.alignment.MappingFormat.Field.CONFIDENCE_FIELD;
import static opennlp.ccg.alignment.MappingFormat.Field.PHRASE_NUMBER_FIELD;
import static opennlp.ccg.alignment.MappingFormat.Field.STATUS_FIELD;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for formatting mappings according to an {@linkplain EncodingScheme encoding scheme} and a specified
 * set of {@linkplain Field formatting fields}. This class
 * extends {@link Format} so that it fits in with the Java text parsing API.
 * <p>
 * Care is always taken to translate a mapping's phrase number and indices to the target
 * encoding scheme's {@linkplain EncodingScheme#getIndexBase() index base}.
 * If a mapping format is not {@linkplain #isStrict() strict}, parsing is handled robustly in that it tolerates
 * fields that are optional and may not always be specified. On output, a non-strict mapping format will only
 * include fields that either (1) do not have a {@linkplain Field#hasDefaultValue() default value}, or (2)
 * have a value different from {@linkplain Field#getDefaultValue() the default}. Strict mapping
 * formats, on the other hand, always expect and generate all and only the {@linkplain #getFields() fields
 * specified}.
 * <p>
 * Instances of mapping formats can be obtained by calling one of the
 * <code>getInstance(...)</code> methods.
 * 
 * @see #getFields()
 * @see #isStrict()
 * @see EncodingScheme
 * @see IndexBase
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class MappingFormat extends Format {
	private static final long serialVersionUID = 1L;
	
	final EncodingScheme encodingScheme;
	final Set<Field> fields;
	final Pattern mappingPattern, fieldPattern;
	final boolean strict;
	
	private static Map<EncodingScheme, Set<MappingFormat>> formatCache; 
	
	/**
	 * Marks mapping fields like ID, first index, second index, etc. Fields can have a 
	 * {@linkplain #getDefaultValue() default value}, which may be used depending on whether this format is
	 * {@linkplain MappingFormat#isStrict() strict}.
	 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
	 */
	public static class Field extends java.text.Format.Field {
		private static final long serialVersionUID = 1L;
		
		Object defaultValue = null;
		
		/**
		 * Field representing a mapping's ID. 
		 */
		public static final Field PHRASE_NUMBER_FIELD = new Field("PHRASE_NUMBER", null);
		
		/**
		 * Field representing a mapping's A index. 
		 */
		public static final Field A_INDEX_FIELD = new Field("A_INDEX", null);
			
		/**
		 * Field representing a mapping's B index. 
		 */
		public static final Field B_INDEX_FIELD = new Field("B_INDEX", null);
			
		/**
		 * Field representing a mapping's status field. 
		 */
		public static final Field STATUS_FIELD = new Field("STATUS", DEFAULT_STATUS);
			
		/**
		 * Field representing a mapping's confidence field. 
		 */
		public static final Field CONFIDENCE_FIELD = new Field("CONFIDENCE", DEFAULT_CONFIDENCE);
		
		/**
		 * Creates a new mapping format field.
		 * @param name The new field's name.
		 * @param defaultValue The default value for this field. If this field does not have a default value,
		 * <code>null</code> is specified.
		 * @throws IllegalArgumentException if <tt>name</tt> is <tt>null</tt>.
		 */
		protected Field(String name, Object defaultValue) {
			super(name);
			
			if(name == null) {
				throw new IllegalArgumentException("name is null");
			}
			
			this.defaultValue = defaultValue;
		}

		/**
		 * Tests whether this field has a default value.
		 * @return true If the {@linkplain #getDefaultValue() default value} is non-null.
		 */
		public boolean hasDefaultValue() {
			return defaultValue != null;
		}
		
		/**
		 * Gets the default value for this field, if any.
		 * @see #hasDefaultValue()
		 */
		public Object getDefaultValue() {
			return defaultValue;
		}

		/**
		 * Overrides the superclass method to return the value of {@link java.text.Format.Field#getName()}.
		 */
		@Override
		public String toString() {
			return getName();
		}
	}
	
	/**
	 * Creates a mapping format based on the given encoding scheme and fields. The boolean flag tells instances
	 * whether or not to output values when they are the default, or expect them during parsing.
	 * 
	 * @param scheme The encoding scheme to create a formatter/parser for.
	 * @param fields The fields to include.
	 * @param strict Whether or not to use/expect default values in formatting and parsing.
	 * @throws IllegalArgumentException If <tt>scheme</tt> or <tt>fields</tt> is <tt>null</tt>,
	 * or if the specified set of fields does not contain all of the
	 * {@linkplain EncodingScheme#getRequired() required fields} of the specified scheme.
	 * @see Field#hasDefaultValue()
	 */
	protected MappingFormat(EncodingScheme scheme, Set<Field> fields, boolean strict) {
		if(scheme == null) {
			throw new IllegalArgumentException("encoding scheme is null");
		}
		if(fields == null) {
			throw new IllegalArgumentException("fields is null");
		}
		
		this.encodingScheme = scheme;
		this.fields = fields;
		this.strict = strict;
		
		if(!fields.containsAll(encodingScheme.getRequired())) {
			throw new IllegalArgumentException("specified fields does not contain all required fields");
		}
		if(!encodingScheme.getOrder().containsAll(fields)) {
			throw new IllegalArgumentException("encoding scheme does not use all the specified fields");
		}
		
		fieldPattern = Pattern.compile(scheme.getFieldDelimiter().toString());
		mappingPattern = Pattern.compile("([\\w\\.]+" + scheme.getFieldDelimiter() + ")+[\\w\\.]+");
	}
	
	/**
	 * Gets an instance of a mapping formatter/parser for a given encoding scheme with the scheme's
	 * {@linkplain EncodingScheme#getDefaults() default fields} as the specified fields.
	 * @see #getInstance(EncodingScheme, Set)
	 */
	public static MappingFormat getInstance(EncodingScheme scheme) {
		return getInstance(scheme, scheme.getDefaults());
	}
	
	/**
	 * Gets an instance of a mapping formatter/parser for a given encoding scheme and field set.
	 * @see #getInstance(EncodingScheme, Set, boolean)
	 * @see Alignments#DEFAULT_STRICTNESS
	 */
	public static MappingFormat getInstance(EncodingScheme scheme, Set<Field> fields) {
		return getInstance(scheme, fields, DEFAULT_STRICTNESS);
	}
	
	/**
	 * Gets an instance of a mapping formatter/parser for a given encoding scheme and field set. The
	 * returned instances are cached to avoid creating multiple copies with the same scheme, fields, and
	 * strictness flag. Cache access is synchronized to avoid threading issues.
	 * 
	 * @param scheme The scheme to create a formatter/parser for.
	 * @param fields The fields to include in the parser/formatter.
	 * @param strict Whether the returned format should be {@linkplain #isStrict() strict}.
	 * @return A new formatter/parser for mappings that will expect mappings encoded per the specified
	 * <code>scheme</code> and will format mappings to strings of that encoding scheme.
	 */
	public static MappingFormat getInstance(EncodingScheme scheme, Set<Field> fields, boolean strict) {
		if(scheme == null) { // test for this here so null isn't added to cache as a key
			throw new IllegalArgumentException("encoding scheme is null");
		}
		
		synchronized(MappingFormat.class) {
			MappingFormat mf = null;
			Set<MappingFormat> fs = null;
			
			if(formatCache == null) {
				formatCache = new HashMap<EncodingScheme, Set<MappingFormat>>();
			}
			else {
				fs = formatCache.get(scheme);
			}		 
			
			if(fs == null) {
				fs = new HashSet<MappingFormat>();
				formatCache.put(scheme, fs);
			}
			
			for(MappingFormat f : fs) {
				if(f.strict == strict && f.fields.equals(fields)) {
					mf = f;
					break;
				}
			}
			
			if(mf == null) {
				mf = new MappingFormat(scheme, fields, strict);
				fs.add(mf);
			}
			
			return mf;
		}
	}
	
	/**
	 * Gets the encoding scheme used by this mapping formatter/parser.
	 * @return The encoding scheme used to create this instance.
	 * @see #getInstance(EncodingScheme)
	 */
	public EncodingScheme getEncodingScheme() {
		return encodingScheme;
	}

	/**
	 * Gets the field set used by this mapping formatter/parser.
	 * @see #getInstance(EncodingScheme)
	 */
	public Set<Field> getFields() {
		return fields;
	}
	
	/**
	 * Tests whether this mapping format will output default values, or expect them during parsing. If
	 * <code>true</code>, this format will output and parse for every specified field. Otherwise, this format
	 * will only write/expect fields that have a default value if their value differs from the default.
	 * @see Field#hasDefaultValue()
	 */
	public boolean isStrict() {
		return strict;
	}

	/**
	 * Formats a mapping according to the {@linkplain #getEncodingScheme() encoding scheme in effect}.
	 * @param mapping The mapping to format.
	 * @return A string in the format required by this formatter/parser's {@link #getEncodingScheme()}.
	 * @see #format(Mapping, StringBuffer, FieldPosition) 
	 */
	public String formatMapping(Mapping mapping) {
		List<Field> order = encodingScheme.getOrder(); 
		Field field = null;
		int index = -1;
		
		for(int i = 0; i < order.size(); i++) {
			Field f = order.get(i);
			if(fields.contains(f)) {
				field = f;
				index = i;
				break;
			}
		}
		
		return format(mapping, new StringBuffer(), new FieldPosition(field, index)).toString();
	}
	
	/**
	 * Overrides the {@link Format#format(Object, StringBuffer, FieldPosition)} method to make sure the
	 * specified <code>Object</code> <code>obj</code> is an instance of {@link Mapping}.
	 * @see #format(Mapping, StringBuffer, FieldPosition)
	 */
	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if(!(obj instanceof Mapping)) {
			throw new IllegalArgumentException("not a mapping: " + obj);
		}
		if(pos.getFieldAttribute() == null) {
			int i = pos.getField();
			pos = new FieldPosition(fieldAtIndex(i), i);
		}
		
		return format((Mapping)obj, toAppendTo, pos);
	}
	
	/**
	 * Formats a given {@link Mapping} based on the specified field position, appending the
	 * output to the string buffer provided. 
	 * @param mapping The mapping to format.
	 * @param toAppendTo The string buffer to append to.
	 * @param pos The field position to use.
	 * @return A string buffer with the information from <code>mapping</code> corresponding to the
	 * field position <code>pos</code> appended to the one provided.
	 * @throws IllegalArgumentException For the following reasons:
	 * <ul> 
	 * 	<li>An attempt is made to format an alignment with a {@link IndexBase#nullValue null index}.</li>
	 * 	<li>The value of <code>pos.getFieldAttribute()</code> is not an instance of {@link MappingFormat.Field}.
	 * 	<li>The encoding scheme does not contain the field specified by <code>pos</code>.</li>
	 * 	<li>The specified {@linkplain #getFields() list of fields} does not contain the field specified by
	 * 		<code>pos</code>.
	 * 	<li>Either a field required by the encoding scheme or the field specified by <code>pos</code>
	 * 		has a <code>null</code> value.
	 * </ul>
	 */
	public StringBuffer format(Mapping mapping, StringBuffer toAppendTo, FieldPosition pos) {
		if(mapping.a.equals(Alignments.DEFAULT_INDEX_BASE.nullValue)
				|| mapping.b.equals(Alignments.DEFAULT_INDEX_BASE.nullValue)) {
			throw new IllegalArgumentException("cannot format null mapping: " + mapping);
		}
		
		java.text.Format.Field f = pos.getFieldAttribute();
		if(!(f instanceof MappingFormat.Field)) {
			int i = pos.getField();
			pos = new FieldPosition(fieldAtIndex(i), i);
		}
		
		Field field = (MappingFormat.Field)f;
		
		if(!encodingScheme.getOrder().contains(field)) {
			throw new IllegalArgumentException("no such field \'" + field + "\' in scheme " + encodingScheme);
		}
		if(!fields.contains(field)) {
			throw new IllegalArgumentException("field \'" + field + "\' not specified for this format");
		}
		
		int start = toAppendTo.length();
		for(Field af : encodingScheme.getOrder()) {
			if(!fields.contains(af)) {
				continue;
			}
			
			Object val = null;
			
			if(af.equals(PHRASE_NUMBER_FIELD)) {
				val = mapping.phraseNumber;				
			}
			else if(af.equals(A_INDEX_FIELD)) {
				val = mapping.a;
			}
			else if(af.equals(B_INDEX_FIELD)) {
				val = mapping.b;
			}
			else if(af.equals(STATUS_FIELD)) {
				val = (strict || encodingScheme.getRequired().contains(STATUS_FIELD)) ? mapping.status
						: (mapping.status == DEFAULT_STATUS) ? null : mapping.status;
			}
			else if(af.equals(CONFIDENCE_FIELD)) {
				val = (strict || encodingScheme.getRequired().contains(CONFIDENCE_FIELD)) ? mapping.confidence
						: (mapping.confidence.equals(DEFAULT_CONFIDENCE)) ? null : mapping.confidence;
			}
			
			if(val == null) { // skip nulls, but check
				if(encodingScheme.getRequired().contains(af)) {
					throw new IllegalArgumentException("required field " + af + " contains null value");
				}
				if(field.equals(af)) {
					throw new IllegalArgumentException("specified field " + field + " contains null value");
				} 
			}
			else {
				if(af.equals(PHRASE_NUMBER_FIELD)
						|| af.equals(A_INDEX_FIELD) || af.equals(B_INDEX_FIELD)) { // translate indices?
					boolean pn = af.equals(PHRASE_NUMBER_FIELD);
					IndexBase mappingBase = pn ? encodingScheme.getPhraseNumberBase()
								: encodingScheme.getIndexBase(),
							defaultBase = pn ? DEFAULT_PHRASE_NUMBER_BASE : DEFAULT_INDEX_BASE;
					
					val = defaultBase.translate((Integer)val, mappingBase);
				}
				else if(af.equals(STATUS_FIELD)) {
					val = ((Status)val).abbreviation;
				}
			
				if(start < toAppendTo.length()) {
					toAppendTo.append(encodingScheme.getFieldDelimiter());
				}
				
				if(field.equals(af)) {
					pos.setBeginIndex(toAppendTo.length());
				}
				
				toAppendTo.append(val);
				
				if(field.equals(af)) {
					pos.setEndIndex(toAppendTo.length());
				}
			}
		}
		
		return toAppendTo;
	}
	
	Field fieldAtIndex(int i) throws IndexOutOfBoundsException {
		Field f = encodingScheme.getOrder().get(i);
		if(f == null) {
			throw new IndexOutOfBoundsException("no field at position " + i);
		}
		
		return f;
	}
	
	/**
	 * Parses a {@link Mapping} from a given string, based on the {@linkplain #getEncodingScheme() encoding
	 * scheme} in effect. 
	 * @param source The string to parse.
	 * @return A mapping object representing the specified string.
	 * @throws ParseException If the string is ill-formed according to this formatter/parser's
	 * {@link #getEncodingScheme() encoding scheme}. The exception thrown will contain an
	 * {@linkplain ParseException#getErrorOffset() error offset} reflecting the position in the string where
	 * the parse error occurred, if possible. 
	 */
	public Mapping parseMapping(String source) throws ParseException {
		ParsePosition pos = new ParsePosition(0);
		Mapping m = (Mapping)parseObject(source, pos);
		if(pos.getErrorIndex() != -1) {
			throw new ParseException("problem parsing input \"" + source + "\"", pos.getErrorIndex());
		}
		
		return m;
	}
	
	/**
	 * Overrides the {@link Format#parseObject(String, ParsePosition)} method to return a mapping, parsing
	 * from the specified {@link ParsePosition}.
	 * @see #parseMapping(String) 
	 */
	@Override
	public Object parseObject(String source, ParsePosition pos) {
		if(pos == null) {
			throw new NullPointerException("parse position is null");
		}
		
		int index = pos.getIndex();
		
		Matcher matcher = mappingPattern.matcher(source);
		if(!matcher.matches()) {
			pos.setErrorIndex(index);
			return null;
		}		
		if(matcher.start() != index) {
			pos.setErrorIndex(index);
			return null;
		}
		
		String[] chunks = fieldPattern.split(source);
		Iterator<MappingFormat.Field> oi = encodingScheme.getOrder().iterator();
		Map<MappingFormat.Field, Object> values
			= new HashMap<MappingFormat.Field, Object>(encodingScheme.getOrder().size());
		
		for(int i = 0; i < chunks.length; i++) {
			String c = chunks[i];
			
			if(c.length() == 0 || !oi.hasNext()) {
				pos.setErrorIndex(index);
				return null;
			}
			
			Object val;
			MappingFormat.Field af = null;
			while(oi.hasNext()) {
				af = oi.next();
				try {
					if(af.equals(PHRASE_NUMBER_FIELD)
							|| af.equals(A_INDEX_FIELD) || af.equals(B_INDEX_FIELD)) {
						boolean pn = af.equals(PHRASE_NUMBER_FIELD);
						IndexBase mappingBase = pn ? encodingScheme.getPhraseNumberBase()
									: encodingScheme.getIndexBase(),
								defaultBase = pn ? DEFAULT_PHRASE_NUMBER_BASE : DEFAULT_INDEX_BASE;
						
						try {
							val = mappingBase.translate(Integer.valueOf(c), defaultBase);
						}
						catch(IllegalArgumentException iie) { // thrown by IndexBase.translate()
							pos.setErrorIndex(index);
							return null;
						}
						
						// can't have null value
						if(((Integer)val).equals(defaultBase.nullValue)) {
							pos.setErrorIndex(index);
							return null;
						}
					}
					else if(af.equals(STATUS_FIELD)) {
						val = Status.forAbbreviation(c);
					}
					else if(af.equals(CONFIDENCE_FIELD)) {
						val = Double.valueOf(c);
					}
					else {
						val = null;
					}
				}
				catch(NumberFormatException e) {
					pos.setErrorIndex(index);
					return null;
				}
				
				if(val == null) {
					if(encodingScheme.getRequired().contains(af) || (strict && fields.contains(af))) {
						pos.setErrorIndex(index);
						return null;
					}
					
					continue; // keep going if not required
				}
				
				values.put(af, val);
				break;
			}
			
			// update parse index
			index += c.length();
			if(i < chunks.length - 1) {
				index++; // add one for delimiter
			}
		}
		
		pos.setIndex(matcher.end());
		
		Set<Field> keys = values.keySet();
		if(!keys.containsAll(encodingScheme.getRequired()) || (strict && !keys.containsAll(fields))) {
			pos.setErrorIndex(index);
			return null;
		}
		
		Integer id = values.containsKey(PHRASE_NUMBER_FIELD) ? (Integer)values.get(PHRASE_NUMBER_FIELD) : null;
		
		Integer first = values.containsKey(A_INDEX_FIELD) ? (Integer)values.get(A_INDEX_FIELD) : null,
			second = values.containsKey(B_INDEX_FIELD) ? (Integer)values.get(B_INDEX_FIELD) : null;
				
		Status status = (Status)values.get(STATUS_FIELD);
		if(status == null && STATUS_FIELD.hasDefaultValue()) {
			status = (Status)STATUS_FIELD.defaultValue;
		}
		
		Double confidence = (Double)values.get(CONFIDENCE_FIELD);
		if(confidence == null && CONFIDENCE_FIELD.hasDefaultValue()) {
			confidence = (Double)CONFIDENCE_FIELD.defaultValue;
		}
		
		return new Mapping(id, first, second, status, confidence);
	}
}
