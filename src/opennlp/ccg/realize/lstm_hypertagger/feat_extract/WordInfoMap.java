///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2018 Reid Fu
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

package opennlp.ccg.realize.lstm_hypertagger.feat_extract;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Map;
import java.util.Set;
import org.jdom2.Element;
import opennlp.ccg.realize.lstm_hypertagger.feat_extract.Exceptions.NoPredicatesException;

public class WordInfoMap {
	private String sentence;
	private Map<String, WordInfo> wordPreds = new HashMap<>();
	
	public WordInfoMap(String sentence) {
		this.sentence = sentence;
	}
	
	public void parse(Element predInfo) throws NoPredicatesException {
		String predInfoStr = predInfo.getAttributeValue("data");
		parse(predInfoStr);
	}
	public void parse(String predInfo) throws NoPredicatesException {
		if(predInfo==null || predInfo.equals("")) {
			throw new NoPredicatesException(sentence);
		}
		String[] words = predInfo.split(" ");
		
		for(String word : words) {
			String[] predInfoArr = word.split(":");
			String wordText = predInfoArr[0];
			WordInfo fullWord = new WordInfo(predInfoArr[1], predInfoArr[2], predInfoArr[3]);
			wordPreds.put(wordText, fullWord);
		}
	}
	
	public void addWordInfo(String wordID, WordInfo wordInfo) {
		wordPreds.put(wordID, wordInfo);
	}
	public boolean containsWordId(String wordID) {
		return wordPreds.containsKey(wordID);
	}
	public String getSentence() {
		return sentence;
	}
	public String getSupertag(String wordID) {
		if(wordPreds.get(wordID) == null) {
			System.err.println("No supertag for word ID " + wordID);
		}
		return wordPreds.get(wordID).getSupertag();
	}
	public String getPOS(String wordID) {
		return wordPreds.get(wordID).getPos();
	}
	public String getLemma(String wordID) {
		return wordPreds.get(wordID).getLemma();
	}
	/** @return word ID's in lexical order
	 * Mainly used for debugging */
	public Set<String> sortedKeys(){
		Set<String> sortedKeys = new TreeSet<>(wordPreds.keySet());
		return sortedKeys;
	}
	public String toString() {
		return wordPreds.toString();
	}
}
