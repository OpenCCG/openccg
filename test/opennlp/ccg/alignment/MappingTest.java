package opennlp.ccg.alignment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class MappingTest {

	Integer id, first, second;
	Mapping mapping;
	
	@Before
	public void setUp() throws Exception {
		id = new Integer(37);
		first = new Integer(4);
		second = new Integer(7);
		mapping = new Mapping(id, first, second);
	}

	@Test
	public void testMapping() {
		try {
			new Mapping(null, 3);
			fail("able to specify null index");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			new Mapping(3, null);
			fail("able to specify null index");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			new Mapping(1, 2, 3, null);
			fail("able to specify null status");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			new Mapping(1, 2, 3, Status.SURE, null);
			fail("able to specify null confidence");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}
	
	@Test
	public void testGet() {
		assertEquals(id, mapping.getPhraseNumber());
		
		assertEquals(first, mapping.getA());
		assertEquals(first, mapping.get(PhrasePosition.A));
		
		assertEquals(second, mapping.getB());
		assertEquals(second, mapping.get(PhrasePosition.B));
	}

	@Test
	public void testCompareTo() {
		Mapping m = new Mapping(id, first, second),
				n = new Mapping(id, first - 1, second + 1),
				o = new Mapping(id, first, second - 1),
				p = new Mapping(id, first + 1, second);
		
		assertEquals(0, mapping.compareTo(m));
		assertEquals(0, m.compareTo(mapping));
		
		assertEquals(1, mapping.compareTo(n));
		assertEquals(-1, n.compareTo(mapping));
		
		assertEquals(1, mapping.compareTo(o));
		assertEquals(-1, o.compareTo(mapping));
		
		assertEquals(-1, mapping.compareTo(p));
		assertEquals(1, p.compareTo(mapping));
	}

	@Test
	public void testEqualsObject() {
		Mapping m = new Mapping(id, first, second),
				n = new Mapping(id, first - 1, second + 1),
				o = new Mapping(id, first, second - 1),
				p = new Mapping(id, first + 1, second);
		
		assertEquals(mapping, m);
		assertNotSame(mapping, n);
		assertNotSame(mapping, o);
		assertNotSame(mapping, p);
	}
	
	@Test
	public void testReverse() {
		assertEquals(new Mapping(id, second, first), mapping.reverse());
	}

	@Test
	public void testSet() {
		assertEquals(Alignments.DEFAULT_STATUS, mapping.getStatus());
		mapping.setStatus(Status.POSSIBLE);
		assertEquals(Status.POSSIBLE, mapping.getStatus());
		
		assertEquals(Alignments.DEFAULT_CONFIDENCE, mapping.getConfidence());
		mapping.setConfidence(0.5);
		assertEquals(Double.valueOf(0.5d), mapping.getConfidence());
	}
	
	@Test
	public void testHashCode() {
		int hash = mapping.hashCode();
		
		mapping.setStatus(Status.POSSIBLE);
		assertEquals(hash, mapping.hashCode());
		mapping.setConfidence(0.5);
		assertEquals(hash, mapping.hashCode());
	}
}
