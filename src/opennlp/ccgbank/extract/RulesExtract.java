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

//Program which extracts unary rules and their frequencies and finally outputs the rules.xml file

package opennlp.ccgbank.extract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import opennlp.ccgbank.extract.ExtractGrammar.ExtractionProperties;

import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.jdom.JDOMException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class RulesExtract {
	
	public static void extractRules(ExtractionProperties extractProps) throws TransformerException, TransformerConfigurationException,SAXException, IOException,JDOMException{
		
		System.out.println("Extracting rule info:");
		
		File rulesFile = new File(new File(extractProps.destDir), "rules.xml");
		File tempFile = new File(new File(extractProps.tempDir), "temp-rules.xml");
		PrintWriter tempOut=new PrintWriter(new FileOutputStream(tempFile),true);
		
		File ccgbankDir = new File(extractProps.srcDir);
		File[] ccgbankSections=ccgbankDir.listFiles();
		Arrays.sort(ccgbankSections);
		
		RulesTally.RULE_FREQ_CUTOFF = extractProps.ruleFreqCutoff;
        RulesTally.KEEP_UNMATCHED = !extractProps.skipUnmatched;
		
		// add root
		tempOut.println("<rules>");
		
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer(ExtractGrammar.getSource("opennlp.ccgbank/transform/rulesExtr.xsl"));
		
		for (int i=extractProps.startSection; i<=extractProps.endSection; i++){
			
			File[] files=ccgbankSections[i].listFiles();
			Arrays.sort(files);
			
			int fileStart = 0; int fileLimit = files.length;
			if (extractProps.fileNum >= 0) {
				fileStart = extractProps.fileNum;
				fileLimit = extractProps.fileNum + 1;
			}
			
			for (int j=fileStart; j<fileLimit; j++){
				String inputFile=files[j].getAbsolutePath();
				if (j == fileStart) System.out.print(files[j].getName() + " ");
				else if (j == (fileLimit-1)) System.out.println(" " + files[j].getName());
				else System.out.print(".");
				if (fileStart == fileLimit-1) System.out.println();
				try {
					transformer.transform(new StreamSource(inputFile),new StreamResult(tempOut));
				}
				catch (Exception exc) {
                    System.out.println("Skipping: " + inputFile);
                    System.out.println(exc.toString());
				}
				tempOut.flush();
			}
		}
		
		tempOut.flush();
		tempOut.println("</rules>");
		tempOut.close();
		
		RulesTally.printTally(extractProps);
		
		System.out.println("Generating rules.xml");
		
		if (tFactory.getFeature(SAXSource.FEATURE) && tFactory.getFeature(SAXResult.FEATURE)){
			
			SAXTransformerFactory saxTFactory = ((SAXTransformerFactory) tFactory);
			
			// Create an XMLFilter for each stylesheet.
			XMLFilter xmlFilter1 = saxTFactory.newXMLFilter(ExtractGrammar.getSource("opennlp.ccgbank/transform/ccgRules.xsl"));
			

			//XMLFilter xmlFilter3 = saxTFactory.newXMLFilter(new StreamSource("foo3.xsl"));
			
			// Create an XMLReader.
			XMLReader reader = XMLReaderFactory.createXMLReader();
			
			// xmlFilter1 uses the XMLReader as its reader.
			xmlFilter1.setParent(reader);
			
			java.util.Properties xmlProps = OutputPropertiesFactory.getDefaultMethodProperties("xml");
			xmlProps.setProperty("indent", "yes");
			xmlProps.setProperty("standalone", "no"); 
			xmlProps.setProperty("{http://xml.apache.org/xalan}indent-amount", "2");
			Serializer serializer = SerializerFactory.getSerializer(xmlProps);
			serializer.setOutputStream(new FileOutputStream(rulesFile));


			XMLFilter xmlFilter = xmlFilter1;
			xmlFilter.setContentHandler(serializer.asContentHandler());
			xmlFilter.parse(new InputSource(tempFile.getPath()));
		}
		
		//Deleting the temporory lex file
		//lexiconTempFile.delete();
	}
}
