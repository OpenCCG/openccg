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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * A writer for phrases. This writer writes {@linkplain Phrase phrase} instances to the underlying writer with
 * the specified string as a word separator. No markup is placed around phrases that are written; only a line
 * separator is written after the phrase.
 * 
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @see Alignments#untokenize(String[], String)
 */
public class PhraseWriter extends FilterWriter {

	final String wordSeparator;
	
	protected PrintWriter printWriter;
	
	/**
	 * Creates a new phrase writer with the default word separator.
	 * @see #PhraseWriter(Writer, String)
	 * @see Alignments#DEFAULT_WORD_SEPARATOR
	 */
	public PhraseWriter(Writer out) {
		this(out, Alignments.DEFAULT_WORD_SEPARATOR);
	}

	/**
	 * Creates a new phrase writer for the underlying input stream that will use
	 * the specified word separator when untokenizing phrases.
	 * 
	 * @param wordSeparator The word separator to use when translating phrases into strings.
	 * @throws IllegalArgumentException if <tt>wordSeparator</tt> is <tt>null</tt>.
	 * @see Alignments#untokenize(String[], String) 
	 */
	public PhraseWriter(Writer out, String wordSeparator) {
		super(new PrintWriter(out));
		
		if(wordSeparator == null) {
			throw new IllegalArgumentException("wordSeparator is null");
		}
		
		this.wordSeparator = wordSeparator;
		printWriter = (PrintWriter)this.out;
	}
	
	/**
	 * Gets the word separator that this phrase writer uses when writing phrases.
	 */
	public String getWordSeparator() {
		return wordSeparator;
	}
	
	/**
	 * Writes a phrase by {@linkplain Alignments#untokenize(List, String) untokenizing} its words
	 * according to the {@linkplain #getWordSeparator() word separator being used}. Before writing the
	 * untokenized phrase, {@link #preWritePhrase(Phrase)} is called exactly once. After writing the phrase,
	 * {@link #postWritePhrase(Phrase)} is called exactly once.
	 * 
	 * @param phrase The phrase to write.
	 * @throws IOException If the underlying writer throws an {@link IOException}, or if one is thrown by
	 * either {@link #preWritePhrase(Phrase)} or {@link #postWritePhrase(Phrase)}.
	 */
	public void writePhrase(Phrase phrase) throws IOException {
		preWritePhrase(phrase);
		printWriter.print(Alignments.untokenize(phrase, wordSeparator));
		postWritePhrase(phrase);
	}
	
	/**
	 * Called before {@link #writePhrase(Phrase)} (to be overridden by implementing classes).
	 * @param phrase The phrase about to be written.
	 */
	protected void preWritePhrase(Phrase phrase) throws IOException {
		// default is to do nothing
	}
	
	/**
	 * Called after {@link #writePhrase(Phrase)} (to be overridden by implementing classes). This implementation
	 * just writes a line separator.
	 * @param phrase The phrase that was just written.
	 */
	protected void postWritePhrase(Phrase phrase) throws IOException {
		printWriter.println();
	}
}
