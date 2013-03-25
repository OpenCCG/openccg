///////////////////////////////////////////////////////////////////////////////
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

package opennlp.ccg.parse.tagger.sequencescoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.ccg.lexicon.Word;
import opennlp.ccg.ngrams.StandardNgramModel;
import opennlp.ccg.util.Interner;
import opennlp.ccg.util.Pair;
import opennlp.ccg.parse.tagger.ProbIndexPair;
import opennlp.ccg.parse.tagger.Constants;

/**
 * Initialise with a language model over the output sequences and,
 * given a List of List<Pair<Double,String>>'s initially tagged with "observation"
 * probabilities (output probabilities only based on local features),
 * return the forward-pass re-estimated probabilites of the output
 * classes.
 * 
 * @author Dennis N. Mehay
 */
public class SequenceScorer extends StandardNgramModel {

    /**
     * A Trellis to hold sequence labels (wrapped in Word classes)
     * functionality. 
     */
    private Trellis<Word> seqLabs;
    /** Trellis for initial observation model scores. */
    private Trellis<Double> initScores;
    /** Trellis for forward-backward re-estimated scores. */
    private Trellis<Double> fbScores;
    /** Trellis of back-pointers (for retrieving n-best sequences). */
    private Trellis<Backpointer> backPointers;
    /** How many of the previous (following) best predictions make it into the forward (or backward) search? */
    private int searchBeam = 200;
    /** Re-usable private data structures. */
    private List<List<Double>> tmpInitScores = new ArrayList<List<Double>>(500);
    private List<List<Double>> tmpFwdScores = new ArrayList<List<Double>>(500);
    private List<List<Word>> tmpSeqLabs = new ArrayList<List<Word>>(500);
    private List<List<Backpointer>> tmpBkpointers = new ArrayList<List<Backpointer>>(500);
    /** For interning Word's */
    private Interner<Word> words = new Interner<Word>();
    private Constants.TaggingAlgorithm alg = Constants.TaggingAlgorithm.FORWARDBACKWARD;

    /** Create a ForwardScorer with a sequence model (over supertags, POSs tags, words, etc.) */
    public SequenceScorer(int order, String lmFile) throws IOException {
        super(order, lmFile);
    }

    /** 
     * A utility method for finding the order of n-gram models (by reading in the ARPA-formatted file.
     * (A bit messy, I know.)
     */
    public static int findOrder(String tagSequenceModel) {
        // find n-gram order of sequence model.
        BufferedReader reader = null;
        String ln = null;
        int ord = 0;
        try {
            reader = new BufferedReader(new FileReader(new File(tagSequenceModel)));
            ln = reader.readLine();
            reader = new BufferedReader(new FileReader(new File(tagSequenceModel)));
            while (ln != null && !ln.startsWith("\\data\\")) {
                ln = reader.readLine();
            }
            ln = reader.readLine();
            while (ln != null & ln.startsWith("ngram ")) {
                ord = Integer.parseInt(ln.split(" ")[1].split("=")[0]);
                ln = reader.readLine();
            }
            reader.close();
        } catch (FileNotFoundException fnfe) {
            Logger.getLogger(SequenceScorer.class.getName()).log(Level.SEVERE, null, fnfe);
        } catch (IOException ioe) {
            Logger.getLogger(SequenceScorer.class.getName()).log(Level.SEVERE, null, ioe);
        }
        return ord;
    }

    /** Set the tagging algorithm (with one of {forward-backward, forward}). */
    public void setAlgorithm(Constants.TaggingAlgorithm newAlg) {
        alg = newAlg;
    }

    /**
     * Set the maximum width of the number of previous hypothesized tags to consider
     * in the forward probabilities.
     */
    public void setSearchBeam(int newBeam) {
        searchBeam = newBeam;
    }

    /** Rescore an observation sequence of (initially) supertagged Word's using the sequence model. */
    public List<List<Pair<Double, String>>> rescoreSequence(List<List<Pair<Double, String>>> observationSequence) {
        // build up initial trellises.
        tmpInitScores.clear();
        tmpFwdScores.clear();
        tmpSeqLabs.clear();
        tmpBkpointers.clear();

        for (List<Pair<Double, String>> tw : observationSequence) {
            ArrayList<Double> scrs = new ArrayList<Double>(tw.size());
            ArrayList<Double> fscs = new ArrayList<Double>(tw.size());
            ArrayList<Word> sLabs = new ArrayList<Word>(tw.size());
            ArrayList<Backpointer> bpts = new ArrayList<Backpointer>(tw.size());
            for (Pair<Double, String> tagging : tw) {
                // add observation score, and convert to log-prob domain, if needed.
                scrs.add((tagging.a > 0) ? Math.log(tagging.a) : tagging.a);
                fscs.add(null);
                sLabs.add(words.intern(Word.createWord(tagging.b, null, null, null, null, null, null)));
                bpts.add(null);
            }
            tmpInitScores.add(scrs);
            tmpSeqLabs.add(sLabs);
            tmpFwdScores.add(fscs);
            tmpBkpointers.add(bpts);
        }
        initScores = new Trellis<Double>(tmpInitScores);
        // these are initially null.
        fbScores = new Trellis<Double>(tmpFwdScores);
        // these are too.
        backPointers = new Trellis<Backpointer>(tmpBkpointers);
        seqLabs = new Trellis<Word>(tmpSeqLabs);

        // forward loop.
        // for each word...
        for (int u = 0; u < observationSequence.size(); u++) {
            List<Pair<Double, String>> tw = observationSequence.get(u);
            double normTot = 0.0;
            // for each of its tags within the search beam.
            for (int v = 0; v < tw.size(); v++) {
                Word currTag = seqLabs.getCoord(u, v);
                List<Word> bestHist = null;
                Double seqScore = null;

                Double obsScore = initScores.getCoord(u, v);
                if (u == 0) {
                    // beginning of sequence.                    
                    bestHist = getBestHist(u, v, order);
                    bestHist.add(currTag);
                    seqScore = lmScore(bestHist);

                    double fs = seqScore + obsScore; 

                    normTot += Math.exp(fs);
                    fbScores.setCoord(u, v, fs);
                } else {
                    // use dynamic programming-computed scores to progress.
                    List<Pair<Double, String>> prevTaggedWord = observationSequence.get(u - 1);
                    ProbIndexPair[] bestPrevScores = new ProbIndexPair[Math.min(prevTaggedWord.size(), searchBeam)];

                    for (int z = 0; z < Math.min(prevTaggedWord.size(), searchBeam); z++) {
                        bestHist = getBestHist(u - 1, z, order - 1);
                        bestHist.add(currTag);
                        seqScore = lmScore(bestHist);
                        double fs = fbScores.getCoord(u - 1, z) + seqScore;
                        fs += obsScore;                         
                        bestPrevScores[z] = new ProbIndexPair(
                                Double.valueOf(fs),
                                Integer.valueOf(z));

                    }

                    // sort descending based on score.
                    Arrays.sort(bestPrevScores);

                    // add up the prob's of all sequences leading to this node.
                    double fsum = 0.0;
                    for (int q = 0; q < bestPrevScores.length; q++) {
                        fsum += Math.exp(bestPrevScores[q].a);
                    }
                    normTot += fsum;
                    //fbScores.setCoord(u, v, bestPrevScores[0].a.doubleValue());
                    fbScores.setCoord(u, v, Math.log(fsum));

                    // add n-best backpointers.
                    List<Integer> bks = new ArrayList<Integer>(bestPrevScores.length);
                    for (int q = 0; q < bestPrevScores.length; q++) {
                        bks.add(bestPrevScores[q].b);
                    }
                    backPointers.setCoord(u, v, new Backpointer(bks));
                }
            }

            // normalise.            
            for (int v = 0; v < tw.size(); v++) {
                fbScores.setCoord(u, v, Math.log(Math.exp(fbScores.getCoord(u, v)) / normTot));
            }
        }

        // backward loop.
        int size = observationSequence.size();
        if (alg == Constants.TaggingAlgorithm.FORWARDBACKWARD) {
            // for each word...
            for (int u = size - 1; u >= 0; u--) {
                List<Pair<Double, String>> tw = observationSequence.get(u);
                double normTot = 0.0;
                // for each of its tags...
                for (int v = 0; v < tw.size(); v++) {
                    List<Word> bestHist = null;
                    
                    Double obsScore = initScores.getCoord(u, v);
                    
                    if (u == (size - 1)) { // right-hand end of sequence.

                        bestHist = getBestHist(u, v, order - 1);
                        bestHist.add(words.intern(Word.createWord("</s>", null, null, null, null, null, null)));
                        double bsc = fbScores.getCoord(u, v) + obsScore;
                        normTot += Math.exp(bsc);
                        fbScores.setCoord(u, v, bsc);
                    } else {
                        // use dynamic programming-computed scores to progress backwards.
                        bestHist = getBestHist(u, v, order - 1);
                        List<Pair<Double, String>> followingTaggedWd = observationSequence.get(u + 1);
                        double backwardSum = 0.0;
                        for (int z = 0; z < followingTaggedWd.size(); z++) {
                            Word followingTag = words.intern(Word.createWord(followingTaggedWd.get(z).b.intern(), null, null, null, null, null, null));
                            if (z > 0) {
                                bestHist.remove(bestHist.size() - 1);
                            }
                            bestHist.add(followingTag);
                            backwardSum += Math.exp(lmScore(bestHist) + fbScores.getCoord(u + 1, z));
                        }
                        double newSc = Math.log(backwardSum) + obsScore;
                        normTot += Math.exp(newSc);
                        fbScores.setCoord(u, v, newSc);
                    }
                }
                // normalise.
                for (int v = 0; v < tw.size(); v++) {
                    fbScores.setCoord(u, v, Math.log(Math.exp(fbScores.getCoord(u, v)) / normTot));
                }
            }
        }

        // re-sort based on re-estimated scores.        
        for (int i = 0; i < observationSequence.size(); i++) {
            ProbIndexPair[] fwdScrs = new ProbIndexPair[observationSequence.get(i).size()];
            List<Pair<Double, String>> tagging = observationSequence.get(i);

            for (int j = 0; j < tagging.size(); j++) {
                double probP = Math.exp(fbScores.getCoord(i, j).doubleValue());
                fwdScrs[j] = new ProbIndexPair(probP, new Integer(j));
            }
            Arrays.sort(fwdScrs);

            List<Pair<Double, String>> newTagging = new ArrayList<Pair<Double, String>>(fwdScrs.length);
            for (int z = 0; z < fwdScrs.length; z++) {
                Double renorm = new Double(fwdScrs[z].a.doubleValue());
                if (renorm.equals(Constants.one)) {
                    renorm = Constants.one;
                }
                newTagging.add(new Pair<Double, String>(renorm, tagging.get(fwdScrs[z].b.intValue()).b));
            }
            observationSequence.set(i, newTagging);
        }
        return observationSequence;
    }

    /** 
     * Use the LM to score a sequence of words.
     */
    private double lmScore(List<Word> seq) {
        setWordsToScore(seq, false);
        prepareToScoreWords();
        return logprob();
    }

    /** Follow the back-pointers to get the best sequence of up to length 'order' leading up to cell (i,j). */
    private List<Word> getBestHist(int i, int j, int order) {
        int size = Math.max(order, 0);
        List<Word> retVal = null;
        Backpointer bp = backPointers.getCoord(i, j);
        if (i == -1) {
            // base case (off of the end of the sequence).            
            retVal = new ArrayList<Word>(size);
            retVal.add(words.intern(Word.createWord("<s>", null, null, null, null, null, null)));
            return retVal;
        } else if (i == 0) {
            // base case (at beginning of sequence)
            retVal = getBestHist(i - 1, 0, order - 1);
            retVal.add(seqLabs.getCoord(i, j));
            return retVal;
        } else if (order == 0) {
            // base case (reached back as far as the n-gram model will need to see).
            retVal = new ArrayList<Word>(size);
            return retVal;
        } else {
            // recursive case.            
            retVal = getBestHist(i - 1, bp.get(0).intValue(), order - 1);
            retVal.add(seqLabs.getCoord(i, j));
            return retVal;
        }
    }
}
