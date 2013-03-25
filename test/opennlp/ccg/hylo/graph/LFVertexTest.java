package opennlp.ccg.hylo.graph;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import opennlp.ccg.hylo.Mode;
import opennlp.ccg.hylo.ModeLabel;
import opennlp.ccg.hylo.Nominal;
import opennlp.ccg.hylo.NominalAtom;
import opennlp.ccg.hylo.Proposition;

import org.junit.Before;
import org.junit.Test;

public class LFVertexTest extends LFBaseTest {

	Nominal wordNominal, nonwordNominal;
	Proposition proposition;
	
	Integer wordIndex, nonwordIndex;
	LFVertex word, nonword;
	
	Map<Mode, Proposition> attrs;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		wordNominal = new NominalAtom("w7");
		nonwordNominal = new NominalAtom("x1");
		proposition = new Proposition("prop");
		
		wordIndex = Integer.valueOf(7);
		nonwordIndex = Integer.valueOf(1);
		
		attrs = new HashMap<Mode, Proposition>();
		attrs.put(new ModeLabel("num"), new Proposition("sg"));
		attrs.put(new ModeLabel("det"), new Proposition("nil"));
		attrs.put(new ModeLabel("tense"), new Proposition("past"));
		
		word = new LFVertex(wordNominal, proposition, attrs);
		nonword = new LFVertex(nonwordNominal, proposition);
	}

	@Test
	public void testLFVertex() {
		try {
			new LFVertex((Nominal)null);
			fail("able to specify null nominal");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			new LFVertex(wordNominal, null);
		}
		catch(IllegalArgumentException expected) {
			fail("unable to specify null proposition");
		}
	}
	
	@Test
	public void testGetType() {
		assertEquals(LFVertexType.WORD, word.getType());
		assertEquals(LFVertexType.NONWORD, nonword.getType());
		
		assertNotSame(LFVertexType.WORD, nonword.getType());
		assertNotSame(LFVertexType.NONWORD, word.getType());
	}

	@Test
	public void testGetIndex() {
		assertEquals(wordIndex, word.getIndex());
		assertEquals(nonwordIndex, nonword.getIndex());
	}

	@Test
	public void testAttributeNames() {
		assertEquals(attrs.keySet(), word.attributeNames());
		assertEquals(Collections.emptySet(), nonword.attributeNames());
		
		try {
			Iterator<Mode> i = word.attributeNames().iterator();
			i.next();
			i.remove();
			fail("able to remove attribute name");
		}
		catch(UnsupportedOperationException expected) {
			// do nothing
		}
	}
	
	@Test
	public void testContainsAttribute() {
		Mode num = new ModeLabel("num");
		assertTrue(word.containsAttribute(num));
		word.removeAttribute(num);
		assertFalse(word.containsAttribute(num));
	}

	@Test
	public void testGetAttribute() {
		for(Mode m : attrs.keySet()) {
			assertEquals(attrs.get(m), word.getAttributeValue(m));
			assertNull(nonword.getAttributeValue(m));
		}
	}

	@Test
	public void testAddAttribute() {
		Mode num = new ModeLabel("num");
		Proposition prop = new Proposition("pl");
		
		assertFalse(word.addAttribute(num, new Proposition("sg")));
		assertTrue(word.addAttribute(num, prop));
		assertFalse(word.addAttribute(num, prop));
		assertTrue(word.containsAttribute(num));
		
		assertTrue(nonword.addAttribute(num, new Proposition("sg")));
		assertFalse(nonword.addAttribute(num, new Proposition("sg")));
		assertTrue(nonword.containsAttribute(num));
		assertTrue(nonword.addAttribute(num, prop));
		assertFalse(nonword.addAttribute(num, prop));
		assertTrue(nonword.containsAttribute(num));
	}
	
	@Test
	public void testSetAttribute() {
		Mode num = new ModeLabel("num");
		Proposition prop = new Proposition("pl");
		
		assertEquals(attrs.get(num), word.setAttribute(num, prop));
		assertEquals(prop, word.getAttributeValue(num));
		
		assertNull(nonword.setAttribute(num, prop));
		assertEquals(prop, nonword.getAttributeValue(num));
	}
	
	@Test
	public void testRemoveAttribute() {
		Mode num = new ModeLabel("num");
		
		assertEquals(attrs.get(num), word.removeAttribute(num));
		assertNull(nonword.removeAttribute(num));
	}

	@Test
	public void testEqualsObject() {
		LFVertex v = new LFVertex(wordNominal, proposition);
		for(Mode m : attrs.keySet()) {
			v.setAttribute(m, attrs.get(m));
		}
		
		assertEquals(v, word);
		assertNotSame(v, nonword);
	}
	
	@Test
	public void testGetAttributeMap() {
		Map<Mode,Proposition> m = word.getAttributeMap();
		assertEquals(attrs, m);
		
		try {
			Iterator<Map.Entry<Mode,Proposition>> i = m.entrySet().iterator();
			i.next();
			i.remove();
			fail("able to remove from attribute map");
		}
		catch(UnsupportedOperationException expected) {
			// do nothing
		}
		
		try {
			m.put(new ModeLabel("foo"), new Proposition("bar"));
			fail("able to put into attribute map");
		}
		catch(UnsupportedOperationException expected) {
			// do nothing
		}
	}
}
