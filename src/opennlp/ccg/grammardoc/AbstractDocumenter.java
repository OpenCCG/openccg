/*
 * $Id: AbstractDocumenter.java,v 1.2 2006/11/01 02:53:20 coffeeblack Exp $
 */
package opennlp.ccg.grammardoc;

/**
 * Convenience base class for documenters to extend
 * 
 * @author Scott Martin (http://www.ling.osu.edu/~scott/)
 * @version $Revision: 1.2 $
 */
public abstract class AbstractDocumenter implements Documenter {

	protected DocumenterContext documenterContext;
	protected String name;

	/**
	 * Default constructor. All documenters must have a no-argument constructor.
	 */
	protected AbstractDocumenter() {}

	/**
	 * Creates a new documenter with the specified name.
	 */
	protected AbstractDocumenter(String name) {
		this.name = name;
	}

	/**
	 * Sets this documenter's context.
	 */
	public void setDocumenterContext(DocumenterContext documenterContext) {
		this.documenterContext = documenterContext;
	}

	/**
	 * Gets this documenter's name.
	 */
	public String getName() {
		return name;
	}

}
