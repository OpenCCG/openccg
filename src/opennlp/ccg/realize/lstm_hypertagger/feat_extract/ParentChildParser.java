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

/**
 * Exchanges features between parent and child logical form nodes
 * @author Reid Fu
 */
public class ParentChildParser {	
	public void exchangeFeatures(LogicalForm lf, WordFeatures parent,
			WordFeatures child, WordInfoMap wordInfo, String relation) {
		if(parent.equals(WordFeatures.HEAD) || parent.getUniqueFeature("id").startsWith("x")) { // parent is HEAD or X node
			child.addFeature("RN", "NULL");
		} else if(child.getUniqueFeature("id").startsWith("w")) {
			String parentID = parent.getUniqueFeature("id");
			String childID = child.getUniqueFeature("id");
			String parentSupertag = wordInfo.getSupertag(parentID);
			String childSupertag = wordInfo.getSupertag(childID);
			
			lf.addSupertag(parentID, parentSupertag);
			lf.addSupertag(childID, childSupertag);
			addParentFeatures(parent, child, parentSupertag, relation);
			addChildFeatures(parent, child, childSupertag, relation);
		}
	}	
	/** Adds parent supertag, lemma, and POS to child feature set */
	public void addParentFeatures(WordFeatures parent, WordFeatures child, String parentSupertag, String relation) {
		child.addFeature("PR", relation);
		String parentLemma = parent.getUniqueFeature("PN");
		child.addFeature("RN", parentLemma);
		String parentPOS = parent.getUniqueFeature("PT");
		child.addFeature("PP", parentPOS);
		child.addFeature("PS", parentSupertag);
	}	
	/** Adds relation, child supertag + lemma + POS + named entity class to parent feature set */
	public void addChildFeatures(WordFeatures parent, WordFeatures child, String childSupertag, String relation) {
		parent.addFeature("CT", relation);
		parent.addFeature("CS", childSupertag);
		String childName = child.getUniqueFeature("PN");
		parent.addFeature("CN", childName);
		String childPOS = child.getUniqueFeature("PT");
		String childNE = child.getUniqueFeature("XC"); //named entity class
		
		if(relation.matches("Arg[0-9][a-z]?")) { // Arg, ArgA, ArgM do not count as argument
			addArgumentFeatures(parent, childSupertag, relation, childName, childPOS, childNE);
		} else {
			addNonargFeatures(parent, childSupertag, relation, childPOS, childNE);
		}
	}
	/** Adds A#N, A#P, AS, and X#D features to parent feature set */
	public void addArgumentFeatures(WordFeatures parent, String childSupertag,
			String relation, String childName, String childPOS, String childNE) {
		char argNum = relation.charAt(3);
		parent.addFeature("A" + argNum + "N", childName);
		parent.addFeature("A" + argNum + "P", childPOS);
		parent.addFeature("AS", childSupertag);
		if(childNE != null) {
			parent.addFeature("X" + argNum + "D", childNE);
		}
	}
	/** Adds MS, MP, and XM features to parent feature set */
	public void addNonargFeatures(WordFeatures parent, String childSupertag,
			String relation, String childPOS, String childNE) {
		parent.addFeature("MS", childSupertag);
		if(relation.equals("Mod")) {
			parent.addFeature("MP", childPOS);
		}
		if(childNE != null) {
			parent.addFeature("XM", childNE);
		}
	}
}
