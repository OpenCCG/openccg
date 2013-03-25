package opennlp.ccg.disjunctivizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import opennlp.ccg.alignment.Alignment;
import opennlp.ccg.alignment.Mapping;
import opennlp.ccg.alignment.Phrase;
import opennlp.ccg.alignment.PhrasePosition;
import opennlp.ccg.hylo.ModeLabel;
import opennlp.ccg.hylo.NominalAtom;
import opennlp.ccg.hylo.Proposition;
import opennlp.ccg.hylo.graph.LFEdge;
import opennlp.ccg.hylo.graph.LFEdgeLabel;
import opennlp.ccg.hylo.graph.LFGraph;
import opennlp.ccg.hylo.graph.LFBaseTest;
import opennlp.ccg.hylo.graph.LFVertex;

import org.junit.Before;
import org.junit.Test;

public class LFGraphDifferenceTest extends LFBaseTest {

	LFGraph aGraph, bGraph;
	Alignment alignment;
	LFGraphDifference diff;
	
	LFEdge aDet, aArg0, aMod, bArg0;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		alignment = new Alignment(new Phrase(337, "A", "boy", "walks", "quickly"),
				new Phrase(337, "He", "moves"),
				Collections.EMPTY_SET);
		
		alignment.add(new Mapping(0, 0));
		alignment.add(new Mapping(1, 0));
		alignment.add(new Mapping(2, 1));
		
		aGraph = new LFGraph();
		
		LFVertex aw0 = new LFVertex(new NominalAtom("w0"), new Proposition("a")),
			aw1 = new LFVertex(new NominalAtom("w1"), new Proposition("boy")),
			aw2 = new LFVertex(new NominalAtom("w2"), new Proposition("walk")),
			aw3 = new LFVertex(new NominalAtom("w3"), new Proposition("quickly"));
		
		aGraph.addVertex(aw0);
		aGraph.addVertex(aw1);
		aGraph.addVertex(aw2);
		aGraph.addVertex(aw3);
				
		aDet = aGraph.addLabeledEdge(aw1, aw0, LFEdgeLabel.forMode(new ModeLabel("Det")));
		aArg0 = aGraph.addLabeledEdge(aw2, aw1, LFEdgeLabel.forMode(new ModeLabel("Arg0")));
		aMod = aGraph.addLabeledEdge(aw2, aw3, LFEdgeLabel.forMode(new ModeLabel("Mod")));
		
		bGraph = new LFGraph();
		
		LFVertex bw0 = new LFVertex(new NominalAtom("w0"), new Proposition("he")),
			bw1 = new LFVertex(new NominalAtom("w1"), new Proposition("move"));
		
		bGraph.addVertex(bw0);
		bGraph.addVertex(bw1);
				
		bArg0 = bGraph.addLabeledEdge(bw1, bw0, LFEdgeLabel.forMode(new ModeLabel("Arg0")));
		
		diff = new LFGraphDifference(aGraph, bGraph, alignment);
	}
	
	@Test
	public void testLFGraphDifference() {
		try {
			new LFGraphDifference(null, bGraph, alignment);
			fail("able to create LF graph difference with null graph");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			new LFGraphDifference(aGraph, null, alignment);
			fail("able to create LF graph difference with null graph");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			new LFGraphDifference(aGraph, bGraph, null);
			fail("able to create LF graph difference with null graph");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}

	@Test
	public void testReverse() {
		LFGraphDifference ffid = diff.reverse();
		
		assertEquals(diff.a.vertexSet(), ffid.b.vertexSet());
		assertEquals(diff.a.edgeSet(), ffid.b.edgeSet());
		
		for(PhrasePosition pos : PhrasePosition.values()) {
			assertEquals(diff.alignment.get(pos), ffid.alignment.get(pos.opposite()));
		}
		
		Map<Integer, Set<Integer>> m = ffid.alignment.asMap();
		
		assertTrue(m.get(0).contains(0));
		assertTrue(m.get(0).contains(1));
		assertTrue(m.get(1).contains(2));
	}

	@Test
	public void testDeletes() {
		Set<LFEdge> dels = diff.deletes();
		
		try {
			dels.add(bArg0);
			fail("able to add edge");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
		
		try {
			dels.remove(bArg0);
			fail("able to remove edge");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
		
		try {
			Iterator<LFEdge> i = dels.iterator();
			i.next();
			i.remove();
			fail("able to remove edge");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
		
		assertEquals(Collections.singleton(aMod), diff.deletes());
		assertEquals(Collections.emptySet(), diff.reverse().deletes());
	}

	@Test
	public void testInserts() {
		Set<LFEdge> ins = diff.inserts();
		
		try {
			ins.add(bArg0);
			fail("able to add edge");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
		
		try {
			ins.remove(bArg0);
			fail("able to remove edge");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
		
		assertEquals(Collections.emptySet(), diff.inserts());
		assertEquals(Collections.singleton(aMod), diff.reverse().inserts());
	}

	@Test
	public void testSubstitutions() {
		Set<LFEdge> subs = diff.substitutions();
		
		try {
			subs.add(bArg0);
			fail("able to add edge");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
		
		try {
			subs.remove(bArg0);
			fail("able to remove edge");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
		
		try {
			Iterator<LFEdge> i = subs.iterator();
			i.next();
			i.remove();
			fail("able to remove edge");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
		
		assertTrue(subs.contains(bArg0));
		assertFalse(subs.contains(aArg0));
		assertFalse(subs.contains(aDet));
		assertFalse(subs.contains(aMod));
		
		assertEquals(Collections.singleton(bArg0), diff.substitutionsFor(aArg0));
		assertEquals(Collections.singleton(aArg0), diff.reverse().substitutionsFor(bArg0));
	}

	@Test
	public void testSubstitutionsBySource() {
		Map<LFVertex, Set<LFEdge>> map = diff.substitutionsBySource();
		assertTrue(map.keySet().contains(bArg0.getSource()));
		assertTrue(map.get(bArg0.getSource()).contains(bArg0));
		assertEquals(1, map.size());
		
		try {
			map.remove(bArg0.getSource());
			fail("able to remove edge");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
		
		try {
			map.put(aArg0.getSource(), Collections.singleton(aArg0));
			fail("able to put edge");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
		
		try {
			Iterator<Map.Entry<LFVertex, Set<LFEdge>>> i = map.entrySet().iterator();
			i.next();
			i.remove();
			fail("able to remove entry");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
	}
	
	@Test
	public void testSubstitutionsBySourceFor() {
		Map<LFVertex, Set<LFEdge>> map = diff.substitutionsBySourceFor(aArg0);
		assertTrue(map.keySet().contains(bArg0.getSource()));
		assertTrue(map.get(bArg0.getSource()).contains(bArg0));
		assertEquals(1, map.size());
		
		try {
			map.remove(bArg0.getSource());
			fail("able to remove edge");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
		
		try {
			map.put(aArg0.getSource(), Collections.singleton(aArg0));
			fail("able to put edge");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
		
		try {
			Iterator<Map.Entry<LFVertex, Set<LFEdge>>> i = map.entrySet().iterator();
			i.next();
			i.remove();
			fail("able to remove entry");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
		
		map = diff.substitutionsBySourceFor(aDet);
		assertTrue(map.isEmpty());
	}
}
