package opennlp.ccg.hylo.graph;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class LFEdgeFactoryTest extends LFEdgeTest {

	LFEdgeFactory factory;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		factory = new DefaultLFEdgeFactory();
	}

	@Test
	public void testCreateEdge() {
		assertNotSame(edge, factory.createEdge(edge.source, edge.target));
		
		edge = new LFEdge(edge.source, edge.target, null);
		assertEquals(edge, factory.createLabeledEdge(edge.source, edge.target, null));
	}

	@Test
	public void testCreateLabeledEdge() {
		assertEquals(edge, factory.createLabeledEdge(edge.source, edge.target, edge.label));
	}

}
