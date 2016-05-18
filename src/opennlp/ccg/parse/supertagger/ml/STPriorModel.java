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
package opennlp.ccg.parse.supertagger.ml;

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
import java.util.Arrays;
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
import opennlp.ccg.parse.tagger.ProbIndexPair;
import opennlp.ccg.parse.supertagger.util.STTaggerPOSDictionary;
import opennlp.ccg.util.Interner;
import opennlp.ccg.util.Pair;

/**
 * (c) (2009) Dennis N. Mehay
 * @author Dennis N. Mehay
 * 
 * Model for predicting p(supertag | word, pos).  Uses an ARPA-formatted
 * SRILM-trained "unigram" factored LM for this, where each "unigram" is
 * a bundle of word:pos:supertag.
 */
public class STPriorModel extends ConditionalProbabilityTable {

    public static final String WORD = DefaultTokenizer.WORD_ATTR;
    public static final String POS_TAG = DefaultTokenizer.POS_ATTR;
    public static final String SUPERTAG = DefaultTokenizer.SUPERTAG_ATTR;
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
    /** String[] of all possible supertag outcomes. */
    private String[] stagVocab = null;
    /** double[] containing the probability distro over all supertags. */
    private double[] stagDistro = null;
    /** 
     * POS-keyed tagging dictionary (to provide restrictions on what the prior model may consider.
     * No restrictions if null.
     */
    private STTaggerPOSDictionary posDict = null;
    /** 
     * Re-usable way of containing the probabilities and a pointer back into where they came from
     * in the probability distro over all supertags. 
     */
    private ProbIndexPair[] stagPointers = null;

    /** Construct a prior model with the FLM config file and corresponding vocab file. */
    public STPriorModel(String flmFile, String vocabFile) throws IOException {
        // create with a null POS dictionary (i.e., no restrictions on taggings).
        this(flmFile, vocabFile, null);
    }

    /** Construct a prior model with the FLM config file and corresponding vocab file. */
    public STPriorModel(String flmFile, String vocabFile, STTaggerPOSDictionary posDict) throws IOException {
        super(flmFile);
        this.posDict = posDict;

        String st = null;
        BufferedReader br = new BufferedReader(new FileReader(new File(vocabFile)));
        st = br.readLine().trim();

        // get next supertag from the vocab.
        while ((st != null) && !st.trim().startsWith(SUPERTAG + "-")) {
            st = br.readLine();
        }
        if (st != null) {
            st = st.trim().split("-")[1];
        }

        Collection<String> allSupertags = new HashSet<String>();

        // find out how many outcomes we have.
        int cnt = 0;
        while (st != null) {
            cnt++;
            allSupertags.add(st);
            while ((st != null) && !st.trim().startsWith(SUPERTAG + "-")) {
                st = br.readLine();
            }
            if (st != null) {
                st = st.trim().split("-")[1];
            }
        }
        br.close();

        // initialize the arrays to this size.
        stagVocab = new String[cnt];
        stagPointers = new ProbIndexPair[cnt];
        stagDistro = new double[cnt];

        cnt = 0;
        // fill the vocab array with all possible supertags.
        for (String stag : allSupertags) {
            stagVocab[cnt++] = stag.intern();
        }
    }

    /** Set the POS-keyed tagging dictionary. */
    public void setPOSDict(STTaggerPOSDictionary posDict) {
        this.posDict = posDict;
    }

    /** Get the prior probability of this supertag/POS/word combo. */
    public double getPriorOf(String supertag, String word, String pos) {
        attrVals.clear();
        Pair<String, String> surfaceForm = pairs.intern(new Pair<String, String>(WORD, DefaultTokenizer.escape(word).intern()));
        attrVals.add(surfaceForm);
        Pair<String, String> partOfSpeech = pairs.intern(new Pair<String, String>(POS_TAG, DefaultTokenizer.escape(pos).intern()));
        attrVals.add(partOfSpeech);
        attrVals.add(pairs.intern(new Pair<String, String>(SUPERTAG, DefaultTokenizer.escape(supertag).intern())));
        return score(attrVals);
    }

    /** Get the beta-best tags for this word, under the prior model. */
    public List<Pair<String, Double>> getBetaBestPriors(Word w, double beta) {
        List<Pair<String, Double>> allPriors = getAllPriors(w);
        List<Pair<String, Double>> betaBestPriors = new ArrayList<Pair<String, Double>>(100);
        double best = allPriors.get(0).b;
        for (Pair<String, Double> prior : allPriors) {
            if (best * beta <= prior.b) {
                betaBestPriors.add(prior);
            } else {
                break;
            }
        }
        return betaBestPriors;
    }

    /** Compute all priors, subject to the POS dict constraints. */
    public void computePriors(Word w) {
        if (posDict != null) {
            priors = getPOSRestrictedPriors(w);
        }
    }

    /** Get the POS-dict restricted prior distribution (sorted descending by prob.) */
    protected List<Pair<Double, String>> getPOSRestrictedPriors(Word w) {
        Collection<String> tagsAllowed = posDict.getEntry(w.getPOS());
        if (tagsAllowed == null || tagsAllowed.size() == 0) {
            return priors;
        } else {
            List<Pair<Double, String>> sortedTags = new ArrayList<Pair<Double, String>>(tagsAllowed.size());
            for (String tag : tagsAllowed) {
                sortedTags.add(new Pair<Double, String>(getPriorOf(tag, w.getForm(), w.getPOS()), tag));
            }
            Collections.sort(sortedTags, ppcomp);
            return sortedTags;
        }
    }

    /** 
     * Get the beta-best tags (using the prior model) only from among the POS-dictionary-allowed possibilities. 
     * beta-best (def'n): {t | p(t) >= beta * p(best-tag) }
     */
    public List<Pair<String, Double>> getRestrictedBetaBestPriors(Word w, double beta) {
        if (posDict == null) {
            return getBetaBestPriors(w, beta);
        } else {
            List<Pair<String,Double>> rez = new ArrayList<Pair<String,Double>>(50);
            double best = priors.get(0).a;
            for(Pair<Double,String> tg : priors) {
                if(tg.a >= (beta * best)) {
                    rez.add(new Pair<String,Double>(tg.b,tg.a));
                } else {
                    break;
                }
            }
            return rez;
        }
    }
    
    /** 
     *  Get the beta-WORST tags (using the prior model) only from among the POS-dictionary-allowed possibilities. 
     *  beta-best (def'n): {t | p(t) >= beta * p(best-tag) }
     *  beta-worst (def'n): {t | p(t) * beta <= p(worst-tag)}
     */
    public List<Pair<String, Double>> getRestrictedBetaWorstPriors(Word w, double beta) {
        if (posDict == null) {
            throw new UnsupportedOperationException("Cannot get beta-worst without a pos-keyed tagging dict.\nNot yet implemented.");
        } else {
            List<Pair<String,Double>> rez = new ArrayList<Pair<String,Double>>(50);
            List<Pair<Double,String>> cpy = new ArrayList<Pair<Double,String>>(priors);
            Collections.reverse(cpy);            
            double worst = cpy.get(0).a;
            for(Pair<Double,String> tg : cpy) {
                if((tg.a * beta) <= worst) {
                    rez.add(new Pair<String,Double>(tg.b,tg.a));
                } else {
                    break;
                }
            }
            return rez;
        }
    }

    public List<Pair<String, Double>> getAllPriors(Word w) {
        return getNBestPriors(w, stagVocab.length);
    }

    /** Get the n-best supertags on the prior model, given this word (with POS). */
    public List<Pair<String, Double>> getNBestPriors(Word w, int n) {
        attrVals.clear();
        Pair<String, String> surfaceForm = pairs.intern(new Pair<String, String>(WORD, DefaultTokenizer.escape(w.getForm()).intern()));
        attrVals.add(surfaceForm);
        Pair<String, String> pos = pairs.intern(new Pair<String, String>(POS_TAG, DefaultTokenizer.escape(w.getPOS()).intern()));
        attrVals.add(pos);

        int cnt = 0;
        for (String st : stagVocab) {
            // remove the last stag factor, if there.
            if (attrVals.size() == 3) {
                attrVals.remove(attrVals.size() - 1);
            }

            attrVals.add(pairs.intern(new Pair<String, String>(SUPERTAG, st)));
            // add the probability of this tag under the prior model to the distro array.
            double sc = score(attrVals);
            stagDistro[cnt] = sc;
            // add this probability with a pointer back to where it came from in the vocab.
            // (so that we can sort by probability, but then retrieve the supertag string).
            stagPointers[cnt] = new ProbIndexPair(sc, cnt);
            cnt++;

        }
        // sort descending by probability (achieved by the comparator implementation of ProbIndexPair).

        Arrays.sort(stagPointers);

        List<Pair<String, Double>> result = new ArrayList<Pair<String, Double>>(n);
        for (int i = 0; i <
                n; i++) {
            result.add(new Pair<String, Double>(stagVocab[stagPointers[i].b], stagPointers[i].a));
        }

        return result;
    }

    public static void main(String[] args) throws IOException {
        String usage = "\nSTPriorModel -vocab <vocabfile> (-c <corpus>) (-o <output>) (-u <catFreqCutoff> ) (-v [ or '-verbose'])\n";
        if (args.length > 0 && args[0].equals("-h")) {
            System.out.println(usage);
            System.exit(0);
        }

        SRILMFactoredBundleCorpusIterator in = null;
        BufferedWriter out = null;
        BufferedWriter voc = null;

        try {
            String inputCorp = "<stdin>", output = "<stdout>", vocabFile = "vocab.voc";
            int catCutoff = 10;

            for (int i = 0; i <
                    args.length; i++) {
                if (args[i].equals("-c")) { inputCorp = args[++i]; continue; }
                if (args[i].equals("-o")) { output = args[++i];    continue; }
                if (args[i].equals("-vocab")) {vocabFile = args[++i]; continue; }
                if (args[i].equals("-u")) { catCutoff = Integer.parseInt(args[++i]); continue; }
                System.out.println("Unrecognized option: " + args[i]);
            }
            
            try {
                in = new SRILMFactoredBundleCorpusIterator(
                        (inputCorp.equals("<stdin>")) ? new BufferedReader(new InputStreamReader(System.in)) : new BufferedReader(new FileReader(new File(inputCorp))));
            } catch (FileNotFoundException ex) {
                System.err.print("Input corpus " + inputCorp + " not found.  Exiting...");
                Logger.getLogger(STPriorModel.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(
                        -1);
            }

            try {
                out = (output.equals("<stdout>")) ? new BufferedWriter(new OutputStreamWriter(System.out)) : new BufferedWriter(new FileWriter(new File(output)));
            } catch (IOException ex) {
                System.err.print("Output file " + output + " not found.  Exiting...");
                Logger.getLogger(STPriorModel.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(
                        -1);
            }

            try {
                voc = new BufferedWriter(new FileWriter(new File(vocabFile)));
            } catch (IOException ex) {
                Logger.getLogger(STPriorModel.class.getName()).log(Level.SEVERE, null, ex);
            }

            Map<String, Integer> vocab = new HashMap<String, Integer>();
            for (List<Word> inLine : in) {
                for (Word w : inLine) {
                    String st = SUPERTAG + "-" + DefaultTokenizer.escape(w.getSupertag()),
                            pos = POS_TAG + "-" + DefaultTokenizer.escape(w.getPOS()),
                            wform = WORD + "-" + DefaultTokenizer.escape(w.getForm());

                    vocab.put(st, (vocab.get(st) == null) ? 1 : vocab.get(st) + 1);
                    vocab.put(pos, (vocab.get(pos) == null) ? 1 : vocab.get(pos) + 1);
                    vocab.put(wform, (vocab.get(wform) == null) ? 1 : vocab.get(wform) + 1);
                }

            }

            // reopen file
            try {
                in = new SRILMFactoredBundleCorpusIterator(
                        (inputCorp.equals("<stdin>")) ? new BufferedReader(new InputStreamReader(System.in)) : new BufferedReader(new FileReader(new File(inputCorp))));
            } catch (FileNotFoundException ex) {
                System.err.print("Input corpus " + inputCorp + " not found.  Exiting...");
                Logger.getLogger(STPriorModel.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(
                        -1);
            }
            for (List<Word> inLine : in) {
                for (Word w : inLine) {
                    String st = SUPERTAG + "-" + DefaultTokenizer.escape(w.getSupertag()),
                            pos = POS_TAG + "-" + DefaultTokenizer.escape(w.getPOS()),
                            wform = WORD + "-" + DefaultTokenizer.escape(w.getForm());
                    if (vocab.get(st) > catCutoff) {
                        out.write(wform + ":" + pos + ":" + st + " ");
                    }
                }

                out.write(System.getProperty("line.separator"));
            }

            out.flush();

            for (String str : vocab.keySet()) {
                if (vocab.get(str) > catCutoff) {
                    voc.write(str + System.getProperty("line.separator"));
                }
            }

            voc.flush();
        } finally {
            try {
                out.close();
                in.close();
                voc.close();
            } catch (IOException ex) {
                Logger.getLogger(STPriorModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
