package opennlp.ccg.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class InverseFilterTest {

	VisitedFilter<Integer> visited;
	InverseFilter<Integer> inverse;
	
	@Before
	public void setUp() throws Exception {
		visited = new VisitedFilter<Integer>();
		inverse = new InverseFilter<Integer>(visited);
	}

	@Test
	public void testInverseFilter() {
		try {
			new InverseFilter<Integer>(null);
			fail("able to specify null filter");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}
	
	@Test
	public void testGetOriginalFilter() {
		assertEquals(visited, inverse.getOriginalFilter());
	}

	@Test
	public void testAllows() {
		assertTrue(visited.allows(37));
		assertFalse(visited.allows(37));
		
		assertTrue(inverse.allows(37));
		assertFalse(inverse.allows(17));
		assertFalse(visited.allows(17));
		assertTrue(inverse.allows(17));
	}

}
