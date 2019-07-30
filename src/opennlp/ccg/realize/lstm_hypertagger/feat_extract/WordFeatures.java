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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Data structure for storing node features */
public class WordFeatures {
	public static final WordFeatures HEAD = new WordFeatures();
	public static final WordFeatures CCONJ = new WordFeatures();
	static {
		HEAD.addFeature("id", "HEAD");
		HEAD.addFeature("PN", "NULL");
	}
	
	private Map<String, List<String>> features = new HashMap<>();
	private Map<String, List<WordFeatures>> parents = new HashMap<>();
	private Map<String, List<WordFeatures>> children = new HashMap<>();
	private int subtreeSize = 1;
	
	// FEATURE METHODS
	public void addFeature(String feature, String value) {
		if(features.containsKey(feature)) {
			features.get(feature).add(value);
		} else {
			List<String> vals = new ArrayList<>();
			vals.add(value);
			features.put(feature, vals);
		}
	}
	public List<String> getFeature(String feature) {
		return features.get(feature);
	}
	public String getUniqueFeature(String feature) {
		List<String> featList = features.get(feature);
		return (featList != null && featList.size() > 0) ? featList.get(0) : null;
	}
	public String getFeatString(String feature) {
		List<String> vals = features.get(feature);
		if(vals == null) {
			return null;
		}
		String featStr = vals.toString();
		return featStr.substring(1, featStr.length()-1);
	}
	public Map<String, List<String>> getFeatures() {
		return features;
	}
	
	// PARENT AND CHILDREN METHODS
	public void addParent(String relation, WordFeatures parent) {
		if(parents.containsKey(relation)) {
			parents.get(relation).add(parent);
		} else {
			List<WordFeatures> vals = new ArrayList<>();
			vals.add(parent);
			parents.put(relation, vals);
		}
	}
	public Map<String, List<WordFeatures>> getParents() {
		return parents;
	}
	public void addChild(String relation, WordFeatures child) {
		if(children.containsKey(relation)) {
			children.get(relation).add(child);
		} else {
			List<WordFeatures> vals = new ArrayList<>();
			vals.add(child);
			children.put(relation, vals);
		}
	}
	public Map<String, List<WordFeatures>> getChildren() {
		return children;
	}
	/** @return list of children not backed up by children map */
	public List<WordFeatures> getChildList() {
		List<WordFeatures> childList = new ArrayList<>();
		for(List<WordFeatures> list : children.values()) {
			childList.addAll(list);
		}
		return childList;
	}
	public List<String> getArgumentChildNames() {
		List<String> argNames = new ArrayList<>();
		for(int i = 0;i <= 5;i++) {
			List<String> argINames = features.get("A" + i + "N");
			if(argINames != null)
				argNames.addAll(argINames);
		}
		return argNames;
	}
	
	// SUBTREE METHODS
	/** Adds specified amount to subtreeSize */
	public void updateSubtreeCount(int increase) {
		subtreeSize += increase;
	}
	public int getSubtreeSize() {
		return subtreeSize;
	}
	
	// OTHER METHODS
	public boolean equals(Object obj) {
		if(!(obj instanceof WordFeatures)) {
			return false;
		} else if(this == obj) {
			return true;
		}
		
		WordFeatures other = (WordFeatures) obj;
		String id = getUniqueFeature("id");
		String otherID = other.getUniqueFeature("id");
		
		if(id == null || otherID == null) {
			return false;
		}
		return id.equals(otherID);
	}
	public Object clone() {
		WordFeatures clone = new WordFeatures();
		clone.features = new HashMap<>(features);
		clone.parents = new HashMap<>(parents);
		clone.children = new HashMap<>(children);
		return clone;
	}
	public String toString() {
		String id = getUniqueFeature("id");
		id = (id == null) ? "" : id;
		String pred = getUniqueFeature("PN");
		pred = (pred == null) ? "X" : pred;
		return "feats(" + id + ":" + pred + ")";
	}
}
