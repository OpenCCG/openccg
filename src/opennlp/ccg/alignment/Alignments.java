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

import static opennlp.ccg.alignment.MappingFormat.Field.A_INDEX_FIELD;
import static opennlp.ccg.alignment.MappingFormat.Field.B_INDEX_FIELD;
import static opennlp.ccg.alignment.MappingFormat.Field.PHRASE_NUMBER_FIELD;
import static opennlp.ccg.alignment.MappingFormat.Field.STATUS_FIELD;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import opennlp.ccg.alignment.MappingFormat.Field;

/**
 * Static non-instantiable class that provides convenience methods for reading
 * and writing phrases, mappings, and alignments.
 * <p>
 * The convenience methods <tt>tokenize</tt> split a string into an array of strings, and the
 * <tt>untokenize</tt> methods provide their inverses. 
 * <p>
 * The <code>readXxxPhrases()</code> methods all call {@link #readPhrases(PhraseReader)} to read
 * {@linkplain Phrase phrases} from an underlying reader. Depending on the method, phrases are assumed to be
 * identified or to merely occur in sequence. Similarly, the <code>writeXxxPhrases</code> all call
 * {@link #writePhrases(List, PhraseWriter)}.
 * <p>
 * {@linkplain Mapping Mappings} can be read using <code>readMappings(...)</code> methods, which will read
 * mappings as formatted by the specified
 * format. The <code>readSortedMappings(...)</code> methods are variants of these that return a map with
 * sorted keys that map to {@linkplain SortedSet sorted sets} of mappings.  
 * The methods {@link #writeMappings(Map, File, MappingFormat)} and
 * {@link #writeMappings(Map, Writer, MappingFormat)} perform the inverse of the methods for reading. 
 * <p>
 * Finally, the <code>readXxxAlignments()</code> methods combine the methods for reading phrases and mappings to
 * allow {@linkplain Alignments alignments} to be read. The methods
 * {@link #writeAlignments(List, File, File, File, String, MappingFormat)} and
 * {@link #writeAlignments(List, Writer, Writer, Writer, String, MappingFormat)} write alignments according to
 * a specified word separator and mapping format.
 * 
 * @see PhraseReader
 * @see PhraseWriter
 * @see IdentifiedPhraseReader
 * @see IdentifiedPhraseWriter
 * @see MappingReader
 * @see MappingWriter 
 * @see MappingFormat
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public final class Alignments {

	/**
	 * The default status: {@link Status#SURE}.
	 */
	public static final Status DEFAULT_STATUS = Status.SURE;
	
	/**
	 * The default mapping confidence: <tt>1.0</tt>.
	 */
	public static final Double DEFAULT_CONFIDENCE = Double.valueOf(1.0d);
	
	/**
	 * The default phrase numbering base: {@link IndexBase#ZERO}.
	 */
	public static final IndexBase DEFAULT_PHRASE_NUMBER_BASE = IndexBase.ZERO;
	
	/**
	 * The default index base: {@link IndexBase#ZERO}.
	 */
	public static final IndexBase DEFAULT_INDEX_BASE = IndexBase.ZERO;
	
	/**
	 * The default phrase position: {@link PhrasePosition#A}.
	 */
	public static final PhrasePosition DEFAULT_PHRASE_POSITION = PhrasePosition.A;
	
	/**
	 * The default word delimiter pattern, which matches multiple whitespace characters.
	 */
	public static final Pattern DEFAULT_WORD_DELIMITER = Pattern.compile("\\s+");
	
	/**
	 * The default word separator, a single space.
	 */
	public static final String DEFAULT_WORD_SEPARATOR = " ";
	
	/**
	 * The default phrase identifier attribute name, &quot;snum&quot;.
	 */
	public static final String DEFAULT_PHRASE_IDENTIFIER_ATTRIBUTE = "snum";
	
	/**
	 * The default phrase tag, &quot;s&quot;.
	 */
	public static final String DEFAULT_PHRASE_TAG = "s";
	
	/**
	 * The Moses encoding scheme.
	 */
	public static final EncodingScheme MOSES_ENCODING_SCHEME;
	
	/**
	 * The NAACL encoding scheme.
	 */
	public static final EncodingScheme NAACL_ENCODING_SCHEME;
	
	/**
	 * The default fields for the Moses format.
	 */
	public static final Set<MappingFormat.Field> MOSES_DEFAULT_FIELDS;
	
	/**
	 * The default fields for the NAACL format.
	 */
	public static final Set<MappingFormat.Field> NAACL_DEFAULT_FIELDS;
	
	/**
	 * The fields used in the abbreviated Moses format: {@link Field#A_INDEX_FIELD},
	 * {@link Field#B_INDEX_FIELD}.
	 */
	public static final Set<MappingFormat.Field> MOSES_SHORT_FIELDS;
	
	/**
	 * The fields used in the abbreviated NAACL format: {@link Field#PHRASE_NUMBER_FIELD},
	 * {@link Field#A_INDEX_FIELD}, {@link Field#B_INDEX_FIELD}, {@link Field#STATUS_FIELD}.
	 */
	public static final Set<MappingFormat.Field> NAACL_SHORT_FIELDS;
	
	/**
	 * The fields used in the extremely abbreviated NAACL format: {@link Field#PHRASE_NUMBER_FIELD},
	 * {@link Field#A_INDEX_FIELD}, {@link Field#B_INDEX_FIELD}.
	 */
	public static final Set<MappingFormat.Field> NAACL_VERY_SHORT_FIELDS;
	
	/**
	 * The default {@linkplain MappingFormat#isStrict() mapping format strictness}: <tt>false</tt>.
	 */
	public static final boolean DEFAULT_STRICTNESS = false;
	
	/**
	 * The default {@linkplain IdentifiedPhraseWriter#isPadding() identified phrase writer padding}:
	 * <tt>false</tt>.
	 */
	public static final boolean DEFAULT_PHRASE_PADDING = false;
	
	
	static {
		Set<Field> msf = new HashSet<MappingFormat.Field>(),
				nsf = new HashSet<MappingFormat.Field>(),
				nvsf = new HashSet<MappingFormat.Field>();
		
		msf.add(A_INDEX_FIELD);
		msf.add(B_INDEX_FIELD);
		
		nsf.add(PHRASE_NUMBER_FIELD);
		nsf.add(A_INDEX_FIELD);
		nsf.add(B_INDEX_FIELD);
		nsf.add(STATUS_FIELD);
		
		nvsf.add(PHRASE_NUMBER_FIELD);
		nvsf.add(A_INDEX_FIELD);
		nvsf.add(B_INDEX_FIELD);
		
		MOSES_SHORT_FIELDS = Collections.unmodifiableSet(msf);
		NAACL_SHORT_FIELDS = Collections.unmodifiableSet(nsf);
		NAACL_VERY_SHORT_FIELDS = Collections.unmodifiableSet(nvsf);
		
		MOSES_DEFAULT_FIELDS = MOSES_SHORT_FIELDS;
		NAACL_DEFAULT_FIELDS = NAACL_SHORT_FIELDS;
		
		// these have to come last, they depend on some of the others
		MOSES_ENCODING_SCHEME = new MosesEncodingScheme(); 
		NAACL_ENCODING_SCHEME = new NAACLEncodingScheme();
	}
	
	private Alignments() {
		// this class should not be instantiated
	}
	
	/**
	 * Tokenizes a string according to the {@linkplain #DEFAULT_WORD_DELIMITER default word delimiter}.
	 */
	public static String[] tokenize(String s) {
		return tokenize(s, DEFAULT_WORD_DELIMITER);
	}
	
	/**
	 * Tokenizes an array of strings according to the {@linkplain #DEFAULT_WORD_DELIMITER default word
	 * delimiter pattern}.
	 */
	public static String[] tokenize(String s, Pattern wordDelimiter) {
		return wordDelimiter.split(s);
	}
	
	/**
	 * Untokenizes a list of tokens into a single string, with former tokens separated by the
	 * {@linkplain #DEFAULT_WORD_SEPARATOR}.
	 * 
	 * @see #untokenize(List, String)
	 */
	public static String untokenize(List<String> tokens) {
		return untokenize(tokens, DEFAULT_WORD_SEPARATOR);
	}
	
	/**
	 * Untokenizes a list of tokens into a single string, with former tokens separated by the
	 * specified <tt>delimiter</tt> string.
	 */
	public static String untokenize(List<String> tokens, String delimiter) {
		return untokenize(tokens.toArray(new String[tokens.size()]));
	}
	
	/**
	 * Untokenizes an array of tokens into a single string using the
	 * {@linkplain #DEFAULT_WORD_SEPARATOR default word separator}.
	 * @param tokens
	 * @return An untokenized string from the given tokens, with individual
	 * tokens separated by the default word separator.
	 */
	public static String untokenize(String[] tokens) {
		return untokenize(tokens, DEFAULT_WORD_SEPARATOR);
	}
	
	/**
	 * Untokenizes an array of tokens into a single string, with former tokens separated by the
	 * specified <tt>delimiter</tt> string.
	 */
	public static String untokenize(String[] tokens, String delimiter) {
		StringBuilder sb = new StringBuilder();
		
		for(String w : tokens) {
			if(sb.length() > 0) {
				sb.append(delimiter);
			}
			
			sb.append(w.toString());
		}
		
		return sb.toString();
	}
	
	/**
	 * Reads phrases sequentially from the specified file.
	 * @see #readPhrases(Reader)
	 */
	public static List<Phrase> readPhrases(File f) throws IOException {
		return readPhrases(new BufferedReader(new FileReader(f)));
	}
	
	/**
	 * Reads phrases sequentially from the specified reader.
	 * @see #readPhrases(Reader, IndexBase)
	 */
	public static List<Phrase> readPhrases(Reader r) throws IOException {
		return readPhrases(r, DEFAULT_PHRASE_NUMBER_BASE);
	}
	
	/**
	 * Reads phrases from the specified reader. Phrases will have their line numbers translated into the
	 * specified index base.
	 * 
	 * @see #readPhrases(PhraseReader)
	 */
	public static List<Phrase> readPhrases(Reader r, IndexBase phraseNumberBase)
			throws IOException {
		PhraseReader reader = new PhraseReader(r, phraseNumberBase);
		
		try {
			return readPhrases(reader);
		}
		finally {
			reader.close();
		}
	}
	
	/**
	 * Reads phrases from the specified file.
	 * 
	 * @see #readIdentifiedPhrases(Reader)
	 */
	public static List<Phrase> readIdentifiedPhrases(File f) throws IOException {
		return readIdentifiedPhrases(new BufferedReader(new FileReader(f)));
	}
	
	/**
	 * Reads phrases from the specified reader, using the default
	 * {@linkplain #DEFAULT_PHRASE_NUMBER_BASE phrase number base},
	 * {@linkplain #DEFAULT_PHRASE_TAG phrase tag}, and
	 * {@linkplain #DEFAULT_PHRASE_IDENTIFIER_ATTRIBUTE phrase identifier attribute}.
	 * 
	 * @see #readIdentifiedPhrases(Reader, IndexBase, String, String)
	 */
	public static List<Phrase> readIdentifiedPhrases(Reader r) throws IOException {
		return readIdentifiedPhrases(r, DEFAULT_PHRASE_NUMBER_BASE, DEFAULT_PHRASE_TAG,
				DEFAULT_PHRASE_IDENTIFIER_ATTRIBUTE);
	}
	
	/**
	 * Reads phrases from the specified reader. The input is assumed to have markup accompanying the phrase data
	 * that indicates the {@linkplain Phrase#getId() phrase ID} for each phrase.
	 * 
	 * @param r The underlying reader.
	 * @param phraseNumberBase The index base to translate line numbers into.
	 * @param phraseTag The tag name for markup signaling a phrase.
	 * @param phraseIdentifierAttribute The name of the attribute that contains the phrase's ID in the markup.
	 * 
	 * @see #readPhrases(PhraseReader)
	 */
	public static List<Phrase> readIdentifiedPhrases(Reader r, IndexBase phraseNumberBase, String phraseTag,
			String phraseIdentifierAttribute) throws IOException {
		PhraseReader reader = new IdentifiedPhraseReader(r, phraseNumberBase, phraseTag,
				phraseIdentifierAttribute);
		
		try {
			return readPhrases(reader);
		}
		finally {
			reader.close();
		}
	}
	
	/**
	 * Reads phrases sequentially from the specified phrase reader.
	 * 
	 * @return A list of phrases in the order they are encountered by calling {@link PhraseReader#readPhrase()}
	 * on the specified <tt>reader</tt>.
	 * @throws IOException if one is thrown by the specified phrase reader.
	 * @see PhraseReader
	 */
	public static List<Phrase> readPhrases(PhraseReader reader) throws IOException {
		List<Phrase> l = new ArrayList<Phrase>();
		
		Phrase p;
		while((p = reader.readPhrase()) != null) {
			l.add(p);
		}
		
		return l;
	}
	
	/**
	 * Writes a list of phrases with IDs to the specified file.
	 * 
	 * @see #writeIdentifiedPhrases(List, Writer)
	 */
	public static void writeIdentifiedPhrases(List<Phrase> phrases, File f) throws IOException {
		writeIdentifiedPhrases(phrases, new BufferedWriter(new FileWriter(f)));
	}
	
	/**
	 * Writes a list of phrases with IDs to the specified writer, using the default
	 * {@linkplain #DEFAULT_WORD_SEPARATOR word separator},
	 * {@linkplain #DEFAULT_PHRASE_TAG phrase tag}, 
	 * {@linkplain #DEFAULT_PHRASE_IDENTIFIER_ATTRIBUTE phrase identifier attribute}, and
	 * {@linkplain #DEFAULT_PHRASE_PADDING padding flag}.
	 * 
	 * @see #writeIdentifiedPhrases(List, Writer, String, String, String, boolean)
	 */
	public static void writeIdentifiedPhrases(List<Phrase> phrases, Writer w) throws IOException {
		writeIdentifiedPhrases(phrases, w, DEFAULT_WORD_SEPARATOR, DEFAULT_PHRASE_TAG,
				DEFAULT_PHRASE_IDENTIFIER_ATTRIBUTE, DEFAULT_PHRASE_PADDING);
	}
	
	/**
	 * Writes a list of phrases to the specified writer.
	 * 
	 * @param phrases The phrases to write.
	 * @param w The underlying writer.
	 * @param wordSeparator The string to use to {@linkplain #untokenize(String[], String) untokenize} with.
	 * @param phraseTag The name of the tag used to signal a phrase in the output markup.
	 * @param phraseIdentifierAttribute The name of the attribute bearing the phrase's ID in the output markup.
	 * @param padding Whether or not to include padding between the markup and the phrase data.
	 * 
	 * @see #writePhrases(List, PhraseWriter)
	 */
	public static void writeIdentifiedPhrases(List<Phrase> phrases, Writer w, String wordSeparator,
			String phraseTag, String phraseIdentifierAttribute, boolean padding) throws IOException {
		PhraseWriter pw = new IdentifiedPhraseWriter(w, wordSeparator, phraseTag, phraseIdentifierAttribute,
				padding);
		
		try {
			writePhrases(phrases, pw);
		}
		finally {
			pw.close();
		}
	}	
	
	/**
	 * Writes a list of phrases to the specified file.
	 * 
	 * @see #writePhrases(List, Writer)
	 */
	public static void writePhrases(List<Phrase> phrases, File f) throws IOException {
		writePhrases(phrases, new BufferedWriter(new FileWriter(f)));
	}
	
	/**
	 * Writes a list of phrases to the specified writer using the 
	 * {@linkplain #DEFAULT_WORD_SEPARATOR default word separator}.
	 * 
	 * @see #writePhrases(List, Writer, String)
	 */
	public static void writePhrases(List<Phrase> phrases, Writer w) throws IOException {
		writePhrases(phrases, w, DEFAULT_WORD_SEPARATOR);
	}
	
	/**
	 * Writes a list of phrases to the specified writer using the specified word separator.
	 * 
	 * @see #writePhrases(List, PhraseWriter)
	 */
	public static void writePhrases(List<Phrase> phrases, Writer w, String wordSeparator)
			throws IOException {
		PhraseWriter pw = new PhraseWriter(w, wordSeparator);
		
		try {
			writePhrases(phrases, pw);
		}
		finally {
			pw.close();
		}
	}
	
	/**
	 * Writes a list of phrases to the specified phrase writer.
	 * 
	 * @param phrases The phrases to write.
	 * @param writer The underlying phrase writer.
	 * @throws IOException if a call to {@link PhraseWriter#writePhrase(Phrase)} throws one for one of the
	 * <tt>phrases</tt>.
	 */
	public static void writePhrases(List<Phrase> phrases, PhraseWriter writer) throws IOException {
		for(Phrase p : phrases) {
			writer.writePhrase(p);
		}
	}
	
	/**
	 * Reads mappings from the specified file using the specified format.
	 * 
	 * @see #readMappings(Reader, MappingFormat)
	 */
	public static Map<Integer, Set<Mapping>> readMappings(File f, MappingFormat format)
			throws IOException {
		return readMappings(new BufferedReader(new FileReader(f)), format);
	}
	
	/**
	 * Reads mappings from the specified reader using the specified format.
	 * 
	 * @see #readMappings(MappingReader)
	 */
	public static Map<Integer, Set<Mapping>> readMappings(Reader r, MappingFormat format)
			throws IOException {
		MappingReader mr = new MappingReader(r, format);
		
		try {
			return readMappings(mr);
		}
		finally {
			mr.close();
		}
	}
	
	/**
	 * Reads mappings from the specified mapping reader. Once all available mappings have been read, the
	 * specified reader is {@linkplain MappingReader#close() closed}.
	 * 
	 * @param reader The mapping reader to read mappings from.
	 * @return A map whose keys are the {@linkplain Phrase#getNumber() phrase numbers} of the corresponding
	 * phrases and whose values are sets containing the mappings for the key phrase. Both the keys and the 
	 * sets of mappings are maintained in the order in which they occur in the input. 
	 * 
	 * @throws IOException if the underlying reader throws an exception, or if one of the 
	 * {@linkplain MappingGroup mapping groups} contains a duplicate mapping.
	 * 
	 * @see MappingReader
	 */
	public static Map<Integer, Set<Mapping>> readMappings(MappingReader reader) throws IOException {
		Map<Integer, Set<Mapping>> am = new LinkedHashMap<Integer, Set<Mapping>>();
		
		try {
			MappingGroup ag = null;
			while((ag = reader.nextGroup()) != null) {
				Set<Mapping> as = am.get(ag.phraseNumber);
				if(as == null) {
					as = new LinkedHashSet<Mapping>();
					am.put(ag.phraseNumber, as);
				}
				
				while(reader.canRead()) {
					Mapping m = reader.readMapping();
					if(!as.add(m)) {
						throw new IOException("duplicate mapping in group " + ag + ": " + m);
					}
				}
			}
		}
		finally {
			reader.close();
		}
		
		return am;
	}
	
	/**
	 * Reads mappings into a sorted map from the specified file, based on the specified format.
	 * 
	 * @see #readSortedMappings(Reader, MappingFormat)
	 */
	public static SortedMap<Integer, SortedSet<Mapping>> readSortedMappings(File f, MappingFormat format) 
			throws IOException {
		return readSortedMappings(new BufferedReader(new FileReader(f)), format);
	}
	
	/**
	 * Reads mappings into a sorted map from the specified reader, using the specified format to parse
	 * mappings. 
	 * 
	 * @see #readSortedMappings(MappingReader)
	 */
	public static SortedMap<Integer, SortedSet<Mapping>> readSortedMappings(Reader r, MappingFormat format)
			throws IOException {
		MappingReader mr = new MappingReader(r, format);
		
		try {
			return readSortedMappings(mr);
		}
		finally {
			mr.close();
		}
	}
	
	/**
	 * Reads mappings into a sorted map from the specified reader, using the specified format to parse
	 * mappings.
	 * 
	 * @param mr The mapping reader to use.
	 * @return A sorted map whose keys and values are also sorted according to their natural order.
	 * 
	 * @throws IOException if the underlying reader throws an exception.
	 * 
	 * @see Mapping#compareTo(Mapping)
	 */
	public static SortedMap<Integer, SortedSet<Mapping>> readSortedMappings(MappingReader mr)
			throws IOException {
		SortedMap<Integer, SortedSet<Mapping>> sm = new TreeMap<Integer, SortedSet<Mapping>>();
		
		Map<Integer, Set<Mapping>> m = readMappings(mr);
		
		for(Integer k : m.keySet()) {
			sm.put(k, new TreeSet<Mapping>(m.get(k)));
		}
		
		return sm;
	}
	
	/**
	 * Writes the specified map to the specified file using the format provided.
	 * 
	 * @see #writeMappings(Map, Writer, MappingFormat)
	 */
	public static void writeMappings(Map<Integer, Set<Mapping>> map, File f, MappingFormat format)
			throws IOException {
		writeMappings(map, new BufferedWriter(new FileWriter(f)), format);
	}
	
	/**
	 * Writes the specified map to the specified writer using the format provided.
	 * 
	 * @see #writeMappings(Map, MappingWriter)
	 */
	public static void writeMappings(Map<Integer, Set<Mapping>> map, Writer w, MappingFormat format)
			throws IOException {
		writeMappings(map, new MappingWriter(w, format));
	}
	
	/**
	 * Writes the specified map to the specified writer, starting {@linkplain MappingGroup mapping groups}
	 * as needed based on the key and the {@linkplain Set#size() size} of the value set. After all the sets
	 * of mappings in the map have been written, {@link MappingWriter#close()} is called.
	 * 
	 * @param map The mappings to write.
	 * @param writer The underlying writer to write to.
	 * @see MappingGroup
	 * @throws IOException if one occurs in the underlying writer.
	 */
	public static void writeMappings(Map<Integer, Set<Mapping>> map, MappingWriter writer) throws IOException {
		try {
			for(Integer k : map.keySet()) {
				Set<Mapping> as = map.get(k);
				
				writer.startGroup(new MappingGroup(k, as.size()));			
				
				for(Mapping a : as) {
					writer.writeMapping(a);
				}
			}
		}
		finally {
			writer.close();
		}
	}
	
	/**
	 * Reads alignments from the specified files, using the provided mapping format.
	 * 
	 * @see #readAlignments(Reader, Reader, Reader, MappingFormat)
	 */
	public static List<Alignment> readAlignments(File phraseA, File phraseB,
			File mappings, Pattern wordDelimiter, MappingFormat format) throws IOException {
		return readAlignments(new BufferedReader(new FileReader(phraseA)),
			new BufferedReader(new FileReader(phraseB)), new BufferedReader(new FileReader(mappings)), format);
	}
	
	/**
	 * Reads alignments from the specified readers, using the provided mapping format.
	 * 
	 * @see #readAlignments(PhraseReader, PhraseReader, Reader, MappingFormat)
	 */
	public static List<Alignment> readAlignments(Reader phraseA, Reader phraseB,
			Reader mappings, MappingFormat format) throws IOException {
		IndexBase idBase = format.encodingScheme.getPhraseNumberBase();
		return readAlignments(new PhraseReader(phraseA, idBase),
				new PhraseReader(phraseB, idBase), mappings, format);
	}
	
	/**
	 * Reads alignments from the specified files, using the provided word delimiter pattern and mapping format.
	 * The files containing phrases are assumed to contain markup indicating the phrase IDs.
	 * 
	 * @see #readIdentifiedAlignments(Reader, Reader, Reader, MappingFormat)
	 */
	public static List<Alignment> readIdentifiedAlignments(File phraseA, File phraseB,
			File mappings, Pattern wordDelimiter, MappingFormat format) throws IOException {
		return readIdentifiedAlignments(new BufferedReader(new FileReader(phraseA)),
				new BufferedReader(new FileReader(phraseB)), new BufferedReader(new FileReader(mappings)), 
				format);
	}
	
	/**
	 * Reads alignments from the specified readers, using the provided word delimiter pattern and mapping format.
	 * The readers for phrases are assumed to have input with markup indicating the phrase IDs.
	 * 
	 * @see #readAlignments(PhraseReader, PhraseReader, Reader, MappingFormat)
	 */
	public static List<Alignment> readIdentifiedAlignments(Reader phraseA,
			Reader phraseB, Reader mappings, MappingFormat format) throws IOException {
		return readAlignments(new IdentifiedPhraseReader(phraseA), new IdentifiedPhraseReader(phraseB),
				mappings, format);
	}
	
	/**
	 * Reads alignments from the specified readers, using the provided format to parse mappings. The
	 * line numbers of the phrases are translated into the
	 * {@linkplain EncodingScheme#getPhraseNumberBase() phrase number base} of the
	 * {@linkplain MappingFormat#getEncodingScheme() format's encoding scheme}.
	 * 
	 * @param phraseA The reader from which the {@linkplain PhrasePosition#A A-position} phrases are read.
	 * @param phraseB The reader from which the {@linkplain PhrasePosition#B B-position} phrases are read.
	 * @param mappings The reader whose input contains the mappings from A-phrases to B-phrases, where the
	 * A-position indices are assumed to correspond to the phrases in <tt>phraseA</tt> and the B-position
	 * indices are assumed to correspond to the phrases in <tt>phraseB</tt>.
	 * @param format The mapping format used to parse mappings read from <tt>mapping</tt>.
	 * @return A list of alignments where the {@linkplain Alignment#getA() A-phrases} are from <tt>phraseA</tt>,
	 * the {@linkplain Alignment#getB() B-phrases} are from <tt>phraseB</tt>, and the mappings are the ones
	 * read from <tt>mappings</tt> with the corresponding {@linkplain Phrase#getNumber() phrase number}. 
	 * @throws IOException if <tt>phraseA</tt> has a different number of phrases than <tt>phraseB</tt>, or if
	 * one is thrown by any of the underlying readers.
	 * 
	 * @see PhraseReader
	 * @see MappingReader
	 * @see #readPhrases(PhraseReader)
	 * @see #readMappings(MappingReader)
	 */
	public static List<Alignment> readAlignments(PhraseReader phraseA, PhraseReader phraseB,
			Reader mappings, MappingFormat format) throws IOException {
		List<Alignment> m = new ArrayList<Alignment>();
				
		try {
			List<Phrase> ps1 = readPhrases(phraseA), ps2 = readPhrases(phraseB);
			Map<Integer, Set<Mapping>> mm = readMappings(mappings, format);
			
			// sanity check
			if(ps1.size() != ps2.size()) {
				throw new IOException("number of phrases different between first and second");
			}

			for(int i = 0; i < ps1.size(); i++) {				
				m.add(new Alignment(ps1.get(i), ps2.get(i), mm.get(i)));
			}
		}
		finally {
			phraseA.close();
			phraseA.close();
			mappings.close();
		}
		
		return m;
	}
	
	/**
	 * Writes a list of alignments to the specified files, using the word separator and format provided.
	 * 
	 * @see #writeAlignments(List, Writer, Writer, Writer, String, MappingFormat)
	 */
	public static void writeAlignments(List<Alignment> alignments,
			File phraseA, File phraseB, File mappings, String wordSeparator, MappingFormat format)
				throws IOException {
		writeAlignments(alignments, new BufferedWriter(new FileWriter(phraseA)),
			new BufferedWriter(new FileWriter(phraseB)), new BufferedWriter(new FileWriter(mappings)),
				wordSeparator, format);
	}
	
	/**
	 * Writes a list of alignments to the specified readers, using the word separator and format provided.
	 * After all the <tt>alignments</tt> are written, all the associated writers are closed.
	 * 
	 * @param alignments The alignments to write.
	 * @param phraseA The writer to which {@linkplain Alignment#getA() A-position phrases} are written.
	 * @param phraseB The writer to which {@linkplain Alignment#getB() B-position phrases} are written.
	 * @param mappings The mappings to write, where the {@linkplain Mapping#getA() A-position indices} are
	 * assumed to correspond to the phrases in <tt>phraseA</tt> and the 
	 * {@linkplain Mapping#getB() B-position indices} are assumed to correspond to the ones in <tt>phraseB</tt>.
	 * @param wordSeparator The word separator to use for
	 * {@linkplain #untokenize(List, String) untokenization}.
	 * @param format The format to use for formatting mappings in the <tt>alignments</tt>.
	 * @throws IOException if one is thrown by any of the underlying writers.
	 */
	public static void writeAlignments(List<Alignment> alignments,
			Writer phraseA, Writer phraseB, Writer mappings, String wordSeparator, MappingFormat format)
				throws IOException {
		PhraseWriter pw1 = new PhraseWriter(phraseA, wordSeparator),
			pw2 = new PhraseWriter(phraseB, wordSeparator);
		MappingWriter mw = new MappingWriter(mappings, format);
		
		try {
			for(int i = 0; i < alignments.size(); i++) {
				Alignment a = alignments.get(i);
				
				pw1.writePhrase(a.a);
				pw2.writePhrase(a.b);
	
				mw.startGroup(new MappingGroup(i, a.size()));
				
				for(Mapping m : a) {
					mw.writeMapping(m);
				}
			}
		}
		finally {
			pw1.close();
			pw2.close();
			mw.close();
		}
	}
}
