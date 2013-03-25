/*
 * $Id: GrammarDocException.java,v 1.2 2006/11/01 02:53:20 coffeeblack Exp $
 */
package opennlp.ccg.grammardoc;

/**
 * An exception that occurs during the execution of {@link GrammarDoc}.
 * 
 * @author Scott Martin (http://www.ling.osu.edu/~scott/)
 * @version $Revision: 1.2 $
 */
public class GrammarDocException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception.
	 */
	public GrammarDocException() {
		super();
	}

	/**
	 * Creates a new exception with the specified message.
	 */
	public GrammarDocException(String message) {
		super(message);
	}

	/**
	 * Creates a new exception with the specified message and underlying cause.
	 */
	public GrammarDocException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new exception with the specified underlying cause.
	 */
	public GrammarDocException(Throwable cause) {
		super(cause);
	}

}
