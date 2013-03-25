///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2008-2013 Michael White
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

package opennlp.ccg.perceptron;

import java.util.*;
import java.util.zip.*;
import java.io.*;

import opennlp.ccg.synsem.Sign;

/**
 * An abstract representation of an event file, whose syntax is a more readable version 
 * of what's used in the TADM toolkit.  A constructor flag controls whether to keep the 
 * events in memory (defaults to false).
 * 
 * An event file may be given an alphabet, which allows features to be filtered to 
 * just those present in the alphabet, when it's closed; otherwise, the 
 * alphabet is constructed dynamically.
 * 
 * The concrete syntax of an event file is as follows.
 * An event file consists of a sequence of blocks.
 * A block starts with the number of events on a line by itself.
 * It is followed by each event, one per line.
 * Each event line has a frequency, followed by the number of feature-value pairs,
 * then the sequence of feature-value pairs, 
 * where the feature name is a string with no white space.
 * Each feature can appear only once in an event, and must have a value greater than zero. 
 * You can have events with a zero frequency -- these are used for dispreferred analyses 
 * in ranking tasks such as parse selection or realization ranking. 
 * 
 * An example file appears below.  There are two blocks, corresponding to the 
 * parses of two different senses.  The first block has two possible parses, 
 * the first of which is correct, while the second block has three possible 
 * parses, where the second one is the correct one.
 * 
 * <pre>
 * 2
 * 1 2 feat1 1.22 feat2 3
 * 0 3 feat1 1.55 feat3 1 feat4 2.7
 * 3
 * 0 2 feat1 1.44 feat4 2.2
 * 1 1 feat1 1.33
 * 0 2 feat1 1.32 feat4 3.21
 * </pre>
 * 
 * @author Michael White
 * @version     $Revision: 1.5 $, $Date: 2009/11/01 22:26:29 $
 */
public class EventFile {

	/** A block is a list of events. */
	public static class Block {
		/** The list. */
		public List<Event> events;
		/** Constructor. */
		public Block(List<Event> events) { this.events = events; }
		/** The event with the highest count (first tied if ties). */
		public Event best() {
			Event retval = null; int max = -1;
			for (Event event : events) {
				if (event.count > max) { retval = event; max = event.count; }
			}
			return retval;
		}
	}
	
	/** An event is a feature vector with a count. */
	public static class Event {
		/** The count. */
		public int count; 
		/** The feature vector. */
		public FeatureVector features;
		/** Constructor. */
		public Event(FeatureVector features, int count) {
			this.features = features;
			this.count = count;
		}
		/** toString. */
		public String toString() { return "event: count: " + count + " " + features; }
	}
	
	
	// the alphabet
	private Alphabet alphabet;
	
	// the file, which can be reopened
	private File file;
	
	// the current reader
	private Reader reader;
	
	// the current tokenizer
	private StreamTokenizer tokenizer;
	
	// the saved blocks, if kept in memory
	private List<Block> blocks = null;
	
	// the iterator over saved blocks
	private Iterator<Block> blockIt = null;
	
	// whether the end-of-file has been reached
	private boolean eofReached = false;
	
	/** Constructor with filename. */
	public EventFile(String filename) throws IOException {
		this(filename, false);
	}

	/** Constructor with filename and in-memory flag. */
	public EventFile(String filename, boolean inMemory) throws IOException {
		this(filename, new Alphabet(10000), inMemory);
	}

	/** Constructor with filename and alphabet. */
	public EventFile(String filename, Alphabet alphabet) throws IOException {
		this(filename, alphabet, false);
	}

	/** Constructor with filename, alphabet and in-memory flag. */
	public EventFile(String filename, Alphabet alphabet, boolean inMemory) throws IOException {
		file = new File(filename); init();
		this.alphabet = alphabet;
		if (inMemory) this.blocks = new ArrayList<Block>(10000);
	}

	
	/** Returns the alphabet. */
	public Alphabet getAlphabet() { return alphabet; }

	
	/** Closes the reader. */
	public void close() throws IOException {
		reader.close();
	}
	
	/** Resets the event file for reading again. */
	public void reset() throws IOException {
		close(); init();
	}
	
	// inits the reader and tokenizer, or 
	// if keeping blocks in memory, resets the iterator
	private void init() throws IOException {
		// in-memory case
		if (blocks != null && eofReached) {
			blockIt = blocks.iterator(); return;
		}
		// degenerate case: keeping blocks in memory but eof not reached
		if (blocks != null) {
			// dump saved blocks
			blocks.clear();
		}
		// regular init
		reader = openReader(file);
		tokenizer = initTokenizer(reader);
	}
	
	
	/** Initializes the given tokenizer to recognize most chars as word chars. */
	public static StreamTokenizer initTokenizer(Reader reader) throws IOException {
		StreamTokenizer tokenizer = new StreamTokenizer(reader);
		tokenizer.resetSyntax();
		tokenizer.wordChars(33, 255);
		tokenizer.whitespaceChars(0, 32);
		return tokenizer;
	}
	
	/** Returns whether EOF has been reached. */
	public boolean endOfFile() throws IOException {
		tokenizer.nextToken();
		boolean eof = (tokenizer.ttype == StreamTokenizer.TT_EOF);
		tokenizer.pushBack();
		return eof;
	}
	
	
	/** Reads the next event. Feature are filtered if apropos. */
	private Event nextEvent() throws IOException {
		tokenizer.nextToken();
		int count = Integer.parseInt(tokenizer.sval); 
		tokenizer.nextToken();
		int numFeats = Integer.parseInt(tokenizer.sval); 
		FeatureList fv = new FeatureList(numFeats);
		for (int i=0; i < numFeats; i++) {
			tokenizer.nextToken();
			String feat = tokenizer.sval;
			tokenizer.nextToken();
			float val = Float.parseFloat(tokenizer.sval);
			Alphabet.Feature f = alphabet.index(feat);
			if (f != null) fv.add(f, val); 
		}
		return new Event(fv, count);
	}
	
	/** Reads the next block, or null if none. */
	public Block nextBlock() throws IOException {
		// first check block iterator for in-mem case
		if (blockIt != null) {
			return (blockIt.hasNext()) ? blockIt.next() : null;
		}
		// otherwise check for eof, noting completion for in-mem case
		if (endOfFile()) {
			eofReached = true; return null;
		}
		// otherwise parse next block
		tokenizer.nextToken();
		int numEvents = Integer.parseInt(tokenizer.sval);
		List<Event> events = new ArrayList<Event>(numEvents);
		for (int i=0; i < numEvents; i++) {
			events.add(nextEvent());
		}
		Block retval = new Block(events);
		// save block with in-mem case
		if (blocks != null) blocks.add(retval);
		// done
		return retval;
	}
	
	
	/** Returns a reader for the given file, using gzip inflation if the file's name ends with .gz. */
	public static Reader openReader(File file) throws IOException {
		if (file.getName().endsWith(".gz"))
			return new InputStreamReader(new GZIPInputStream(new FileInputStream(file)));
		else
			return new BufferedReader(new FileReader(file));
	}

	/** Returns a printwriter for the given file, using gzip deflation if the file's name ends with .gz. */
	public static PrintWriter openWriter(File file) throws IOException {
		if (file.getName().endsWith(".gz"))
			return new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file))));
		else
			return new PrintWriter(new BufferedWriter(new FileWriter(file)));
	}
	
	/** Writes the events for a given list of signs according to the feature extractor and best sign. */
	public static void writeEvents(PrintWriter pw, List<Sign> signs, Sign best, FeatureExtractor fe) throws IOException {
    	Collections.shuffle(signs);
    	pw.println(Integer.toString(signs.size()));
    	for (Sign s : signs) {
    		int count = 0;
    		if (s == best) count = 1;
    		pw.print(count + " ");
    		FeatureVector fvect = fe.extractFeatures(s, true);
    		int numfeats = fvect.size();
    		pw.print(numfeats + " ");
			for (FeatureVector.Iterator it = fvect.iterator(); it.hasNext(); ) {
    			pw.print(it.nextFeature().name() + " " + it.nextValue() + " ");
			}
    		pw.println();
    	}
	}
}
