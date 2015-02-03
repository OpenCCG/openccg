package opennlp.ccg.lexicon;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LexiconObject {

	public List<Family> families = new LinkedList<Family>();
	public LicensingFeature[] licensingFeatures;
	public String[] distributiveFeatures;
	public Map<String, Integer> relationIndexMap = new HashMap<String, Integer>();

}
