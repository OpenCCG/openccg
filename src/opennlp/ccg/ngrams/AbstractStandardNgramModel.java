/*
 * $Id: AbstractStandardNgramModel.java,v 1.3 2009/12/21 03:27:18 mwhite14850 Exp $ 
 */
package opennlp.ccg.ngrams;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import opennlp.ccg.lexicon.Word;
import opennlp.ccg.util.Pair;


/**
 * Abstract class for shared methods used by all standard ngram models.
 * Adapted from the original StandardNgramModel class. 
 * 
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $Revision: 1.3 $
 * @since 0.9.2
 */
abstract class AbstractStandardNgramModel extends NgramScorer {

	/** Reusable list of strings to score. */
    protected List<String> stringsToScore = new ArrayList<String>();
	
    /**
     * Creates a new ngram model of the given order.
     * @param order The order of the model.
     * @param useSemClasses Whether this model should use semantic classes.
     * @see NgramScorer#NgramScorer(int, boolean)
     */
    protected AbstractStandardNgramModel(int order, boolean useSemClasses) {
		super(order, useSemClasses);
		numNgrams = new int[order];
	}

    /**
     * Creates a new ngram model with the specified order.
     * @see AbstractStandardNgramModel#AbstractStandardNgramModel(int, boolean)
     */
    protected AbstractStandardNgramModel(int order) {
		this(order, false);
	}

	/**
     * Converts the words in wordsToScore to strings in stringsToScore, before
     * scoring.
     */
    @Override
    protected void prepareToScoreWords() {
        stringsToScore.clear();
        for (int i = 0; i < wordsToScore.size(); i++) {
            Word w = wordsToScore.get(i);
            String s = w.getForm();
            // check for sem class replacement
            String scr = semClassReplacement(w);
            if (scr != null) s = scr;
            // add pitch accent and attrs, if any
            String pitchAccent = w.getPitchAccent();
            Iterator<Pair<String,String>> pairs = w.getAttrValPairs();
            if (pitchAccent != null || pairs.hasNext()) {
                StringBuffer sb = new StringBuffer();
                sb.append(s);
                if (pitchAccent != null) sb.append('_').append(pitchAccent);
                for (; pairs.hasNext(); ) {
                	Pair<String,String> p = pairs.next();
                    sb.append('_').append(p.b);
                }
                s = sb.toString().intern();
            }
            // check for unknown word
            if (openVocab && trieMapRoot.getChild(s) == null)
                s = "<unk>";
            // add key
            stringsToScore.add(s);
        }
    }
    
    /**
     * Returns the log prob of the ngram starting at the given index 
     * in wordsToScore and with the given order, with backoff. 
     * (Assumes words in wordsToScore have already been converted to strings in 
     * stringsToScore, via call to prepareToScoreWords.)
     */
    @Override
    protected float logProbFromNgram(int i, int order) {
        // skip initial start tag
        if (i == 0 && order == 1 && stringsToScore.get(0) == "<s>") return 0;
        // set keys list
        keysList.clear();
        for (int j = i; j < i+order; j++) {
            keysList.add(stringsToScore.get(j));
        }
        if (debugScore) {
            System.out.print("logp( " + keysList.get(order-1) + " | ");
            if (order > 1) { 
                System.out.print(keysList.get(order-2) + " ... ");
            }
            System.out.print(") = ");
        }
        // calc log prob
        float retval = logProb(0, order);
        if (debugScore) System.out.println("" + retval);
        return retval;
    }

}
