package opennlp.ccg.disjunctivizer;

import static org.junit.Assert.*;

import opennlp.ccg.disjunctivizer.LabelMatchFilter;
import opennlp.ccg.hylo.ModeLabel;
import opennlp.ccg.hylo.NominalAtom;
import opennlp.ccg.hylo.Proposition;
import opennlp.ccg.hylo.graph.LFEdge;
import opennlp.ccg.hylo.graph.LFEdgeLabel;
import opennlp.ccg.hylo.graph.LFBaseTest;
import opennlp.ccg.hylo.graph.LFVertex;

import org.junit.Before;
import org.junit.Test;

public class LabelMatchFilterTest extends LFBaseTest {

	LabelMatchFilter filter;
	LFEdgeLabel label;
	LFEdge one, two;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		one = new LFEdge(new LFVertex(new NominalAtom("w0"), new Proposition("blah")),
				new LFVertex(new NominalAtom("w1"), new Proposition("blah blah")),
				new LFEdgeLabel(new ModeLabel("blase")));
		two = new LFEdge(new LFVertex(new NominalAtom("w2"), new Proposition("blah")),
				new LFVertex(new NominalAtom("w3"), new Proposition("blah blah")),
				new LFEdgeLabel(new ModeLabel("blurg")));
		
		label = new LFEdgeLabel(new ModeLabel("blase"));
		
		filter = new LabelMatchFilter(label);
	}
	
	@Test
	public void testLabelMatchFilter() {
		try {
			new LabelMatchFilter(null);
			fail("able to specify null label");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}

	@Test
	public void testSetBasis() {
		filter.setBasis(new LFEdgeLabel(new ModeLabel("boo")));
		
		assertFalse(filter.allows(one));
		assertFalse(filter.allows(two));
		
		try {
			filter.setBasis(null);
			fail("able to specify null label");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}

	@Test
	public void testAllows() {
		assertTrue(filter.allows(one));
		assertFalse(filter.allows(two));
	}

}
