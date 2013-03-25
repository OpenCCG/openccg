
package plugins;

import opennlp.ccg.ngrams.*;
import opennlp.ccg.synsem.*;

import java.io.*;

public class MyNgramPrecisionPerceptronInterp extends SignScorerInterpolation implements SelfParaphraseBiaser
{
    static String[] targets = { 
	"lee said brianna had dragged food , toys and other things into the bedroom .", 
	"lee , 33 , said the girl had dragged the food , toys and other things into her mother 's bedroom ." 
	//"charles o. prince , 53 , was named as mr. weill 's successor .",
	//"mr. weill 's longtime confidant , charles o. prince , 53 , was named as his successor ."
    };

    NgramPrecisionModel selfBiaser;

    public MyNgramPrecisionPerceptronInterp() throws IOException {
        super(
	      new SignScorer[] { new NgramPrecisionModel(targets), new MyRealizerPerceptronScorer() },
	      //new double[] { 100.0, 1.0 }
	      new double[] { 10000.0, 1.0 }
	);
	selfBiaser = (NgramPrecisionModel) models[0];
    }

    public void setTargets(String[] targets) { selfBiaser.setTargets(targets); }
}