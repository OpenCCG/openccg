package opennlp.ccg.alignment;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import opennlp.ccg.alignment.MappingFormat.Field;

import org.junit.Before;
import org.junit.Test;

import static opennlp.ccg.alignment.Alignments.*;

public class MappingFormatTest {

	Set<MappingFormat> formats;
	Mapping vanilla, chocolate, nullId, nullValue;
	
	@Before
	public void setUp() throws Exception {
		MappingFormat moses = MappingFormat.getInstance(MOSES_ENCODING_SCHEME),
			mosesShort = MappingFormat.getInstance(MOSES_ENCODING_SCHEME,
				Alignments.MOSES_SHORT_FIELDS),
			mosesShortStrict = MappingFormat.getInstance(MOSES_ENCODING_SCHEME,
				Alignments.MOSES_SHORT_FIELDS, true),
			naacl = MappingFormat.getInstance(NAACL_ENCODING_SCHEME),
			naaclShort = MappingFormat.getInstance(NAACL_ENCODING_SCHEME, Alignments.NAACL_SHORT_FIELDS),
			naaclShortStrict = MappingFormat.getInstance(NAACL_ENCODING_SCHEME, Alignments.NAACL_SHORT_FIELDS,
					true),
			naaclVeryShort = MappingFormat.getInstance(NAACL_ENCODING_SCHEME, Alignments.NAACL_VERY_SHORT_FIELDS);
		
		formats = new HashSet<MappingFormat>();
		formats.add(moses);
		formats.add(mosesShort);
		formats.add(mosesShortStrict);
		formats.add(naacl);
		formats.add(naaclShort);
		formats.add(naaclShortStrict);
		formats.add(naaclVeryShort);
		
		vanilla = new Mapping(31, 4, 9);
		chocolate = new Mapping(31, 13, 5, Status.POSSIBLE, 0.75);
		nullId = new Mapping(null, 2, 2);
		nullValue = new Mapping(17, -1, 5);
	}
	
	@Test
	public void testMappingFormat() {
		Set<Field> fields = new HashSet<Field>();
		
		fields.add(Field.PHRASE_NUMBER_FIELD);
		
		try {
			MappingFormat.getInstance(MOSES_ENCODING_SCHEME, fields, true);
			fail("able to create Moses formatter with ID field");
		}
		catch(IllegalArgumentException expected) {
			// should happen
		}
		
		try {
			MappingFormat.getInstance(NAACL_ENCODING_SCHEME, fields, false);
			fail("able to create NAACL formatter with only ID field");
		}
		catch(IllegalArgumentException expected) {
			// should happen
		}
		
		try {
			MappingFormat.getInstance(MOSES_ENCODING_SCHEME, null, true);
			fail("able to create Moses formatter with null fields");
		}
		catch(IllegalArgumentException expected) {
			// should happen
		}
		
		try {
			MappingFormat.getInstance(NAACL_ENCODING_SCHEME, null, false);
			fail("able to create NAACL formatter with null fields");
		}
		catch(IllegalArgumentException expected) {
			// should happen
		}
		
		try {
			MappingFormat.getInstance(null, fields, true);
			fail("able to create formatter null scheme");
		}
		catch(IllegalArgumentException expected) {
			// should happen
		}
	}

	@Test
	public void testFormatMapping() {
		for(MappingFormat mf : formats) {
			String v = mf.format(vanilla), c = mf.format(chocolate), ni = null;
			EncodingScheme es = mf.encodingScheme;
			
			try {
				ni = mf.formatMapping(nullId);
			}
			catch(IllegalArgumentException e) {
				if(!es.getRequired().contains(MappingFormat.Field.PHRASE_NUMBER_FIELD)) {
					fail("unexpected exception: " + e.getMessage());
				}
			}
			
			try {
				mf.format(nullValue);
				fail("able to format mapping with null index");
			}
			catch(IllegalArgumentException expected) {
				// should happen
			}
				
			if(es.equals(MOSES_ENCODING_SCHEME)) {
				if(mf.fields.contains(MappingFormat.Field.STATUS_FIELD)) {
					assertEquals("13-5-P", c);
					if(mf.isStrict()) {
						assertEquals("4-9-S", v);
						assertEquals("2-2-S", ni);
					}
					else {
						assertEquals("4-9", v);
						assertEquals("2-2", ni);
					}
				}
				else {
					assertEquals("4-9", v);
					assertEquals("13-5", c);
					assertEquals("2-2", ni);
				}
			}
			else if(es.equals(NAACL_ENCODING_SCHEME)) {
				try {
					ni = mf.formatMapping(nullId);
					fail("able to format mapping with null id");
				}
				catch(IllegalArgumentException expected) {
					// should happen
				}
				
				if(mf.fields.contains(MappingFormat.Field.STATUS_FIELD)) {
					if(mf.fields.contains(MappingFormat.Field.CONFIDENCE_FIELD)) {
						if(mf.isStrict()) {
							assertEquals("31 5 10 S 1.0", v);
							assertEquals("31 14 6 P 0.75", c);
						}
						else {
							assertEquals("31 5 10", v);
							assertEquals("31 14 6 P 0.75", c);
						}
					}
					else {
						assertEquals("31 14 6 P", c);						
						
						if(mf.isStrict()) {
							assertEquals("31 5 10 S", v);
						}
						else {
							assertEquals("31 5 10", v);
						}
					}
				}
				else {
					assertEquals("31 5 10", v);
					assertEquals("31 14 6", c);
				}
			}
		}
	}

	@Test
	public void testParseMapping() {
		for(MappingFormat mf : formats) {
			EncodingScheme es = mf.encodingScheme;
			Mapping v, c, ni;
			
			if(es.equals(MOSES_ENCODING_SCHEME)) {
				try {
					mf.parseMapping("-1-5");
					fail("able to parse mapping with negative index");
				}
				catch(ParseException expected) {
					// should
				}
				
				if(mf.fields.contains(MappingFormat.Field.STATUS_FIELD)) {
					try {
						v = mf.parseMapping("4-9-S").copyWithPhraseNumber(chocolate.phraseNumber);
						
						assertEquals(vanilla, v);
						
						c = mf.parseMapping("13-5-P").copyWithPhraseNumber(chocolate.phraseNumber);
						c.setConfidence(chocolate.confidence);
						assertEquals(chocolate, c);
						
						ni = mf.parseMapping("2-2-S");
						assertEquals(nullId, ni);
						
						if(mf.isStrict()) {
							try {
								mf.parseMapping("4-9");
								fail("strict format able to parse loose input");
							}
							catch(ParseException expected) {
								assertEquals(3, expected.getErrorOffset());
							}
						}
						else {
							v = mf.parseMapping("4-9").copyWithPhraseNumber(chocolate.phraseNumber);
							assertEquals(vanilla, v);
							
							ni = mf.parseMapping("2-2");
							assertEquals(nullId, ni);
						}
					}
					catch(ParseException p) {
						fail("parse exception: " + p.getMessage());
					}					
				}
				else {
					try {
						v = mf.parseMapping("4-9").copyWithPhraseNumber(chocolate.phraseNumber);
						assertEquals(vanilla, v);
						
						c = mf.parseMapping("13-5").copyWithPhraseNumber(chocolate.phraseNumber);
						c.setStatus(Status.POSSIBLE);
						c.setConfidence(chocolate.confidence);
						assertEquals(chocolate, c);
						
						ni = mf.parseMapping("2-2");
						assertEquals(nullId, ni);
					}
					catch(ParseException p) {
						fail("parse exception: " + p.getMessage());
					}
				}
			}
			else if(es.equals(NAACL_ENCODING_SCHEME)) {
				try {
					mf.parseMapping("31 0 6 S 1.0");
					fail("able to parse mapping with 0 index, but index base is 1");
				}
				catch(ParseException expected) {
					// should
				}
				
				if(mf.fields.contains(MappingFormat.Field.STATUS_FIELD)) {
					if(mf.fields.contains(MappingFormat.Field.CONFIDENCE_FIELD)) {
						try {
							if(mf.isStrict()) {
								v = mf.parseMapping("31 5 10 S 1.0");
								assertEquals(vanilla, v);
								
								try {
									mf.parseMapping("31 5 10");
									fail("able to parse loose input with strict format");
								}
								catch(ParseException expected) {
									assertEquals(7, expected.getErrorOffset());
								}
							}
							else {
								v = mf.parseMapping("31 5 10");
								assertEquals(vanilla, v);
							}
							
							c = mf.parseMapping("31 14 6 P 0.75");
							assertEquals(chocolate, c);
							
							try {
								ni = mf.parseMapping("3 3 S");
							}
							catch(ParseException should) {
								// expected
								assertEquals(4, should.getErrorOffset());
							}
						}
						catch(ParseException p) {
							fail("parse exception: " + p.getMessage());
						}
					}
					else {
						try {
							if(mf.isStrict()) {
								v = mf.parseMapping("31 5 10 S");
								assertEquals(vanilla, v);
								
								try {
									mf.parseMapping("31 5 10");
									fail("able to parse loose input with strict format");
								}
								catch(ParseException expected) {
									assertEquals(7, expected.getErrorOffset());
								}
							}
							else {
								v = mf.parseMapping("31 5 10");
								assertEquals(vanilla, v);
							}
							
							c = mf.parseMapping("31 14 6 P");
							c.setConfidence(chocolate.confidence);
							assertEquals(chocolate, c);
							
							try {
								ni = mf.parseMapping("3 3 S");
							}
							catch(ParseException should) {
								// expected
								assertEquals(4, should.getErrorOffset());
							}
						}
						catch(ParseException p) {
							fail("parse exception: " + p.getMessage());
						}
					}
				}
				else {
					try {
						v = mf.parseMapping("31 5 10");
						assertEquals(vanilla, v);
						
						c = mf.parseMapping("31 14 6");
						c.setStatus(Status.POSSIBLE);
						c.setConfidence(chocolate.confidence);
						assertEquals(chocolate, c);
						
						try {
							ni = mf.parseMapping("3 3 S");
							fail("able to parse mapping without ID");
						}
						catch(ParseException should) {
							// expected
							assertEquals(4, should.getErrorOffset());
						}
					}
					catch(ParseException p) {
						fail("parse exception: " + p.getMessage());
					}
				}
			}			
		}
	}

}
