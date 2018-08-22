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

package opennlp.ccg.realize.hylo_feat_extract;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import opennlp.ccg.realize.feat_extract.*;
import opennlp.ccg.hylo.HyloHelper;
import opennlp.ccg.hylo.Nominal;
import opennlp.ccg.hylo.Op;
import opennlp.ccg.hylo.SatOp;
import opennlp.ccg.synsem.LF;

public class HyloFlatLFParser {
	private HyloWordFeatParser wordParser = new HyloWordFeatParser();
	private ParentChildParser parentChildParser = new ParentChildParser();
	private List<String> noms;
	private List<HyloRel> relations;
	private WordInfoMap wordInfoMap;
	
	public LogicalForm parse(List<SatOp> preds) {
		relations = new ArrayList<>();
		wordInfoMap = new WordInfoMap(null);
		
		LogicalForm lf = new LogicalForm(null);
		Map<String,List<SatOp>> nomPredMap = getNomPredMap(preds);
		noms = new ArrayList<>(nomPredMap.keySet());
		LF hyloLF = unflatten(preds);
		List<String> roots = getRoots(hyloLF);
		
		processNoms(lf, nomPredMap);
		processRels(lf);
		processRootsAndNumChildren(lf, roots);
		updateSubtreeSizes(lf);
		return lf;
	}	
	public void processNoms(LogicalForm lf, Map<String, List<SatOp>> nomPredMap) {
		for(String nom : noms) {
			assert nom.matches("[h,w,x][0-9]+(:[A-Z]+)?");
			String wordNE = nom.contains(":") ? nom.substring(nom.indexOf(":") + 1) : null;
			String wordID = nom.contains(":") ? nom.substring(0, nom.indexOf(":")) : nom;
			List<SatOp> nomPreds = nomPredMap.get(nom);
			WordFeatures wordFeats = wordParser.getWordFeatures(lf, wordID, wordNE, nomPreds);
			if(!wordInfoMap.containsWordId(wordID)) {
				wordInfoMap.addWordInfo(wordID, new WordInfo("","",""));
			}
			lf.addWordFeatures(wordID, wordFeats);
		}
	}
	public void processRels(LogicalForm lf) {
		for(HyloRel relation : relations) {
			WordFeatures parent = lf.getWordFeatures(relation.parent);
			if(parent == null) {
				parent = lf.addXNode(relation.parent);
				if(relation.parent.startsWith("w")) {
					wordInfoMap.addWordInfo(relation.parent, new WordInfo("","",""));
				}
			}
			WordFeatures child = lf.getWordFeatures(relation.child);
			if(child == null) {
				child = lf.addXNode(relation.child);
				if(relation.child.startsWith("w")) {
					wordInfoMap.addWordInfo(relation.child, new WordInfo("","",""));
				}
			}
			parentChildParser.exchangeFeatures(lf, parent, child, wordInfoMap, relation.name);
			parent.addChild(relation.name, child);
			child.addParent(relation.name, parent);
		}
	}
	public void processRootsAndNumChildren(LogicalForm lf, List<String> roots) {
		for(String nom : noms) {
			String wordID = nom.contains(":") ? nom.substring(0, nom.indexOf(":")) : nom;
			WordFeatures feats = lf.getWordFeatures(wordID);
			wordParser.updateNumChildren(feats);
			
			if(feats.getParents().size() == 0 || roots.contains(wordID)) {
				lf.getHead().addChild("None", feats);
				feats.addFeature("RN", "NULL");
			}
		}
	}
	
	public LF unflatten(List<SatOp> preds) {
		List<LF> lfs = new ArrayList<>();
		for(SatOp pred : preds)
			lfs.add(pred);
		Op conj = new Op(Op.CONJ, lfs);
		LF result = HyloHelper.compact(conj, null);
		return result;
	}
	public List<String> getRoots(LF hyloLF){
		List<String> roots = new ArrayList<>();
		if(hyloLF instanceof SatOp) {
			String root = getRoot(hyloLF);
			roots.add(root);
		} else { // hyloLF is CONJ Op
			assert ((Op) hyloLF).getName().equals("conj");
			List<LF> lfs = ((Op) hyloLF).getArguments();
			for(LF lf : lfs) {
				String root = getRoot(lf);
				roots.add(root);
			}
		}
		return roots;
	}
	public String getRoot(LF hyloLF) {
		Nominal rootNom = ((SatOp) hyloLF).getNominal();
		String root = rootNom.getName();
		return root;
	}
	
	public Map<String, List<SatOp>> getNomPredMap(List<SatOp> preds) {
		Map<String,List<SatOp>> nomPredMap = new HashMap<>();
		// Classify each predicate
		for(SatOp pred : preds) {
			String nom1 = HyloHelper.getPrincipalNominal(pred).toString();			
			if(HyloHelper.isLexPred(pred) || HyloHelper.isAttrPred(pred)) {
				addToNomPredMap(nomPredMap, nom1, pred);
			} else if(HyloHelper.isRelPred(pred)) {
				HyloRel hyloRel = parseRelPred(pred);
				relations.add(hyloRel);
				addToNomPredMap(nomPredMap, nom1);
				String nom2 = HyloHelper.getSecondaryNominal(pred).toString();
				addToNomPredMap(nomPredMap, nom2);
			}
		}
		// Check that preds.size() equals sum of sizes of lists in nomPredMap + relations.size()
		int sizeSum = 0;
		for(String nom : nomPredMap.keySet()) {
			sizeSum += nomPredMap.get(nom).size();
		}
		if(sizeSum + relations.size() != preds.size()) {
			System.err.println("getNomPredMap: Sum of list sizes wrong");
		}
		return nomPredMap;
	}
	private void addToNomPredMap(Map<String, List<SatOp>> nomPredMap, String nom, SatOp pred) {
		if(!nomPredMap.containsKey(nom)) {
			List<SatOp> preds = new ArrayList<>();
			preds.add(pred);
			nomPredMap.put(nom, preds);
		} else {
			nomPredMap.get(nom).add(pred);
		}
	}
	private void addToNomPredMap(Map<String, List<SatOp>> nomPredMap, String nom) {
		if(!nomPredMap.containsKey(nom)) {
			List<SatOp> preds = new ArrayList<>();
			nomPredMap.put(nom, preds);
		}
	}
	
	public void updateSubtreeSizes(LogicalForm lf) {
		updateSubtreeSize(lf.getHead(), new HashSet<String>());
	}
	public void updateSubtreeSize(WordFeatures feats, Set<String> visited) {
		for(WordFeatures child : feats.getChildList()) {
			String wordID = child.getUniqueFeature("id");
			if(wordID != null && !visited.contains(wordID)) {
				visited.add(wordID);
				updateSubtreeSize(child, visited);
			}
			feats.updateSubtreeCount(child.getSubtreeSize());
		}
	}
	
	public HyloRel parseRelPred(SatOp relPred) {
		String name = HyloHelper.getRel(relPred);
		String parent = HyloHelper.getPrincipalNominal(relPred).getName(); //string that matches regex "w[0-9]+"
		String child = HyloHelper.getSecondaryNominal(relPred).getName();
		return new HyloRel(name, parent, child);
	}
	public class HyloRel {
		private String name;
		private String parent;
		private String child;
		
		public HyloRel(String name, String parent, String child) {
			this.name = name;
			this.parent = parent;
			this.child = child;
		}
		public String toString() {
			return name + "(" + parent + ", " + child + ")";
		}
	}
}
