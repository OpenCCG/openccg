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

package opennlp.ccg.realize.lstm_hypertagger;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;

import opennlp.ccg.realize.feat_extract.WordInfoMap;
import opennlp.ccg.realize.linearizer.*;
import opennlp.ccg.realize.feat_extract.Exceptions.NoPredicatesException;
import opennlp.ccg.hylo.HyloHelper;
import opennlp.ccg.hylo.SatOp;
import opennlp.ccg.realize.Hypertagger;

public class LSTMHypertagger implements Hypertagger {
	private List<SatOp> preds;
	private int currentPred;
	private WordInfoMap wordInfoMap;
	private double[] betas;
	private int currentBeta;
	private boolean includeGoldTags;
	private HypertagResultsGenerator resultsGen;
	private HypertagResults results;
	private HypertagResultsProcessor processor;
	
	private Set<String> loggedSet = new HashSet<>();
	private static Logger LOGGER = LSTMHypertaggerLogger.LOGGER;
	
	public LSTMHypertagger() {
		this(new LinConfig(FeatOrders.ALL_REL, 5, false, ChildOrders.ENG),
				1234, new double[]{0.00175, 0.00055, 0.000135, 2.53e-05, 5.31e-06, 8.63e-07, 1.47e-07, 2.4e-09});
	}
	public LSTMHypertagger(LinConfig linConfig, int portNum, double betas[]) {
		try {
			processor = new HypertagResultsProcessor();
			resultsGen = new HypertagResultsGenerator(linConfig, portNum, betas[betas.length-1], processor);
			this.betas = betas;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Map<String, Double> getSupertags() {
		if(!HyloHelper.isLexPred(preds.get(currentPred))) {
			return null; // Not handling EPs for rels or feats, so return null
		}
		Map<String,Double> tags = results.tagsForPred(currentPred);
		tags = processor.filter(tags, betas[currentBeta]);
		String goldTag = processor.goldTag(preds.get(currentPred), wordInfoMap);
		if(includeGoldTags && !tags.containsKey(goldTag)) {
			tags.put(goldTag, 1.0);
		}
		String pred = preds.get(currentPred).toString();
		if(!loggedSet.contains(pred)) {
			LOGGER.log(Level.INFO, pred + "\t" + tags.toString());
			loggedSet.add(pred);
		}
		return tags;
	}
	@Override
	public void setIncludeGold(boolean includeGold) {
		includeGoldTags = includeGold;
	}
	@Override
	public void resetBeta() {
		currentBeta = 0;
	}
	@Override
	public void resetBetaToMax() {
		currentBeta = betas.length - 1;
	}
	@Override
	public void nextBeta() {
		if(currentBeta < betas.length -1) {
			currentBeta++;
		}
	}
	@Override
	public void previousBeta() {
		if(currentBeta > 0) {
			currentBeta--;
		}
	}
	@Override
	public boolean hasMoreBetas() {
		return currentBeta < betas.length - 1;
	}
	@Override
	public boolean hasLessBetas() {
		return currentBeta > 0;
	}
	@Override
	public double[] getBetas() {
		return betas;
	}
	@Override
	public void setBetas(double[] betas) {
		this.betas = betas;
	}
	@Override
	public double getCurrentBetaValue() {
		return betas[currentBeta];
	}
	@Override
	public void mapPreds(List<SatOp> preds) {
		try {
			loggedSet.clear();
			this.preds = preds;
			LOGGER.log(Level.INFO, "Calling mapPreds: " + preds.toString());
			currentPred = 0;
			results = resultsGen.getHypertagResults(preds);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void setPred(int index) {
		currentPred = index;
	}
	@Override
	public void storeGoldStdPredInfo(String goldStdPredInfo) {
		try {
			wordInfoMap = new WordInfoMap(null);
			wordInfoMap.parse(goldStdPredInfo);
		} catch (NoPredicatesException e) {
			e.printStackTrace();
		}
	}
}
