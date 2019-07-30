package opennlp.ccg.realize.lstm_hypertagger;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * Unit tests for HypertaggerResults class
 * @author Reid Fu
 */
public class TestHypertaggerResults {	
	@Test
	public void test0() {
		String[] predOrderArr = {"w1","w1","w0","w2","w0"};
		String[] linOrderArr = {"(","w1","w0",")",")","w2"};
		List<String> predOrder = Arrays.asList(predOrderArr);
		List<String> linOrder = Arrays.asList(linOrderArr);
		
		List<Map<String,Double>> results = new ArrayList<>();
		results.add(new HashMap<>());
		Map<String,Double> mapW1 = new HashMap<>();
		mapW1.put("np", 1.0);
		results.add(mapW1);
		Map<String,Double> mapW0 = new HashMap<>();
		mapW0.put("s\np/np", 1.0);
		results.add(mapW0);
		results.add(new HashMap<>());
		results.add(new HashMap<>());
		Map<String,Double> mapW2 = new HashMap<>();
		mapW2.put("np", 1.0);
		results.add(mapW2);
		
		Map<Integer,Integer> predLinMap = new HashMap<>();
		predLinMap.put(0, 1); predLinMap.put(1, 1);
		predLinMap.put(2, 2); predLinMap.put(3, 5);
		predLinMap.put(4, 2);
		
		HypertagResults htResults = new HypertagResults(predOrder, linOrder, results);
		assertEquals(htResults.buildPredLinMap(predOrder, linOrder), predLinMap);
		assertEquals(htResults.tagsForPred(4), mapW0);
	}
}
