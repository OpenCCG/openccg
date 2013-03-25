/*
 * $Id: DocumenterFactory.java,v 1.4 2006/12/11 18:19:24 coffeeblack Exp $
 */
package opennlp.ccg.grammardoc;

import java.util.EnumMap;
import java.util.Map;

/**
 * Factory class for creating documenters based on a predefined name.
 * 
 * @author Scott Martin (http://www.ling.osu.edu/~scott/)
 * @version $Revision: 1.4 $
 */
public class DocumenterFactory {

	private static final Map<DocumenterName, Documenter> documenters
		= new EnumMap<DocumenterName, Documenter>(DocumenterName.class);
	static final DocumenterFactory documenterFactory = new DocumenterFactory();

	private DocumenterFactory() {}

	/**
	 * Gets a new documenter factory.
	 */
	public static DocumenterFactory newInstance() {
		return documenterFactory;
	}

	/**
	 * Gets a new instance of the default documenter.
	 * 
	 * @return Calls {@link #newDocumenter(DocumenterName)} with
	 *         {@link DocumenterName#DEFAULT} as its argument.
	 * @throws DocumenterNotFoundException Does not throw this exception. This
	 *             is included for binary compatibility with
	 *             {@link #newDocumenter(DocumenterName)}.
	 */
	public Documenter newDocumenter() throws DocumenterNotFoundException {
		return newDocumenter(DocumenterName.DEFAULT);
	}

	/**
	 * Gets a new instance of the named documenter.
	 * 
	 * @param name Used to look up the documenter {@link DocumenterName name}.
	 * @return A documenter that corresponds to the specified name.
	 * @throws DocumenterNotFoundException If no documenter can be created for
	 *             the specified name.
	 */
	public Documenter newDocumenter(String name)
			throws DocumenterNotFoundException {
		try {
			DocumenterName nm = DocumenterName.valueOf(name);
			return newDocumenter(nm);
		}
		catch(IllegalArgumentException iae) {
			throw new DocumenterNotFoundException(name);
		}
	}

	/**
	 * Gets a new instance of the named documenter.
	 * 
	 * @param name The {@link DocumenterName name} of the documenter to create.
	 * @return A documenter that corresponds to the specified name.
	 * @throws DocumenterNotFoundException If no documenter can be created for
	 *             the specified name.
	 */
	public synchronized Documenter newDocumenter(DocumenterName name)
			throws DocumenterNotFoundException {
		Documenter d = documenters.get(name);

		if(d == null) {
			try {
				d = name.documenterClass.newInstance();
			}
			catch(InstantiationException ie) {
				throw new DocumenterNotFoundException(name, ie);
			}
			catch(IllegalAccessException iae) {
				throw new DocumenterNotFoundException(name, iae);
			}
			
			documenters.put(name, d);
		}

		return d;
	}
}
