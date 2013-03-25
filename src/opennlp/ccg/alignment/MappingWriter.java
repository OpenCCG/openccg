//////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2012 Scott Martin
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
package opennlp.ccg.alignment;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * A writer for outputting {@link Mapping}s to a specified underlying {@link Writer}.
 * <p>
 * Mapping writers write mappings by group, so that before any mappings are written,
 * {@link #startGroup(MappingGroup)} must always be called
 * to signal to the writer that a new group is starting (and what its length is). The outgoing mappings are
 * formatted according to a {@link MappingFormat} specified at creation.
 * <p>
 * After a new group is started, exactly the {@linkplain MappingGroup#getLength() number of mappings} in that
 * group must be written. Otherwise, an {@link IOException} is thrown. An {@link IOException} is also thrown
 * if an attempt is made to write a mapping whose {@linkplain Mapping#getPhraseNumber() id} is different from the 
 * current group's {@linkplain MappingGroup#getPhraseNumber() id}, or if {@link #writeMapping(Mapping)} is called without
 * first calling {@link #startGroup(MappingGroup)}.
 * <p>
 * Example usage:
 * <blockquote><pre>
 * MappingWriter mw = ...;
 * 
 * // while there are more groups
 * mw.startGroup(new MappingGroup(...));
 * while(mw.canWrite()) {
 * 	mw.writeMapping(...);
 * }
 * 
 * mw.close();</pre></blockquote>
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 */
public class MappingWriter extends FilterWriter {

	final MappingFormat format;
	
	private MappingGroup currentGroup;
	private int mappingCount = 0;
	
	private String mappingDelimiter, groupDelimiter;
	
	/**
	 * Creates a new mapping writer.
	 * @param out The underlying writer.
	 * @param format The mapping format to use.
	 * @throws IllegalArgumentException if <tt>format</tt> is <tt>null</tt>.
	 */
	public MappingWriter(Writer out, MappingFormat format) {
		super(out);
		
		if(format == null) {
			throw new IllegalArgumentException("format is null");
		}
		
		this.format = format;
	}
		
	/**
	 * Gets the mapping format used by this writer.
	 */
	public MappingFormat getFormat() {
		return format;
	}
	
	/**
	 * Gets the current mapping group being written.
	 */
	public MappingGroup getCurrentGroup() {
		return currentGroup;
	}
	
	void checkWrite() throws IOException {
		if(!canWrite()) {
			throw new IOException("unable to write");
		}
	}
	
	void checkMappingCount() throws IOException {
		if(currentGroup != null && mappingCount != currentGroup.length) {
			throw new IOException("incorrect mapping count for group " +
					+ currentGroup.phraseNumber + "; expected "
					+ currentGroup.length + ", but was " + mappingCount);
		}
	}
	
	/**
	 * Starts a new mapping group for writing mappings. If {@link #endGroup()} was not called explicitly,
	 * it is first called to end the current group.
	 * @param mappingGroup The group to start.
	 * @throws IOException If the number of mappings written since the last call to
	 * {@link #startGroup(MappingGroup)}does not exactly equal the length of the
	 * {@linkplain #getCurrentGroup() current group}.
	 * @see #writeMapping(Mapping)
	 * @see #endGroup()
	 */
	public void startGroup(MappingGroup mappingGroup) throws IOException {
		if(currentGroup != null) {
			endGroup();	
		}
		
		currentGroup = mappingGroup;
	}
	
	/**
	 * Ends the current group, writing the {@linkplain EncodingScheme#getGroupDelimiter() proper group
	 * delimiter} for the {@linkplain #getFormat() mapping format in effect}.
	 * @throws IOException If {@link #startGroup(MappingGroup)} was not first called, or if the correct number
	 * of mappings for the current group was not written.
	 * @see #startGroup(MappingGroup)
	 */
	public void endGroup() throws IOException {
		if(currentGroup == null) {
			throw new IOException("no current group");
		}
		
		checkMappingCount();
		
		if(currentGroup.length > 0) {
			if(groupDelimiter == null) {
				Character gd = format.encodingScheme.getGroupDelimiter();
				
				groupDelimiter = AbstractEncodingScheme.isLineSeparator(gd)
						? System.getProperty("line.separator") : String.valueOf(gd);
			}
			
			out.write(groupDelimiter); // no empty lines
		}
		
		currentGroup = null;
		mappingCount = 0;
	}
	
	/**
	 * Writes to the underlying writer, first checking if mappings can be written.
	 * @see #canWrite()
	 */
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		checkWrite();
		super.write(cbuf, off, len);
	}

	/**
	 * Writes to the underlying writer, first checking if mappings can be written.
	 * @see #canWrite()
	 */
	@Override
	public void write(int c) throws IOException {
		checkWrite();
		super.write(c);
	}

	/**
	 * Writes to the underlying writer, first checking if mappings can be written.
	 * @see #canWrite()
	 */
	@Override
	public void write(String str, int off, int len) throws IOException {
		checkWrite();
		super.write(str, off, len);
	}

	/**
	 * Writes to the underlying writer, first checking if mappings can be written.
	 * @see #canWrite()
	 */
	@Override
	public void write(char[] cbuf) throws IOException {
		checkWrite();
		super.write(cbuf);
	}

	/**
	 * Writes to the underlying writer, first checking if mappings can be written.
	 * @see #canWrite()
	 */
	@Override
	public void write(String str) throws IOException {
		checkWrite();
		super.write(str);
	}
	
	/**
	 * Tests whether the mapping writer is currently in a state in which mappings can be written without
	 * throwing an {@link IOException}. Mappings
	 * can only be written when a current group has been {@linkplain #startGroup(MappingGroup) started} and 
	 * the number of mappings written since the last group started is less than the total
	 * {@linkplain MappingGroup#getLength() length} of the current group.
	 * @return true If the {@linkplain #getCurrentGroup() current group} is non-null and the number of mappings
	 * written to the current group is less than its length.
	 */
	public boolean canWrite() {
		return currentGroup != null && mappingCount < currentGroup.length; 
	}

	/**
	 * Writes a mapping to the underlying writer, formatted by {@linkplain #getFormat() the mapping format}. If
	 * other mappings have been written since the last call to {@link #startGroup(MappingGroup)}, the
	 * {@linkplain EncodingScheme#getMappingDelimiter() mapping delimiter} used by the current format is first
	 * written.
	 * @param mapping The mapping to write.
	 * @throws IOException If {@link #canWrite()} returns <code>false</code>, if
	 * {@link #startGroup(MappingGroup)} was not first called, or if an attempt is made
	 * to write a mapping with an {@linkplain Mapping#getPhraseNumber() id} that does not equal the current group's
	 * {@linkplain MappingGroup#getPhraseNumber() id}.
	 */
	public void writeMapping(Mapping mapping) throws IOException {
		checkWrite();
		if(mapping == null) {
			throw new NullPointerException("null mapping");
		}		
		if(mapping.phraseNumber != null && !mapping.phraseNumber.equals(currentGroup.phraseNumber)) {
			throw new IOException("mapping from group " + mapping.phraseNumber
				+ ", but current group is " + currentGroup.phraseNumber);
		}
		
		if(mappingCount > 0) {
			if(mappingDelimiter == null) {
				Character md = format.encodingScheme.getMappingDelimiter();
				mappingDelimiter = AbstractEncodingScheme.isLineSeparator(md)
						? System.getProperty("line.separator") : String.valueOf(md);
			}
			
			out.write(mappingDelimiter);
		}		
		
		out.write(format.formatMapping(mapping));
		mappingCount++;
	}

	/**
	 * Overrides the superclass method to first check that the correct number of mappings were written.
	 * @throws IOException If a number of mappings have been written that does not exactly equal the length 
	 * of the {@linkplain #getCurrentGroup() current group}.
	 */
	@Override
	public void close() throws IOException {
		try {
			checkMappingCount();
		}
		finally {
			super.close();
		}
	}
}
