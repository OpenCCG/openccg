package opennlp.ccg.lexicon;

import java.util.ArrayList;
import java.util.List;

import opennlp.ccg.parse.ChartCompleter;
import opennlp.ccg.synsem.Sign;

/**
 * The product of an automatic linguistic analysis.
 *
 * @author Daniel Couto-Vale
 */
public class ParseProduct {

	/**
	 * The timestamp of parse start
	 */
	private long startTime = 0;

	/**
	 * The duration of parse
	 */
	private int parseTime = 0;

	/**
	 * The duration of form recognition
	 */
	private int lexTime = 0;

	/**
	 * The duration of chart construction
	 */
	private int chartTime = 0;

	/**
	 * The duration of supertag unpacking
	 */
	private int unpackingTime = 0;

	/**
	 * The chart
	 */
	private ChartCompleter chart = null;

	/**
	 * The symbols
	 */
	private List<Sign> result = new ArrayList<Sign>();

	/**
	 * The symbol scores
	 */
	private List<Double> scores = new ArrayList<Double>();

	private int parseLimit;

	private int edgeLimit;

	private int pruneValue;

	private int cellPruneValue;

	private Boolean lazyUnpacking;

	/**
	 * @return the lexTime
	 */
	public final int getLexTime() {
		return lexTime;
	}

	/**
	 * @param lexTime the lexTime to set
	 */
	public final void setLexTime(int lexTime) {
		this.lexTime = lexTime;
	}

	/**
	 * @return the startTime
	 */
	public final long getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public final void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the parseTime
	 */
	public final int getParseTime() {
		return parseTime;
	}

	/**
	 * @param parseTime the parseTime to set
	 */
	public final void setParseTime(int parseTime) {
		this.parseTime = parseTime;
	}

	/**
	 * @return the chartTime
	 */
	public final int getChartTime() {
		return chartTime;
	}

	/**
	 * @param chartTime the chartTime to set
	 */
	public final void setChartTime(int chartTime) {
		this.chartTime = chartTime;
	}

	/**
	 * @return the unpackingTime
	 */
	public final int getUnpackingTime() {
		return unpackingTime;
	}

	/**
	 * @param unpackingTime the unpackingTime to set
	 */
	public final void setUnpackingTime(int unpackingTime) {
		this.unpackingTime = unpackingTime;
	}

	/**
	 * @return the chart
	 */
	public final ChartCompleter getChartCompleter() {
		return chart;
	}

	/**
	 * @param chart the chart to set
	 */
	public final void setChart(ChartCompleter chart) {
		this.chart = chart;
	}

	/**
	 * @return the result
	 */
	public final List<Sign> getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public final void setResult(List<Sign> result) {
		this.result = result;
	}

	/**
	 * @return the scores
	 */
	public final List<Double> getScores() {
		return scores;
	}

	/**
	 * @param scores the scores to set
	 */
	public final void setScores(List<Double> scores) {
		this.scores = scores;
	}

	/**
	 * @param parseLimit the limit for parse duration
	 */
	public final void setParseLimit(int parseLimit) {
		this.parseLimit = parseLimit;
	}

	/**
	 * @param edgeLimit the limit for the number of edges
	 */
	public final void setEdgeLimit(int edgeLimit) {
		this.edgeLimit = edgeLimit;
	}

	/**
	 * @param pruneValue the prune value
	 */
	public final void setPruneValue(int pruneValue) {
		this.pruneValue = pruneValue;
	}

	/**
	 * @param cellPruneValue the cell prune value
	 */
	public final void setCellPruneValue(int cellPruneValue) {
		this.cellPruneValue = cellPruneValue;
	}

	/**
	 * @param lazyUnpacking the lazy unpacking
	 */
	public final void setLazyUnpacking(Boolean lazyUnpacking) {
		this.lazyUnpacking = lazyUnpacking;
	}

	/**
	 * @return the prune value
	 */
	public final int getPruneValue() {
		return pruneValue;
	}

	public final int getParseLimit() {
		return parseLimit;
	}

	public final int getEdgeLimit() {
		return edgeLimit;
	}

	public final int getCellPruneValue() {
		return cellPruneValue;
	}

	public final boolean getLazyUnpacking() {
		return lazyUnpacking;
	}

}
