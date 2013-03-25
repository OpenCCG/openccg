/*
 * $Id: HTMLDocumenter.java,v 1.9 2009/12/21 04:18:31 mwhite14850 Exp $
 */
package opennlp.ccg.grammardoc.html;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import opennlp.ccg.grammardoc.AbstractDocumenter;
import opennlp.ccg.grammardoc.DocumenterException;
import opennlp.ccg.grammardoc.DocumenterSourceException;
import opennlp.ccg.grammardoc.SourceGrammar;
import opennlp.ccg.grammardoc.SourceGrammarFile;
import opennlp.ccg.grammardoc.SourceGrammarFileType;

/**
 * A grammardoc documenter that produces <acronym title="Hypertext Markup
 * Language">HTML</acronym> documentation.
 * 
 * @author Scott Martin (http://www.ling.osu.edu/~scott/)
 * @version $Revision: 1.9 $
 */
public class HTMLDocumenter extends AbstractDocumenter implements URIResolver {

	private static final int FILE_BUFFER_SIZE = 256;
	private SourceGrammar sourceGrammar;
	private Map<String, Templates> templateCache;
	
	final TransformerFactory factory = TransformerFactory.newInstance();
	
	static enum FileName {
		STYLESHEET("grammardoc.css"), LEXICON_SCRIPT("lexicon.js");

		final String name;

		private FileName(String name) {
			this.name = name;
		}
	}

	/**
	 * Creates a new HTML Documenter.
	 */
	public HTMLDocumenter() {
		super("GrammarDoc HTML Documenter");
		templateCache = new HashMap<String, Templates>();
		factory.setURIResolver(this);
	}

	/**
	 * Documents a source grammar, producing linked HTML files for its source
	 * files.
	 */
	public void document(SourceGrammar grammar) throws DocumenterException {
		this.sourceGrammar = grammar;
		File destDir = documenterContext.getDestinationDirectory();
		copyFiles(destDir);
		
		String sections;
		StringBuilder sb = new StringBuilder();
		for(SourceGrammarFileType fileType
				: grammar.getSourceGrammarFileTypes()) {
			if(sb.length() > 0) {
				sb.append('|');
			}
			sb.append(fileType.getFileName());
		}
		sections = sb.toString();

		for(SourceGrammarFileType fileType : grammar
				.getSourceGrammarFileTypes()) {
			String baseName = fileType.getFileName();

			StringBuilder fb = new StringBuilder();
			fb.append(baseName.equals(
					SourceGrammarFileType.GRAMMAR.getFileName())
					? "index" : baseName);
			fb.append(".html");
			String targetName = fb.toString();

			SourceGrammarFile sourceFile
				= grammar.getSourceGrammarFile(fileType);
			Templates templates = loadTemplates(baseName);
			
			if(templates != null) {
				documenterContext.log("Generating " + targetName);

				try {
					File f = new File(destDir, targetName);
					StreamResult res = new StreamResult(
							new BufferedOutputStream(new FileOutputStream(f)));
					res.setSystemId(f);
					
					Transformer transformer = templates.newTransformer();
					transformer.setURIResolver(this);
					transformer.setParameter("sections", sections);		
					transformer.transform(
							new StreamSource(sourceFile.getSourceFile()), res);
				}
				catch(TransformerException te) {
					throw new DocumenterSourceException(
							"problem transforming output: "
									+ te.getMessageAndLocation(), te,
									sourceFile);
				}
				catch(IOException ioe) {
					throw new DocumenterException(ioe);
				}
			}
		}
	}

	/**
	 * Resolves URIs to sources. Used by the XSLT files in this package to
	 * resolve xsl:import and document() URIs.
	 */
	public Source resolve(String href, String base) throws TransformerException {

		StreamSource ss = null;
		
		if(href != null && href.length() > 0) {
			if(href.endsWith(".xsl")) {
				ss = new StreamSource(getResource(href));
			}
			else {
				File f = new File(sourceGrammar.getSourceDirectory(), href);
				if(!f.exists()) {
					throw new TransformerException("file does not exist: " + f);
				}
				if(f.isDirectory()) {
					throw new TransformerException("file is a directory: " + f);
				}
				
				ss = new StreamSource(f);
				ss.setSystemId(f);
			}
		}
		
		return ss;
	}

	private Templates loadTemplates(String baseName)
			throws DocumenterException {
		StringBuilder tb = new StringBuilder(baseName);
		tb.append(".xsl");
		String templateName = tb.toString();

		if(!templateCache.containsKey(templateName)) {
			InputStream is = getResource(templateName.toString());
			if(is == null) {
				return null;
			}

			try { // cache for later
				templateCache.put(templateName, 
						factory.newTemplates(new StreamSource(is)));
			}
			catch(TransformerConfigurationException tce) {
				throw new DocumenterException("problem loading template "
						+ templateName.toString() + ": "
							+ tce.getMessageAndLocation(), tce);
			}
		}
		
		return templateCache.get(templateName);
	}

	private void copyFiles(File destDir) throws DocumenterException {
		for(FileName fileName : FileName.values()) {
			doCopyFile(fileName, destDir);
		}
	}

	private void doCopyFile(FileName fileName, File destDir)
			throws DocumenterException {
		InputStream in = getResource(fileName.name);

		if(in == null) {
			throw new DocumenterException("Could not find " + fileName.name);
		}

		File f = new File(destDir, fileName.name);
		documenterContext.log("Writing " + f.getAbsolutePath());

		try {
			FileOutputStream fileOut = new FileOutputStream(f);
			byte[] buffer = new byte[HTMLDocumenter.FILE_BUFFER_SIZE];

			int i;
			while((i = in.read(buffer)) != -1) {
				fileOut.write(buffer, 0, i);
			}

			in.close();
			fileOut.close();
		}
		catch(IOException ioe) {
			throw new DocumenterException("problem copying file: "
					+ ioe.getMessage(), ioe);
		}
	}

	private InputStream getResource(String resourceName) {
		Class<? extends HTMLDocumenter> cl = getClass();
		String cn = cl.getName();
		String pkg = cn.substring(0, cn.lastIndexOf('.'));

		StringBuilder sb = new StringBuilder();
		sb.append(pkg.replace('.', '/'));
		sb.append('/');
		sb.append(resourceName);

		return cl.getClassLoader().getResourceAsStream(sb.toString());
	}
}
