package opennlp.ccg.disjunctivizer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import opennlp.ccg.disjunctivizer.EdgeMatchFilter;
import opennlp.ccg.disjunctivizer.MatchType;
import opennlp.ccg.hylo.ModeLabel;
import opennlp.ccg.hylo.NominalAtom;
import opennlp.ccg.hylo.Proposition;
import opennlp.ccg.hylo.graph.LFEdge;
import opennlp.ccg.hylo.graph.LFEdgeLabel;
import opennlp.ccg.hylo.graph.LFBaseTest;
import opennlp.ccg.hylo.graph.LFVertex;

import org.junit.Before;
import org.junit.Test;

public class EdgeMatchFilterTest extends LFBaseTest {

	EdgeMatchFilter filter;
	LFEdge edge;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		edge = new LFEdge(new LFVertex(new NominalAtom("w0"), new Proposition("blah")),
				new LFVertex(new NominalAtom("w1"), new Proposition("blah blah")),
				new LFEdgeLabel(new ModeLabel("blase")));
		
		filter = new EdgeMatchFilter(edge, MatchType.LABEL_MISMATCH, MatchType.SOURCE_PREDICATE_MISMATCH,
				MatchType.TARGET_PREDICATE_MATCH);
	}

	@Test
	public void testEdgeMatchFilter() {
		try {
			new EdgeMatchFilter(null, MatchType.LABEL_MATCH);
			fail("able to specify null edge");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}
	
	@Test
	public void testAllows() {
		LFEdge test = new LFEdge(new LFVertex(new NominalAtom("w0"), new Proposition("blah blah")),
				new LFVertex(new NominalAtom("w1"), new Proposition("blah blah")),
				new LFEdgeLabel(new ModeLabel("boring")));
		
		assertTrue(filter.allows(test));
		
		test = new LFEdge(test.getSource(), test.getTarget(), edge.getLabel());
		assertFalse(filter.allows(test));
	}

}
