package opennlp.ccg.alignment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class PhraseReaderWriterTest {

	String lineSep = System.getProperty("line.separator");
	
	String input = "Phrase one.\nPhrase two\r\nPhrase three .",
			output = "Phrase one." + lineSep + "Phrase two" + lineSep + "Phrase three ." + lineSep;
	
	PhraseReader reader;
	PhraseWriter writer;
	List<Phrase> phrases;
	
	@Before
	public void setUp() throws Exception {
		phrases = new ArrayList<Phrase>();
		phrases.add(new Phrase(0, Alignments.tokenize("Phrase one.")));
		phrases.add(new Phrase(1, Alignments.tokenize("Phrase two")));
		phrases.add(new Phrase(2, Alignments.tokenize("Phrase three .")));
	}

	@Test
	public void testConstructors() {
		try {
			new PhraseReader(new StringReader(""), null);
			fail("able to specify null number base");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			new PhraseWriter(new StringWriter(), null);
			fail("able to specify null number base");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}
	
	@Test
	public void testBoth() {
		StringWriter sw = new StringWriter();
		writer = new PhraseWriter(sw);
		
		try {
			for(Phrase p : phrases) {
				writer.writePhrase(p);
			}
		}
		catch(IOException io) {
			fail(io.getMessage());
		}
		
		reader = new PhraseReader(new StringReader(sw.getBuffer().toString()));
		Iterator<Phrase> i = phrases.iterator();
		
		try {
			Phrase p;
			while((p = reader.readPhrase()) != null) {
				assertEquals(i.next(), p);
			}
		}
		catch(IOException io) {
			fail(io.getMessage());
		}
	}
	
	@Test
	public void testReadPhrase() {
		reader = new PhraseReader(new StringReader(input));
		
		try {
			Iterator<Phrase> i = phrases.iterator();
			Phrase p;
			while((p = reader.readPhrase()) != null) {
				assertEquals(i.next(), p);
			}
			
			reader = new PhraseReader(new StringReader(""), reader.getNumberBase());
			assertNull(reader.readPhrase());
		}
		catch(IOException io) {
			fail(io.getMessage());
		}
	}
	
	@Test
	public void testWritePhrase() {
		StringWriter sw = new StringWriter();
		writer = new PhraseWriter(sw);
		
		try {
			for(Phrase p : phrases) {
				writer.writePhrase(p);
			}
			
			assertEquals(output, sw.getBuffer().toString());
		}
		catch(IOException io) {
			fail(io.getMessage());
		}
	}

}
