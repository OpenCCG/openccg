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

package opennlp.ccg.disjunctivizer;

import static opennlp.ccg.alignment.PhrasePosition.A;
import static opennlp.ccg.alignment.PhrasePosition.B;
import static opennlp.ccg.disjunctivizer.Disjunctivizer.VertexType.LOCAL_ANCESTOR;
import static opennlp.ccg.disjunctivizer.Disjunctivizer.VertexType.OPTIONAL;
import static opennlp.ccg.disjunctivizer.Disjunctivizer.VertexType.PREDICATES;
import static opennlp.ccg.disjunctivizer.Disjunctivizer.VertexType.SHARED;
import static opennlp.ccg.disjunctivizer.Disjunctivizer.VertexType.VISITED;
import static opennlp.ccg.disjunctivizer.MatchType.TARGET_PREDICATE_MISMATCH;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import opennlp.ccg.alignment.PhrasePosition;
import opennlp.ccg.hylo.Mode;
import opennlp.ccg.hylo.Proposition;
import opennlp.ccg.hylo.graph.LFEdge;
import opennlp.ccg.hylo.graph.LFEdgeLabel;
import opennlp.ccg.hylo.graph.LFGraph;
import opennlp.ccg.hylo.graph.LFVertex;
import opennlp.ccg.util.DelegatedFilter;
import opennlp.ccg.util.Filter;
import opennlp.ccg.util.FilteredSet;
import opennlp.ccg.util.MembershipFilter;
import opennlp.ccg.util.VisitedFilter;

import org.jgrapht.traverse.DepthFirstIterator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Creates a disjunctive logical form from a difference between two graphs.
 * Instances can be configured to switch handling of {@link LFGraphDifference#inserts()},
 * {@link LFGraphDifference#deletes()} and {@link LFGraphDifference#substitutions()}
 * on or off. By default, all three are performed.
 * <p>
 * Disjunctivizers can be re-used, so that  all of the boolean parameters
 * {@link #setProcessingDeletes(boolean)}, {@link #setProcessingInserts(boolean)},
 * and {@link #setProcessingSubstitutions(boolean)} can be modified in between calls to
 * {@link #buildDisjunctiveLFFor(LFGraphDifference)}.
 * When these parameters are changed, the disjunctive LF built will change as well.
 * The {@link Document} used to create disjunctive LF elements (and the elements within them)
 * can be configured as well, either at creation or via {@link #setDocument(Document)}. 
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class Disjunctivizer {

	/**
	 * Attribute set tag name: <tt>atts</tt>.
	 */
	public static final String ATTS_TAG = "atts";
	
	/**
	 * Choice disjunction tag name: <tt>one-of</tt>.
	 */
	public static final String CHOICE_TAG = "one-of";
	
	/**
	 * Disjunctive LF tag name: <tt>dlf</tt>.
	 */
	public static final String DLF_TAG = "dlf";
	
	/**
	 * Node tag name: <tt>node</tt>.
	 */
	public static final String NODE_TAG = "node";
	
	/**
	 * Optional disjunction tag name: <tt>opt</tt>.
	 */
	public static final String OPTIONAL_TAG = "opt";
	
	/**
	 * Relation tag name: <tt>rel</tt>.
	 */
	public static final String RELATION_TAG = "rel";
	
	/**
	 * ID attribute name: <tt>id</tt>.
	 */
	public static final String ID_ATTR = "id";
	
	/**
	 * ID reference attribute name: <tt>idref</tt>.
	 */
	public static final String IDREF_ATTR = "idref";
	
	/**
	 * Name attribute name: <tt>name</tt>.
	 */
	public static final String NAME_ATTR = "name";
	
	/**
	 * Predicate attribute name: <tt>pred</tt>.
	 */
	public static final String PRED_ATTR = "pred";
	
	/**
	 * Attribute name for node sharedness: <tt>shared</tt>.
	 */
	public static final String SHARED_ATTR = "shared";
	
	/**
	 * The suffix appended to foreign nodes: <tt>f</tt>.
	 */
	public static final String FOREIGN_SUFFIX = "f";
	
	Document document;
	boolean processingInserts, processingDeletes, processingSubstitutions;
	
	private Element disjunctiveLF;
	
	private LFGraphDifference graphDifference;
	private Set<LFVertex> importedVertices = null;
	private Map<LFVertex, LFVertex> vertexAliases = null;
	private Map<LFVertex, LFVertex> foreignAlignedSubgraphRoots = null;
	
	/**
	 * Creates a new disjunctivizer using a new document.
	 * @see #Disjunctivizer(Document)
	 */
	public Disjunctivizer() throws ParserConfigurationException {
		this(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
	}
	
	/**
	 * Creates a new disjunctivizer that will use the specified document
	 * to create the elements in its generated disjunctive LFs.
	 * @see #Disjunctivizer(Document, boolean, boolean, boolean)
	 */
	public Disjunctivizer(Document document) {
		this(document, true, true, true);
	}
	
	/**
	 * Creates a new disjunctivizer that will use the specified document
	 * to create the elements in its generated disjunctive LFs, with the specified parameters dictating
	 * whether to process inserts, deletes, and substitutions.
	 * 
	 * @param document The document to use for creating elements, attributes, nodes, etc.
	 * @param processingInserts Whether to process {@link LFGraphDifference#inserts()}.
	 * @param processingDeletes Whether to process {@link LFGraphDifference#deletes()}.
	 * @param processingSubstitutions Whether to process {@link LFGraphDifference#substitutions()}.
	 */
	public Disjunctivizer(Document document,
			boolean processingInserts, boolean processingDeletes, boolean processingSubstitutions) {
		if(document == null) {
			throw new IllegalArgumentException("document is null");
		}
		
		this.document = document;
		this.processingInserts = processingInserts;
		this.processingDeletes = processingDeletes;
		this.processingSubstitutions = processingSubstitutions;
	}

	/**
	 * Gets the document used to create elements, nodes, attributes, etc.
	 * @see #Disjunctivizer(Document, boolean, boolean, boolean)
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Sets the document used to create elements.	
	 * @param document The document that will be used while building disjunctive LF elements.
	 */
	public void setDocument(Document document) {
		this.document = document;
	}
	
	/**
	 * Returns whether this disjunctivizer processes {@link LFGraphDifference#inserts()}.
	 */
	public boolean isProcessingInserts() {
		return processingInserts;
	}

	/**
	 * Sets whether this disjunctivizer processes {@link LFGraphDifference#inserts()}.
	 */
	public void setProcessingInserts(boolean processingInserts) {
		if(this.processingInserts != processingInserts) {
			this.processingInserts = processingInserts;
			resetDisjunctiveLF();
		}
	}

	/**
	 * Returns whether this disjunctivizer processes {@link LFGraphDifference#deletes()}.
	 */
	public boolean isProcessingDeletes() {
		return processingDeletes;
	}

	/**
	 * Sets whether this disjunctivizer processes {@link LFGraphDifference#deletes()}.
	 */
	public void setProcessingDeletes(boolean processingDeletes) {
		if(this.processingDeletes != processingDeletes) {
			this.processingDeletes = processingDeletes;
			resetDisjunctiveLF();
		}
	}

	/**
	 * Returns whether this disjunctivizer processes {@link LFGraphDifference#substitutions()}.
	 */
	public boolean isProcessingSubstitutions() {
		return processingSubstitutions;
	}

	/**
	 * Sets whether this disjunctivizer processes {@link LFGraphDifference#substitutions()}.
	 */
	public void setProcessingSubstitutions(boolean processingSubstitutions) {
		if(this.processingSubstitutions != processingSubstitutions) {
			this.processingSubstitutions = processingSubstitutions;
			resetDisjunctiveLF();
		}
	}

	private void resetDisjunctiveLF() {
		this.disjunctiveLF = null;
	}
	
	/**
	 * Builds a disjunctive LF based on the specified graph difference.
	 * The shape of the returned element will change depending on whether inserts, deletes, or substitutions
	 * are being processed.
	 * @param graphDifference The graph difference to use for building the disjunctive LF.
	 * @return A recursively build disjunctive LF based on this disjunctivizer's graph difference.
	 * @throws IllegalArgumentException If <tt>graphDifference</tt> is <tt>null</tt>.
	 */
	public Element buildDisjunctiveLFFor(LFGraphDifference graphDifference) {
		if(graphDifference == null) {
			throw new IllegalArgumentException("graph difference is null");
		}
		
		if(disjunctiveLF == null || !this.graphDifference.equals(graphDifference)) {
			this.graphDifference = graphDifference;
						
			// reset in case this has been previously called
			if(foreignAlignedSubgraphRoots != null) {
				foreignAlignedSubgraphRoots = null;
			}
			
			if(importedVertices == null) {
				importedVertices = new HashSet<LFVertex>();
			}
			else {
				importedVertices.clear();
			}
			
			if(vertexAliases == null) {
				vertexAliases = new HashMap<LFVertex, LFVertex>();
			}
			else {
				vertexAliases.clear();
			}		
			
			disjunctiveLF = document.createElement(DLF_TAG);
			
			for(LFVertex p : graphDifference.a.highestLFAncestors()) {
				disjunctiveLF.appendChild(createDisjunctiveElement(new DLFContext(A, p, disjunctiveLF)));
			}
		}
		
		return disjunctiveLF;
	}
	
	private void findForeignAlignedSubgraphRoots() {
		foreignAlignedSubgraphRoots = new HashMap<LFVertex, LFVertex>();
		
		LFGraphIterator rootIterator = new LFGraphIterator(graphDifference.b);
		Map<Integer, Set<Integer>> mappings = graphDifference.alignment.asMap(B);
		
		while(rootIterator.hasNext()) {
			LFVertex vertex = rootIterator.next();
			
			if(!foreignAlignedSubgraphRoots.containsKey(vertex)) { // already encountered?
				if(mappings.containsKey(vertex.getIndex())) { // aligned?
					LFGraphIterator subgraphIterator = new LFGraphIterator(graphDifference.b, vertex);
					
					while(subgraphIterator.hasNext()) { // map whole subgraph to the aligned vertex
						foreignAlignedSubgraphRoots.put(subgraphIterator.next(), vertex);
					}
				}
			}
		}
	}
	
	private Element createDisjunctiveElement(DLFContext context) {
		LFVertex alias = vertexAliases.get(context.vertex);
		
		String vertexName = nameFor(context.vertex);
		boolean imported = importedVertices.contains(context.vertex);
		
		Element newNode = document.createElement(NODE_TAG);
		context.parent.appendChild(newNode);
		
		DLFContext localContext = context.copy();
		localContext.parent = newNode;
		Set<LFVertex> locals = localContext.getVertices(LOCAL_ANCESTOR),
				visited = localContext.getVertices(VISITED);
		
		if(locals.contains(localContext.vertex)
				|| (alias != null && visited.contains(localContext.vertex))) {
			LFVertex v = (alias == null) ? localContext.vertex : alias;
			localContext.parent.setAttribute(IDREF_ATTR, nameFor(v));
			
			if(visited.contains(localContext.vertex)
					&& localContext.getVertices(SHARED).contains(v) && !locals.contains(v)) {
				localContext.parent.setAttribute(SHARED_ATTR, "true");
			}
		}
		else {
			localContext.parent.setAttribute(ID_ATTR, alias == null ? vertexName : nameFor(alias));
			
			visited.add(localContext.vertex);
			locals.add(localContext.vertex);
			
			addNonPredAttributes(localContext);
			
			if(!imported && processingInserts) { // inserts
				processInserts(localContext);
			}
			if(!imported && processingDeletes) { // deletes
				processDeletes(localContext);
			}
			
			LFGraph graph = localContext.getGraph();
			
			@SuppressWarnings("unchecked")
			Set<LFEdge> outgoingEdges = graph.containsVertex(localContext.vertex)
					? graph.outgoingEdgesOf(localContext.vertex) : Collections.EMPTY_SET;
			
			if(outgoingEdges.isEmpty()) { // leaf?
				setPredicateName(localContext);
			}
			else {
				if(!imported && processingSubstitutions) { // do substitutions, if applicable
					processSubstitutions(localContext);
				}
				else {
					for(LFEdge out : outgoingEdges) {
						if(imported) {
							importedVertices.add(out.getTarget());
						}
						
						// context.graph should be the B graph if imported
						processNonsubstitutedEdge(localContext, out);
					}
				}
			}
		}
		
		fixLabelReferences(newNode);
		
		return newNode;
	}
	
	private String nameFor(LFVertex vertex) {
		String vn = vertex.getName();
		return importedVertices.contains(vertex) ? vn + FOREIGN_SUFFIX : vn;
	}
	
	private void processInserts(DLFContext context) {
		Element optional = null;
		
		for(LFEdge ins : graphDifference.insertsFor(context.vertex)) {
			// check if subgraph is aligned somewhere
			if(foreignAlignedSubgraphRoots == null) {
				findForeignAlignedSubgraphRoots();
			}
			
			if(!foreignAlignedSubgraphRoots.containsKey(ins.getTarget())) {
				DLFContext ctxt = context.copy();
				ctxt.graphPosition = B; // use foreign graph
				
				if(optional == null) {
					ctxt.parent = addOptional(context);
				}
				
				importedVertices.add(ins.getTarget()); // remember that inserted vertex is foreign
				
				doInsertDelete(ctxt, ins);
			}
		}
	}
	
	private void processDeletes(DLFContext context) {
		Element optional = null;
		
		for(LFEdge del : graphDifference.deletesFor(context.vertex)) {
			DLFContext ctxt = context.copy();
			
			if(optional == null) {
				ctxt.parent = addOptional(context);
			}
			
			doInsertDelete(ctxt.copy(), del);
		}
	}
	
	private void doInsertDelete(DLFContext context, LFEdge edge) {
		LFVertex trg = edge.getTarget();
		DLFContext ctxt = context.copy(); 
		ctxt.vertex = trg;
		ctxt.parent = addRelation(ctxt, edge.getLabel());
		ctxt.addVertex(trg, OPTIONAL);
		
		ctxt.parent.appendChild(createDisjunctiveElement(ctxt));
	}
	
	private void processSubstitutions(DLFContext context) {
		for(LFEdge outgoing : context.getGraph().outgoingEdgesOf(context.vertex)) {
			if(graphDifference.substitutionsFor(outgoing).isEmpty()) { // no substitution(s) for this edge?
				processNonsubstitutedEdge(context.copy(), outgoing);
			}
			else {
				processSubstitutedEdge(context.copy(), outgoing);
			}
		}
	}
	
	private void processNonsubstitutedEdge(DLFContext context, LFEdge outgoing) {
		if(!context.getVertices(PREDICATES).contains(context.vertex)) {
			setPredicateName(context);
		}
		
		LFVertex trg = outgoing.getTarget();
		Set<LFVertex> similarTargets = new FilteredSet<LFVertex>(context.getVertices(OPTIONAL), 
				new SimilarTargetVertexFilter(trg));
		
		if(similarTargets.isEmpty()) {
			DLFContext ctxt = context.copy();
			ctxt.vertex = trg;
			ctxt.parent = addRelation(context, outgoing.getLabel());
			
			ctxt.parent.appendChild(createDisjunctiveElement(ctxt));
		}
		else { // target already present as an option
			for(LFVertex similar : similarTargets) {
				assimilateAttributes(context.copy(), trg, similar);
			}
		}
	}
	
	private void processSubstitutedEdge(DLFContext context, LFEdge outgoing) {
		processSubstitutedSimilarTarget(context.copy(), outgoing);
		
		if(!context.getVertices(PREDICATES).contains(context.vertex)) {
			processSubstitutedPredicates(context.copy(), outgoing);
		}
		
		// get the substitutions for the outgoing edge
		Map<LFVertex, Set<LFEdge>> subsBySource = graphDifference.substitutionsBySourceFor(outgoing);
		EdgeMatchFilter predicateFilter = null;
		
		for(LFVertex subSource : subsBySource.keySet()) {
			Set<LFEdge> subEdges = subsBySource.get(subSource);
			
			if(predicateFilter == null) {
				predicateFilter = new EdgeMatchFilter(outgoing, TARGET_PREDICATE_MISMATCH);
			}
			else {
				predicateFilter.setBasis(outgoing);
			}
			
			// find the edges matching the outgoing edge's label, and the vertices with different predicates
			// from the outgoing edge's target vertex
			FilteredLFEdgeSet identicals = new FilteredLFEdgeSet(subEdges, new MembershipFilter<LFEdge>(
										context.getGraph().outgoingEdgesOf(context.vertex)));
			Set<LFEdge> matchingLabels
				= new FilteredLFEdgeSet(subEdges, new LabelMatchFilter(outgoing.getLabel()));
			Set<LFVertex> differentPredicates
				= new FilteredSet<LFVertex>(
						new FilteredLFEdgeSet(matchingLabels, predicateFilter).targetView(),
						new VisitedFilter<LFVertex>());
			
			// deal with the edges with matching labels separately from other substitutions
			subEdges.removeAll(matchingLabels);
			differentPredicates.removeAll(identicals.targetView());
			subEdges.removeAll(identicals);
			
			if(subEdges.isEmpty() && differentPredicates.isEmpty()) { // no substitutions to make
				DLFContext ctxt = context.copyWithVertexMask(LOCAL_ANCESTOR, PREDICATES);
				fixOptions(ctxt, outgoing.getLabel());
				
				ctxt.parent = addRelation(context, outgoing.getLabel());
				ctxt.vertex = outgoing.getTarget();
				ctxt.parent.appendChild(createDisjunctiveElement(ctxt));				
			}
			else {
				if(!differentPredicates.isEmpty()) { // handle matching labels but different predicates
					processDifferentPredicates(context.copy(), outgoing, differentPredicates);
				}
				
				if(!subEdges.isEmpty()) { // handle others
					processSubstitutedEdges(context.copy(), outgoing, subEdges);
				}
			}
		}
	}
	
	private void processDifferentPredicates(DLFContext context, LFEdge outgoing,
			Set<LFVertex> differentPredicates) {
		LFEdgeLabel label = outgoing.getLabel();
		
		boolean terminal = context.getGraph().outDegreeOf(context.vertex) == 0;
		if(!terminal) {
			terminal = !new FilteredSet<LFVertex>(
							differentPredicates, new TerminalFilter(graphDifference.b))
						.isEmpty();
		}
		
		if(terminal) {
			DLFContext ctxt = context.copy();
			ctxt.vertex = outgoing.getTarget();
						
			if(differentPredicates.size() == 1) { // if we're here at all, it's at least non-empty
				processSingletonDifferentPredicate(ctxt, outgoing, differentPredicates.iterator().next());
			}
			else {
				processMultipleDifferentPredicates(ctxt, outgoing, differentPredicates);
			}
		}
		else { // non-terminal, continue recursing through the graph
			DLFContext ctxt = context.copyWithVertexMask(LOCAL_ANCESTOR, PREDICATES);
			
			ctxt.vertex = outgoing.getTarget();
			ctxt.parent = addRelation(ctxt, label);
			
			ctxt.parent.appendChild(createDisjunctiveElement(ctxt));
		}
	}
	
	private void processSingletonDifferentPredicate(DLFContext context, LFEdge outgoing,
			LFVertex differentPredicate) {
		LFEdgeLabel label = outgoing.getLabel();
		
		// add relation, then choice point
		Element newRel = addRelation(context, label);
		context.parent = newRel;
		Element choiceElement = addChoice(context);
		context.parent = choiceElement;
		
		// generate the target element, but do not propagate changes to tracked vertices
		Element targetElement = createDisjunctiveElement(context.copy(true));
		
		if(!vertexAliases.containsKey(differentPredicate)) {
			vertexAliases.put(differentPredicate, outgoing.getTarget());
		}
		
		context.vertex = differentPredicate;
		context.parent.appendChild(createDisjunctiveElement(context.copy(true)));
		
		// cleanup: how many new nodes were aliased?
		NodeList newNodes = newRel.getElementsByTagName(NODE_TAG);
		for(int j = 0; j < newNodes.getLength(); j++) {
			if(newNodes.item(j).getAttributes().getNamedItem(IDREF_ATTR) == null) {
				return; // one wasn't aliased
			}
		}
		
		// if we get here, they all were aliased: use generated target element instead
		newRel.replaceChild(targetElement, choiceElement);
	}
	
	private void processMultipleDifferentPredicates(DLFContext context, LFEdge outgoing,
			Set<LFVertex> differentPredicates) {
		LFEdgeLabel label = outgoing.getLabel();
		
		// generate the choice point
		Element choiceElement = addChoice(context);
		context.parent = choiceElement;
		
		// and the relation, but do not propagate changes to tracked vertices
		context.parent = addRelation(context, label);
		context.parent.appendChild(createDisjunctiveElement(context.copy(true)));
		
		// add attributes tag, after resetting parent to choice point
		context.parent = choiceElement;
		Element atts = addElement(context, ATTS_TAG);
		
		// then go through the different predicates, checking for aliases
		boolean aliased = false;
		for(LFVertex d : differentPredicates) {
			context.parent = atts;
			
			if(!aliased && !vertexAliases.containsKey(d)) {
				vertexAliases.put(d, outgoing.getTarget());
				aliased = true;
			}
			
			// add new relation for each different pred.
			context.parent = addRelation(context, label);
			context.vertex = d;
			context.parent.appendChild(createDisjunctiveElement(context.copy(true)));
		}
	}
	
	private void processSubstitutedEdges(DLFContext context, LFEdge outgoing, Set<LFEdge> substituedEdges) {
		LFEdgeLabel label = outgoing.getLabel();
		boolean singleton = substituedEdges.size() == 1; // can't be empty if we get here
		
		Element choiceElement = addChoice(context);
		context.parent = choiceElement;
		
		Element toAppendTo = singleton ? choiceElement : addElement(context, ATTS_TAG);
		
		context.parent = addRelation(context, label);
		
		DLFContext ctxt = context.copy(true);
		ctxt.vertex = outgoing.getTarget();
		ctxt.parent.appendChild(createDisjunctiveElement(ctxt));
		
		boolean aliased = false;
		context.parent = toAppendTo;
		
		for(LFEdge s : substituedEdges) {
			LFVertex t = s.getTarget();
			String vPred = context.vertex.getPredicate(), tPred = t.getPredicate();
			LFEdgeLabel l = s.getLabel();
			
			context.parent = addRelation(context, l);
			
			// shared?
			if(vPred != null && vPred.equals(tPred) && !label.equals(l)) { 
				Element subNode = addElement(context, NODE_TAG);
				subNode.setAttribute(IDREF_ATTR, nameFor(context.vertex));
				
				LFVertex sAlias = vertexAliases.get(context.vertex);
				if(context.getVertices(VISITED).contains(sAlias)
						&& context.getVertices(SHARED).contains(sAlias)
						&& !context.getVertices(LOCAL_ANCESTOR).contains(sAlias)) {
					subNode.setAttribute(SHARED_ATTR, "true");
				}
			}
			else {
				if((singleton || !aliased) && !vertexAliases.containsKey(t)) {
					vertexAliases.put(t, outgoing.getTarget());
					aliased = true;
				}
				
				DLFContext c = context.copy(true);
				
				c.vertex = t;
				importedVertices.add(t);
				
				c.graphPosition = B; // use foreign graph for substitution
				c.parent.appendChild(createDisjunctiveElement(c));
			}
		}
	}
	
	private void processSubstitutedSimilarTarget(DLFContext context, LFEdge outgoing) {
		LFVertex target = outgoing.getTarget();
		Map<LFVertex, Set<LFEdge>> subsBySource = graphDifference.substitutionsBySourceFor(outgoing);
		
		DLFContext ctxt = context.copy();
		
		// for each substituted edge, look for similar target
		for(LFVertex subSource : subsBySource.keySet()) {
			Set<LFEdge> similarTargetEdges = new FilteredLFEdgeSet(subsBySource.get(subSource),
					new SimilarTargetEdgeFilter(ctxt.vertex, outgoing.getLabel()));
			
			if(!similarTargetEdges.isEmpty()) {
				if(similarTargetEdges.size() > 1) { // more than one similar target?
					System.err.println("more than one similar target edge for " + ctxt.vertex
							+ ": " + similarTargetEdges); // TODO figure out what to do about this
				}
				
				assimilateAttributes(ctxt, target, similarTargetEdges.iterator().next().getTarget());
				LFVertex hp = ctxt.getGraph().highestLFAncestorOf(target);
				
				if(hp == null || hp.equals(outgoing.getSource())) {
					context.getVertices(SHARED).add(target);
					
					ctxt.vertex = target;
					ctxt.vertices = context.copyVertices(LOCAL_ANCESTOR, PREDICATES);
					ctxt.parent.appendChild(createDisjunctiveElement(ctxt));
					
					return; // stop after similar target found
				}
			}
		}
	}
	
	private void processSubstitutedPredicates(DLFContext context, LFEdge outgoing) {
		final String predicate = context.vertex.getPredicate();
		if(predicate != null) {
			Set<LFVertex> alternates = new FilteredSet<LFVertex>(
					graphDifference.substitutionsBySourceFor(outgoing).keySet(),
					new DelegatedFilter<LFVertex, String>(new Filter<String>(){
						@Override
						public boolean allows(String s) {
							return !predicate.equals(s);
						}
					}) {
						@Override
						public String delegateValueFor(LFVertex e) {
							return e.getPredicate();
						}
					});
			
			if(alternates.isEmpty()) { // the simple case, no other predicates involved
				setPredicateName(context);
			}
			else { // add alternates as choice, with predicate an option
				DLFContext ctxt = context.copy();
				ctxt.getVertices(PREDICATES).add(ctxt.vertex);
				
				ctxt.parent = addChoice(ctxt);
				addAttributes(ctxt, PRED_ATTR, predicate);
				
				for(LFVertex ap : alternates) {
					addAttributes(ctxt, PRED_ATTR, ap.getPredicate());
				}
			}
		}
	}
	
	private Element addRelation(DLFContext context, LFEdgeLabel label) {
		Element newRel = addElement(context, RELATION_TAG);
		newRel.setAttribute(NAME_ATTR, label.getName());
		
		return newRel;
	}
	
	private Element addOptional(DLFContext context) {
		return addElement(context, OPTIONAL_TAG);
	}
	
	private Element addChoice(DLFContext context) {
		return addElement(context, CHOICE_TAG);
	}
	
	private Element addElement(DLFContext context, String elementName) {
		Element newEl = document.createElement(elementName);
		context.parent.appendChild(newEl);
		
		return newEl;
	}
	
	private Element addAttributes(DLFContext context, String name, String value) {
		Element newAtts = document.createElement(ATTS_TAG);
		context.parent.appendChild(newAtts);
		
		newAtts.setAttribute(name, value);
		
		return newAtts;
	}
	
	private Element addAttributes(DLFContext context, Map<Mode,Proposition> attributes) {
		Element newAtts = document.createElement(ATTS_TAG);
		context.parent.appendChild(newAtts);
		
		for(Mode m : attributes.keySet()) {
			String n = m.getName();
			if(!n.equals(PRED_ATTR)) { // TODO does this ever happen?
				newAtts.setAttribute(n, attributes.get(m).getName());
			}
		}
		
		return newAtts;
	}
	
	private void fixLabelReferences(Element newNode) {
		NodeList rels = newNode.getChildNodes();
		int rlen = rels.getLength();
		Map<String,String> refRels = new HashMap<String, String>(rlen);
		
		for(int k = 0; k < rlen; k++) {
			Node n = rels.item(k);
			if(n != null && n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(RELATION_TAG)) {
				Element ne = (Element)n;
				Node m = ne.getFirstChild();
				if(m != null && m.getNodeType() == Node.ELEMENT_NODE && m.getNodeName().equals(NODE_TAG)) {
					Element me = (Element)m;
					String l = ne.getAttribute(NAME_ATTR);
					String idref = me.getAttribute(IDREF_ATTR);
					
					if(idref == null || idref.length() == 0) {
						String id = me.getAttribute(ID_ATTR);
						if(id != null && id.length() > 0) {
							refRels.put(l, id);
						}
					}
					else {
						if(idref.equals(refRels.get(l))) {
							newNode.removeChild(n);
						}
						else {
							refRels.put(l, idref);
						}
					}
				}
			}
		}
	}
	
	private void fixOptions(DLFContext context, LFEdgeLabel label) {
		String cPred = context.vertex.getPredicate();
		if(cPred == null) {
			return;
		}
		
		NodeList ncs = context.parent.getChildNodes();
		for(int j = 0; j < ncs.getLength(); j++) {
			Node c = ncs.item(j);
			if(c != null && c.getNodeType() == Node.ELEMENT_NODE && c.getNodeName().equals(OPTIONAL_TAG)) {
				NodeList rs = c.getChildNodes();
				for(int k = 0; k < rs.getLength(); k++) {
					Node r = rs.item(k);
					if(r != null && r.getNodeType() == Node.ELEMENT_NODE
							&& r.getNodeName().equals(RELATION_TAG)) {
						Element re = (Element)r;
						if(label.getName().equals(re.getAttribute(NAME_ATTR))) {
							Node d = re.getFirstChild();
							if(d != null && d.getNodeType() == Node.ELEMENT_NODE
									&& d.getNodeName().equals(NODE_TAG)
									&& cPred.equals(((Element)d).getAttribute(PRED_ATTR))) {
								context.parent.removeChild(c); 
								break; // don't try to remove more than once, throws DOMException
							}
						}
					}
				}
			}
		}
	}
	
	private void setPredicateName(DLFContext context) {
		String p = context.vertex.getPredicate(); 
		if(p != null) {
			context.parent.setAttribute(PRED_ATTR, p);
			context.getVertices(PREDICATES).add(context.vertex);
		}
	}
		
	private void addNonPredAttributes(DLFContext context) {
		for(Mode m : context.vertex.attributeNames()) {
			String n = m.getName();
			if(!n.equals(PRED_ATTR)) { // TODO is this attribute ever present??
				context.parent.setAttribute(n, context.vertex.getAttributeValue(m).getName());
			}
		}
	}
	
	private void assimilateAttributes(DLFContext context, LFVertex one, LFVertex two) {
		// copy attribute maps for both vertices
		Map<Mode, Proposition> oneAttrs = new HashMap<Mode, Proposition>(one.getAttributeMap()),
				twoAttrs = new HashMap<Mode, Proposition>(two.getAttributeMap());
		
		// add all attributes common to both vertices, remove from both maps
		Iterator<Map.Entry<Mode, Proposition>> i = oneAttrs.entrySet().iterator();
		while(i.hasNext()) {
			Map.Entry<Mode, Proposition> e = i.next();
			Set<Map.Entry<Mode, Proposition>> tes = twoAttrs.entrySet();			
			
			if(tes.contains(e)) {
				context.parent.setAttribute(e.getKey().getName(), e.getValue().getName());
				
				i.remove();
				tes.remove(e);
			}
		}
		
		if(oneAttrs.isEmpty()) { // if first is empty, add second as optional
			if(!twoAttrs.isEmpty()) {
				DLFContext ctxt = context.copy(true);
				ctxt.parent = addOptional(context);
				
				addAttributes(ctxt, twoAttrs);
			}
		}
		else if(twoAttrs.isEmpty()) { // some attributes remain for first vertex
			addAttributes(context.copy(), oneAttrs);
		}
		else { // both are non-empty, make choice
			DLFContext ctxt = context.copy(true);
			ctxt.parent = addChoice(context);
			
			addAttributes(ctxt, oneAttrs);
			addAttributes(ctxt, twoAttrs);
		}
	}

	static class SimilarTargetVertexFilter implements Filter<LFVertex> {
		LFVertex vertex;
		
		SimilarTargetVertexFilter(LFVertex vertex) {
			this.vertex = vertex;
		}
		
		@Override
		public boolean allows(LFVertex v) {
			String p = vertex.getPredicate();
			return p != null && p.equals(v.getPredicate());
		}
	}
	
	static class SimilarTargetEdgeFilter extends DelegatedFilter<LFEdge, LFVertex> {

		LFEdgeLabel label;
		
		SimilarTargetEdgeFilter(LFVertex vertex, LFEdgeLabel label) {
			super(new SimilarTargetVertexFilter(vertex));
			this.label = label;
		}
		
		@Override
		public boolean allows(LFEdge e) {
			return super.allows(e) && label.equals(e.getLabel());
		}

		@Override
		public LFVertex delegateValueFor(LFEdge e) {
			return e.getTarget();
		}
	}
	
	class TerminalFilter implements Filter<LFVertex> {
		LFGraph graph;
		
		TerminalFilter(LFGraph graph) {
			this.graph = graph;
		}

		@Override
		public boolean allows(LFVertex e) {
			return graph.outDegreeOf(e) == 0;
		}
	}
	
	static enum VertexType {
		LOCAL_ANCESTOR, OPTIONAL, PREDICATES, SHARED, VISITED;
	}
	
	class DLFContext {
		PhrasePosition graphPosition;
		LFVertex vertex;
		Element parent;
		
		private Map<VertexType, Set<LFVertex>> vertices;
		
		DLFContext(PhrasePosition graphPosition, LFVertex vertex, Element parent) {
			this(graphPosition, vertex, parent, new EnumMap<VertexType, Set<LFVertex>>(VertexType.class));
		}
		
		DLFContext(PhrasePosition graphPosition, LFVertex vertex, Element parent, Map<VertexType,
				Set<LFVertex>> vertices) {
			this.graphPosition = graphPosition;
			this.vertex = vertex;
			this.parent = parent;
			this.vertices = vertices;
		}
		
		LFGraph getGraph() {
			return graphDifference.get(graphPosition);
		}
		
		DLFContext copy() {
			return copy(false);
		}
		
		DLFContext copy(boolean copyVertices) {
			return copyVertices ? copyWithVertexMask(VertexType.values())
					: new DLFContext(graphPosition, vertex, parent, vertices);
		}
		
		DLFContext copyWithVertexMask(VertexType... vertexType) {
			return new DLFContext(graphPosition, vertex, parent, copyVertices(vertexType));
		}
		
		Map<VertexType, Set<LFVertex>> copyVertices() {
			return copyVertices(VertexType.values());
		}
		
		Map<VertexType, Set<LFVertex>> copyVertices(VertexType... vertexType) {
			Map<VertexType, Set<LFVertex>> m = new EnumMap<VertexType, Set<LFVertex>>(vertices);
			m.keySet().retainAll(Arrays.asList(vertexType));
			
			return m;
		}
		
		Set<LFVertex> getVertices(VertexType vertexType) {
			Set<LFVertex> vs = vertices.get(vertexType);
			
			if(vs == null) {
				vs = new HashSet<LFVertex>();
				vertices.put(vertexType, vs);
			}
			
			return vs;
		}
		
		boolean addVertex(LFVertex vertex, VertexType vertexType) {
			return getVertices(vertexType).add(vertex);
		}
	}
	
	static class LFGraphIterator extends DepthFirstIterator<LFVertex, LFEdge> {
		LFGraphIterator(LFGraph graph) {
			super(graph);
		}
		
		LFGraphIterator(LFGraph graph, LFVertex startVertex) {
			super(graph, startVertex);
		}
	}
}
