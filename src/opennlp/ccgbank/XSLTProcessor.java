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
 * $Id: XSLTProcessor.java,v 1.4 2010/09/05 15:54:43 mwhite14850 Exp $
 * Copyright (C) 2009 Scott Martin (http://www.coffeeblack.org/contact/)
 */
package opennlp.ccgbank;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.BuildException;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Abstract base class for XSLT processing. Templates are added to a processor,
 * then {@link #process(InputSource)} is called for each input source. 
 * Subclasses will implement different processing strategies for transforming
 * XML using a series of XSL templates.
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $Revision: 1.4 $
 */
abstract class XSLTProcessor {
	
	SAXTransformerFactory transformerFactory = newTransformerFactory();
	
	static final Properties xmlProperties
		= OutputPropertiesFactory.getDefaultMethodProperties("xml");
	
	static {
		xmlProperties.setProperty(OutputKeys.INDENT, "yes");
		xmlProperties.setProperty(
				"{http://xml.apache.org/xalan}indent-amount", "2");
	}
	
	List<CCGBankTaskTemplates> taskTemplatesList
		= new ArrayList<CCGBankTaskTemplates>();
	Serializer serializer = SerializerFactory.getSerializer(
			XSLTProcessor.xmlProperties);
	ErrorListener errorListener;	
	
	XSLTProcessor(ErrorListener errorListener) {
		this.errorListener = errorListener;
		transformerFactory.setErrorListener(errorListener);
	}
	
	boolean addAllTemplates(List<CCGBankTaskTemplates> templateList) {
		boolean b = false;
		
		for(CCGBankTaskTemplates t : templateList) {
			b |= addTemplates(t);
		}
		
		return b;
	}
	
	boolean addTemplates(CCGBankTaskTemplates taskTemplates) {
		return taskTemplatesList.add(taskTemplates);
	}
	
	
	/**
	 * Processes an input source, applying each of the templates specified 
	 * using {@link #addTemplates(CCGBankTaskTemplates)} or
	 * {@link #addAllTemplates(List)}. Subclasses will actually implement this
	 * method.
	 * @param inputSource The input source to which the templates will be
	 * applied.
	 * @throws IOException If a problem reading or writing occurs.
	 * @throws SAXException If a subclass uses a SAX processor and there is a 
	 * problem with it.
	 * @throws TransformerException If a subclass uses a processor that causes
	 * a transformer problem.
	 */
	abstract void process(InputSource inputSource)
		throws IOException,SAXException,TransformerException;
	
	void setTarget(File file) throws FileNotFoundException {
		serializer.setOutputStream(
			new BufferedOutputStream(new FileOutputStream(file)));
		// ensure output properties set (shouldn't really be nec!)
		serializer.setOutputFormat(xmlProperties);
	}
	
	/**
	 * Resets the serializer, if resetting is possible. If not, re-creates the
	 * serializer.
	 */
	void resetSerializer() {
		if(!serializer.reset()) {
	    	serializer // create new unless re-useable
	    		= SerializerFactory.getSerializer(xmlProperties);
	    }
	}
	
	SAXTransformerFactory newTransformerFactory() {
		SAXTransformerFactory tf
			= (SAXTransformerFactory)TransformerFactory.newInstance();
		
		if(!tf.getFeature(SAXSource.FEATURE)) {
			throw new IllegalStateException(
				"SAX transformer factory does not support SAXSource");
		}
		if(!tf.getFeature(SAXResult.FEATURE)) {
			throw new IllegalStateException(
				"SAX transformer factory does not support SAXResult");
		}
		
		return tf;
	}
	
	/**
	 * Makes a list of templates from a single xsltProcessors object.
	 * @see #makeTemplates(List)
	 */
	List<Templates> makeTemplates(CCGBankTaskTemplates taskTemplates) 
			throws FileNotFoundException,TransformerConfigurationException {
		return makeTemplates(Collections.singletonList(taskTemplates));
	}
	
	
	/**
	 * Makes a list of templates from a series of xsltProcessors that applies those xsltProcessors
	 * in order.
	 * @param templateList The series of xsltProcessors used to construct the
	 * filter.
	 * @throws BuildException If no xsltProcessors are specified.
	 */
	List<Templates> makeTemplates(List<CCGBankTaskTemplates> templateList)
			throws FileNotFoundException,TransformerConfigurationException {
		List<Templates> l = new ArrayList<Templates>();
		
		for(CCGBankTaskTemplates taskTemplates : templateList) {
			for(File f : taskTemplates) {
				StreamSource ss = new StreamSource(
					new BufferedInputStream(new FileInputStream(f)));
				ss.setSystemId(f);
				
				l.add(transformerFactory.newTemplates(ss));				
			}
		}
		
		return Collections.unmodifiableList(l);
	}
}
