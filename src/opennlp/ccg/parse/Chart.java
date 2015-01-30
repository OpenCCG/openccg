package opennlp.ccg.parse;

import java.util.List;

import opennlp.ccg.synsem.Sign;
import opennlp.ccg.synsem.SignScorer;

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

	boolean insert(int i, int i2, Sign sign);

	void insertCell(int i, int i2) throws ParseException;

	void insertCell(int i, int k, int j, int j2, int i2, int j3) throws ParseException;

	boolean cellIsEmpty(int i, int j);

	void insertCellFrag(int i, int k, int j, int j2, int i2, int j3) throws ParseException;

	List<Edge> lazyUnpack(int i, int j);

	List<Edge> unpack(int i, int j);

}