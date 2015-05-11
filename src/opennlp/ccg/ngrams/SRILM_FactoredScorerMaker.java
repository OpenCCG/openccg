///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004 University of Edinburgh (Michael White)
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
package opennlp.ccg.ngrams;

import opennlp.ccg.synsem.SignScorer;

import java.io.*;

/**
 * A custom scorer maker that builds and loads factored n-gram models using the 
 * SRILM toolkit, which must be separately installed.
 * Most parameters are set in the FLM spec file.
 *
 * @author  Michael White
 * @version $Revision: 1.6 $, $Date: 2007/12/21 05:13:37 $
 */
public class SRILM_FactoredScorerMaker extends SRILM_ScorerMaker {
    
    /** The base of the model FLM spec file name.  Defaults to "test.flm". */
    public String flmSpecFileBase = "test.flm";
    
    
    /** Returns the model FLM spec filename, extending flmSpecFileBase with the discount options string and 
        n-gram order, eg "test.flm.n4". */
    protected String flmSpecFilename() {
        return flmSpecFileBase + "." + discountOptionsStr + getOrder();
    }
    
    /** Returns the root of the perplexities filename. */
    protected String pplFileRoot() { return flmSpecFilename(); }
    
    /** Creates fold-specific FLM spec filenames from fold numbers, eg "test.flm.n4.fold1". */
    protected String filename(int foldNum) {
        return flmSpecFilename() + "." + "fold" + foldNum;
    }
    
    
    /** Writes a fold-specific FLM spec file, given the tmp dir and fold num.
        The fold-specific file is created by simply replacing ".count" with ".foldN.count" 
        and ".lm" with ".foldN.lm", where N is the fold num, in the model FLM spec file. */
    protected void writeFoldSpecFile(File tmpDir, int foldNum) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(flmSpecFilename()));
        File foldSpecFile = new File(tmpDir, filename(foldNum));
        PrintWriter out = new PrintWriter(new FileWriter(foldSpecFile));
        String dotFoldN = ".fold" + foldNum;
        String line = null;
        while ((line = br.readLine()) != null) {
            int countIndex = line.indexOf(".count");
            if (countIndex > 0) {
                String foldLine = line.substring(0, countIndex);
                foldLine += dotFoldN;
                int lmIndex = line.indexOf(".lm", countIndex);
                foldLine += line.substring(countIndex, lmIndex);
                foldLine += dotFoldN;
                foldLine += line.substring(lmIndex);
                out.println(foldLine);
            }
            else {
                out.println(line);
            }
        }
        out.close();
        br.close();
    }
    
    /**
     * Prepares a scoring model from the training data, 
     * by exec-ing the SRILM fngram-count tool with the FLM spec file, 
     * and computes perplexity on the test data.
     * The training/test data are written to foldN-train.txt and foldN-test.txt, 
     * if not already present.
     * The fold-specific FLM spec file's name is determined by filename(N).
     */
    public void prepScorer(File tmpDir, int foldNum, File trainFile, File testFile) throws IOException {
        // write fold spec file
        writeFoldSpecFile(tmpDir, foldNum);
        // do rest much like standard n-gram scorers
        super.prepScorer(tmpDir, foldNum, trainFile, testFile);
    }
    
    /** Writes training/test targets. */
    protected void writeTargets(File tbFile, String textfile) throws IOException {
        if (!useSemClasses) 
            cvr.tester.writeTargetsF(tbFile, textfile); 
        else 
            cvr.tester.writeTargetsFSC(tbFile, textfile); 
    }
    
    /** Returns the command for making an ngram model. */
    protected String countNgrams(int foldNum) {
        String cmd = "fngram-count -nonull -write-counts -lm " +
                     ((unk) ? "-unk " : "") + 
                     "-factor-file " + filename(foldNum) + " " +  
                     "-text " + trainingfile(foldNum) + " " +  
                     "-debug " + debugLevel;
        return cmd;
    }
    
    /** Returns the command for calculating perplexity. 
        NB: At present, only the perplexity from the first model is collected. */
    protected String scoreNgrams(int foldNum) {
        String lmfile = filename(foldNum);
        String cmd2 = "fngram -nonull " + 
                      ((unk) ? "-unk " : "") + 
                      "-factor-file " + lmfile + " " +  
                      "-ppl " + testfile(foldNum);
        return cmd2;
    }
    
    
    /**
     * Loads a scoring model created from the training data. 
     */
    public SignScorer loadScorer(File tmpDir, int foldNum, File trainFile) throws IOException {
        File foldSpecFile = new File(tmpDir, filename(foldNum));
        String foldSpecPath = foldSpecFile.getCanonicalPath();
        return new FactoredNgramModelFamily(foldSpecPath, useSemClasses);
    }
}


