package opennlp.ccg.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class VisitedFilterTest {
	
	VisitedFilter<Integer> filter;

	@Before
	public void setUp() throws Exception {
		filter = new VisitedFilter<Integer>();
	}

	@Test
	public void testAllows() {
		assertTrue(filter.allows(1));
		assertFalse(filter.allows(1));
		assertTrue(filter.allows(0));
	}

	@Test
	public void testHasVisited() {
		assertTrue(filter.allows(1));
		assertTrue(filter.hasVisited(1));
		assertFalse(filter.hasVisited(13));
	}

}
