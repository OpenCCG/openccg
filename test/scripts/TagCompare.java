package scripts;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** Script for calculating accuray and beta values corresponding to different multi-tag levels */
public class TagCompare {
	public enum AccType {NORMAL, UNK_PREDS, UNK_PREDTAGS};
	private List<String> goldTags = new LinkedList<>();
	private List<String> predNames = new LinkedList<>();
	private List<Map<String, Double>> maxBetaTags = new LinkedList<>();
	private List<Integer> unkPredIndices = new LinkedList<>();
	private List<Integer> unkPredTagIndices = new LinkedList<>();
	
	public void addGoldTag(String goldTag) {
		goldTags.add(goldTag);
	}
	public void addPredName(String predName) {
		predNames.add(predName);
	}
	public void addTokenBetaTags(Map<String,Double> tokenBetaTags) {
		maxBetaTags.add(tokenBetaTags);
	}
	public void addUnkPredIndex(int index) {
		unkPredIndices.add(index);
	}
	public void addUnkPredTagIndex(int index) {
		unkPredTagIndices.add(index);
	}
	
	public List<Map<String,Double>> betaTags(double beta, AccType accType) {
		List<Map<String,Double>> betaTags = new LinkedList<>();
		List<Map<String,Double>> betaTagsSrc = (accType == AccType.NORMAL) ? maxBetaTags
				: ((accType == AccType.UNK_PREDS) ? maxBetaTagsSubset(unkPredIndices) : maxBetaTagsSubset(unkPredTagIndices));
		
		for(Map<String,Double> tokenMaxBetaTags : betaTagsSrc) {
			Map<String,Double> tokenBetaTags = tokenBetaTags(beta, tokenMaxBetaTags);
			betaTags.add(tokenBetaTags);
		}
		return betaTags;
	}
	private List<Map<String,Double>> maxBetaTagsSubset(List<Integer> indices) {
		List<Map<String,Double>> betaTags = new LinkedList<>();
		for(Integer ind : indices) {
			betaTags.add(maxBetaTags.get(ind));
		}
		return betaTags;
	}
	private List<String> goldTagsSubset(List<Integer> indices) {
		List<String> subset = new LinkedList<>();
		for(Integer ind : indices) {
			subset.add(goldTags.get(ind));
		}
		return subset;
	}
	private Map<String,Double> tokenBetaTags(double beta, Map<String,Double> maxTokenBetaTags) {
		List<Entry<String,Double>> sortedTags = sortTags(maxTokenBetaTags);
		double threshProb = beta*sortedTags.get(0).getValue();
		Map<String,Double> tokenBetaTags = new HashMap<>();
		
		int i = 0;
		while(i < sortedTags.size() && sortedTags.get(i).getValue() >= threshProb) {
			tokenBetaTags.put(sortedTags.get(i).getKey(), sortedTags.get(i).getValue());
			i += 1;
		}
		return tokenBetaTags;
	}
	/** Sort tags in maxTokenBetaTags descending based on values */
	private List<Entry<String,Double>> sortTags(Map<String,Double> maxTokenBetaTags) {
		List<Entry<String,Double>> sortedTags = new ArrayList<>(maxTokenBetaTags.entrySet());
		Collections.sort(sortedTags, new Comparator<Entry<String,Double>>() {
			@Override
			public int compare(Entry<String, Double> arg0, Entry<String, Double> arg1) {
				return arg1.getValue().compareTo(arg0.getValue());
			}
		});
		return sortedTags;
	}
	
	public double findBeta(double multitag) {
		double low = 1.0;
		double high = 0.00001;
		double medMultiTag = multitag(0.5*low + 0.5*high);
		
		while(Math.abs(multitag - medMultiTag) > 0.01) {
			if(multitag > medMultiTag) {
				low = 0.5*low + 0.5*high;
			} else {
				high = 0.5*low + 0.5*high;
			}
			medMultiTag = multitag(0.5*low + 0.5*high);
		}
		return 0.5*low + 0.5*high;
	}
	public double accuracy(double beta, AccType accType) {
		List<Map<String,Double>> betaTags = betaTags(beta, accType);
		List<String> goldTagSrc = (accType == AccType.NORMAL) ? goldTags
				: ((accType == AccType.UNK_PREDS) ? goldTagsSubset(unkPredIndices) : goldTagsSubset(unkPredTagIndices));
		int numCorrect = 0;
		
		for(int i = 0;i < betaTags.size();i++) {
			if(betaTags.get(i).containsKey(goldTagSrc.get(i))) {
				numCorrect++;
			}
		}
		return numCorrect*1.0 / betaTags.size();
	}
	public double multitag(double beta) {
		List<Map<String,Double>> betaTags = betaTags(beta, AccType.NORMAL);
		int numTags = 0;
		for(Map<String,Double> tags : betaTags) {
			numTags += tags.size();
		}
		return numTags*1.0 / betaTags.size();
	}
}