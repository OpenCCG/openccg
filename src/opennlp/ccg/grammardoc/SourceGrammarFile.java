/*
 * $Id: SourceGrammarFile.java,v 1.4 2007/05/30 22:53:17 coffeeblack Exp $
 */
package opennlp.ccg.grammardoc;

import java.io.File;

import org.w3c.dom.Document;

/**
 * A file found in a source grammar. This class encapsulates the predefined
 * {@link SourceGrammarFileType file name} and {@link Document DOM document}.
 * 
 * @author Scott Martin (http://www.ling.osu.edu/~scott/)
 * @version $Revision: 1.4 $
 */
public class SourceGrammarFile {

	SourceGrammarFileType fileType;
	File sourceFile;

	/**
	 * Creates a new source grammar file.
	 */
	SourceGrammarFile(SourceGrammarFileType fileName, File sourceFile) {
		this.fileType = fileName;
		this.sourceFile = sourceFile;
	}

	/**
	 * @return Returns the fileType.
	 */
	public SourceGrammarFileType getFileType() {
		return fileType;
	}

	/**
	 * @return Returns the source file.
	 */
	public File getSourceFile() {
		return sourceFile;
	}

	/**
	 * Gets a string version of this source grammar file.
	 * 
	 * @return The value of this grammar file's
	 *         {@link SourceGrammarFileType#toString() file type}.
	 */
	@Override
	public String toString() {
		return fileType.toString();
	}

}
