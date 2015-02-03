package opennlp.ccg.parse;

import gnu.trove.THashMap;
import gnu.trove.TObjectHashingStrategy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import opennlp.ccg.synsem.Symbol;
import opennlp.ccg.synsem.SymbolHash;

/**
 * An instance of a form that is associated with zero or more scored symbols.
 * Implementationwise, a form that is associated with a sorted list of scored
 * symbols and a scored symbol map.
 *
 * @author Jason Baldridge
 * @author Gann Bierner
 * @author Daniel Couto-Vale
 */
public class Form implements Serializable {

	// maps edges to representative edges, according to their headwords and
	// cats, sans LFs
	// NB: using unfilled dependencies in equiv relation appears to unacceptably
	// slow down parsing,
	// with a significant drop in complete parses
	@SuppressWarnings("unchecked")
	private static Map<ScoredSymbol, ScoredSymbol> createScoredSymbolMap() {
		return new THashMap(11, representativeEdgeStrategy);
	}

	private static TObjectHashingStrategy representativeEdgeStrategy = new TObjectHashingStrategy() {
		private static final long serialVersionUID = 1L;

		public int computeHashCode(Object o) {
			Symbol sign = ((ScoredSymbol) o).symbol;
			int x1 = ScoredSymbol.recoverScoredSymbol(sign.getLexHead()).x1;
			return 31 * x1 + sign.getCategory().hashCodeNoLF();
			// return 31*headpos + sign.getCategory().hashCodeNoLF() +
			// 17*sign.getUnfilledDeps().hashCode();
		}

		public boolean equals(Object o1, Object o2) {
			if (!(o1 instanceof ScoredSymbol) || !(o2 instanceof ScoredSymbol))
				return false;
			Symbol sign1 = ((ScoredSymbol) o1).symbol;
			Symbol sign2 = ((ScoredSymbol) o2).symbol;
			return ScoredSymbol.recoverScoredSymbol(sign1.getLexHead()).x1 == ScoredSymbol
					.recoverScoredSymbol(sign2.getLexHead()).x1
					&& sign1.getCategory().equalsNoLF(sign2.getCategory());
			// && sign1.getUnfilledDeps().equals(sign2.getUnfilledDeps());
		}
	};

	/**
	 * Inserts a scored symbol in the list of scored symbols of a form. It
	 * applies insert sort.
	 * 
	 * WARNING: All indexed symbols are kept even if the number of indexed
	 * symbols is larger than the limit. In such cases, for each added symbols
	 * derived with unary rules for the current form, one symbol will be taken
	 * out of the list (and not all symbols beyond the limit).
	 * 
	 * @param scoredSymbol the scored symbol
	 * @param scoredSymbolList the list of scored symbols
	 * @param map the map of scored symbols (alternatives?)
	 * @param sizeLimit the limit of symbols in the list
	 * @param scoredSymbolComparator the comparator of scored symbols
	 * @return <code>true</code> if the symbol is inserted and
	 *         <code>false</code> otherwise
	 */
	public static boolean insertScoredSymbol(ScoredSymbol scoredSymbol,
			List<ScoredSymbol> scoredSymbolList, Map<ScoredSymbol, ScoredSymbol> map, int sizeLimit,
			Comparator<ScoredSymbol> scoredSymbolComparator) {
		int index = Collections
				.binarySearch(scoredSymbolList, scoredSymbol, scoredSymbolComparator);
		int insertPosition = Math.abs(index) - 1;
		if (insertPosition < 0) {
			insertPosition = scoredSymbolList.size();
		}

		// If a derived symbol is to be inserted in a position beyond the limit
		// of symbols for the current form, do not insert it.
		boolean limitActive = sizeLimit > 0 && scoredSymbol.symbol.isDerived();
		if (limitActive && insertPosition >= sizeLimit) {
			return false;
		}

		// Insert the symbol and inserpt position
		scoredSymbolList.add(insertPosition, scoredSymbol);
		if (map != null) {
			map.put(scoredSymbol, scoredSymbol);
		}

		// If the number of symbols for the current form is beyond the limit,
		// remove last symbol. This is the expected behaviour, see warning above
		// for the reason.
		if (limitActive && scoredSymbolList.size() > sizeLimit) {
			ScoredSymbol last = scoredSymbolList.remove(scoredSymbolList.size() - 1);
			if (map != null)
				map.remove(last);
		}
		return true;
	}

	private static final long serialVersionUID = 1L;

	/**
	 * List of scored symbols
	 */
	private final List<ScoredSymbol> scoredSymbols = new ArrayList<ScoredSymbol>();

	/**
	 * Map of scored symbols
	 */
	final Map<ScoredSymbol, ScoredSymbol> scoredSymbolMap = createScoredSymbolMap();

	private final int sizeLimit;
	private final Comparator<ScoredSymbol> scoredSymbolComparator;

	public Form(int cellLimit, Comparator<ScoredSymbol> scoredSymbolComparator) {
		this.sizeLimit = cellLimit;
		this.scoredSymbolComparator = scoredSymbolComparator;
	}

	int size() {
		return scoredSymbols.size();
	}

	public final ScoredSymbol get(ScoredSymbol scoredSymbol) {
		return scoredSymbolMap.get(scoredSymbol);
	}

	// add edge, preserving cell limit; return true iff given edge added
	public final boolean add(ScoredSymbol scoredSymbol) {
		if (scoredSymbolMap.containsKey(scoredSymbol))
			return false;
		return insertScoredSymbol(scoredSymbol, scoredSymbols, scoredSymbolMap, sizeLimit, scoredSymbolComparator);
	}

	public final List<Symbol> sortSymbols() {
		List<Symbol> retval = new ArrayList<Symbol>(scoredSymbols.size());
		for (ScoredSymbol e : scoredSymbols)
			retval.add(e.symbol);
		return retval;
	}

	public final SymbolHash getSymbols() {
		SymbolHash signHash = new SymbolHash();
		for (ScoredSymbol e : scoredSymbols) {
			signHash.insert(e.symbol);
		}
		return signHash;
	}

	public final boolean hasSymbols() {
		return !scoredSymbols.isEmpty();
	}

	public final List<ScoredSymbol> getScoredSymbols() {
		return scoredSymbols;
	}

}
