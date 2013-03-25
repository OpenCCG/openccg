
package plugins;

import opennlp.ccg.synsem.*;
import opennlp.ccg.perceptron.*;
import opennlp.ccg.hylo.*;

import java.io.*;

public class MyParserPerceptronScorer extends ReRankingPerceptronScorer
{
    static String modeldir = getModelDir();
    static String getModelDir() {
	String retval = System.getProperty("parser.models.dir", "models/parser");
	if (!retval.endsWith("/")) retval += "/";
	return retval;
    }

    static String modelname = getModelName();
    static String getModelName() { return System.getProperty("parser.model.name", "model.gz"); }

    public MyParserPerceptronScorer() throws IOException {
        super(
	      //new ComposedFeatureExtractor(new MyGenSynScorer(), new SyntacticFeatureExtractor()), 
	      new ComposedFeatureExtractor(new MyGenSynScorer(), new MySynSemFeatureExtractor()), 
	      new Model(modeldir + modelname)
	);
    }

    protected SignScorer getBaseScorer(FeatureExtractor featureExtractor) {
	return (SignScorer) ((ComposedFeatureExtractor)featureExtractor).featureExtractors[0];
    }
}