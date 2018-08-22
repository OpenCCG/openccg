package opennlp.ccg.realize.hylo_feat_extract;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.junit.Test;

import opennlp.ccg.grammar.Grammar;
import opennlp.ccg.hylo.HyloHelper;
import opennlp.ccg.hylo.SatOp;
import opennlp.ccg.realize.lstm_hypertagger.feat_extract.LogicalForm;
import opennlp.ccg.realize.lstm_hypertagger.hylo_feat_extract.HyloFlatLFParser;
import opennlp.ccg.synsem.LF;
import opennlp.ccg.test.RegressionInfo;

public class TestHyloFlatLFParser {
	static String GRAMMAR_FILE = "/home/reid/projects/research/ccg/openccg/ccgbank/extract/grammar.xml";
	private HyloFlatLFParser uut = new HyloFlatLFParser();
	
	public List<SatOp> getPreds(String grammarFile, String testXML) throws IOException {
		URL grammarURL = new File(grammarFile).toURI().toURL();
		Grammar grammar = new Grammar(grammarURL);
		RegressionInfo rinfo = new RegressionInfo(grammar, new File(testXML));
		RegressionInfo.TestItem testItem = rinfo.getItem(0);
		Element lfElt = testItem.lfElt;
		Document doc = new Document();
        lfElt.detach();
        doc.setRootElement(lfElt);
        LF inputLF = grammar.loadLF(doc);
        List<SatOp> preds = HyloHelper.flatten(inputLF);
        return preds;
	}
	@Test
	public void testError0() throws IOException {
		String testXML = "/home/reid/projects/research/ccg/openccg/ccgbank/extract/test/test_realize/test_error0.xml";
		List<SatOp> preds = getPreds(GRAMMAR_FILE, testXML);
		LogicalForm parse = uut.parse(preds);
		assertEquals(2, parse.getHead().getChildList().size());
		assertEquals(1, parse.getWordFeatures("x1").getChildList().size());
		assertEquals(parse.getWordFeatures("w8"), parse.getWordFeatures("x1").getChildList().get(0));
	}
}
