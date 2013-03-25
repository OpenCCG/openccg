/*
 * $Id: DocumenterName.java,v 1.3 2006/12/11 18:19:24 coffeeblack Exp $
 */
package opennlp.ccg.grammardoc;

import opennlp.ccg.grammardoc.html.HTMLDocumenter;

/**
 * The known documenters that the {@link DocumenterFactory documenter factory}
 * is aware of. Calling {@link DocumenterFactory#newDocumenter(DocumenterName)}
 * with any of these values will return a valid documenter without throwing an
 * exception.
 * 
 * @author Scott Martin (http://www.ling.osu.edu/~scott/)
 * @version $Revision: 1.3 $
 */
public enum DocumenterName {
	/**
	 * The default documenter.
	 */
	DEFAULT(HTMLDocumenter.class),

	/**
	 * A documenter that produces HTML output.
	 */
	HTML(HTMLDocumenter.class);
	
	Class<? extends Documenter> documenterClass;
	
	private DocumenterName(Class<? extends Documenter> documenterClass) {
		this.documenterClass = documenterClass;
	}
}
