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
 * $Id: CCGBankConvert.java,v 1.8 2011/11/10 22:18:42 mwhite14850 Exp $ 
 */
package opennlp.ccgbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import opennlp.ccgbank.convert.InfoHelper;
import opennlp.ccgbank.convert.XSLTTrueCaser;
import opennlp.ccgbank.convert.MorphLookup;
import opennlp.ccgbank.parse.CCGbankDerivation;
import opennlp.ccgbank.parse.SimpleNode;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.transform.JDOMSource;
import org.xml.sax.InputSource;


/**
 * Converts the CCGBank to a modified version for grammar extraction.
 * <p>
 * Within this task, a series of <code>FileList</code>s is specified. These
 * files are the lists of xsltProcessors that should be used to transform the 
 * CCGBank. These xsltProcessors are processed in the order they occur in the
 * <code>FileList</code> specified within this task. 
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @author Rajakrishnan Rajkumar
 * @version $Revision: 1.8 $
 * @see CCGBankExtract
 */
public class CCGBankConvert extends CCGBankTask {
	
	/** Flag for whether to keep case-marking preps in PP categories; defaults to false. */
	boolean keepPPHeads = false;

	TreeWalker treeWalker = new TreeWalker();
	
	CCGbankDerivation deriv = null;
	File auxFileDirectory, bbnAuxDirectory, wordsFile, stemsFile,
		currentDirectory = null;
	
	/* (non-Javadoc)
	 * @see opennlp.ccgbank.CCGBankTask#start()
	 */
	@Override
	protected void start() throws BuildException {
		
		InfoHelper.init(auxFileDirectory, bbnAuxDirectory);
		String trueCaseListPath=auxFileDirectory.getAbsolutePath()+"/"+"truecase-list.gz";
		XSLTTrueCaser.init(trueCaseListPath);
		
		try {
			MorphLookup.init(wordsFile, stemsFile);
			
		}
		catch(IOException io) {
			throw new BuildException("problem loading words or stems", io,
					getLocation());
		}
		
		xsltProcessor = useXMLFilter
			? new XMLFilterProcessor(this, this)
			: new TemplatesProcessor(this);
		
		// "prime" parser
		// TODO fix this hack!!
		try {
			File tmp = File.createTempFile(getClass().getName(), "prime");
			tmp.deleteOnExit();
						
			deriv = new CCGbankDerivation(new FileReader(tmp));
		}
		catch(IOException e) {
			throw new BuildException("Problem priming parser: "
				+ e.getMessage(), e, getLocation());
		}
	}

	/** Read aux files for the next WSJ section **/
	@Override
	protected void nextDirectory(File section) throws BuildException {
		
		currentDirectory = section;
		
		// only create if a numbered directory
		File d = new File(target, currentDirectory.getName());
		if(!d.exists() && !d.mkdirs()) {
			throw new BuildException("unable to create directory " + d);
		}
		
		//Read in aux files
		try { 
			InfoHelper.readBBNAuxfiles(section.getName());
			InfoHelper.readQuoteAuxfiles(section.getName());
			InfoHelper.readPTBAuxfiles(section.getName());
			InfoHelper.readTreeAuxfiles(section.getName());
		}
		catch(NumberFormatException nfe) {
			// not a numbered PTB directory
		}
	}

	@Override
	protected InputSource nextFile(File file) throws BuildException {
		try {
			Reader reader = new BufferedReader(new FileReader(file));
			
			if(deriv == null) { 
				deriv = new CCGbankDerivation(reader);
			}
			else {
				CCGbankDerivation.ReInit(reader);
			}
			
			SimpleNode root = CCGbankDerivation.start();
			Element result = new Element("Derivation");
			
			String fileName = file.getName();
			int start = fileName.contains(File.separator)
				? fileName.lastIndexOf(File.separatorChar) : 0;
			
			StringBuilder sb = new StringBuilder(
			    	fileName.substring(start, fileName.lastIndexOf('.')));
		    sb.append(".xml");
		    
		    File targetDir = new File(target, currentDirectory.getName());
		    File targetFile = new File(targetDir, sb.toString());			        
		    
		    xsltProcessor.resetSerializer();
		    xsltProcessor.setTarget(targetFile);
		    		    
		    Document doc = new Document(treeWalker.eval(root, result));
		    
		    // TODO attempt to get error reporting for file / line !!
		    Source s = new JDOMSource(doc);
		    s.setSystemId(file.toURI().toString());
		    
		    return SAXSource.sourceToInputSource(s);
		}
		catch(Exception e) {
			throw new BuildException(e, getLocation());
		}
	}
	
	
	/** @param keepPPHeads the keepPPHeads value to set */
	public void setKeepPPHeads(boolean keepPPHeads) { this.keepPPHeads = keepPPHeads; }
	
	
	/**
	 * @param stemsFile the stemsFile to set
	 */
	public void setStemsFile(File stemsFile) {
		this.stemsFile = stemsFile;
	}

	
	/**
	 * @param wordsFile the wordsFile to set
	 */
	public void setWordsFile(File wordsFile) {
		this.wordsFile = wordsFile;
	}

	/**
	 * @param auxFileDirectory the auxFileDirectory to set
	 */
	public void setAuxFileDirectory(File auxFileDirectory) {
		this.auxFileDirectory = auxFileDirectory;
		
	}
	
	/**
	 * @param bbnAuxDirectory the bbnAuxDirectory to set
	 */
	public void setBbnAuxDirectory(File bbnAuxDirectory) {
		this.bbnAuxDirectory = bbnAuxDirectory;
	}

	public static void main(String[] args) {
		File baseDir = new File(System.getProperty("user.dir"));
		File buildFile = new File(baseDir, "build.xml");
		
		Project project = new Project();
		
		project.init();
		
		project.setBaseDir(baseDir);
		
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		
		project.setProjectReference(helper);
		
		helper.parse(project, buildFile);
		
		DefaultLogger logger = new DefaultLogger();
		logger.setErrorPrintStream(System.err);
		logger.setOutputPrintStream(System.out);
		
		project.addBuildListener(logger);
		
		project.executeTarget("convert-base");		
	}
	
	class TreeWalker {
		// General purpose datastructure to store ccgbank indices of categories.
		// Refreshed after the lifespan of a node is over.
		public List<String> idList = new ArrayList<String>();
		// flag for whether under a leaf node;
		// used to control whether to add fs id's
		private boolean underLeaf = false;
		
		public Element eval(SimpleNode node, Element root) throws Exception {

			// No:of children of any given node
			int numC = node.jjtGetNumChildren();

			// Loop & flag variables
			int i = 0;
			SimpleNode child;

			// Processing the child nodes of the current node.
			for (i = 0; i < numC; i++) {

				child = (SimpleNode) node.jjtGetChild(i);

				// Cat spec without co-indexation info in the leafnodes
				if (child.type.equals("Redundant")) {

					if (node.type.equals("Leafnode")) {
						node.catRedundant = child.print();
						continue;
					}

					// Processes treenode categories
					if (node.type.equals("Treenode"))
						child.type = "complexcat";
				}

				// The header node is accessed and the CCGbankId is passed on to the
				// treenode root of the sentence which is processed next
				if (child.type.equals("Header")) {

					i++;
					String temp1 = child.getHeader();
					int spacePos = temp1.indexOf(' ');
					if (spacePos > 0)
						temp1 = temp1.substring(0, spacePos);

					child = (SimpleNode) node.jjtGetChild(i);
					child.setHeader(temp1);
					// System.out.println(temp1);
				}

				// Xml element which is going to be generated.
				Element leaf = new Element(child.type);

				if (child.type.equals("complexcat")
						|| child.type.equals("Treenode")
						|| child.type.equals("Leafnode")) {

					// Atomic categories are represented in the javacc tree as
					// catSpec-aotmcat. So for such cases the catSpec child is
					// skipped and the next child is accessed.

					if (child.jjtGetNumChildren() == 1
							&& child.type.equals("complexcat")) {

						child = (SimpleNode) child.jjtGetChild(0);

						// The element which is to be added to the xml
						// representation
						leaf = new Element("atomcat");

						// Extracting the content of the node and storing it.
						node.cat = child.print();
					} else {

						// A complexcat element is created.
						leaf = ccinserter(child, leaf);

						// Extracting the content of the node and storing it.
						if (root.getName().equals("Leafnode")) {
							node.cat = child.print();
						}

						if (root.getName().equals("Treenode")
								&& leaf.getName().equals("complexcat")) {
							// Leafnode and treenode cat spec elements created
							node.cat = child.print();
						}

						// Recursive processing of the children of the current node
						leaf = eval(child, leaf);

						// Sending Leaf,Tree nodes for to a function which inserts
						// the family (ie normalized cat spec) of its contents.
						if (!child.type.equals("complexcat"))
							leaf = catInserter(child, leaf);

						// Adding the current element to its parent in the xml tree.
						root.addContent(leaf);

						continue;
					}

				}

				// Slash elements added to the tree
				if (child.cat.equals("/") || child.cat.equals("\\")) {
					leaf = opinserter(child);
					root.addContent(leaf);
					continue;
				}

				// Atomcat elements added to the tree
				leaf = atomcatinserter(child);
				root.addContent(leaf);

				// if(!child.type.matches("\\p{Punct}"))
			}

			// The final result of the above operations returned.
			return root;

		}
		
		public Element ccinserter(SimpleNode node, Element leaf) {

			// This function produces complexcat/treenode/leafnode elements.
			// mww: the name of this function is not very helpful

			// The node can be of any of the above types.
			String name;
			name = node.type;

			// Set treebankId,parseNo at root of the sent
			String h = node.getHeader();
			if (h != null)
				leaf.setAttribute("Header", h);

			// Treenode info ie head,daughter represented
			if (name.equals("Treenode")) {

				leaf.setAttribute("head", node.head);
				leaf.setAttribute("dtr", node.dtr);

				idList.clear();
				underLeaf = false;
			}

			// Leafnode info represented
			if (name.equals("Leafnode")) {
				leaf.setAttribute("lexeme", node.lex); // nb: may be truecased later
				leaf.setAttribute("lexeme0", node.lex);
				leaf.setAttribute("pos", node.pos);
				idList.clear();
				underLeaf = true;
			}

			// add propbank info here
			if (node.nodeRoles != null) {
				String roles = "";
				for (SimpleNode.LexSenseRole lexSenseRole : node.nodeRoles) {
					if (lexSenseRole.role.equals("rel")) {
						leaf.setAttribute("rel", lexSenseRole.lex + "." + lexSenseRole.sense);
					}
					else {
						String role = adjustRole(lexSenseRole.role);
						roles += lexSenseRole.lex + "." + lexSenseRole.sense + ":" + role + " ";
					}
				}
				if (roles.length() > 0) leaf.setAttribute("roles", roles.trim());
			}
			if (node.argRoles != null) {
				String args = "";
				for (String role : node.argRoles) {
					role = adjustRole(role);
					args += role + " ";
				}
				leaf.setAttribute("argRoles", args.trim());
			}
			// done
			return leaf;
		}

		public Element atomcatinserter(SimpleNode node) throws Exception {

			// Predicate for atomcat creation.
			// Flag signifies whether the elem is a single atomcat.
			// Relevant as if the present cat is an atomcat LF variable can be set
			// in the syntax here itself.

			StringTokenizer lex;
			Element atomcat = new Element("atomcat");
			Element fs = new Element("fs");
			Element feat = new Element("feat");
			String id = "NotGiven";
			String form;

			// Current element info extracted from the node
			String elem = node.print();

			// Index extracted by sensing undescore delimiter
			if (elem.contains("_")) {
				// elem=elem.replaceAll(":[A-Z]","");
				lex = new StringTokenizer(elem, "_");
				elem = lex.nextToken();
				id = lex.nextToken();
				String x[] = id.split(":");
				// System.out.println(id);

				if (x.length == 2) {
					// mww: moved this to atomcat
					//feat.setAttribute("attr", "dep");
					//feat.setAttribute("val", x[1]);
					//fs.addContent(feat);
					atomcat.setAttribute("dep", x[1]);
				}

				id = id.replaceAll(":[A-Z]", "");
			} else {
				// Default id 1 is alloted otherwise
				id = Integer.toString(idList.size() + 1);
				idList.add(id);
			}

			// Normalizing the id by comparing with previous indices.
			if (!idList.contains(id))
				idList.add(id);
			id = Integer.toString(idList.indexOf(id) + 1);

			// Normalized index is set (leaves only)
			if (underLeaf)
				fs.setAttribute("id", id);

			// make lowercase
			elem = elem.toLowerCase();
			// remove superfluous [nb] in np[nb]
			elem = elem.replaceAll("np\\[nb\\]", "np");
			// also strip PP heads if apropos
			elem = stripPPHeads(elem);

			// Form attrtibute detected and set.
			if (elem.contains("[")) {
				lex = new StringTokenizer(elem, "[,]");
				elem = lex.nextToken();
				form = lex.nextToken();
				feat = new Element("feat");
				feat.setAttribute("attr", "form");
				feat.setAttribute("val", form);
				fs.addContent(feat);
			}

			// add fs if non-empty
			if (underLeaf || fs.getContentSize() > 0)
				atomcat.addContent(fs);
			atomcat.setAttribute("type", elem);
			return atomcat;

		}

		public Element opinserter(SimpleNode node) {

			Element slash = new Element("slash");
			String dir;
			String op = node.cat;

			// Slash direction sensed and set.

			if (op.equals("\\"))
				dir = "<";
			else
				dir = ">";

			slash.setAttribute("dir", op);
			slash.setAttribute("mode", dir);
			return slash;

		}

		public Element catInserter(SimpleNode node, Element leaf) {
			int i;

			// The normalization process. Relevant indices replaced by 1,2,3..n

			if (idList.size() > 0) {
				for (i = 0; i < idList.size(); i++)
					node.cat = node.cat.replaceAll(idList.get(i),
							Integer.toString(i + 1));
			}

			String l = node.getLeftover();
			if (l != null)
				node.cat = node.cat + l;

			// Purging the cat spec of indices outside brackets & colons ie )_2 ,:B
			int ind = node.cat.indexOf(")_");

			while (ind != -1) {

				String str1 = node.cat.substring(0, ind + 1);
				String str2 = node.cat.substring(ind + 1, node.cat.length());

				str2 = str2.replaceFirst("_(\\p{Digit})++", "");

				// System.out.println(str1);
				// System.out.println(str2);

				node.cat = str1 + str2;

				ind = node.cat.indexOf(")_");

			}

			node.cat = node.cat.replaceAll(":[A-Z]", "");

			// Add categories with normalized indices, lowercased
			String cat = node.cat.toLowerCase();
			cat = cat.replaceAll("np\\[nb\\]", "np");
			// also strip PP heads if apropos
			cat = stripPPHeads(cat);
			leaf.setAttribute("cat", cat);

			String cat0 = "";

			// Add the same category to the treenodes
			if (node.type.equals("Treenode"))
				cat0 = node.cat;
			else
				cat0 = node.catRedundant;

			// Add the bare category to the leafnodes
			leaf.setAttribute("cat0", cat0);

			// Refresh index list.
			idList.clear();

			return leaf;
		}
	}
	
	// strips PP heads if apropos
	private String stripPPHeads(String cat) {
		if (keepPPHeads) return cat;
		return cat.replaceAll("pp\\[[a-z]+\\]", "pp");
	}
	
	// adjusts role, stripping PP head if apropos
	private String adjustRole(String role) {
		role = role.replaceFirst("ARG", "Arg");
		if (!keepPPHeads) {
			int hyph = role.indexOf('-');
			if (hyph > 0) role = role.substring(0, hyph);
		}
		return role;
	}
}
