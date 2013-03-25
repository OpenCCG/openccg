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
import opennlp.ccg.test.*;
import java.io.*;
import java.util.*;

/**
 * A custom scorer maker that builds and loads standard n-gram models using the 
 * SRILM toolkit, which must be separately installed.
 * This class may be subclassed to set different parameters in the constructor, 
 * for use in cross-validation tests with the realizer.
 *
 * @author  Michael White
 * @version $Revision: 1.10 $, $Date: 2007/12/21 05:13:37 $
 */
public class SRILM_ScorerMaker implements ScorerMaker {
    
    /** Flag specifying whether to use semantic class replacement. */
    public boolean useSemClasses = true;
    
    /** Flag specifying whether to keep &lt;unk&gt; in the LM. */
    public boolean unk = true;
    
    /** String specifying the min counts to use in building the n-gram model. */
    public String minCountOptions = "-gt1min 1 -gt2min 1 -gt3min 1 -gt4min 1 -gt5min 1 -gt6min 1";
    
    /** String specifying the discounting parameters. */
    public String discountOptions = N_DISCOUNT_PARAMS;
    
    /** String specifying natural discounting parameters. */
    public static final String N_DISCOUNT_PARAMS = "-ndiscount1 -ndiscount2 -ndiscount3 -ndiscount4 -ndiscount5 -ndiscount6";

    /** String specifying Witten-Bell discounting parameters. */
    public static final String WB_DISCOUNT_PARAMS = "-wbdiscount1 -wbdiscount2 -wbdiscount3 -wbdiscount4 -wbdiscount5 -wbdiscount6";
    
    /** String specifying modified Kneser-Ney natural discounting parameters. */
    public static final String KN_DISCOUNT_PARAMS = "-kndiscount1 -kndiscount2 -kndiscount3 -kndiscount4 -kndiscount5 -kndiscount6";
    
    /** String indicating the discounting option in filenames. */
    public String discountOptionsStr = "n"; 
    
    /** Debug level to use. */
    public int debugLevel = 1;
    
    
    /** The context for this scorer maker. */
    public CrossValidateRealizer cvr = null;
    
    /** Sets the context for this scorer maker. */
    public void setCVR(CrossValidateRealizer cvr) { this.cvr = cvr; }
    
    
    /** Stores perplexities for each fold, after calls to prepScorer. */
    protected List<Double> perplexities = new ArrayList<Double>();
    
    /** Gets the n-gram order from the context, defaulting to 3. */
    protected int getOrder() {
        int order = cvr.tester.ngramOrder;
        return (order > 0) ? order : 3;
    }
    
    /** Creates LM filenames from fold numbers. */
    protected String filename(int foldNum) {
        String retval = "fold" + foldNum + "-" + discountOptionsStr;  
        if (useSemClasses) retval += "-sc";
        retval += "." + getOrder() + "bo";
        return retval;
    }
    
    /**
     * Prepares a scoring model from the training data, 
     * by exec-ing the SRILM ngram-count tool with the current options, 
     * and computes perplexity on the test data.
     * The training/test data are written to foldN-train.txt and foldN-test.txt, 
     * if not already present.
     * The LM file's name is determined by filename(N).
     */
    public void prepScorer(File tmpDir, int foldNum, File trainFile, File testFile) throws IOException {
        // write training/test files, if not already present
        writeTrainingAndTestFiles(tmpDir, foldNum, trainFile, testFile);
        // make counting command
        String cmd = countNgrams(foldNum);
        // exec command
        System.out.print("Writing " + getOrder() + "-gram model: " + filename(foldNum) + "\n");
        Process makeLM = Runtime.getRuntime().exec(cmd, null, tmpDir);
        try {
            InputStream istr = makeLM.getErrorStream(); //.getInputStream();
            int b;
            while ((b = istr.read()) != -1) { System.out.print((char)b); }
            int exitval = makeLM.waitFor();
            if (exitval != 0) {
                System.out.println("(f)ngram-count exitval: " + exitval);
            }
        }
        catch (InterruptedException exc) {
            throw (RuntimeException) new RuntimeException().initCause(exc);
        }
        System.out.println();
        // make scoring command, for perplexity
        String cmd2 = scoreNgrams(foldNum);
        // exec command
        System.out.print("Measuring perplexity with: " + filename(foldNum) + "\n");
        Process measurePPL = Runtime.getRuntime().exec(cmd2, null, tmpDir);
        try {
            InputStream istr = measurePPL.getInputStream();
            int b;
            StringBuffer sb = new StringBuffer();
            while ((b = istr.read()) != -1) { 
                System.out.print((char)b); sb.append((char)b); 
            }
            int exitval = measurePPL.waitFor();
            // extract perplexity following "ppl= "
            String pplOut = sb.toString();
            int pplStart = pplOut.indexOf("ppl= ") + "ppl= ".length();
            int pplEnd = pplOut.indexOf(" ", pplStart);
            String pplStr = pplOut.substring(pplStart, pplEnd);
            try {
                perplexities.add(new Double(pplStr));
            }
            catch (NumberFormatException exc) { 
                System.out.println("Warning, unable to extract perplexity from: " + pplStr);
                System.out.println(exc.toString());
            }
            if (exitval != 0) {
                System.out.println("(f)ngram exitval: " + exitval);
            }
        }
        catch (InterruptedException exc) {
            throw (RuntimeException) new RuntimeException().initCause(exc);
        }
        System.out.println();
    }
    
    /** Writes the training and test files, if not already present. */
    protected void writeTrainingAndTestFiles(File tmpDir, int foldNum, File trainFile, File testFile) throws IOException {
        File trainingFoldFile = new File(tmpDir, trainingfile(foldNum)); 
        if (!trainingFoldFile.exists()) {
            String trainingFoldPath = trainingFoldFile.getCanonicalPath();
            writeTargets(trainFile, trainingFoldPath);
        }
        File testFoldFile = new File(tmpDir, testfile(foldNum)); 
        if (!testFoldFile.exists()) {
            String testFoldPath = testFoldFile.getCanonicalPath();
            writeTargets(testFile, testFoldPath); 
        }
    }
    
    /** Returns the name of the training file. */
    protected String trainingfile(int foldNum) { return "fold" + foldNum + "-train.txt"; }
        
    /** Returns the name of the test file. */
    protected String testfile(int foldNum) { return "fold" + foldNum + "-test.txt"; }
        
    /** Writes training/test targets. */
    protected void writeTargets(File tbFile, String textfile) throws IOException {
        if (!useSemClasses) 
            cvr.tester.writeTargets(tbFile, textfile); 
        else 
            cvr.tester.writeTargetsSC(tbFile, textfile); 
    }

    /** Returns the command for making an ngram model. */
    protected String countNgrams(int foldNum) {
        String lmfile = filename(foldNum);
        String cmd = "ngram-count -order " + getOrder() + " " + 
                     ((unk) ? "-unk " : "") + 
                     minCountOptions + " " + 
                     discountOptions + " " + 
                     "-text " + trainingfile(foldNum) + " " +  
                     "-lm " + lmfile + " " + 
                     "-debug " + debugLevel;
        return cmd;
    }
    
    /** Returns the command for calculating perplexity. */
    protected String scoreNgrams(int foldNum) {
        String lmfile = filename(foldNum);
        String cmd2 = "ngram -order " + getOrder() + " " + 
                      ((unk) ? "-unk " : "") + 
                      "-ppl " + testfile(foldNum) + " " +  
                      "-lm " + lmfile;
        return cmd2;
    }
    
    /** Returns the root of the perplexities filename. */
    protected String pplFileRoot() {
        return discountOptionsStr + getOrder();
    }
    
    /**
     * Summarizes perplexities after all calls to prepScorer.
     */
    public void prepScorersSummary(File tmpDir) throws IOException {
        // summarize to sysout
        double sum = 0;
        System.out.print("Perplexities: ");
        for (int i = 0; i < perplexities.size(); i++) {
            double ppl = perplexities.get(i).doubleValue();
            System.out.print(ppl + " ");
            sum += ppl;
        }
        System.out.println();
        double avg = sum / perplexities.size();
        System.out.println("Avg: " + avg);
        // then to xml
        String filename = "ppl";  
        if (useSemClasses) filename += "-sc";
        filename += "." + pplFileRoot() + ".xml";
        System.out.println("Writing perplexities: " + filename);
        PrintWriter pw = new PrintWriter(new FileWriter(new File(tmpDir, filename)));
        pw.println("<perplexities avg=\"" + avg + "\">");
        for (int i = 0; i < perplexities.size(); i++) {
            pw.println("  <fold num=\"" + i + "\" ppl=\"" + perplexities.get(i) + "\"/>");
        }
        pw.println("</perplexities>");
        pw.close();
    }
    
    
    /**
     * Loads a scoring model created from the training data. 
     */
    public SignScorer loadScorer(File tmpDir, int foldNum, File trainFile) throws IOException {
        String lmfile = filename(foldNum);
        String lmPath = new File(tmpDir, lmfile).getCanonicalPath();
        return new StandardNgramModel(getOrder(), lmPath, useSemClasses);
    }
}


