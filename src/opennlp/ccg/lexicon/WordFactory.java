package opennlp.ccg.lexicon;

import java.util.List;

import opennlp.ccg.util.Pair;

/**
 * A word factory interface.
 * 
 * @author Daniel Couto-Vale
 */
public interface WordFactory {

	/**
	 * Creates a sufrace word with the given interned form
	 * 
	 * @param form the form to intern
	 * @return the word
	 */
    public Word create(String form);

    /**
     * Creates a surface or full word with the given normalized parallel value. The parallel keys
     * Tokenizer.FORM_ASSOCIATE... Tokenizer.ENTITY_CLASS_ASSOCIATE can be used to create a muster
     * with form... entity class.
     *   
     * @param associateKey
     * @param associateKey
     * @return the word
     */
    public Word create(String associateKey, String associateValue);

    /**
     * Creates a (surface or full) word with canonical associates.
     */
    public Word create(String form, String tone, List<Pair<String,String>> parallelPairs, 
        String stem, String POS, String supertag, String semClass 
    );

}