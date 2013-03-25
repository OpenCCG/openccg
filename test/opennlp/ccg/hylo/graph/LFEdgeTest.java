package opennlp.ccg.hylo.graph;

import static org.junit.Assert.*;

import opennlp.ccg.hylo.ModeLabel;
import opennlp.ccg.hylo.NominalAtom;
import opennlp.ccg.hylo.Proposition;

import org.junit.Before;
import org.junit.Test;

public class LFEdgeTest extends LFBaseTest {

	LFVertex source, target;
	LFEdgeLabel label;
	LFEdge edge;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		source = new LFVertex(new NominalAtom("w3"), new Proposition("prop1"));
		target = new LFVertex(new NominalAtom("w9"), new Proposition("prop2"));
		label = new LFEdgeLabel(new ModeLabel("Arg0"));
		
		edge = new LFEdge(source, target, label);
	}

	@Test
	public void testLFEdge() {
		try {
			new LFEdge(null, target);
			fail("able to specify null target");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			new LFEdge(source, null);
			fail("able to specify null target");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			new LFEdge(source, target, null);
		}
		catch(IllegalArgumentException expected) {
			fail("unable to specify null label");
		}
	}
	
	@Test
	public void testEqualsObject() {
		assertEquals(edge, new LFEdge(edge.source, edge.target, edge.label));
	}

}
