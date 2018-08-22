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

package opennlp.ccg.realize.linearizer;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import opennlp.ccg.realize.feat_extract.LogicalForm;
import opennlp.ccg.realize.feat_extract.WordFeatures;

public abstract class Linearizer {
	public List<String> getOrder(LogicalForm lf, WordFeatures current, Set<String> visited, LinConfig config) {
		List<String> order = order(lf, current, visited, config);
		if(config.relSeq()) {
			putRelationInSequence(lf, order);
		}
		return order;
	}
	public abstract List<String> order(LogicalForm lf, WordFeatures current, Set<String> visited, LinConfig config);
	
	/** @return list with current, if current has word ID in format w# and hasn't been visited<br/>
	 * null if current has word ID in format w# and has been visited<br/>
	 * empty list otherwise */
	public List<String> addCurrentToOrderAndVisited(WordFeatures current, Set<String> visited) {
		List<String> order = new LinkedList<>();
		String wordID = current.getUniqueFeature("id");
		if(wordID != null && wordID.charAt(0) == 'w' && !visited.contains(wordID)) {
			order.add(wordID);
			visited.add(wordID);
		} else if(wordID != null && wordID.charAt(0) == 'w') {
			return null;
		}
		return order;
	}
	public void putRelationInSequence(LogicalForm lf, List<String> order) {
		for(int i = 0;i < order.size();i++) {
			String item = order.get(i);
			if(item.matches("w[0-9]*")) { //item is word ID
				WordFeatures feats = lf.getWordFeatures(item); //should not be null
				List<String> parentRels = feats.getFeature("PR");
				if(parentRels != null && parentRels.size() > 0) {
					order.add(i, parentRels.get(0));
					i++;
				}
			}
		}
	}
	public void maybeAddParens(WordFeatures current, LinConfig config, List<String> order) {
		int parenSubtreeSize = config.parenSubtreeSize();
		if(parenSubtreeSize > 0 && current.getSubtreeSize() >= parenSubtreeSize) {
			order.add(0, "(");
			order.add(")");
		}
	}
}
