///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-4 Jason Baldridge and University of Edinburgh (Michael White)
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
package opennlp.ccg.test;

import opennlp.ccg.grammar.Grammar;
import opennlp.ccg.synsem.*;

import org.jdom.*;
import org.jdom.input.*;
import java.io.*;
import java.util.*;

/**
 * Manages the info in a regression test file.
 *
 * @author  Jason Baldridge
 * @author  Michael White
 * @version $Revision: 1.16 $, $Date: 2011/08/27 19:27:01 $
 */
public class RegressionInfo {

    // the grammar
    private Grammar grammar;
    
    // the test items
    private TestItem[] testItems;
    
    /** Test item. */
    public class TestItem {
        /** The test sentence/phrase. */
        public String sentence;
        /** An alternative paraphrase to target, or null if none. */
        public String alt = null;
        /** The desired number of parses. */
        public int numOfParses = 1;
        /** Whether the sentence/phrase is known to fail to parse. */
        public boolean knownFailure = false;
        /** The full words for the sentence/phrase, or null if none, formatted by the configured tokenizer. */
        public String fullWords = null;
        /** The LF, in XML, for the sentence/phrase, or null if none. */
        public Element lfElt = null;
        /** Any additionally id info, or null if none. */
        public String info = null;
		/** The gold std nominal id name & supertag for LF predicates. @deprecated Should use fullWords. */
		public String predInfo = null;
		/** The gold standard derivation. */
		public Sign sign = null;
		/** Returns the id from info, without prefixed "ID=" if present. */
		public String getId() {
			if (info == null) return null;
			int pos = info.indexOf('=');
			return (pos < 0) ? info : info.substring(pos+1);
		}
    }

    /** Reads in the given regression test file and corresponding .ser file, if any. */    
    public RegressionInfo(Grammar grammar, File regressionFile) throws FileNotFoundException {
    	this(grammar, new FileInputStream(regressionFile), serStream(regressionFile));
    }
    
    /** Reads in a regression test from the given input stream and object input stream. */    
	@SuppressWarnings("unchecked")
	public RegressionInfo(Grammar grammar, InputStream istr, ObjectInputStream serStream) {
        this.grammar = grammar;
        SAXBuilder builder = new SAXBuilder();
        try {
        	Map<String,Sign> signMap = readSerStream(serStream);
            Document doc = builder.build(istr);
            Element root = doc.getRootElement();
            List<Element> items = root.getChildren("item");
            testItems = new TestItem[items.size()];
            for (int i = 0; i < items.size(); i++) {
                Element item = (Element) items.get(i);
                TestItem testItem = new TestItem();
                testItems[i] = testItem;
                testItem.sentence = item.getAttributeValue("string");
                testItem.alt = item.getAttributeValue("alt");
                testItem.numOfParses = Integer.parseInt(item.getAttributeValue("numOfParses"));
                testItem.knownFailure = ("true".equals(item.getAttributeValue("known"))) ? true : false;
                Element fullWordsElt = item.getChild("full-words");
                if (fullWordsElt != null) testItem.fullWords = fullWordsElt.getTextNormalize();
                testItem.lfElt = item.getChild("lf");
                testItem.info = item.getAttributeValue("info");
				Element predInfoElt = item.getChild("pred-info");
				if (predInfoElt != null) testItem.predInfo = predInfoElt.getAttributeValue("data");
				if (signMap != null && testItem.info != null) testItem.sign = signMap.get(testItem.info);
            }
        } catch (Exception e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
    }

    
    /** Returns the corresponding .ser file for loading sign objects. */
    public static File serFile(File regressionFile) {
    	String name = regressionFile.getName();
    	String prefix = name.substring(0, name.lastIndexOf('.'));
    	return new File(regressionFile.getParentFile(), prefix + ".ser");
    }

    /** Returns object input stream for corresponding .ser file, or null if none. */
    public static ObjectInputStream serStream(File regressionFile) {
    	File serFile = serFile(regressionFile);
    	if (serFile.exists()) {
			try {
				return new ObjectInputStream(new FileInputStream(serFile));
			} catch (FileNotFoundException e) {
	            throw (RuntimeException) new RuntimeException().initCause(e); 
			} catch (IOException e) {
	            throw (RuntimeException) new RuntimeException().initCause(e);
			}
    	}
		else return null;
    }
    
    /** Reads in a map of info keys and gold standard signs from the given stream, or returns null if the stream is null. */
    @SuppressWarnings("unchecked")
	public static Map<String,Sign> readSerStream(ObjectInputStream serStream) throws IOException {
    	if (serStream == null) return null;
    	try {
			return (Map<String,Sign>) serStream.readObject();
		} catch (ClassNotFoundException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
		}
    }
    
    /** Writes the map of info keys and gold standard signs to the corresponding .ser file. */
    public static void writeSerFile(Map<String,Sign> signMap, File regressionFile) throws FileNotFoundException, IOException {
    	File serFile = serFile(regressionFile);
    	ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serFile));
    	oos.writeObject(signMap);
    	oos.close();
    }
    
    
    /** Returns the number of test items. */
    public int numberOfItems() {
        return testItems.length;
    }
    
    /** Returns the test item with the given index. */ 
    public TestItem getItem(int i) {
        return testItems[i];
    }
    
    
    /**
     * Makes an XML test item from the given test item object.
     */
    public static Element makeTestItem(TestItem testItem) {
        Element item = new Element("item");
        item.setAttribute("numOfParses", "" + testItem.numOfParses);
        if (testItem.knownFailure) item.setAttribute("known", "true");
        item.setAttribute("string", testItem.sentence);
        if (testItem.alt != null) item.setAttribute("alt", testItem.alt);
        if (testItem.fullWords != null) {
            Element fullWordsElt = new Element("full-words");
            item.addContent(fullWordsElt);
            fullWordsElt.addContent(testItem.fullWords);
        }
        if (testItem.lfElt != null) {
            testItem.lfElt.detach();
            item.addContent(testItem.lfElt);
        }
        if (testItem.info != null) item.setAttribute("info", testItem.info);
        return item;
    }
    
    /**
     * Makes an XML test item with the given string, number of parses and LF, 
     * applying the configured to-XML transformations.
     */
    public Element makeTestItem(String target, int numParses, LF lf) throws IOException { 
        return makeTestItem(grammar, target, numParses, lf);
    }
    
    /**
     * Makes an XML test item with the given string, number of parses and LF, 
     * applying the configured to-XML transformations.
     */
    public static Element makeTestItem(Grammar grammar, String target, int numParses, LF lf) throws IOException { 
        return makeTestItem(grammar, target, numParses, lf, null);
    }

    /**
     * Makes an XML test item with the given string, number of parses, LF and info attribute,  
     * applying the configured to-XML transformations.
     */
    public static Element makeTestItem(Grammar grammar, String target, int numParses, LF lf, String info) throws IOException { 
        Element item = new Element("item");
        item.setAttribute("numOfParses", "" + numParses);
        item.setAttribute("string", target);
        if (lf != null) item.addContent(grammar.makeLfElt(lf));
        if (info != null) item.setAttribute("info", info);
        return item;
    }

    /**
     * Adds the given sign with its string, number of parses and converted LF 
     * as a test item to the testbed with the given filename, applying the configured to-XML
     * transformations.
     */
    public static void addToTestbed(Grammar grammar, Sign sign, int numParses, LF lf, String filename) throws IOException { 

        // ensure dirs exist for filename
        File file = new File(filename);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) { parent.mkdirs(); }
        
        // load or make doc
        Document doc; 
        Element root;
        boolean newDoc = false;
        if (file.exists()) {
            // read XML
            SAXBuilder builder = new SAXBuilder();
            try {
                doc = builder.build(file);
            } catch (JDOMException jde) {
                throw (IOException) new IOException().initCause(jde);
            }
            root = doc.getRootElement();
        }
        else {
            doc = new Document();
            root = new Element("regression");
            doc.setRootElement(root);
            newDoc = true;
        }
        
        // load or make sign map
    	Map<String,Sign> signMap = readSerStream(serStream(file));
    	if (signMap == null) signMap = new HashMap<String,Sign>();
        
    	// find unique id
    	int count = 0;
    	String id = "i" + count;
    	while (signMap.containsKey(id)) id = "i" + ++count;
    	
        // make test item
    	String target = sign.getOrthography();
        Element item = makeTestItem(grammar, target, numParses, lf, id);
        
        // append new item
        if (!newDoc) root.addContent("  "); // nb: for some reason, this gets the indenting right
        root.addContent(item);
        
        // add sign to map
        signMap.put(id, sign);
        
        // save
        FileOutputStream out = new FileOutputStream(file); 
        grammar.serializeXml(doc, out);
        out.close();
        writeSerFile(signMap, file);
    }
}
