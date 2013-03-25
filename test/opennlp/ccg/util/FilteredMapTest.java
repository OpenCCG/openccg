package opennlp.ccg.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

public class FilteredMapTest {

	Map<Integer,String> map;
	FilteredMap<Integer, String> filteredMap;
	Filter<Integer> keyFilter;
	
	Integer target = 37;
	
	@Before
	public void setUp() throws Exception {
		map = new HashMap<Integer, String>();
		map.put(17, "seventeen");
		map.put(31, "thirty-one");
		map.put(37, "thirty-seven");
		map.put(43, "forty-three");
		
		keyFilter = new Filter<Integer>() {
			
			@Override
			public boolean allows(Integer e) {
				return target >= e;
			}
		};
		
		filteredMap = new FilteredMap<Integer, String>(map, keyFilter);
	}

	@Test
	public void testFilteredMap() {
		try {
			new FilteredMap<Integer, String>(map, null);
			fail("able to specify null key filter");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}
	
	@Test
	public void testGetOriginalMap() {
		assertEquals(map, filteredMap.getOriginalMap());
	}

	@Test
	public void testGetKeyFilter() {
		assertEquals(keyFilter, filteredMap.getKeyFilter());
	}

	@Test
	public void testEntrySet() {
		for(Entry<Integer, String> e : filteredMap.entrySet()) {
			if(e.getKey() > target) {
				fail("filtered map contains bad key");
			}
		}
	}

	@Test
	public void testPut() {
		assertNull(filteredMap.put(47, "blah"));
		assertNull(filteredMap.put(29, "twenty-nine"));
		
		assertFalse(filteredMap.containsKey(47));
		assertTrue(filteredMap.containsKey(29));
		
		assertEquals("twenty-nine", filteredMap.put(29, "blah"));
		assertEquals("blah", filteredMap.get(29));
	}

	@Test
	public void testContainsValue() {
		assertTrue(filteredMap.containsValue("seventeen"));
		assertFalse(filteredMap.containsValue("forty-three"));
	}

	@Test
	public void testContainsKey() {
		assertTrue(filteredMap.containsKey(31));
		assertFalse(filteredMap.containsKey(43));
	}

	@Test
	public void testKeySet() {
		for(Integer k : filteredMap.keySet()) {
			if(k.equals(43)) {
				fail("filtered map contains bad key");
			}
		}
	}

	@Test
	public void testValues() {
		assertTrue(filteredMap.values().contains("seventeen"));
		assertFalse(filteredMap.values().contains("forty-three"));
		
		filteredMap.remove(17);
		assertFalse(filteredMap.values().contains("seventeen"));
	}

}
