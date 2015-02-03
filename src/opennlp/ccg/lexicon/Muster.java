package opennlp.ccg.lexicon;

/**
 * A morphological/phonologial/graphological muster.
 * 
 * @author Daniel Couto-Vale
 */
public class Muster implements Comparable<Muster> {

	private final static String emptyString = "";

	/**
	 * A muster sequence of characters
	 */
	private final String form;

	/**
	 * A muster sequence of pitches
	 */
	private final String tone;

	/**
	 * A muster sequence of caps
	 */
	private final String caps;

	/**
	 * Constructor
	 * 
	 * @param form the muster sequence of characters
	 * @param tone the muster sequence of pitches
	 * @param caps the muster sequence of caps
	 */
	public Muster(String form, String tone, String caps) {
		this.form = form.intern();
		this.tone = tone != null ? tone.intern() : emptyString;
		this.caps = caps != null ? caps.intern() : emptyString;
	}

	/**
	 * Constructor
	 * 
	 * @param form the muster sequence of characters
	 * @param tone the muster sequence of pitches
	 * @param caps the muster sequence of caps
	 */
	public Muster(String form) {
		this(form, null, null);
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
	 * @return the caps
	 */
	public final String getCaps() {
		return caps;
	}

	@Override
	public int compareTo(Muster o) {
		if (form != o.form) {
			return form.compareTo(o.form);
		} else if (tone != o.tone){
			return tone.compareTo(o.tone);
		} else if (caps != o.caps) {
			return caps.compareTo(o.caps);
		} else {
			return 0;
		}
	}

}
