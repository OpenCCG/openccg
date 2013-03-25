package opennlp.ccg.alignment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class PhraseTest {

	Phrase phrase;
	
	String[] wordList = Alignments.tokenize("This is a test .");
	
	
	@Before
	public void setUp() throws Exception {
		phrase = new Phrase(37, wordList);
	}

	@Test
	public void testCompare() {
		Phrase o = new Phrase(43, phrase), t = new Phrase(43, phrase);
		
		assertEquals(-1, phrase.compareTo(o));
		assertEquals(1, t.compareTo(phrase));
		assertEquals(0, o.compareTo(t));
		assertEquals(0, phrase.compareTo(phrase));
	}
	
	@Test
	public void testSize() {
		assertEquals(5, phrase.size());
	}

	@Test
	public void testPhraseComparableOfIListOfString() {
		assertEquals(phrase, new Phrase(phrase.getNumber(), wordList));
		
		String[] str = null;
		
		try {
			new Phrase(phrase.getNumber(), str);
			fail("able to create phrase with null word list");
		}
		catch(IllegalArgumentException ex) {
			// expected
		}
		
		str = new String[]{"blah", null, "blah"};
		
		try {
			new Phrase(phrase.getNumber(), str);
			fail("able to create phrase with null word in list");
		}
		catch(IllegalArgumentException ex) {
			// expected
		}
	}

	@Test
	public void testGetNumber() {
		assertEquals(new Integer(37), phrase.getNumber());
	}

	@Test
	public void testGetInt() {
		assertEquals("is", phrase.get(1));
		assertEquals("a", phrase.get(2));
		assertEquals(".", phrase.get(4));
		
		try {
			phrase.get(phrase.size());
			fail("able to access word in phrase after end");
		}
		catch(IndexOutOfBoundsException expected) {
			// do nothing
		}
	}

	@Test
	public void testSetIntString() {
		try {
			phrase.set(2, "sdfskjdlkjflksjdlkj");
			fail("able to set");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
	}
	
	@Test
	public void testAdd() {
		try {
			phrase.add("blah");
			fail("able to add");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
	}
	
	public void testRemove() {
		try {
			phrase.remove("is");
			fail("able to remove");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
	}
	
	public void testIteratorRemove() {
		try {
			Iterator<String> i = phrase.iterator();
			i.next();
			i.remove();
			fail("able to remove via iterator");
		}
		catch(UnsupportedOperationException expected) {
			// noop
		}
	}

	@Test
	public void testEqualsObject() {
		List<String> l = new ArrayList<String>();
		
		for(int i = 0; i < 3; i++) {
			l.add("blah");
		}
		
		Phrase same = new Phrase(phrase.getNumber(), phrase), diff = new Phrase(17, l);
		
		assertEquals(phrase, same);
		assertNotSame(phrase, diff);
		
		if(!phrase.equals(same)) {
			fail("not equal");
		}
		if(phrase.equals(diff)) {
			fail("equal");
		}
	}

}
