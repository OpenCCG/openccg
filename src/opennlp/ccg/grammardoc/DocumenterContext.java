/*
 * $Id: DocumenterContext.java,v 1.2 2006/11/01 02:53:20 coffeeblack Exp $
 */
package opennlp.ccg.grammardoc;

import java.io.File;

/**
 * Provides a context inside which a {@link Documenter} will execute. This class
 * gives the documenter access to objects it will need to produce its
 * documentation.
 * 
 * @author Scott Martin (http://www.ling.osu.edu/~scott/)
 * @version $Revision: 1.2 $
 */
public interface DocumenterContext {

	/**
	 * Logs a message from a documenter.
	 */
	public void log(String message);

	/**
	 * Gets the target location where the documenter should generate its
	 * documentation files.
	 * 
	 * @return A directory that exists in a filesystem.
	 */
	public File getDestinationDirectory();
}
