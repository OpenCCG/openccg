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
import java.io.LineNumberReader;
import java.io.Reader;

/**
 * A phrase reader just reads line numbers, but does not search for phrase IDs. The line number of each phrase
 * read is {@linkplain IndexBase#translate(Integer, IndexBase) translated} into the {@link IndexBase} provided
 * at creation.
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 */
public class PhraseReader extends LineNumberReader {

	final IndexBase numberBase;
	
	/**
	 * The index base that starts numbering lines at 1.
	 * @see IndexBase#ONE
	 */
	public static final IndexBase LINE_NUMBER_BASE = IndexBase.ONE;
	
	/**
	 * Creates a phrase reader from the specified underlying reader and word index base for phrase numbers.
	 * @see #PhraseReader(Reader, IndexBase)
	 * @see Alignments#DEFAULT_PHRASE_NUMBER_BASE
	 */
	public PhraseReader(Reader in) {
		this(in, Alignments.DEFAULT_PHRASE_NUMBER_BASE);
	}
	
	/**
	 * Creates a phrase reader.
	 * @param numberBase The target phrase numbering base. Phrases read from the underlying reader will have
	 * their numbers translated from {@link #LINE_NUMBER_BASE the default} to <code>numberBase</code>.
	 * @throws IllegalArgumentException if <tt>numberBase</tt> is <tt>null</tt>.
	 * @see IndexBase#translate(Integer, IndexBase)
	 * @see #readPhrase()
	 */
	public PhraseReader(Reader in, IndexBase numberBase) {
		super(in);
		
		if(numberBase == null) {
			throw new IllegalArgumentException("numberBase is null");
		}
		
		this.numberBase = numberBase;
	}

	/**
	 * The target number base that new phrase IDs will have their line numbers translated into.
	 * @see #readPhrase()
	 */
	public IndexBase getNumberBase() {
		return numberBase;
	}
	
	/**
	 * Gets the number last assigned to a phrase, translated into the specified
	 * {@linkplain #getNumberBase() number base}. Note that this method may return a different result than
	 * {@link #getLineNumber()} due to the base translation.
	 * @see IndexBase#translate(Integer, IndexBase)
	 */
	public Integer getPhraseNumber() {
		return LINE_NUMBER_BASE.translate(getLineNumber(), numberBase);
	}

	/**
	 * Reads the next phrase from the underlying reader. The number is determined by translating the line number
	 * of the phrase into the {@linkplain #getNumberBase() target number base}. The words in the phrase are
	 * tokenized by the {@link Alignments#tokenize(String)} method.
	 * @return null if no phrases can be read from the underlying reader.
	 */
	public Phrase readPhrase() throws IOException {
		String ln = readLine();
		return (ln == null) ? null : new Phrase(getPhraseNumber(), Alignments.tokenize(ln));
	}
}
