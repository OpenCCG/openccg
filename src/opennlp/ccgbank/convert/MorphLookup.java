package opennlp.ccgbank.convert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for looking up stems as determined by the morpha utility. 
 */ 
public class MorphLookup {

	// map from word_pos to stem
	private static Map<String,String> stemMap = null;
	static File words, stems;
	
	public static void init(File wordsFile, File stemsFile) throws IOException {
		MorphLookup.words = wordsFile;
		MorphLookup.stems = stemsFile;
		
		if(MorphLookup.words == null) {
			throw new IllegalArgumentException("words file not specified");
		}
		if(MorphLookup.stems == null) {
			throw new IllegalArgumentException("stems file not specified");
		}
		
		stemMap = new HashMap<String,String>();
		
		BufferedReader wordsReader = new BufferedReader(
				new FileReader(words));
		BufferedReader stemsReader = new BufferedReader(
				new FileReader(stems));
		String wordsLine, stemsLine;
		// read lines in parallel
		while ( (wordsLine = wordsReader.readLine()) != null ) {
			stemsLine = stemsReader.readLine();
			//System.out.println(stemsLine);
			// wordsLine has a word and a POS
			String[] tokens = wordsLine.split("\\s+");
			String word = tokens[0]; String pos = tokens[1];
			// stemsLine just has a stem; lowercase it, for good measure
			String stem = stemsLine.trim().toLowerCase();
			// add word_POS -> stem to map, also with word lowercased
			String key = word + "_" + pos;
			String key2 = word.toLowerCase() + "_" + pos;
			stemMap.put(key, stem);
			stemMap.put(key2, stem);
		}
		wordsReader.close();
		stemsReader.close();
	}
	
	/** Returns the stem for the given word and pos, or the empty string if none. */
	public String getStem(String word, String pos) {
		String retval="";String key=word + "_" + pos;
		if (MorphLookup.stemMap.containsKey(key))
			retval = stemMap.get(word + "_" + pos);
			/*if (retval == null) retval = "";
		//System.out.println(key+" "+retval);*/
		
		if(retval.length()==0)
			System.out.println("addStems: No stem for: "+key);
		return retval;
	}
}
