package opennlp.ccg.lexicon;

import java.util.ArrayList;
import java.util.List;

import opennlp.ccg.parse.Chart;
import opennlp.ccg.synsem.Sign;

/**
 * The product of an automatic linguistic analysis.
 *
 * @author Daniel Couto-Vale
 */
public class ParsingProduct {

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
	private Chart chart = null;

	/**
	 * The symbols
	 */
	private List<Sign> result = new ArrayList<Sign>();

	/**
	 * The symbol scores
	 */
	private List<Double> scores = new ArrayList<Double>();

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
	public final Chart getChart() {
		return chart;
	}

	/**
	 * @param chart the chart to set
	 */
	public final void setChart(Chart chart) {
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

}
