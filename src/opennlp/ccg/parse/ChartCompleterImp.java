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
 * Special functions are provided for combining forms of the chart into another
 * form. Time or scoredSymbol or form limits can be placed on initial chart
 * construction. A pruning value applies to unpacking, which also limits the
 * number of equivalent scoredSymbols kept during chart construction.
 * 
 * @author Jason Baldridge
 * @author Gann Bierner
 * @author Michael White
 * @version $Revision: 1.41 $, $Date: 2011/11/16 03:25:27 $
 */
public class ChartCompleterImp implements ChartCompleter {

	/**
	 * Compares scoredSymbols based on their relative score, in descending order, then
	 * their signs.
	 */
	public static final Comparator<ScoredSymbol> scoredSymbolComparator = new Comparator<ScoredSymbol>() {
		public int compare(ScoredSymbol scoredSymbol1, ScoredSymbol scoredSymbol2) {
			if (scoredSymbol1.score != scoredSymbol2.score)
				return -1 * Double.compare(scoredSymbol1.score, scoredSymbol2.score);
			else
				return SymbolHash.compareTo(scoredSymbol1.symbol, scoredSymbol2.symbol);
		}
	};

	/** Its size. */
	protected int _size;

	/** The count of scoredSymbols created before unpacking. */
	protected int _numEdges = 0;

	/** The count of scoredSymbols created while unpacking. */
	protected int _numUnpackingEdges = 0;

	/** The max form size before unpacking. */
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

	/** The scoredSymbol limit (0 if none). */
	protected int _scoredSymbolLimit = 0;

	/** The form limit on non-lexical scoredSymbols (0 if none). */
	protected int formLimit = 0;

	/**
	 * The chart to build.
	 */
	private Chart chart;

	/** Constructor. */
	public ChartCompleterImp(int size, RuleGroup _R) {
		_rules = _R;
		_size = size;
		chart = new SparseChart(size);
	}

	/** Sets the sign scorer. */
	public void setSignScorer(SignScorer signScorer) {
		_signScorer = signScorer;
	}

	/** Sets the n-best pruning val. */
	public void setPruneValue(int n) {
		_pruneVal = n;
	}

	/** Sets the time limit. */
	public void setParseTimeLimit(int timeLimit) {
		_timeLimit = timeLimit;
	}

	/** Sets the start time. */
	public void setStartTime(long startTime) {
		_startTime = startTime;
	}

	/** Sets the scoredSymbol limit. */
	public void setEdgeLimit(int scoredSymbolLimit) {
		_scoredSymbolLimit = scoredSymbolLimit;
	}

	/** Sets the form limit on non-lexical scoredSymbols. */
	public void setCellPruneValue(int formLimit) {
		this.formLimit = formLimit;
	}

	/** Returns the scoredSymbol count prior to unpacking. */
	public int getEdgeCount() {
		return _numEdges;
	}

	/** Returns the scoredSymbol count while unpacking. */
	public int getUnpackingEdgeCount() {
		return _numUnpackingEdges;
	}

	/** Returns the max form size prior to unpacking. */
	public int getMaxCellSize() {
		return _maxCellSize;
	}

	// -----------------------------------------------------------
	// Chart construction

	/**
	 * Makes a form starting at a x1 position and ending at x2 position.
	 * 
	 * @param x1 the x1 position
	 * @param x2 the x2 position
	 * @return the form starting at the start position with the given length
	 */
	protected Form makeForm(int x1, int x2) {
		if (chart.getForm(x1, x2) == null) {
			chart.setForm(x1, x2, new Form(formLimit, scoredSymbolComparator));
		}
		return chart.getForm(x1, x2);
	}

	/**
	 * Gets signs for a given form (ensuring non-null).
	 */
	protected SymbolHash getSigns(int x1, int x2) {
		Form form = makeForm(x1, x2);
		return form.getSymbols();
	}

	@Override
	public boolean annotateForm(int x1, int x2, Symbol symbol) {
		Form form = makeForm(x1, x2);
		boolean retval = false;
		// make scoredSymbol
		ScoredSymbol scoredSymbol = new ScoredSymbol(symbol);
		if (symbol.isIndexed()) {
			scoredSymbol.setWordPos(x1);
		}
		// get representative scoredSymbol
		ScoredSymbol rep = form.get(scoredSymbol);
		// if none, add as representative
		if (rep == null) {
			scoredSymbol.initAltEdges();
			retval = form.add(scoredSymbol);
		}
		// otherwise add as an alternative
		else {
			Form.insertScoredSymbol(scoredSymbol, rep.alternatives, null, _pruneVal, scoredSymbolComparator);
		}
		// update scoredSymbol count, max form size
		_numEdges++;
		if (form.size() > _maxCellSize)
			_maxCellSize = form.size();
		// done
		return retval;
	}

	@Override
	public final void annotateForm(int x1, int x2) throws ParseException {
		if (chart.getForm(x1, x2) == null)
			return;
		List<Symbol> inputs = chart.getForm(x1, x2).sortSymbols();
		List<Symbol> nextInputs = new ArrayList<Symbol>(inputs.size());
		// repeat until no more inputs
		while (inputs.size() > 0) {
			// apply rules
			for (Symbol sign : inputs) {
				checkLimits();
				List<Symbol> results = _rules.applyUnaryRules(sign);
				for (Symbol result : results) {
					// check for unary rule cycle; skip result if found
					if (!result.getDerivationHistory().containsCycle()) {
						// insert result
						boolean newEdgeClass = annotateForm(x1, x2, result);
						// add to next inputs if it yielded a new equiv class
						if (newEdgeClass)
							nextInputs.add(result);
					}
				}
			}
			// move all results to inputs
			inputs.clear();
			inputs.addAll(nextInputs);
			nextInputs.clear();
		}
	}

	@Override
	public void combineForms(int x1, int y1, int x2, int y2, int x3, int y3) throws ParseException {
		if (chart.getForm(x1, y1) == null)
			return;
		if (chart.getForm(x2, y2) == null)
			return;
		List<Symbol> inputs1 = chart.getForm(x1, y1).sortSymbols();
		List<Symbol> inputs2 = chart.getForm(x2, y2).sortSymbols();
		for (Symbol sign1 : inputs1) {
			for (Symbol sign2 : inputs2) {
				checkLimits();
				List<Symbol> results = _rules.applyBinaryRules(sign1, sign2);
				for (Symbol result : results)
					annotateForm(x3, y3, result);
			}
		}
	}

	@Override
	public void glueForms(int x1, int y1, int x2, int y2, int x3, int y3)
			throws ParseException {
		if (chart.getForm(x1, y1) == null)
			return;
		if (chart.getForm(x2, y2) == null)
			return;
		if (!isEmpty(x3, y3))
			return;
		List<Symbol> inputs1 = chart.getForm(x1, y1).sortSymbols();
		List<Symbol> inputs2 = chart.getForm(x2, y2).sortSymbols();
		for (Symbol sign1 : inputs1) {
			for (Symbol sign2 : inputs2) {
				checkLimits();
				List<Symbol> results = _rules.applyGlueRule(sign1, sign2);
				for (Symbol result : results)
					annotateForm(x3, y3, result);
			}
		}
	}

	/**
	 * Checks limits
	 * 
	 * @throws ParseException if limits are exceeded
	 */
	private final void checkLimits() throws ParseException {
		if (_scoredSymbolLimit > 0 && _numEdges > _scoredSymbolLimit) {
			throw new ParseException(ParseException.EDGE_LIMIT_EXCEEDED);
		}
		if (_timeLimit > 0) {
			int timeSoFar = (int) (System.currentTimeMillis() - _startTime);
			if (timeSoFar > _timeLimit) {
				throw new ParseException(ParseException.TIME_LIMIT_EXCEEDED);
			}
		}
	}

	@Override
	public boolean isEmpty(int x1, int x2) {
		Form form = chart.getForm(x1, x2);
		if (form == null) {
			return true;
		} else {
			return !form.hasSymbols();
		}
	}

	// -----------------------------------------------------------
	// Unpacking

	/** Unpacks the scoredSymbols in the given form as an n-best list. */
	public List<ScoredSymbol> unpack(int x, int y) {
		Form form = makeForm(x, y);
		// recursively unpack each scoredSymbol
		@SuppressWarnings("unchecked")
		Set<ScoredSymbol> unpacked = new THashSet(new TObjectIdentityHashingStrategy());
		@SuppressWarnings("unchecked")
		Set<ScoredSymbol> startedUnpacking = new THashSet(new TObjectIdentityHashingStrategy());
		for (ScoredSymbol scoredSymbol : form.getScoredSymbols())
			unpack(scoredSymbol, unpacked, startedUnpacking);
		// collect and sort results
		EdgeHash merged = new EdgeHash();
		for (ScoredSymbol scoredSymbol : form.getScoredSymbols()) {
			merged.addAll(scoredSymbol.alternatives);
		}
		List<ScoredSymbol> retval = new ArrayList<ScoredSymbol>(merged.asEdgeSet());
		Collections.sort(retval, scoredSymbolComparator);
		// prune
		if (_pruneVal > 0) {
			while (retval.size() > _pruneVal)
				retval.remove(retval.size() - 1);
		}
		// restore alts
		for (ScoredSymbol scoredSymbol : form.getScoredSymbols())
			scoredSymbol.restoreAltEdges();
		// return
		return retval;
	}

	// recursively unpack scoredSymbol, unless already visited
	private void unpack(ScoredSymbol scoredSymbol, Set<ScoredSymbol> unpacked, Set<ScoredSymbol> startedUnpacking) {
		// check visited
		if (unpacked.contains(scoredSymbol))
			return;
		if (startedUnpacking.contains(scoredSymbol)) {
			System.err.println("Warning, revisiting scoredSymbol before unpacking complete: " + scoredSymbol);
			System.err.println(scoredSymbol.symbol.getDerivationHistory().toString());
			return;
		}
		startedUnpacking.add(scoredSymbol);
		// OR: recursively unpack alts, merging resulting alts
		EdgeHash merged = new EdgeHash();
		for (ScoredSymbol alt : scoredSymbol.alternatives) {
			// AND: unpack inputs, make alts, add to merged
			unpackAlt(alt, unpacked, startedUnpacking, merged);
		}
		// score
		boolean complete = (scoredSymbol.symbol.getWords().size() == _size);
		for (ScoredSymbol m : merged.asEdgeSet()) {
			m.setScore(_signScorer.score(m.symbol, complete));
		}
		// sort
		List<ScoredSymbol> mergedList = new ArrayList<ScoredSymbol>(merged.asEdgeSet());
		Collections.sort(mergedList, scoredSymbolComparator);
		// prune
		if (_pruneVal > 0) {
			while (mergedList.size() > _pruneVal)
				mergedList.remove(mergedList.size() - 1);
		}
		// replace scoredSymbol's alts
		scoredSymbol.replaceAltEdges(mergedList);
		// add to unpacked set
		unpacked.add(scoredSymbol);
	}

	// recursively unpack inputs, make alt combos and add to merged
	private void unpackAlt(ScoredSymbol alt, Set<ScoredSymbol> unpacked, Set<ScoredSymbol> startedUnpacking, EdgeHash merged) {
		// unpack via input signs
		DerivationHistory history = alt.symbol.getDerivationHistory();
		Symbol[] inputSigns = history.getInputs();
		// base case: no inputs
		if (inputSigns == null) {
			merged.insert(alt);
			return;
		}
		// otherwise recursively unpack
		ScoredSymbol[] inputEdges = new ScoredSymbol[inputSigns.length];
		for (int i = 0; i < inputSigns.length; i++) {
			inputEdges[i] = ScoredSymbol.getScoredSymbol(inputSigns[i]);
			unpack(inputEdges[i], unpacked, startedUnpacking);
		}
		// then make scoredSymbols for new combos, using same rule, and add to merged
		// (if unseen)
		Rule rule = history.getRule();
		List<Symbol[]> altCombos = inputCombos(inputEdges, 0);
		List<Symbol> results = new ArrayList<Symbol>(1);
		for (Symbol[] combo : altCombos) {
			// use this alt for same combo
			if (sameSigns(inputSigns, combo)) {
				merged.insert(alt);
				continue;
			}
			results.clear();
			((AbstractRule) rule).applyRule(combo, results); // TODO: bypass
																// rule app for
																// efficiency?
																// (requires
																// doing
																// something
																// about var
																// subst)
			if (results.isEmpty())
				continue; // (rare?)
			Symbol sign = results.get(0); // assuming single result
			merged.insert(new ScoredSymbol(sign)); // make scoredSymbol for new alt
			_numUnpackingEdges++;
		}
	}

	// returns a list of sign arrays, with each array of length
	// inputEdges.length - i,
	// representing all combinations of alt signs from i onwards
	private List<Symbol[]> inputCombos(ScoredSymbol[] inputEdges, int index) {
		ScoredSymbol scoredSymbol = inputEdges[index];
		// base case, inputEdges[last]
		if (index == inputEdges.length - 1) {
			List<ScoredSymbol> altEdges = scoredSymbol.alternatives;
			List<Symbol[]> retval = new ArrayList<Symbol[]>(altEdges.size());
			for (ScoredSymbol alt : altEdges) {
				retval.add(new Symbol[] { alt.symbol });
			}
			return retval;
		}
		// otherwise recurse on index+1
		List<Symbol[]> nextCombos = inputCombos(inputEdges, index + 1);
		// and make new combos
		List<ScoredSymbol> altEdges = scoredSymbol.alternatives;
		List<Symbol[]> retval = new ArrayList<Symbol[]>(altEdges.size() * nextCombos.size());
		for (ScoredSymbol alt : altEdges) {
			for (int i = 0; i < nextCombos.size(); i++) {
				Symbol[] nextSigns = nextCombos.get(i);
				Symbol[] newCombo = new Symbol[nextSigns.length + 1];
				newCombo[0] = alt.symbol;
				System.arraycopy(nextSigns, 0, newCombo, 1, nextSigns.length);
				retval.add(newCombo);
			}
		}
		return retval;
	}

	// checks for same signs
	private boolean sameSigns(Symbol[] a, Symbol[] b) {
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; i++)
			if (a[i] != b[i])
				return false;
		return true;
	}

	// -----------------------------------------------------------
	// Lazy Unpacking

	/**
	 * Lazily unpacks the scoredSymbols in the given form as an n-best list using a
	 * variant of "cube pruning". The algorithm essentially follows Algorithm 2
	 * of Huang and Chiang (2005), with checking for spurious ambiguity.
	 */
	@SuppressWarnings("unchecked")
	public List<ScoredSymbol> lazyUnpack(int x, int y) {
		// if no pruning value set, use basic unpacking algorithm
		if (_pruneVal <= 0)
			return unpack(x, y);
		// recursively sort scoredSymbol alts
		Form form = makeForm(x, y);
		// make top-level candidate list and derivs map
		List<Candidate> topcands = new ArrayList<Candidate>(_pruneVal);
		Map<ScoredSymbol, List<ScoredSymbol>> derivsmap = new THashMap(new TObjectIdentityHashingStrategy());
		for (ScoredSymbol scoredSymbol : form.getScoredSymbols()) {
			List<Candidate> cands = getCandidates(scoredSymbol, derivsmap);
			topcands.addAll(cands);
		}
		sortAndPrune(topcands);
		// NB: no single scoredSymbol for top form, so must treat it as a special case
		// of findKBest
		List<ScoredSymbol> retval = new ArrayList<ScoredSymbol>(_pruneVal);
		EdgeHash merged = new EdgeHash();
		while (merged.size() < _pruneVal && !topcands.isEmpty()) {
			appendNext(topcands, merged, derivsmap);
		}
		retval.addAll(merged.asEdgeSet());
		// rescore scoredSymbols if apropos
		if (_signScorer instanceof ReRankingScorer) {
			ReRankingScorer rescorer = (ReRankingScorer) _signScorer;
			rescorer.setFullModel(true);
			for (ScoredSymbol e : retval) {
				e.score = rescorer.score(e.symbol, true);
			}
			rescorer.setFullModel(false);
		}
		Collections.sort(retval, scoredSymbolComparator);
		// done
		return retval;
	}

	// lazily find k-best derivations, if scoredSymbol not already visited
	private void findKBest(ScoredSymbol scoredSymbol, Map<ScoredSymbol, List<ScoredSymbol>> derivsmap) {
		if (derivsmap.containsKey(scoredSymbol))
			return;
		List<Candidate> cands = getCandidates(scoredSymbol, derivsmap);
		EdgeHash merged = new EdgeHash();
		while (merged.size() < _pruneVal && !cands.isEmpty()) {
			appendNext(cands, merged, derivsmap);
		}
		List<ScoredSymbol> derivs = new ArrayList<ScoredSymbol>(_pruneVal);
		derivs.addAll(merged.asEdgeSet());
		Collections.sort(derivs, scoredSymbolComparator);
		derivsmap.put(scoredSymbol, derivs);
	}

	// appends next candidate, expands frontier
	private void appendNext(List<Candidate> cands, EdgeHash merged, Map<ScoredSymbol, List<ScoredSymbol>> derivsmap) {
		// append next
		Candidate cand = cands.remove(0);
		merged.add(cand.scoredSymbol);
		// check for lex cand
		if (cand.indices == null)
			return;
		// enumerate frontier
		for (int i = 0; i < cand.indices.length; i++) {
			// inc nextIndices at i
			int[] nextIndices = new int[cand.indices.length];
			for (int m = 0; m < nextIndices.length; m++)
				nextIndices[m] = cand.indices[m];
			nextIndices[i]++;
			ScoredSymbol next = getEdgeForIndices(cand.scoredSymbol, cand.inputReps, nextIndices, derivsmap);
			// add next candidate, if any, if not already there
			if (next != null) {
				Candidate nextCand = new Candidate(next, cand.inputReps, nextIndices);
				if (!cands.contains(nextCand)) {
					int index = Collections.binarySearch(cands, nextCand);
					index = Math.abs(index) - 1; // convert index to insertion
													// point
					if (index >= 0)
						cands.add(index, nextCand);
					else
						cands.add(nextCand);
				}
			}
		}
	}

	// candidate is an scoredSymbol plus an array of indices for keeping track of
	// where to pull candidates from next (or null if lexical),
	// using the input representatives
	private static class Candidate implements Comparable<Candidate> {
		ScoredSymbol scoredSymbol;
		ScoredSymbol[] inputReps;
		int[] indices;

		Candidate(ScoredSymbol scoredSymbol, ScoredSymbol[] inputReps, int[] indices) {
			this.scoredSymbol = scoredSymbol;
			this.inputReps = inputReps;
			this.indices = indices;
		}

		public int compareTo(Candidate c) {
			int retval = scoredSymbolComparator.compare(scoredSymbol, c.scoredSymbol);
			if (retval != 0)
				return retval;
			if (indices == null && c.indices == null)
				return 0;
			if (indices == null && c.indices != null)
				return -1;
			if (indices != null && c.indices == null)
				return 1;
			if (indices.length < c.indices.length)
				return -1;
			if (indices.length > c.indices.length)
				return 1;
			for (int i = 0; i < indices.length; i++) {
				if (indices[i] < c.indices[i])
					return -1;
				if (indices[i] > c.indices[i])
					return 1;
			}
			return 0;
		}

		public boolean equals(Object o) {
			if (!(o instanceof Candidate))
				return false;
			Candidate c = (Candidate) o;
			if (indices != null && c.indices == null)
				return false;
			if (indices == null && c.indices != null)
				return false;
			if (indices != null && c.indices != null) {
				if (indices.length != c.indices.length)
					return false;
				for (int i = 0; i < indices.length; i++) {
					if (indices[i] != c.indices[i])
						return false;
				}
			}
			return scoredSymbol.equals(c.scoredSymbol);
		}
	}

	// get candidates for unpacking an scoredSymbol
	private List<Candidate> getCandidates(ScoredSymbol scoredSymbol, Map<ScoredSymbol, List<ScoredSymbol>> derivsmap) {
		List<Candidate> retval = new ArrayList<Candidate>(_pruneVal);
		// make initial candidate for each alt
		// nb: should only get initial candidates for representative scoredSymbols,
		// but may as well ensure that at least this scoredSymbol is included
		List<ScoredSymbol> alts = new ArrayList<ScoredSymbol>(scoredSymbol.getAltEdges());
		if (alts.isEmpty())
			alts.add(scoredSymbol);
		for (ScoredSymbol alt : alts) {
			Symbol[] inputs = alt.symbol.getDerivationHistory().getInputs();
			// lex case: no indices
			if (inputs == null) {
				retval.add(new Candidate(alt, null, null));
				continue;
			}
			// otherwise get scoredSymbol for best inputs
			ScoredSymbol[] inputReps = new ScoredSymbol[inputs.length];
			int[] indices = new int[inputs.length];
			for (int i = 0; i < inputs.length; i++) {
				inputReps[i] = ScoredSymbol.getScoredSymbol(inputs[i]);
				indices[i] = 0;
			}
			ScoredSymbol e = getEdgeForIndices(alt, inputReps, indices, derivsmap);
			if (e != null) {
				retval.add(new Candidate(e, inputReps, indices));
			}
		}
		// sort and prune
		sortAndPrune(retval);
		// done
		return retval;
	}

	// returns the scoredSymbol for the given input indices, or null if none
	private ScoredSymbol getEdgeForIndices(ScoredSymbol scoredSymbol, ScoredSymbol[] inputReps, int[] indices,
			Map<ScoredSymbol, List<ScoredSymbol>> derivsmap) {
		DerivationHistory history = scoredSymbol.symbol.getDerivationHistory();
		Symbol[] combo = new Symbol[inputReps.length];
		for (int i = 0; i < inputReps.length; i++) {
			ScoredSymbol inputEdge = inputReps[i];
			// recurse
			findKBest(inputEdge, derivsmap);
			// get derivs
			List<ScoredSymbol> inputDerivs = derivsmap.get(inputEdge);
			// check index, return null if out of bounds
			if (indices[i] < inputDerivs.size())
				combo[i] = inputDerivs.get(indices[i]).symbol;
			else
				return null;
		}
		// return scoredSymbol if combo is same as input signs
		Symbol[] inputSigns = history.getInputs();
		if (sameSigns(inputSigns, combo))
			return scoredSymbol;
		// otherwise return new scoredSymbol for combo
		Rule rule = history.getRule();
		List<Symbol> results = new ArrayList<Symbol>(1);
		((AbstractRule) rule).applyRule(combo, results); // TODO: bypass rule
															// app for
															// efficiency?
															// (requires doing
															// something about
															// var subst)
		if (results.isEmpty())
			return null; // (rare?)
		Symbol sign = results.get(0); // assuming single result
		ScoredSymbol retval = new ScoredSymbol(sign); // make scoredSymbol for new combo
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
		while (cands.size() > _pruneVal)
			cands.remove(cands.size() - 1);
	}

	// -----------------------------------------------------------

	/** Saves the chart entries to the given file. */
	public void saveChartEntries(File file) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(
				new FileOutputStream(file)));
		out.writeObject(chart);
		out.flush();
		out.close();
	}

	/** Loads the chart entries from the given file. */
	public void loadChartEntries(File file) throws IOException {
		chart = new DenseChart(file);
		_numUnpackingEdges = 0;
	}

	// -----------------------------------------------------------

	/** Returns the number of entries in each form in the chart. */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < _size; i++) {
			for (int j = 0; j < _size; j++) {
				sb.append(makeForm(i, j).size()).append('\t');
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	@Override
	public final void print(PrintStream out) {
		ChartPrinter chartPrinter = new ChartPrinter();
		chartPrinter.print(chart, out);
	}

	@Override
	public int getSize() {
		return _size;
	}
}
