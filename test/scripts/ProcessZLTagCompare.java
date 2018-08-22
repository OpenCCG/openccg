package scripts;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ProcessZLTagCompare {
	public static void main(String[] args) throws IOException {
		Scanner tagcompareFile = new Scanner(new File("hypertagger_logs/zltagcompare.txt"));
		List<String> unkPreds = Files.readAllLines(new File("/home/reid/projects/research/ccg/taggerflow_modified/data/unknown_preds_decode").toPath());
		List<String> unkPredTags = Files.readAllLines(new File("/home/reid/projects/research/ccg/taggerflow_modified/data/unknown_predtags_decode").toPath());
		TagCompare tagcompare = parseTagCompareFile(tagcompareFile, unkPreds, unkPredTags);
		
//		double multitag = 3.9;
//		double beta = tagcompare.findBeta(multitag);
//		System.out.println(beta);
		System.out.println(tagcompare.accuracy(0.625, TagCompare.AccType.UNK_PREDS));
		System.out.println(tagcompare.accuracy(0.625, TagCompare.AccType.UNK_PREDTAGS));
	}
	public static TagCompare parseTagCompareFile(Scanner file, List<String> unkPreds, List<String> unkPredTags) {
		TagCompare tagcompare = new TagCompare();
		int numSents = 0, numTokens = 0;		
		while(file.hasNext()) {
			String line = file.nextLine();
			if(line.length() == 0) {
				numSents += 1;
				continue;
			}
			String[] items = line.split(" ");
			tagcompare.addGoldTag(items[0]);
			tagcompare.addPredName(items[1]);
			Map<String,Double> tokenBetaTags = new HashMap<>();
			for(int i = 2;i < items.length;i += 2) {
				tokenBetaTags.put(items[i+1], Double.parseDouble(items[i]));
			}
			tagcompare.addTokenBetaTags(tokenBetaTags);
			
			if(unkPreds.contains(items[1])) {
				tagcompare.addUnkPredIndex(numTokens);
			}
			if(unkPredTags.contains(items[1] + "|" + items[0])) {
				tagcompare.addUnkPredTagIndex(numTokens);
			}
			numTokens += 1;
		}
		System.out.println("Read " + numSents + " sentences, " + numTokens + " tokens");
		return tagcompare;
	}
}
