///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2011 Dennis N. Mehay
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

package opennlp.ccg.lexicon;

/**
 * A simple class that is constructed with a file containing a list of words
 * that are all and only those found (presumably from a large corpus) to be
 * more frequently upper-cased.  Any word-string passed to it (that is also
 * not a named entity string) is queried in this list.  If the word is there
 * it is restored to the casing found in the list, if it is not, it is converted
 * to lower-case.
 *
 * There is an option, skipAlreadyLower, to skip any word that is already in lower case, 
 * so as to avoid mistakenly uppercasing words.  This option is enabled by default.  
 * 
 * @author      Dennis N. Mehay
 *
 */

import java.util.*;
import java.io.*;
import java.nio.charset.Charset;
import opennlp.ccg.lexicon.Word;
import opennlp.ccg.lexicon.DefaultTokenizer;
import java.util.zip.GZIPInputStream;

public class TrueCaser {

    /** Flag for skipping words already in lower case (enabled by default). */
    public boolean skipAlreadyLower = true;
    
    /**
     * A map from lower-cased keys to the true-cased forms (from the list passed in during construction).
     */
    private Map<String,String> caseMap = new HashMap<String,String>();

    /**
     * For the heuristics that determine whether something is in title case: what percentage of the sentence must be
     * cased to make the title-case detector go off?
     */
    private double titleCaseThreshold;

    /**
     * Constructor that just takes a path to a list of words that are only upper-case (title-case is default = 0.9).
     */
    public TrueCaser(String pathToOnlyUCWords) {
    	this(pathToOnlyUCWords, 0.5);
    }

    /**
     * Constructor that takes a path to a list of words that are only upper-case and a title-case threshold.
     */
    public TrueCaser(String pathToOnlyUCWords, double titleCaseThreshold) {
		this.titleCaseThreshold = titleCaseThreshold;
		this.caseMap = TrueCaser.readInCaseMap(pathToOnlyUCWords);
		if (this.caseMap == null) {
		    System.err.println("Something went wrong."+System.getProperty("line.separator")+
				       "Make sure you passed in a file of true-cased words, etc.");
		    System.exit(-1);
		} else {
		    //System.err.println("Reading in list of true-cased words: "+pathToOnlyUCWords+System.getProperty("line.separator")+
		    //		       " at a title-case heursitic detection threshold of: "+titleCaseThreshold);
		}
    }

    /**
     * Static method to read in the list of words.
     */ 
    public static Map<String,String> readInCaseMap(String pathToOnlyUCWords) {
		Map<String,String> caseMap = new HashMap<String,String>();
		BufferedReader inRead = null;
		try {
		    Charset utf8 = Charset.availableCharsets().get("UTF-8");
	
		    inRead = new BufferedReader(
						(pathToOnlyUCWords.toLowerCase().endsWith(".gz")) ?
						(new InputStreamReader(new GZIPInputStream(new FileInputStream(new File(pathToOnlyUCWords))), utf8)) :
						(new InputStreamReader(new FileInputStream(new File(pathToOnlyUCWords)), utf8))
						);
	
		    String ln = inRead.readLine();
		    while(ln != null) {
			ln = ln.trim();
			if(!ln.startsWith("#")) {
			    caseMap.put(ln.toLowerCase(), ln);
			}
				ln = inRead.readLine();
		    }
		} catch (IOException ioe) { 
		    return null;
		} finally {
		    try {
		    	inRead.close();
		    } catch (Exception e) {
		    	// do nothing.
		    }
		}
		return caseMap;
    }

    /**
     * Truecase a candidate word. If the word is in the list of more commonly
     * cased words, then return this cased form.  If not, normalize to lowercase if
     * this is the first word or the sentence is in titlecase. Else return the word as-is.
     * If skipAlreadyLower is enabled, skip the word if it is already in lower case.
     */ 
    public String trueCase(String theWord, boolean isTitle, boolean isFirstWord) {
		String loweredWord = theWord.toLowerCase();
		// skip word in lower case per flag
		if (skipAlreadyLower && theWord.equals(loweredWord))
			return theWord;
		// look up the truecased version; if not there, and title-case or first
		// word in sentence, lowercase it, otherwise, don't touch it.
		String trueCasedTheWord = caseMap.get(loweredWord);
		if (trueCasedTheWord != null) {
			return trueCasedTheWord;
		} else {
			return (isTitle || isFirstWord) ? loweredWord : theWord;
		}
    }

    /**
     * Truecase a whole sentence.  If the sentence appears to be in title-case (as determined by a heuristic
     * that is triggered by there being greater than 0.X of the first 10 words, if there are that many,
     * being cased) normalize any word that is not in the true-case list to lower-case.  Otherwise, leave all
     * words that are not in the true-case list alone, except the first word (which is normalized to lower-case)
     * The title-case threshold is a creation-time parameter. 
     * As always, if skipAlreadyLower is enabled, the word is skipped if it is already in lower case.  Note that 
     * the second word is counted as the first word if the first token is a left quote (single or double).
     */
    public String trueCaseSentence(String sentence) { 
		String[] parts = sentence.split("\\s+");
		StringBuffer res = new StringBuffer(parts.length);

		boolean isTitle = isTitleCased(parts);
		// truecase the whole sentence (only normalizing by lowercasing if
		// titlecase detector went off).
		int i = 0;
		for (String prt : parts) {
			boolean isFirstWord = (i == 0 || (i == 1 && (parts[0].equals("``") || parts[0].equals("`"))));
			res.append(" " + trueCase(prt, isTitle, isFirstWord));
			i++;
		}
		return res.toString().trim();
    }

    /**
     * Returns true iff the percentage of the first 10 words (or the whole sentence if it's less than 10 words) 
     * that have a case distinction is greater than or equal to 'titleCaseThreshold'.
     */
    public boolean isTitleCased(String[] words) {
		int numCased = 0, cursor = 0;
		for (String wd : words) {
			if (cursor >= 10) {
				break;
			}
			if (!wd.toLowerCase().equals(wd)) {
				numCased++;
			}
			cursor++;
		}
		return (numCased / ((words.length < 10) ? (words.length + 0.0) : 10.0) >= titleCaseThreshold);
    }

    public String tcWordToString(String newWordForm, Word oldWord) {
        StringBuffer sb = new StringBuffer();
        sb.append(newWordForm);
        if (oldWord.getPOS() != null) sb.append(":P-").append(DefaultTokenizer.escape(oldWord.getPOS()));
        if (oldWord.getSupertag() != null) sb.append(":T-").append(DefaultTokenizer.escape(oldWord.getSupertag()));
        if (oldWord.getSemClass() != null) sb.append(":C-").append(DefaultTokenizer.escape(oldWord.getSemClass()));
        if (sb.length() == 0) sb.append((String) null);
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
    	String newline = System.getProperty("line.separator");
    	String usage = 
    			newline + 
    			"java TrueCaser -t <truecase-file> -r <title-threshold> -i <input [default=stdin]> -o <output [default=stdout]>" 
    			+ newline;
    	if(args.length == 0 || args[0] .equals("h") || args[0] .equals("-h") || args[0] .equals("--h") 
    			|| args[0] .equals("--help") || args[0] .equals("-help")) {
		    System.err.println(usage);
		    System.exit(0);
		}

		String truecasefile = null, inputfile = null, outputfile = null;
		double tcThresh = 0.5;
		for (int a = 0; a < args.length; a++) {
			if (args[a].equals("-t")) {
				truecasefile = args[++a];
				continue;
			}
			if (args[a].equals("-r")) {
				tcThresh = Double.parseDouble(args[++a]);
				continue;
			}
			if (args[a].equals("-i")) {
				inputfile = args[++a];
				continue;
			}
			if (args[a].equals("-o")) {
				outputfile = args[++a];
				continue;
			}

			System.err.println("unrecognized option " + args[a] + ".");
			System.err.println(usage);
			System.exit(0);
		}    
		
		Charset utf8 = Charset.availableCharsets().get("UTF-8");

		// input of text (assumed to be tokenized utf-8-encoded text).
		BufferedReader in = new BufferedReader(new InputStreamReader(
				((inputfile == null) ? System.in : (new FileInputStream(
						new File(inputfile)))), utf8));

		// output stream (back to tokenized utf-8-encoded text).
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				((outputfile == null) ? System.out : (new FileOutputStream(
						new File(outputfile)))), utf8));

		// file of true-cased words is arg0.
		TrueCaser tc = new TrueCaser(truecasefile, tcThresh);

		String sent = in.readLine();
		while (sent != null) {
			sent = tc.trueCaseSentence(sent.trim());
			out.write(sent + System.getProperty("line.separator"));
			sent = in.readLine();
		}
		out.close();
		in.close();
	}
}