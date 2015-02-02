package opennlp.ccg.parse;

import java.io.PrintStream;
import java.util.List;

import opennlp.ccg.synsem.Symbol;
import opennlp.ccg.synsem.SignScorer;

/**
 * A chart completer.
 * 
 * The x1 position is the position of the first atom of the form x.
 * The x2 position is the position of the last atom of the form x.
 * 
 * If we tokenize the input string into word tokens, our atoms shall be words. However, if we
 * recognize forms in the input string instead of tokenizing it, each character of the input string
 * is an atom.
 * 
 * @author Daniel Couto-Vale
 */
public interface ChartCompleter {

	/**
	 * The size of the chart
	 * 
	 * @return the size
	 */
	int getSize();

	/**
	 * Prints th signs in the chart to a print stream.
	 * 
	 * @param out the print stream
	 */
	void print(PrintStream out);

	int getScoredSymbolCount();

	int getNonfinalScoredSymbolCount();

	int getMaxFormSize();

	void setSignScorer(SignScorer signScorer);

	void setPruneValue(int pruneValue);

	void setParseTimeLimit(int parseTimeLimit);

	void setStartTime(long startTime);

	void setEdgeLimit(int edgeLimit);

	/**
	 * Sets the size limit for forms of derived symbols.
	 */
	void setFormSizeLimt(int cellPruneValue);

	/**
	 * Associates form (x,y) with an edge for a given sign.
	 * 
	 * @param x1 the x1 position
	 * @param x2 the y2 position
	 * @param sign the sign to insert
	 * @return <code>true</code> if an edge is duly created and associated to the form and
	 *     <code>false</code> otherwise
	 */
	boolean annotateForm(int x1, int x2, Symbol sign);

	/**
	 * Associates form (x1,x2) with edges that result from applying unary rules to those already
	 * associated with the form.
	 * 
	 * @param x1 the x1 position
	 * @param x2 the x2 position
	 * @throws ParseException
	 */
	void annotateForm(int x1, int x2) throws ParseException;

	/**
	 * Associates form (z1,z2) with edges that result from combining the edges of form (x1,x2) with
	 * those of form (y1,y2).
	 * 
	 * @param x1 the x1 position
	 * @param x2 the x2 position
	 * @param y1 the y1 position
	 * @param y2 the y2 position
	 * @param z1 the z1 position
	 * @param z2 the z2 position
	 * @throws ParseException
	 */
	void combineForms(int x1, int x2, int y1, int y2, int z1, int z2) throws ParseException;

	/**
	 * Associates form (z1,z2) with fragmentary edges, if it is non-empty, that result from
	 * combining the edges of form (x1,x2) with those of form (y1,y2) using the glue rule.
	 * 
	 * @param x1 the x1 position
	 * @param x2 the x2 position
	 * @param y1 the y1 position
	 * @param y2 the y2 position
	 * @param z1 the z1 position
	 * @param z2 the z2 position
	 * @throws ParseException
	 */
	void glueForms(int x1, int x2, int y1, int y2, int z1, int z2) throws ParseException;

	/**
	 * Checks whether form (x,y) has no associated edge.
	 * 
	 * @param x1 the x1 position
	 * @param x2 the x2 position
	 * @return <code>true</code> if the cell is empty and <code>false</code> otherwise
	 */
	boolean isEmpty(int x1, int x2);

	/**
	 * Gets whether it is lazy unpack.
	 * 
	 * @param x1 the x1 position
	 * @param x2 the x2 position
	 * @return the edges
	 */
	List<ScoredSymbol> lazyUnpack(int x1, int x2);

	/**
	 * Gets whether it is unpack.
	 * 
	 * @param x1 the x1 position
	 * @param x2 the x2 position
	 * @return the edges
	 */
	List<ScoredSymbol> unpack(int x1, int x2);

}
