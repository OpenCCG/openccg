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

import static opennlp.ccg.alignment.Alignments.DEFAULT_PHRASE_IDENTIFIER_ATTRIBUTE;
import static opennlp.ccg.alignment.Alignments.DEFAULT_PHRASE_NUMBER_BASE;
import static opennlp.ccg.alignment.Alignments.DEFAULT_PHRASE_TAG;
import static opennlp.ccg.alignment.Alignments.DEFAULT_WORD_DELIMITER;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Phrase reader for phrases that have an identifier in addition to a line number, usually formatted by
 * markup like
 * <blockquote><pre>
 * &lt;s snum=&quot;157&quot;&gt; ... &lt;/s&gt;
 * </pre></blockquote>
 * Since the value of the identifier is not necessarily an {@link Integer}, this class gives IDs as strings.
 * The {@linkplain #getPhraseIdentifierAttribute() phrase identifier attribute} (here <code>snum</code>) and
 * {@linkplain #getPhraseTag() phrase tag} (here <code>s</code>) are configurable when instances are
 * constructed.
 * 
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @see Alignments#DEFAULT_PHRASE_TAG
 * @see Alignments#DEFAULT_PHRASE_IDENTIFIER_ATTRIBUTE
 */
public class IdentifiedPhraseReader extends PhraseReader {

	static final String TAG_START = "<", TAG_END = ">";
	
	final String phraseTag, phraseIdentifierAttribute;
	
	private String lastId = null;
	
	/**
	 * Creates an identified phrase reader with the {@linkplain Alignments#DEFAULT_PHRASE_NUMBER_BASE default
	 * number base}.
	 * 
	 * @see #IdentifiedPhraseReader(Reader, IndexBase)
	 * @see Alignments#DEFAULT_PHRASE_TAG
	 * @see Alignments#DEFAULT_PHRASE_IDENTIFIER_ATTRIBUTE
	 */
	public IdentifiedPhraseReader(Reader in) {
		this(in, DEFAULT_PHRASE_NUMBER_BASE);		
	}
	
	/**
	 * Creates an identified phrase reader with the {@linkplain Alignments#DEFAULT_PHRASE_TAG default phrase tag}
	 * and {@link Alignments#DEFAULT_PHRASE_IDENTIFIER_ATTRIBUTE default phrase identifier attribute}.
	 * 
	 * @see #IdentifiedPhraseReader(Reader, IndexBase, String, String)
	 * @see Alignments#DEFAULT_PHRASE_TAG
	 * @see Alignments#DEFAULT_PHRASE_IDENTIFIER_ATTRIBUTE
	 */
	public IdentifiedPhraseReader(Reader in, IndexBase numberBase) {
		this(in, numberBase, DEFAULT_PHRASE_TAG, DEFAULT_PHRASE_IDENTIFIER_ATTRIBUTE);		
	}

	/**
	 * Creates an identified phrase reader for the specified phrase tag and identifier attribute.
	 * @param phraseTag The phrase tag that will be used to parse identifiers.
	 * @param phraseIdentifierAttribute The attribute that will denote identifiers when parsing phrases.
	 * @throws IllegalArgumentException if <tt>phraseIdentifierAttribute</tt> or <tt>phraseTag</tt> 
	 * is <tt>null</tt>. 
	 */
	public IdentifiedPhraseReader(Reader in, IndexBase numberBase, String phraseTag,
			String phraseIdentifierAttribute) {
		super(in, numberBase);
		
		if(phraseIdentifierAttribute == null) {
			throw new IllegalArgumentException("phraseIdentifierAttribute is null");
		}
		if(phraseTag == null) {
			throw new IllegalArgumentException("phraseTag is null");
		}
		
		this.phraseIdentifierAttribute = phraseIdentifierAttribute;
		this.phraseTag = phraseTag;
	}

	/**
	 * Gets the last ID encountered.
	 */
	public String getLastId() {
		return lastId;
	}

	/**
	 * Gets the tag that denotes a phrase.
	 */
	public String getPhraseTag() {
		return phraseTag;
	}

	/**
	 * Gets the attribute name that denotes a phrase identifier.
	 */
	public String getPhraseIdentifierAttribute() {
		return phraseIdentifierAttribute;
	}

	/**
	 * Reads a phrase from the underlying input stream, first parsing the {@linkplain Phrase#getNumber()
	 * phrase's number} based on the {@linkplain #getPhraseTag() phrase tag} and
	 * {@linkplain #getPhraseIdentifierAttribute() identifier attribute} being used.
	 * @return A phrase with the ID signaled in the input.
	 * @throws IOException If the underlying input contains ill-formated phrase markup, or if no ID cannot be
	 * determined after parsing the phrase markup.
	 */
	@Override
	public Phrase readPhrase() throws IOException {
		String ln = readLine();
		if(ln == null) {
			return null;
		}
		
		String[] chunks = DEFAULT_WORD_DELIMITER.split(ln);
		String c = chunks[0];
		
		if(!c.trim().startsWith(TAG_START)) {
			throw new IOException("unable to parse: " + ln + "; expected <, but was " + c.trim());
		}
		
		int clen = c.length();
		int pos = (clen > 1) ? 1 : 2;
		String t = (pos == 1) ? c.substring(1).trim() : chunks[1];
		if(!t.equals(phraseTag)) {
			throw new IOException("expected sequence tag " + phraseTag + ", but was " + t);
		}
		
		boolean foundIndex = false;
		int start = -1;
		
		for(int i = pos; i < chunks.length; i++) {
			if(!foundIndex) {
				String[] subchunks = chunks[i].trim().split("=");
				if(subchunks.length > 1) {
					if(subchunks[0].equals(phraseIdentifierAttribute)) {
						char[] idVal = subchunks[1].toCharArray();
						
						int idStart = 0, idEnd = idVal.length - 1;
						boolean foundStart = false;
						for(int j = 0; j < idVal.length; j++) {
							if(idVal[j] == '\'' || idVal[j] == '\"') {
								if(foundStart) {
									idEnd = j;
									break;
								}
								else {
									foundStart = true;
									idStart = j + 1;
								}
							}
						}
						
						lastId = new String(idVal).substring(idStart, idEnd);
						foundIndex = true;
					}
				}
			}
			
			if(chunks[i].contains(TAG_END)) {
				start = i;
				break;
			}
		}
		
		if(!foundIndex) {
			throw new IOException("no ID found on line " + getLineNumber());
		}
		
		List<String> l = new ArrayList<String>(chunks.length);
		for(int j = start; j < chunks.length; j++) {
			String cj = chunks[j];
			
			if(j == start) {
				int te = cj.indexOf(TAG_END);
				if(te != -1) {
					cj = cj.substring(te + 1);
				}
			}
			
			if(j + 1 == chunks.length) {
				int ts = cj.indexOf(TAG_START);
				if(ts != -1) {
					cj = cj.substring(0, ts);
				}
			}
			
			if(cj.length() > 0) {
				l.add(cj);
			}
		}
		
		return new Phrase(lastId, getPhraseNumber(), l);
	}
}
