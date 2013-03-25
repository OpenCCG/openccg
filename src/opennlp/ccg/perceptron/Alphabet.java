///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2008 Michael White
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
import java.io.*;

import opennlp.ccg.util.*;
import opennlp.ccg.lexicon.DefaultTokenizer;

/**
 * A bidirectional mapping between feature names and indices.
 * 
 * An alphabet can be read from either an alphabet file or a model file.
 * An alphabet file starts with the number of features on one line, 
 * followed by one line per feature pairing the feature name with its 
 * frequency (which is ignored).
 * 
 * An alphabet can be open or closed.  An closed alphabet does not 
 * accept new features, and thus can be used to filter out features 
 * not already in the alphabet.
 * 
 * The main routine filters the input event file to an output alphabet file,
 * optionally with a table size and pruning threshold.
 * 
 * @author Michael White
 * @version     $Revision: 1.7 $, $Date: 2009/11/02 03:44:15 $
 */
public class Alphabet {
	
	/** Feature as a node in a trie, with each node storing the feature index. */
	public static class Feature extends TrieMap<String,Integer> {
		
		/** Constructor with index. */
		public Feature(Integer index) { super(index); }
		
		/** Factory method, for adding empty child nodes. */
		protected Feature createNode() { return new Feature(null); }
		
		/** Returns a string name by concatenating escaped keys using colons. */
		public String name() {
			StringBuffer namebuf = new StringBuffer();
			List<String> keys = traceKeys();
			for (int i=0; i < keys.size(); i++) {
				namebuf.append(DefaultTokenizer.escape(keys.get(i)));
				if (i+1 < keys.size()) namebuf.append(':');
			}
			return namebuf.toString();
		}
		
		/** Returns the index. */
		public Integer getIndex() { return data; }
	}
	
	
	// mappings
	private Feature dict;
	private List<Feature> dictInv;
	
	// size
	private int size = 0;
	
	// closed flag
	private boolean closed = false;
	
	
	/** Constructor with initial size. The alphabet is left open. */
	public Alphabet(int size) {
		init(size);
	}

	/** Constructor to load an alphabet from a file. The alphabet is set to closed. */
	public Alphabet(String filename) throws IOException {
		Reader reader = EventFile.openReader(new File(filename));
		StreamTokenizer tokenizer = EventFile.initTokenizer(reader);
		tokenizer.nextToken();
		int size = Integer.parseInt(tokenizer.sval);
		init(size);
		for (int i=0; i < size; i++) {
			tokenizer.nextToken();
			add(tokenizer.sval);
			tokenizer.nextToken(); // skip freq or weight
		}
		reader.close();
		closed = true;
	}
	
	// initializes dict, dictInv
	private void init(int size) {
		dict = new Feature(null);
		dictInv = new ArrayList<Feature>(size);
	}
	
	
	/** Size. */
	public int size() { return size; }
	
	/** Closed. */
	public boolean closed() { return closed; }
	
	/** Set closed. */
	public void setClosed(boolean closed) { this.closed = closed; }
	
	
	/** Add feature with given name, if not already present, returning added feature. */
	public Feature add(String feat) {
		return add(parseKeys(feat));
	}
	
	/** Add equivalent feature, if not already present, returning added feature. */
	public Feature add(Feature f) {
		return add(f.traceKeys());
	}
	
	/** Add feature with given keys, if not already present, returning added feature. */
	public Feature add(List<String> keys) {
		if (closed) throw new RuntimeException("Can't add to a closed alphabet!");
		Feature node = (Feature) dict.findChildFromList(keys);
		return addNode(node);
	}
	
	/** Add feature with given keys, if not already present, returning added feature. */
	public Feature addLazy(List<TrieMap.KeyExtractor<String>> keyExtractors) {
		if (closed) throw new RuntimeException("Can't add to a closed alphabet!");
		Feature node = (Feature) dict.findChildFromLazyList(keyExtractors);
		return addNode(node);
	}
	
	// adds a feature node
	private Feature addNode(Feature node) {
		if (node.data != null) return node;
		node.data = size++;
		dictInv.add(node);
		return node;
	}
	
	/** 
	 * Parses a feature name into a list of unescaped interned string keys, 
	 * breaking on colons. 
	 */
	public static List<String> parseKeys(String feat) {
		List<String> retval = new ArrayList<String>();
		int current = 0; 
		while (current < feat.length()) {
			int breakpos = feat.indexOf(":", current);
			String key;
			if (breakpos >= 0) {
				key = feat.substring(current, breakpos);
				current = breakpos + 1;
			}
			else {
				key = feat.substring(current);
				current = feat.length();
			}
			retval.add(DefaultTokenizer.unescape(key).intern());
		}
		return retval;
	}
	
	
	/** Get or add index of feature with given name (null if none when closed). */
	public Feature index(String feat) { 
		return index(parseKeys(feat));
	}
	
	/** Get or add index of equivalent feature (null if none when closed). */
	public Feature index(Feature f) {
		return index(f.traceKeys());
	}
	
	/** Get or add index of feature with given keys (null if none when closed). */
	public Feature index(List<String> keys) {
		if (!closed) return add(keys);
		Feature node = (Feature) dict.getChildFromList(keys);
		return node;
	}
	
	/** Get or add index of feature with given key extractors (null if none when closed). */
	public Feature indexLazy(List<TrieMap.KeyExtractor<String>> keyExtractors) {
		if (!closed) return addLazy(keyExtractors);
		Feature node = (Feature) dict.getChildFromLazyList(keyExtractors);
		return node;
	}
	
	/** Get indexed feature. */
	public Feature feature(int index) { return dictInv.get(index); }
	
	
	/** 
	 * Creates an alphabet file from an event file, 
	 * saving it to a file with features sorted by frequency, 
	 * using the given table size and pruning threshold. 
	 **/
	public static void createAlphabet(String eventfile, String alphabetfile, int tablesize, int pruningthreshold) throws IOException {
		// open files
		EventFile eventFile = new EventFile(eventfile);
		PrintWriter out = EventFile.openWriter(new File(alphabetfile));
		// init freq tally
		Map<Feature,Integer> freqTally = new HashMap<Feature,Integer>(tablesize*2);
		// read event file, incrementing tallies
		EventFile.Block block;
		FeatureMap goldMap = new FeatureMap();
		Set<Alphabet.Feature> seenFeats = new HashSet<Alphabet.Feature>();
		while ( (block = eventFile.nextBlock()) != null ) {
			// set gold map (nb: assumes single gold event)
			goldMap.clear();
			seenFeats.clear();
			for (EventFile.Event event : block.events) {
				if (event.count > 0) goldMap.add(event.features);
			}
			// tally distinct feats not in gold map
			for (EventFile.Event event : block.events) {
				if (event.count > 0) continue;
				for (FeatureVector.Iterator it = event.features.iterator(); it.hasNext(); ) {
					Feature feat = it.nextFeature();
					float val = it.nextValue();
					seenFeats.add(feat);
					float goldVal = goldMap.get(feat);
					if (val != goldVal) {
						Integer tally = freqTally.get(feat);
						if (tally != null) freqTally.put(feat, ++tally);
						else freqTally.put(feat, 1);
					}
				}
			}
			// tally unseen feats from gold event
			for (EventFile.Event event : block.events) {
				if (event.count == 0) continue;
				for (FeatureVector.Iterator it = event.features.iterator(); it.hasNext(); ) {
					Feature feat = it.nextFeature();
					if (!seenFeats.contains(feat)) {
						Integer tally = freqTally.get(feat);
						if (tally != null) freqTally.put(feat, ++tally);
						else freqTally.put(feat, 1);
					}
				}
			}
		}
		// get tallies passing frequency threshold
		List<Map.Entry<Feature,Integer>> tallies = new ArrayList<Map.Entry<Feature,Integer>>(freqTally.size());
		if (pruningthreshold > 0) {
			for (Map.Entry<Feature,Integer> entry : freqTally.entrySet()) {
				if (entry.getValue() >= pruningthreshold) tallies.add(entry);
			}
		}
		else tallies.addAll(freqTally.entrySet());
		// sort tallies by descending frequency 
		// (further sorting alphabetically seems to take too long)
		Collections.sort(
			tallies, 
			new Comparator<Map.Entry<Feature,Integer>>() {
				public int compare(Map.Entry<Feature,Integer> entry1, Map.Entry<Feature,Integer> entry2) {
					int val1 = entry1.getValue(); int val2 = entry2.getValue(); 
					if (val1 > val2) return -1;
					if (val1 < val2) return 1;
					return 0;
					//return entry1.getKey().name().compareTo(entry2.getKey().name());
				}
			}
		);
		// write tallied features to file
		int size = tallies.size();
		out.println(Integer.toString(size));
		for (int i=0; i < size; i++) {
			Map.Entry<Feature,Integer> entry = tallies.get(i);
			out.println(entry.getKey().name() + " " + entry.getValue());
		}
		// close files
		eventFile.close();
		out.close();
	}

	/** Main routine for filtering event file to an alphabet file. */
	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.out.println("Usage: java perceptron.Alphabet <eventfile> <alphabetfile> (-s <tablesize>) (-p <pruningthreshold>");
			System.exit(0);
		}
		String eventfile = args[0];
		String alphabetfile = args[1];
		int tablesize = 1000000;
		int pruningthreshold = 0;
		for (int i=2; i < args.length; i++) {
			if (args[i].equals("-s")) tablesize = Integer.valueOf(args[++i]);
			if (args[i].equals("-p")) pruningthreshold = Integer.valueOf(args[++i]);
		}
		System.out.println("Writing alphabet to " + alphabetfile + " from event file " + eventfile);
		if (pruningthreshold > 0) System.out.println("with pruning threshold " + pruningthreshold);
		createAlphabet(eventfile, alphabetfile, tablesize, pruningthreshold);
	}
}
