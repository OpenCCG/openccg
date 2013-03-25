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

//Program which creates a temp.xml file from the bareparse. temp.xml serves are the input for creating lexicon.xml & morph.xml

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


/**
 * Program which reads in each file of the bare parse xml rep and generates a lexicon, 
 * a freq tally of the lexical info and a list of ccgbank sentences.
 */
public class LexExtract{
	
	public static void extractLex(ExtractionProperties extractProps) throws TransformerException,TransformerConfigurationException,SAXException,IOException,JDOMException {
		
		System.out.println("Extracting lexicon info:");
		
		File lexFile = new File(new File(extractProps.destDir), "lexicon.xml");
		File tempFile = new File(new File(extractProps.tempDir), "temp.xml");
		PrintWriter tempOut = new PrintWriter(new FileOutputStream(tempFile),true);
		
		File ccgbankDir = new File(extractProps.srcDir);
		File[] ccgbankSections=ccgbankDir.listFiles();
		Arrays.sort(ccgbankSections);
		
		FreqTally.CAT_FREQ_CUTOFF = extractProps.catFreqCutoff;
		FreqTally.LEX_FREQ_CUTOFF = extractProps.lexFreqCutoff;
		FreqTally.OPEN_FREQ_CUTOFF = extractProps.openFreqCutoff;
		
		//temp.xml creation

		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer lexExtrTransformer = tFactory.newTransformer(ExtractGrammar.getSource("opennlp.ccgbank/transform/lexExtr.xsl"));
		
		// add root 
		tempOut.println("<ccg-lexicon>");
		
		for (int i=extractProps.startSection; i<=extractProps.endSection; i++){
			
			System.out.println("Section " + ccgbankSections[i].getName());
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
					lexExtrTransformer.transform(new StreamSource(inputFile), new StreamResult(tempOut));
				}
				catch (Exception exc) {
                    System.out.println("Skipping: " + inputFile);
                    System.out.println(exc.toString());
				}
				tempOut.flush();
			}
		}
		
		//Closing the root element
		tempOut.println("</ccg-lexicon>");
		tempOut.flush();
		tempOut.close();
		
		//Generating a freq tally from static datastructures
		FreqTally.printTally(extractProps);
		
		System.out.println("Generating lexicon.xml");
		
		if (tFactory.getFeature(SAXSource.FEATURE) && tFactory.getFeature(SAXResult.FEATURE)) {
			
			SAXTransformerFactory saxTFactory = ((SAXTransformerFactory) tFactory);
			
			// Create an XMLFilter for each stylesheet.
			
			// Extract lexicon from temp.xml
			XMLFilter xmlFilter0 = saxTFactory.newXMLFilter(ExtractGrammar.getSource("opennlp.ccgbank/transform/filterLex.xsl"));
			
			XMLFilter xmlFilter1 = saxTFactory.newXMLFilter(ExtractGrammar.getSource("opennlp.ccgbank/transform/closedCatInsert.xsl"));
			
			XMLFilter xmlFilter2 = saxTFactory.newXMLFilter(ExtractGrammar.getSource("opennlp.ccgbank/transform/insertLF.xsl"));

			XMLFilter xmlFilter3 = saxTFactory.newXMLFilter(ExtractGrammar.getSource("opennlp.ccgbank/transform/insertPunctLF.xsl"));

			XMLFilter xmlFilter4 = saxTFactory.newXMLFilter(ExtractGrammar.getSource("opennlp.ccgbank/transform/insertOrigPunctsLF.xsl"));

			XMLFilter xmlFilter5 = saxTFactory.newXMLFilter(ExtractGrammar.getSource("opennlp.ccgbank/transform/addFilterLexFeats.xsl"));
			
			XMLFilter xmlFilter6 = saxTFactory.newXMLFilter(ExtractGrammar.getSource("opennlp.ccgbank/transform/insertSemFeats.xsl"));
			
			XMLFilter xmlFilter7 = saxTFactory.newXMLFilter(ExtractGrammar.getSource("opennlp.ccgbank/transform/markUnmatched.xsl"));
			
			// Create an XMLReader.
			XMLReader reader = XMLReaderFactory.createXMLReader();
			
			// xmlFilter0 uses the XMLReader as its reader.
			xmlFilter0.setParent(reader);
			xmlFilter1.setParent(xmlFilter0);

			xmlFilter2.setParent(xmlFilter1);
			xmlFilter3.setParent(xmlFilter2);

			if (extractProps.lexF) {
				xmlFilter5.setParent(xmlFilter3);
				xmlFilter6.setParent(xmlFilter5);
			} else if (extractProps.origPuncts) {
				xmlFilter4.setParent(xmlFilter2);
				xmlFilter6.setParent(xmlFilter4);
			}

			else xmlFilter6.setParent(xmlFilter3);

			xmlFilter7.setParent(xmlFilter6);
			XMLFilter xmlFilter = xmlFilter7;
			
			java.util.Properties xmlProps = OutputPropertiesFactory.getDefaultMethodProperties("xml");
			xmlProps.setProperty("indent", "yes");
			xmlProps.setProperty("standalone", "no"); 
    		xmlProps.setProperty("{http://xml.apache.org/xalan}indent-amount", "2");
			Serializer serializer = SerializerFactory.getSerializer(xmlProps);              
			serializer.setOutputStream(new FileOutputStream(lexFile));
			xmlFilter.setContentHandler(serializer.asContentHandler());
			xmlFilter.parse(new InputSource(tempFile.getPath()));
		}
	}
}
