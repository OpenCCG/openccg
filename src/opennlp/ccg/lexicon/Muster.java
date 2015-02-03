package opennlp.ccg.lexicon;

import java.util.Map;

/**
 * A morphological/graphological/phonologial muster
 * 
 * @author Daniel Couto-Vale
 */
public class Muster {

	/**
	 * A muster sequence of characters
	 */
	private final String form;

	/**
	 * A muster sequence of pitches
	 */
	private final String tone;

	/**
	 * A parallel map
	 */
	private final Map<String, String> parallelMap;

	/**
	 * Constructoe
	 * 
	 * @param form the muster sequence of characters
	 * @param tone the muster sequence of pitches
	 * @param parallelMap parallel musters
	 */
	public Muster(String form, String tone, Map<String, String> parallelMap) {
		this.form = form;
		this.tone = tone;
		this.parallelMap = parallelMap;
	}

	/**
	 * @return the form to recognize
	 */
    public final String getForm() {
    	return form;
    }

    /**
     * @return the tone to recognize
     */
    public final String getTone() {
    	return tone;
    }

    /**
     * @return the parallel map
     */
    public final Map<String, String> getParallelMap() {
    	return parallelMap;
    }

    /**
     * Gets the parallel key
     * 
     * @param parallelKey the parallel key
     * @return the parallel
     */
    public String getParallel(String parallelKey) {
    	return parallelMap.get(parallelKey);
    }

}
