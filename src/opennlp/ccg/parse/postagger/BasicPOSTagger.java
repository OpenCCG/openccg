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

package opennlp.ccg.parse.postagger;
import opennlp.ccg.parse.postagger.ml.POSPriorModel;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.ccg.lexicon.Word;
import opennlp.ccg.parse.tagger.TaggedWord;
import opennlp.ccg.parse.tagger.ml.MaxentModel;
import opennlp.ccg.parse.supertagger.ml.FeatureExtractor;
import opennlp.ccg.util.Pair;
import opennlp.ccg.parse.tagger.io.SRILMFactoredBundleCorpusIterator;
import opennlp.ccg.parse.tagger.util.ResultSink;
import opennlp.ccg.parse.tagger.sequencescoring.SequenceScorer;
import opennlp.ccg.parse.tagger.Constants;

/**
 * A non-dummy POS tagger.
 * 
 * @author Dennis N. Mehay
 */
public class BasicPOSTagger extends POSTagger {    
    
    private FeatureExtractor posFex = null;
    private MaxentModel tagMod = null;    
    
    private static final Comparator<Pair<Double,Integer>> comp = new Comparator<Pair<Double,Integer>>() {
        public int compare(Pair<Double, Integer> pr0, Pair<Double, Integer> pr1) { 
            // sorts descending by prob (the double member of the pair).
            if(pr0.a == pr1.a) { return 0; } else if (pr0.a < pr1.a) { return 1; } else { return -1; }  
        }
    };
    
    public BasicPOSTagger(MaxentModel tagMod, FeatureExtractor posFex, String tagSequenceModel)  {
        this.posFex = posFex;
        this.tagMod = tagMod;
        int ord = SequenceScorer.findOrder(tagSequenceModel);
        try {
            posSeqMod = new SequenceScorer(ord, tagSequenceModel);
            // set the search algorithm.
            posSeqMod.setAlgorithm(Constants.TaggingAlgorithm.FORWARDBACKWARD);
            // set the search beam width
            posSeqMod.setSearchBeam(5);
            
        } catch (IOException ex) {
            Logger.getLogger(BasicPOSTagger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    public List<TaggedWord> tagSentence(List<Word> sentence) { 
        List<TaggedWord> result = new ArrayList<TaggedWord>(sentence.size());
        
        // the prob-string taggings (to be filtered, etc. before adding them to the taggings of the TaggedWord list).        
        List<List<Pair<Double,String>>> taggings = new ArrayList<List<Pair<Double,String>>>(sentence.size());
        
        Map<Integer, TaggedWord> sentMap = new HashMap<Integer, TaggedWord>(sentence.size());
        int ind = 0; 
        for(Word w : sentence) {        
            sentMap.put(ind++, new TaggedWord(w)); 
        }
        List<Collection<Pair<String,Double>>> ftss = posFex.getSentenceFeatures(sentMap);
        
        double[] distro = null;        
        
        int wordIndex = 0;
        for(Collection<Pair<String,Double>> fts : ftss) {
            
            distro = tagMod.eval(fts);
            List<Pair<Double,Integer>> distroList = new ArrayList<Pair<Double,Integer>>(distro.length);
            ind = 0; for(double prob : distro) { distroList.add(new Pair<Double,Integer>(prob, ind++)); }            
            Collections.sort(distroList, comp);                        
            // widen beta a little bit (we're going to do some fwd-bwd rescoring inp a minute, but we don't
            // want to do the fwd-bwd alg over ALL possible tags -- too inefficient).
            List<Pair<Double,String>> tagging = new ArrayList<Pair<Double,String>>(distro.length);
            double best = distroList.get(0).a;
            double widenedBeta = beta/8;            
            
            String goldPOS = sentence.get(wordIndex).getPOS();
            
            for(Pair<Double,Integer> outcome : distroList) { 
                if( (outcome.a >= (widenedBeta * best)) || (includeGold && tagMod.getOutcome(outcome.b).equals(goldPOS)) ) {
                   tagging.add(new Pair<Double,String>(outcome.a, tagMod.getOutcome(outcome.b))); 
                } else {
                    if(!includeGold) {  // if not still potentially fishing for a gold POS tag, then break (they're in sorted order).
                        break;
                    } 
                }
            }
            taggings.add(tagging);
            wordIndex++;
        }
        // rescore using forward-backward.
        taggings = posSeqMod.rescoreSequence(taggings);        
        // add these rescored taggings to the list of TaggedWord's.
        int wInd = 0;
        for(List<Pair<Double,String>> tagging : taggings) {
            TaggedWord tmpWd = new TaggedWord(sentence.get(wInd++));
            tmpWd.setPOSTagging(tagging);            
            result.add(tmpWd);
        }
        // now filter down to the beta-best.
        return betaBestFilter(result); 
    }
    
    
    public static void main(String[] args) throws IOException {
        String usage = "\nBasicPOSTagger -c <configFile> (-i <input> [defaults to <stdin>]) (-o <output> [defaults to <stdout>])\n"+
                       "                 (-e [test tagger; assumes input is gold-standard corpus])\n";
        if (args.length > 0 && args[0].equals("-h")) {
            System.out.println(usage);
            System.exit(0);
        }

        SRILMFactoredBundleCorpusIterator inp = null;
        BufferedWriter out = null;
        
        try {
            String inputCorp = "<stdin>", output = "<stdout>", 
                   configFile = null;
            
            boolean test = false;

            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-i")) { inputCorp = args[++i]; continue; }
                if (args[i].equals("-o")) { output = args[++i];    continue; }
                if (args[i].equals("-e")) { test = true; continue; }
                if (args[i].equals("-c")) { configFile = args[++i]; continue; }
                System.out.println("Unrecognized option: " + args[i]);
            }

            ResultSink rs = new ResultSink(ResultSink.ResultSinkType.POSTAG);
            
            try {                        
                inp = new SRILMFactoredBundleCorpusIterator(
                        (inputCorp.equals("<stdin>")) ? 
                            new BufferedReader(new InputStreamReader(System.in)) : 
                            new BufferedReader(new FileReader(new File(inputCorp))));                
            } catch (FileNotFoundException ex) {
                System.err.print("Input corpus " + inputCorp + " not found.  Exiting...");
                Logger.getLogger(POSPriorModel.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }  

            try {
                out = (output.equals("<stdout>")) ? new BufferedWriter(new OutputStreamWriter(System.out)) : new BufferedWriter(new FileWriter(new File(output)));
            } catch (IOException ex) {
                System.err.print("Output file " + output + " not found.  Exiting...");
                Logger.getLogger(POSPriorModel.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }

            POSTagger post = POSTagger.posTaggerFactory(configFile);
            
            for (List<Word> inLine : inp) {
                List<TaggedWord> taggedSent = post.tagSentence(inLine);
                List<List<Pair<Double,String>>> sentTagging = new ArrayList<List<Pair<Double,String>>>(taggedSent.size());
                for(TaggedWord tw : taggedSent) { sentTagging.add(tw.getPOSTagging()); }
                if(test) { rs.addSent(sentTagging, inLine); }
                out.write("<s>" + System.getProperty("line.separator"));
                for(TaggedWord tw : taggedSent) {
                    out.write(tw.getForm());
                    for(Pair<Double,String> tg : tw.getPOSTagging()) {
                        out.write("\t" + tg.b + "\t" + tg.a);
                    }
                    out.write(System.getProperty("line.separator"));
                }
                out.write("</s>" + System.getProperty("line.separator"));
            }
            out.flush();

            if(test) { System.err.println(rs.report()); }
        } catch(Throwable t) {
            t.printStackTrace();
        } finally {
            try {                
                inp.close();
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(POSPriorModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}