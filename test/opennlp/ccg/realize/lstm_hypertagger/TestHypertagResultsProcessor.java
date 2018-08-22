package opennlp.ccg.realize.lstm_hypertagger;
import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
//import feat_extract.WordInfoMap;
//import main.Exceptions.NoPredicatesException;
//import opennlp.ccg.hylo.*;
//import opennlp.ccg.synsem.LF;

public class TestHypertagResultsProcessor {
	private HypertagResultsProcessor uut = new HypertagResultsProcessor();
	
	@Test
	public void testFilter() {
		Map<String,Double> tags = new HashMap<>();
		tags.put("s\\np", 0.9);
		tags.put("s\\np/np", 0.08);
		tags.put("s", 0.016);
		tags.put("np", 0.004);
		double beta = 0.16;
		tags = uut.filter(tags, beta);
		
		Map<String,Double> expTags = new HashMap<>();
		expTags.put("s\np", 0.9);
		assertEquals(expTags, tags);
	}
	// TODO: No easy way to instantiate a Nominal currently
//	@Test
//	public void testGoldTag() throws NoPredicatesException {
//		String predInfo = "w0:np:NNP:John w1:s\np/np:VB:like w2:np:NNP:Mary";
//		WordInfoMap wordInfoMap = new WordInfoMap(null);
//		wordInfoMap.parse(predInfo);
//		
//		Nominal nom = new NominalAtom("w0");
//		LF arg = new Proposition("John");
//		SatOp pred = new SatOp(nom, arg);
//		String goldTag = uut.goldTag(pred, wordInfoMap);
//		assertEquals(goldTag, "np");
//	}
}
