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
package opennlp.ccg.parse.postagger;

import opennlp.ccg.parse.tagger.Constants.TaggingAlgorithm;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.ccg.parse.tagger.TaggedWord;
import opennlp.ccg.lexicon.Word;
import opennlp.ccg.parse.postagger.ml.POSPriorModel;
import opennlp.ccg.parse.postagger.ml.POSTagFex;
import opennlp.ccg.parse.tagger.ml.MaxentModel;
import opennlp.ccg.parse.tagger.ml.ZLMEM;
import opennlp.ccg.parse.tagger.sequencescoring.SequenceScorer;
import opennlp.ccg.util.Pair;
import opennlp.ccg.parse.tagger.util.ConfigFileProcessor;

/**
 * Interface for POS taggers.
 * 
 * @author Dennis N. Mehay
 */
public abstract class POSTagger {

    protected SequenceScorer posSeqMod = null;
    public TaggingAlgorithm alg = TaggingAlgorithm.FORWARDBACKWARD;
    public double beta = 1.0;
    protected boolean includeGold = false;

    /** In goes a list of {@code Word}s; out comes a list of {@code TaggedWord}s*/
    public abstract List<TaggedWord> tagSentence(List<Word> sentence);

    /** Set the tagging algorithm. */    
    public void setTaggingAlgorithm(TaggingAlgorithm newAlg) { 
        alg = newAlg; 
        posSeqMod.setAlgorithm(alg);
    }
    
    /** Set the beam width (by default, it's 1.0 -- i.e., single-best). */
    public void setBeta(double beta) {
        this.beta = beta;
    }

    /** Say whether or not we will include gold tags (e.g., for training). */
    public void setIncludeGold(boolean includeGoldOrNot) { includeGold = includeGoldOrNot; }
    
    /** 
     * Filter the POS tags by the beta filter and return the (potentially) trimmed-down results. 
     * It is assumed that the tags of the tagged word are sorted in descending order of
     * probability.
     */
    public List<TaggedWord> betaBestFilter(List<TaggedWord> sentence) {
        List<TaggedWord> res = new ArrayList<TaggedWord>(sentence.size());
        for (TaggedWord tw : sentence) {
            Word w = tw.getWord();
            double best = tw.getPOSTagging().get(0).a;
            int endIndex = 0;
            for (Pair<Double, String> tagging : tw.getPOSTagging()) {
                if (tagging.a >= (beta * best)) {
                    endIndex++;
                } else {
                    break;
                }
            }
            List<Pair<Double, String>> tmpTagging = new ArrayList<Pair<Double, String>>(tw.getPOSTagging().size());
            for (Pair<Double, String> tg : tw.getPOSTagging()) {
                tmpTagging.add(tg);
            }
            tmpTagging.subList(endIndex, tmpTagging.size()).clear();
            TaggedWord twTmp = new TaggedWord(w);
            twTmp.setPOSTagging(tmpTagging);
            res.add(twTmp);
        }
        return res;
    }
    
    /** 
     * Build a POS tagger from a config file. 
     * A non-dummy config file might contain:
     * ...
     * # this is a comment
     * taggerType=basic
     * priorModel=/home/.../posprior/prior.flm
     * # note that CaSE dOES Not matter for the key (but does for the value, e.g. a file name).
     * PRIORmodelvocab=/home/.../posprior/vocab.voc
     * # you can also repeat opions. the last one will take effect.
     * priormodelvocab=/home/.../posprior/vocab2.voc
     * 
     * # did you see that empty line get ignored?
     * # notice that spaces around the '=' get ignored, as well.
     * maxentModel = /home/.../maxentmodels/myposmod.mod
     * # this last must be an ARPA-formatted n-gram model over POS tags (7-grams work well).
     * sequenceModel=/home/.../pos.lm
     * # lastly, the tagging beam width (1.0 means "single-best" -- i.e., a unitagger).
     * beta=0.1
     */
    public static POSTagger posTaggerFactory(String configFile) {
        POSTagger res = null;
        try {

            String[] pathKeys = { "maxentmodel", "priormodel", "priormodelvocab", "sequencemodel"};
            Map<String, String> opts = ConfigFileProcessor.readInConfig(configFile, pathKeys);
            if (opts.get("taggertype").equalsIgnoreCase("dummy")) {
                return new DummyPOSTagger();
            }
            MaxentModel mem = new ZLMEM(new File(opts.get("maxentmodel")));
            POSPriorModel posPrior = null;
	    if (opts.get("priormodel") != null) {
		posPrior = new POSPriorModel(opts.get("priormodel"), opts.get("priormodelvocab"));
	    }
            POSTagFex fexer = new POSTagFex(posPrior);

            res = new BasicPOSTagger(mem, fexer, opts.get("sequencemodel"));
            res.setBeta(Double.parseDouble(opts.get("beta")));
            TaggingAlgorithm alg = (opts.get("taggingalgorithm") == null || opts.get("taggingalgorithm").equals("forward-backward")) ? 
                TaggingAlgorithm.FORWARDBACKWARD : TaggingAlgorithm.FORWARD;
            res.setTaggingAlgorithm(alg);

            res.setIncludeGold((opts.get("includegold") == null || opts.get("includegold").equals("false")) ? false : true);
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(POSTagger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(POSTagger.class.getName()).log(Level.SEVERE, null, ex);
        }

        return res;
    }
}
