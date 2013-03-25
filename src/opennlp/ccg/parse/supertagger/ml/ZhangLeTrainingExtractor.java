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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.ccg.lexicon.Word;
import opennlp.ccg.parse.tagger.io.PipeDelimitedFactoredBundleCorpusIterator;
import opennlp.ccg.parse.tagger.io.SRILMFactoredBundleCorpusIterator;
import opennlp.ccg.parse.supertagger.ml.FeatureExtractor;
import opennlp.ccg.parse.supertagger.ml.STFex;
import opennlp.ccg.parse.tagger.TaggedWord;
import opennlp.ccg.util.Pair;

/**
 * @author Dennis N. Mehay
 * @version $Revision: 1.3 $, $Date: 2010/09/21 04:12:41 $
 */
public class ZhangLeTrainingExtractor {

    private File outputF;
    private Iterator<List<Word>> incorp;
    private FeatureExtractor fexer = new STFex();

    /**
     * Create a training feature extractor that will extract features (with results)
     * for every instance in the input (training) corpus corpusName.
     * 
     * @param corpusName A <code>String</code> giving the complete
     * path to the input file of SRILM-compliant factored bundles.
     * @param outputFileName A <code>String</code> giving the complete
     * path to the output file where the features will be written.
     */
    public ZhangLeTrainingExtractor(File corpus, File outputF, String tokenisation) {
        this(corpus, outputF, tokenisation, new STFex());
    }
    
    public ZhangLeTrainingExtractor(File corpus, File outputF, String tokenisation, FeatureExtractor fexer) {
        this.fexer = fexer;
        this.outputF = outputF;
        try {
            if (tokenisation.equalsIgnoreCase("srilm")) {
                incorp = new SRILMFactoredBundleCorpusIterator(new BufferedReader(new FileReader(corpus)));
            } else {
                incorp = new PipeDelimitedFactoredBundleCorpusIterator(new BufferedReader(new FileReader(corpus)));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ZhangLeTrainingExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Writes training feats to file.
     */
    public void writeFeats() {
        BufferedWriter bw = null;
        try {
            try {
                bw = new BufferedWriter(new FileWriter(this.outputF));
            } catch (IOException ex) {
                Logger.getLogger(ZhangLeTrainingExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (bw != null || this.incorp != null) {
                List<Word> sent = null;
                Map<Integer, TaggedWord> snt = null;

                Iterator<List<Word>> sents = this.incorp;
                while (sents.hasNext()) {
                    //for (Iterator<List<Word>> sents = this.incorp; sents.hasNext();) {
                    sent = sents.next();

                    // turn the sent into a map from integer string indices to Words.
                    int index = 0;
                    snt = new TreeMap<Integer, TaggedWord>();
                    for (Word w : sent) {
                        snt.put(index++, new TaggedWord(w));
                    }

                    // 'true' says "we're getting training feats"
                    for (Collection<Pair<String, Double>> sentFeatsWithActivation : fexer.getSentenceFeatures(snt, true)) {
                        try {
                            boolean isLabel = true;
                            for (Pair<String, Double> ftWAct : sentFeatsWithActivation) {
                                if (isLabel) {
                                    bw.write(ftWAct.a + " ");
                                    isLabel = false;
                                } else {
                                    bw.write(ftWAct.a + ":" + ftWAct.b.doubleValue() + " ");
                                }
                            }
                            bw.newLine();
                        } catch (IOException ex) {
                            Logger.getLogger(ZhangLeTrainingExtractor.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }
            }
        } finally {
            try {
                bw.flush();
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(ZhangLeTrainingExtractor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}