package test;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import opennlp.ccg.realize.feat_extract.LogicalForm;
import opennlp.ccg.realize.feat_print.AnnotSeqPrinter;
import opennlp.ccg.realize.linearizer.*;
import opennlp.ccg.grammar.Grammar;
import opennlp.ccg.hylo.HyloHelper;
import opennlp.ccg.hylo.SatOp;
import opennlp.ccg.realize.hylo_feat_extract.HyloFlatLFParser;
import opennlp.ccg.synsem.LF;
import opennlp.ccg.test.Regression;
import opennlp.ccg.test.RegressionInfo;

public class PrintHyloLF {
	static String TEST_DIR = "/home/reid/projects/research/ccg/openccg/ccgbank/extract/test/00";
	static String GRAMMAR_FILE = "/home/reid/projects/research/ccg/openccg/ccgbank/extract/grammar.xml";
	static String OUTPUT_DATA = "/home/reid/projects/research/ccg/ccg_extractor_linearizer/output/hylo_dev";
	static String OUTPUT_SENTS = "/home/reid/projects/research/ccg/ccg_extractor_linearizer/output/hylo_dev_sents.txt";
	
	public static void main(String[] args) throws IOException {
		URL grammarURL = new File(GRAMMAR_FILE).toURI().toURL();
		Grammar grammar = new Grammar(grammarURL);
		List<File> testFiles = Regression.getXMLFiles(new File(TEST_DIR));
		HyloFlatLFParser parser = new HyloFlatLFParser();
		LinConfig config = new LinConfig(FeatOrders.ALL_REL, 5, false, ChildOrders.ENG);
		Linearizer linearizer = config.getLinearizer();
		AnnotSeqPrinter printer = new AnnotSeqPrinter(OUTPUT_DATA, OUTPUT_SENTS);
		
		for(File file : testFiles) {
			RegressionInfo rinfo = new RegressionInfo(grammar, file);
			int numItems = rinfo.numberOfItems();
			for (int i=0; i < numItems; i++) {
				RegressionInfo.TestItem testItem = rinfo.getItem(i);
				Element lfElt = testItem.lfElt;
				Document doc = new Document();
		        lfElt.detach();
		        doc.setRootElement(lfElt);
		        LF inputLF = grammar.loadLF(doc);
		        List<SatOp> preds = HyloHelper.flatten(inputLF);
		        LogicalForm lf = parser.parse(preds);
		        List<String> linOrder = linearizer.getOrder(lf, lf.getHead(), new HashSet<String>(), config);
		        printer.writeLF(lf, config.featOrder(), linOrder);
			}
		}
		printer.close();
		System.out.println("Done");
	}
}
