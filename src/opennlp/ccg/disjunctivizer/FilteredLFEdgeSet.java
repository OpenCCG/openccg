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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import opennlp.ccg.hylo.graph.LFEdge;
import opennlp.ccg.hylo.graph.LFEdgeLabel;
import opennlp.ccg.hylo.graph.LFVertex;
import opennlp.ccg.util.Filter;
import opennlp.ccg.util.FilteredSet;


/**
 * A filtered set of LF edges. This class extends {@link FilteredSet} to provide the additional functionality
 * of edge-based views of its contents: {@link #sourceView()}, {@link #targetView()}, and {@link #labelView()},
 * which respectively get the set of source vertices, set of target vertices, and set of labels contained
 * in the edges in this set. All of these view sets are read-only, so that attempting to add or remove
 * elements from them (including via the iterator) throws {@link UnsupportedOperationException}.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class FilteredLFEdgeSet extends FilteredSet<LFEdge> {

	private VertexView sourceView = null, targetView = null;
	private LabelView labelView = null;
	
	/**
	 * Creates a new filtered edge set based on the specified underlying edge set and edge filter.
	 */
	public FilteredLFEdgeSet(Set<? extends LFEdge> edges, Filter<? super LFEdge> edgeFilter) {
		super(edges, edgeFilter);
	}
	
	/**
	 * Gets a view of this filtered edge set as a set of LF vertices that are the 
	 * {@linkplain LFEdge#getSource() source vertices} for each edge in this set.
	 * @return A set containing every LF vertex that is the source vertex of some edge in this set.
	 * Note that the returned collection is immutable, and may contain duplicate vertices.
	 */
	public Collection<LFVertex> sourceView() {
		return (sourceView == null) ? (sourceView = new VertexView(true)) : sourceView;
	}
	
	/**
	 * Gets a view of this filtered edge set as a set of LF vertices that are the 
	 * {@linkplain LFEdge#getTarget() target vertices} for each edge in this set.
	 * @return A set containing every LF vertex that is the target vertex of some edge in this set.
	 * Note that the returned collection is immutable, and may contain duplicate vertices.
	 */
	public Collection<LFVertex> targetView() {
		return (targetView == null) ? (targetView = new VertexView(false)) : targetView;
	}
	
	/**
	 * Gets a view of this filtered edge set as a set of LF vertices that are the 
	 * {@linkplain LFEdge#getLabel() labels} for each edge in this set.
	 * @return A set containing every LF edge label that is the label of some edge in this set.
	 * Note that the returned collection is immutable, and may contain duplicate labels.
	 */
	public Collection<LFEdgeLabel> labelView() {
		return (labelView == null) ? (labelView = new LabelView()) : labelView;
	}
	
	abstract class ComponentView<T> extends AbstractCollection<T> {
		
		abstract T componentOf(LFEdge edge);
		
		@Override
		public Iterator<T> iterator() {
			return new Iterator<T>() {
				private Iterator<LFEdge> i = FilteredLFEdgeSet.this.iterator();
				
				@Override
				public boolean hasNext() {
					return i.hasNext();
				}

				@Override
				public T next() {
					// don't have to worry whether hasNext() is true, iterator should throw exception if not
					return componentOf(i.next());
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public int size() {
			return FilteredLFEdgeSet.this.size();
		}
	}
	
	class VertexView extends ComponentView<LFVertex> {

		boolean source;
		
		VertexView(boolean source) {
			this.source = source;
		}
		
		@Override
		LFVertex componentOf(LFEdge edge) {
			return source ? edge.getSource() : edge.getTarget();
		}		
	}
	
	class LabelView extends ComponentView<LFEdgeLabel> {

		@Override
		LFEdgeLabel componentOf(LFEdge edge) {
			return edge.getLabel();
		}
	}
}
