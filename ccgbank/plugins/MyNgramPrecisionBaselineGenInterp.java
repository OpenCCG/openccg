
package plugins;

import opennlp.ccg.ngrams.*;
import opennlp.ccg.synsem.*;

import java.io.*;

public class MyNgramPrecisionBaselineGenInterp extends SignScorerInterpolation implements SelfParaphraseBiaser
{
    static String[] targets = { "e plurubus unum" };

    NgramPrecisionModel selfBiaser;

    public MyNgramPrecisionBaselineGenInterp() throws IOException {
        super(
	      new SignScorer[] { new NgramPrecisionModel(targets), new MyNgramGenSynProduct() },
	      new double[] { 100.0, 1.0 }
	);
	selfBiaser = (NgramPrecisionModel) models[0];
    }

    public void setTargets(String[] targets) { selfBiaser.setTargets(targets); }
}