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
 * $Id: TemplatesProcessor.java,v 1.2 2010/09/04 16:24:36 mwhite14850 Exp $
 * Copyright (C) 2009 Scott Martin (http://www.coffeeblack.org/contact/)
 */
package opennlp.ccgbank;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Implements an XSLT processor using {@link Templates}. This class processes
 * XSLT template objects successively with a given input, writing the output
 * of each successive transformation into memory, then feeding that output to
 * the next template in the chain.
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $Revision: 1.2 $
 */
class TemplatesProcessor extends XSLTProcessor {
	List<Templates> templates = null;
	
	TemplatesProcessor(ErrorListener errorListener) {
		super(errorListener);
	}
	
	void addTemplates(Templates t) {
		if(templates == null) {
			templates = new ArrayList<Templates>();
		}
		
		templates.add(t);
	}
	
	/* (non-Javadoc)
	 * @see opennlp.ccgbank.XSLTProcessor#process(java.io.File)
	 */
	@Override
	void process(InputSource inputSource) throws IOException,SAXException,
			TransformerException {
		if(templates == null) {
			templates = makeTemplates(taskTemplatesList);
		}
		
		StreamSource input = new InputSourceAdapter(inputSource);
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] bytesIn = null;
		
		try {
			// transform input with each template successively,
			// writing the output of each to a memory buffer
			Iterator<Templates> i = templates.iterator();
			Source source;
			StreamSource memorySource = null;
			while(i.hasNext()) {
				if(bytesIn == null) { // first pass?
					source = input; // use source
				}
				else { // use buffer otherwise
					InputStream in = new ByteArrayInputStream(bytesIn);
					if(memorySource == null) {
						memorySource = new StreamSource(in);						
					}
					else {
						memorySource.setInputStream(in);
					}
					
					source = memorySource;
				}
				
				// get and configure transformer for this template
				Templates template = i.next();
				Transformer transformer = template.newTransformer();
				transformer.setOutputProperties(xmlProperties);
				transformer.setErrorListener(errorListener);
				
				boolean ihn = i.hasNext(); // reuse
				
				Result result = ihn // last template?
					? new StreamResult(buffer)
					// if it's the last, write output to file
					: new StreamResult(new BufferedOutputStream(
							serializer.getOutputStream()));
										
				transformer.transform(source, result);
				
				if(ihn) {
					bytesIn = buffer.toByteArray();
					buffer.reset();
				}
			}
		}
		finally {
			bytesIn = null;
			try {
				buffer.close();
			}
			catch(IOException e) {
				// do nothing
			}
		}
	}
}
