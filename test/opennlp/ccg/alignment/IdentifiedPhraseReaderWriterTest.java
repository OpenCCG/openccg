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


public class IdentifiedPhraseReaderWriterTest {

	String lineSep = System.getProperty("line.separator");
	
	String input = "<s snum=\"157\'>First phrase.</s>\n<s snum=\'387b\'> Second phrase</s>\r\n<s snum=\"55\"> Third phrase . </s>",
		output = "<s snum=\"157\">First phrase.</s>" + lineSep + "<s snum=\"387b\">Second phrase</s>"
			+ lineSep + "<s snum=\"55\">Third phrase .</s>" + lineSep,
		paddedOutput = "<s snum=\"157\"> First phrase. </s>" + lineSep + "<s snum=\"387b\"> Second phrase </s>"
				+ lineSep + "<s snum=\"55\"> Third phrase . </s>" + lineSep;
	
	List<Phrase> phrases;
	
	@Before
	public void setUp() throws Exception {
		phrases = new ArrayList<Phrase>();
		phrases.add(new Phrase("157", 0, Alignments.tokenize("First phrase.")));
		phrases.add(new Phrase("387b", 1, Alignments.tokenize("Second phrase")));
		phrases.add(new Phrase("55", 2, Alignments.tokenize("Third phrase .")));
	}
	
	@Test
	public void testIdentifiedPhraseReader() {
		try {
			new IdentifiedPhraseReader(new StringReader(""), null);
			fail("able to specify null number base");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			new IdentifiedPhraseReader(new StringReader(""), IndexBase.ZERO, null, "");
			fail("able to specify null string");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			new IdentifiedPhraseReader(new StringReader(""), IndexBase.ZERO, "", null);
			fail("able to specify null string");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}
	
	@Test
	public void testBoth() {
		StringWriter sw = new StringWriter();
		IdentifiedPhraseWriter writer = new IdentifiedPhraseWriter(sw);
		
		try {
			for(Phrase p : phrases) {
				writer.writePhrase(p);
			}
		}
		catch(IOException io) {
			fail(io.getMessage());
		}
		
		IdentifiedPhraseReader reader = new IdentifiedPhraseReader(new StringReader(sw.getBuffer().toString()));
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
		IdentifiedPhraseReader reader = new IdentifiedPhraseReader(new StringReader(input));
		
		try {
			Iterator<Phrase> i = phrases.iterator();
			Phrase p;
			while((p = reader.readPhrase()) != null) {
				assertEquals(i.next(), p);
			}
			
			reader = new IdentifiedPhraseReader(new StringReader(""));
			assertNull(reader.readPhrase());
			
			reader.close();
		}
		catch(IOException io) {
			fail(io.getMessage());
		}
	}

	@Test
	public void testWritePhrase() {
		StringWriter sw = new StringWriter();
		IdentifiedPhraseWriter writer = new IdentifiedPhraseWriter(sw);
		
		try {
			for(Phrase p : phrases) {
				writer.writePhrase(p);
			}
			
			assertEquals(output, sw.getBuffer().toString());
			
			writer.close();
		}
		catch(IOException io) {
			fail(io.getMessage());
		}
		
		// test padded version
		sw = new StringWriter();
		writer = new IdentifiedPhraseWriter(sw, writer.getWordSeparator(), writer.getPhraseTag(),
				writer.getPhraseIdentifierAttribute(), true);
		
		try {
			for(Phrase p : phrases) {
				writer.writePhrase(p);
			}
			
			assertEquals(paddedOutput, sw.getBuffer().toString());
			
			writer.close();
		}
		catch(IOException io) {
			fail(io.getMessage());
		}
	}
}
