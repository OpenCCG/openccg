package plugins;

import opennlp.ccg.ngrams.*;
import java.io.*;
import java.util.*;
import java.lang.Thread;

public class MyNgramCombo extends LinearNgramScorerCombo
{
    static String bigWordsLM() { 
	String retval = System.getProperty("big.words.lm", "models/realizer/gigaword4.5g.kenlm.bin"); 
	if (new File(retval).exists()) return retval;
	System.out.println("Reusing trigram model as a stand-in for the big LM");
	return null;
    }
    static String wordsLM() { return System.getProperty("words.lm", "models/realizer/train.3bo"); }
    static String wordsSCLM() { return System.getProperty("words.sc.lm", "models/realizer/train-sc.3bo"); }
    static String stposFLM() { return System.getProperty("stpos.flm", "models/realizer/stp3.flm"); }

    // map to keep track of trigram model for reuse
    static Map<Thread,NgramScorer> lmMap = new IdentityHashMap<Thread,NgramScorer>(5);

    // return big lm, while setting trigram model if using it as a stand-in
    static NgramScorer getBigLM() throws IOException {
	String biglm = bigWordsLM();
	if (biglm != null) return new KenNgramModel(5, biglm, false, true, true, '_', false);
	NgramScorer retval = new StandardNgramModel(3, wordsLM());
	lmMap.put(Thread.currentThread(), retval);
	return retval;
    }

    // return trigram lm, reusing existing one if present
    static NgramScorer getWordsLM() throws IOException {
	NgramScorer retval = lmMap.get(Thread.currentThread());
	if (retval != null) {
	    lmMap.remove(Thread.currentThread());
	    return retval;
	}
	return new StandardNgramModel(3, wordsLM());
    }

    public MyNgramCombo() throws IOException {
	super(new NgramScorer[] { 
		getBigLM(), 
		getWordsLM(),
		new StandardNgramModel(3, wordsSCLM(), true), 
		new FactoredNgramModelFamily(stposFLM()) 
	    });
    }
}
