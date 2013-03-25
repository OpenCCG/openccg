package opennlp.ccg.disjunctivizer;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import opennlp.ccg.disjunctivizer.AlignedEdgeFilter;
import opennlp.ccg.disjunctivizer.EdgeMatchFilter;
import opennlp.ccg.disjunctivizer.FilteredLFEdgeSet;
import opennlp.ccg.disjunctivizer.MatchType;
import opennlp.ccg.hylo.ModeLabel;
import opennlp.ccg.hylo.NominalAtom;
import opennlp.ccg.hylo.Proposition;
import opennlp.ccg.hylo.graph.LFEdge;
import opennlp.ccg.hylo.graph.LFEdgeLabel;
import opennlp.ccg.hylo.graph.LFBaseTest;
import opennlp.ccg.hylo.graph.LFVertex;
import opennlp.ccg.util.CompositeFilter;
import opennlp.ccg.util.Filter;

import org.junit.Before;
import org.junit.Test;

public class FilteredLFEdgeSetTest extends LFBaseTest {

	FilteredLFEdgeSet set;
	Set<LFEdge> edges;
	Filter<LFEdge> edgeFilter;
	
	LFEdge one, two, three;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		one = new LFEdge(new LFVertex(new NominalAtom("w0"), new Proposition("blah")),
				new LFVertex(new NominalAtom("w1"), new Proposition("blah blah")),
				new LFEdgeLabel(new ModeLabel("blase")));
		two = new LFEdge(new LFVertex(new NominalAtom("w2"), new Proposition("blah")),
				new LFVertex(new NominalAtom("w3"), new Proposition("blah blah")),
				new LFEdgeLabel(new ModeLabel("bored")));
		three = new LFEdge(new LFVertex(new NominalAtom("w0"), new Proposition("zzz")),
				new LFVertex(new NominalAtom("w2"), new Proposition("snooze")),
				new LFEdgeLabel(new ModeLabel("blase")));
		
		edges = new LinkedHashSet<LFEdge>();
		edges.add(one);
		edges.add(two);
		edges.add(three);
		
		Set<Integer> indices = new HashSet<Integer>();
		indices.add(0);
		indices.add(2);
		indices.add(3);
		
		edgeFilter = new CompositeFilter<LFEdge>(new EdgeMatchFilter(one, MatchType.SOURCE_PREDICATE_MATCH,
				MatchType.LABEL_MISMATCH), new AlignedEdgeFilter(indices,
						MatchType.TARGET_ALIGNED, MatchType.SOURCE_ALIGNED));
		
		set = new FilteredLFEdgeSet(edges, edgeFilter);
	}

	@Test
	public void testSourceView() {
		assertTrue(set.sourceView().contains(two.getSource()));
		assertEquals(1, set.sourceView().size());
	}

	@Test
	public void testTargetView() {
		assertTrue(set.targetView().contains(two.getTarget()));
		assertEquals(1, set.targetView().size());
	}

	@Test
	public void testLabelView() {
		assertTrue(set.labelView().contains(two.getLabel()));
		assertEquals(1, set.labelView().size());
	}

}
