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
 * $Id: CCGBankTask.java,v 1.5 2010/11/30 18:44:32 mwhite14850 Exp $ 
 */
package opennlp.ccgbank;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import opennlp.ccgbank.parse.TokenMgrError;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Abstract class to provide functionality for applying XSLT to an XML stream.
 * <p>
 * This class is designed to be run as a task from within an Ant build file.
 * There is one concrete implementer for each of the separate tasks of 
 * converting the CCGBank and extracting a grammar for the converted corpus.
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $Revision: 1.5 $
 * @see CCGBankConvert
 * @see CCGBankExtract
 * @see <a href="http://ant.apache.org/">Ant home page</a>
 */
public abstract class CCGBankTask extends Task
		implements TaskContainer,ErrorHandler,ErrorListener {
	
	File target;
	Set<CCGBankTaskSources> ccgBankTaskSources
		= new HashSet<CCGBankTaskSources>();
	List<CCGBankTaskTemplates> ccgBankTaskTemplates
		= new ArrayList<CCGBankTaskTemplates>();
	XSLTProcessor xsltProcessor = null;
	
	boolean useXMLFilter = true;
	boolean terminateOnError = true, terminateOnWarning = false;
		
	/**
	 * Counters
	 */
	int directoriesProcessed, filesProcessed, warnings, errors;
	
	
	/**
	 * Adds a sub-task, but included here only for binary compatibility.
	 * @throws BuildException Always throws an exception, only
	 * sourcesSet or xsltProcessors can be added to this task.
	 */
	public void addTask(Task task) {
		throw new BuildException("nested task \"" + task.getTaskName()
				+ "\" not supported, only sourcesSet or xsltProcessors");
	}

	
	/**
	 * Sets the target directory.
	 * @param target The location of the result of the XSLT conversion (the 
	 * converted corpus, extracted grammar, etc.).
	 * @throws BuildException If the target is <code>null</code>
	 * or not a directory.
	 */
	public void setTarget(File target) {
		this.target = target;
		
		if(target == null) {
			throw new BuildException("no target specified");
		}
		if(!target.exists()) {
			target.mkdirs();
		}
		else if(!target.isDirectory()) {
			throw new BuildException("specified target is not a directory");
		}
	}
	
	/**
	 * @param terminateOnError the terminateOnError to set
	 */
	public void setTerminateOnError(boolean terminateOnError) {
		this.terminateOnError = terminateOnError;
	}


	
	/**
	 * @param terminateOnWarning the terminateOnWarning to set
	 */
	public void setTerminateOnWarning(boolean terminateOnWarning) {
		this.terminateOnWarning = terminateOnWarning;
	}

	
	/**
	 * @param useXMLFilter the useXMLFilter to set
	 */
	public void setUseXMLFilter(boolean useXMLFilter) {
		this.useXMLFilter = useXMLFilter;
	}


	/**
	 * Adds a file set of source files.
	 */
	public void addConfiguredSources(CCGBankTaskSources sources) {
		ccgBankTaskSources.add(sources);
	}
	
	
	/**
	 * Adds a series of xsltProcessors for XSLT transformation.
	 */
	public void addConfiguredTemplates(CCGBankTaskTemplates templates) {
		ccgBankTaskTemplates.add(templates);
	}

	
	/**
	 * Hook to be overridden by subclasses that want notification of the start
	 * of the transformation process.
	 */
	protected void start() throws BuildException {		
		// to be overridden
	}
	
	
	/**
	 * Hook to be overridden by subclasses that want notification of the end
	 * of the transformation process.
	 */
	protected void finish() throws BuildException {		
		// to be overridden
	}

	
	/**
	 * Hook that lets subclasses be notified when processing starts on a new
	 * directory.
	 * @param section The file (directory) on which processing is starting.
	 */
	protected void nextDirectory(File section)
			throws BuildException {		
		// to be overridden
	} 
	

	/**
	 * Hook that lets implementing subclasses know when processing starts on
	 * a new file.
	 * @param file The file on which processing is about to start.
	 * @return The input source to process.
	 */
	protected InputSource nextFile(File file) throws BuildException {
		try {
			return new InputSource(
					new BufferedInputStream(new FileInputStream(file)));
		}
		catch(FileNotFoundException fnfe) {
			throw new BuildException("Unable to find file " + file,
					fnfe, getLocation());
		}
	}
	
	
	/**
	 * Required by {@link ErrorHandler}. Reports the specified error using the
	 * Ant task {@link Task#log(String)} method.
	 */
	public void error(SAXParseException exception) {
		errors++;
		handleError("Error", exception, terminateOnError);
	}

	
	/**
	 * Required by {@link ErrorHandler}. Reports the specified error using the
	 * Ant task {@link Task#log(String)} method.
	 */
	public void fatalError(SAXParseException exception) {
		errors++;
		handleError("Fatal error", exception, terminateOnError);
	}

	
	/**
	 * Required by {@link ErrorHandler}. Reports the specified error using the
	 * Ant task {@link Task#log(String)} method.
	 */
	public void warning(SAXParseException exception) {
		warnings++;
		handleError("Warning", exception, terminateOnWarning);
	}
	
	
	/**
	 * Required by {@link ErrorListener}. Reports the specified error using the
	 * Ant task {@link Task#log(String)} method.
	 */
	public void error(TransformerException exception) {
		errors++;
		handleError("Error", exception, terminateOnError);
	}

	
	/**
	 * Required by {@link ErrorListener}. Reports the specified error using the
	 * Ant task {@link Task#log(String)} method.
	 */
	public void fatalError(TransformerException exception) {
		errors++;
		handleError("Fatal error", exception, terminateOnError);
	}

	
	/**
	 * Required by {@link ErrorListener}. Reports the specified error using the
	 * Ant task {@link Task#log(String)} method.
	 */
	public void warning(TransformerException exception) {
		warnings++;
		handleError("Warning", exception, terminateOnWarning);
	}

	
	/**
	 * Helper method for the methods required by {@link ErrorHandler}.
	 */
	void handleError(String prefix, SAXParseException spe, boolean terminate) {
		StringBuilder sb = new StringBuilder(prefix);
		sb.append(": problem in parse: ");
		sb.append(spe.getSystemId());
		sb.append(" on line ");
		sb.append(spe.getLineNumber());
		sb.append(", column ");
		sb.append(spe.getColumnNumber());
		sb.append(": ");
		sb.append(spe.getMessage());
		
		if(!terminate) {
			log(sb.toString());
		}
		else {
			throw new BuildException(sb.toString(), spe, getLocation());
		}
	}
	
	
	/**
	 * Helper method for the methods required by {@link ErrorListener}.
	 */
	void handleError(String prefix, TransformerException te,
			boolean terminate) {
		StringBuilder sb = new StringBuilder(prefix);
		sb.append(": problem in transform: ");
		sb.append(te.getMessageAndLocation());
		
		if(!terminate) {
			log(sb.toString());
		}
		else {
			throw new BuildException(sb.toString(), te, getLocation());
		}
	}
	
	
	/**
	 * Does the work of transforming the CCGBank and extracting grammars.
	 * @throws BuildException In case no sourcesSet have been specified or an 
	 * error occurs during the transformation process.
	 * <p>
	 * This method calls {@link #start()}, {@link #finish()},
	 * {@link #nextDirectory(File)}, and {@link #nextFile(File)} as required.
	 */
	@Override
	public void execute() throws BuildException {
		if(ccgBankTaskSources.isEmpty()) {
			throw new BuildException("no sourcesSet specified");
		}
				
		filesProcessed = directoriesProcessed = warnings = errors = 0;
		
		start();
		log("Target: " + target);
		
		if(xsltProcessor == null) { // should have been configured
			throw new BuildException("null XSLT processor");
		}
		
		xsltProcessor.addAllTemplates(ccgBankTaskTemplates);
		
		try {
			for(CCGBankTaskSources sources : ccgBankTaskSources) {
				File prevDir = null;
				File currentDir = null;
				
				for(File file : sources) {
					currentDir = file.getParentFile();
					if(!currentDir.equals(prevDir)) {
						log("Processing " + currentDir + " ...");
						directoriesProcessed++;
						
						nextDirectory(currentDir);
					}
					
					prevDir = currentDir;
					
					log("Processing " + file);
					filesProcessed++;
					
					xsltProcessor.process(nextFile(file));
				}
			}
		}
		catch(IOException io) {
			throw new BuildException("I/O problem during processing: " + 
				io.getMessage(), io, getLocation());
		}
		catch(SAXException se) {
			throw new BuildException("Problem during processing: " + 
					se.getMessage(), se, getLocation());
		}
		catch(TransformerException te) {
			throw new BuildException("I/O problem during processing: " + 
					te.getMessageAndLocation(), te, getLocation());
		}
		catch (TokenMgrError te) {
			throw new BuildException("I/O problem during processing: " + 
					te.getMessage(), te, getLocation());
		}
		finally {
			finish();
			
			StringBuilder sb = new StringBuilder("Processed ");
			sb.append(filesProcessed);
			sb.append(" files in ");
			sb.append(directoriesProcessed);
			sb.append(" directories with ");
			sb.append(errors);
			sb.append(" error");
			if(errors != 1) {
				sb.append('s');
			}
			sb.append(" and ");
			sb.append(warnings);
			sb.append(" warning");
			if(warnings != 1) {
				sb.append('s');
			}
			
			log(sb.toString());
		}
	}

}
