///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2009 Dennis N. Mehay
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
package opennlp.ccg.parse.supertagger.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import opennlp.ccg.util.Pair;

/**
 * @author Dennis N. Mehay
 * @version $Revision: 1.2 $, $Date: 2009/11/15 04:52:26 $
 */
public class STTaggerWordDictionary implements STTaggerDictionary, Serializable {
	
	private static final long serialVersionUID = -2474606825228545547L;
	
	/* This object represents our dictionary.  The String is the
	 * word we want to look up, and the Pair holds
	 * the word frequency count (= `a') and the Collection<String> of the CCG lex. cat's
	 * seen with that word (= `b').
	 */
	private Map<String, Pair<Integer, Collection<String>>> dict = null;


	/* This constructor does not create the dictionary; that must
	 * be done using a <code>DictionaryMaker</code>.    
	 */
	public STTaggerWordDictionary(Map<String, Pair<Integer, Collection<String>>> dict) { 
		this.dict = dict;
	} 

	/** 
	 * This method implements the interface STTaggerDictionary
	 * by delegating to <code>getEntry(String, int)</code> (see below).
	 */
	public Collection<String> getEntry(String word) {
		return getEntry(word, 1);
	}

	/**
	 * A method for getting the dictionary entry for a particular
	 * <code>String</code> word, only if that word appears at least
	 * `freq' times in the corpus.  
	 * 
	 * @param key A <code>String</code> representing a particular
	 *            word.
	 * @param freq An <code>int</code> specifying the number of times a 
	 *             word should have occured before it is returned.
	 * @return A <code>String[]</code> containing all supertags (CCG lexical 
	 *         categories) seen with the specified word.
	 *         Returns <code>null</code> if the word does not appear at least
	 *         `freq' times in the corpus from which the dictionary was 
	 *         created or if the word does not appear at all.
	 *         (N.B. Passing in an <code>int</code> `freq' value of zero will elicit the
	 *          same behavior as passing in a `freq' value of 1.)
	 */
	@SuppressWarnings("unchecked")
	public Collection<String> getEntry(String word, int freq) {
		if (word == null) return null; // mww: extra null check
		Object o = this.dict.get(word);
		if(o==null) { return null; }

		Pair<Integer,Collection<String>> p = (Pair<Integer,Collection<String>>)o;
		int wfreq = p.a.intValue();
		if(wfreq>=freq) {
			return p.b;
		}
		else {
			return null;
		}
	} // End method getEntry(String, int)

	/**
	 * A method to test whether this <code>STTaggerDictionary</code> contains 
	 * an entry for a particular <code>String</code> representing a word.  
	 *
	 * @param key A <code>String</code> representing a particular
	 *            word.
	 * @return A <code>boolean</code> value of <code>true</code> or 
	 *           <code>false</code> answering the question of whether this
	 *           dictionary contains an entry for the specified word.
	 */
	public boolean containsEntry(String word) { return this.dict.containsKey(word); }

	/**
	 * A method that returns the contents of the mapping embodied in this dictionary.
	 * @return An <code>Iterator</code> of <code>supertagger.util.Pair</code>s
	 * that represent the word -> (freq, { ... supertags ...}) mappings in the dictionary.
	 */
	public Iterator<Pair<String, Pair<Integer,Collection<String>>>> getMappings() {
		Iterator<String> keyset = this.dict.keySet().iterator();
		ArrayList<Pair<String, Pair<Integer,Collection<String>>>> preRes = 
			new ArrayList<Pair<String, Pair<Integer,Collection<String>>>>();
		String tempS = null;
		while(keyset.hasNext()) {
			tempS = keyset.next();
			preRes.add(
					new Pair<String, Pair<Integer, Collection<String>>>(tempS, this.dict.get(tempS)));
		}
		return preRes.iterator();
	}

	/** 
	 * A method for getting the number of times a word was seen 
	 * in the training data with which this dictionary was created.
	 * 
	 * @param word A <code>String</code> representing the word in question.
	 * @return An <code>int</code> count of this word's frequency in the 
	 *         corpus with which this dictionary was created.
	 */
	@SuppressWarnings("unchecked")
	public int getCount(String word) { 
		Object o = dict.get(word);
		if(o==null) { return 0; }
		else {
			Pair<Integer,Collection<String>> p = (Pair<Integer,Collection<String>>)o;
			return p.a.intValue();
		}
	} // End method getCount(word)
} // End class STTaggerWordDictionary





