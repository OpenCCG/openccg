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
 * $Id: CCGBankTaskSources.java,v 1.1 2009/11/09 19:21:50 mwhite14850 Exp $ 
 */
package opennlp.ccgbank;

import java.io.File;
import java.util.HashSet;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;


/**
 * Represents a set of source files. This class encapsulates a set of 
 * {@link FileSet}s.
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $Revision: 1.1 $
 * @see <a href="http://ant.apache.org/">Ant home page</a>
 * @see FileSet
 */
public class CCGBankTaskSources extends CCGBankTaskFileGroup<FileSet> {

	/**
	 * Creates a new sources object (required by Ant).
	 */
	public CCGBankTaskSources() {
		super(new HashSet<FileSet>());
	}
	
	
	/**
	 * Adds a file set to this sources object.
	 */
	public void addConfiguredFileSet(FileSet fileSet) {
		addGroup(fileSet);
	}


	/**
	 * Gets the files in the specified group as an array of files. The files in
	 * the returned array are in the order returned by <code>group</code>'s
	 * {@link FileSet#getDirectoryScanner(Project) directory scanner}.
	 */
	@Override
	protected File[] getFiles(FileSet group) {
		Project proj = getProject();
		DirectoryScanner scanner = group.getDirectoryScanner(proj);
		scanner.scan();
		
		String[] fileNames = scanner.getIncludedFiles();
		if(fileNames.length == 0) {
			throw new BuildException("no source files included");
		}
		
		return makeFiles(group.getDir(proj), fileNames);
	}

}
