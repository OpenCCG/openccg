package opennlp.ccg.parse;

import java.io.PrintStream;
import java.util.List;

import opennlp.ccg.synsem.Sign;
import opennlp.ccg.synsem.SignScorer;

/**
 * A chart completer.
 * 
 * The x coordinate is the index of the last atom of a given form.
 * The y coordinate is the number of hops between atoms of a given form.
 * 
 * This means that, if we tokenize the input string into word tokens, our atoms shall be words.
 * However, if we recognize forms in the input string instead of chopping it into 
 * we segment
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

	int getEdgeCount();

	int getUnpackingEdgeCount();

	int getMaxCellSize();

	void setSignScorer(SignScorer signScorer);

	void setPruneValue(int pruneValue);

	void setParseTimeLimit(int parseTimeLimit);

	void setStartTime(long startTime);

	void setEdgeLimit(int edgeLimit);

	void setCellPruneValue(int cellPruneValue);

	/**
	 * Associates form (x,y) with an edge for a given sign.
	 * 
	 * @param x1 the x1 coordinate
	 * @param x2 the y2 coordinate
	 * @param sign the sign to insert
	 * @return <code>true</code> if an edge is inserted and <code>false</code> otherwise
	 */
	boolean annotateForm(int x1, int x2, Sign sign);

	/**
	 * Associates form (x,y) with edges that result from applying unary rules to those already
	 * associated with it.
	 * 
	 * @param x1 the x1 coordinate
	 * @param x2 the x2 coordinate
	 * @throws ParseException
	 */
	void annotateForm(int x1, int x2) throws ParseException;

	/**
	 * Associates form (z1,z2) with edges that result from combining the edges of form (x1,x2) with
	 * those of form (y1,y2).
	 * 
	 * @param x1 the x1 coordinate
	 * @param x2 the x2 coordinate
	 * @param y1 the y1 coordinate
	 * @param y2 the y2 coordinate
	 * @param z1 the z1 coordinate
	 * @param z2 the z2 coordinate
	 * @throws ParseException
	 */
	void combineForms(int x1, int x2, int y1, int y2, int z1, int z2) throws ParseException;

	/**
	 * Associates cell (z1,z2) with fragmentary edges, if it is non-empty, that result from
	 * combining the edges of form (x1,x2) with those in cell (y1,y2) using the glue rule.
	 * 
	 * @param x1 the x1 coordinate
	 * @param x2 the x2 coordinate
	 * @param y1 the y1 coordinate
	 * @param y2 the y2 coordinate
	 * @param z1 the z1 coordinate
	 * @param z2 the z2 coordinate
	 * @throws ParseException
	 */
	void glueForms(int x1, int x2, int y1, int y2, int z1, int z2) throws ParseException;

	/**
	 * Checks whether form (x,y) has no associated edge.
	 * 
	 * @param x1 the x1 coordinate
	 * @param x2 the x2 coordinate
	 * @return <code>true</code> if the cell is empty and <code>false</code> otherwise
	 */
	boolean isEmpty(int x1, int x2);

	/**
	 * Gets whether it is lazy unpack.
	 * 
	 * @param x1 the x1 coordinate
	 * @param x2 the x2 coordinate
	 * @return the edges
	 */
	List<Edge> lazyUnpack(int x1, int x2);

	/**
	 * Gets whether it is unpack.
	 * 
	 * @param x1 the x1 coordinate
	 * @param x2 the x2 coordinate
	 * @return the edges
	 */
	List<Edge> unpack(int x1, int x2);

}
