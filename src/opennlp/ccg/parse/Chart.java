///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-10 Jason Baldridge, Gann Bierner and Michael White
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

package opennlp.ccg.parse;

import opennlp.ccg.grammar.*;
import opennlp.ccg.synsem.*;
import gnu.trove.*;

import java.io.*;
import java.util.*;

/**
 * An implementation of the table (or chart) used for chart parsers like CKY.
 * Special functions are provided for combining cells of the chart into another
 * cell.  Time or edge or cell limits can be placed on initial chart construction. 
 * A pruning value applies to unpacking, which also limits the number of equivalent 
 * edges kept during chart construction.
 * 
 * @author Jason Baldridge
 * @author Gann Bierner
 * @author Michael White
 * @version $Revision: 1.41 $, $Date: 2011/11/16 03:25:27 $
 */
public class Chart {

    // maps edges to representative edges, according to their headwords and cats, sans LFs
	// NB: using unfilled dependencies in equiv relation appears to unacceptably slow down parsing, 
	//     with a significant drop in complete parses
    @SuppressWarnings("unchecked")
    private static Map<Edge,Edge> createEdgeMap() {
    	return new THashMap(11, representativeEdgeStrategy);
    }
    
    private static TObjectHashingStrategy representativeEdgeStrategy = new TObjectHashingStrategy() {
		private static final long serialVersionUID = 1L;
		public int computeHashCode(Object o) {
            Sign sign = ((Edge)o).sign;
            int headpos = Edge.getEdge(sign.getLexHead()).wordPos; 
            return 31*headpos + sign.getCategory().hashCodeNoLF(); 
            //return 31*headpos + sign.getCategory().hashCodeNoLF() + 17*sign.getUnfilledDeps().hashCode(); 
        }
        public boolean equals(Object o1, Object o2) {
        	if (!(o1 instanceof Edge) || !(o2 instanceof Edge)) return false;
            Sign sign1 = ((Edge)o1).sign; Sign sign2 = ((Edge)o2).sign;
            return Edge.getEdge(sign1.getLexHead()).wordPos == Edge.getEdge(sign2.getLexHead()).wordPos && 
            	sign1.getCategory().equalsNoLF(sign2.getCategory()); 
            	//&& sign1.getUnfilledDeps().equals(sign2.getUnfilledDeps());
        }
    };
    
    // a cell pairs a sorted list with an edge map
    private class Cell implements Serializable {
		private static final long serialVersionUID = 1L;
		final List<Edge> list = new ArrayList<Edge>();
    	final Map<Edge,Edge> map = createEdgeMap();
    	int size() { return list.size(); }
    	Edge get(Edge edge) { return map.get(edge); }
    	// add edge, preserving cell limit; return true iff given edge added
    	boolean add(Edge edge) {
    		if (map.containsKey(edge)) return false;
    		return addEdgeSorted(edge, list, map, _cellLimit);
    	}
    	List<Sign> getSignsSorted() {
        	List<Sign> retval = new ArrayList<Sign>(list.size());
        	for (Edge e : list) retval.add(e.sign);
        	return retval;
    	}
    	SignHash getSigns() {
    		SignHash retval = new SignHash();
    		for (Edge e : list) retval.insert(e.sign);
        	return retval;
    	}
    };	
    
    // adds edge to sorted list and optional map, preserving limit; returns true iff edge added
    // nb: all lexical edges kept
    private boolean addEdgeSorted(Edge edge, List<Edge> list, Map<Edge,Edge> map, int limit) {
		int index = Collections.binarySearch(list, edge, edgeComparator);
		// convert index to insertion point
        index = Math.abs(index) - 1;
        // if somehow negative, use last position
        if (index < 0) index = list.size();
        // check if last and at limit
        boolean limitActive = limit > 0 && !edge.sign.isLexical();
        if (limitActive && index >= limit) return false;
        // otherwise add edge
        list.add(index, edge);
		if (map != null) map.put(edge, edge);
		// remove last if over limit
		if (limitActive && list.size() > limit) {
			Edge last = list.remove(list.size()-1);
			if (map != null) map.remove(last);
		}
		return true;    	
    }
    
    /** Compares edges based on their relative score, in descending order, then their signs. */
	public static final Comparator<Edge> edgeComparator = new Comparator<Edge>() {
		public int compare(Edge edge1, Edge edge2) {
			if (edge1.score != edge2.score)
				return -1 * Double.compare(edge1.score, edge2.score);
			else 
				return SignHash.compareTo(edge1.sign, edge2.sign);
		}
	};

    /** The chart. */
	protected Cell[][] _table;

	/** Its size. */
	protected int _size;

	/** The count of edges created before unpacking. */
	protected int _numEdges = 0;
	
	/** The count of edges created while unpacking. */
	protected int _numUnpackingEdges = 0;
	
	/** The max cell size before unpacking. */
	protected int _maxCellSize = 0;
	
	/** The rules. */
	protected RuleGroup _rules;

	/** The sign scorer (defaults to the null scorer). */
	protected SignScorer _signScorer = SignScorer.nullScorer;
	
	/** The "n" for n-best pruning (or 0 if none). */
	protected int _pruneVal = 0;
	
	/** The time limit (0 if none). */
	protected int _timeLimit = 0;
	
	/** The start time. */
	protected long _startTime = 0;
	
	/** The edge limit (0 if none). */
	protected int _edgeLimit = 0;
	
	/** The cell limit on non-lexical edges (0 if none). */
	protected int _cellLimit = 0;
	
	/** Constructor. */
	public Chart(int s, RuleGroup _R) {
		_rules = _R; _size = s;
		_table = new Cell[_size][_size];
	}

	/** Sets the sign scorer. */
	public void setSignScorer(SignScorer signScorer) { _signScorer = signScorer; }
	
	/** Sets the n-best pruning val. */
	public void setPruneVal(int n) { _pruneVal = n; }
	
	/** Sets the time limit. */
	public void setTimeLimit(int timeLimit) { _timeLimit = timeLimit; }
	
	/** Sets the start time. */
	public void setStartTime(long startTime) { _startTime = startTime; }
	
	/** Sets the edge limit. */
	public void setEdgeLimit(int edgeLimit) { _edgeLimit = edgeLimit; }
	
	/** Sets the cell limit on non-lexical edges. */
	public void setCellLimit(int cellLimit) { _cellLimit = cellLimit; }
	
	/** Returns the edge count prior to unpacking. */
	public int edgeCount() { return _numEdges; }
	
	/** Returns the edge count while unpacking. */
	public int unpackingEdgeCount() { return _numUnpackingEdges; }
	
	/** Returns the max cell size prior to unpacking. */
	public int maxCellSize() { return _maxCellSize; }
	
	
	//-----------------------------------------------------------
	// Chart construction
	
	/** 
	 * Inserts a sign at the given cell (modulo pruning).  
	 * Returns true if an edge for the sign is added as a new equiv class. 
	 */
	public boolean insert(int x, int y, Sign w) {
		Cell cell = get(x, y);
		boolean retval = false;
		// make edge
		Edge edge = new Edge(w);
		if (w.isLexical()) edge.setWordPos(x);
		// get representative edge
		Edge rep = cell.get(edge);
		// if none, add as representative
		if (rep == null) {
			edge.initAltEdges();
			retval = cell.add(edge);
		}
		// otherwise add as an alternative
		else {
			addEdgeSorted(edge, rep.altEdges, null, _pruneVal);
		}
		// update edge count, max cell size
		_numEdges++;
		if (cell.size() > _maxCellSize) _maxCellSize = cell.size();
		// done
		return retval;
	}

	/** Returns the given cell (ensuring non-null). */
	protected Cell get(int x, int y) {
		if (_table[x][y] == null) _table[x][y] = new Cell();
		return _table[x][y];
	}

	/** Returns the signs for a given cell (ensuring non-null). */
	protected SignHash getSigns(int x, int y) {
		Cell cell = get(x, y);
		return cell.getSigns();
	}

	/** Inserts edges into (x,y) that result from applying unary rules to those already in (x,y). 
	 * @throws ParseException */ 
	protected void insertCell(int x, int y) throws ParseException {
		if (_table[x][y] == null) return;
		List<Sign> inputs = _table[x][y].getSignsSorted();
		List<Sign> nextInputs = new ArrayList<Sign>(inputs.size());
		// repeat until no more inputs
		while (inputs.size() > 0) {
			// apply rules
			for (Sign sign : inputs) {
				checkLimits();
				List<Sign> results = _rules.applyUnaryRules(sign);
				for (Sign result : results) {
					// check for unary rule cycle; skip result if found
					if (!result.getDerivationHistory().containsCycle()) {
						// insert result
						boolean newEdgeClass = insert(x, y, result);
						// add to next inputs if it yielded a new equiv class
						if (newEdgeClass) nextInputs.add(result);
					}
				}
			}
			// move all results to inputs
			inputs.clear();
			inputs.addAll(nextInputs);
			nextInputs.clear();
		}
	}

	/** Inserts edges into (x3,y3) resulting from combining those in (x1,y1) and (x2,y2). 
	 * @throws ParseException */
	protected void insertCell(int x1, int y1, int x2, int y2, int x3, int y3) throws ParseException {
		if (_table[x1][y1] == null) return;
		if (_table[x2][y2] == null) return;
		List<Sign> inputs1 = _table[x1][y1].getSignsSorted();
		List<Sign> inputs2 = _table[x2][y2].getSignsSorted();
		for (Sign sign1 : inputs1) {
			for (Sign sign2 : inputs2) {
				checkLimits();
				List<Sign> results = _rules.applyBinaryRules(sign1, sign2);
				for (Sign result : results)
					insert(x3, y3, result);
			}
		}
	}

	/** 
	 * Inserts fragmentary edges into (x3,y3), if non-empty, resulting from combining 
	 * those in (x1,y1) and (x2,y2) using the glue rule. 
	 * @throws ParseException 
	 */
	protected void insertCellFrag(int x1, int y1, int x2, int y2, int x3, int y3) throws ParseException {
		if (_table[x1][y1] == null) return;
		if (_table[x2][y2] == null) return;
		if (!cellIsEmpty(x3, y3)) return;
		List<Sign> inputs1 = _table[x1][y1].getSignsSorted();
		List<Sign> inputs2 = _table[x2][y2].getSignsSorted();
		for (Sign sign1 : inputs1) {
			for (Sign sign2 : inputs2) {
				checkLimits();
				List<Sign> results = _rules.applyGlueRule(sign1, sign2);
				for (Sign result : results)
					insert(x3, y3, result);
			}
		}
	}

    // check edge and time limit
    private void checkLimits() throws ParseException {
    	if (_edgeLimit > 0 && _numEdges > _edgeLimit) {
    		throw new ParseException(ParseException.EDGE_LIMIT_EXCEEDED);
    	}
        if (_timeLimit > 0) {
        	int timeSoFar = (int) (System.currentTimeMillis() - _startTime);
        	if (timeSoFar > _timeLimit) {
        		throw new ParseException(ParseException.TIME_LIMIT_EXCEEDED);
        	}
        }
    }
    
	/** Returns whether the given cell is empty. */
    public boolean cellIsEmpty(int x, int y) {
		Cell cell = get(x, y);
    	return cell.list.isEmpty();
    }
    
    
	//-----------------------------------------------------------
	// Unpacking 
	
	/** Unpacks the edges in the given cell as an n-best list. */
	public List<Edge> unpack(int x, int y) {
		Cell cell = get(x, y);
		// recursively unpack each edge
	    @SuppressWarnings("unchecked")
        Set<Edge> unpacked = new THashSet(new TObjectIdentityHashingStrategy());
	    @SuppressWarnings("unchecked")
        Set<Edge> startedUnpacking = new THashSet(new TObjectIdentityHashingStrategy());
		for (Edge edge : cell.list) unpack(edge, unpacked, startedUnpacking); 
		// collect and sort results
        EdgeHash merged = new EdgeHash();
		for (Edge edge : cell.list) {
			merged.addAll(edge.altEdges);
		}
		List<Edge> retval = new ArrayList<Edge>(merged.asEdgeSet());
		Collections.sort(retval, edgeComparator);
        // prune
        if (_pruneVal > 0) {
        	while (retval.size() > _pruneVal) retval.remove(retval.size()-1);
        }
        // restore alts
        for (Edge edge : cell.list) edge.restoreAltEdges();
		// return
		return retval;
	}

	// recursively unpack edge, unless already visited
	private void unpack(Edge edge, Set<Edge> unpacked, Set<Edge> startedUnpacking) {
		// check visited
        if (unpacked.contains(edge)) return;
        if (startedUnpacking.contains(edge)) {
        	System.err.println("Warning, revisiting edge before unpacking complete: " + edge);
        	System.err.println(edge.sign.getDerivationHistory().toString());
        	return;
        }
        startedUnpacking.add(edge);
        // OR: recursively unpack alts, merging resulting alts
        EdgeHash merged = new EdgeHash();
        for (Edge alt : edge.altEdges) {
            // AND: unpack inputs, make alts, add to merged
            unpackAlt(alt, unpacked, startedUnpacking, merged);
        }
        // score
        boolean complete = (edge.sign.getWords().size() == _size);
        for (Edge m : merged.asEdgeSet()) { m.setScore(_signScorer.score(m.sign, complete)); }
        // sort
        List<Edge> mergedList = new ArrayList<Edge>(merged.asEdgeSet());
        Collections.sort(mergedList, edgeComparator);
        // prune
        if (_pruneVal > 0) {
        	while (mergedList.size() > _pruneVal) mergedList.remove(mergedList.size()-1);
        }
        // replace edge's alts
        edge.replaceAltEdges(mergedList);
        // add to unpacked set
        unpacked.add(edge);
    }
    
    // recursively unpack inputs, make alt combos and add to merged
    private void unpackAlt(Edge alt, Set<Edge> unpacked, Set<Edge> startedUnpacking, EdgeHash merged) {
        // unpack via input signs
        DerivationHistory history = alt.sign.getDerivationHistory(); 
        Sign[] inputSigns = history.getInputs();
        // base case: no inputs
        if (inputSigns == null) {
            merged.insert(alt); return;
        }
        // otherwise recursively unpack
        Edge[] inputEdges = new Edge[inputSigns.length];
        for (int i = 0; i < inputSigns.length; i++) {
            inputEdges[i] = Edge.getEdge(inputSigns[i]); 
            unpack(inputEdges[i], unpacked, startedUnpacking);
        }
        // then make edges for new combos, using same rule, and add to merged (if unseen)
        Rule rule = history.getRule();
        List<Sign[]> altCombos = inputCombos(inputEdges, 0);
        List<Sign> results = new ArrayList<Sign>(1);
        for (Sign[] combo : altCombos) {
        	// use this alt for same combo
        	if (sameSigns(inputSigns, combo)) {
        		merged.insert(alt); continue;
        	}
        	results.clear();
        	((AbstractRule)rule).applyRule(combo, results); // TODO: bypass rule app for efficiency? (requires doing something about var subst)
        	if (results.isEmpty()) continue; // (rare?)
            Sign sign = results.get(0); // assuming single result
            merged.insert(new Edge(sign)); // make edge for new alt
            _numUnpackingEdges++;
        }
	}
	
    // returns a list of sign arrays, with each array of length inputEdges.length - i, 
    // representing all combinations of alt signs from i onwards
    private List<Sign[]> inputCombos(Edge[] inputEdges, int index) {
        Edge edge = inputEdges[index];
        // base case, inputEdges[last]
        if (index == inputEdges.length-1) {
            List<Edge> altEdges = edge.altEdges; 
            List<Sign[]> retval = new ArrayList<Sign[]>(altEdges.size());
            for (Edge alt : altEdges) {
                retval.add(new Sign[] { alt.sign });
            }
            return retval;
        }
        // otherwise recurse on index+1
        List<Sign[]> nextCombos = inputCombos(inputEdges, index+1);
        // and make new combos
        List<Edge> altEdges = edge.altEdges; 
        List<Sign[]> retval = new ArrayList<Sign[]>(altEdges.size() * nextCombos.size());
        for (Edge alt : altEdges) {
            for (int i = 0; i < nextCombos.size(); i++) {
                Sign[] nextSigns = nextCombos.get(i);
                Sign[] newCombo = new Sign[nextSigns.length+1];
                newCombo[0] = alt.sign;
                System.arraycopy(nextSigns, 0, newCombo, 1, nextSigns.length);
                retval.add(newCombo);
            }
        }
        return retval;
    }

    // checks for same signs
    private boolean sameSigns(Sign[] a, Sign[] b) {
    	if (a.length != b.length) return false;
    	for (int i=0; i < a.length; i++)
    		if (a[i] != b[i]) return false;
    	return true;
    }
    
	
	//-----------------------------------------------------------
	// Lazy Unpacking 
	
	/** 
	 * Lazily unpacks the edges in the given cell as an n-best list 
	 * using a variant of "cube pruning".  The algorithm essentially 
	 * follows Algorithm 2 of Huang and Chiang (2005), with checking 
	 * for spurious ambiguity.
	 */ 
	@SuppressWarnings("unchecked")
	public List<Edge> lazyUnpack(int x, int y) {
		// if no pruning value set, use basic unpacking algorithm
		if (_pruneVal <= 0) return unpack(x, y);
		// recursively sort edge alts
		Cell cell = get(x, y);
		// make top-level candidate list and derivs map
		List<Candidate> topcands = new ArrayList<Candidate>(_pruneVal);
		Map<Edge, List<Edge>> derivsmap = new THashMap(new TObjectIdentityHashingStrategy());
		for (Edge edge : cell.list) {
			List<Candidate> cands = getCandidates(edge, derivsmap);
			topcands.addAll(cands);
		}
		sortAndPrune(topcands);
		// NB: no single edge for top cell, so must treat it as a special case of findKBest
		List<Edge> retval = new ArrayList<Edge>(_pruneVal);
    	EdgeHash merged = new EdgeHash();
    	while (merged.size() < _pruneVal && !topcands.isEmpty()) {
    		appendNext(topcands, merged, derivsmap);
    	}
    	retval.addAll(merged.asEdgeSet());
    	// rescore edges if apropos
    	if (_signScorer instanceof ReRankingScorer) {
    		ReRankingScorer rescorer = (ReRankingScorer) _signScorer;
    		rescorer.setFullModel(true);
    		for (Edge e : retval) {
    			e.score = rescorer.score(e.sign, true);
    		}
    		rescorer.setFullModel(false);
    	}
    	Collections.sort(retval, edgeComparator);
		// done
		return retval;
	}
	
    // lazily find k-best derivations, if edge not already visited
    private void findKBest(Edge edge, Map<Edge, List<Edge>> derivsmap) {
    	if (derivsmap.containsKey(edge)) return;
    	List<Candidate> cands = getCandidates(edge, derivsmap);
    	EdgeHash merged = new EdgeHash();
    	while (merged.size() < _pruneVal && !cands.isEmpty()) {
    		appendNext(cands, merged, derivsmap);
    	}
    	List<Edge> derivs = new ArrayList<Edge>(_pruneVal);
    	derivs.addAll(merged.asEdgeSet());
    	Collections.sort(derivs, edgeComparator);
    	derivsmap.put(edge, derivs);
    }
    
    // appends next candidate, expands frontier
    private void appendNext(List<Candidate> cands, EdgeHash merged, Map<Edge, List<Edge>> derivsmap) {
    	// append next
    	Candidate cand = cands.remove(0);
    	merged.add(cand.edge);
		// check for lex cand
		if (cand.indices == null) return;
		// enumerate frontier
		for (int i=0; i < cand.indices.length; i++) {
			// inc nextIndices at i
			int[] nextIndices = new int[cand.indices.length];
			for (int m=0; m < nextIndices.length; m++) nextIndices[m] = cand.indices[m];
			nextIndices[i]++;
			Edge next = getEdgeForIndices(cand.edge, cand.inputReps, nextIndices, derivsmap); 
			// add next candidate, if any, if not already there
			if (next != null) {
				Candidate nextCand = new Candidate(next, cand.inputReps, nextIndices);
				if (!cands.contains(nextCand)) {
					int index = Collections.binarySearch(cands, nextCand);
		            index = Math.abs(index) - 1; // convert index to insertion point
		            if (index >= 0) 
		            	cands.add(index, nextCand);
		            else cands.add(nextCand);
				}
			}
		}
    }
    
	// candidate is an edge plus an array of indices for keeping track of 
	// where to pull candidates from next (or null if lexical),  
    // using the input representatives
	private static class Candidate implements Comparable<Candidate> {
		Edge edge; Edge[] inputReps; int[] indices;
		Candidate(Edge edge, Edge[] inputReps, int[] indices) { 
			this.edge = edge; this.inputReps = inputReps; this.indices = indices; 
		}
		public int compareTo(Candidate c) { 
			int retval = edgeComparator.compare(edge, c.edge);
			if (retval != 0) return retval;
			if (indices == null && c.indices == null) return 0;
			if (indices == null && c.indices != null) return -1;
			if (indices != null && c.indices == null) return 1;
			if (indices.length < c.indices.length) return -1;
			if (indices.length > c.indices.length) return 1;
			for (int i=0; i < indices.length; i++) {
				if (indices[i] < c.indices[i]) return -1;
				if (indices[i] > c.indices[i]) return 1;
			}
			return 0;
		}
		public boolean equals(Object o) {
			if (!(o instanceof Candidate)) return false;
			Candidate c = (Candidate)o;
			if (indices != null && c.indices == null) return false;
			if (indices == null && c.indices != null) return false;
			if (indices != null && c.indices != null) {
				if (indices.length != c.indices.length) return false;
				for (int i=0; i < indices.length; i++) {
					if (indices[i] != c.indices[i]) return false;
				}
			}
			return edge.equals(c.edge);
		}
	}
	
	// get candidates for unpacking an edge
	private List<Candidate> getCandidates(Edge edge, Map<Edge, List<Edge>> derivsmap) {
		List<Candidate> retval = new ArrayList<Candidate>(_pruneVal);
		// make initial candidate for each alt
		// nb: should only get initial candidates for representative edges, 
		//     but may as well ensure that at least this edge is included
		List<Edge> alts = new ArrayList<Edge>(edge.getAltEdges());
		if (alts.isEmpty()) alts.add(edge);
		for (Edge alt : alts) {
			Sign[] inputs = alt.sign.getDerivationHistory().getInputs();
			// lex case: no indices
			if (inputs == null) {
				retval.add(new Candidate(alt, null, null));
				continue;
			}
			// otherwise get edge for best inputs
			Edge[] inputReps = new Edge[inputs.length];
			int[] indices = new int[inputs.length];
			for (int i=0; i < inputs.length; i++) {
				inputReps[i] = Edge.getEdge(inputs[i]);
				indices[i] = 0;
			}
			Edge e = getEdgeForIndices(alt, inputReps, indices, derivsmap);
			if (e != null) {
				retval.add(new Candidate(e, inputReps, indices));
			}
		}
		// sort and prune
		sortAndPrune(retval);
		// done
		return retval;
	}
    
	// returns the edge for the given input indices, or null if none
	private Edge getEdgeForIndices(Edge edge, Edge[] inputReps, int[] indices, Map<Edge, List<Edge>> derivsmap) {
		DerivationHistory history = edge.sign.getDerivationHistory();
        Sign[] combo = new Sign[inputReps.length];
        for (int i = 0; i < inputReps.length; i++) {
            Edge inputEdge = inputReps[i];
            // recurse
            findKBest(inputEdge, derivsmap);
            // get derivs
            List<Edge> inputDerivs = derivsmap.get(inputEdge);
            // check index, return null if out of bounds
            if (indices[i] < inputDerivs.size()) 
            	combo[i] = inputDerivs.get(indices[i]).sign;
            else return null;
        }
        // return edge if combo is same as input signs
		Sign[] inputSigns = history.getInputs();
        if (sameSigns(inputSigns, combo)) return edge;
        // otherwise return new edge for combo
        Rule rule = history.getRule();
        List<Sign> results = new ArrayList<Sign>(1);
    	((AbstractRule)rule).applyRule(combo, results); // TODO: bypass rule app for efficiency? (requires doing something about var subst)
    	if (results.isEmpty()) return null; // (rare?)
        Sign sign = results.get(0); // assuming single result
        Edge retval = new Edge(sign); // make edge for new combo
        _numUnpackingEdges++;
        // score it
        boolean complete = (sign.getWords().size() == _size);
        retval.setScore(_signScorer.score(sign, complete));
        // done 
		return retval;
	}
	
	// sort and prune candidate list
	private void sortAndPrune(List<Candidate> cands) {
		Collections.sort(cands);
		while (cands.size() > _pruneVal) cands.remove(cands.size()-1);
	}
	
    
	//-----------------------------------------------------------

	/** Saves the chart entries to the given file. */
	public void saveChartEntries(File file) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		out.writeObject(_table);
		out.flush(); out.close();
	}
	
	/** Loads the chart entries from the given file. */
	public void loadChartEntries(File file) throws IOException {
		ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
		try {
			// read entries
			_table = (Cell[][]) in.readObject();
			// restore size, unpacking edge count
			_size = _table.length;
			_numUnpackingEdges = 0;
		} catch (ClassNotFoundException e) {
			in.close();
            throw (RuntimeException) new RuntimeException().initCause(e);
		}
		in.close();
	}
	
	
	//-----------------------------------------------------------
    
	/** Returns the number of entries in each cell in the chart. */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < _size; i++) {
			for (int j = 0; j < _size; j++) {
				sb.append(get(i, j).size()).append('\t');
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	/** Prints the signs in the chart to System.out. */
	public void printChart() {
		int[] sizes = new int[_size];
		int rows = 0;
		for (int i = 0; i < _size; i++) {
			for (int j = i; j < _size; j++)
				if (get(i, j).size() > sizes[i])
					sizes[i] = get(i, j).size();
			rows += sizes[i];
		}

		String[][] toprint = new String[rows][_size];
		String[] words = new String[_size];
		int maxwidth = 0;

		for (int i = 0, row = 0; i < _size; row += sizes[i++]) {
			for (int j = 0; j < _size; j++)
				for (int s = 0; s < sizes[i]; s++) {
					SignHash cell = getSigns(i, j);
					if (i == j) words[i] = cell.asSignSet().iterator().next().getOrthography();
					if (cell.size() >= s + 1) {
						toprint[row + s][j] = ((Sign) cell.toArray()[s])
								.getCategory().toString();
						if (toprint[row + s][j].length() > maxwidth)
							maxwidth = toprint[row + s][j].length();
					}
				}
		}

		int fullwidth = _size * (maxwidth + 3) - 1;
		System.out.print(" ");
		for (String w : words) {
			System.out.print(w);
			int pad = (maxwidth + 3) - w.length();
			for (int p = 0; p < pad; p++)
				System.out.print(" ");
		}
		System.out.print("|");
		System.out.println();
		for (int p = 0; p < fullwidth; p++)
			System.out.print("-");
		System.out.print("| ");
		System.out.println();

		for (int i = 0, entry = sizes[0], e = 0; i < rows; i++) {
			if (i == entry) {
				System.out.print("|");
				for (int p = 0; p < fullwidth; p++)
					System.out.print("-");
				System.out.print("|");
				System.out.println();
				entry += sizes[++e];
			}
			System.out.print("| ");

			for (int j = 0; j < _size; j++) {
				int pad = 1 + maxwidth;
				if (toprint[i][j] != null) {
					System.out.print(toprint[i][j]);
					pad -= toprint[i][j].length();
				}
				for (int p = 0; p < pad; p++)
					System.out.print(" ");
				System.out.print("| ");
			}
			System.out.println();
		}
		System.out.print("|");
		for (int p = 0; p < fullwidth; p++)
			System.out.print("-");
		System.out.print("| ");
		System.out.println();
	}
}
