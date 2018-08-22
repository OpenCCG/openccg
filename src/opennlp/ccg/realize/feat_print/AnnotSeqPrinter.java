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

package opennlp.ccg.realize.feat_print;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import opennlp.ccg.realize.feat_extract.LogicalForm;
import opennlp.ccg.realize.feat_extract.WordFeatures;

public class AnnotSeqPrinter {
	public static final String WORD_SEP = " ";
	public static final String FEAT_SEP = "|";
	protected WordFeatStringBuilder wordBuilder;
	protected PrintWriter featWriter;
	protected PrintWriter orderWriter;
	
	public AnnotSeqPrinter() {
		wordBuilder = new WordFeatStringBuilder();
	}
	public AnnotSeqPrinter(String featFile, String sentFile) throws FileNotFoundException {
		wordBuilder = new WordFeatStringBuilder();
		featWriter = new PrintWriter(featFile);
		orderWriter = new PrintWriter(sentFile);
	}
	
	public void writeLF(LogicalForm sentence, String[] featOrder, List<String> wordIDOrder) {
		String sentFeatStr = sentFeatString(sentence, featOrder, wordIDOrder);
		featWriter.println(sentFeatStr);
		orderWriter.println(wordIDOrder);
		String words = wordsInOrderOfID(sentence, wordIDOrder);
		orderWriter.println(words);
	}
	public void close() {
		featWriter.close();
		orderWriter.close();
	}
	
	/** @return sentence feature string consisting of word feature strings separated by spaces.
	 * Word feature strings are in format pred_name|supertag|feature1|..., with order of features specified by featureOrder.
	 * Order of words in sentence depends on what feature printer is used. Default prints words in random order. */
	public String sentFeatString(LogicalForm sentence, String[] featOrder, List<String> wordIDOrder) {
		String sentFeatStr = "";		
		for(String wordID : wordIDOrder) {
			if(wordID.matches("w[0-9]*")) {
				WordFeatures wordFeats = sentence.getWordFeatures(wordID);
				String supertag = sentence.getSupertag(wordID);
				String wordFeatStr = wordBuilder.wordFeatString(wordFeats, supertag, featOrder);
				sentFeatStr += wordFeatStr + WORD_SEP;
			} else {
				sentFeatStr += nonwordFeatString(wordID, featOrder) + WORD_SEP;
			}
		}
		return sentFeatStr.trim();
	}
	/** Verified only called for parentheses */
	public String nonwordFeatString(String nonword, String[] featOrder) {
		String result = nonword + FEAT_SEP;
		for(String feat : featOrder) {
			if(feat.contains("#")) {
				int numVals = Integer.parseInt(feat.substring(feat.indexOf("#") + 1));
				result += StringUtils.repeat(FEAT_SEP, numVals);
			} else {
				result += FEAT_SEP;
			}
		}
		return result;
	}
	public String wordsInOrderOfID(LogicalForm sentence, List<String> wordIDOrder) {
		String str = "";
		for(String wordID : wordIDOrder) {
			if(wordID.matches("w[0-9]*")) {
				String word = sentence.getWordFeatures(wordID).getUniqueFeature("PN");
				str += word + ", ";
			}
		}
		return str.substring(0, str.length()-2).replace(",", "");
	}
}
