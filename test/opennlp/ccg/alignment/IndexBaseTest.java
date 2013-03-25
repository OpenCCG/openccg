package opennlp.ccg.alignment;

import static opennlp.ccg.alignment.IndexBase.ONE;
import static opennlp.ccg.alignment.IndexBase.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class IndexBaseTest {

	Integer zero = new Integer(0), one = new Integer(1),
			two = new Integer(2), negOne = new Integer(-1),
			negTwo = new Integer(-2);
	
	@Test
	public void testGetStart() {
		assertEquals(zero, ZERO.start);
		assertEquals(one, ONE.start);
	}

	@Test
	public void testGetNullValue() {
		assertEquals(negOne, ZERO.nullValue);
		assertEquals(zero, ONE.nullValue);
	}

	@Test
	public void testIsValidIndex() {
		assertTrue(ZERO.isValidIndex(zero));
		assertTrue(ZERO.isValidIndex(one));
		assertTrue(ZERO.isValidIndex(negOne));
		assertFalse(ZERO.isValidIndex(negTwo));
		
		assertTrue(ONE.isValidIndex(zero));
		assertTrue(ONE.isValidIndex(one));
		assertFalse(ONE.isValidIndex(negOne));
		assertFalse(ONE.isValidIndex(negTwo));
		
		assertTrue(ZERO.isValidIndex(two));
		assertTrue(ONE.isValidIndex(two));
	}

	@Test
	public void testTranslate() {
		try {
			ZERO.translate(negTwo, ONE);
			fail("ZERO able to translate " + negTwo);
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			ONE.translate(negOne, ZERO);
			fail("ONE able to translate " + negOne);
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
				
		// identity tests
		
		assertEquals(one, ZERO.translate(one, ZERO));
		assertEquals(one, ONE.translate(one, ONE));
		
		assertEquals(zero, ZERO.translate(zero, ZERO));
		assertEquals(zero, ONE.translate(zero, ONE));
		
		// actual translations
		
		assertEquals(zero, ZERO.translate(negOne, ONE));
		assertEquals(one, ZERO.translate(zero, ONE));
		assertEquals(two, ZERO.translate(one, ONE));
		
		assertEquals(negOne, ONE.translate(zero, ZERO));
		assertEquals(zero, ONE.translate(one, ZERO));
		assertEquals(one, ONE.translate(two, ZERO));
	}

}
