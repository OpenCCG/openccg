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
 * $Id: CCGBankTaskFileGroup.java,v 1.1 2009/11/09 19:21:50 mwhite14850 Exp $ 
 */
package opennlp.ccgbank;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.FileSet;


/**
 * Abstract class providing generalized functionality for file groups used by
 * {@link CCGBankTask}s.
 * <p>
 * For convenience, this class implements {@link Iterable} over {@link File}s.
 * This allows instances to be used in standard iteration constructs while
 * abstracting away from implementation details such as multiple
 * {@link FileSet}s or {@link FileList}s. 
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $Revision: 1.1 $
 */
public abstract class CCGBankTaskFileGroup<G> extends Task implements
		TaskContainer,Iterable<File> {

	protected Collection<G> subGroups;
	
	/**
	 * Creates a file group over the specified collection of subgroups.
	 * @param subGroups The collection over which this class is an abstracted
	 * view.
	 */
	protected CCGBankTaskFileGroup(Collection<G> subGroups) {
		this.subGroups = subGroups;
	}
	
	
	/**
	 * Adds a subgroup to the collection of subgroups this class abstracts over.
	 */
	protected void addGroup(G group) {
		subGroups.add(group);
	}
	
	
	/**
	 * Gets all the files in a group as an array. To be implemented by extending
	 * classes, as {@link FileSet} and {@link FileList} represent files 
	 * differently.
	 * @return The collection of files in <code>group</code>, as an array.
	 */
	protected abstract File[] getFiles(G group); 
	
	
	/**
	 * Creates an array of files given a directory and an array of file names
	 * (specified relative to that directory).
	 * @param directory The directory that the specified file names are relative
	 * to.
	 * @param fileNames The file names, relative to the specified directory.
	 * @return An array containing all the files as specified relative to the
	 * specified directory.
	 */
	protected File[] makeFiles(File directory, String[] fileNames) {
		File[] files = new File[fileNames.length];
		
		for(int i = 0; i < fileNames.length; i++) {
			files[i] = new File(directory, fileNames[i]);
		}
		
		return files;
	}
	
	
	/**
	 * Included for binary compatibility with {@link TaskContainer}.
	 * @throws BuildException Always throws a build exception, as only the
	 * parameterized type of this class's subgroups can be contained by this
	 * task.
	 */
	public void addTask(Task task) {
		throw new BuildException("nested task \"" + task
				+ "\" not supported, only "
				+ subGroups.getClass().getTypeParameters()[0]
				  .getGenericDeclaration());
	}
	
	
	/**
	 * Provides an
	 * iterator over all the files in the collection of subgroups contained by
	 * this instance. The iterator returned will iterate through files in all
	 * the subgroups returned in the same order as the order returned by the
	 * subgroups collection. 
	 */
	public Iterator<File> iterator() {
		return new AllFileView();
	}

	
	/**
	 * Implements an iterator over the files contained in the subgroups
	 * collection. This class iterates over all the files contained in the
	 * groups in the subgroups collection, in the order that they
	 * are returned by the subgroups collection.
	 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
	 * @version $Revision: 1.1 $
	 */
	class AllFileView implements Iterator<File> {

		Iterator<G> groupIterator = subGroups.iterator();
		Iterator<File> currentIterator;
		
		/**
		 * Tests whether there is a next file.
		 * @return true If the current subgroup contains a next file, or if
		 * there is a next subgroup that is non-empty.
		 */
		public boolean hasNext() {
			while((currentIterator == null || !currentIterator.hasNext())
					&& groupIterator.hasNext()) {
				currentIterator = new FileArrayIterator(
						getFiles(groupIterator.next()));
			}
			
			// current may be empty
			return (currentIterator != null && currentIterator.hasNext()); 
		}
		
		
		/**
		 * Gets the next file in the series, as returned in order by the 
		 * subgroups collection.
		 * @throws NoSuchElementException If the collection of subgroups is
		 * exhausted.
		 */
		public File next() {
			if(!hasNext()) {
				throw new NoSuchElementException("elements exhausted");
			}
			
			return currentIterator.next();
		}
		
		/**
		 * Included only for binary compatibility with {@link Iterator}.
		 * @throws UnsupportedOperationException Always, as this operation is
		 * not supported.
		 */
		public void remove() {
			throw new UnsupportedOperationException("removed not supported");
		}
		
	}
	
	
	/**
	 * Implements an iterator view of an array of {@link File} objects.
	 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
	 * @version $Revision: 1.1 $
	 */
	class FileArrayIterator implements Iterator<File> {

		File[] array;
		int index = 0;
		
		
		/**
		 * Creates a new iterator view over the specified array of files.
		 * @param array The file array backing this iterator view.
		 */
		FileArrayIterator(File[] array) {
			this.array = array;
		}
		
		/**
		 * Tests whether the array of files is exhausted.
		 * @return true If the current index is less than the array length.
		 */
		public boolean hasNext() {
			return (index < array.length);
		}

		/**
		 * Gets the next file in series, as specified by the array backing this
		 * iterator view.
		 * @throws NoSuchElementException If the array of files is exhausted.
		 * @see #hasNext()
		 */
		public File next() {
			if(!hasNext()) {
				throw new NoSuchElementException("elements exhausted");
			}
			
			return array[index++];
		}

		/**
		 * Included only for binary compatibility with {@link Iterator}.
		 * @throws UnsupportedOperationException Always, as this operation is
		 * not supported.
		 */
		public void remove() {
			throw new UnsupportedOperationException("remove not supported");
		}
		
	}
}
