///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2010 Dennis N. Mehay
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
package opennlp.ccg.parse.postagger.ml;

import opennlp.ccg.parse.supertagger.util.ProbPairComparator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.ccg.lexicon.DefaultTokenizer;
import opennlp.ccg.lexicon.Word;
import opennlp.ccg.ngrams.ConditionalProbabilityTable;
import opennlp.ccg.parse.tagger.io.SRILMFactoredBundleCorpusIterator;
import opennlp.ccg.util.Interner;
import opennlp.ccg.util.Pair;

/**
 * (c) (2009) Dennis N. Mehay
 * @author Dennis N. Mehay
 * 
 * Model for predicting p(POS | word).  Uses an ARPA-formatted
 * SRILM-trained "unigram" factored LM for this, where each "unigram" is
 * a bundle of word:pos.
 */
public class POSPriorModel extends ConditionalProbabilityTable {

    public static final String WORD = DefaultTokenizer.WORD_ATTR;
    public static final String POS_TAG = DefaultTokenizer.POS_ATTR;
    private Interner<Pair<String, String>> pairs = new Interner<Pair<String, String>>();
    /** 
     * Re-usable list for attr-val pairs of word-pos-supertag inputs to the prior model
     * (i.e., for predicting p(STag | word, POS). 
     */
    public List<Pair<String, String>> attrVals = new ArrayList<Pair<String, String>>(5);
    /** 
     * A comparator for sorting Pair<Double,String>'s where the Double is a probability
     * (effectively sorts by descending order of probability).
     */
    private ProbPairComparator ppcomp = new ProbPairComparator();
    /** All the priors. Reference them when getting beta-best, beta-worst, etc. */
    List<Pair<Double, String>> priors = new ArrayList<Pair<Double, String>>(1000);

    /** String[] of all possible POS outcomes. */
    private String[] posVocab = null;

    /** Construct a prior model with the FLM config file and corresponding vocab file. */
    public POSPriorModel(String flmFile, String vocabFile) throws IOException {
        super(flmFile);
        String post = null;
        BufferedReader br = new BufferedReader(new FileReader(new File(vocabFile)));
        post = br.readLine().trim();

        // get next POS tag from the vocab.
        while ((post != null) && !post.trim().startsWith(POS_TAG + "-")) {
            post = br.readLine();
        }
        if (post != null) {
            post = post.trim().split("-")[1];
        }

        Collection<String> allSupertags = new HashSet<String>();

        // find out how many outcomes we have.
        int cnt = 0;
        while (post != null) {
            cnt++;
            allSupertags.add(post);
            while ((post != null) && !post.trim().startsWith(POS_TAG + "-")) {
                post = br.readLine();
            }
            if (post != null) {
                post = post.trim().split("-")[1];
            }
        }

        // initialize the arrays to this size.
        posVocab = new String[cnt];
        

        cnt = 0;
        // fill the vocab array with all possible POS tags.
        for (String posTag : allSupertags) {
            posVocab[cnt++] = posTag.intern();
        }
        br.close();
    }

    /** Get the prior probability of this POS/word combo. */
    public double getPriorOf(String pos, String word) {
        attrVals.clear();
        Pair<String, String> surfaceForm = pairs.intern(new Pair<String, String>(WORD, word.intern()));
        attrVals.add(surfaceForm);
        Pair<String, String> partOfSpeech = pairs.intern(new Pair<String, String>(POS_TAG, pos.intern()));
        attrVals.add(partOfSpeech);
        return score(attrVals);
    }

    /** Get the POS-dict restricted prior distribution (sorted descending by prob.) */
    public List<Pair<Double, String>> getPriors(Word w) {
        List<Pair<Double, String>> sortedTags = new ArrayList<Pair<Double, String>>(posVocab.length);
        for (String postag : posVocab) {
            sortedTags.add(new Pair<Double, String>(getPriorOf(postag, w.getForm()), postag));
        }
        Collections.sort(sortedTags, ppcomp);
        return sortedTags;
    }
    /* added by DCE, to facilitate use in hypertagging
     * Identical to above method, but accepts a String (name of EP) rather than
     * a Word object.
     */
    public List<Pair<Double, String>> getPriors(String s) {
    	s.intern();
    	List<Pair<Double, String>> sortedTags = new ArrayList<Pair<Double, String>>(posVocab.length);
      for (String postag : posVocab) {
          sortedTags.add(new Pair<Double, String>(getPriorOf(postag, s), postag));
      }
      Collections.sort(sortedTags, ppcomp);
      return sortedTags;
    }
    
    public static void main(String[] args) throws IOException {
        String usage = "\nPOSPriorModel -vocab <vocabfile> (-c <corpus> [default = <stdin>]) (-o <output> [default = <stdout>])\n";
        if (args.length > 0 && args[0].equals("-h")) {
            System.out.println(usage);
            System.exit(0);
        }

        SRILMFactoredBundleCorpusIterator in = null;
        BufferedWriter out = null;
        BufferedWriter voc = null;

        try {
            String inputCorp = "<stdin>", output = "<stdout>", vocabFile = "vocab.voc";

            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-c")) { inputCorp = args[++i]; continue; }
                if (args[i].equals("-o")) { output = args[++i];    continue; }
                if (args[i].equals("-vocab")) { vocabFile = args[++i];  continue; }
                System.out.println("Unrecognized option: " + args[i]);
            }

            try {
                in = new SRILMFactoredBundleCorpusIterator(
                        (inputCorp.equals("<stdin>")) ? new BufferedReader(new InputStreamReader(System.in)) : new BufferedReader(new FileReader(new File(inputCorp))));
            } catch (FileNotFoundException ex) {
                System.err.print("Input corpus " + inputCorp + " not found.  Exiting...");
                Logger.getLogger(POSPriorModel.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(
                        -1);
            }

            try {
                out = (output.equals("<stdout>")) ? new BufferedWriter(new OutputStreamWriter(System.out)) : new BufferedWriter(new FileWriter(new File(output)));
            } catch (IOException ex) {
                System.err.print("Output file " + output + " not found.  Exiting...");
                Logger.getLogger(POSPriorModel.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(
                        -1);
            }

            try {
                voc = new BufferedWriter(new FileWriter(new File(vocabFile)));
            } catch (IOException ex) {
                Logger.getLogger(POSPriorModel.class.getName()).log(Level.SEVERE, null, ex);
            }

            Map<String, Integer> vocab = new HashMap<String, Integer>();
            for (List<Word> inLine : in) {
                for (Word w : inLine) {
                    String pos = POS_TAG + "-" + DefaultTokenizer.escape(w.getPOS()),
                        wform = WORD + "-" + DefaultTokenizer.escape(w.getForm());

                    vocab.put(pos, (vocab.get(pos) == null) ? 1 : vocab.get(pos) + 1);
                    vocab.put(wform, (vocab.get(wform) == null) ? 1 : vocab.get(wform) + 1);
                    out.write(wform + ":" + pos + " ");
                }
                out.write(System.getProperty("line.separator"));
            }
            out.flush();

            for (String str : vocab.keySet()) {
                    voc.write(str + System.getProperty("line.separator"));
            }
            voc.flush();
        } finally {
            try {
                out.close();
                in.close();
                voc.close();
            } catch (IOException ex) {
                Logger.getLogger(POSPriorModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
