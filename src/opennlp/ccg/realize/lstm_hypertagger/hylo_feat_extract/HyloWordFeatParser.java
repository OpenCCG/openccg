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

package opennlp.ccg.realize.lstm_hypertagger.hylo_feat_extract;
import java.util.List;
import opennlp.ccg.hylo.Diamond;
import opennlp.ccg.hylo.HyloHelper;
import opennlp.ccg.hylo.SatOp;
import opennlp.ccg.realize.lstm_hypertagger.feat_extract.LogicalForm;
import opennlp.ccg.realize.lstm_hypertagger.feat_extract.WordFeatures;
import opennlp.ccg.synsem.LF;

/** Adds id, XC, PN, ZN, ZD, ZT, ZM, ZP features to word */
public class HyloWordFeatParser {
	@SuppressWarnings("unused")
	public WordFeatures getWordFeatures(LogicalForm lf, String wordID, String wordNE, List<SatOp> nomPreds) {
		assert lf.getWordFeatures(wordID) == null;
		if(lf.getWordFeatures(wordID) != null)
			return lf.getWordFeatures(wordID);
		WordFeatures wordFeats = new WordFeatures();
		wordFeats.addFeature("id", wordID);
		if(wordNE != null)
			wordFeats.addFeature("XC", wordNE);
		
		int numLex = 0, numFeats = 0;
		for(SatOp pred : nomPreds) {
			if(HyloHelper.isLexPred(pred)){
				numLex += 1;
				addPNFeat(wordFeats, pred);
			} else { // pred is feature pred
				numFeats += 1;
				addFeat(wordFeats, pred);
			}
		}
		assert numLex <= 1;
		return wordFeats;
	}
	public void addPNFeat(WordFeatures wordFeats, LF lexPred) {
		String lemma = HyloHelper.getLexPred(lexPred);
		wordFeats.addFeature("PN", lemma);
	}
	public void addFeat(WordFeatures wordFeats, LF attrPred) {
		LF arg = ((SatOp) attrPred).getArg();
		String featName = ((Diamond) arg).getMode().getName();
		featName = translateToSymbol(featName);
		String featVal = HyloHelper.getVal(attrPred);
		wordFeats.addFeature(featName, featVal);
	}
	public String translateToSymbol(String fn) {
		if(fn.equals("num")) return "ZN";
		if(fn.equals("det")) return "ZD";
		if(fn.equals("tense")) return "ZT";
		if(fn.equals("mood")) return "ZM";
		if(fn.equals("partic")) return "ZP";
//		System.out.println(fn);
		return fn;
	}
	public void updateNumChildren(WordFeatures feats) {
		int numArgs = 0, numChildren = 0;
		for(String rel : feats.getChildren().keySet()) {
			numChildren += feats.getChildren().get(rel).size();
			if(rel.matches("Arg[0-9][a-z]?")) {
				numArgs += feats.getChildren().get(rel).size();
			}
		}
		feats.addFeature("FO", "" + numChildren);
		feats.addFeature("NA", "" + numArgs);
	}
}
