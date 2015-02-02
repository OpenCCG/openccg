package opennlp.ccg.parse;

import gnu.trove.THashMap;
import gnu.trove.TObjectHashingStrategy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import opennlp.ccg.synsem.Sign;
import opennlp.ccg.synsem.SignHash;

/**
 * A cell pairs a sorted list with an edge map
 * 
 * @author Jason Baldridge
 * @author Gann Bierner
 * @author Daniel Couto-Vale
 */
public class Cell implements Serializable {

	// maps edges to representative edges, according to their headwords and
	// cats, sans LFs
	// NB: using unfilled dependencies in equiv relation appears to unacceptably
	// slow down parsing,
	// with a significant drop in complete parses
	@SuppressWarnings("unchecked")
	private static Map<Edge, Edge> createEdgeMap() {
		return new THashMap(11, representativeEdgeStrategy);
	}

	private static TObjectHashingStrategy representativeEdgeStrategy = new TObjectHashingStrategy() {
		private static final long serialVersionUID = 1L;

		public int computeHashCode(Object o) {
			Sign sign = ((Edge) o).sign;
			int headpos = Edge.getEdge(sign.getLexHead()).wordPos;
			return 31 * headpos + sign.getCategory().hashCodeNoLF();
			// return 31*headpos + sign.getCategory().hashCodeNoLF() +
			// 17*sign.getUnfilledDeps().hashCode();
		}

		public boolean equals(Object o1, Object o2) {
			if (!(o1 instanceof Edge) || !(o2 instanceof Edge))
				return false;
			Sign sign1 = ((Edge) o1).sign;
			Sign sign2 = ((Edge) o2).sign;
			return Edge.getEdge(sign1.getLexHead()).wordPos == Edge.getEdge(sign2.getLexHead()).wordPos
					&& sign1.getCategory().equalsNoLF(sign2.getCategory());
			// && sign1.getUnfilledDeps().equals(sign2.getUnfilledDeps());
		}
	};

	// adds edge to sorted list and optional map, preserving limit; returns true
	// iff edge added
	// nb: all lexical edges kept
	public static boolean addEdgeSorted(Edge edge, List<Edge> list, Map<Edge, Edge> map, int limit, Comparator<Edge> edgeComparator) {
		int index = Collections.binarySearch(list, edge, edgeComparator);
		// convert index to insertion point
		index = Math.abs(index) - 1;
		// if somehow negative, use last position
		if (index < 0)
			index = list.size();
		// check if last and at limit
		boolean limitActive = limit > 0 && !edge.sign.isLexical();
		if (limitActive && index >= limit)
			return false;
		// otherwise add edge
		list.add(index, edge);
		if (map != null)
			map.put(edge, edge);
		// remove last if over limit
		if (limitActive && list.size() > limit) {
			Edge last = list.remove(list.size() - 1);
			if (map != null)
				map.remove(last);
		}
		return true;
	}

	private static final long serialVersionUID = 1L;
	final List<Edge> list = new ArrayList<Edge>();
	final Map<Edge, Edge> map = createEdgeMap();

	private final int cellLimit;
	private final Comparator<Edge> edgeComparator;

	public Cell(int cellLimit, Comparator<Edge> edgeComparator) {
		this.cellLimit = cellLimit;
		this.edgeComparator = edgeComparator;
	}

	int size() {
		return list.size();
	}

	Edge get(Edge edge) {
		return map.get(edge);
	}

	// add edge, preserving cell limit; return true iff given edge added
	boolean add(Edge edge) {
		if (map.containsKey(edge))
			return false;
		return addEdgeSorted(edge, list, map, cellLimit, edgeComparator);
	}

	List<Sign> getSignsSorted() {
		List<Sign> retval = new ArrayList<Sign>(list.size());
		for (Edge e : list)
			retval.add(e.sign);
		return retval;
	}

	SignHash getSigns() {
		SignHash signHash = new SignHash();
		for (Edge e : list) {
			signHash.insert(e.sign);
		}
		return signHash;
	}
}
