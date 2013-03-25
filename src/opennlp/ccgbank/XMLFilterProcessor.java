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
 * $Id: XMLFilterProcessor.java,v 1.4 2010/09/05 15:54:43 mwhite14850 Exp $
 * Copyright (C) 2009 Scott Martin (http://www.coffeeblack.org/contact/)
 */
package opennlp.ccgbank;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.apache.tools.ant.BuildException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * An implementation of {@link XSLTProcessor} that performs transformations
 * using an {@link XMLFilter}. This particular implementation uses the XSLTC
 * compiler distributed with Apache's Xalan in order to avoid the known 
 * problems with re-using {@link XMLFilter}.
 * @see <a href="http://xml.apache.org/xalan-j/">Apache Xalan</a>
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $Revision: 1.4 $
 */
class XMLFilterProcessor extends XSLTProcessor {
	
	// nb: this could be in the super class
	List<Templates> templates = null;
	
	XMLFilter filter;
	ErrorHandler errorHandler;
	
	static final String
		XSLT_KEY = "javax.xml.transform.TransformerFactory",
		XSLTC_VALUE = "org.apache.xalan.xsltc.trax.TransformerFactoryImpl";
	
	XMLFilterProcessor(ErrorListener errorListener, ErrorHandler errorHandler) {
		super(errorListener);
		this.errorHandler = errorHandler;
	}
	
	SAXTransformerFactory newTransformerFactory() {
		// TODO try using xsltc (seems to yield hard-to-trace bugs at the moment)
		//System.setProperty(XSLT_KEY, XSLTC_VALUE);
		return super.newTransformerFactory();
	}
	
	/* (non-Javadoc)
	 * @see opennlp.ccgbank.XSLTProcessor#process(java.io.File)
	 */
	@Override
	void process(InputSource inputSource) throws IOException,SAXException,
			TransformerException {
		// TODO figure out how to re-use filter without breaking :(
		// make new filter each time
		filter = makeFilter(taskTemplatesList);
		filter.setContentHandler(serializer.asContentHandler());
		filter.parse(inputSource);
	}
	
	/**
	 * Makes a filter from a single xsltProcessors object.
	 * @see #makeFilter(List)
	 */
	XMLFilter makeFilter(CCGBankTaskTemplates templates)
		throws FileNotFoundException,SAXException,
			TransformerConfigurationException {
		return makeFilter(Collections.singletonList(templates));
	}
	
	
	/**
	 * Makes a filter from a series of xsltProcessors that applies those 
	 * templates in order.
	 * @param templateList The series of xsltProcessors used to construct the
	 * filter.
	 * @throws BuildException If no xsltProcessors are specified.
	 */
	XMLFilter makeFilter(List<CCGBankTaskTemplates> templateList)
		throws FileNotFoundException,SAXException,
			 TransformerConfigurationException {

		// make templates
		if(templates == null) {
			templates = makeTemplates(taskTemplatesList);
		}
		
		// assemble list of xslt templates into a filter
		XMLFilter currentFilter = null, previousFilter = null;
		for (Templates t : templates) {
			currentFilter = transformerFactory.newXMLFilter(t);
			currentFilter.setErrorHandler(errorHandler);
			
			if(previousFilter == null) { // it's the first one
				currentFilter.setParent(
						XMLReaderFactory.createXMLReader());					
			}
			else {
				currentFilter.setParent(previousFilter);
			}
			
			previousFilter = currentFilter;
		}
		
		if(currentFilter == null ) {
			throw new IllegalArgumentException("no templates specified");
		}
		
		currentFilter.setErrorHandler(errorHandler);
		currentFilter.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
		
		return currentFilter;
	}
}
