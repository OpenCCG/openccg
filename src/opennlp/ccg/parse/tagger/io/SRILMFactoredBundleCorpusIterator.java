///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2009 Dennis N. Mehay
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
package opennlp.ccg.parse.tagger.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import opennlp.ccg.lexicon.DefaultTokenizer;
import opennlp.ccg.lexicon.Tokenizer;
import opennlp.ccg.lexicon.Word;

/**
 * Assuming an input file of n different sentences of the form:
 * <s> wordbundle1 wordbundle2 ... wordbundleM </s>
 * ... [n-2 lines]
 * <s> wordbundle1 ... wordbundleQ </s>
 * 
 * where the 'wordbundle's are SRILM factored LM-compliant
 * bundles of factors (wordform, lemma, POS, supertag, semantic class, etc.).
 * 
 * We assume one sentence per line, so the <s> ... </s> bracketing is just
 * a formality (what SRILM expects).
 * 
 * @author Dennis N. Mehay
 * @version $Revision: 1.2 $, $Date: 2010/09/26 05:50:15 $
 */
public class SRILMFactoredBundleCorpusIterator implements CorpusIterator, Iterator<List<Word>>, Iterable<List<Word>> {

    private BufferedReader reader;
    private String nextLine,  nextID;
    public static final String SENT_START = "<s>",  SENT_END = "</s>";
    private Tokenizer toker = new DefaultTokenizer();

    /** Creates a new instance of SRILMFactoredBundleCorpusIterator */
    public SRILMFactoredBundleCorpusIterator(BufferedReader file) {
        try {
            this.reader = file;
            String line = this.reader.readLine();
            if (line != null && line.length() > 0) {
                line = line.trim();
                this.nextLine = line;
            } else {
                this.nextLine = this.nextID = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The client of this method is responsible for checking that there
     * is in fact a next line (by calling <code>hasNext</code> before 
     * calling this method.
     * 
     * @return A <code>String</code> representing the next line in the 
     *         file.
     * @throws <code>java.io.IOException</code>. 
     */
    public List<Word> next() {
        List<Word> currentSent = null;
        try {
            if (this.hasNext()) {
                String line = this.reader.readLine();
                //while(line != null && (line.length()==0 || line.trim().equals(""))) {
                //    line = this.reader.readLine();
                //}
                if (line != null) {
                    line = line.trim();
                }
                if (this.nextLine.endsWith(SENT_END)) {
                    currentSent = toker.tokenize(this.nextLine.substring(this.nextLine.indexOf(">") + 1, this.nextLine.lastIndexOf("<")).trim());
                } else {
                    currentSent = toker.tokenize(this.nextLine.substring(this.nextLine.indexOf(">") + 1).trim());
                }

                if (line != null && !(line.trim().equals(""))) {
                    this.nextLine = line;
                } else {
                    this.nextLine = this.nextID = null;
                }
            } else {
                throw new IOException("There is no next line.");
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                    ex.getMessage(), ex);
        }
        return currentSent;
    }

    /**
     * @return A <code>String</code> representing the current parse ID
     *         (a la CCGbank).
     */
    public String getCurrentID() {
        return this.nextID;
    }

    /**
     * @return A <code>boolean</code> as to whether there is a next line
     *         in the file. 
     */
    public boolean hasNext() {
        return this.nextLine != null;
    }

    /**
     * Closes the underlying <code>BufferedReader</code>.
     * 
     */
    public void close() {
        try {
            this.reader.close();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                    ex.getMessage(), ex);
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Iterator<List<Word>> iterator() {
        return this;
    }
}
