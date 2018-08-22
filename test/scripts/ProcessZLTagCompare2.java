package scripts;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessZLTagCompare2 {
	public static void main(String[] args) throws IOException {
		Path ht2Feats = new File("ccgbank/feats/hypertagger/dev.ht2.feats").toPath();
		Path assignTagFile = new File("ccgbank/feats/hypertagger/maxent.ht2.dev.out").toPath();
		List<String> unkPreds = Files.readAllLines(new File("/home/reid/projects/research/ccg/taggerflow_modified/data/unknown_preds_decode").toPath());
		List<String> unkPredTags = Files.readAllLines(new File("/home/reid/projects/research/ccg/taggerflow_modified/data/unknown_predtags_decode").toPath());
		TagCompare tagCompare = parseTagCompare(ht2Feats, assignTagFile, unkPreds, unkPredTags);
		
		System.out.println(tagCompare.accuracy(1, TagCompare.AccType.NORMAL));
		System.out.println(tagCompare.accuracy(1, TagCompare.AccType.UNK_PREDS));
		System.out.println(tagCompare.accuracy(1, TagCompare.AccType.UNK_PREDTAGS));
	}
	public static TagCompare parseTagCompare(Path ht2Feats, Path assignTagFile, List<String> unkPreds, List<String> unkPredTags) throws IOException {
		TagCompare tagCompare = new TagCompare();
		List<String> feats = Files.readAllLines(ht2Feats);
		List<String> assignTags = Files.readAllLines(assignTagFile);
		
		for(int i = 0;i < feats.size();i++) {
			String[] predAndGoldTag = getPredAndGoldTag(feats.get(i));
			String assignTag = assignTags.get(i);
			Map<String,Double> tokenBetaTags = new HashMap<>();
			tokenBetaTags.put(assignTag, 1.0);
			
			tagCompare.addGoldTag(predAndGoldTag[0]);
			tagCompare.addPredName(predAndGoldTag[1]);
			tagCompare.addTokenBetaTags(tokenBetaTags);
			
			if(unkPreds.contains(predAndGoldTag[1])) {
				tagCompare.addUnkPredIndex(i);
			}
			if(unkPredTags.contains(predAndGoldTag[1] + "|" + predAndGoldTag[0])) {
				tagCompare.addUnkPredTagIndex(i);
			}
		}
		return tagCompare;
	}
	/** Returned array contains gold tag, then predicate name */
	public static String[] getPredAndGoldTag(String tokenFeats) {
		String[] predAndGoldTag = new String[2];
		String[] tokenFeatsArr = tokenFeats.split(" ");
		predAndGoldTag[0] = tokenFeatsArr[0];
		
		for(String feat : tokenFeatsArr) {
			if(feat.startsWith("PN=")) {
				String pred = feat.substring(3, feat.indexOf(":1.0"));
				pred = (pred.matches("[A-Z]?[a-z]+\\.[0-9]+")) ? pred.substring(0, pred.indexOf(".")) : pred;
				predAndGoldTag[1] = pred;
				break;
			}
		}
		return predAndGoldTag;
	}
}
