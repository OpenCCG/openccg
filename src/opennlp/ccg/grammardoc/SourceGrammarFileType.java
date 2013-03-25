/*
 * $Id: SourceGrammarFileType.java,v 1.3 2007/05/02 21:51:35 coffeeblack Exp $
 */
package opennlp.ccg.grammardoc;

/**
 * File types for source grammars.
 * 
 * @author Scott Martin (http://www.ling.osu.edu/~scott/)
 * @version $Revision: 1.3 $
 */
public enum SourceGrammarFileType {
	GRAMMAR("grammar"),
	LEXICON("lexicon"),
	MORPHOLOGY("morph"),
	RULES("rules"),
	TYPES("types", false),
	DOCUMENTATION("documentation", false);
	
	final String fileName;
	final boolean required;
	
	private SourceGrammarFileType(String fileName) {
		this(fileName, true);
	}
	
	private SourceGrammarFileType(String fileName, boolean required) {
		this.fileName = fileName;
		this.required = required;
	}
	
	/**
	 * Gets the file name associated with this file type.
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Tests whether or not this grammar file name is required.
	 * @return true iff this grammar file type is required to be present in a
	 * grammar.
	 */
	public boolean isRequired() {
		return required;
	}
}