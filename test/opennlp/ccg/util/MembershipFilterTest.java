package opennlp.ccg.util;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class MembershipFilterTest {

	Filter<String> filter;
	Set<String> strings;
	
	@Before
	public void setUp() throws Exception {
		strings = new HashSet<String>();
		strings.add("test");
		strings.add("one");
		strings.add("two");
		strings.add("three");
		
		filter = new MembershipFilter<String>(strings);
	}

	@Test
	public void testMembershipFilter() {
		try {
			new MembershipFilter<String>(null);
			fail("able to specify null members");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}
	
	@Test
	public void testAllows() {
		for(String s : strings) {
			assertTrue(filter.allows(s));
		}
		
		assertFalse(filter.allows("blah"));
		assertFalse(filter.allows(""));
		assertFalse(filter.allows(null));
	}

}
