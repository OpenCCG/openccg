package opennlp.ccg.disjunctivizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import opennlp.ccg.disjunctivizer.AlignedEdgeFilter;
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

public class AlignedEdgeFilterTest extends LFBaseTest {

	AlignedEdgeFilter filter;
	Set<Integer> indices;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		indices = new HashSet<Integer>();
		indices.add(0);
		indices.add(2);
		indices.add(3);
		
		filter = new AlignedEdgeFilter(indices, MatchType.SOURCE_ALIGNED, MatchType.TARGET_UNALIGNED);
	}
	
	@Test
	public void testAlignedEdgeFilter() {
		try {
			new AlignedEdgeFilter(null, MatchType.SOURCE_ALIGNED);
			fail("able to specify null alignment indices");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}

	@Test
	public void testGetAlignmentIndices() {
		assertEquals(indices, filter.getAlignmentIndices());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSetAlignmentIndices() {
		filter.setAlignmentIndices(Collections.EMPTY_SET);
		assertTrue(filter.getAlignmentIndices().isEmpty());
		
		try {
			filter.setAlignmentIndices(null);
			fail("able to specify null alignment indices");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAllows() {
		LFEdge one = new LFEdge(new LFVertex(new NominalAtom("w0"), new Proposition("blah")),
				new LFVertex(new NominalAtom("w1"), new Proposition("blah blah")),
				new LFEdgeLabel(new ModeLabel("blase"))),
			two = new LFEdge(new LFVertex(new NominalAtom("w2"), new Proposition("blah")),
				new LFVertex(new NominalAtom("w3"), new Proposition("blah blah")),
				new LFEdgeLabel(new ModeLabel("blase")));
		
		assertTrue(filter.allows(one));
		assertFalse(filter.allows(two));
		
		filter.setAlignmentIndices(Collections.EMPTY_SET);
		assertFalse(filter.allows(one));
		assertFalse(filter.allows(two));
		
		// make self-contradictory filter
		filter = new AlignedEdgeFilter(indices, MatchType.SOURCE_ALIGNED, MatchType.SOURCE_UNALIGNED);
		assertFalse(filter.allows(one));
		assertFalse(filter.allows(two));
	}

}
