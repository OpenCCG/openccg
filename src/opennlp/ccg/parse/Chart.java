package opennlp.ccg.parse;

import java.util.List;

import opennlp.ccg.synsem.Sign;
import opennlp.ccg.synsem.SignScorer;

/**
 * A chart constructor.
 * 
 * @author Daniel Couto-Vale
 */
public interface Chart {

	void printChart();

	int edgeCount();

	int unpackingEdgeCount();

	int maxCellSize();

	void setSignScorer(SignScorer signScorer);

	void setPruneVal(int pruneValToUse);

	void setTimeLimit(int timeLimitToUse);

	void setStartTime(long startTime);

	void setEdgeLimit(int edgeLimitToUse);

	void setCellLimit(int cellPruneValToUse);

	/**
	 * Insterts an edge for a given sign into cell (x,y)
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param sign the sign to insert
	 * @return <code>true</code> if an edge is inserted and <code>false</code> otherwise
	 */
	boolean insert(int x, int y, Sign sign);

	/**
	 * Inserts edges into cell (x,y) that result from applying unary rules to those already there.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @throws ParseException
	 */
	void insertCell(int x, int y) throws ParseException;

	/**
	 * Inserts edges into cell (x3,y3) that result from combining those in cell (x1,y1) with those
	 * in cell (x2,y2).
	 * 
	 * @param x1 the x1 coordinate
	 * @param y1 the y1 coordinate
	 * @param x2 the x2 coordinate
	 * @param y2 the y2 coordinate
	 * @param x3 the x3 coordinate
	 * @param y3 the y3 coordinate
	 * @throws ParseException
	 */
	void insertCell(int x1, int y1, int x2, int y2, int x3, int y3) throws ParseException;

	/**
	 * Inserts fragmentary edges into cell (x3,y3), if it is non-empty, that result from
	 * combining those in cell (x1,y1) with those in cell (x2,y2) using the glue rule.
	 * 
	 * @param x1 the x1 coordinate
	 * @param y1 the y1 coordinate
	 * @param x2 the x2 coordinate
	 * @param y2 the y2 coordinate
	 * @param x3 the x3 coordinate
	 * @param y3 the y3 coordinate
	 * @throws ParseException
	 */
	void insertCellFrag(int x1, int y1, int x2, int y2, int x3, int y3) throws ParseException;

	/**
	 * Returns if cell (x,y) is empty, i.e. has no edges.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return <code>true</code> if the cell is empty and <code>false</code> otherwise
	 */
	boolean isEmpty(int x, int y);

	List<Edge> lazyUnpack(int i, int j);

	List<Edge> unpack(int i, int j);

}