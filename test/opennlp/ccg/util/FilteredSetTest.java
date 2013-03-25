package opennlp.ccg.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class FilteredSetTest {

	FilteredSet<String> testSet, sameSet;
	List<String> values;
	
	Filter<String> testFilter, sameFilter;
	
	@Before
	public void setUp() throws Exception {
		values = new ArrayList<String>(Arrays.asList("test", "test", "other", "different"));
		
		testFilter = new Filter<String>() {
			
			@Override
			public boolean allows(String e) {
				return e.equals("test");
			}
		};
		
		sameFilter = new VisitedFilter<String>();
		
		testSet = new FilteredSet<String>(values, testFilter);
		sameSet = new FilteredSet<String>(values, sameFilter);
	}

	@Test
	public void testFilteredSet() {
		try {
			new FilteredSet<String>(testSet, null);
			fail("able to specify null filter");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}
	
	@Test
	public void testSize() {
		assertEquals(1, testSet.size());
		assertEquals(values.size() - 1, sameSet.size());
	}

	@Test
	public void testAdd() {
		int sz = testSet.size();
		assertFalse(testSet.add("test"));
		assertTrue(testSet.remove("test"));
		assertEquals(sz - 1, testSet.size());
		
		assertFalse(sameSet.add("test"));
		assertTrue(sameSet.add("blah"));
		assertTrue(sameSet.add("xyxyx"));
		assertFalse(sameSet.add("xyxyx"));
		assertEquals(5, sameSet.size());
	}

	@Test
	public void testIterator() {
		Iterator<String> i = testSet.iterator();
		assertTrue(i.hasNext());
		assertEquals("test", i.next());
		assertFalse(i.hasNext());
		
		i = sameSet.iterator();
		assertTrue(i.hasNext());
		assertEquals("test", i.next());
		assertEquals("other", i.next());
		assertEquals("different", i.next());
		
		i = sameSet.iterator();
		assertEquals("test", i.next());
		i.remove();
		assertFalse(sameSet.contains("test"));
	}

	@Test
	public void testRemove() {
		testSet.remove("test");
		assertFalse(testSet.contains("test"));
		
		sameSet.remove("test");
		assertFalse(sameSet.contains("test"));
	}

	@Test
	public void testClear() {
		testSet.clear();
		assertEquals(0, testSet.size());
		sameSet.clear();
		assertEquals(0, sameSet.size());
	}
	
	@Test
	public void testGetOriginalCollection() {
		assertEquals(values, testSet.getOriginalCollection());
		assertEquals(values, sameSet.getOriginalCollection());
	}
}
