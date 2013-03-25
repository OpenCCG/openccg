/*
 * $Id: Documenter.java,v 1.2 2006/11/01 02:53:20 coffeeblack Exp $
 */
package opennlp.ccg.grammardoc;

/**
 * A documenter that produces documentation for an OpenCCG grammar.
 * 
 * @author Scott Martin (http://www.ling.osu.edu/~scott/)
 * @version $Revision: 1.2 $
 */
public interface Documenter {

	/**
	 * Gets the name of this documenter.
	 * 
	 * @return A string like <code>SGML Documenter</code>.
	 */
	public String getName();

	/**
	 * Sets this documenter's context.
	 * 
	 * @param documenterContext The context this documenter should use.
	 */
	public void setDocumenterContext(DocumenterContext documenterContext);

	/**
	 * Causes a documenter to produce documentation for the provided grammar.
	 * Before any calls to this method are made, the documenter will first be
	 * configured with a (single) call to
	 * {@link #setDocumenterContext(DocumenterContext)}.
	 * 
	 * @param grammar The grammar to document.
	 * @throws DocumenterException If any problems occur during the process of
	 *             generating documentation.
	 */
	public void document(SourceGrammar grammar) throws DocumenterException;
}
