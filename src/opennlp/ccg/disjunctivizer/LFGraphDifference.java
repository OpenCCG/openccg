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
import static opennlp.ccg.disjunctivizer.MatchType.SOURCE_ALIGNED;
import static opennlp.ccg.disjunctivizer.MatchType.SOURCE_MATCH;
import static opennlp.ccg.disjunctivizer.MatchType.SOURCE_UNALIGNED;
import static opennlp.ccg.disjunctivizer.MatchType.TARGET_ALIGNED;
import static opennlp.ccg.disjunctivizer.MatchType.TARGET_UNALIGNED;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import opennlp.ccg.alignment.Alignment;
import opennlp.ccg.alignment.PhrasePosition;
import opennlp.ccg.hylo.graph.LFEdge;
import opennlp.ccg.hylo.graph.LFGraph;
import opennlp.ccg.hylo.graph.LFVertex;
import opennlp.ccg.util.CompositeFilter;
import opennlp.ccg.util.Filter;
import opennlp.ccg.util.FilteredMap;
import opennlp.ccg.util.FilteredSet;
import opennlp.ccg.util.VisitedFilter;

/**
 * Represents the difference between two {@link LFGraph}s that characterizes their difference as a set 
 * of edits: either {@link #inserts()}, {@link #deletes()}, or {@link #substitutions()}. These edits are
 * determined by a specified {@linkplain #getAlignment() alignment} between the phrases the graphs
 * are supposed to represent. All of the sets of edges returned by this class, and the convenience maps
 * build on top of them, are read-only. Attempting to add or remove elements or keys from any of these
 * (including via any of the iterators) throws an {@link UnsupportedOperationException}.
 * <p>
 * This class also provides the convenience methods {@link #insertsFor(LFVertex)} and  
 * {@link #deletesFor(LFVertex)}, for getting just the inserts or deletes for a specified vertex.
 * The convenience methods {@link #substitutionsFor(LFEdge)} gets the set of substitutions correspoding
 * to a given edge, and {@link #substitutionsBySource()} and {@link #substitutionsBySourceFor(LFEdge)}
 * are similar methods that provide maps whose keys are the source vertices of the substituted edges. 
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class LFGraphDifference {

	final LFGraph a, b;
	final Alignment alignment;
	
	private Set<LFEdge> deletes, inserts, substitutions;
	
	/**
	 * Creates a new graph difference between <tt>a</tt> and <tt>b</tt>, as determined by the specified
	 * alignment.
	 * @param a The {@linkplain PhrasePosition#A A-position} graph.
	 * @param b The {@linkplain PhrasePosition#B B-position} graph.
	 * @param alignment An alignment between <tt>a</tt> and <tt>b</tt> where the
	 * {@linkplain PhrasePosition#A A-position} indices are understood to correspond to <tt>a</tt> and
	 * {@linkplain PhrasePosition#B B-position} indices are understood to correspond to <tt>b</tt>.
	 * @throws IllegalArgumentException If either graph is <tt>null</tt>, or if the alignment is
	 * <tt>null</tt>.
	 */
	public LFGraphDifference(LFGraph a, LFGraph b, Alignment alignment) {
		checkGraph(a, A);
		checkGraph(b, B);
		
		if(alignment == null) {
			throw new IllegalArgumentException("alignment is null");
		}
		
		this.a = a;
		this.b = b;
		this.alignment = alignment;
	}
	
	private void checkGraph(LFGraph g, PhrasePosition pos) {
		if(g == null) {
			throw new IllegalArgumentException(pos.name() + " graph is null");
		}
	}

	/**
	 * Gets the {@linkplain PhrasePosition#A A-position} graph.
	 */
	public LFGraph getA() {
		return get(A);
	}

	/**
	 * Gets the {@linkplain PhrasePosition#B B-position} graph.
	 */
	public LFGraph getB() {
		return get(B);
	}
	
	/**
	 * Gets the graph in the specified position.
	 * @param position The position to retrieve a graph for.
	 * @return The value of {@link #getA()} if <tt>position</tt> is {@link PhrasePosition#A}, and
	 * the value of {@link #getB()} otherwise.
	 */
	public LFGraph get(PhrasePosition position) {
		return (position == A) ? a : b;
	}

	/**
	 * Gets the alignment used to determine the edits between the two graphs.
	 */
	public Alignment getAlignment() {
		return alignment;
	}
	
	/**
	 * Computes a hash code for this graph difference based on its graphs and the
	 * alignment between them.
	 */
	@Override
	public int hashCode() {
		return 31 * a.hashCode() + b.hashCode() + alignment.hashCode(); 
	}

	/**
	 * Tests whether this LF graph difference is equivalent to another by comparing their
	 * graphs and the alignment between them.
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof LFGraphDifference) {
			LFGraphDifference diff = (LFGraphDifference)obj;
			return a.equals(diff.a) && b.equals(diff.b) && alignment.equals(diff.alignment);
		}
		
		return false;
	}
	
	/**
	 * Gets a string representation of this graph difference.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("difference for graphs: ");
		
		for(PhrasePosition pos : PhrasePosition.values()) {
			sb.append(pos);
			sb.append(": ");
			sb.append(get(pos));
			sb.append(", ");
		}
		
		sb.append("alignment: ");
		sb.append(alignment.toString());
		
		return sb.toString();
	}

	/**
	 * Gets an LF graph difference that is the reverse of the present one.
	 * @return An LF graph difference whose {@linkplain PhrasePosition#A A-position} graph is the value of this
	 * difference's {@link #getB()}, whose {@linkplain PhrasePosition#B B-position} graph is the value of this
	 * difference's {@link #getA()}, and whose alignments are the {@linkplain Alignment#reverse() reverse} of
	 * this difference's {@link #getAlignment()}. 
	 */
	public LFGraphDifference reverse() {
		return new LFGraphDifference(b, a, alignment.reverse());
	}
	
	/**
	 * Gets the deletes for this graph difference.
	 * @return The set of edges in the {@linkplain PhrasePosition#A A-position} graph that have an aligned 
	 * {@linkplain LFEdge#getSource() source vertex} and an unaligned 
	 * {@linkplain LFEdge#getTarget() target vertex}.
	 * 
	 * @see AlignedEdgeFilter
	 */
	public Set<LFEdge> deletes() {
		return (deletes == null) ? (deletes = doDeletes(A)) : deletes;
	}
	
	/**
	 * Gets the inserts for this graph difference.
	 * @return The set of edges in the {@linkplain PhrasePosition#B B-position} graph that have an aligned 
	 * {@linkplain LFEdge#getSource() source vertex} and an unaligned 
	 * {@linkplain LFEdge#getTarget() target vertex}.
	 * 
	 * @see AlignedEdgeFilter
	 */
	public Set<LFEdge> inserts() {
		return (inserts == null) ? (inserts = doDeletes(B)) : inserts;
	}
	
	Set<LFEdge> doDeletes(PhrasePosition keyPosition) {
		return Collections.unmodifiableSet(new FilteredLFEdgeSet(get(keyPosition).edgeSet(),
				new AlignedEdgeFilter(alignment.asMap(keyPosition).keySet(),
						SOURCE_ALIGNED, TARGET_UNALIGNED)));
	}
	
	/**
	 * Gets the inserts for a specified vertex.	
	 * @param vertex The vertex to return the inserts for.
	 * @return The subset of {@link #inserts()} whose {@linkplain LFEdge#getSource() source} index is among the
	 * {@linkplain Alignment#getTargets(Integer) targets} for the specified vertex, or {@link Collections#EMPTY_SET}
	 * if none exist.
	 * 
	 * @see AlignedEdgeFilter
	 */
	@SuppressWarnings("unchecked")
	public Set<LFEdge> insertsFor(LFVertex vertex) {
		Set<Integer> indices = alignment.getTargets(vertex.getIndex());
		
		return (indices.isEmpty()) ? Collections.EMPTY_SET
				: new FilteredLFEdgeSet(inserts(), new AlignedEdgeFilter(indices, SOURCE_ALIGNED));
	}
	
	/**
	 * Gets the deletes for a specified vertex.
	 * @param vertex The vertex to get the deletes for.
	 * @return The subset of {@link #deletes()} whose {@linkplain LFEdge#getSource() source vertex} is
	 * the specified vertex.
	 * 
	 * @see VertexMatchFilter
	 */
	public Set<LFEdge> deletesFor(LFVertex vertex) {
		return new FilteredLFEdgeSet(deletes(), new VertexMatchFilter(vertex, SOURCE_MATCH));
	}
	
	/**
	 * Gets the substitutions for this graph difference.
	 * @return The subset of the {@linkplain PhrasePosition#B B-position} graph's edges for which there
	 * exists an edge in the {@linkplain PhrasePosition#A A-position} graph that meets the following
	 * conditions:
	 * <ol>
	 * 	<li>The B edge's source is aligned to the A edge's source, but the B edge's target is not.</li>
	 * 	<li>The B edge's target is aligned to the A edge's target, but the B edge's source is not.</li>
	 * </ol>
	 * 
	 * @see CompositeFilter
	 */
	public Set<LFEdge> substitutions() {
		if(substitutions == null) {
			substitutions = new LinkedHashSet<LFEdge>();
			Set<LFEdge> bEdges = b.edgeSet();
			
			AlignedEdgeFilter sourceFilter = null, targetFilter = null;
			CompositeFilter<LFEdge> filter = new CompositeFilter<LFEdge>();
			
			for(LFEdge aEdge : a.edgeSet()) {
				Set<Integer> sMaps = alignment.getTargets(aEdge.getSource().getIndex()),
								tMaps = alignment.getTargets(aEdge.getTarget().getIndex());
				
				if(!sMaps.isEmpty() && !tMaps.isEmpty()) {
					if(sourceFilter == null) {
						sourceFilter = new AlignedEdgeFilter(sMaps, SOURCE_ALIGNED, TARGET_UNALIGNED);
						targetFilter = new AlignedEdgeFilter(tMaps, TARGET_ALIGNED, SOURCE_UNALIGNED);
						
						filter.addFilter(sourceFilter);
						filter.addFilter(targetFilter);
					}
					else {
						sourceFilter.setAlignmentIndices(sMaps);
						targetFilter.setAlignmentIndices(tMaps);
					}
					
					substitutions.addAll(new FilteredLFEdgeSet(bEdges, filter));
				}
			}
		}
		
		return Collections.unmodifiableSet(substitutions);
	}
	
	/**
	 * Gets the substitutions for the specified edge.
	 * @param edge The edge to get substitutions for.
	 * @return The subset of {@link #substitutions()} whose source is aligned to the edge's source and
	 * whose target is aligned to the edge's target, or {@link Collections#EMPTY_SET} if none exist. 
	 */
	@SuppressWarnings("unchecked")
	public Set<LFEdge> substitutionsFor(LFEdge edge) {
		Set<Integer> srcMapsTo = alignment.getTargets(edge.getSource().getIndex()),
			trgMapsTo = alignment.getTargets(edge.getTarget().getIndex());
		
		return (srcMapsTo.isEmpty() || trgMapsTo.isEmpty()) ? Collections.EMPTY_SET
				: Collections.unmodifiableSet(new FilteredLFEdgeSet(substitutions(),
						new CompositeFilter<LFEdge>(new AlignedEdgeFilter(srcMapsTo, SOURCE_ALIGNED),
								new AlignedEdgeFilter(trgMapsTo, TARGET_ALIGNED))));
	}
	
	/**
	 * Gets a map view of the substitutions in this graph difference.
	 * @return A map whose keys are the source vertices in the set of {@link #substitutions()} and whose values
	 * are the edges whose {@linkplain LFEdge#getSource() source vertex} is the same as the corresponding key.
	 * If there are no substitutions, {@link Collections#EMPTY_MAP} is returned.
	 * <p>
	 * Note that the returned map is
	 * read-only, that is, both its {@link Map#put(Object, Object)} method and its 
	 * {@linkplain Map#entrySet() entry set}'s iterator's {@link Iterator#remove()} method throw an
	 * {@link UnsupportedOperationException}. Also, the members of the returned map's entry set are immutable,
	 * so that their {@link Entry#setValue(Object)} methods also throw an {@link UnsupportedOperationException}.
	 */
	@SuppressWarnings("unchecked")
	public Map<LFVertex, Set<LFEdge>> substitutionsBySource() {
		Set<LFEdge> subs = substitutions();
		
		return subs.isEmpty() ? Collections.EMPTY_MAP
				: Collections.unmodifiableMap(
						new FilteredMap<LFVertex, Set<LFEdge>>(new SourceView(),
								new VisitedFilter<LFVertex>()));
	}
	
	/**
	 * Gets a map view of the substitutions for the specified edge.
	 * @param edge The edge to get substitutions for.
	 * @return The subset of {@link #substitutionsBySource()} in which the keys are aligned to the specified edge's
	 * source and the associated values' targets are aligned to the specified edge's target. Since this map is
	 * based on the one returned by {@link #substitutionsBySource()}, it is also read-only, and the same stipulations
	 * apply to it.
	 * 
	 * @see #substitutionsBySource()
	 */
	public Map<LFVertex, Set<LFEdge>> substitutionsBySourceFor(LFEdge edge) {
		Map<LFVertex, Set<LFEdge>> subsBySource = substitutionsBySource();		
		return subsBySource.isEmpty() ? subsBySource : new SubstitutedSourceView(subsBySource, edge);
	}
	
	class SourceView extends AbstractMap<LFVertex, FilteredLFEdgeSet> {

		@Override
		public Set<Entry<LFVertex, FilteredLFEdgeSet>> entrySet() {
			return new AbstractSet<Entry<LFVertex,FilteredLFEdgeSet>>() {
				Set<LFEdge> subs = substitutions();
				
				@Override
				public int size() {
					return subs.size();
				}

				@Override
				public Iterator<Entry<LFVertex, FilteredLFEdgeSet>> iterator() {
					return new Iterator<Entry<LFVertex,FilteredLFEdgeSet>>() {
						private Iterator<LFEdge> edgeIterator = null;
																			
						@Override
						public boolean hasNext() {
							if(edgeIterator == null) {
								edgeIterator = subs.iterator();
							}
							
							return edgeIterator.hasNext();
						}

						@Override
						public Entry<LFVertex, FilteredLFEdgeSet> next() {
							if(edgeIterator == null) {
								edgeIterator = subs.iterator();
							}
							
							LFVertex src = edgeIterator.next().getSource();
							
							return new SimpleImmutableEntry<LFVertex, FilteredLFEdgeSet>(src,
									new FilteredLFEdgeSet(subs, new VertexMatchFilter(src, SOURCE_MATCH)));
						}
						
						@Override
						public void remove() { // subs.iterator() should be read-only, but just in case
							throw new UnsupportedOperationException();
						}
					};
				}
			};
		}
	}
	
	class SubstitutedSourceView extends AbstractMap<LFVertex, Set<LFEdge>> {

		Map<LFVertex, Set<LFEdge>> sourceView;
		LFEdge edge;
		
		private Set<Entry<LFVertex, Set<LFEdge>>> entrySet;
		
		SubstitutedSourceView(Map<LFVertex, Set<LFEdge>> sourceView, LFEdge edge) {
			this.sourceView = sourceView;
			this.edge = edge;
		}
		
		@Override
		public Set<Entry<LFVertex, Set<LFEdge>>> entrySet() {
			return (entrySet == null) ? (entrySet = new EntrySet()) : entrySet;
		}
		
		class EntrySet extends AbstractSet<Entry<LFVertex, Set<LFEdge>>> {
			private Set<Entry<LFVertex, Set<LFEdge>>> entries;
			Set<Integer> srcMapsTo = alignment.getTargets(edge.getSource().getIndex()),
								trgMapsTo = alignment.getTargets(edge.getTarget().getIndex());
			
			Set<Entry<LFVertex, Set<LFEdge>>> entries() {
				if(entries == null) {
					entries = new FilteredSet<Entry<LFVertex, Set<LFEdge>>>(
						sourceView.entrySet(),
						new Filter<Entry<LFVertex, Set<LFEdge>>>() {
							@Override
							public boolean allows(Entry<LFVertex, Set<LFEdge>> e) {
								if(srcMapsTo.contains(e.getKey().getIndex())) {
									for(LFEdge t : e.getValue()) {
										if(trgMapsTo.contains(t.getTarget().getIndex())) {
											return true;
										}
									}
								}
								
								return false;
							}
						}
					);
				}
				
				return entries;
			}
			
			@Override
			public int size() {
				return entries().size();
			}
			
			@Override
			public Iterator<Entry<LFVertex, Set<LFEdge>>> iterator() {
				return new Iterator<Entry<LFVertex, Set<LFEdge>>>() {
					private Iterator<Entry<LFVertex, Set<LFEdge>>> i = entries().iterator();
					
					@Override
					public boolean hasNext() {
						return i.hasNext();
					}

					@Override
					public Entry<LFVertex, Set<LFEdge>> next() {
						Entry<LFVertex, Set<LFEdge>> e = i.next();
						return new SimpleImmutableEntry<LFVertex, Set<LFEdge>>(
								e.getKey(),
								new FilteredLFEdgeSet(e.getValue(), new Filter<LFEdge>() {
									@Override
									public boolean allows(LFEdge e) {
										return trgMapsTo.contains(e.getTarget().getIndex());
									}									
								}
							)
						);
					}

					@Override
					public void remove() { // source view is already read-only, so just in case
						throw new UnsupportedOperationException();						
					}					
				};
			}
		}
	}
}
