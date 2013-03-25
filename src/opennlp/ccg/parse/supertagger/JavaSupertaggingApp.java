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

import opennlp.ccg.parse.tagger.util.ResultSink;
import opennlp.ccg.parse.supertagger.ml.STFex;
import opennlp.ccg.parse.supertagger.ml.FeatureExtractor;
import opennlp.ccg.parse.tagger.ml.ZLMEM;
import opennlp.ccg.parse.tagger.io.SRILMFactoredBundleCorpusIterator;
import opennlp.ccg.parse.tagger.io.PipeDelimitedFactoredBundleCorpusIterator;
import opennlp.ccg.parse.tagger.Constants;
import java.io.*;
import java.util.*;
import static java.util.Arrays.*;
import joptsimple.*;
import opennlp.ccg.lexicon.Word;
import opennlp.ccg.parse.supertagger.io.*;
import opennlp.ccg.parse.supertagger.ml.*;
import opennlp.ccg.parse.supertagger.util.*;
import opennlp.ccg.util.Pair;

/**
 * @author Dennis N. Mehay
 * @version $Revision: 1.6 $, $Date: 2010/09/21 04:12:41 $
 */
public class JavaSupertaggingApp {

    public static void main(String[] args) throws Exception {
        try {
            // instantiate command-line option parser, setting up type-safe expectations about
            // what should be passed for the options.
            OptionParser parser = new OptionParser();
            parser.acceptsAll(asList("train", "R"), "extract training features.");
            parser.acceptsAll(asList("tag", "T"), "supertag a POS-tagged file.");
            parser.acceptsAll(asList("test","E"), "test tagger against gold standard.");            
            parser.acceptsAll(asList("tagdictextract", "D"), "extract tagging dictionaries.");
            parser.acceptsAll(asList("h", "?"), "show help.");
            OptionSpec<String> tokenisation = parser.acceptsAll(asList("delimiter", "d")).withRequiredArg().ofType(String.class).describedAs("SRILM factor bundles or C&C-style" +
                    "(pipe-delimited) factor bundles [choose one of: \"SRILM\", \"candc\"]");
            OptionSpec<File> goldstandspec = parser.acceptsAll(asList("g","gold")).withRequiredArg().ofType(File.class).
                    describedAs("the gold standard tagged file [file must have same bundle format as input corpus, \"SRILM\" or \"candc\"]");
            OptionSpec<File> inputspec = parser.acceptsAll(asList("i", "input")).withRequiredArg().ofType(File.class).describedAs("training or tagging/testing file");
            OptionSpec<File> outputspec = parser.acceptsAll(asList("o", "output")).withRequiredArg().ofType(File.class).describedAs("output location (for training feats or tags)");
            OptionSpec<File> modspec = parser.acceptsAll(asList("m", "model")).withRequiredArg().ofType(File.class).describedAs("textual model file (ZhangLe maxent-style) [for tagging/testing only]");
            OptionSpec<String> priormodspec = parser.acceptsAll(asList("priorModelF")).withRequiredArg().ofType(String.class).describedAs("config file for ARPA-formatted FLM [for tagging/testing and feature extraction"+
                    "MUST also give vocab file]");
            OptionSpec<String> vocabspec = parser.acceptsAll(asList("vocabF")).withRequiredArg().ofType(String.class).describedAs("vocab file for ARPA-formatted FLM [for tagging/testing and feature extraction]");
            OptionSpec<Integer> kspec = parser.accepts("K").withRequiredArg().ofType(Integer.class).describedAs("K parameter of Clark and Curran [for tagging/testing only]");
            OptionSpec<Double> betaspec = parser.accepts("beta").withRequiredArg().ofType(Double.class).describedAs("beam width for supertagger [for tagging only]");
            OptionSpec<File> wdictspec = parser.acceptsAll(asList("w", "worddict")).withRequiredArg().ofType(File.class).describedAs("path to the word-based tagging dictionary file");
            OptionSpec<File> pdictspec = parser.acceptsAll(asList("p", "posdict")).withRequiredArg().ofType(File.class).describedAs("path to the POS-based tagging dictionary file");
            OptionSpec<String> seqModel = parser.acceptsAll(asList("s","seqModel")).withOptionalArg().ofType(String.class).describedAs("the tag sequence model (for forward-backward tagging)");
            OptionSpec<Integer> fbBeam = parser.acceptsAll(asList("fbBeamWidth")).withOptionalArg().ofType(Integer.class).describedAs("maximum width of the forward-backward beam [default = 5]");
            OptionSpec<String> tagAlgorithm = parser.acceptsAll(asList("taggingAlgorithm")).withOptionalArg().ofType(String.class).describedAs("tagging algorithm. choose from {forward-backward, forward} [default = forward-backward]");
            OptionSet options = parser.parse(args);
            if (options.has("?") || args.length == 0) {
                parser.printHelpOn(System.out);
                System.exit(0);
            }
            assert (options.valueOf(tokenisation).equalsIgnoreCase("candc") || options.valueOf(tokenisation).equalsIgnoreCase("srilm"));
            // Must say whether we are tagging (or testing) or training (extracting features, actually).
            assert (options.has("tag") || options.has("train") || options.has("test") || options.has("D"));
            
            // Can't both train and tag/test, or train and extract tagging dict, or tag/test and do the last.
            assert !(options.has("train") && (options.has("tag") || options.has("test")));
            assert !(options.has("train") && options.has("D"));
            assert !((options.has("tag") || options.has("test")) && options.has("D"));
            
            // either we're doing forward-backward tagging, or we're not.
            assert (options.has("seqModel") || !(options.has("seqInterp") || options.has("fbBeamWidth")));
                        
            // Can't have a model file input when we are training....
            assert !(options.has("train") && options.has("m"));
            // ... or when extracting a tag dict.
            assert !(options.has("D") && options.has("m"));
            
            // Must have tagging dict files when tagging or extracting tag dicts, 
            // and additionally beta and K when tagging .
            assert (!(options.has("tag") || options.has("test") || options.has("D")) || (options.has("p") && options.has("w")));
            assert (!(options.has("tag") || options.has("test")) || (options.has("K") && options.has("beta")));
            
            // can't use prior model if no vocab file is given (so that the prior model knows which 
            // classes to make probabilistic predictions over) or no POS dictionary is given (so
            // that we can restrict our priors to those supertags that have occurred with a particular
            // POS).
            assert (!(options.has("priorModelF") && (!options.has("vocabF") || !options.has("p"))));
            STPriorModel stPrior = null;
            if (options.has("priorModelF")) {
                stPrior = new STPriorModel(options.valueOf(priormodspec), 
                        options.valueOf(vocabspec), 
                        new XMLPOSDictionaryReader(options.valueOf(pdictspec)).read());
            }
            
            if (options.has("tag") || options.has("test")) {
              long start = System.currentTimeMillis();
              // tag (and potentially measure performance against the gold-standard).              
              //File mod = options.valueOf(modspec);
              //Integer k = options.valueOf(kspec);
              Double beta = options.valueOf(betaspec);
              
              
              ZLMEM maxentModel;
              String seqMod = options.has("seqModel") ? options.valueOf(seqModel) : null;
              Integer fbWidth = options.has("fbBeamWidth") ? options.valueOf(fbBeam) : 5;
              
              String algStr = options.has("taggingAlgorithm") ? options.valueOf(tagAlgorithm) : "forward-backward";
              Constants.TaggingAlgorithm alg = algStr.equalsIgnoreCase("forward") ? 
                  Constants.TaggingAlgorithm.FORWARD : 
                  Constants.TaggingAlgorithm.FORWARDBACKWARD;
              
              STTaggerWordDictionary wd = null;
              STTaggerPOSDictionary pd = null;
              
              if(options.has("w")) wd = new XMLWordDictionaryReader(options.valueOf(wdictspec)).read();
              if(options.has("p")) pd = new XMLPOSDictionaryReader(options.valueOf(pdictspec)).read();
              
              WordAndPOSDictionaryLabellingStrategy tagger = new WordAndPOSDictionaryLabellingStrategy(
                      wd,
                      pd, 
                      (options.has("K") ? options.valueOf(kspec).intValue() : 20), 
                      maxentModel = new ZLMEM(options.valueOf(modspec)),
                      new STFex(stPrior),
                      seqMod,
                      alg);
              
              tagger.setMaxSearchBeam(fbWidth);
              maxentModel.verbose = true;
              
              Iterator<List<Word>> corpus = null;
              Iterator<List<Word>> goldCorpus = null;
              
              if(options.valueOf(tokenisation).equalsIgnoreCase("srilm")) {
                  corpus = new SRILMFactoredBundleCorpusIterator(new BufferedReader(new FileReader(options.valueOf(inputspec))));
              } else if(options.valueOf(tokenisation).equalsIgnoreCase("candc")) {
                  corpus = new PipeDelimitedFactoredBundleCorpusIterator(new BufferedReader(new FileReader(options.valueOf(inputspec))));
              }
              if(options.has("test") && options.valueOf(tokenisation).equalsIgnoreCase("srilm")) {
                  goldCorpus = new SRILMFactoredBundleCorpusIterator(new BufferedReader(new FileReader(options.valueOf(goldstandspec))));
              } else if(options.has("test") && options.valueOf(tokenisation).equalsIgnoreCase("candc")) {
                  goldCorpus = new PipeDelimitedFactoredBundleCorpusIterator(new BufferedReader(new FileReader(options.valueOf(goldstandspec))));
              }
              
              BufferedWriter outf = new BufferedWriter(new FileWriter(options.valueOf(outputspec)));
              
              boolean test = options.has("test");
              
              ResultSink results = new ResultSink();
              int sentCnt = 0;
              
              tagger.setBetas(new double[] {beta});
              
              while(corpus.hasNext()) {
                  sentCnt++;
                  List<Word> sent = corpus.next();
                  
                  List<List<Pair<Double,String>>> taggings = tagger.multitag(sent, beta); 
                  
                  if(test) {
                      List<Word> goldsent = goldCorpus.next();
                      results.addSent(taggings, goldsent);
                  }                  
                  
                  Iterator<Word> sentiter = sent.iterator(); 
                  // output file format = word goldtag tag1 ... tagK                  
                  outf.write("<s>"+System.getProperty("line.separator"));
                  for(List<Pair<Double,String>> tagging : taggings) {                      
                      Word nextw = sentiter.next();
                      outf.write(nextw.getForm() + "\t1\t" + nextw.getPOS() + "\t1.0\t" + tagging.size() + "\t");// + nextw.getSupertag() + " ");
                      //outf.write(nextw.getForm() + "|||"+ nextw.getStem() + "|||" + nextw.getPOS() + "|||");
                      String tags = "";
                      for(Pair<Double,String> tg : tagging) {
                          //tags+="^"+tg.b+":"+tg.a;
                          tags+= "\t" + tg.b + "\t"+tg.a;
                      }
                      // write out the multitagging, minus the initial space (tab).
                      outf.write(tags.substring(1) + System.getProperty("line.separator"));
                      
                      //// write out the multitagging, minus the initial ^.
                      //outf.write(tags.substring(1) + " ");
                  }                  
                                
                  outf.write("</s>"+System.getProperty("line.separator"));
                  if(sentCnt % 10 == 0) {
                      outf.flush();
                  }
              }
              outf.flush();
              outf.close();
              if(test) {
                  System.err.println(results.report());
              }
              long end = System.currentTimeMillis();
              System.err.println("Time to tag: " + ((end - start + 0.0)/1000) + " seconds.");
              
            } else if (options.has("tagdictextract")) {
              // extract tagging dictionaries.
              File wd = options.valueOf(wdictspec);
              File pd = options.valueOf(pdictspec);
              File inf = options.valueOf(inputspec);
              TaggingDictionaryExtractor tde = new TaggingDictionaryExtractor(inf,wd,pd,options.valueOf(tokenisation));
              System.err.println("Extracting dictionaries from: "+inf.toString()+" into files: "+wd.toString()+" and: "+pd.toString()+"\n(wdict and posdict, resp.).");
              tde.extract();
            } else {
                // train (extract features).        
                File inf = options.valueOf(inputspec);
                File outf = options.valueOf(outputspec);
                FeatureExtractor fexer = (stPrior == null) ? new STFex() : new STFex(stPrior);
                ZhangLeTrainingExtractor fexApp = new ZhangLeTrainingExtractor(inf, outf, options.valueOf(tokenisation), fexer);
                System.err.println("Extracting features from file: " + inf.toString() + ", and placing extracted features in: " + outf.toString() + ".");
                fexApp.writeFeats();
            }

        } catch (OptionException e) {
            throw e;
        } catch (Exception e) {
            throw e;
            //System.err.println("Something went wrong.  Double-check your inputs.");
        }
    }
}
