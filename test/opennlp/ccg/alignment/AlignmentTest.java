package opennlp.ccg.alignment;

import static opennlp.ccg.alignment.PhrasePosition.A;
import static opennlp.ccg.alignment.PhrasePosition.B;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class AlignmentTest {

	Integer id;
	Phrase one, two;
	Set<Mapping> mappings;
	Alignment alignment;
	
	Map<Integer, Set<Integer>> map, pam;
	
	Set<Integer> twoAVals;
	
	@Before
	public void setUp() throws Exception {
		id = new Integer(37);
		one = new Phrase(id, Alignments.tokenize("This is it ."));
		two = new Phrase(id, Alignments.tokenize("A test this is ."));
		
		mappings = new HashSet<Mapping>();
		mappings.add(new Mapping(id, 0, 2));
		mappings.add(new Mapping(id, 1, 3));
		mappings.add(new Mapping(id, 3, 4));
		mappings.add(new Mapping(id, 2, 0));
		mappings.add(new Mapping(id, 2, 1));
		
		map = new HashMap<Integer, Set<Integer>>();
		pam = new HashMap<Integer, Set<Integer>>();
		
		map.put(0, new HashSet<Integer>(Collections.singleton(2)));
		map.put(1, new HashSet<Integer>(Collections.singleton(3)));
		map.put(3, new HashSet<Integer>(Collections.singleton(4)));
		
		twoAVals = new HashSet<Integer>();
		twoAVals.add(0);
		twoAVals.add(1);
		twoAVals = Collections.unmodifiableSet(twoAVals);
		
		map.put(2, twoAVals);
		
		pam.put(2, new HashSet<Integer>(Collections.singleton(0)));
		pam.put(3, new HashSet<Integer>(Collections.singleton(1)));
		pam.put(4, new HashSet<Integer>(Collections.singleton(3)));
		pam.put(0, new HashSet<Integer>(Collections.singleton(2)));
		pam.put(1, new HashSet<Integer>(Collections.singleton(2)));
		
		alignment = new Alignment(one, two, mappings);
		assertEquals(mappings, alignment);
	}
	
	@Test
	public void testCompare() {
		Phrase o = new Phrase(43, one), t = new Phrase(43, two);
		
		Set<Mapping> ms = new HashSet<Mapping>();
		for(Mapping m : mappings) {
			ms.add(m.copyWithPhraseNumber(43));
		}
		
		Alignment a = new Alignment(o, t, ms);
		
		assertEquals(-1, alignment.compareTo(a));
		assertEquals(1, a.compareTo(alignment));
		assertEquals(0, a.compareTo(a));
		assertEquals(0, alignment.compareTo(alignment));
	}

	@Test
	public void testAlignment() {
		try {
			new Alignment(null, two, mappings);
			fail("able to create alignment with null phrase");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			new Alignment(one, null, mappings);
			fail("able to create alignment with null phrase");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			new Alignment(one, two, null);
			fail("able to create alignment with null mappings");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}
	
	@Test
	public void testSize() {
		assertEquals(mappings.size(), alignment.size());
	}

	@Test
	public void testGet() {
		assertEquals(one, alignment.getA());
		assertEquals(two, alignment.getB());
		assertNotSame(one, alignment.getB());
	}

	@Test
	public void testAddMapping() {
		Mapping m = new Mapping(id, 0, 4);
		
		assertTrue(alignment.add(m));
		assertFalse(alignment.add(new Mapping(id, 3, 4)));
		
		alignment.remove(m);
		
		try {
			alignment.add(new Mapping(id + 1, 0, 4));
			fail("able to add mapping with non-matching ID");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			alignment.add(new Mapping(id, null, 5));
			fail("able to add mapping with null index");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			alignment.add(new Mapping(id, 0, 5));
			fail("able to add mapping with out of bounds index");
		}
		catch(IndexOutOfBoundsException expected) {
			// do nothing
		}
		
		try {
			alignment.add(new Mapping(id, -2, 3));
			fail("able to add mapping with out of bounds index");
		}
		catch(IndexOutOfBoundsException expected) {
			// do nothing
		}
		
		try {
			alignment.add(new Mapping(id, 5, 3));
			fail("able to add mapping with out of bounds index");
		}
		catch(IndexOutOfBoundsException expected) {
			// do nothing
		}
		
		try {
			alignment.add(new Mapping(id, 3, -3));
			fail("able to add mapping with out of bounds index");
		}
		catch(IndexOutOfBoundsException expected) {
			// do nothing
		}
	}
	
	@Test
	public void testGetTargets() {
		Set<Integer> ts = alignment.getTargets(2, A);
		Set<Integer> s = new HashSet<Integer>();
		s.add(0);
		s.add(1);
		
		assertFalse(s.retainAll(ts));
		assertEquals(s.size(), ts.size());
		
		ts.add(4);
		assertTrue(ts.contains(4));
		
		try {
			ts.remove(4);
			assertFalse(ts.contains(4));
		}
		catch(UnsupportedOperationException e) {
			fail("unable to call remove()");			
		}
		
		try {
			Iterator<Integer> i = ts.iterator();
			i.next();
			i.remove();
		}
		catch(UnsupportedOperationException e) {
			fail("unable to call iterator().remove()");
		}
	}
	
	@Test
	public void testMapEntrySet() {
		Map<Integer, Set<Integer>> amap = alignment.asMap(A), bmap = alignment.asMap(B);
		Set<Integer> s = new HashSet<Integer>(twoAVals);
		
		for(Map.Entry<Integer, Set<Integer>> e : amap.entrySet()) {
			if(e.getKey().equals(2)) {
				assertEquals(s, e.getValue());
			}
		}
		
		Iterator<Map.Entry<Integer, Set<Integer>>> i = bmap.entrySet().iterator();
		while(i.hasNext()) {
			Map.Entry<Integer, Set<Integer>> e = i.next();
			if(e.getKey().equals(1)) {
				assertEquals(Collections.singleton(2), e.getValue());
			}
			else if(e.getKey().equals(4)) {
				assertEquals(Collections.singleton(3), e.getValue());
			}
			else {
				try {
					i.remove();
				}
				catch(UnsupportedOperationException ex) {
					fail("unable to call Iterator.remove()");
				}
				
				try {
					assertTrue(e.getValue().add(3));
					assertTrue(e.getValue().contains(3));
					assertFalse(e.getValue().add(3));
				}
				catch(UnsupportedOperationException ex) {
					fail("unable to add to entry value");			
				}
				
				try {
					assertTrue(e.getValue().remove(3));
					assertFalse(e.getValue().contains(3));
					assertFalse(e.getValue().remove(3));
				}
				catch(UnsupportedOperationException ex) {
					fail("unable to remove from entry value");			
				}
				
				try {
					if(!e.getValue().isEmpty()) {
						e.getValue().remove(e.getValue().iterator().next());
					}
				}
				catch(UnsupportedOperationException ex) {
					fail("unable to call remove() for entry value");			
				}
				
				try {
					if(!e.getValue().isEmpty()) {
						Iterator<Integer> it = e.getValue().iterator();
						it.next();
						it.remove();
					}
				}
				catch(UnsupportedOperationException ex) {
					fail("unable to call remove() for entry value iterator");			
				}
				
				try {
					e.setValue(new HashSet<Integer>(Collections.singleton(0)));
				}
				catch(UnsupportedOperationException ex) {
					fail("unable to set entry value");
				}
			}
		}
	}
	
	@Test
	public void testMapValues() {
		Set<Set<Integer>> as = new HashSet<Set<Integer>>();
		as.add(Collections.singleton(2));
		as.add(Collections.singleton(3));
		as.add(Collections.singleton(4));
		
		Set<Integer> s = new HashSet<Integer>(twoAVals);
		as.add(s);
		
		Map<Integer, Set<Integer>> amap = alignment.asMap(A), bmap = alignment.asMap(B);
		
		assertTrue(amap.values().size() == as.size() && amap.values().containsAll(as));
		
		Set<Set<Integer>> bvals = new HashSet<Set<Integer>>(); // avoid doubling
		bvals.addAll(bmap.values());
		
		as.remove(s);
		as.add(Collections.singleton(0));
		as.add(Collections.singleton(1));
		as.remove(Collections.singleton(4));
		
		assertEquals(as, bvals);
		assertTrue(bmap.values().contains(Collections.singleton(2)));
		
		try {
			amap.values().add(Collections.singleton(1));
			fail("able to add value");
		}
		catch(UnsupportedOperationException expected) {
			// do nothing			
		}
		
		try {
			if(!amap.values().isEmpty()) {
				amap.values().remove(amap.values().iterator().next());
			}
		}
		catch(UnsupportedOperationException ex) {
			fail("unable to remove value");
		}
	}
	
	@Test
	public void testMapKeySet() {
		Map<Integer, Set<Integer>> amap = alignment.asMap(A), bmap = alignment.asMap(B);
		
		assertTrue(amap.keySet().contains(1));
		assertTrue(bmap.keySet().contains(3));
		
		assertFalse(amap.keySet().contains(4));
		assertFalse(amap.keySet().contains(null));
		assertFalse(bmap.keySet().contains(5));
				
		try {
			amap.keySet().add(4);
			fail("able to add key to key set");
		}
		catch(UnsupportedOperationException expected) {
			// do nothing, expected
		}
		
		try {
			bmap.keySet().add(4);
			fail("able to add key to key set");
		}
		catch(UnsupportedOperationException expected) {
			// do nothing, expected
		}
		
		try {
			amap.keySet().remove(1);
			assertFalse(amap.keySet().contains(1));
		}
		catch(UnsupportedOperationException ex) {
			fail("unable to remove key from key set");
		}
		
		try {
			bmap.keySet().remove(3);
			assertFalse(bmap.keySet().contains(3));
		}
		catch(UnsupportedOperationException ex) {
			fail("unable to remove key from key set");
		}
		
		try {
			amap.keySet().clear();
			assertTrue(amap.keySet().isEmpty());
		}
		catch(UnsupportedOperationException expected) {
			fail("unable to clear key set");
		}
		
		try {
			bmap.keySet().clear();
			assertTrue(bmap.keySet().isEmpty());
		}
		catch(UnsupportedOperationException expected) {
			fail("able to clear key set");
		}
		
		try{
			amap.keySet().add(5);
			fail("able to add to key set");
		}
		catch(UnsupportedOperationException ex) {
			// expected
		}
	}
	
	@Test
	public void testMapContains() {
		Map<Integer, Set<Integer>> amap = alignment.asMap(A), bmap = alignment.asMap(B);
		
		for(int i = 0; i < 4; i++) {
			assertTrue(amap.containsKey(i));
			assertTrue(bmap.containsKey(i));
		}
		
		assertTrue(bmap.containsKey(4));
		assertFalse(amap.containsKey(4));
		
		assertTrue(bmap.containsValue(Collections.singleton(2)));
		
		Set<Integer> s = new HashSet<Integer>(twoAVals);
		assertTrue(amap.containsValue(s));
	}
	
	@Test
	public void testMapGet() {
		Map<Integer, Set<Integer>> amap = alignment.asMap(A), bmap = alignment.asMap(B);
		
		Set<Integer> s = new HashSet<Integer>(twoAVals);
		
		assertEquals(s, amap.get(2));
		assertEquals(Collections.singleton(3), bmap.get(4));
		assertNull(amap.get(4));
		
		try {
			assertTrue(amap.get(2).contains(1));
			amap.get(2).remove(1);
			assertFalse(amap.get(2).contains(1));
		}
		catch(UnsupportedOperationException expected) {
			fail("unable remove from value set");
		}
		
		try {
			assertFalse(bmap.get(4).contains(2));
			bmap.get(4).add(2);
			assertTrue(bmap.get(4).contains(2));
		}
		catch(UnsupportedOperationException expected) {
			fail("unable add to value set");
		}
	}

	@Test
	public void testMapPut() {
		Map<Integer, Set<Integer>> amap = alignment.asMap(A), bmap = alignment.asMap(B);
		
		try {
			assertEquals(Collections.singleton(3), amap.put(1, Collections.singleton(2)));
			assertEquals(Collections.singleton(2), amap.get(1));
		}
		catch(UnsupportedOperationException expected) {
			fail("unable to put");
		}
		
		try {
			assertEquals(Collections.singleton(0), bmap.put(2, Collections.singleton(3)));
			assertEquals(Collections.singleton(3), bmap.get(2));
		}
		catch(UnsupportedOperationException expected) {
			fail("unable to put");
		}
		
		amap.remove(1);
		assertNull(amap.put(1, Collections.singleton(0)));
		
		
	}
	
	@Test
	public void testMapRemove() {
		Map<Integer, Set<Integer>> amap = alignment.asMap(A), bmap = alignment.asMap(B);
		
		try {
			assertTrue(amap.containsKey(1));
			amap.remove(1);
			assertFalse(amap.containsKey(1));
		}
		catch(UnsupportedOperationException expected) {
			fail("unable to remove");
		}
		
		try {
			assertTrue(bmap.containsKey(2));
			bmap.remove(2);
			assertFalse(bmap.containsKey(2));
		}
		catch(UnsupportedOperationException expected) {
			fail("unable to remove");
		}
	}
	
	@Test
	public void testMapAdd() {
		Map<Integer, Set<Integer>> amap = alignment.asMap(A), bmap = alignment.asMap(B);
		
		try {
			amap.get(1).add(0);
			assertTrue(amap.get(1).contains(0));
		}
		catch(UnsupportedOperationException expected) {
			fail("unable to add");
		}
		
		try {
			bmap.get(2).add(3);
			assertTrue(bmap.get(2).contains(3));
		}
		catch(UnsupportedOperationException expected) {
			fail("unable to add");
		}
	}
	
	@Test
	public void testMapClear() {
		Map<Integer, Set<Integer>> amap = alignment.asMap(A), bmap = alignment.asMap(B);
		
		try {
			amap.clear();
			assertTrue(amap.isEmpty());
		}
		catch(UnsupportedOperationException expected) {
			fail("unable to clear");
		}
		
		try {
			bmap.clear();
			assertTrue(bmap.isEmpty());
		}
		catch(UnsupportedOperationException expected) {
			fail("unable to clear");
		}
	}
	
	@Test
	public void testAsMap() {
		Map<Integer, Set<Integer>> amap = alignment.asMap(A), bmap = alignment.asMap(B);
		
		assertEquals(map, amap);
		assertEquals(pam, bmap);
		
		assertTrue(map.keySet().containsAll(amap.keySet()));
		assertTrue(pam.keySet().containsAll(bmap.keySet()));
		
		assertTrue(map.values().containsAll(amap.values()));
		assertTrue(pam.values().containsAll(bmap.values()));
		
		assertEquals(4, amap.size());
		assertEquals(5, bmap.size());
		
		assertFalse(amap.isEmpty());
		assertFalse(bmap.isEmpty());
		
		alignment.add(new Mapping(id, 2, 2));
		assertTrue(alignment.asMap(A).get(2).contains(2));
	}

	@Test
	public void testFromMap() {
		assertEquals(alignment, Alignment.fromMap(one, two, map));
		assertEquals(alignment.reverse(), Alignment.fromMap(two, one, pam));
		
		assertEquals(alignment, Alignment.fromMap(one, two, alignment.asMap(A)));
		assertEquals(alignment.reverse(), Alignment.fromMap(two, one, alignment.asMap(B)));
	}
	
	@Test
	public void testReverse() {
		for(PhrasePosition pos : PhrasePosition.values()) {
			assertEquals(alignment.get(pos), alignment.reverse().get(pos.opposite()));
		}
		
		for(Mapping r : alignment.reverse()) {
			assertTrue(alignment.contains(r.reverse()));
		}
		
		assertEquals(alignment, alignment.reverse().reverse());
	}
	
	@Test
	public void testGetIndices() {
		Set<Integer> is = new HashSet<Integer>();
		for(int i = 0; i < 4; i++) {
			is.add(i);
		}
		
		assertEquals(is, alignment.getIndices(A));
		
		is.add(4);
		
		assertEquals(is, alignment.getIndices(B));
	}
}
