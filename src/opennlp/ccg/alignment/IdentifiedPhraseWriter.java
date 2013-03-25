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

import java.io.IOException;
import java.io.Writer;

import static opennlp.ccg.alignment.Alignments.*;

/**
 * A writer for phrases with {@linkplain Phrase#getId() ids} in addition to
 * {@linkplain Phrase#getNumber() numbers}. Identified phrases are ones read from
 * markup that signals an ID for each phrase.
 * <p>
 * In addition to allowing the phrase tag and identifier attribute to be configured, this class can optionally
 * output {@linkplain #isPadding() padding} of a single space between the markup and the phrase.
 * 
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @see IdentifiedPhraseReader
 */
public class IdentifiedPhraseWriter extends PhraseWriter {

	final String phraseTag, phraseIdentifierAttribute;
	final boolean padding;
	
	/**
	 * Creates a new identified phrase writer. The word separator used is
	 * {@link Alignments#DEFAULT_WORD_SEPARATOR}.
	 * @see #IdentifiedPhraseWriter(Writer, String)
	 */
	public IdentifiedPhraseWriter(Writer out) {
		this(out, Alignments.DEFAULT_WORD_SEPARATOR);
	}

	/**
	 * Creates a new identified phrase writer with the given word separator. The phrase tag used is the default,
	 * {@link Alignments#DEFAULT_PHRASE_TAG}, as is the phrase ID attribute
	 * ({@link Alignments#DEFAULT_PHRASE_IDENTIFIER_ATTRIBUTE}).
	 * @see #IdentifiedPhraseWriter(Writer, String, String, String)
	 */
	public IdentifiedPhraseWriter(Writer out, String wordSeparator) {
		this(out, wordSeparator, DEFAULT_PHRASE_TAG, DEFAULT_PHRASE_IDENTIFIER_ATTRIBUTE);
	}
	
	/**
	 * Creates a new identified phrase writer with the given word separator. The phrase tag used is the default,
	 * {@link Alignments#DEFAULT_PHRASE_TAG}, as is the phrase ID attribute
	 * ({@link Alignments#DEFAULT_PHRASE_IDENTIFIER_ATTRIBUTE}).
	 * @see #IdentifiedPhraseWriter(Writer, String, String, String, boolean)
	 */
	public IdentifiedPhraseWriter(Writer out, String wordSeparator, String phraseTag,
			String phraseIdentifierAttribute) {
		this(out, wordSeparator, phraseTag, phraseIdentifierAttribute, DEFAULT_PHRASE_PADDING);
	}
	
	/**
	 * Creates a new identified phrase writer for writing phrases to the underlying writer.
	 * @param phraseTag The name of the tag that holds the phrase identifier.
	 * @param phraseIdentifierAttribute The phrase identifier attribute.
	 * @param padding Whether this writer should write a space between the pre-markup and the phrase, and
	 * between the phrase and the post-markup.
	 * @throws IllegalArgumentException if <tt>phraseTag</tt> or <tt>phraseIdentifierAttribute</tt>
	 * is <tt>null</tt>.
	 */
	public IdentifiedPhraseWriter(Writer out, String wordSeparator, String phraseTag,
			String phraseIdentifierAttribute, boolean padding) {
		super(out, wordSeparator);
		
		if(phraseTag == null) {
			throw new IllegalArgumentException("phraseTag is null");
		}
		if(phraseIdentifierAttribute == null) {
			throw new IllegalArgumentException("phraseIdentifierAttribute is null");
		}
		
		this.phraseTag = phraseTag;
		this.phraseIdentifierAttribute = phraseIdentifierAttribute;
		this.padding = padding;
	}

	/**
	 * Gets the phrase tag this writer generates with each phrase.
	 */
	public String getPhraseTag() {
		return phraseTag;
	}

	/**
	 * Gets the phrase identifier attribute used to signal the phrase ID for each phrase.
	 */
	public String getPhraseIdentifierAttribute() {
		return phraseIdentifierAttribute;
	}

	/**
	 * Tests whether this writer writes space padding between its pre- and post-markup and the phrase itself.
	 * @return true if this writer uses padding.
	 */
	public boolean isPadding() {
		return padding;
	}

	/**
	 * Writes the start markup indicating a phrase's ID. The tag and ID attribute are the ones used to
	 * create this writer.
	 * 
	 * @see #getPhraseTag()
	 * @see #getPhraseIdentifierAttribute()
	 * @throws IOException If a problem occurs in the underlying writer.
	 */
	@Override
	public void preWritePhrase(Phrase phrase) throws IOException {
		String id = phrase.getId();
		if(id == null) {
			throw new IOException("attempt to write phrase with null ID: " + phrase);
		}
		
		printWriter.print('<');
		printWriter.print(phraseTag);
		printWriter.print(' ');
		printWriter.print(phraseIdentifierAttribute);
		printWriter.print("=\"");
		printWriter.print(id);
		printWriter.print("\">");
		
		if(padding) {
			printWriter.print(' ');
		}
	}
	
	/**
	 * Writes the end markup signaling the end of a phrase, plus a line separator.
	 * 
	 * @throws IOException If a problem occurs in the underlying writer.
	 */
	@Override
	public void postWritePhrase(Phrase phrase) throws IOException {
		if(padding) {
			printWriter.print(' ');
		}
		
		printWriter.print("</");
		printWriter.print(phraseTag);
		printWriter.println(">");
	}
}
