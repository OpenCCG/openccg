
package plugins;

import opennlp.ccg.synsem.*;
import opennlp.ccg.perceptron.*;

import java.io.*;

public class MyRealizerPerceptronScorer extends PerceptronScorer
{
    static String getModelDir() {
	String retval = System.getProperty("realizer.models.dir", "models/realizer");
	if (!retval.endsWith("/")) retval += "/";
	return retval;
    }

    static String getModelName() { return System.getProperty("realizer.model.name", "model.gz"); }

    static String getAgrMultiplier() { return System.getProperty("realizer.agr.mult", "10"); }

    static double calcAgrMultiplier() {
	try {
	    double mult = Double.parseDouble(getAgrMultiplier());
	    return mult;
	}
	catch (NumberFormatException exc) {
	    System.out.println("Ignoring unparseable negative agreement feature weight multiplier: " + getAgrMultiplier());
	    return 1.0;
	}
    }

    static double agrMult = calcAgrMultiplier();

    public MyRealizerPerceptronScorer() throws IOException {
        super(
	      //new ComposedFeatureExtractor(new MyNgramGenSynProduct(), new MySynAgrFeatureExtractor()), 
	      new ComposedFeatureExtractor(new MyNgramGenSynProduct(), new MySynSemAgrFeatureExtractor()), 
	      new Model(getModelDir() + getModelName(), agreementFilter)
	);
	System.out.println("Loading perceptron model from: " + getModelDir() + getModelName());
	System.out.println("Boosting negative agreement and punctuation balancing feature weights by a factor of " + agrMult);
    }

    // feature filter for boosting negative agreement feature weights
    private static Model.FeatureFilter agreementFilter = new Model.FeatureFilter() {
	    /** Returns the modified feature weight for the given feature. */
	    public double adjustedWeight(String name, double weight) {
		if (weight >= 0) return weight;
		if (name.equals("$punct") || name.startsWith("AGR") || isAdjacentPunctFeat(name)) 
		    return weight * agrMult;
		return weight;
	    }
	};

    // returns whether a feature is an adjacent punctuation n-gram feature
    private static boolean isAdjacentPunctFeat(String name) {
	String[] keys = name.split(":");
	if (keys.length >= 2 && isPunct(keys[0]) && isPunct(keys[1]))
	    return true;
	if (keys.length >= 4 && keys[0].equals("P") && keys[2].equals("P") && isPunct(keys[1]) && isPunct(keys[3]))
	    return true;
	return false;
    }

    // sentence-boundary markers treated like punctuation
    private static boolean isPunct(String token) {
	return token.equals("-") || token.equals("--") 
	    || token.equals(",") || token.equals(";") 
	    || token.equals(":") || token.equals("!")
	    || token.equals("?") || token.equals(".")
	    || token.equals("...") || token.equals("``")
	    || token.equals("'") || token.equals("''")
	    || token.equals("LRB") || token.equals("RRB");
    }
}