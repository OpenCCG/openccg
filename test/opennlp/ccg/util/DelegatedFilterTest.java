package opennlp.ccg.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DelegatedFilterTest {

	Filter<Integer> lengthFilter;
	DelegatedFilter<String, Integer> stringFilter;
	
	@Before
	public void setUp() throws Exception {
		lengthFilter = new Filter<Integer>() {
			@Override
			public boolean allows(Integer i) {
				return i <= 5;
			}
		};
		
		stringFilter = new DelegatedFilter<String, Integer>(lengthFilter) {
			@Override
			public Integer delegateValueFor(String e) {
				return e.length();
			}
		};
	}

	@Test
	public void testAllows() {
		assertTrue(stringFilter.allows("Scott"));
		assertTrue(stringFilter.allows("Mike"));
		assertTrue(stringFilter.allows("Jason"));
		assertFalse(stringFilter.allows("Dominic"));
		assertFalse(stringFilter.allows("Dennis"));
	}

	@Test
	public void testDelegateValueFor() {
		assertEquals(Integer.valueOf(5), stringFilter.delegateValueFor("Scott"));
	}

}
