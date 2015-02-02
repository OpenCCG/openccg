///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2007 Michael White
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

import opennlp.ccg.synsem.*;
import java.util.*;
import java.io.Serializable;
import java.text.*;

/**
 * <p>
 * A scored symbol is a wrapper for a symbol, i.e. a symbol annotated with a score, and
 * optionally a list of alternative scored symbols.
 * 
 * A representative scored symbol is a scored symbol that stands in for other scored symbols.
 * It has the same category but different rhetorico-semantic entities (LFs).
 * The represented scored symbols are stored in the list of alternative scored symbols during
 * chart construction. The representative scored symbol is considered disjunctive when there is
 * more than one alternatives to it.
 * 
 * WARNING: A representative scored symbol will initially be present in its list of alternatives,
 * but this may not be the case all the way through chart construction. This is the case because
 * the representative scored symbol as any other alternative may be removed from the list of
 * alternatives through pruning.
 * </p>
 *
 * @author Michael White
 * @author Daniel Couto-Vale
 * @version $Revision: 1.4 $, $Date: 2009/12/22 22:19:00 $
 */
public class ScoredSymbol implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Class for storing back-refs from signs. */
	public static class EdgeRef implements Serializable {
		private static final long serialVersionUID = 1L;
		/** The edge. */
		public final ScoredSymbol scoredSymbol;

		/** Constructor. */
		public EdgeRef(ScoredSymbol scoredSymbol) {
			this.scoredSymbol = scoredSymbol;
		}
	}

	/** Returns the edge associated with this sign, or null if none. */
	public static ScoredSymbol getScoredSymbol(Symbol symbol) {
		EdgeRef eref = (EdgeRef) symbol.getData(EdgeRef.class);
		return (eref != null) ? eref.scoredSymbol : null;
	}

	/** The sign. */
	protected Symbol symbol;

	/** The edge score. */
	protected double score;

	/** Word position, for lexical edges (otherwise -1). */
	protected int wordPos = -1;

	/** The alternative scored symbols (none initially). */
	protected List<ScoredSymbol> alternatives = null;

	/** Saved list of alternative edges, for restoring chart after unpacking. */
	protected transient List<ScoredSymbol> savedAltEdges = null;

	/** Constructor (score defaults to 0.0). */
	public ScoredSymbol(Symbol sign) {
		this(sign, 0.0);
	}

	/** Constructor with score. */
	public ScoredSymbol(Symbol sign, double score) {
		this.symbol = sign;
		this.score = score;
		sign.addData(new EdgeRef(this));
	}

	/** Returns the sign. */
	public Symbol getSign() {
		return symbol;
	}

	/** Returns the score. */
	public double getScore() {
		return score;
	}

	/** Sets the score. */
	public void setScore(double score) {
		this.score = score;
	}

	/** Returns the word position of a lexical edge (otherwise -1). */
	public int getWordPos() {
		return wordPos;
	}

	/** Sets the word position of a lexical edge. */
	public void setWordPos(int pos) {
		wordPos = pos;
	}

	/** Returns whether this edge is a representative. */
	public boolean isRepresentative() {
		return alternatives != null;
	}

	/** Returns whether this edge is disjunctive. */
	public boolean isDisjunctive() {
		return alternatives != null && alternatives.size() > 1;
	}

	/** Returns the list of alt edges, or the empty list if none. */
	public List<ScoredSymbol> getAltEdges() {
		if (alternatives == null)
			return Collections.emptyList();
		else
			return alternatives;
	}

	/**
	 * Initializes the alt edges list with a default capacity, adding this edge.
	 */
	public void initAltEdges() {
		initAltEdges(3);
	}

	/**
	 * Initializes the alt edges list with the given capacity, adding this edge.
	 */
	public void initAltEdges(int capacity) {
		// check uninitialized
		if (alternatives != null)
			throw new RuntimeException("Alt edges already initialized!");
		alternatives = new ArrayList<ScoredSymbol>(capacity);
		alternatives.add(this);
	}

	/** Replaces the alt edges, saving the current ones for later restoration. */
	public void replaceAltEdges(List<ScoredSymbol> newAlts) {
		savedAltEdges = alternatives;
		alternatives = newAlts;
	}

	/** Recursively restores saved alt edges, if any. */
	public void restoreAltEdges() {
		if (savedAltEdges != null) {
			// restore
			alternatives = savedAltEdges;
			savedAltEdges = null;
			// recurse
			for (ScoredSymbol alt : alternatives) {
				Symbol[] inputs = alt.symbol.getDerivationHistory().getInputs();
				if (inputs != null) {
					for (Symbol s : inputs)
						getScoredSymbol(s).restoreAltEdges();
				}
			}
		}
	}

	/**
	 * Returns a hash code for this edge, based on its sign. (Alternatives and
	 * the score are not considered.)
	 */
	public int hashCode() {
		return symbol.hashCode() * 23;
	}

	/**
	 * Returns a hash code for this edge based on the sign's surface words.
	 * (Alternatives and the score are not considered.)
	 */
	public int surfaceWordHashCode() {
		return symbol.surfaceWordHashCode() * 23;
	}

	/**
	 * Returns whether this edge equals the given object. (Alternatives and the
	 * score are not considered.)
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ScoredSymbol))
			return false;
		ScoredSymbol edge = (ScoredSymbol) obj;
		return symbol.equals(edge.symbol);
	}

	/**
	 * Returns whether this edge equals the given object based on the sign's
	 * surface words. (Alternatives and the score are not considered.)
	 */
	public boolean surfaceWordEquals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ScoredSymbol))
			return false;
		ScoredSymbol edge = (ScoredSymbol) obj;
		return symbol.surfaceWordEquals(edge.symbol);
	}

	/**
	 * Returns a string for the edge in the format [score] orthography :-
	 * category.
	 */
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		if (score >= 0.001 || score == 0.0)
			sbuf.append("[" + nf3.format(score) + "] ");
		else
			sbuf.append("[" + nfE.format(score) + "] ");
		sbuf.append(symbol.toString());
		return sbuf.toString();
	}

	// formats to three decimal places
	private static final NumberFormat nf3 = initNF3();

	private static NumberFormat initNF3() {
		NumberFormat f = NumberFormat.getInstance();
		f.setMinimumIntegerDigits(1);
		f.setMinimumFractionDigits(3);
		f.setMaximumFractionDigits(3);
		return f;
	}

	// formats to "0.##E0"
	private static final NumberFormat nfE = new DecimalFormat("0.##E0");
}
