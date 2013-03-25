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
package opennlp.ccg.parse.supertagger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import opennlp.ccg.parse.supertagger.io.XMLWordDictionaryReader;
import opennlp.ccg.parse.supertagger.io.XMLPOSDictionaryReader;
import opennlp.ccg.parse.supertagger.ml.STFex;
import opennlp.ccg.parse.supertagger.ml.FeatureExtractor;
import opennlp.ccg.parse.tagger.ProbIndexPair;
import opennlp.ccg.parse.tagger.TaggedWord;
import opennlp.ccg.parse.tagger.Constants;
import opennlp.ccg.parse.tagger.io.SRILMFactoredBundleCorpusIterator;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.ccg.lexicon.*;
import opennlp.ccg.parse.Supertagger;
import opennlp.ccg.parse.postagger.DummyPOSTagger;
import opennlp.ccg.parse.postagger.POSTagger;
import opennlp.ccg.parse.supertagger.ml.STPriorModel;
import opennlp.ccg.parse.tagger.ml.MaxentModel;
import opennlp.ccg.parse.supertagger.util.*;
import opennlp.ccg.parse.tagger.Constants.TaggingAlgorithm;
import opennlp.ccg.parse.tagger.ml.ZLMEM;
import opennlp.ccg.util.Pair;
import opennlp.ccg.parse.tagger.sequencescoring.SequenceScorer;
import opennlp.ccg.parse.tagger.util.ConfigFileProcessor;
import opennlp.ccg.parse.tagger.util.ResultSink;

/**
 * A `labelling strategy' for a CCG supertagger that
 * restricts the output of the model based on word and POS `tagging
 * dictionaries' in the following way:
 *
 * if a word w occurs at least K times in training, the model's output
 * is constrained to the outcomes seen with w during training.  If w
 * did not occur at least K times during training, the model's output is
 * constrained to the outcomes seen with w's POS tag during training.
 * In the off chance that the POS tag was not seen in training, the model's
 * best prediction is used.
 *
 * @author Dennis N. Mehay
 * @version $Revision: 1.22 $, $Date: 2011/03/22 03:20:25 $
 */
public class WordAndPOSDictionaryLabellingStrategy implements LabellingStrategy, Supertagger {

    // print warnings?
    private boolean verbose = false;
    // use tagging dictionaries?
    private boolean useWordDict = false;
    private boolean usePOSDict = false;
    private SequenceScorer seqScorer = null;
    private STTaggerWordDictionary wd;
    private STTaggerPOSDictionary pd;
    private int K, usualK, finalK;    
    private MaxentModel mo;
    // extracts features from the context of a word.
    private FeatureExtractor fexer = new STFex();
    
    // postagger for non-gold-POS supertagging.
    private POSTagger posTagger;

    // the current tagging.
    private List<TaggedWord> tagging;
    
    // POS-specific multipliers to "tighten" or "loosen" up the tagging beam width
    // ("beta") as needed. E.g., the beta for period/full stop might not need to be
    // very permissive, while those for lexical verbs or some fancy punctuation marks
    // might need to be.
    public Map<String,Double> betaMultipliers = new HashMap<String,Double>();
    public double minMultiplier = 1.0;
            
    /** Constructor without n-gram model (for scoring tag sequences). */
    public WordAndPOSDictionaryLabellingStrategy(STTaggerWordDictionary wd, STTaggerPOSDictionary pd, int K, MaxentModel mo,
            FeatureExtractor fexer) {
        this(wd, pd, K, mo, fexer, null, null);
    }

    
    /** Constructor WITH n-gram model (for scoring tag sequences). */
    public WordAndPOSDictionaryLabellingStrategy(
            STTaggerWordDictionary wd,
            STTaggerPOSDictionary pd,
            int K,
            MaxentModel mo,
            FeatureExtractor fexer,
            String tagSequenceModel,
            Constants.TaggingAlgorithm alg) {
        this(wd, pd, K, mo, fexer, tagSequenceModel, alg, new DummyPOSTagger());
    }
    /** Constructor with n-gram model and POS tagger */
    public WordAndPOSDictionaryLabellingStrategy(
            STTaggerWordDictionary wd,
            STTaggerPOSDictionary pd,
            int K,
            MaxentModel mo,
            FeatureExtractor fexer,
            String tagSequenceModel,
            Constants.TaggingAlgorithm alg,
            POSTagger posTagger) {
        this.wd = wd;
        this.pd = pd;
        this.mo = mo;
        this.posTagger = posTagger;
        if (K > 0) {
            this.K = K;
        } else {
            this.K = 0;
        }
        usualK = K; finalK = K;
        
        this.fexer = fexer;     
        
        try {

            if (tagSequenceModel != null) {
                // find the n-gram order of the model.
                int ord = SequenceScorer.findOrder(tagSequenceModel);
                // load it into the SequenceScorer.
                seqScorer = new SequenceScorer(ord, tagSequenceModel);
                seqScorer.setSearchBeam(5);
                seqScorer.setAlgorithm(alg);
            }
        } catch (IOException ex) {
            Logger.getLogger(WordAndPOSDictionaryLabellingStrategy.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }

    public void useWordDict(boolean useIt) { useWordDict = useIt; }
    public void usePOSDict(boolean useIt) { usePOSDict = useIt; }
    public WordAndPOSDictionaryLabellingStrategy(STTaggerWordDictionary wd, STTaggerPOSDictionary pd, int K, MaxentModel mo) {
        this(wd, pd, K, mo, new STFex());
    }

    /**
     * Set the maximum width of the beam in the forward-backward tagger.
     */
    public void setMaxSearchBeam(int maxSearchBeam) { if (seqScorer != null) seqScorer.setSearchBeam(maxSearchBeam); }
    
    /**
     * Reset the K parameter.
     */
    public void setK(int newK) {
        this.K = newK;
    }

     /**
     * Set the usual K parameter.
     */
    public void setUsualK(int newK) {
        this.usualK = newK;
    }

    /**
     * Set the final K parameter.
     */
    public void setFinalK(int newK) {
        this.finalK = newK;
    }

    /**
     * A method that returns all labels given by the model that both (1) are assigned probability `p' s.t.:
     * p>=(`beta'*<bestProb>), where `beta' is a factor passed in by the client of this method and
     * where <bestProb> is the probability of the most probably outcome of the model and (2) (if the word
     * (obtained from the <code>String[]</code> `context') has occured at least K times in training)
     * are in the <code>STTaggerWordDictionary</code> under the entry for said word.  If the word did
     * not occur at least K times in training, the output set is constrained by a <code>STTaggerPOSDictionary</code>.
     * In the off chance that a POS did not occur in the training data, the models predictions themselves are
     * submitted to the `beta constraint'.
     *
     * @param context A <code>String[]</code> of contextual predicates (in the maximum entropy modelling sense)
     * @param mo A <code>MaxentModel</code>.
     * @param beta A <code>double</code> specifying how close in probability all returned outcomes must be.
     * @return An <code>ArrayList<String></code> of labels that meet the above constraints.
     */
    public List<String> multitag(Word w, Collection<Pair<String, Double>> context, double beta) {
        List<Pair<Double, String>> temp = this.multitagWithScores(w, context, beta);
        ArrayList<String> res = new ArrayList<String>(temp.size());
        for (Pair<Double, String> t : temp) {
            res.add(t.b);
        }
        return res;
    }

    /**
     * A method to return the set of labels that are greater than or equal to 
     * the best label multiplied by a factor `beta', given a model and a <code>String[]</code>
     * of contextual predicates.
     * @param thisWord a <code>opennlp.ccg.lexicon.Word</code> representing the current word being tagged.
     * @param context A <code>Collection<Pair<String,Double>></code> of contextual predicates 
     * (in the maximum entropy modelling sense) with their corresponding activations (real-valued, hence the
     * <code>Double</code>).
     * @param model A model for generating the base predictions.
     * @param beta A positive <code>double</code> specifying how close to the best label
     *             each label returned must be.
     * @return An <code>ArrayList<Pair<Double,String>></code> of the outcomes
     *         {o: score(o)>=[beta * score(bestLabel)]}.
     */
    public List<Pair<Double, String>> multitagWithScores(Word thisWord, Collection<Pair<String, Double>> context, double beta) {
        // All the scores of the outcomes (the index of each double score
        // is the key which allows us to retrieve the outcome from the model).
        double[] ocs = mo.eval(context);
        // Sort in descending order of probability.
        ProbIndexPair[] sortedOutcomes = new ProbIndexPair[ocs.length];
        for (int i = 0; i < ocs.length; i++) {
            sortedOutcomes[i] = new ProbIndexPair(new Double(ocs[i]), new Integer(i));
        }
        Arrays.sort(sortedOutcomes);
        String tempOutcome = "";
        String word = thisWord.getForm();
        String pos = thisWord.getPOS();
        ArrayList<Pair<Double, String>> retVal = new ArrayList<Pair<Double, String>>(30);

        // Find the best outcomes seen with the word in training that
        // meet the `beta' constraint.
        // *******************************************************************************************
        double bestOutcomeProb, currentOutcomeProb;
        bestOutcomeProb = 0;
        // mww: changed to not always include front of list, as it may not meet dict constraints
        ProbIndexPair temp;
        // Now loop to see how many make the cut.
        // (But make sure to be sensitive to the dictionary, if necessary.)
        // See whether the word has a freq of this.K in the training corpus.
        Collection<String> wordPermittedOutcomes = (wd != null) ? this.wd.getEntry(word, this.K) : null;
        if (wordPermittedOutcomes != null && useWordDict) {
            // The word (lemma) was seen at least K times in training.
            // Get all beta-OK outcomes that are in the dictionary entry.
            for (int ocInd = 0; ocInd < sortedOutcomes.length; ocInd++) {
                temp = sortedOutcomes[ocInd];
                tempOutcome = mo.getOutcome(temp.b.intValue());
                currentOutcomeProb = temp.a.doubleValue();
                if (wordPermittedOutcomes.contains(tempOutcome)) {
                    if (bestOutcomeProb == 0) {
                        bestOutcomeProb = currentOutcomeProb;
                    }
                    if (currentOutcomeProb >= (bestOutcomeProb * beta)) { // Beta constraint.
                        // The cut-off was met, add the outcome.

                        retVal.add(new Pair<Double, String>(temp.a, tempOutcome));
                        // update max, for first selected outcome
                        if (currentOutcomeProb > bestOutcomeProb) {
                            bestOutcomeProb = currentOutcomeProb;
                        }
                    } else {
                        // Else, since our ProbIndexPair[] is sorted by probablity, there will be no more
                        // outcomes that make the (beta) cut.
                        break;
                    }
                } // If the word is not in the dictionary specified outcomes, move along.                

            }
        } else {
            // Revert to the POS dictionary.
            Collection<String> posPermittedOutcomes = null;
            if (pos != null) {
                posPermittedOutcomes = (pd != null) ? this.pd.getEntry(pos) : null;
            } else {
                if(verbose) { System.err.println("warning: null POS for: " + word);}  // mww: check for null pos

            }
            if (posPermittedOutcomes != null && usePOSDict) {
                // Get all beta-OK outcomes that are in the POS dictionary entry.
                for (int ocInd2 = 0; ocInd2 < sortedOutcomes.length; ocInd2++) {
                    temp = sortedOutcomes[ocInd2];
                    tempOutcome = mo.getOutcome(temp.b.intValue());
                    currentOutcomeProb = temp.a.doubleValue();

                    if (posPermittedOutcomes.contains(tempOutcome.trim())) {
                        if (bestOutcomeProb == 0) {
                            bestOutcomeProb = currentOutcomeProb;
                        }
                        if (currentOutcomeProb >= (bestOutcomeProb * beta)) { // Beta constraint.
                            // Made the cut-off, add the outcome.

                            retVal.add(new Pair<Double, String>(temp.a, tempOutcome));
                            // update max, for first selected outcome
                            if (currentOutcomeProb > bestOutcomeProb) {
                                bestOutcomeProb = currentOutcomeProb;
                            }
                        } else {
                            // Else, since our ProbIndexPair[] is sorted by probablity, there will be no more
                            // outcomes that make the (beta) cut.
                            break;
                        }
                    } // If the word is not in the dictionary specified outcomes, move along.                    

                }
            } else {
                // Otherwise, just get all model predictions that meet the beta constraint,
                // ignoring the word and POS dictionaries.
                for (int ocInd3 = 0; ocInd3 < sortedOutcomes.length; ocInd3++) {
                    temp = sortedOutcomes[ocInd3];
                    currentOutcomeProb = temp.a.doubleValue();

                    if (bestOutcomeProb == 0) {
                        bestOutcomeProb = currentOutcomeProb;
                    }
                    if (currentOutcomeProb >= (bestOutcomeProb * beta)) {
                        // Made the cut-off, add the outcome.
                        retVal.add(new Pair<Double, String>(temp.a, mo.getOutcome(temp.b.intValue())));
                        // update max, for first selected outcome
                        if (currentOutcomeProb > bestOutcomeProb) {
                            bestOutcomeProb = currentOutcomeProb;
                        }
                    } else {
                        // Else, since our ProbIndexPair[] is sorted by probability, there will be no more
                        // outcomes that make the cut.
                        break;
                    }
                }
            }
        }       

        // include the gold standard tag, if not in there.
        if(includeGold) {
            // assume input word has the gold tag in it.
            String gold = thisWord.getSupertag();
            // check whether gold is in the output.
            boolean containsGold = false;
            for(Pair<Double,String> tg : retVal) {
                if(tg.b.equals(gold)) {
                    containsGold = true;
                    break;
                }
            }
            if(!containsGold) {
                // insert it
                containsGold = false;
                for(ProbIndexPair oc : sortedOutcomes) {
                    if(mo.getOutcome(oc.b).equals(gold)) {
                        retVal.add(new Pair<Double,String>(oc.a, mo.getOutcome(oc.b)));
                        containsGold = true;
                        break;
                    }
                }          
            }
            if(!containsGold) {
                // if the gold-standard still isn't in there, it must not be part of the tag set, add it with epsilon probability.
                // we're assuming that gold tags are needed for a training routine that doesn't care about supertag probabilities
                // (as in Clark and Curran (2007)).
                // check to see whether we are in the log domain (by checking for negative scores -- kind of a hack).
                retVal.add(new Pair<Double,String>((sortedOutcomes[0].a < 0) ? -99 : 1.0112214926104486e-43, thisWord.getSupertag()));
            }
        }
        // *******************************************************************************************
        return retVal;
    }


    // get the current tagging (now only used to grab the POS tagging).    
    public List<TaggedWord> getCurrentTagging() { return tagging; }
    // set the current tagging (now only used to set the current POS tagging).
    public void setCurrentTagging(List<TaggedWord> tgging) { tagging = tgging; }
    
    public List<List<Pair<Double, String>>> multitag(List<Word> sentence, double beta) {
        List<List<Pair<Double, String>>> results = new ArrayList<List<Pair<Double, String>>>(sentence.size());
        Map<Integer, TaggedWord> sent = new TreeMap<Integer, TaggedWord>();
        int cnt = 0;
        
        List<TaggedWord> taggedSent = posTagger.tagSentence(sentence);
        setCurrentTagging(taggedSent);
        
        for (TaggedWord werd : taggedSent) {
            sent.put(new Integer(cnt++), werd);
        }

        List<Collection<Pair<String, Double>>> contexts = fexer.getSentenceFeatures(sent);

        // Iterate simultaneously through both the words and the contextual features.
        Iterator<Word> wds = sentence.iterator();
        Word w = null;
        Iterator<Collection<Pair<String, Double>>> ctxts = contexts.iterator();
        Collection<Pair<String, Double>> context = null;

        int cursor = 0;
        while (wds.hasNext() && ctxts.hasNext()) {
            // get the next word.
            w = wds.next();
            if(w.getPOS() == null) {
                w = Word.createFullWord(w, w.getForm(), tagging.get(cursor).getPOSTagging().get(0).b, w.getSupertag(), w.getSemClass()); 
            }
            context = ctxts.next();
            if (seqScorer != null) {
                // increase the tag ambiguity (for re-scoring using forward-backward).
                double newBeta = Math.min(beta * minMultiplier, beta / 8);
                if(beta < 0.00001) { newBeta = Math.min(beta * minMultiplier, beta / 2);  }
                    results.add(multitagWithScores(w, context, newBeta));
            } else { results.add(multitagWithScores(w, context, beta)); }
            cursor++;
        }
        
        List<List<Pair<Double,String>>> finalResults = null;
        if (seqScorer != null) {
            // rescore and filter. pass in input sentence (in case, e.g., we have set the includeGold flag).
            finalResults = betaBestFilter(seqScorer.rescoreSequence(results), beta, sentence);
        } else {
            finalResults = results;
        }       
        return finalResults;
    }

    /** 
     * Return a beta-best filtered subset of the tags in each multitagging list (each multitagging list is assumed to be non-empty). 
     */
    private List<List<Pair<Double, String>>> betaBestFilter(List<List<Pair<Double, String>>> multitaggings, double beta, List<Word> inputSentence) {
        List<List<Pair<Double, String>>> res = new ArrayList<List<Pair<Double, String>>>(multitaggings.size());

        int wordIndex = 0;
        for (List<Pair<Double, String>> mtagging : multitaggings) {
            List<Pair<Double, String>> tempTagging = new ArrayList<Pair<Double, String>>(mtagging.size());            
            Word thisWord = inputSentence.get(wordIndex);
            // set to a (possibly different, possibly less restrictive?) beta if this POS has a beta multiplier set.
            Double bmult = betaMultipliers.get(thisWord.getPOS());
            double possiblyNewBeta = Math.min(1.0, (bmult != null) ? (bmult * beta) : beta);
            
            double best = mtagging.get(0).a;
            for (Pair<Double, String> tg : mtagging) {
                if (tg.a >= (possiblyNewBeta * best) || (includeGold && tg.b.equals(thisWord.getSupertag()))) {
                    tempTagging.add(tg);
                } else {
                    if(!includeGold) {  // if we're not still fishing for gold...
                        // ...stop, since they're in sorted order.
                        break;
                    }
                }
            }            
            res.add(tempTagging);
            wordIndex++;
        }
        return res;
    }
    
    
    
    
    //-------------------------------------------------------------------------
    // Supertagger interface methods (added by Michael White)
    
    /**
     * The sequence of beta values to use in tagging.
     */
    protected double[] betas = null;
    
    /**
     * The current betaIndex.
     */
    protected int betaIndex = 0;
    
    /**
     * The current tagging.
     */
    protected List<List<Pair<Double, String>>> currentTagging = null;
    
    /**
     * The current word.
     */
    protected int currentWord = 0;
    
    /**
     * Flag for whether to include gold tags.
     */
    protected boolean includeGold = false;

    /** Sets the beta values. */
    public void setBetas(double[] betas) {
        this.betas = betas;
    }

    /** Returns all the beta values. */
    public double[] getBetas() {
        return betas;
    }

    /** Returns the current beta value. */
    public double getCurrentBetaValue() {
        return betas[betaIndex];
    }

    /**
     * Advances beta to the next most restrictive setting.
     */
    public void nextBeta() {
        betaIndex++;
    }

    /**
     * Advances beta to the next less restrictive setting.
     */
    public void previousBeta() {
        betaIndex--;
    }

    /**
     * Returns whether there are any less restrictive beta settings
     * remaining in the sequence.
     */
    public boolean hasMoreBetas() {
        return betaIndex < betas.length - 1;
    }

    /**
     * Returns whether there are any more restrictive beta settings
     * remaining in the sequence.
     */
    public boolean hasLessBetas() {
        return betaIndex > 0;
    }

    /**
     * Resets beta to the most restrictive value.
     */
    public void resetBeta() {
        betaIndex = 0;
    }

    /**
     * Resets beta to the least restrictive value.
     */
    public void resetBetaToMax() {
        betaIndex = betas.length - 1;
    }

    /**
     * Sets the flag for whether to include gold tags.
     */
    public void setIncludeGold(boolean includeGold) { this.includeGold = includeGold; }

    /**
     * Maps the given words to their predicted categories, 
     * so that the beta-best categories can be returned by calls to setWord
     * and getSupertags.
     */
    public void mapWords(List<Word> words) {
        if(hasMoreBetas()) {
            K = usualK;
        } else {
            K = finalK;            
        }        
        currentTagging = multitag(words, getCurrentBetaValue());
    }

    /**
     * Sets the current word to the one with the given index, 
     * so that the beta-best categories for it can be returned by a call to 
     * getSupertags.
     */
    public void setWord(int index) {
        currentWord = index;
    }

    /**
     * Returns the supertags of the desired categories for the current lexical lookup
     * as a map from supertags to contextual probabilities (or null to accept all). 
     */
    public Map<String, Double> getSupertags() {
        Map<String, Double> retval = new HashMap<String, Double>();
        List<Pair<Double, String>> tags = currentTagging.get(currentWord);
        for (Pair<Double, String> tag : tags) {
            retval.put(tag.b, tag.a);
        }
        return retval;
    }
    
    /** 
     * A factory method to make a supertagger from a config file (see the sample config file:
     *  
     * $OPENCCG_HOME/ccgbank/models/supertagger/st.config
     * 
     * for more information).
     */
    @SuppressWarnings("unused")
		public static WordAndPOSDictionaryLabellingStrategy supertaggerFactory(String configFile) {        
        WordAndPOSDictionaryLabellingStrategy res = null;
        String[] pathKeys = { "priormodel", "priormodelvocab", "sequencemodel", "wdict", "posdict", "maxentmodel", "posconfig" };
        Map<String,String> opts = ConfigFileProcessor.readInConfig(configFile, pathKeys);
        boolean verbose = (opts.get("verbose").equals("true")) ? true : false;
        // 'S' is for string repr.
        String priorModS = opts.get("priormodel"), 
               priorVocabS = opts.get("priormodelvocab"),
               seqModS = opts.get("sequencemodel"),
               wDictS = opts.get("wdict"),
               pDictS = opts.get("posdict"),
               firstKS = opts.get("firstk"),
               lastKS = opts.get("lastk"),
               maxentModS = opts.get("maxentmodel"),
               posConfigS = opts.get("posconfig"),
               betasS = opts.get("betas"),
               betaMults = opts.get("betamultipliers"), // POS-specific multipliers to "tighten" or "loosen" up the tagging beam width.
               includeGold = opts.get("includegold");
        
        assert (maxentModS != null) : "Empty maxent model.";
        
        // either use prior model (and have prior vocab specified) or not.
        assert (priorModS != null && priorVocabS != null) || (priorModS == null && priorVocabS == null) : "using prior model with no vocab file.";
        
        // ensure that there are word- and pos-keyed tagging dicts if there
        // is no st prior model.
        assert (wDictS == null || pDictS == null) && priorModS == null : "need tagging dicts if no supertagging prior model and prior vocab are specified.";
        
        // need the POS-keyed tagging dict, no matter what.
        assert (priorModS != null && pDictS == null) : "need POS-keyed tagging dict for prior model.";
        
        // need 'K' values if not using tagging dicts.
        assert (priorModS == null || (firstKS != null & lastKS != null)) : "need to specify first and last 'K' value when not using prior model.";
        
        // seqMod probably shouldn't be null. warn if in verbose mode.
        if(seqModS == null && verbose) { System.err.println("Warning: empty sequence model. Performance will suffer."); }
        
        STPriorModel priorM = null;
        if(priorModS != null && priorVocabS != null) {
            try { priorM = new STPriorModel(priorModS, priorVocabS); } 
            catch (IOException ex) {
                Logger.getLogger(WordAndPOSDictionaryLabellingStrategy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        STFex fex = new STFex(priorM);
        STTaggerWordDictionary wD = (wDictS != null) ? new XMLWordDictionaryReader(new File(wDictS)).read() : null;        
        STTaggerPOSDictionary pD = (pDictS != null) ? new XMLPOSDictionaryReader(new File(pDictS)).read() : null;
        int kay = (opts.get("firstk") == null) ? 20 : Integer.parseInt(opts.get("firstk")), firstK, lastK;
        firstK = (opts.get("firstk") == null) ? 20 : Integer.parseInt(opts.get("firstk"));
        lastK = (opts.get("lastk") == null) ? 100 : Integer.parseInt(opts.get("lastk"));
        double[] betaz = new double[betasS.split("\\s+").length];
        int cursor = 0;
        for(String beta : betasS.split("\\s+")) {
            betaz[cursor++] = Double.parseDouble(beta);
        }
        
        // should we use the tagging dictionaries (yes if there is no prior model).
        boolean useWordDictionary = (wDictS != null);
        boolean usePOSDictionary = (pDictS != null);
        POSTagger pTagger = (posConfigS == null) ? null : POSTagger.posTaggerFactory(posConfigS);
        TaggingAlgorithm alg = (opts.get("taggingalgorithm") == null || opts.get("taggingalgorithm").equals("forward-backward")) ?
            TaggingAlgorithm.FORWARDBACKWARD : TaggingAlgorithm.FORWARD;
        MaxentModel mem = new ZLMEM(new File(maxentModS));
        //STTaggerWordDictionary wd,STTaggerPOSDictionary pd,  int K, MaxentModel mo, FeatureExtractor fexer,
        //String tagSequenceModel, Constants.TaggingAlgorithm alg, POSTagger posTagger
        res = (pTagger != null) ? 
            new WordAndPOSDictionaryLabellingStrategy(wD, pD, kay, mem, fex, seqModS, alg, pTagger) :
            new WordAndPOSDictionaryLabellingStrategy(wD, pD, kay, mem, fex, seqModS, alg);
        res.setK(kay);
        res.setUsualK(firstK);
        res.setFinalK(lastK);
        res.setBetas(betaz);
        res.useWordDict(useWordDictionary);
        res.usePOSDict(usePOSDictionary);
        res.setIncludeGold((opts.get("includegold") == null || opts.get("includegold").equals("false")) ? false : true); 
        
        // get POS-specific beta multipliers (as a string of <POS,double> pairs -- all space delimited).
        if(betaMults != null) {
            String[] bmts = betaMults.split("\\s+");
            for(int a=0, b=1; b < bmts.length; a = a + 2, b = b + 2) {
                double mul = Double.parseDouble(bmts[b]);
                res.betaMultipliers.put(bmts[a], mul);
                if(mul < res.minMultiplier) {
                    res.minMultiplier = mul;
                }
            }
        }
        return res;
    }
    
    public static void main(String[] args) {
        String usage = "\nWordAndPOSDictLabellingStrategy (-h [gets this message]) -e <areWeTesting> [defaults to not testing] -c <configFile> > -beta [0.0,1.0]\n"+
                       "                                  (-i <input> [default=<stdin>]) (-o <output> [default=<stdout>])\n";
        
        if (args.length > 0 && args[0].equals("-h")) {
            System.out.println(usage);
            System.exit(0);
        }

        SRILMFactoredBundleCorpusIterator in = null;
        BufferedWriter out = null;
        try {
            
            String inputCorp = "<stdin>", output = "<stdout>", 
                   configFile = null;
            
            double beta = 1.0;
            boolean test = false;

            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-i")) { inputCorp = args[++i]; continue; }
                if (args[i].equals("-o")) { output = args[++i];    continue; }
                if (args[i].equals("-e")) { test = true; continue; }
                if (args[i].equals("-c")) { configFile = args[++i]; continue; }
                if (args[i].equals("-beta")) { beta = Double.parseDouble(args[++i]); continue; }
                System.out.println("Unrecognized option: " + args[i]);
            }

            ResultSink rs = new ResultSink(ResultSink.ResultSinkType.SUPERTAG);
            try {                
                in = new SRILMFactoredBundleCorpusIterator(
                        (inputCorp.equals("<stdin>")) ? 
                            new BufferedReader(new InputStreamReader(System.in)) : 
                            new BufferedReader(new FileReader(new File(inputCorp))));                
            } catch (FileNotFoundException ex) {
                System.err.print("Input corpus " + inputCorp + " not found.  Exiting...");
                Logger.getLogger(WordAndPOSDictionaryLabellingStrategy.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }

            try {
                out = (output.equals("<stdout>")) ? new BufferedWriter(new OutputStreamWriter(System.out)) : new BufferedWriter(new FileWriter(new File(output)));
            } catch (IOException ex) {
                System.err.print("Output file " + output + " not found.  Exiting...");
                Logger.getLogger(WordAndPOSDictionaryLabellingStrategy.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }

            WordAndPOSDictionaryLabellingStrategy stgger = WordAndPOSDictionaryLabellingStrategy.supertaggerFactory(configFile);
            
            // for each sentence, print out:
            // <s>
            // w1   <numPOSTags>    <posTag1>   ... <posTagK>   <numSupertags>  <supertag1> ... <supertagL>
            // ...
            // wN   <numPOSTags>    <posTag1>   ... <posTagM>   <numSupertags>  <supertag1> ... <supertagU>
            // </s>
            for (List<Word> inLine : in) {
                
                List<List<Pair<Double,String>>> taggedSent = stgger.multitag(inLine, beta);
                if(test) { rs.addSent(taggedSent, inLine); }
                // beginning of sentence...
                out.write("<s>" + System.getProperty("line.separator"));                
                List<TaggedWord> posTagging = stgger.getCurrentTagging();
                int cursor = -1;
                while(++cursor < taggedSent.size()) {
                    Word wdIn = inLine.get(cursor);
                    // word form...
                    out.write(wdIn.getForm());
                    TaggedWord posT = posTagging.get(cursor);
                    // print out number of POS tags, followed by tab-separated probabilized POS tagging.
                    out.write("\t" + posT.getPOSTagging().size());
                    for(Pair<Double,String> pt : posT.getPOSTagging()) {
                        out.write("\t" + pt.b + "\t" + pt.a);
                    }
                    // now print out number of and list of tab-separated, probabilized supertags.
                    out.write("\t" + taggedSent.get(cursor).size());
                    for(Pair<Double,String> stg : taggedSent.get(cursor)) {
                        out.write("\t" + stg.b + "\t" + stg.a);
                    }
                    out.write(System.getProperty("line.separator"));
                }
                out.write("</s>" + System.getProperty("line.separator"));
            }
            out.flush();

            if(test) { System.err.println(rs.report()); }
        } catch (IOException ex) {
            Logger.getLogger(WordAndPOSDictionaryLabellingStrategy.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(WordAndPOSDictionaryLabellingStrategy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
} // End class WordPOSDictLabellingStrategy

