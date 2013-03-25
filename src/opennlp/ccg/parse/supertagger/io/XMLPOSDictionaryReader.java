///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2009 Dennis N. Mehay
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
package opennlp.ccg.parse.supertagger.io;

import java.io.File;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.*;

import opennlp.ccg.parse.supertagger.util.STTaggerPOSDictionary;
import opennlp.ccg.util.Pair;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Dennis N. Mehay
 * @version $Revision: 1.2 $, $Date: 2009/12/21 02:10:57 $
 */
public class XMLPOSDictionaryReader {
    
    private File dictFile;
    private XMLReader reader;
    private Map<String, Collection<String>> dict; 
    
    /** Creates a new instance of XMLDictionaryReader
     * @param dictFile A <code>String</code> pointing to the location of
     * the XML file specifying the word dictionary.
     */
    public XMLPOSDictionaryReader(File df) {
        if(!df.exists()) {
            throw new RuntimeException("File "+df.getAbsolutePath().toString()+" does not exist.");
        }
        this.dictFile = df;       
    }
    
    /**
     * Read in the dictionary file and create a new STTaggerPOSDictionary.
     * @return A new <tt>STTaggerPOSDictionary</tt>.
     */
    public STTaggerPOSDictionary read() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            reader = parser.getXMLReader();
            reader.setContentHandler(new wdContentHandler());
            reader.parse(this.dictFile.toURI().toString());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return new STTaggerPOSDictionary(this.dict);
    }
    
    public static void main(String[] args) {
        // This is just to verify that the XML doc read in is the one 
        // spit out.
        String fname = args[0];
        XMLPOSDictionaryReader rdr = new XMLPOSDictionaryReader(new File(fname));
        STTaggerPOSDictionary dct = rdr.read();
        Iterator<Pair<String, Collection<String>>> it = dct.getMappings();
        Pair<String, Collection<String>> tempP = null;
        System.out.println("<posdict>");
        while(it.hasNext()) {
            tempP = it.next();
            System.out.println("     <entry pos=\""+tempP.a+"\">");            
            for(Iterator<String> stgs = tempP.b.iterator(); stgs.hasNext(); ) {
                System.out.println("          <supertag> "+stgs.next().trim()+" </supertag>");
            }
            System.out.println("     </entry>");
        }
        System.out.print("</posdict>");
    }
    
    /*
     * A ContentHandler to properly interpret the "semantics" of the XML (semantics
     * in the CS sense of formal semantics of a structured document).
     */
    class wdContentHandler extends DefaultHandler {
        private boolean inEntry = false, inSupertag = false;
        private String curPOS = null, currSTFrag = null;
        
        @Override
        public void startDocument() {
            dict = new TreeMap<String, Collection<String>>();
        }
        
        @Override
        public void startElement(String namespaceURI, String lname, String qname, Attributes attrs)
                throws SAXException {
            if(qname.equalsIgnoreCase("entry")) {  
                if(this.inEntry) {
                    throw new SAXException("Something is wrong.\nThis is not a well-formed dictionary.");
                } else {
                    this.inEntry = true;
                    String pos = attrs.getValue(0).trim();                    
                    dict.
                         put(pos,
                             new HashSet<String>());
                    this.curPOS = pos;
                }
                
            } else if(qname.equalsIgnoreCase("supertag")) {
                if(!this.inEntry) {
                    throw new SAXException("Something is wrong.\nThis is not a well-formed dictionary.");
                } else {
                    this.inSupertag = true;
                    this.currSTFrag = "";
                }
            }
        }
        
        @Override
        public void endElement(String uri, String name, String qName) {
            if(qName.equalsIgnoreCase("entry")) {
                this.inEntry = false; this.curPOS = null;
            } else if(qName.equalsIgnoreCase("supertag")) {
                this.inSupertag = false; 
                Collection<String> tempL = dict.get(this.curPOS);
                tempL.add(this.currSTFrag.trim());
                dict.put(this.curPOS, tempL);
                this.currSTFrag = null;
            }
        }
        
        @Override
        public void characters(char[] ch, int start, int length) {
            if(this.inSupertag && this.curPOS!=null) {
                // Get this supertag and add it to the list mapped to by this POS (i.e., the list
                // of supertags seen with this POS in training).
                String temp = new String(ch);      
                temp = temp.substring(start, start+length);
                this.currSTFrag += temp;
            } else if(this.inSupertag) {
                System.err.println("Something is wrong.\nThis is not a well-formed dictionary.");
            }
        }
    }
}
