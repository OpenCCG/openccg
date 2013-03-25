package opennlp.ccg.disjunctivizer;

import static org.junit.Assert.*;

import opennlp.ccg.disjunctivizer.MatchType;
import opennlp.ccg.disjunctivizer.VertexMatchFilter;
import opennlp.ccg.hylo.ModeLabel;
import opennlp.ccg.hylo.NominalAtom;
import opennlp.ccg.hylo.Proposition;
import opennlp.ccg.hylo.graph.LFEdge;
import opennlp.ccg.hylo.graph.LFEdgeLabel;
import opennlp.ccg.hylo.graph.LFBaseTest;
import opennlp.ccg.hylo.graph.LFVertex;

import org.junit.Before;
import org.junit.Test;

public class VertexMatchFilterTest extends LFBaseTest {
	
	VertexMatchFilter filter;
	LFVertex one, two;
	LFEdge edge;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		one = new LFVertex(new NominalAtom("w0"), new Proposition("blah"));
		two = new LFVertex(new NominalAtom("w1"), new Proposition("blah"));
		
		edge = new LFEdge(two,
				new LFVertex(new NominalAtom("w2"), new Proposition("blah blah")),
				new LFEdgeLabel(new ModeLabel("blase")));
		
		filter = new VertexMatchFilter(two, MatchType.SOURCE_MATCH);
	}

	@Test
	public void testVertexMatchFilter() {
		try {
			new VertexMatchFilter(null, MatchType.SOURCE_PREDICATE_MATCH);
			fail("able to specify null basis");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}
	
	@Test
	public void testSetBasis() {
		filter.setBasis(one);
		assertFalse(filter.allows(edge));
		
		edge = new LFEdge(one, edge.getTarget(), edge.getLabel());
		assertTrue(filter.allows(edge));
		
		try {
			filter.setBasis(null);
			fail("able to specify null basis");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}

	@Test
	public void testAllows() {
		assertTrue(filter.allows(edge));
		
		edge = new LFEdge(one, edge.getTarget(), edge.getLabel());
		assertFalse(filter.allows(edge));
	}

}
