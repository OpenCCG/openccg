package opennlp.ccg.alignment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class MappingGroupTest {

	MappingGroup one, two;
	
	@Before
	public void setUp() throws Exception {
		one = new MappingGroup(37, 12);
		two = new MappingGroup(1, 8);
	}
	
	@Test
	public void testMappingGroup() {
		try {
			new MappingGroup(null, 1);
			fail("able to specify null number");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			new MappingGroup(37, -1);
			fail("able to specify negative length");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}

	@Test
	public void testEqualsObject() {
		assertNotSame(one, two);
		assertNotSame(two, null);
		assertEquals(one, new MappingGroup(one.phraseNumber, one.length));
	}

	@Test
	public void testCompareTo() {
		assertEquals(0, one.compareTo(one));
		assertEquals(1, one.compareTo(two));
	}

}
