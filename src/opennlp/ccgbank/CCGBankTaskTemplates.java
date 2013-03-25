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
 * $Id: CCGBankTaskTemplates.java,v 1.1 2009/11/09 19:21:50 mwhite14850 Exp $ 
 */
package opennlp.ccgbank;

import java.io.File;
import java.util.ArrayList;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileList;


/**
 * Represents a series of templates. This class encapsulates a list of lists of
 * {@link FileList}s.
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $Revision: 1.1 $
 * @see <a href="http://ant.apache.org/">Ant home page</a>
 * @see FileList
 */
public class CCGBankTaskTemplates extends CCGBankTaskFileGroup<FileList> {

	/**
	 * File types/names for the generated OpenCCG-format grammar files.
	 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
	 * @version $Revision: 1.1 $
	 */
	enum Type {
		/**
		 * The lexicon file.
		 */
		LEXICON, 
		
		/**
		 * The file containing morphological information.
		 */
		MORPH, 
		
		/**
		 * The file where the grammar rules are stored.
		 */
		RULES;
		
		/**
		 * Gets a filename corresponding to a given file type.
		 * @return The file type's name, lowercased, with the string 
		 * <code>".xml"</code> appended. Example: for <code>LEXICON</code>,
		 * returns the string <code>lexicon.xml</code>.
		 */
		String fileName() {
			StringBuilder sb = new StringBuilder(name().toLowerCase());
			sb.append(".xml");
			return sb.toString();
		}
	}
	
	Type type = null;
	
	
	/**
	 * Creates a new xsltProcessors object (no-arg constructor required by Ant).
	 */
	public CCGBankTaskTemplates() {
		super(new ArrayList<FileList>());
	}
	
	/**
	 * Adds a file list to the list of transforms.
	 * @param fileList The <code>FileList</code> object to add.
	 */
	public void addConfiguredFilelist(FileList fileList) {
		addGroup(fileList);
	}

	
	/**
	 * Gets the list of files contained in <code>group</code> as an array.
	 * The order of files in the returned array is the same as the order 
	 * of <code>group</code>'s {@link FileList#getFiles(Project) files}.
	 */
	@Override
	protected File[] getFiles(FileList group) {
		Project proj = getProject();
		return makeFiles(group.getDir(proj), group.getFiles(proj));
	}

	/**
	 * Sets the {@link CCGBankTaskTemplates#type file type}.
	 * @param typeName The name of the type to set. The actual type is
	 * coerced using {@link Enum#valueOf(Class, String)} using
	 * <code>typeName</code> as an argument.
	 */
	public void setType(String typeName) {
		this.type = Type.valueOf(typeName.toUpperCase());
	}
}
