package opennlp.ccg.lexicon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A morphological/graphological/phonologial muster
 * 
 * @author Daniel Couto-Vale
 */
public class Muster implements Comparable<Muster> {

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

	@Override
	public int compareTo(Muster o) {
		int compare = 0;
		compare = form.compareTo(o.form);
		if (compare != 0) return compare;
		compare = tone.compareTo(o.tone);
		if (compare != 0) return compare;
		compare = parallelMap.size() - o.parallelMap.size();
		if (compare != 0) return compare;
		List<String> keyList = new ArrayList<String>(parallelMap.keySet());
		List<String> oKeyList = new ArrayList<String>(o.parallelMap.keySet());
		Collections.sort(keyList);
		Collections.sort(oKeyList);
		for (int i = 0; i < keyList.size(); i++) {
			String key = keyList.get(i);
			String oKey = oKeyList.get(i);
			compare = key.compareTo(oKey);
			if (compare != 0) return compare;
			String value = parallelMap.get(key);
			String oValue = parallelMap.get(oKey);
			compare = value.compareTo(oValue);
			if (compare != 0) return compare;
		}
		return compare;
	}

}
