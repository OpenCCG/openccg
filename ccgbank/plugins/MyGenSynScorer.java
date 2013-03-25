
package plugins;

import opennlp.ccg.synsem.*;
import java.io.*;

public class MyGenSynScorer extends GenerativeSyntacticModel
{
    static String modeldir = getModelDir();
    static String getModelDir() {
	String retval = System.getProperty("gensyn.model.dir", "models/parser");
	if (!retval.endsWith("/")) retval += "/";
	return retval;
    }

    public MyGenSynScorer() throws IOException {
        super(modeldir+"top.flm", modeldir+"leaf.flm", modeldir+"unary.flm", modeldir+"binary.flm");
    }
}