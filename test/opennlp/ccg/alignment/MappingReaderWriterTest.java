package opennlp.ccg.alignment;

import static opennlp.ccg.alignment.Status.POSSIBLE;
import static opennlp.ccg.alignment.Status.SURE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static opennlp.ccg.alignment.Alignments.*;

public class MappingReaderWriterTest {

	MappingReader mosesReader, naaclReader;
	MappingWriter mosesWriter, naaclWriter;
	StringWriter mosesStringWriter, naaclStringWriter;
	MappingFormat mosesFormat = MappingFormat.getInstance(MOSES_ENCODING_SCHEME),
		naaclFormat = MappingFormat.getInstance(NAACL_ENCODING_SCHEME, Alignments.NAACL_SHORT_FIELDS, false);
	
	Set<Mapping> mosesMappings = new LinkedHashSet<Mapping>(), naaclMappings = new LinkedHashSet<Mapping>();
	Set<MappingGroup> mosesGroups = new LinkedHashSet<MappingGroup>(),
			naaclGroups = new LinkedHashSet<MappingGroup>();
	
	String lineSep = System.getProperty("line.separator");
	
	String mosesInput = "0-1-S 0-0 2-1-P 3-3 4-8 21-23\r\n3-4 34-55-P 1-4 23-1-S\n",
			mosesOutput = "0-1 0-0 2-1-P 3-3 4-8 21-23" + lineSep + "3-4 34-55-P 1-4 23-1",
			naaclInput = "17 1 1 S\r17 2 3 P\n17 5 5\r\n17 4 4 S 0.75\n37 3 2 P",
			naaclOutput = "17 1 1" + lineSep + "17 2 3 P" + lineSep + "17 5 5"
				+ lineSep + "17 4 4" + lineSep + "37 3 2 P",
			emptyInput = "",
			lineEndingOnly = "\n";
	
	@Before
	public void setUp() throws Exception {
		mosesReader = new MappingReader(new StringReader(mosesInput), mosesFormat);
		naaclReader = new MappingReader(new StringReader(naaclInput), naaclFormat);
		
		mosesStringWriter = new StringWriter();
		mosesWriter = new MappingWriter(mosesStringWriter, mosesFormat);
		naaclStringWriter = new StringWriter();
		naaclWriter = new MappingWriter(naaclStringWriter, naaclFormat);
		
		mosesMappings.add(new Mapping(0, 0, 1, SURE));
		mosesMappings.add(new Mapping(0, 0, 0));
		mosesMappings.add(new Mapping(0, 2, 1, POSSIBLE));
		mosesMappings.add(new Mapping(0, 3, 3));
		mosesMappings.add(new Mapping(0, 4, 8));
		mosesMappings.add(new Mapping(0, 21, 23));
		mosesMappings.add(new Mapping(1, 3, 4));
		mosesMappings.add(new Mapping(1, 34, 55, POSSIBLE));
		mosesMappings.add(new Mapping(1, 1, 4));
		mosesMappings.add(new Mapping(1, 23, 1, SURE));
		
		naaclMappings.add(new Mapping(17, 0, 0, SURE));
		naaclMappings.add(new Mapping(17, 1, 2, POSSIBLE));
		naaclMappings.add(new Mapping(17, 4, 4));
		naaclMappings.add(new Mapping(17, 3, 3, SURE, new Double(0.75d)));
		naaclMappings.add(new Mapping(37, 2, 1, POSSIBLE));
		
		mosesGroups.add(new MappingGroup(0, 6));
		mosesGroups.add(new MappingGroup(1, 4));
		
		naaclGroups.add(new MappingGroup(17, 4));
		naaclGroups.add(new MappingGroup(37, 1));
	}
	
	@Test
	public void testConstructors() {
		try {
			new MappingReader(new StringReader(""), null);
			fail("able to specify null format");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
		
		try {
			new MappingWriter(new StringWriter(), null);
			fail("able to specify null format");
		}
		catch(IllegalArgumentException expected) {
			// do nothing
		}
	}
	
	@Test
	public void testBoth() {
		Iterator<Mapping> mi = mosesMappings.iterator();
		Iterator<MappingGroup> gi = mosesGroups.iterator();
		
		StringWriter sw = new StringWriter();
		MappingWriter mw = new MappingWriter(sw, mosesFormat);
		
		try {
			while(gi.hasNext()) {
				mw.startGroup(gi.next());
				while(mw.canWrite() && mi.hasNext()) {
					mw.writeMapping(mi.next());
				}
			}
			
			gi = mosesGroups.iterator();
			mi = mosesMappings.iterator();
			MappingReader mr = new MappingReader(new StringReader(sw.getBuffer().toString()), mosesFormat);
			
			while(gi.hasNext()) {
				MappingGroup g = gi.next();
				assertEquals(g, mr.nextGroup());
				while(mr.canRead()) {
					assertEquals(mi.next(), mr.readMapping());
				}
			}
			
			mi = naaclMappings.iterator();
			gi = naaclGroups.iterator();
			sw = new StringWriter();
			
			mw.close();
			mw = new MappingWriter(sw, naaclFormat);
			
			while(gi.hasNext()) {
				mw.startGroup(gi.next());
				while(mw.canWrite() && mi.hasNext()) {
					mw.writeMapping(mi.next());
				}
			}
			
			mi = naaclMappings.iterator();
			gi = naaclGroups.iterator();
			
			mr.close();
			mr = new MappingReader(new StringReader(sw.getBuffer().toString()), naaclFormat);
			
			while(gi.hasNext()) {
				MappingGroup g = gi.next();
				assertEquals(g, mr.nextGroup());
				while(mr.canRead()) {
					Mapping m = mi.next();
					m.setConfidence(Alignments.DEFAULT_CONFIDENCE);
					assertEquals(m, mr.readMapping());
				}
			}
			
			mosesWriter.close();
			naaclWriter.close();
			mr.close();
			mw.close();
		}
		catch(IOException io) {
			fail(io.getMessage());
		}
	}
	
	@Test
	public void testMappingWriter() {
		Iterator<Mapping> mi = mosesMappings.iterator();
		Iterator<MappingGroup> gi = mosesGroups.iterator();
		
		try {
			while(gi.hasNext()) {
				mosesWriter.startGroup(gi.next());
				while(mosesWriter.canWrite()) {
					mosesWriter.writeMapping(mi.next());
				}
			}
			
			mosesWriter.close();
			
			assertEquals(mosesOutput, mosesStringWriter.getBuffer().toString());
			
			mi = naaclMappings.iterator();
			gi = naaclGroups.iterator();
						
			while(gi.hasNext()) {
				naaclWriter.startGroup(gi.next());
				while(naaclWriter.canWrite()) {
					naaclWriter.writeMapping(mi.next());
				}
			}
			
			naaclWriter.close();
			
			assertEquals(naaclOutput, naaclStringWriter.getBuffer().toString());
			
		}
		catch(IOException io) {
			fail(io.getMessage());
		}
		
		MappingWriter mw = new MappingWriter(new StringWriter(), naaclFormat);
		try {
			mw.writeMapping(new Mapping(1, 0));
			fail("able to write mapping without starting group");
		}
		catch(IOException expected) {
			//should happen
		}
		
		mw = new MappingWriter(new StringWriter(), mosesFormat);
		
		try {
			mw.startGroup(new MappingGroup(0, 1));
			try {
				mw.writeMapping(new Mapping(1, 1, 1));
				fail("able to write mapping from different group");
			}
			catch(IOException expected) {
				//should happen
			}
		}
		catch(IOException io) {
			fail("problem testing: " + io.getMessage());
		}
		
		mw = new MappingWriter(new StringWriter(), naaclFormat);
		
		try {
			mw.startGroup(new MappingGroup(0, 1));
			mw.writeMapping(new Mapping(0, 1, 1));
			try {
				mw.writeMapping(new Mapping(0, 1, 2));
				fail("able to write too many mappings");
			}
			catch(IOException expected) {
				//should happen
			}
		}
		catch(IOException io) {
			fail("problem testing: " + io.getMessage());
		}
		
		mw = new MappingWriter(new StringWriter(), mosesFormat);
		
		try {
			mw.startGroup(new MappingGroup(0, 2));
			mw.writeMapping(new Mapping(0, 1, 1));
			try {
				mw.close();
				fail("able to write too few mappings");
			}
			catch(IOException expected) {
				//should happen
			}
		}
		catch(IOException io) {
			fail("problem testing: " + io.getMessage());
		}
		
		StringWriter sw = new StringWriter();		
		mw = new MappingWriter(sw, naaclFormat);
		
		try {
			mw.startGroup(new MappingGroup(0, 0));
			mw.endGroup();
			mw.close();
			assertEquals("", sw.getBuffer().toString());
		}
		catch(IOException io) {
			fail("problem testing: " + io.getMessage());
		}
	}

	@Test
	public void testMappingReader() {
		Iterator<Mapping> mi = mosesMappings.iterator();
		Iterator<MappingGroup> gi = mosesGroups.iterator();
		
		try {
			MappingGroup g;
			while((g = mosesReader.nextGroup()) != null) {
				assertEquals(gi.next(), g);
				while(mosesReader.canRead()) {
					assertEquals(mi.next(), mosesReader.readMapping());
				}
			}
			
			mosesReader.close();
			
			mi = naaclMappings.iterator();
			gi = naaclGroups.iterator();
			
			while((g = naaclReader.nextGroup()) != null) {
				assertEquals(gi.next(), g);
				while(naaclReader.canRead()) {
					assertEquals(mi.next(), naaclReader.readMapping());
				}
			}
			
			naaclReader.close();
			
			mosesReader = new MappingReader(new StringReader(emptyInput), mosesFormat);
			naaclReader = new MappingReader(new StringReader(emptyInput), naaclFormat);
			
			try {
				assertFalse(mosesReader.ready());
				assertFalse(mosesReader.canRead());
				assertNull(mosesReader.nextGroup());
			}
			catch(IOException io) {
				fail("problem testing: " + io.getMessage());
			}
			
			try {
				assertFalse(naaclReader.ready());
				assertFalse(naaclReader.canRead());
				assertNull(naaclReader.nextGroup());
			}
			catch(IOException expected) {
				// should happen
			}
			
			mosesReader = new MappingReader(new StringReader(lineEndingOnly), mosesFormat);
			naaclReader = new MappingReader(new StringReader(lineEndingOnly), naaclFormat);
			
			try {
				assertFalse(mosesReader.ready());
				assertFalse(mosesReader.canRead());
				assertNull(mosesReader.nextGroup());
			}
			catch(IOException io) {
				fail("problem testing: " + io.getMessage());
			}
			
			try {
				assertFalse(naaclReader.ready());
				assertFalse(naaclReader.canRead());
				assertNull(naaclReader.nextGroup());
			}
			catch(IOException io) {
				fail("problem testing: " + io.getMessage());
			}
			
			mosesReader = new MappingReader(new StringReader("5-4-"), mosesFormat);
			naaclReader = new MappingReader(new StringReader("0 S\n"), naaclFormat);
			
			try {
				mosesReader.nextGroup();
				fail("able to get next group from garbage input");
			}
			catch(IOException expected) {
				// should happen
			}
			
			try {
				naaclReader.nextGroup();
				fail("able to get next group from garbage input");
			}
			catch(IOException expected) {
				// should happen
			}
			
			mosesReader = new MappingReader(new StringReader(mosesInput), mosesFormat);
			naaclReader = new MappingReader(new StringReader(naaclInput), naaclFormat);
			
			try {
				mosesReader.readMapping();
				fail("able to read mapping without group");
			}
			catch(IOException expected) {
				// should happen
			}
			
			try {
				naaclReader.readMapping();
				fail("able to read mapping without group");
			}
			catch(IOException expected) {
				// should happen
			}
			
			mosesReader = new MappingReader(new StringReader(mosesInput), mosesFormat);
			naaclReader = new MappingReader(new StringReader(naaclInput), naaclFormat);
			
			MappingGroup mg = mosesReader.nextGroup();
			for(int i = 0; i < mg.length - 1; i++) {
				mosesReader.readMapping();
			}
			
			try {
				mosesReader.close();
				fail("able to read too few mappings");
			}
			catch(IOException expected) {
				// should happen
			}
			
			mg = naaclReader.nextGroup();
			for(int i = 0; i < mg.length - 1; i++) {
				naaclReader.readMapping();
			}
			
			try {
				naaclReader.close();
				fail("able to read too few mappings");
			}
			catch(IOException expected) {
				// should happen
			}
		}
		catch(IOException io) {
			fail(io.getMessage());
		}
	}

}
