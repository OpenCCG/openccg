package opennlp.ccg.hylo.graph;

import java.io.File;

import opennlp.ccg.grammar.Grammar;

import org.junit.Before;
import org.junit.Test;

public class LFBaseTest {

	static Grammar grammar = null;
	
	@Before
	@SuppressWarnings("deprecation")
	public void setUp() throws Exception {
		if(grammar == null) {
			grammar = new Grammar(new File(new File(
					new File(System.getProperty("user.dir")), "test"),
					"grammar.xml").toURL());
		}
	}

	@Test
	public void dummy() {}

}
