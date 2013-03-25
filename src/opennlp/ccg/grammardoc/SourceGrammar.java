/*
 * $Id: SourceGrammar.java,v 1.4 2007/03/19 17:45:35 coffeeblack Exp $
 */
package opennlp.ccg.grammardoc;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents an OpenCCG grammar that is specified in a series of XML files.
 * 
 * @author Scott Martin (http://www.ling.osu.edu/~scott/)
 * @version $Revision: 1.4 $
 */
public class SourceGrammar {

	File sourceDirectory;
	Map<SourceGrammarFileType, SourceGrammarFile> sourceFiles;

	SourceGrammar(File sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
		sourceFiles = new EnumMap<SourceGrammarFileType, SourceGrammarFile>
			(SourceGrammarFileType.class);
	}

	/**
	 * @return Returns the sourceDirectory.
	 */
	public File getSourceDirectory() {
		return sourceDirectory;
	}

	/**
	 * Gets the {@link SourceGrammarFileType file names} found in this source
	 * grammar.
	 */
	public Set<SourceGrammarFileType> getSourceGrammarFileTypes() {
		return sourceFiles.keySet();
	}

	/**
	 * Gets a source grammar file based on a specified
	 * {@link SourceGrammarFileType file name}.
	 * 
	 * @param fileType The file name identifying the desired
	 *            {@link SourceGrammarFile source grammar file}.
	 */
	public SourceGrammarFile getSourceGrammarFile(
			SourceGrammarFileType fileType) {
		return sourceFiles.get(fileType);
	}

	void addSourceGrammarFile(SourceGrammarFileType fileType,
			SourceGrammarFile sourceGrammarFile) {
		sourceFiles.put(fileType, sourceGrammarFile);
	}

}
