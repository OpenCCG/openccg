//////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2012 Scott Martin
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

package opennlp.ccg.hylo.graph;

import static opennlp.ccg.hylo.HyloHelper.isAttrPred;
import static opennlp.ccg.hylo.HyloHelper.isElementaryPredication;
import static opennlp.ccg.hylo.HyloHelper.isLexPred;
import static opennlp.ccg.hylo.HyloHelper.isRelPred;

import java.util.List;
import java.util.Map;

import opennlp.ccg.hylo.Diamond;
import opennlp.ccg.hylo.Flattener;
import opennlp.ccg.hylo.HyloHelper;
import opennlp.ccg.hylo.Nominal;
import opennlp.ccg.hylo.Proposition;
import opennlp.ccg.hylo.SatOp;
import opennlp.ccg.realize.Realizer;
import opennlp.ccg.synsem.LF;

import org.jdom.Element;
import org.jdom.input.DOMBuilder;

/**
 * Factory class for creating LF graphs based on {@link LF}s and XML elements 
 * that represent them. Graph factories cannot be instantiated, however, their
 * methods for building LF graphs are statically accessible.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 * @see LFGraph
 * @see LFGraphFactory#newGraphFrom(LF)
 */
public class LFGraphFactory {
	
	/**
	 * An instance of {@link DefaultLFEdgeFactory}.
	 */
	public static final LFEdgeFactory DEFAULT_EDGE_FACTORY = new DefaultLFEdgeFactory();
	static final DOMBuilder DOM_BUILDER = new DOMBuilder();
	static final Flattener FLATTENER = new Flattener();
	
	private LFGraphFactory() {
		// can't instantiate
	}
	
	/**
	 * Builds a new LF graph based on the representation provided in the specified w3c XML element.
	 * @param lfElement The XML element with root element tagged <tt>lf</tt>.
	 * @return The value of {@link #newGraphFrom(Element)} after using a {@link DOMBuilder} to create a
	 * jdom document.
	 * 
	 * @see #newGraphFrom(Element)
	 */
	public static LFGraph newGraphFrom(org.w3c.dom.Element lfElement) {
		return newGraphFrom(DOM_BUILDER.build(lfElement));
	}
	
	/**
	 * Creates a new LF graph from the corresponding representation contained in the specified 
	 * jdom XML element.
	 * @param lfElement The jdom element containing the representation of the LF.
	 * @return An LF graph build from an {@link LF} object obtained by calling 
	 * {@link Realizer#getLfFromElt(Element)}.
	 * 
	 * @see LFGraphFactory#newGraphFrom(LF)
	 */
	public static LFGraph newGraphFrom(Element lfElement) {
		return newGraphFrom(Realizer.getLfFromElt(lfElement));
	}
	
	/**
	 * Creates a new LF graph based on the specified LF object. The LF object is first flattened, and then
	 * its LF ancestry structure is obtained by calling {@link Flattener#getHighestParentMap()}.
	 * <p>
	 * This method makes two passes over the list of {@link SatOp}s obtained by flattening the specified LF.
	 * The first pass adds vertices to the graph for every lexical predication, as determined by calling
	 * {@link HyloHelper#isLexPred(LF)} on the {@linkplain SatOp#getNominal() SatOp's nominal} and
	 * {@linkplain SatOp#getArg() proposition argument}.
	 * <p>
	 * The second pass proceeds by cases, depending on the nature of the SatOp in question:
	 * <dl>
	 * 	<dt>Lexical predications</dt>
	 * 	<dd>cause the new LF graph to be updated with the corresponding LF ancestry, as determined by
	 * 		{@link Flattener#getHighestParentMap()}.</dd>
	 *  <dt>Relation predications</dt>
	 *  <dd>cause a new {@link LFEdge} to be added to the LF graph based on the
	 *  	{@linkplain SatOp#getArg() SatOp's argument} and {@linkplain Diamond#getMode() the argument's 
	 *  	mode}.</dd>
	 *  <dt>Attribute-value predications</dt>
	 *  <dd>cause the vertex corresponding to the {@linkplain SatOp#getNominal() SatOp's nominal} to have
	 *  	attributes {@linkplain LFVertex#setAttribute(opennlp.ccg.hylo.Mode, Proposition) added} based on
	 *  	the {@linkplain SatOp#getArg() SatOp's argument}.</dd>
	 * </dl>
	 * where the nature of the SatOp in question is determined using {@link HyloHelper#isLexPred(LF)},
	 * {@link HyloHelper#isRelPred(LF)}, and {@link HyloHelper#isAttrPred(LF)}.
	 * 
	 * @param lf The LF object to build an LF graph for.
	 * @return A new LF graph whose vertices represent the nominals in the LF's flattened representation and
	 * whose edges represent its relation predications.
	 * @throws IllegalArgumentException If <tt>lf</tt> is <tt>null</tt>.
	 */
	public static LFGraph newGraphFrom(LF lf) {
		if(lf == null) {
			throw new IllegalArgumentException("lf is null");
		}
		
		LFGraph g = new LFGraph(DEFAULT_EDGE_FACTORY);
		
		Flattener f = new Flattener();
		List<SatOp> satOps = f.flatten(lf);
		Map<Nominal,Nominal> ancestorMap = f.getHighestParentMap();
		
		for(SatOp so : satOps) { // first pass adds vertices
			if(isLexPred(so)) {
				g.addVertex(new LFVertex(so.getNominal(), (Proposition)so.getArg()));
			}
		}
		
		for(SatOp so : satOps) { // second pass adds edges and attributes, sets highest parent (if any)
			if(isElementaryPredication(so)) {
				Nominal soNom = so.getNominal();
				LFVertex source = g.findVertexByNominal(soNom);
				
				// check if node is not yet added (not a lex. pred.)
				if(source == null) {
					source = new LFVertex(soNom);
					g.addVertex(source);
				}
				
				if(isLexPred(so)) {
					Nominal parent = ancestorMap.get(source.nominal);
					if(parent != null) {
						g.highestAncestorMap.put(source, g.findVertexByNominal(parent));
					}
				}
				else if(isRelPred(so)) {
					Diamond d = (Diamond)so.getArg();
					
					Nominal dArg = (Nominal)d.getArg();
					LFVertex target = g.findVertexByNominal(dArg);
					
					if(target == null) { 
						target = new LFVertex(dArg);
						g.addVertex(target);
					}
					
					g.addLabeledEdge(source, target, LFEdgeLabel.forMode(d.getMode()));
				}
				else if(isAttrPred(so)) {
					Diamond d = (Diamond)so.getArg();
					source.addAttribute(d.getMode(), (Proposition)d.getArg());
				}
			}
		}
		
		return g;
	}

}
