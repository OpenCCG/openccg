/*
 * $Id: GrammarDoc.java,v 1.7 2007/05/30 22:53:17 coffeeblack Exp $
 */
package opennlp.ccg.grammardoc;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class that implements the <code>grammardoc</code> ant task.
 * 
 * @author Scott Martin (http://www.ling.osu.edu/~scott/)
 * @version $Revision: 1.7 $
 */
public class GrammarDoc extends Task implements DocumenterContext {

	private File srcDir, destDir;

	/**
	 * Executes the grammardoc task. This method finds the specified
	 * <code>srcDir</code> and <code>destDir</code>, then invokes a
	 * {@link Documenter documenter} with those parameters.
	 * 
	 * @throws BuildException If a source directory or destination directory is
	 *             not specified, or the source directory does not exist.
	 */
	@Override
	public void execute() throws BuildException {
		if(srcDir == null) {
			srcDir = new File(System.getProperty("user.dir"));
		}
		
		if(!srcDir.exists()) {
			throw new BuildException("Source directory does not exist");
		}
		
		if(destDir == null) {
			destDir = new File(srcDir, "docs");
		}

		if(!destDir.exists()) {
			log("Creating directory " + destDir);
			destDir.mkdirs();
		}

		try {
			Documenter documenter = DocumenterFactory.newInstance()
					.newDocumenter();

			log("Using " + documenter.getName());
			log("Documenting " + srcDir.getAbsolutePath());
			log("Generating documentation to " + destDir);

			documenter.setDocumenterContext(this);
			documenter.document(loadSourceGrammar());
			log("Done");
		}
		catch(DocumenterNotFoundException dnfe) {
			throw new BuildException("documenter not found: "
					+ dnfe.getMessage(), getLocation());
		}
		catch(DocumenterSourceException dse) {
			throw new BuildException("problem in source file "
					+ dse.getSourceGrammarFile() + ": " + dse.getMessage(),
					getLocation());
		}
		catch(DocumenterException de) {
			throw new BuildException("problem documenting: " + de.getMessage(),
					de);
		}
		catch(GrammarDocException gde) {
			throw new BuildException(gde.getMessage(), getLocation());
		}
	}

	SourceGrammar loadSourceGrammar() throws GrammarDocException {
		SourceGrammar sourceGrammar = new SourceGrammar(srcDir);

		try {
			SourceGrammarFile grammar = loadGrammarFile(
					SourceGrammarFileType.GRAMMAR,
					new File(srcDir, SourceGrammarFileType.GRAMMAR.fileName
							+ ".xml"));
			sourceGrammar.addSourceGrammarFile(SourceGrammarFileType.GRAMMAR,
					grammar);
			//TODO make these StreamSource instead
			File gd = grammar.sourceFile;
			
			for(SourceGrammarFileType fileType
					: SourceGrammarFileType.values()) {
				if(!fileType.equals(SourceGrammarFileType.GRAMMAR)) { // already
					DocumentBuilder db = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
					Document doc = db.parse(gd);
					
					NodeList fileEls = doc.getElementsByTagName(
							fileType.name().toLowerCase());
					if(fileEls.getLength() == 0) {
						if(fileType.isRequired()) {
							throw new GrammarDocException(
								"file type required but missing: " + fileType);
						}
					}
					else {
						sourceGrammar.addSourceGrammarFile(fileType,
								loadGrammarFile(fileType, 
									new File(srcDir,
											fileType.fileName + ".xml")));
					}
				}
			}
		}
		catch(ParserConfigurationException pce) {
			throw new GrammarDocException("parser configuration problem: "
					+ pce.getMessage(), pce);
		}
		catch(SAXException se) {
			throw new GrammarDocException("problem parsing source files: "
					+ se.getMessage(), se);
		}
		catch(IOException io) {
			throw new GrammarDocException("io problem with source files: "
					+ io.getMessage(), io);
		}

		return sourceGrammar;
	}
	
	SourceGrammarFile loadGrammarFile(SourceGrammarFileType fileType, File file) 
			throws GrammarDocException {
		if(!file.exists()) {			
			throw new GrammarDocException("file " + file.getName()
					+ " does not exist in " + srcDir);
		}
		else if(file.isDirectory()) {
			throw new GrammarDocException(file.getName()
					+ " refers to a directory in " + srcDir);
		}
		else {
			log("Loading " + file.getName());

			try {
				return new SourceGrammarFile(fileType, file);
			}
			catch(Exception e) {
				throw new GrammarDocException("problem parsing "
						+ file + ": " + e.getMessage(), e);
			}
		}
	}

	/**
	 * For conformance with {@link DocumenterContext}.
	 */
	public File getDestinationDirectory() {
		return getDestDir();
	}

	/**
	 * @return Returns the destDir.
	 */
	public File getDestDir() {
		return destDir;
	}

	/**
	 * @param destDir The destDir to set.
	 */
	public void setDestDir(File destDir) {
		this.destDir = destDir.getAbsoluteFile();
	}

	/**
	 * @return Returns the sourceDirectory.
	 */
	public File getSrcDir() {
		return srcDir;
	}

	/**
	 * @param srcDir The sourceDirectory to set.
	 */
	public void setSrcDir(File srcDir) {
		this.srcDir = srcDir.getAbsoluteFile();
	}
	
	public static void main(String[] args) {
		List<String> arguments = Arrays.asList(args);
		PrintStream out = System.out;
		GrammarDoc gd = new CommandGrammarDoc(out);
		
		if(arguments.contains("-h") || arguments.contains("--help")) {
			out.println("usage: ccg-grammardoc [-s|--source sourceDir] "
				+ "[-d|--dest destDir]");
		}
		else {
			Iterator<String> i = arguments.iterator();
			
			try {
				while(i.hasNext()) {
					String s = i.next();
					
					if(s.equals("-s") || s.equals("--source")) {
						if(gd.srcDir != null) {
							throw new IllegalArgumentException(
									"source directory already specified");
						}
						if(!i.hasNext()) {
							throw new IllegalArgumentException(
									"encountered flag " + s 
										+ ", but no directory specified");
						}
						
						gd.setSrcDir(new File(i.next()));
					}
					else if(s.equals("-d") || s.equals("--dest")) {
						if(gd.destDir != null) {
							throw new IllegalArgumentException(
									"destination directory already specified");
						}
						if(!i.hasNext()) {
							throw new IllegalArgumentException(
									"encountered flag " + s
										+ ", but no directory specified");
						}
						
						gd.setDestDir(new File(i.next()));
					}
				}
				
				gd.execute();
			}
			catch(Exception e) {
				gd.log("Error: " + e.getMessage());
			}			
		}
	}
	
	static final class CommandGrammarDoc extends GrammarDoc {
		PrintStream out;
		static final String logPrefix = "[grammardoc] ";
		
		CommandGrammarDoc(PrintStream out) {
			this.out = out;
			
			setProject(new Project());
			setOwningTarget(new Target());
		}

		@Override
		public void log(String s) {
			out.print(logPrefix);			
			out.println(s);
		}
	}
}
