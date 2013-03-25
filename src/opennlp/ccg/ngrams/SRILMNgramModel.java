/*
 * $Id: SRILMNgramModel.java,v 1.5 2008/11/09 03:29:36 mwhite14850 Exp $ 
 */
package opennlp.ccg.ngrams;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import opennlp.ccg.lexicon.DefaultTokenizer;
import opennlp.ccg.lexicon.Tokenizer;
import opennlp.ccg.lexicon.Word;


/**
 * A language model that uses the
 * <acronym title="Stanford Research Institute">SRI</acronym> language modeling
 * toolkit.
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @see <a href="http://www.speech.sri.com/projects/srilm/">SRILM</a>
 * @version $Revision: 1.5 $
 * @since 0.9.2
 */
public class SRILMNgramModel extends AbstractStandardNgramModel {

	/**
	 * Load the binary, platform-dependent library containing the SRILM JNI
	 * bridge code. See ${OPENCCG_HOME}/src/srilmbridge.
	 * @throws UnsatisfiedLinkError If Java can't find the srilmbridge library.
	 */
	static {
		System.loadLibrary("srilmbridge");
	}
	
	/**
	 * Creates a SRILM language model with the specified ngram order and model
	 * type.
	 * @param order The ngram order to use.
	 * @param lmFile The file to read the langauge model from.
	 * @param useSemClasses Whether or not to use semantic classes.
	 * @param modelType The type of language model.
	 * @throws IOException If a problem occurs reading the language model file.
	 * These include non-existent or unreadable files, file format problems,
	 * etc. 
	 */
	public SRILMNgramModel(int order, File lmFile,
		boolean useSemClasses, SRILMNgramModelType modelType)
			throws IOException {
		super(order, useSemClasses);
		loadLMFromFile(order, lmFile, modelType);
	}

	/**
	 * Creates a new SRILM language model.
	 * @see SRILMNgramModel#SRILMNgramModel(int, File, boolean,
	 * SRILMNgramModelType)
	 */
	public SRILMNgramModel(int order, File lmFile,
		SRILMNgramModelType modelType)
			throws IOException {
		this(order, lmFile, false, modelType);
	}
	
	/**
	 * Loads an LM from a file.
	 * @param ngramOrder The ngram order to use.
	 * @param lmFile The file containing the language model.
	 * @param lmType The type of langauge model to expect.
	 * @throws IOException If the language model file is non-existent or
	 * <code>null</code>, or if a problem occurs loading or parsing the file.
	 */
	protected void loadLMFromFile(int ngramOrder, File lmFile, 
				SRILMNgramModelType lmType)
			throws IOException {
		if(lmFile == null) {
			throw new IOException("null file");
		}
		if(!lmFile.exists()) {
			throw new IOException("file does not exist: " + lmFile);
		}
		if(lmFile.isDirectory()) {
			throw new IOException("file is a directory: " + lmFile);
		}
		if(!lmFile.canRead()) {
			throw new IOException("unable to read file: " + lmFile);
		}
		
		loadLM(ngramOrder, lmFile.getAbsolutePath(), lmType.ordinal());
	}

	/**
	 * Calculates a log probability of a delineated substring of the strings
	 * to score using SRILM. This method reverses the context before passing
	 * the string to SRILM, as this is the format SRILM expects.
	 * @param pos The start position (inclusive) within the strings to score.
	 * @param len The length, starting from <null>pos</null>, of the string
	 * that should be used.  
	 */
	@Override
	public float logProb(int pos, int len) {
		try {
			// create new because reversing list affects keysList
			List<Object> range = keysList.subList(pos, pos + len);
			int rangeSize = range.size();
			if(rangeSize == 0) {
				throw new IllegalArgumentException(
						"empty range specified for log prob");
			}
			
			// only allocate context array if we have to
			String[] context = (rangeSize > 1)
				? new String[rangeSize - 1] : null;
			
			if(context != null) { // reverse for SRILM				
				ListIterator<Object> contextIterator
					= range.listIterator(rangeSize - 1);
				int i = 0;
				while(contextIterator.hasPrevious()) {
					context[i++] = contextIterator.previous().toString();
				}
			}
			
			// call SRILM to get word in reversed context
			return doLogProb(range.get(rangeSize - 1).toString(), context);
		}
		catch(IndexOutOfBoundsException e) {
			return 0.0f;
		}
	}

	/**
	 * Invokes SRILM to load a language model.
	 * @param ngramOrder The order of the language model
	 * @param fileAbsolutePath The absolute path of the file containing the
	 * language model.
	 * @param lmType The language model type.
	 * @throws IOException If a problem happens with SRILM while trying to
	 * load the language model.
	 */
	private native void loadLM(int ngramOrder, String fileAbsolutePath,
			int lmType)
		throws IOException;

	/**
	 * Invokes SRILM to calculate the log probability of a string in the
	 * given context. SRILM will make its calculations based on the language
	 * model loaded in {@link #loadLM(int, String, int)}.
	 * @param word The word to calculate a probability for.
	 * @param context The context, in reverse order. For example, to calculate
	 * the probability of the word &quot;rain&quot; in the context of the
	 * string &quot;in the rain&quot;, the context should be the array
	 * <code>{the, in}</code>. If the context is <code>null</code> or
	 * zero-length, SRILM will assume this means no context should be used.
	 * @return The log probability of the given word in the given (reversed)
	 * context, as determined by SRILM.
	 */
	private native float doLogProb(String word, String[] context);

	/**
	 * Invokes SRILM to clean up any initialized objects.
	 */
	@Override
	protected native void finalize() throws Throwable;
	
	public static void main(String[] args) throws Exception {
		String usage = "Usage: java opennlp.ccg.ngrams.SRILMNgramModel"
			+ " <order> <lmfile> <lmtype> <tokens> (-reverse)";
        
        if (args.length > 0 && args[0].equals("-h")) {
            System.out.println(usage);
            return;
        }
        
        long start = System.currentTimeMillis();
        String order = args[0];
        String lmfile = args[1];
        String lmType = args[2];
        String tokens = args[3];
        String reversed = (args.length >= 5 && args[4].equals("-reverse"))
        	? "reversed " : "";
        System.out.println("Loading " + reversed
        		+ "n-gram model with order " + order + " from: " + lmfile);
        SRILMNgramModel lm = new SRILMNgramModel(Integer.parseInt(order),
        		new File(lmfile), SRILMNgramModelType.valueOf(lmType));
        if (reversed.length() > 0) lm.setReverse(true);
        System.out.println("openVocab: " + lm.openVocab);
        int secs = (int) (System.currentTimeMillis() - start) / 1000;
        System.out.println("secs: " + secs);
        System.out.println();
        // System.out.println("trie map: ");
        // System.out.println(lm.trieMapRoot.toString());
        // System.out.println();
        
        Tokenizer tokenizer = new DefaultTokenizer();
        List<Word> words = tokenizer.tokenize(tokens);
        System.out.println("scoring: " + tokens);
        System.out.println();
        lm.debugScore = true;
        lm.setWordsToScore(words, true);
        lm.prepareToScoreWords();
        double logprob = lm.logprob();
        double score = convertToProb(logprob);
        System.out.println();
        System.out.println("score: " + score);
        System.out.println("logprob: " + logprob);
        System.out.println("ppl: " + NgramScorer.convertToPPL(logprob / (words.size()-1)));
	}
}
