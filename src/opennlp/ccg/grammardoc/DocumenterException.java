/*
 * $Id: DocumenterException.java,v 1.2 2006/11/01 02:53:20 coffeeblack Exp $
 */
package opennlp.ccg.grammardoc;

/**
 * An exception thrown by a {@link Documenter}.
 * 
 * @author Scott Martin (http://www.ling.osu.edu/~scott/)
 * @version $Revision: 1.2 $
 */
public class DocumenterException extends GrammarDocException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new documenter exception.
	 */
	public DocumenterException() {
		super();
	}

	/**
	 * Creates a new exception with the specified message.
	 */
	public DocumenterException(String message) {
		super(message);
	}

	/**
	 * Creates a new exception with the specified message and underlying cause.
	 */
	public DocumenterException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new exception with the specified underlying cause.
	 */
	public DocumenterException(Throwable cause) {
		super(cause);
	}

}
