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

//Class which stores lexical info and associated frequencies. 

//This class is invoked by LexExtr.xsl and StemInsert.xsl transforms

package opennlp.ccgbank.extract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import opennlp.ccgbank.extract.ExtractGrammar.ExtractionProperties;

//import javax.xml.transform.TransformerConfigurationException;
//import javax.xml.transform.TransformerException;

//import org.jdom.JDOMException;
//import org.xml.sax.SAXException;


public class FreqTally{

	// Frequency cutoff for including an extracted cat 
	public static int CAT_FREQ_CUTOFF = 1;  

	// Frequency cutoff for including an extracted lex, cat, pos triple 
	public static int LEX_FREQ_CUTOFF = 1; 

	// Frequency cutoff for making a family (ie, cat & pos) open
	public static int OPEN_FREQ_CUTOFF = 100; 


	//The object where lexical info of each category has been stored
	private static Map<String,CatNode> catInfo = new HashMap<String,CatNode>();

	//Freq of cat specs
	private static Map<String,Integer> catFreq = new HashMap<String,Integer>();

	//Sentence id
	private static String id="";

	// Observed lex combos
	private static Set<String> observedLexCombos = new HashSet<String>();

	/** Resets the statically held tallies. */
	public static void reset() {
		catInfo = new HashMap<String,CatNode>();
		catFreq = new HashMap<String,Integer>();
		id="";
		observedLexCombos = new HashSet<String>();
	}
	
	//Proc which traps and stores id of each sentence
	public String storeId(String x) {
		if(x.length()>0){
			id = x;
			int posEquals = x.indexOf('=');
			if (posEquals > 0) {
				id = x.substring(posEquals+1);
			}
		}
		return id;
	}

	//Changes case of proper nouns
	public String changeCase(String lex,String pos){

		//if(!pos.equals("NNP") && !pos.equals("NNPS") && !lex.equals("I"))lex=lex.toLowerCase();
		return lex;

	}

	// Creating a freq tally using hashtables. Invoked by LexExtr.xsl.
	// Returns whether cat+pos is seen for the first time.
	public boolean loadTally(String lex, String cat, String pos) {

		String key = catPosKey(cat, pos);
		CatNode cn;
		boolean retval;

		//First occurence of cat+pos. Entry made
		if(!catFreq.containsKey(key)){
			cn = new CatNode(cat, pos);
			catInfo.put(key, cn);
			catFreq.put(key,1);
			retval = true;
		}
		else { // otherwise inc count
			cn = catInfo.get(key);
			catFreq.put(key, catFreq.get(key)+1);
			retval = false;
		}

		// store lex info 
		cn.lexInsert(lex,id);

		return retval;
	}


	/** Returns a string key for a cat and pos. */
	public static String catPosKey(String cat, String pos) {
		return cat+"-"+pos;
	}


	//Proc which outputs list of map keys in descending order of frequencies
	public static List<String> sortTally(Map<String,Integer> x) { 

		// retval
		List<String> sortedList = new ArrayList<String>();

		//Sorting by freq
		ArrayList<Integer> vals1 = new ArrayList<Integer>(x.values());
		Collections.sort(vals1);

		//Removing unique frequencies to a new arraylist
		ArrayList<Integer> vals = new ArrayList<Integer>(vals1.size());
		int prev = -1;
		for (Integer freq : vals1) {
			if (freq != prev) vals.add(freq);
			prev = freq;
		}

		//Finding all the keys corresponding to a particular freq
		for (int i=vals.size()-1; i >=0; i--) {
			int sortedFreq = vals.get(i);
			for (String key : x.keySet()) {
				int freq = x.get(key);
				if(freq==sortedFreq) sortedList.add(key);
			}
		}

		return sortedList;
	}

	public static void printTally(ExtractionProperties extractProps) throws FileNotFoundException {
		FreqTally.printTally(new File(extractProps.tempDir));
	}


	public static void printTally(File directory) throws FileNotFoundException {

		System.out.println("Generating CorpFreq.html");

		//Freq Output file
		File freqFile = new File(directory, "CorpFreq.html");
		PrintWriter output=new PrintWriter(new FileOutputStream(freqFile));
		List<String> sortedCatKeys = sortTally(catFreq);

		//Printing the final ouput in html form
		output.println("<html>");
		output.println("<head>");
		output.println("<title>");
		output.println("Lexical Info");
		output.println("</title>");
		output.println("</head>");
		output.println("<body>");
		output.flush();

		for (int i=0; i < sortedCatKeys.size(); i++) {

			String key = sortedCatKeys.get(i);
			CatNode cn = catInfo.get(key);
			String cat = cn.cat;
			String pos = cn.pos;
			int freq = catFreq.get(key);

			output.println("<p>");
			output.println(i+1+" Category: "+cat+" POS: "+pos+" Freq: "+freq);
			output.println("</p>");

			output.println();
			cn.printTally(output);
			output.flush();
		}

		output.println("</body>");
		output.println("</html>");
	}


	/** Returns whether this lex combo has been seen for the first time. */
	public boolean firstLexCombo(String lex, String stem, String rel, String cat, String pos,String semClass) {
		String key = lex + "_" + stem + "_" + rel + "_" + cat + "_" + pos + "_" + semClass;
		//String key = lex + "_" + stem + "_" + rel + "_" + cat + "_" + pos;

		if (observedLexCombos.contains(key)) return false;
		observedLexCombos.add(key);
		return true;
	}


	// returns the freq for the given key, or 0 if not present
	private int getFreq(String key) {
		Integer freq = catFreq.get(key);
		return (freq != null) ? freq : 0;
	}

	/** Returns the frequency of the cat and pos. */
	public int getFreq(String cat, String pos) {
		String key = catPosKey(cat, pos);
		return getFreq(key); 
	}

	/** Returns whether the cat and pos pass the frequency cutoff. */
	public boolean checkFreqStatus(String cat, String pos) {

		/*if(cat.contains("Arg") || cat.startsWith("pp["))
				return true;*/

		/*if(id.contains("wsj_00"))
				return true;*/

		return getFreq(cat, pos) >= CAT_FREQ_CUTOFF; 
	}

	/** Returns whether the lex, cat and pos pass the frequency cutoffs. */
	public boolean checkFreqStatus(String lex, String cat, String pos) {
		String key = catPosKey(cat, pos);
		//System.out.println(cat);
		if(cat.contains("pp["))
			return true;

		/*if(id.contains("wsj_00"))
			return true;*/

		if (getFreq(key) < CAT_FREQ_CUTOFF) return false; 
		CatNode cn = catInfo.get(key);
		return cn.getLexFreq(lex) >= LEX_FREQ_CUTOFF;
	}

	/** Returns whether the cat and pos are for an open family. */
	public boolean isOpen(String cat, String pos) {

		if (getFreq(cat, pos) < OPEN_FREQ_CUTOFF) return false;
		if (pos.startsWith("NN") || pos.equals("CD")) return true;
		else if (pos.startsWith("JJ") && (cat.equals("n_~1/n_1") || cat.equals("s[adj]_1\np_2"))) return true;
		else return false;
	}
}
