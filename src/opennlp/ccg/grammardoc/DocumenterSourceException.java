/*
 * $Id: DocumenterSourceException.java,v 1.2 2006/11/01 02:53:20 coffeeblack Exp $
 */
package opennlp.ccg.grammardoc;

/**
 * An exception thrown by a {@link Documenter} because of a problem in the
 * source grammar. This exception tracks the
 * {@link SourceGrammarFile source grammar file} where the problem occurred.
 * 
 * @author Scott Martin (http://www.ling.osu.edu/~scott/)
 * @version $Revision: 1.2 $
 */
public class DocumenterSourceException extends DocumenterException {

	private static final long serialVersionUID = 1L;

	protected SourceGrammarFile sourceGrammarFile;

	/**
	 * Creates a new exception signaling a problem in the specified source
	 * grammar file.
	 */
	public DocumenterSourceException(SourceGrammarFile sourceGrammarFile) {
		this.sourceGrammarFile = sourceGrammarFile;
	}

	/**
	 * Creates a new exception with the specified message, signaling a problem
	 * in the specified source grammar file.
	 */
	public DocumenterSourceException(String message,
			SourceGrammarFile sourceGrammarFile) {
		super(message);
		this.sourceGrammarFile = sourceGrammarFile;
	}

	/**
	 * Creates a new exception with the specified message and underlying cause,
	 * signaling a problem in the specified source grammar file.
	 */
	public DocumenterSourceException(String message, Throwable cause,
			SourceGrammarFile sourceGrammarFile) {
		super(message, cause);
		this.sourceGrammarFile = sourceGrammarFile;
	}

	/**
	 * Creates a new exception with the specified underlying cause, signaling a
	 * problem in the specified source grammar file.
	 */
	public DocumenterSourceException(Throwable cause,
			SourceGrammarFile sourceGrammarFile) {
		super(cause);
		this.sourceGrammarFile = sourceGrammarFile;
	}

	/**
	 * Gets the source grammar file where this problem occurred.
	 */
	public SourceGrammarFile getSourceGrammarFile() {
		return sourceGrammarFile;
	}

}
