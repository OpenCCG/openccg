///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2005-2009 Scott Martin, Rajakrishan Rajkumar and Michael White
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

/*
 * $Id: CCGBankExtract.java,v 1.5 2011/11/04 01:49:57 raja-asoka Exp $ 
 */
package opennlp.ccgbank;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.EnumMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import opennlp.ccgbank.CCGBankTaskTemplates.Type;
import opennlp.ccgbank.extract.FreqTally;
import opennlp.ccgbank.extract.RulesTally;
import opennlp.ccgbank.extract.Testbed;

import org.apache.tools.ant.BuildException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Extracts a grammar from a converted version of the CCGBank.
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $Revision: 1.5 $
 * @see CCGBankConvert
 */
public class CCGBankExtract extends CCGBankTask implements URIResolver {
	
	static String pkgPath = null;
	static final String LEXICON_TEMPLATE = "lexicon-base.xsl",
		RULES_TEMPLATE = "rules-base.xsl";
	
	String grammarName = "ccgbankextract";
	boolean pPheads = true, skipUnmatched = false;
	int catFreqCutoff = 1, lexFreqCutoff = 1, openFreqCutoff = 100,
		ruleFreqCutoff = 1;
	CCGBankTaskTestbed testbed = null;
	
	File lexiconTempFile, rulesTempFile;
	TemplatesProcessor ruleProcessor;
	Map<Type, XSLTProcessor> xsltProcessors
		= new EnumMap<Type, XSLTProcessor>(Type.class);
	
	public CCGBankExtract() {
		super();
		if(pkgPath == null) {
			pkgPath = getClass().getPackage().getName().replace('.', '/');
		}
	}
	
	/**
	 * Sets the name of the generated grammar.
	 * @param grammarName The name of the generated grammar. This is the string
	 * that will appear in the "name" attribute of the root element of the
	 * generated grammar's <code>grammar.xml</code> file.
	 */
	public void setGrammarName(String grammarName) {
		this.grammarName = grammarName;
	}

	
	/**
	 * @param tb the testbed to set
	 */
	public void addConfiguredTestbed(CCGBankTaskTestbed tb) {
		this.testbed = tb;
	}

	
	/**
	 * @param catFreqCutoff the catFreqCutoff to set
	 */
	public void setCatFreqCutoff(int catFreqCutoff) {
		this.catFreqCutoff = catFreqCutoff;
	}

	
	/**
	 * @param lexFreqCutoff the lexFreqCutoff to set
	 */
	public void setLexFreqCutoff(int lexFreqCutoff) {
		this.lexFreqCutoff = lexFreqCutoff;
	}

	
	/**
	 * @param openFreqCutoff the openFreqCutoff to set
	 */
	public void setOpenFreqCutoff(int openFreqCutoff) {
		this.openFreqCutoff = openFreqCutoff;
	}

	
	/**
	 * @param pPheads the ppheads to set
	 */
	public void setPPheads(boolean pPheads) {
		this.pPheads = pPheads;
	}

	
	/**
	 * @param ruleFreqCutoff the ruleFreqCutoff to set
	 */
	public void setRuleFreqCutoff(int ruleFreqCutoff) {
		this.ruleFreqCutoff = ruleFreqCutoff;
	}

	
	/**
	 * @param skipUnmatched the skipUnmatched to set
	 */
	public void setSkipUnmatched(boolean skipUnmatched) {
		this.skipUnmatched = skipUnmatched;
	}
	
	
	/* (non-Javadoc)
	 * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
	 */
	public Source resolve(String href, String base) {
		if(href != null && href.length() > 0 && href.startsWith(pkgPath)) {
			String lastChunk = (href.contains("/") && !href.endsWith("/"))
				? href.substring(href.lastIndexOf('/') + 1) : href;
			if(lastChunk.endsWith(CCGBankExtract.LEXICON_TEMPLATE)
					|| lastChunk.endsWith(CCGBankExtract.RULES_TEMPLATE)) {
				return new StreamSource(getResource(href));
			}
		}
		
		return new StreamSource(new File(href));
	}
	
	
	/* (non-Javadoc)
	 * @see opennlp.ccgbank.CCGBankTask#addConfiguredCCGBankTaskTemplates(opennlp.ccgbank.CCGBankTaskTemplates)
	 */
	@Override
	public void addConfiguredTemplates(CCGBankTaskTemplates taskTemplates) {
		if(xsltProcessors.containsKey(taskTemplates.type)) {
			throw new BuildException(taskTemplates.type 
					+ " extraction type is multiply defined");
		}
		
		XSLTProcessor xp = useXMLFilter
			? new XMLFilterProcessor(this, this)
			: new TemplatesProcessor(this);
		
		xp.addTemplates(taskTemplates);
		xp.transformerFactory.setURIResolver(this);
		
		xsltProcessors.put(taskTemplates.type, xp);
	}


	/* (non-Javadoc)
	 * @see opennlp.ccgbank.CCGBankTask#start()
	 */
	@Override
	protected void start() throws BuildException {
		xsltProcessor = new TemplatesProcessor(this);
		((TemplatesProcessor)xsltProcessor).addTemplates(
			loadTemplates(pkgPath + "/" + CCGBankExtract.LEXICON_TEMPLATE));
		ruleProcessor = new TemplatesProcessor(this);
		ruleProcessor.addTemplates(loadTemplates(pkgPath + "/"
				+ CCGBankExtract.RULES_TEMPLATE));

		FreqTally.reset();
		FreqTally.CAT_FREQ_CUTOFF = catFreqCutoff;
		FreqTally.LEX_FREQ_CUTOFF = lexFreqCutoff;
		FreqTally.OPEN_FREQ_CUTOFF = openFreqCutoff;
		
		RulesTally.reset();
		RulesTally.RULE_FREQ_CUTOFF = ruleFreqCutoff;
        RulesTally.KEEP_UNMATCHED = !skipUnmatched;
		
		try {
			lexiconTempFile = File.createTempFile(grammarName, ".xml");
			lexiconTempFile.deleteOnExit();
			xsltProcessor.setTarget(lexiconTempFile);
			
			rulesTempFile = File.createTempFile(grammarName + "-rules", ".xml");
			rulesTempFile.deleteOnExit();
			ruleProcessor.setTarget(rulesTempFile);
			
			Writer w = xsltProcessor.serializer.getWriter();
			w.write("<ccg-lexicon>");
			w.flush();
			
			Writer rw = ruleProcessor.serializer.getWriter();
			rw.write("<rules>");
			rw.flush();
		}
		catch(IOException io) {
			throw new BuildException(io, getLocation());
		}
	}
	
		
	/* (non-Javadoc)
	 * @see opennlp.ccgbank.CCGBankTask#nextFile(java.io.File)
	 */
	@Override
	protected InputSource nextFile(File file) throws BuildException {
		try {
			ruleProcessor.process(super.nextFile(file));
		}
		catch(IOException io) {
			throw new BuildException("I/O problem processing " + file + ": "
				+ io.getMessage(), io, getLocation());
		}
		catch(SAXException se) {
			throw new BuildException("Problem processing " + file + ": "
				+ se.getMessage(), se, getLocation());
		}
		catch(TransformerException te) {
			throw new BuildException("Problem processing " + file + ": "
				+ te.getMessageAndLocation(), te, getLocation());
		}
		
		return super.nextFile(file); // TODO is this right?
	}


	/* (non-Javadoc)
	 * @see opennlp.ccgbank.CCGBankTask#finish()
	 */
	@Override
	protected void finish() throws BuildException {
		try {
			Writer w = xsltProcessor.serializer.getWriter();
			w.write("</ccg-lexicon>");
			w.close();
			
			Writer rw = ruleProcessor.serializer.getWriter();
			rw.write("</rules>");
			rw.close();
		}
		catch(IOException io) {
			throw new BuildException(io, getLocation());
		}
		
		// generate lexicon, morph, rules
		for(Type t : xsltProcessors.keySet()) {
			if(t == Type.LEXICON) {
				try {
					FreqTally.printTally(target);
				}
				catch(FileNotFoundException fnfe) {
					throw new BuildException("problem generating frequencies",
							fnfe, getLocation());
				}
			}
			else if(t == Type.RULES) {
				try {
					RulesTally.printTally(target);
				}
				catch(FileNotFoundException fnfe) {
					throw new BuildException(
						"problem generating rule frequencies", fnfe,
						getLocation());
				}
			}
			
			String fileName = t.fileName();
			log("Generating " + fileName);
			try {
				XSLTProcessor xp = xsltProcessors.get(t);
				xp.setTarget(new File(target, fileName));
				
				xp.process(new InputSource(
					new BufferedInputStream(new FileInputStream(						
						(t == Type.RULES) ? rulesTempFile : lexiconTempFile))));
			}
			catch(IOException io) {
				throw new BuildException("I/O problem writing " + fileName,
						io, getLocation());
			}
			catch(TransformerException te) {
				throw new BuildException("Problem transforming " + fileName
					+ ": " + te.getMessageAndLocation(), te, getLocation());
			}
			catch(SAXException se) {
				throw new BuildException("Problem transforming " + fileName
					+ ": " + se.getMessage(), se, getLocation());
			}
		}
		
		// generate grammar.xml, if it doesn't already exist
		// nb: should eventually make schema refs relative to OPENCCG_HOME		
		try {
			File gramFile = new File(target, "grammar.xml");
			if (!gramFile.exists()) {
				log("Generating grammar.xml");
				PrintWriter gramOut = new PrintWriter(new FileWriter(gramFile));
				gramOut.println("<?xml version=\"1.0\"?>");
				gramOut.println("<grammar name=\"" + grammarName + "\"");
				gramOut.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
				gramOut.println("  xsi:noNamespaceSchemaLocation=\"../grammar.xsd\"");
				gramOut.println(">");
				gramOut.println("  <lexicon file=\"lexicon.xml\"/>");
				gramOut.println("  <morphology file=\"morph.xml\"/>");
				gramOut.println("  <rules file=\"rules.xml\"/>");
				gramOut.println("<tokenizer replacement-sem-classes=\"DATE LOCATION MONEY ORGANIZATION PERCENT PERSON TIME\"/>");
				gramOut.println("<LF-from-XML>");
				gramOut.println("<transform file=\"convert-to-hlds.xsl\"/>");
				gramOut.println("<transform file=\"add-chunks.xsl\"/>");
				gramOut.println("</LF-from-XML>");
				gramOut.println("<LF-to-XML>");
				gramOut.println("<transform file=\"raise-nodes.xsl\"/>");
				gramOut.println("<transform file=\"convert-to-graph.xsl\"/>");
				gramOut.println("</LF-to-XML>");
				gramOut.println("</grammar>");
				gramOut.close();
			}
		}
		catch(IOException io) {
			throw new BuildException("problem generating grammar.xml",
					io, getLocation());
		}
		
		if(testbed != null) {
			log("Creating testbed ...");
			
			try {
				Testbed ct = new Testbed(ccgBankTaskSources,
						target, testbed);
				ct.createTestFiles();
			}
			catch(Exception e) {e.printStackTrace();
				throw new BuildException("problem generating testbed: "
						+ e.getMessage(), e, getLocation());
			}
		}
	}
	
	Templates loadTemplates(String resourceName) throws BuildException {
		try {
			// XXX nb: no xsltc option this way
			//TransformerFactory tf = XSLTProcessor.newTransformerFactory(); 
			SAXTransformerFactory tf = (SAXTransformerFactory)TransformerFactory.newInstance();
			
			return tf.newTemplates(new StreamSource(new BufferedInputStream(
				getResource(resourceName))));
		}
		catch(TransformerConfigurationException e) {
			throw new BuildException("Problem loading template "
				+ resourceName + ": " + e.getMessage(), e, getLocation());
		}
	}
	
	/**
	 * Loads a resource using the fully qualified name with the current
	 * class loader
	 */
	InputStream getResource(String resourceName) {
		return getClass().getClassLoader().getResourceAsStream(resourceName);
	}
}
