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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Class for reading {@link Mapping}s from an underlying reader based on a specified {@link MappingFormat}.
 * <p>
 * Instances of this class read mappings group by group, with {@link #nextGroup()} called after reading the
 * correct number of mappings for the current group, as signaled by the {@link #canRead()} method.
 * <p>
 * The following code fragment illustrates the usage of this class:
 * <blockquote><pre>
 * MappingReader reader = ...;
 * 
 * MappingGroup g;
 * while((g = reader.getNextGroup()) != null) {
 * 	while(reader.canRead()) {
 * 		Mapping m = reader.readMapping();
 * 		...
 * 	}
 * }</pre></blockquote>
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @see MappingFormat
 */
public class MappingReader extends FilterReader {

	final MappingFormat format;
	
	private MappingGroup currentGroup;	
	private Queue<Mapping> mappingQueue;
	private int mappingCount = 0;
	private boolean skipLF = false;
	
	/**
	 * Creates a mapping reader.
	 * @param r The underlying reader.
	 * @param format The mapping format to use for reading {@link Mapping}s.
	 * @throws IllegalArgumentException if <tt>format</tt> is <tt>null</tt>.
	 */
	public MappingReader(Reader r, MappingFormat format) {
		super(r);
		
		if(format == null) {
			throw new IllegalArgumentException("format is null");
		}
		
		this.format = format;
		mappingQueue = new LinkedList<Mapping>();		
	}
	
	/**
	 * @return The format used to read mappings.
	 */
	public MappingFormat getFormat() {
		return format;
	}
	
	/**
	 * Starts reading from the next mapping group.
	 * @return The next {@link MappingGroup} found by reading from the underlying reader.
	 * @throws IOException If a {@link ParseException} is encountered when calling
	 * {@link MappingFormat#parseMapping(String)} based on the underlying input, or if one is thrown by the
	 * underlying reader. An IOException is also thrown if the number of mappings in the
	 * {@linkplain MappingGroup#getLength() current group} could not be read. 
	 */
	public MappingGroup nextGroup() throws IOException {
		checkMappingCount();
		mappingCount = 0;
		
		MappingGroup previous = (currentGroup == null) ? null : currentGroup;
		int newCount = mappingQueue.size();
		
		currentGroup = (newCount == 0)
			? null : new MappingGroup(mappingQueue.peek().phraseNumber, newCount);
		
		boolean eog = false;
		
		while(!eog) {
			StringBuilder sb = new StringBuilder();
			
			int i;
			while((i = in.read()) != -1) {
				char c = (char)i;
				
				if(skipLF) {
					skipLF = false;
					if(c == '\n') {
						continue;
					}
				}
				
				if(c == '\r') {
					skipLF = true;
				}
				
				if(format.encodingScheme.isMappingDelimiter(c)) {
					break;
				}
				else if(format.encodingScheme.isGroupDelimiter(c)) {
					eog = true;
					break;
				}
				else {
					sb.append(c); 
				}
			}
			
			if(sb.length() == 0) {
				break; // for EOF and end of group
			}
			
			Mapping a = null;
			try {
				a = format.parseMapping(sb.toString());
			}
			catch(ParseException pe) {
				throw new IOException(((currentGroup == null) ? ""
						: "group " + currentGroup.phraseNumber + ": ") + "problem formatting mapping "
						+ sb.toString() + " at offset " + pe.getErrorOffset() + ": " + pe.getMessage(), pe);
			}
			
			// if the format allows null IDs, use previous's running counter
			if(currentGroup == null) {
				Integer I = (a.phraseNumber == null)
					? (previous == null) ? format.encodingScheme.getPhraseNumberBase().start
							: previous.phraseNumber + 1
					: a.phraseNumber;
				
				currentGroup = new MappingGroup(I, 0);
			}
			
			if(a.phraseNumber == null) {
				// have to copy because phraseNumber is immutable (and final)
				a = a.copyWithPhraseNumber(currentGroup.phraseNumber);
			}
			
			if(!currentGroup.phraseNumber.equals(a.phraseNumber)) {
				eog = true;
			}
			else {
				newCount++; // only increment if should be read
			}			
			
			if(!mappingQueue.offer(a)) { // save for next read
				throw new IOException("unable to read mapping");
			}
		}
		
		if(currentGroup != null) {
			currentGroup.length = newCount;
		}
		
		return (currentGroup == null || currentGroup.length == 0) ? null : currentGroup;
	}
	
	/**
	 * Tests whether mappings can be read from this reader without throwing an {@link IOException}.
	 * @return true If there is a current mapping group and mappings remain to be read from it.
	 * @see #nextGroup()
	 * @see #readMapping()
	 */
	public boolean canRead() {
		return currentGroup != null && mappingCount < currentGroup.length;
	}
	
	/**
	 * Overrides the superclass method to check first if any mappings are available.
	 * @throws IOException If no mappings are available.
	 * @see #canRead()
	 */
	@Override
	public int read() throws IOException {
		checkRead();
		int c = super.read();
		if(skipLF) {
			skipLF = false;
			if(c == '\n') {
				c = super.read();
			}
		}
		
		return c;
	}

	/**
	 * Overrides the superclass method to check first if any mappings are available.
	 * @throws IOException If no mappings are available.
	 * @see #canRead()
	 */
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		checkRead();
		
		if(len < 1) {
			return 0;
		}
		
		if(skipLF) {
			int c = read();
			skipLF = false;
			
			if(c == -1) {
				return c;
			}
			else if(c != '\n') {
				cbuf[off++] = (char)c;
				len--;
			}
		}
		
		return super.read(cbuf, off, len);
	}

	/**
	 * Tests whether mappings can be read without blocking.
	 * @return true If it is guaranteed that a call to {@link #readMapping()} will not block for input.
	 */
	@Override
	public boolean ready() throws IOException {
		return canRead();
	}

	/**
	 * Reads a mapping from the underlying reader, if one is {@linkplain #canRead() available}.
	 * @return A mapping formatted by the {@linkplain #getFormat() format in effect}.
	 * @throws IOException If no mappings are available in the current group.
	 * @see #canRead()
	 */
	public Mapping readMapping() throws IOException {
		checkRead();
		
		Mapping a = mappingQueue.poll();
		
		if(a != null) {
			mappingCount++;
		}
		
		return a;
	}
	
	/**
	 * Closes this reader, checking first if the correct number of mappings were read.
	 * @throws IOException If mappings remain to be read from the current group.
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
	
	void checkRead() throws IOException {
		if(!canRead()) {
			throw new IOException("no mappings available");
		}
	}

	void checkMappingCount() throws IOException {
		if((currentGroup == null && mappingCount > 0) 
				|| (currentGroup != null && mappingCount != currentGroup.length)) {
			throw new IOException(
				currentGroup == null ? "" : "group " + currentGroup.phraseNumber + ": "
					+ "mapping count does not match: expected "
					+ ((currentGroup == null) ? 0 : currentGroup.length)
					+ ", but was " + mappingCount);
		}
	}
}
