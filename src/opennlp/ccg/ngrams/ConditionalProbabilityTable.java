///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2010 Michael White
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

import java.io.IOException;
import java.util.*;

import opennlp.ccg.lexicon.*;
import opennlp.ccg.util.*;

/**
 * A conditional probability table implemented via a FactoredNgramModelFamily, 
 * where probabilities are determined by n-grams of "factors" of a single "word" 
 * (whether the pairs of attributes and values have anything to do with words
 * or not).
 *
 * @author      Michael White
 * @version     $Revision: 1.2 $, $Date: 2010/02/25 22:26:10 $
 */
public class ConditionalProbabilityTable {
	
	/**
	 * The model, as a factored n-gram model family.
	 */
	protected FactoredNgramModelFamily model;
	
	/**
	 * Constructor with filename for model as a factored n-gram model family.
	 */
	public ConditionalProbabilityTable(String filename) throws IOException {
		model = new FactoredNgramModelFamily(filename, false);
		if (model.order != 1) {
			throw new RuntimeException("A conditional probability table must have n-gram order 1.");
		}
	}

    /** Sets the debug score flag. */
    public void setDebug(boolean debugScore) { model.setDebug(debugScore); }
    
	/**
	 * Returns a probability from the model for the given list of attribute-value 
	 * pairs, which are assumed to have already been interned, by converting 
	 * the result of the <code>logprob</code> method.
	 */
	public double score(List<Pair<String,String>> attrValList) {
		return NgramScorer.convertToProb(logprob(attrValList));
	}
	
	/**
	 * Returns a log probability from the model for the given list of attribute-value 
	 * pairs, which are assumed to have already been interned.
	 * The order of the list does not matter because the model probabilities are 
	 * defined by the factored n-gram model family specification.
	 */
	public double logprob(List<Pair<String,String>> attrValList) {
		return model.logprob(new SingletonList<Word>(new ListPairWord(attrValList)));
	}
	
    /** Tests loading and scoring. */
    public static void main(String[] args) throws IOException {
        
        String usage = "Usage: java opennlp.ccg.ngrams.ConditionalProbabilityTable <specfile> <string of attr-val pairs>";
        
        if (args.length > 0 && args[0].equals("-h")) {
            System.out.println(usage);
            System.exit(0);
        }
        
        String specfile = args[0];
        String tokens = args[1];
        
        List<Pair<String,String>> pairs = new ArrayList<Pair<String,String>>();
        String[] tokenArray = tokens.split("\\s+");
        for (int i=0; i < tokenArray.length; i+=2) {
        	String attr = tokenArray[i].intern();
        	String val = tokenArray[i+1].intern();
        	pairs.add(new Pair<String,String>(attr, val));
        }
        
        System.out.println("Loading conditional probability table from: " + specfile);
        ConditionalProbabilityTable table = new ConditionalProbabilityTable(specfile);
        FactoredNgramModelFamily lmFamily = table.model;
        System.out.println("primary child var: " + lmFamily.primaryGroup.childName);
        if (lmFamily.furtherGroups != null) {
            for (int i = 0; i < lmFamily.furtherGroups.length; i++) {
                System.out.println("further child var: " + lmFamily.furtherGroups[i].childName);
            }
        }
        System.out.println("openVocab: " + lmFamily.openVocab);
        System.out.println();
        
        System.out.println("scoring: " + tokens);
        System.out.println();
        table.setDebug(true);
        double logprob = table.logprob(pairs);
        double score = NgramScorer.convertToProb(logprob);
        System.out.println("score: " + score);
        System.out.println("logprob: " + logprob);
    }
}
