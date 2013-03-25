///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003 University of Edinburgh (Michael White)
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

// import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
// import java.net.*;


/**
 * Validates XML files against their declared schemas.
 *
 * @author  Michael White
 * @version $Revision: 1.4 $, $Date: 2005/10/20 18:49:42 $
 */
public class Validator {

    public static void main(String[] args) throws Exception {
        
        if (args.length == 0) {
            System.out.println("Usage: java opennlp.ccg.test.Validator <XML files>");
            System.exit(1);
        }

        // configure schema validating XML parser
        XMLReader parser = getXercesSchemaValidatingParser();
        
        // parse
        for (int i = 0; i < args.length; i++) {
            // System.out.println("Parsing: " + args[i]);
            parser.parse(args[i]);
        }
    }

    // NB: this requires xercesImpl.jar, but on the other hand it does not seem 
    //     possible to validate with the version of JAXP that comes with JDK 1.4.1
    //     (cf. JAXP sample SAXLocalNameCount.java)
    private static XMLReader getXercesSchemaValidatingParser() throws Exception {
        String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
        XMLReader parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
        String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";
        String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";
        parser.setFeature(VALIDATION_FEATURE_ID, true);
        parser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, true);
        return parser;
    }
}

