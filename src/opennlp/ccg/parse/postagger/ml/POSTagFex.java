///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010 Dennis N. Mehay
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed inp the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

package opennlp.ccg.parse.postagger.ml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import opennlp.ccg.parse.tagger.Constants;
import opennlp.ccg.parse.supertagger.ml.FeatureExtractor;
import opennlp.ccg.parse.tagger.TaggedWord;
import opennlp.ccg.util.Pair;
import opennlp.ccg.parse.tagger.io.SRILMFactoredBundleCorpusIterator;
import opennlp.ccg.lexicon.Word;

/**
 * Feature extractor for POS taggers.
 * 
 * The inputs are "TaggedWord"s simply for consistency of interface.
 * There should be no tags assigned to the words (short, perhaps, TOBI 
 * tags or the like).
 * 
 * @author Dennis N. Mehay
 */
public class POSTagFex implements FeatureExtractor {
    private POSPriorModel posPrior = null;
    
    /** Constructor with a prior model (replaces tagging dictionary). */
    public POSTagFex(POSPriorModel posPrior) {
        this.posPrior = posPrior;
    }
    
    /** Constructor without prior model. Prior features will not be used. */    
    public POSTagFex() { this(null); }
    
    public static final String curL = "X";
    public static final String prevL = "X-1";
    public static final String prevPrevL = "X-2";
    public static final String nextL = "X+1";
    public static final String nextNextL = "X+2";
    private static final String[] lxfLabs = {prevPrevL, prevL, curL, nextL, nextNextL};
    private static final String prefix = "prefix", suffix = "suffix";
    private static final String hyphen = "containsHyphen";
    private static final String caps = "containsUC";
    private static final String num = "containsNum";
    private static final String neConn = "containsNEConnector";
    private static final String priorF = "PPOS";
    /** The string that connects elements of a fused named entity. */
    private String neConnecter = "_";
    /** Get a word's features for applying the tagger (i.e., not training mode). */
    public Collection<Pair<String, Double>> getFeatures(Map<Integer, TaggedWord> sentence, Integer wordIndex) {
        return getFeatures(sentence, wordIndex, false);
    }

    /** Get a sentence of words' features for applying the tagger (i.e., not training mode). */
    public List<Collection<Pair<String, Double>>> getSentenceFeatures(Map<Integer, TaggedWord> sentence) {
        return getSentenceFeatures(sentence, false);
    }

    /** 
     * Get the features for a word in context.  training == true iff the output class is to be collected as well. 
     * 
     * TODO: This and supertagger feature extractor (fex) should be merged into a more general, parameterizable 
     * sentence-level contextual feature extractor. (VERY todo-ish, though.)
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
            prevPrev = Constants.OOB;
        } else {
            prev = prevPrev = Constants.OOB;
        }

        // -------- The right periphery -----------
        int tempSize = sentence.size();
        if ((tempSize - (wind + 1)) >= 2) {
            next = sentence.get(wind + 1);
            nextNext = sentence.get(wind + 2);
        } else if (tempSize - (wind + 1) >= 1) {
            next = sentence.get(wind + 1);
            nextNext = Constants.OOB;
        } else {
            next = nextNext = Constants.OOB;
        }

        Double activation = Constants.one;
        
        if (training) {
            result.add(new Pair<String, Double>(current.getPOS(), activation));
        }

        // we do not use tag-sequence features in this model.
        // these are in a separate sequence model (n-gram model over POS sequences).
        
        // standard contextual features (word to the left, current word, word to the right, etc.).
        // these features are from Ratnaparkhi (1996).
        result.add(new Pair<String, Double>(curL + "=" + current.getForm(), activation));
        result.add(new Pair<String, Double>(prevL + "=" + prev.getForm(), activation));
        result.add(new Pair<String, Double>(prevPrevL + "=" + prevPrev.getForm(), activation));
        result.add(new Pair<String, Double>(nextL + "=" + next.getForm(), activation));
        result.add(new Pair<String, Double>(nextNextL + "=" + nextNext.getForm(), activation));
        
        // features that replace the tagging dictionary. 
        // add real-valued (activation = prior log-prob) features for each of the beta-best prior
        // tags, given this word.
        if(posPrior != null) {
            List<Pair<Double,String>> priors = posPrior.getPriors(current.getWord());
            double beta = 0.1;
            double best = priors.get(0).a;
            String wform = current.getForm();
            for(Pair<Double,String> prior : priors) {
                if(prior.a > (beta * best)) {
                    // add the features PPOS=<POSTAG>:<log-prob> and PPOS_word=<POSTAG>_<wordForm>:<log-prob>.
                    result.add(new Pair<String,Double>(priorF + "=" + prior.b, prior.a));
                    result.add(new Pair<String,Double>(priorF + "_word" + "=" + prior.b + "_" + wform, prior.a));
                } else {
                    break;
                }
            }
        }
        
        // these are in addition to Ratnaparkhi's (1996) contextual features.
        // now for conjunctions of features: w-2w-1=..., w-1w+1=..., w+1w+2=... (same for posp).
        // (i.e., bigram features over words and parts of speech and bigrams of words and POSs that straddle the current token).
        // N.B. only use single-best POSs (maybe change later).
        TaggedWord[] wds = {prevPrev, prev, current, next, nextNext};

        for (int j = 1; j < wds.length; j++) {
            result.add(new Pair<String, Double>(lxfLabs[j - 1] + "|" + lxfLabs[j] + "=" + wds[j - 1].getForm() + "|" + wds[j].getForm(), activation));
            // also, if at the current word slot, add bigrams that straddle the current word.
            if (j == 2) {
                result.add(new Pair<String, Double>(lxfLabs[j - 1] + "|" + lxfLabs[j + 1] + "=" + wds[j - 1].getForm() + "|" + wds[j + 1].getForm(), activation));
            }
        }
        
        // affix features from Ratnaparkhi (1996).
        // if the word's length is > 4, then extract the 1-, 2-, 3- and 4-character affixes.        
        if(current.getForm().length() > 4) {
            StringBuffer prefixes = new StringBuffer(4), suffixes = new StringBuffer(4);
            char[] wdForm = current.getForm().toCharArray();
            // prefixes.
            int cursor = 0;
            for(cursor = 0; cursor < 4; cursor++) {
                prefixes.append(wdForm[cursor]);
                result.add(new Pair<String,Double>(prefix+"="+prefixes.toString(), Constants.one));
            }
            // suffixes.
            for(cursor = wdForm.length-1; cursor >= wdForm.length-5; cursor--) {
                suffixes.insert(0, wdForm[cursor]);
                result.add(new Pair<String,Double>(suffix+"="+suffixes.toString(), Constants.one));
            }
        }
        
        // now do "contains hyphen", "contains number", "contains uppercase letter" and contains fused NE connecter (_) features.
        // also from Ratnaparkhi (1996).        
        if(current.getForm().contains("-")) { result.add(new Pair<String,Double>(hyphen, Constants.one)); }
        if(current.getForm().matches(".*[0-9]+.*")) { result.add(new Pair<String,Double>(num, Constants.one)); }
        if(!current.getForm().toLowerCase().equals(current.getForm())) { result.add(new Pair<String,Double>(caps, Constants.one)); }
	// if we see a NE connector, this is likely a NNP (in English, e.g.).
        if(current.getForm().contains(neConnecter)) { result.add(new Pair<String,Double>(neConn, Constants.one)); }
        return result;
    }

    /** 
     * Get the features for a sentence of words in context.  
     * training == true iff the output classes are to be collected as well. 
     */
    public List<Collection<Pair<String, Double>>> getSentenceFeatures(Map<Integer, TaggedWord> sentence, boolean training) {
        List<Collection<Pair<String, Double>>> result = new ArrayList<Collection<Pair<String, Double>>>(30);
        List<Integer> keys = new ArrayList<Integer>(sentence.keySet().size());
        for(Integer wordIndex : sentence.keySet()) { keys.add(wordIndex); }
        Collections.sort(keys);
        for(Integer wordIndex : keys) {
            result.add(getFeatures(sentence, wordIndex, training));
        }
        return result;
    }
    
    public static void main(String[] args) throws IOException {        
        String usage = 
                "POSTagFex (-h [gets this message]) (-i <input> [defaults to <stdin>]) (-o <output> [defaults to <stdout>])\n"+
                "          (-p <posPriorModel> [.flm] -v <priorModVocab>)\n";
        if(args.length > 0 && args[0].equals("-h")) { System.out.println(usage); System.exit(0); }
        
        String input = "<stdin>", output = "<stdout>", priorModF = null, priorVocab = null;
        for(int j = 0; j < args.length; j++) {
            if(args[j].equals("-i")) { input = args[++j]; continue; }
            if(args[j].equals("-o")) { output = args[++j]; continue; }
            if(args[j].equals("-p")) { priorModF = args[++j]; continue; }
            if(args[j].equals("-v")) { priorVocab = args[++j]; continue; }
            System.err.println("Unrecognized option: " + args[j]); 
        }
        SRILMFactoredBundleCorpusIterator corp = new SRILMFactoredBundleCorpusIterator(
                input.equals("<stdin>") ?
                    new BufferedReader(new InputStreamReader(System.in)) :
                    new BufferedReader(new FileReader(new File(input))));
        BufferedWriter out = new BufferedWriter(
                output.equals("<stdout>") ?
                    new BufferedWriter(new OutputStreamWriter(System.out)) :
                    new BufferedWriter(new FileWriter(new File(output))));
        
        POSPriorModel posPriorMod = null;
        if(priorModF != null) {
            posPriorMod = new POSPriorModel(priorModF, priorVocab);
        }
        POSTagFex fexer = new POSTagFex(posPriorMod);        
        for(List<Word> sentence : corp) {
            Map<Integer, TaggedWord> sent = new HashMap<Integer, TaggedWord>(sentence.size());
            int index = 0;
            for(Word w : sentence) { sent.put(index++, new TaggedWord(w)); }
            
            List<Collection<Pair<String,Double>>> ftss = fexer.getSentenceFeatures(sent, true);
            
            for(Collection<Pair<String,Double>> fts : ftss) {
                index = 0;
                for(Pair<String,Double> ft : fts) {
                    // if we're at the first item, print out the label.
                    if (index == 0) {
                        out.write(ft.a);
                    } else {
                        out.write(" " + ft.a + ":" + ft.b);
                    }
                    index++;   
                }
                out.write(System.getProperty("line.separator"));
            }
        }
	out.flush();
    }
}
