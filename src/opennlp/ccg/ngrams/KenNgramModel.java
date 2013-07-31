package opennlp.ccg.ngrams;
import opennlp.ccg.ngrams.kenlm.jni.KenLM;
import opennlp.ccg.ngrams.kenlm.MurmurHash;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import opennlp.ccg.lexicon.DefaultTokenizer;
import opennlp.ccg.lexicon.Tokenizer;
import opennlp.ccg.lexicon.Word;

public class KenNgramModel extends AbstractStandardNgramModel {
    
    // the nuts-n-bolts JNI class.
    private KenLM kenlm = null;

    // Map of hash of word to integer representation (integerized word used by KenLM).
    private Map<Long,Integer> hash2ID = new HashMap<Long,Integer>();
    // Map of String (word to score) to hash of word.
//    private Map<String,Integer> word2Hash = new HashMap<String,Integer>();
    // Map from hashes to tokens (to see whether we have encountered a new token). 
    private Map<Long,String> hash2String = new HashMap<Long,String>();
    // List of vocabulary tokens. New items are added and assigned their index as a representation.
    private List<String> vocabList = new ArrayList<String>();
    // Whether to lowercase text before querying to the language model (e.g., "Pierre Vinken" => "pierre vinken").
    private boolean lowercaseText = false;
    // Whether to split up named entities before querying the language model (e.g., "Pierre_Vinken" => "Pierre Vinken")
    private boolean splitNEs = false;
    // What character delimter to use to split NEs on.
    private char neDelim = '_';
    
    // A reusable container for scoring strings.
//    private List<String> someStringsToScore = null;

    // Whether to print out messages that trace the scoring process.
    public boolean debugScore = false;

    public KenNgramModel(int order, 
			 String lmFile,
			 boolean useSemClasses,
			 boolean lowercaseText,
			 boolean splitNEs,
			 char neDelim,
			 boolean useNgramFeatures) throws IOException {
	super(order, useSemClasses);
	this.lowercaseText = lowercaseText;
	this.splitNEs = splitNEs;
	this.neDelim = neDelim;
	kenlm = new KenLM(order, lmFile);	
//	someStringsToScore = new ArrayList<String>(order);
	this.useNgramFeatures = useNgramFeatures;
    }

    /** Construct with order and filename. (Delegates to superclass for these flags). */
    public KenNgramModel(int order, String lmFile) throws IOException { this(order, lmFile, false); }
    /** Construct with order and filename and an indication of whether to use semantic classes. (Delegates to superclass for these flags). */
    public KenNgramModel(int order, String lmFile, boolean useSemClasses) throws IOException {
	this(order, lmFile, useSemClasses, false, false, '_', false);
    }


    /**
     * Integerize a word and register it with the LM, if needed.
     */
    public int id(String token) {
	synchronized (this) {
	    long hash = 0;
	    try {
		hash = MurmurHash.hash64(token);
	    } catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	    }
	    String hash_word = hash2String.get(hash);
	    if (hash_word != null) {
		return hash2ID.get(hash);
	    } else {
		int id = vocabList.size();
		// let kenlm know about this word's ID.
		kenlm.registerWord(token, id);
		vocabList.add(token);
		hash2String.put(hash, token);
		hash2ID.put(hash, id);
		return id;
	    }
	}
    }

    /**
     * Lowercase each token, if desired, and split each token into a list of tokens
     * (splitting on NE delim token), if desired.
     */
    protected List<Word> splitAndLowercase(List<Word> words) {
	List<Word> tmp = new ArrayList<Word>(words.size());
	if(!(lowercaseText || splitNEs)) {
	    return words;
	} else {
	    for(Word w : words) {
		String wdString = w.getForm();
		String[] parts = wdString.replace(neDelim,' ').split("\\s+");
//		ArrayList<String> subTmp = new ArrayList<String>(parts.length);
		for(String part : parts) {
		    String newWdForm = (lowercaseText) ? part.toLowerCase() : part;
		    // add null attr/val list, since it is not accessible.
		    tmp.add(Word.createWord(newWdForm, w.getPitchAccent(), null, newWdForm, w.getPOS(), w.getSupertag(), w.getSemClass()));
		}
	    }
	    return tmp;
	}
    }

    /**
      * Resets wordsToScore to the given ones, reversing them when the reverse
      * flag is true, and adding sentence delimiters if not already present, when
      * the completeness flag is true. Delegates to the superclass 
      */
    @Override
    protected void setWordsToScore(List<Word> words, boolean complete) {
        wordsToScore.clear();
        tagsAdded = false; 
	List<Word> tmp = splitAndLowercase(words);
	words = tmp;
	super.setWordsToScore(words, complete);
    }


    /**
     * Calculates a log probability of a delineated substring of the strings
     * to score using KenLM. 
     * @param pos The start position (inclusive) within the strings to score.
     * @param len The length, starting from <null>pos</null>, of the string
     * that should be used.  
     */
    @Override
    public float logProb(int pos, int len) {
	try {
	    List<String> range = new ArrayList<String>(keysList.size());
	    for(Object wts : keysList.subList(pos, pos + len)) range.add((String)wts);

	    int rangeSize = range.size();
	    if(rangeSize == 0) {
		throw new IllegalArgumentException("empty range specified for log prob");
	    }

	    // Get hashes of words.
	    int[] wds = new int[range.size()];
	    int cursor = 0;
	    for(String s : range) wds[cursor++] = id(s);
	    
	    // call KenLM
	    float result = kenlm.prob(wds);
	    if(debugScore) {
		String wd = range.get(range.size()-1);
		String context = "";
		for(String contextWord : range.subList(0,range.size()-1)) context += " " + contextWord;
		context = context.trim();
		System.out.println("logp(" + wd + " | " + context + ") = " + result);
	    }
	    return result;
	} catch(IndexOutOfBoundsException e) {
	    return 0.0f;
	}
    }


    /** Test loading and scoring. */
    // NB: This produces the same scores as the SRILM ngram tool when both 
    //     <s> and </s> tags are used.
    public static void main(String[] args) throws IOException {
        
        String usage = "Usage: java opennlp.ccg.ngrams.KenLM <order> <lmfile> <tokens>";
        
        if (args.length > 0 && args[0].equals("-h")) {
            System.out.println(usage);
            System.exit(0);
        }
        
        long start = System.currentTimeMillis();
        String order = args[0]; String lmfile = args[1]; String tokens = args[2];
	boolean lowercase = true, splitNEs = false;

	// we want to prove that there are NEs to split and that there are uppercase chars to preserve.
	for(char c : tokens.toCharArray()) {
	    if (c == '_') {
		splitNEs = true;
		if (!lowercase) break;
	    }
	    
	    if (Character.isUpperCase(c)) {
		lowercase = false;
		if(splitNEs) break;
	    }
	}

        System.out.println("Loading n-gram model with order " + order + " from: " + lmfile);
        KenNgramModel lm = new KenNgramModel(Integer.parseInt(order), lmfile, false, lowercase, splitNEs, '_', false);
	lm.debugScore = true;
        int secs = (int) (System.currentTimeMillis() - start) / 1000;
        System.out.println("secs: " + secs);
        System.out.println();
        Tokenizer tokenizer = new DefaultTokenizer();
        List<Word> words = tokenizer.tokenize(tokens);
        System.out.println("scoring: " + tokens);
        System.out.println();
        lm.setWordsToScore(words, true);
        lm.prepareToScoreWords();
        double logprob = lm.logprob();
        double score = convertToProb(logprob);
        System.out.println();
        System.out.println("score: " + score);
        System.out.println("logprob: " + logprob);
	// Find out how many words there are here.
	int size = lm.splitAndLowercase(words).size();
        System.out.println("ppl: " + NgramScorer.convertToPPL(logprob / (size-1)));
    }
}
