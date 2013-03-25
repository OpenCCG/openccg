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
package opennlp.ccg.parse.supertagger.ml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.ccg.lexicon.Word;
import opennlp.ccg.parse.tagger.TaggedWord;
import opennlp.ccg.parse.tagger.Constants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import opennlp.ccg.parse.postagger.POSTagger;
import opennlp.ccg.parse.tagger.io.SRILMFactoredBundleCorpusIterator;
import opennlp.ccg.util.Pair;

/**
 * @author Dennis N. Mehay
 * @version $Revision: 1.7 $, $Date: 2010/12/15 07:16:03 $
 */
public class STFex implements FeatureExtractor {
	
    private boolean useMultiPOS = false;
    public static final String LEX = "X";
    public static final String POS = "P";
    public static final String prevP = POS + "-1=";
    public static final String prevPrevP = POS + "-2=";
    public static final String nextP = POS + "+1=";
    public static final String nextNextP = POS + "+2=";
    public static final String curP = POS + "=";
    public static final String prevL = LEX + "-1=";
    public static final String prevPrevL = LEX + "-2=";
    public static final String nextL = LEX + "+1=";
    public static final String nextNextL = LEX + "+2=";
    public static final String curL = LEX + "=";
    public static final String priorST = "PST" + "=";
    public static final String wordPOSPriorST = "WPosPST" + "=";
    public static final String wordPriorST = "WPST" + "=";
    public static final String POSPriorST = "PosPST" + "=";
    public static final String antiPriorST = "APST" + "=";    
    public static final String wordPOSAntiPriorST = "WPosAPST" + "=";
    public static final String wordAntiPriorST = "WAPST" + "=";
    public static final String POSAntiPriorST = "PosAPST" + "=";
    public static final TaggedWord outOfBounds = Constants.OOB;
    
    private static final String[] lxfLabs = {LEX + "-2", LEX + "-1", LEX,     LEX + "+1", LEX + "+2"};
    private static final String[] posfLabs = {POS + "-2", POS + "-1", POS,     POS + "+1", POS + "+2"};   
    
    /** 
     * An object containing a ConditionalProbabilityTable that can give a prior distribution over all
     * known supertags given a POS-tagged word.
     * 
     * Trained as a factored LM (presumably using SRILM).
     */
    protected STPriorModel priorMod;

    /** Constructor with no prior model. */
    public STFex( ) { this.priorMod = null; }
    
    /** Constructor with a prior model over supertags (to be used as a feature). */
    public STFex(STPriorModel priorMod) { this.priorMod = priorMod; }

    /** Pass in true to use multi-POS features, pass in false not to. */
    public void useMultiPOS(boolean trueOrFalse) { useMultiPOS = trueOrFalse; }
    
    /**
     * Extracts an <code>ArrayList<String></code> representing the contextual
     * predicates (features) of a line of (tokenised) text (each <code>String</code>
     * represents the predicates that fire for a word in the line).
     * Each resulting <code>String</code> will have the form:
     * cp1 cp2 ... cpK
     * @param sentence A <code>List<Word></code> of feature bundles.
     * @param wordIndex An <code>int</code> giving the location of the word to be tagged.
     * @return A <code>Collection<Pair<String,Double>></code> of real-valued feature activations
     * for the word at index <tt>wordIndex</tt>
     */
    public Collection<Pair<String, Double>> getFeatures(Map<Integer, TaggedWord> sentence, Integer wordIndex, boolean training) {
        Collection<Pair<String, Double>> result = new ArrayList<Pair<String, Double>>(30);

        TaggedWord current, prev, prevPrev, next, nextNext;
        current = sentence.get(wordIndex);
        // -------- The left periphery ------------
        int wind = wordIndex.intValue();
        if (wind > 1) {
            prev = sentence.get(wind - 1);
            prevPrev = sentence.get(wind - 2);
        } else if (wind > 0) {
            prev = sentence.get(wind - 1);
            prevPrev = outOfBounds;
        } else {
            prev = prevPrev = outOfBounds;
        }

        // -------- The right periphery -----------
        int tempSize = sentence.size();
        if ((tempSize - (wind + 1)) >= 2) {
            next = sentence.get(wind + 1);
            nextNext = sentence.get(wind + 2);
        } else if (tempSize - (wind + 1) >= 1) {
            next = sentence.get(wind + 1);
            nextNext = outOfBounds;
        } else {
            next = nextNext = outOfBounds;
        }
        Double activation = new Double(1.0);
        if (training)
            result.add(new Pair<String, Double>(current.getSupertag(), activation));      
        
        result.add(new Pair<String, Double>(curL + current.getForm(), activation));        
        if(useMultiPOS) { for(Pair<Double,String> tg : current.getPOSTagging()) result.add(new Pair<String,Double>(curP + tg.b, tg.a)); }
        else { result.add(new Pair<String, Double>(curP + current.getPOS(), activation)); }
        
        result.add(new Pair<String, Double>(prevL + prev.getForm(), activation));        
        if(useMultiPOS && prev != Constants.OOB) { for(Pair<Double,String> tg : prev.getPOSTagging()) result.add(new Pair<String,Double>(prevP + tg.b, tg.a)); }
        else { result.add(new Pair<String, Double>(prevP + prev.getPOS(), activation)); }        
        
        result.add(new Pair<String, Double>(prevPrevL + prevPrev.getForm(), activation));        
        if(useMultiPOS && prevPrev != Constants.OOB) { for(Pair<Double,String> tg : prevPrev.getPOSTagging()) result.add(new Pair<String,Double>(prevPrevP + tg.b, tg.a)); }
        else { result.add(new Pair<String, Double>(prevPrevP + prevPrev.getPOS(), activation)); }
        
        result.add(new Pair<String, Double>(nextL + next.getForm(), activation));        
        if(useMultiPOS && next != Constants.OOB) { for(Pair<Double,String> tg : next.getPOSTagging()) result.add(new Pair<String,Double>(nextP + tg.b, tg.a)); }
        else { result.add(new Pair<String, Double>(nextP + next.getPOS(), activation)); }
        
        result.add(new Pair<String, Double>(nextNextL + nextNext.getForm(), activation));
        if(useMultiPOS && nextNext != Constants.OOB) { for(Pair<Double,String> tg : nextNext.getPOSTagging()) result.add(new Pair<String,Double>(nextNextP + tg.b, tg.a)); }
        else { result.add(new Pair<String, Double>(nextNextP + nextNext.getPOS(), activation)); }
        
        // now for conjunctions of features: w-2w-1=..., w-1w+1=..., w+1w+2=... (same for posp).
        // (i.e., bigram features over words and parts of speech and bigrams of words and POSs that straddle the current token).
        // N.B. only use single-best POSs (maybe change later).       
        TaggedWord[] wds = {prevPrev,   prev,       current, next,       nextNext};
        
        for (int j = 1; j < wds.length; j++) {
            // add bigram features (only for single-best POS).           
            result.add(new Pair<String,Double>(lxfLabs[j - 1] + "|" + lxfLabs[j] + "=" + wds[j - 1].getForm() + "|" + wds[j].getForm(), activation));
            result.add(new Pair<String,Double>(posfLabs[j - 1] + "|" + posfLabs[j] + "=" + wds[j - 1].getPOS() + "|" + wds[j].getPOS(), activation));
            // also, if at the current word slot, add bigrams that straddle the current word.
            if (j == 2) {
                result.add(new Pair<String,Double>(lxfLabs[j - 1] + "|" + lxfLabs[j + 1] + "=" + wds[j - 1].getForm() + "|" + wds[j + 1].getForm(), activation));
                result.add(new Pair<String,Double>(posfLabs[j - 1] + "|" + posfLabs[j + 1] + "=" + wds[j - 1].getPOS() + "|" + wds[j + 1].getPOS(), activation));
            }
        }
        
        // If the prior model is not null, extract a feature for the beta-best (beta = 0.1) classes
        // predicted by the prior model (for all output classes -- supertags -- seen with this word's
        // POS).
        // Extract prior features from these. 
        if(priorMod != null) {
            priorMod.computePriors(current.getWord());
            for(Pair<String,Double> priorClassActivationPair : priorMod.getRestrictedBetaBestPriors(current.getWord(), 0.1)) { // TODO: make beta parameterizable.
                double act = Math.log(priorClassActivationPair.b);              
                String wd = current.getForm().intern(), pos = current.getPOS().intern();
                result.add(new Pair<String,Double>(priorST + priorClassActivationPair.a.intern(), act)); // log(prob)
                result.add(new Pair<String,Double>(wordPriorST + priorClassActivationPair.a.intern()+"_"+wd, act)); // log(prob)
                result.add(new Pair<String,Double>(wordPOSPriorST + priorClassActivationPair.a.intern()+"_"+wd+"_"+pos, act)); // log(prob)
                result.add(new Pair<String,Double>(POSPriorST + priorClassActivationPair.a.intern()+"_"+pos, act)); // log(prob)
                result.add(new Pair<String,Double>(antiPriorST + priorClassActivationPair.a.intern(), Math.log(1-Math.exp(act)))); // log(1-prob)
                // TODO: come up with sensible "anti-prior" features that simulate the filtering effect of the tagging dict.
            }
        }        
        return result;
    }

    public List<Collection<Pair<String, Double>>> getSentenceFeatures(Map<Integer, TaggedWord> sentence, boolean training) {
        List<Collection<Pair<String,Double>>> res = new ArrayList<Collection<Pair<String,Double>>>(sentence.size());
        List<Integer> keys = new ArrayList<Integer>(sentence.keySet().size());
        for(Integer i : sentence.keySet()) { keys.add(i); }
        Collections.sort(keys);
        for(Integer wordIndex : keys) {
            res.add(getFeatures(sentence, wordIndex, training));
        }
        return res;
    }

    public Collection<Pair<String, Double>> getFeatures(Map<Integer, TaggedWord> sentence, Integer wordIndex) {
        return getFeatures(sentence, wordIndex, false);
    }

    public List<Collection<Pair<String, Double>>> getSentenceFeatures(Map<Integer, TaggedWord> sentence) {
        return getSentenceFeatures(sentence, false);
    }
    
    // main method for extracting features from a file (for training).
    // pass in a supertag prior model and prior model vocab file, if desired.
    // (these replace tagging dictionaries).
    // pass in a POS tagger config file,  if we aren't only using gold POS tags only.
    // input corpus is from stdin, output goes to stdout.
    public static void main(String[] args) {
        // we assume that the training data is being streamed in from stdin (no parse IDs, just SRILM factor bundle lines),
        // and that output will stream to stdout.
        String usage = 
                "\n<stdin> | STFex (-h [gets this message]) (-r <priorSTModelF> -v <priorSTModelVocabF>) (-p <postaggerConfigFile>) | <stdout>";
        if(args.length > 0 && args[0].equals("-h")) { System.out.println(usage); System.exit(0); }
        
        String priorModF = null, priorVocab = null, posConfig = null;
        for(int j = 0; j < args.length; j++) {
            if(args[j].equals("-r")) { priorModF  = args[++j]; continue; }
            if(args[j].equals("-v")) { priorVocab = args[++j]; continue; }
            if(args[j].equals("-p")) { posConfig  = args[++j]; continue; }
            System.err.println("Unrecognized option: " + args[j]); 
        }
        SRILMFactoredBundleCorpusIterator corp = new SRILMFactoredBundleCorpusIterator(new BufferedReader(new InputStreamReader(System.in)));   
        STPriorModel stPriorMod = null;
        if(priorModF != null) {
            try {
                stPriorMod = new STPriorModel(priorModF, priorVocab);
            } catch (IOException ex) {
                Logger.getLogger(STFex.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        STFex fexer = new STFex(stPriorMod);        
        POSTagger posT = (posConfig == null) ? null : POSTagger.posTaggerFactory(posConfig);
        
        if(posT != null) { fexer.useMultiPOS(true); } else { fexer.useMultiPOS(false); }
        
        for(List<Word> sentence : corp) {
            Map<Integer, TaggedWord> sent = new HashMap<Integer, TaggedWord>(sentence.size());
            int index = 0;                
            if(posT == null) {                
                for(Word w : sentence) { sent.put(index++, new TaggedWord(w)); }            
            } else {
                List<TaggedWord> posTagging = posT.tagSentence(sentence);
                for(TaggedWord tw : posTagging) { sent.put(index++, tw); }
            }
            List<Collection<Pair<String,Double>>> ftss = fexer.getSentenceFeatures(sent, true);            
            for(Collection<Pair<String,Double>> fts : ftss) {
                index = 0;
                for(Pair<String,Double> ft : fts) {
                    // if we're at the first item, print out the label.
                    if (index == 0) {
                        System.out.print(ft.a);
                    } else {
                        System.out.print(" " + ft.a + ":" + ft.b);
                    }
                    index++;   
                }
                System.out.println();
            }
        }
    }
}
