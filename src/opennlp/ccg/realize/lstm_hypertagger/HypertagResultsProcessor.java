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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opennlp.ccg.realize.feat_extract.WordInfoMap;
import opennlp.ccg.hylo.HyloHelper;
import opennlp.ccg.hylo.SatOp;

public class HypertagResultsProcessor {
	public Map<String, Double> filter(Map<String, Double> tags, double beta) {
		if(tags.size() == 0) {
			return tags;
		}
		
		List<String> sortedKeys = new ArrayList<>(tags.keySet());
		Collections.sort(sortedKeys, new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				double result = tags.get(arg0) - tags.get(arg1);
				if(result < 0) {
					return -1;
				} else if(result > 0) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		String bestTag = sortedKeys.get(sortedKeys.size()-1);
		double threshold = tags.get(bestTag)*beta;
		Map<String, Double> filteredTags = new HashMap<>();
		for(int i = sortedKeys.size()-1;i >= 0;i--) {
			String key = sortedKeys.get(i);
			if(tags.get(key) >= threshold) {
				filteredTags.put(key, tags.get(key));
			}
		}
		return filteredTags;
	}
	/** Assumes that pred is LexPred
	 * @return null if wordInfoMap is null or nominal of pred is not in wordInfoMap; otherwise, supertag associated with nom */
	public String goldTag(SatOp pred, WordInfoMap wordInfoMap) {
		String nom = HyloHelper.getPrincipalNominal(pred).getName();
		if(wordInfoMap == null || !wordInfoMap.containsWordId(nom)) {
			return null;
		}
		return wordInfoMap.getSupertag(nom);
	}
	/** Expected format of response is "[[{'tag': prob, ...}, {'tag': prob, ...}, ...]]" */
	public List<Map<String,Double>> processServerResponse(String response, int linOrderSize) {
		List<Map<String,Double>> result = new ArrayList<>();
		response = response.substring(2, response.length()-2);
		String[] tagMaps = response.split("(?<=}), ");
		assert tagMaps.length == 180;
		
		for(int i = 0;i < linOrderSize;i++) {
			String tagMap = tagMaps[i];
			Map<String,Double> map = new HashMap<>();
			tagMap = tagMap.substring(1, tagMap.length()-1);
			
			if(tagMap.length() > 0) {
				String[] items = tagMap.split(", ");
				for(String item : items) {
					String[] tagPair = item.split(": ");
					assert tagPair[0].matches("'.*'") || tagPair[0].equals("None");
					String tag = processTag(tagPair[0]);
					map.put(tag, Double.parseDouble(tagPair[1]));
				}
			}
			result.add(map);
		}
		return result;
	}
	/** Expected format of tag is "'tag'" */
	public String processTag(String tag) {
		String tag2 = tag.substring(1, tag.length()-1);
		tag2 = tag2.replaceAll("\\\\+", "\\\\");
		tag2 = StringDecoder.decode(tag2);
		return tag2;
	}
}
