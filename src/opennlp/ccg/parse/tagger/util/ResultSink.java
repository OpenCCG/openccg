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
package opennlp.ccg.parse.tagger.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import opennlp.ccg.lexicon.Word;
import opennlp.ccg.util.Pair;

/**
 * Inspired (loosely, based on my recollection) by Jason Baldridge's 
 * similar class for tracking classifier performance.
 * Here we simply track the <code>Word</code>-by-<code>Word</code>
 * tagging performance of a CCG supertagger by passing in a multitagging
 * and a gold-standard answer and tabulating the results.  The results
 * are reported by a custom <code>report</code> method, which returns
 * a <code>String</code> representation of the results.
 * 
 * @author Dennis N. Mehay
 * @version $Revision: 1.1 $, $Date: 2010/09/21 04:12:42 $
 */
public class ResultSink {

    public static enum ResultSinkType { SUPERTAG, POSTAG };
    
    private int totalTags = 0,  totalWords = 0,  totalRight = 0;
    // for keeping pos-specific stats.
    private Map<String, Integer> posToRight = new HashMap<String, Integer>(),  posTot = new HashMap<String, Integer>();
    // for tracking the total number of sentences, number totally tagged
    // correctly, etc.
    private int sentNum = 0,  sentsCorrect = 0;
    private boolean allCorrect = true;
    // what type of tag are we tracking the results over?
    private ResultSinkType whatType;
    // for general pos-specific stats (e.g., N... -> <stats>, not NNP -> <stats> and NNPS -> <stats>, etc.)
    private Map<String, Integer> genPOSToRight = new HashMap<String, Integer>(),  genPOSTot = new HashMap<String, Integer>();

    /** 
     * Nullary constructor.  Defaults to supertag result sink.
     * (TODO: add log file logging for more detailed error reporting.)
     */
    public ResultSink() {
        this(ResultSinkType.SUPERTAG);
    }

    public ResultSink(ResultSinkType whatType) {
        this.whatType = whatType;
    }

    /** 
     * Add and store a sentence of tagged words (<code>List<List<Pair<Double,String>>></code>)
     * wrt a gold-standard tagged word. 
     */
    public void addSent(List<List<Pair<Double, String>>> sent, List<Word> goldTagging) {
        sentNum++;
        allCorrect = true;
        Iterator<Word> gold = goldTagging.iterator();
        for (List<Pair<Double, String>> tgging : sent) {
            addResult(tgging, gold.next());
        }
        if (allCorrect) {
            sentsCorrect++;
        }
    }

    /**
     * Add a single-word tagging result alongside its gold-standard tagging.
     * Compare and log whether the gold-standard tag is in the beta-best (also
     * log pos-specific error stats).
     */
    public void addResult(List<Pair<Double, String>> tagging, Word goldTagging) {
        String goldTag = (whatType == ResultSinkType.SUPERTAG) ? goldTagging.getSupertag() : goldTagging.getPOS();
        totalTags += tagging.size();
        totalWords++;
        // mww: check for missing gold POS (grrr)
        if (goldTagging.getPOS() == null) {
            System.err.println("Warning: found null gold POS, skipping word: " + goldTagging);
            this.allCorrect = false;
            return;
        }

        String thisPOS = goldTagging.getPOS(), thisGenPOS = goldTagging.getPOS().substring(0, 1);
        Integer posT = this.posTot.get(thisPOS), gPOST = this.genPOSTot.get(thisGenPOS);
        if (posT == null) {
            this.posTot.put(thisPOS, new Integer(1));
        } else {
            this.posTot.put(thisPOS, new Integer(posT.intValue() + 1));
        }
        if (gPOST == null) {
            this.genPOSTot.put(thisGenPOS, new Integer(1));
        } else {
            this.genPOSTot.put(thisGenPOS, new Integer(gPOST.intValue() + 1));
        }
        // assume this tagging is incorrect, until proven otherwise.
        boolean gotIt = false;
        for (Pair<Double, String> tag : tagging) {
            if (tag.b.equals(goldTag)) {
                gotIt = true;
                totalRight++;
                // add one both to the pos right and total for that pos type.
                Integer posLkup = this.posToRight.get(thisPOS),
                        genPOSLkup = this.genPOSToRight.get(thisGenPOS);
                if (posLkup == null) {
                    this.posToRight.put(thisPOS, new Integer(1));
                } else {
                    this.posToRight.put(thisPOS, new Integer(posLkup.intValue() + 1));
                }
                if (genPOSLkup == null) {
                    this.genPOSToRight.put(thisGenPOS, new Integer(1));
                } else {
                    this.genPOSToRight.put(thisGenPOS, new Integer(genPOSLkup.intValue() + 1));
                }
                break;
            }
        }
        // mistagged this one word, so tagging the whole sentence correctly -- 
        // allCorrect==true -- is not possible. 
        if (!gotIt) {
            this.allCorrect = false;
        }

    }

    public String report() {
        // make sure 0 counts are inserted for POS types that were never got right.
        for (String post : this.posTot.keySet()) {
            if (this.posToRight.get(post) == null) {
                this.posToRight.put(post, new Integer(0));
            }
        }
        for (String post : this.genPOSTot.keySet()) {
            if (this.genPOSToRight.get(post) == null) {
                this.genPOSToRight.put(post, new Integer(0));
            }
        }

        String rep = "";
        rep += "\n\nAccuracy by POS type:\n\n";
        for (String post : this.posTot.keySet()) {
            rep += post + ": " + ((this.posToRight.get(post).intValue() + 0.0) / (this.posTot.get(post))) +
                    " <==> " + this.posToRight.get(post).intValue() + "/" + (this.posTot.get(post)) + " = " +
                    (100 * ((this.posTot.get(post) - this.posToRight.get(post) + 0.0) / (totalWords - totalRight))) + " (% of total errors) \n";
        }
        rep += "\nAccuracy by general (truncated) POS type:\n\n";
        for (String post : this.genPOSTot.keySet()) {
            rep += post + ": " + (this.genPOSToRight.get(post).intValue() + 0.0) / (this.genPOSTot.get(post)) +
                    " <==> " + this.genPOSToRight.get(post).intValue() + "/" + (this.genPOSTot.get(post)) + " = " +
                    (100 * ((this.genPOSTot.get(post) - this.genPOSToRight.get(post) + 0.0) / (totalWords - totalRight))) + " (% of total errors) \n";
        }
        rep += "\nTotal words: " + totalWords +
                "\nTotal sents: " + this.sentNum +
                "\nAggregate total tags: " + totalTags +
                "\nAve. tags/word: " + ((totalTags + 0.0) / (totalWords + 0.0)) +
                "\nWord accuracy: " + ((totalRight + 0.0) / totalWords) + "\n" +
                "\nSent accuracy: " + ((this.sentsCorrect + 0.0) / (this.sentNum)) + "\n\n";
        return rep;
    }
}