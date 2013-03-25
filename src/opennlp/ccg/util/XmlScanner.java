///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2006 Michael White (The Ohio State University)
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

package opennlp.ccg.util;

//JAXP packages
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

// jdom
import org.jdom.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Quick-and-dirty class for using JDOM elements in parsing XML 
 * without building a document for the whole XML file.
 * The <code>parse</code> method parses an XML file with a SAX 
 * parser, constructing JDOM elements for root and the top-level elements,
 * without attaching the top-level elements to the root.
 * Along the way, the <code>handleRoot</code> and <code>handleElement</code> 
 * methods are invoked, to handle these elements incrementally.
 * To use the class, just implement <code>handleElement</code>, and optionally 
 * implement <code>handleRoot</code>, and call <code>parse</code> on the 
 * input XML via its URL.  Note that with Java's incremental garbage 
 * collection, an XmlScanner should be reasonably efficient, but not 
 * as efficient as a pure SAX parser (which however requires considerably 
 * more work to implement).  At present, the parser only handles elements 
 * and attributes without namespaces; all attributes with namespaces, text nodes, 
 * comments, etc. are ignored. 
 *
 * @author      Michael White
 * @version     $Revision: 1.3 $, $Date: 2009/12/21 03:27:18 $
 */
public abstract class XmlScanner extends DefaultHandler {
    
	/** 
	 * Method for handling the root element, sans children. 
	 * The default method does nothing. 
	 */
    public void handleRoot(Element e) {}
    
	/** Method for handling top-level elements. */
    public abstract void handleElement(Element e);
    
    /** 
     * Method for parsing an XML document, handling the childless root element 
     * and the top-level elements along the way. 
     */
    public void parse(URL url) throws IOException {
    	try {
	        // Create a JAXP SAXParserFactory and configure it
	        SAXParserFactory spf = SAXParserFactory.newInstance();
	
	        // Create a JAXP SAXParser
	        SAXParser saxParser = spf.newSAXParser();
	
	        // Get the encapsulated SAX XMLReader
	        XMLReader xmlReader = saxParser.getXMLReader();
	
	        // Set the ContentHandler of the XMLReader
	        xmlReader.setContentHandler(this);
	
	        // Tell the XMLReader to parse the XML document
	        xmlReader.parse(url.toString());
    	}
    	catch (ParserConfigurationException exc) {
    		throw (IOException) new IOException().initCause(exc);
    	}
    	catch (SAXException exc) {
    		throw (IOException) new IOException().initCause(exc);
    	}
    }

    
    // flag for whether the root element has been seen yet
    private boolean seenRoot = false;
    // the element that is currently being processed
    private Element current = null;

    
    //
    // ContentHandler methods (just elements)
    //

    /** 
     * For the root, a childless element is created and handled, via <code>handleRoot</code>; 
     * for all other elements, a new current element is created and added as a child of the 
     * current element, if any. 
     */
    public void startElement(String uri, String localName, String qname, Attributes attributes) throws SAXException {
        
        if (!seenRoot) { 
        	seenRoot = true;
        	Element root = createElement(uri, localName, qname, attributes);
        	handleRoot(root);
        	return; 
    	}
        
        Element parent = current;
        current = createElement(uri, localName, qname, attributes);
        if (parent != null) parent.addContent(current);
    }

    /** 
     * Constructs and returns a new element from the given info.
     * This implementation ignores the <code>uri</code> and <code>localName</code>, 
     * and filters out any attributes whose <code>qname</code> contains a colon.
     */
    protected Element createElement(String uri, String localName, String qname, Attributes attributes) {
        Element retval = new Element(qname);
        if (attributes != null) {
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                String attrQName = attributes.getQName(i);
                if (attrQName.indexOf(':') >= 0) continue;
                String attrValue = attributes.getValue(i);
                retval.setAttribute(attrQName, attrValue);
            }
        }
        return retval;
    }
    
    /** 
     * Resets the current element to its parent, after first invoking 
     * <code>handleElement</code> on the element if it's a top-level one. 
     */
    public void endElement(String uri, String localName, String qname) throws SAXException {
        if (current == null) return; // for root
        Element parent = (Element) current.getParent();
        if (parent == null) handleElement(current);
        current = parent;
    } 

    

    /**
     *  Example scanner: prints root, counts top- and second-level elements.
     */
    public static class MyScanner extends XmlScanner {
        int count = 0; int second = 0;
		public void handleRoot(Element e) {
        	System.out.println("root name: " + e.getName());
        	System.out.print("root attributes: ");
            @SuppressWarnings("unchecked")
        	List<Attribute> attrs = (List<Attribute>) e.getAttributes();
        	for (Attribute attr : attrs) {
        		System.out.print(attr.getName() + "=" + attr.getValue() + " ");
        	}
        	System.out.println();
        }
        public void handleElement(Element e) { 
            count++; second += e.getContentSize();
        }
    }
    
    /**
     * The main method shows an example of using a scanner, by 
     * invoking an instance of <code>MyScanner</code> on the file 
     * whose name is given by the first arg.
     */
    public static void main(String[] args) throws IOException {
        String filename = args[0];
        MyScanner myScanner = new MyScanner();
        myScanner.parse(new File(filename).toURI().toURL());
        System.out.println("top-level elements: " + myScanner.count);
        System.out.println("second-level elements: " + myScanner.second);
    }
}
