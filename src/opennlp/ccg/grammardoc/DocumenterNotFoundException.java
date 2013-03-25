/*
 * $Id: DocumenterNotFoundException.java,v 1.3 2006/12/11 18:19:24 coffeeblack Exp $
 */
package opennlp.ccg.grammardoc;

/**
 * Signals that no documenter could be found for a given name.
 * 
 * @author Scott Martin (http://www.ling.osu.edu/~scott/)
 * @version $Revision: 1.3 $
 */
public class DocumenterNotFoundException extends DocumenterException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception indicating that a documenter with the specified
	 * name was not found.
	 */
	public DocumenterNotFoundException(DocumenterName name) {
		this(name.name());
	}

	/**
	 * Creates a new exception indicating that a documenter with the specified
	 * name was not found.
	 */
	public DocumenterNotFoundException(String name) {
		super(name);
	}

	/**
	 * Creates a new exception indicating that a documenter with the specified
	 * name was not found for the specified underlying reason.
	 */
	DocumenterNotFoundException(DocumenterName name, Throwable cause) {
		super(name.name(), cause);
	}
}
