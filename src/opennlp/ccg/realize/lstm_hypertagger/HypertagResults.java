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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HypertagResults {
	private List<String> nomOrder;
	private List<String> linearization;
	/** Mapping from indices in predOrder to indices in linearization */
	private Map<Integer, Integer> predLinMap;
	private List<Map<String, Double>> results;
	private static Logger LOGGER = LSTMHypertaggerLogger.LOGGER;
	
	/** @param nomOrder
	 * @param linearization
	 * @param results Each map is probability distribution of hypertags for predicate. Predicates are in order of linearization.
	 */
	public HypertagResults(List<String> nomOrder, List<String> linearization, List<Map<String, Double>> results) {
		this.nomOrder = nomOrder;
		this.linearization = linearization;
		this.results = results;
		predLinMap = buildPredLinMap(this.nomOrder, this.linearization);
		LOGGER.log(Level.INFO, "Predlin map: " + predLinMap.toString());
	}
	public Map<Integer, Integer> buildPredLinMap(List<String> nomOrder, List<String> linearization) {
		Map<Integer,Integer> map = new HashMap<>();		
		Map<String,Integer> linInds = new HashMap<>();
		for(int i = 0;i < linearization.size();i++)
			linInds.put(linearization.get(i), i);
		// Add extra element in linearization and results for preds that start with 'x' or 'h'
		linearization.add("");
		results.add(new HashMap<>());
		
		for(int i = 0;i < nomOrder.size();i++) {
			String nom = nomOrder.get(i);
			int linInd = nom.startsWith("w") ? linInds.get(nom) : linearization.size() - 1;
			map.put(i, linInd);
		}
		return map;
	}
	public Map<String, Double> tagsForPred(int predIndex) {
		return results.get(predLinMap.get(predIndex));
	}
}
