///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004-5 University of Edinburgh (Michael White)
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

import opennlp.ccg.lexicon.*;
import opennlp.ccg.perceptron.Alphabet;
import opennlp.ccg.perceptron.FeatureMap;

import java.util.*;
import java.io.*;

/**
 * A scorer consisting of a family of factored n-gram backoff models.
 * The family of models is specified using the factored language model specification 
 * file format given as input to the SRILM version 1.4.1 fngram-count tool.
 * Each individual model is loaded as a FactoredNgramModel instance, and thus 
 * only static backoff orders are supported at present, with 
 * the most distant parent variable dropped at each backoff point.
 * The first model in the specification file should be the primary one.
 * It may be followed by any number of models for the same child variable 
 * but with lower history orders; these models will be used when the 
 * full history is unavailable, if their order matches the available 
 * number of words in the history.  For example, if the primary model is 
 * a trigram model, then a bigram model may also be given (with a potentially 
 * different backoff order) for scoring bigram word sequences.
 * Following these models, there may also be further (sequences of) models for scoring 
 * different child variables.  If present, the scores calculated for these 
 * child variables will be multipled with the score calculated for the primary 
 * model's child variable (typically the word form).  For example, one or more 
 * models may be given to calculate the probability of the word's pitch accent, 
 * independently of the word form (though potentially looking at some of the same history).
 * With each individual model, the parents are assumed to be listed in backoff order.
 * Unknown words are mapped to &lt;unk&gt; if the latter is present in 
 * the first model.
 *
 * @author      Michael White
 * @version     $Revision: 1.15 $, $Date: 2010/02/25 22:26:11 $
 */
public class FactoredNgramModelFamily extends NgramScorer
{
    /** The primary model group. */
    public final ModelGroup primaryGroup;
    
    /** Any additional model groups, for different child variables. */
    public final ModelGroup[] furtherGroups;
    
    /** A factored n-gram model and any secondary ones for the same child variable. */
    public class ModelGroup {
        /** The child variable name. */
        public final String childName;
        /** The primary model. */
        public final FactoredNgramModel primaryModel;
        /** The secondary models. */
        public final FactoredNgramModel[] secondaryModels;
        /** Makes a model group from the given primary model. */
        public ModelGroup(FactoredNgramModel primaryModel, FactoredNgramModel[] secondaryModels) {
            this.childName = primaryModel.child.name;
            this.primaryModel = primaryModel;
            this.secondaryModels = secondaryModels;
        }
        /** Returns the appropriate model for the given order. */
        public FactoredNgramModel getModel(int order) {
            if (secondaryModels == null) return primaryModel;
            for (int i = 0; i < secondaryModels.length; i++) {
                if (secondaryModels[i].order == order) return secondaryModels[i];
            }
            return primaryModel;
        }
        /** Propagates the reverse flag. */
        public void setReverse(boolean reverse) { 
            primaryModel.setReverse(reverse); 
            if (secondaryModels == null) return;
            for (int i = 0; i < secondaryModels.length; i++) {
                secondaryModels[i].setReverse(reverse);
            }
        }
        /** Propagates the debug score flag. */
        public void setDebug(boolean debugScore) { 
            primaryModel.setDebug(debugScore); 
            if (secondaryModels == null) return;
            for (int i = 0; i < secondaryModels.length; i++) {
                secondaryModels[i].setDebug(debugScore);
            }
        }
        /** Propagates wordsToScore to the given list, for sharing purposes. */
        protected void shareWordsToScore(List<Word> wordsToScore) {
            primaryModel.shareWordsToScore(wordsToScore);
            if (secondaryModels == null) return;
            for (int i = 0; i < secondaryModels.length; i++) {
                secondaryModels[i].shareWordsToScore(wordsToScore);
            }
        }
    	/** Sets the alphabet. */
    	public void setAlphabet(Alphabet alphabet) {
    		primaryModel.setAlphabet(alphabet);
            if (secondaryModels == null) return;
            for (int i = 0; i < secondaryModels.length; i++) {
                secondaryModels[i].setAlphabet(alphabet);
            }
    	}
    }
    
    
    /** 
     * Loads a family of factored n-gram models
     * from the file with the given name, in the SRILM format.
     * The flag for using sem classes is defaulted to true.
     */
    public FactoredNgramModelFamily(String filename) throws IOException { 
        this(filename, true);
    }
    
    /** 
     * Loads a family of factored n-gram models
     * from the file with the given name, in the SRILM format, 
     * and with the given flag for using sem classes.
     */
    public FactoredNgramModelFamily(String filename, boolean useSemClasses) throws IOException {
        this.useSemClasses = useSemClasses;
        List<ModelGroup> modelGroups = readModel(filename);
        this.primaryGroup = modelGroups.get(0);
        if (modelGroups.size() == 1) this.furtherGroups = null;
        else {
            this.furtherGroups = new ModelGroup[modelGroups.size()-1];
            for (int i = 1; i < modelGroups.size(); i++) {
                this.furtherGroups[i-1] = modelGroups.get(i);
            }
        }
        order = primaryGroup.primaryModel.order;
        openVocab = primaryGroup.primaryModel.openVocab; 
    }
    
    
    /** Sets reverse flag, and propagates to component models. */
    public void setReverse(boolean reverse) { 
        super.setReverse(reverse);
        primaryGroup.setReverse(reverse);
        if (furtherGroups == null) return;
        for (int i = 0; i < furtherGroups.length; i++) {
            furtherGroups[i].setReverse(reverse);
        }
    }
    
    /** Sets debug score flag, and propagates to component models. */
    public void setDebug(boolean debugScore) { 
        super.setDebug(debugScore);
        primaryGroup.setDebug(debugScore);
        if (furtherGroups == null) return;
        for (int i = 0; i < furtherGroups.length; i++) {
            furtherGroups[i].setDebug(debugScore);
        }
    }
    

    /** Sets wordsToScore to the given list, for sharing purposes. */
    protected void shareWordsToScore(List<Word> wordsToScore) {
        this.wordsToScore = wordsToScore;
        primaryGroup.shareWordsToScore(wordsToScore);
        if (furtherGroups == null) return;
        for (int i = 0; i < furtherGroups.length; i++) {
            furtherGroups[i].shareWordsToScore(wordsToScore);
        }
    }

    
	/** Sets the alphabet. */
	public void setAlphabet(Alphabet alphabet) {
		super.setAlphabet(alphabet);
		primaryGroup.setAlphabet(alphabet);
        if (furtherGroups == null) return;
        for (int i = 0; i < furtherGroups.length; i++) {
        	furtherGroups[i].setAlphabet(alphabet);
        }
	}
	
	/**
	 * Increments ngram counts for the ngrams starting at the given index in
	 * wordsToScore and with the given order.
	 */
	protected void incNgrams(FeatureMap featmap, int i, int order) {
        // do primary group
        List<String> ngram = ngram(primaryGroup, i, order);
        if (ngram != null) {
	        Alphabet.Feature f = alphabet.index(ngram);
			if (f != null) featmap.inc(f);
        }
        // then any further ones
        if (furtherGroups != null) {
            for (int j = 0; j < furtherGroups.length; j++) {
                List<String> ngram2 = ngram(furtherGroups[j], i, order);
                if (ngram2 == null) continue;
                Alphabet.Feature f2 = alphabet.index(ngram2);
        		if (f2 != null) featmap.inc(f2);
            }
        }
	}
	
    // get ngram from a model group
    private List<String> ngram(ModelGroup modelGroup, int i, int order) {
        FactoredNgramModel modelToUse = modelGroup.primaryModel; 
        // with less than full history, get possibly different model to use
        if (order < modelToUse.order) {
            modelToUse = modelGroup.getModel(order); 
        }
        return modelToUse.ngram(i, order);
    }
    
    
    /** Returns the log prob of the ngram starting at the given index 
        in wordsToScore and with the given order, with backoff. */
    protected float logProbFromNgram(int i, int order) {
        float logProbTotal = 0;
        // do primary group
        logProbTotal += logProbFromNgram(primaryGroup, i, order);
        // then any further ones
        if (furtherGroups != null) {
            for (int j = 0; j < furtherGroups.length; j++) 
                logProbTotal += logProbFromNgram(furtherGroups[j], i, order);
        }
        return logProbTotal;
    }
    
    // calculate the log prob from a model group
    private float logProbFromNgram(ModelGroup modelGroup, int i, int order) {
        FactoredNgramModel modelToUse = modelGroup.primaryModel; 
        // with less than full history, get possibly different model to use
        if (order < modelToUse.order) {
            modelToUse = modelGroup.getModel(order); 
            if (debugScore && modelToUse != modelGroup.primaryModel) {
                int modelNum = Arrays.asList(modelGroup.secondaryModels).indexOf(modelToUse);
                System.out.print("[2ndary model " + modelNum + "] ");
            }
        }
        return modelToUse.logProbFromNgram(i, order);
    }
    
    /** The max number of tokens to allow per line in the spec file. */
    public static int MAX_TOKENS_PER_LINE = 64;
    
    // reads in model, returning model groups
    private List<ModelGroup> readModel(String filename) throws IOException {
        // setup
        File infile = new File(filename);
        Reader in = new BufferedReader(new FileReader(infile));
        StreamTokenizer tokenizer = initTokenizer(in); 
        String[] tokens = new String[MAX_TOKENS_PER_LINE];
        // read in models
        FactoredNgramModel[] models = null;
        int numModels = -1;
        int currentModel = 0;
        // loop through lines
        while (tokenizer.ttype != StreamTokenizer.TT_EOF) {
            // read line into tokens
            readLine(tokenizer, tokens);
            // check for blank line
            if (tokens[0] == null) continue;
            // check for comment
            if (tokens[0].charAt(0) == '#') continue;
            // read num models, if not yet found
            if (numModels < 0) {
                numModels = Integer.parseInt(tokens[0]);
                models = new FactoredNgramModel[numModels];
                continue;
            }
            // skip rest if already read in numModels
            if (currentModel >= numModels) break;
            // read model spec if second token is a colon
            // line format is <child> : <numParents> <parent1> ... <parentN> <countfile> <lmfile> <numbognodes>
            if (tokens[1] != null && tokens[1].equals(":")) {
                // read child
                String child = tokens[0];
                // read parents
                int numParents = Integer.parseInt(tokens[2]);
                String[] parents = new String[numParents];
                for (int i = 0; i < numParents; i++) parents[i] = tokens[i+3];
                // read lm filename (skipping count file name)
                String lmfn = tokens[numParents+4];
                // make filename relative to spec file dir
                File lmfile = new File(infile.getParentFile(), lmfn);
                lmfn = lmfile.getPath();
                // load current model
            	models[currentModel] = new FactoredNgramModel(child, parents, lmfn, useSemClasses);
                // share wordsToScore
                models[currentModel].shareWordsToScore(wordsToScore);
                // inc current model
                currentModel++;
            }
        }
        // ensure models found
        if (models == null) {
            throw new IOException("No models found in: " + filename);
        }
        // check num models
        int actualNumModels = 0;
        for (int i = 0; i < numModels; i++) {
            if (models[i] != null) actualNumModels++;
        }
        if (actualNumModels != numModels) {
            System.err.println("Warning: Only found " + actualNumModels + "/" + numModels + " in " + filename);
            numModels = actualNumModels;
        }
        // assign models to groups
        List<ModelGroup> modelGroups = new ArrayList<ModelGroup>();
        int modelIndex = 0;
        while (modelIndex < numModels) {
            // get primary model, remember child name
            FactoredNgramModel primaryModel = models[modelIndex];
            String childName = primaryModel.child.name;
            modelIndex++;
            List<FactoredNgramModel> secondaryModelsList = new ArrayList<FactoredNgramModel>();
            // get secondary models with same child name
            while (modelIndex < numModels && models[modelIndex].child.name == childName) {
                secondaryModelsList.add(models[modelIndex]);
                modelIndex++;
            }
            FactoredNgramModel[] secondaryModels = new FactoredNgramModel[secondaryModelsList.size()];
            secondaryModelsList.toArray(secondaryModels);
            // make, add model group
            modelGroups.add(new ModelGroup(primaryModel, secondaryModels));
        }
        // done
        return modelGroups;
    }

    
    /** Test loading and scoring. */
    public static void main(String[] args) throws IOException {
        
        String usage = "Usage: java opennlp.ccg.ngrams.FactoredNgramModelFamily <specfile> <tokens>";
        
        if (args.length > 0 && args[0].equals("-h")) {
            System.out.println(usage);
            System.exit(0);
        }
        
        String specfile = args[0];
        String tokens = args[1];
        
        System.out.println("Loading n-gram model family from: " + specfile);
        FactoredNgramModelFamily lmFamily = new FactoredNgramModelFamily(specfile); 
        System.out.println("primary child var: " + lmFamily.primaryGroup.childName);
        if (lmFamily.furtherGroups != null) {
            for (int i = 0; i < lmFamily.furtherGroups.length; i++) {
                System.out.println("further child var: " + lmFamily.furtherGroups[i].childName);
            }
        }
        System.out.println("order: " + lmFamily.order);
        System.out.println("openVocab: " + lmFamily.openVocab);
        System.out.println();
        
        Tokenizer tokenizer = new DefaultTokenizer();
        List<Word> words = tokenizer.tokenize(tokens, true);
        System.out.println("scoring: ");
        for (int i = 0; i < words.size(); i++) {
            System.out.println(words.get(i).toString());
        }
        System.out.println();
        lmFamily.setDebug(true);
        lmFamily.setWordsToScore(words, true);
        lmFamily.prepareToScoreWords();
        double logprob = lmFamily.logprob();
        double score = convertToProb(logprob);
        System.out.println("score: " + score);
        System.out.println("logprob: " + logprob);
        System.out.println("ppl: " + NgramScorer.convertToPPL(logprob / (words.size()-1)));
    }
}
