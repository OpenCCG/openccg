package opennlp.ccg.hylo.graph;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import opennlp.ccg.hylo.ModeLabel;
import opennlp.ccg.hylo.NominalAtom;
import opennlp.ccg.hylo.Proposition;
import opennlp.ccg.realize.Realizer;
import opennlp.ccg.synsem.LF;

import org.jdom.input.DOMBuilder;
import org.junit.Before;
import org.junit.Test;

public class LFGraphTest extends LFBaseTest {

	LF testLF;
	LFGraph graph, expected;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		DocumentBuilder db;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		catch(ParserConfigurationException e) {
			throw new Exception("problem with parser configuration: " + e.getLocalizedMessage(), e);
		}
		
		File testFile = new File(new File(new File(System.getProperty("user.dir")), "test"), "testlf.xml");
		testLF = Realizer.getLfFromElt(new DOMBuilder().build(db.parse(testFile).getDocumentElement()));
		
		graph = LFGraphFactory.newGraphFrom(testLF);
		
		expected = new LFGraph(LFGraphFactory.DEFAULT_EDGE_FACTORY);
		LFVertex w7 = new LFVertex(new NominalAtom("w7"), new Proposition("be"));
		w7.setAttribute(new ModeLabel("mood"), new Proposition("dcl"));
		w7.setAttribute(new ModeLabel("tense"), new Proposition("past"));
		expected.addVertex(w7);
		
		LFVertex w0 = new LFVertex(new NominalAtom("w0"), new Proposition("bank"));
		w0.setAttribute(new ModeLabel("det"), new Proposition("nil"));
		expected.addVertex(w0);
		
		LFVertex w1 = new LFVertex(new NominalAtom("w1"), new Proposition("of"));
		expected.addVertex(w1);
		
		LFVertex w2 = new LFVertex(new NominalAtom("w2"), new Proposition("holland"));
		w2.setAttribute(new ModeLabel("det"), new Proposition("nil"));
		w2.setAttribute(new ModeLabel("num"), new Proposition("sg"));
		expected.addVertex(w2);
		
		LFVertex w5 = new LFVertex(new NominalAtom("w5"), new Proposition("office"));
		w5.setAttribute(new ModeLabel("det"), new Proposition("nil"));
		w5.setAttribute(new ModeLabel("num"), new Proposition("sg"));
		expected.addVertex(w5);
		
		LFVertex w4 = new LFVertex(new NominalAtom("w4"), new Proposition("wuhan"));
		w4.setAttribute(new ModeLabel("num"), new Proposition("sg"));
		expected.addVertex(w4);
		
		LFVertex w9 = new LFVertex(new NominalAtom("w9"), new Proposition("officially"));
		expected.addVertex(w9);
		
		LFVertex w8 = new LFVertex(new NominalAtom("w8"), new Proposition("also"));
		expected.addVertex(w8);
		
		LFVertex w10 = new LFVertex(new NominalAtom("w10"), new Proposition("establish"));
		w10.setAttribute(new ModeLabel("tense"), new Proposition("past"));
		expected.addVertex(w10);
		
		LFVertex w11 = new LFVertex(new NominalAtom("w11"), new Proposition("just"));
		expected.addVertex(w11);
		
		LFVertex w12 = new LFVertex(new NominalAtom("w12"), new Proposition("recently"));
		expected.addVertex(w12);
		
		expected.addLabeledEdge(w7, w0, LFEdgeLabel.forMode(new ModeLabel("Arg0")));
		expected.addLabeledEdge(w0, w1, LFEdgeLabel.forMode(new ModeLabel("Mod")));
		expected.addLabeledEdge(w1, w2, LFEdgeLabel.forMode(new ModeLabel("Arg1")));
		expected.addLabeledEdge(w2, w5, LFEdgeLabel.forMode(new ModeLabel("ApposRel")));
		expected.addLabeledEdge(w5, w4, LFEdgeLabel.forMode(new ModeLabel("Mod")));
		
		expected.addLabeledEdge(w7, w9, LFEdgeLabel.forMode(new ModeLabel("Arg1")));
		expected.addLabeledEdge(w9, w0, LFEdgeLabel.forMode(new ModeLabel("Arg0")));
		
		expected.addLabeledEdge(w7, w8, LFEdgeLabel.forMode(new ModeLabel("Mod")));
		expected.addLabeledEdge(w7, w10, LFEdgeLabel.forMode(new ModeLabel("GenRel")));
		expected.addLabeledEdge(w10, w0, LFEdgeLabel.forMode(new ModeLabel("Arg1")));
		expected.addLabeledEdge(w10, w11, LFEdgeLabel.forMode(new ModeLabel("Mod")));
		expected.addLabeledEdge(w10, w12, LFEdgeLabel.forMode(new ModeLabel("Mod")));		
	}

	@Test
	public void testLFGraph() {
		assertEquals(expected.vertexSet(), graph.vertexSet());
		assertEquals(expected.edgeSet(), graph.edgeSet());
	}

	@Test
	public void testRemoveVertex() {
		for(LFVertex v : new HashSet<LFVertex>(graph.vertexSet())) {
			graph.removeVertex(v);
			assertNull(graph.findVertexByNominal(v.nominal));
		}
	}
	
	@Test
	public void testFindVertexByNominal() {
		for(LFVertex vertex : expected.vertexSet()) {
			assertEquals(vertex, graph.findVertexByNominal(vertex.nominal));
		}
	}

}
