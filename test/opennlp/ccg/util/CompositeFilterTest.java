package opennlp.ccg.util;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class CompositeFilterTest {

	VisitedFilter<Integer> visited;
	Integer target = 37;
	Filter<Integer> lessThanFilter, greaterThanFilter;
	CompositeFilter<Integer> bothFilter, equalToFilter;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		visited = new VisitedFilter<Integer>();
		
		lessThanFilter = new Filter<Integer>() {
			@Override
			public boolean allows(Integer e) {
				return e < target;
			}
		};
		
		greaterThanFilter = new Filter<Integer>() {
			@Override
			public boolean allows(Integer e) {
				return e > target;
			}
		};
		
		Set<Filter<Integer>> s = new HashSet<Filter<Integer>>();
		s.add(lessThanFilter);
		s.add(greaterThanFilter);
		
		bothFilter = new CompositeFilter<Integer>(s);
		equalToFilter = new CompositeFilter<Integer>(new InverseFilter<Integer>(bothFilter));
	}

	@Test
	public void testContainsFilter() {
		assertTrue(bothFilter.containsFilter(lessThanFilter));
		assertTrue(bothFilter.containsFilter(greaterThanFilter));
		assertFalse(bothFilter.containsFilter(bothFilter));
	}

	@Test
	public void testAddFilter() {
		Filter<Integer> f = new VisitedFilter<Integer>();
		equalToFilter.addFilter(f);
		
		assertTrue(equalToFilter.allows(37));
		assertFalse(equalToFilter.allows(37));
		
		try {
			equalToFilter.addFilter(null);
			fail("able to add null filter");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}

	@Test
	public void testRemoveFilter() {
		Filter<Integer> f = new VisitedFilter<Integer>();
		equalToFilter.addFilter(f);
		assertTrue(equalToFilter.allows(37));
		
		equalToFilter.removeFilter(f);
		
		assertTrue(equalToFilter.allows(37));
		assertTrue(equalToFilter.allows(37));
	}

	@Test
	public void testAllows() {
		assertTrue(lessThanFilter.allows(17));
		assertFalse(lessThanFilter.allows(38));
		
		assertFalse(greaterThanFilter.allows(17));
		assertTrue(greaterThanFilter.allows(38));
		
		assertFalse(bothFilter.allows(37));
		assertTrue(equalToFilter.allows(37));
	}

}
