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

package opennlp.ccg.realize.feat_extract;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Non-HLDS data structure for representing logical forms
 * @author Reid Fu
 */
public class LogicalForm {
	private String sentence;
	private Map<String, WordFeatures> featureMap;
	private Map<String, String> supertags;
	private WordFeatures head;
	
	public LogicalForm(String sentence) {
		this.sentence = sentence;
		featureMap = new HashMap<>();
		supertags = new HashMap<>();
		head = (WordFeatures) WordFeatures.HEAD.clone();
	}
	public String getSentence() {
		return sentence;
	}
	public Set<String> getWordIds(){
		return featureMap.keySet();
	}
	public void addWordFeatures(String wordID, WordFeatures wordFeats) {
		featureMap.put(wordID, wordFeats);
	}
	public WordFeatures getWordFeatures(String wordID) {
		return featureMap.get(wordID);
	}
	public void addSupertag(String wordID, String supertag) {
		supertags.put(wordID, supertag);
	}
	public String getSupertag(String wordID) {
		return supertags.get(wordID);
	}
	public WordFeatures getHead() {
		return head;
	}
	public WordFeatures addXNode(String wordID) {
		if(!wordID.startsWith("x")) {
			System.err.println("Calling addXNode with wordID that doesn't start with x");
		}
		WordFeatures x = new WordFeatures();
		x.addFeature("id", wordID);
		addWordFeatures(wordID, x);
		return x;
	}
}
