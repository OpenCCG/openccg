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

	// Form recognition duration
	private int lexTime = 0;

	// Chart construction start time
	private long startTime = 0;

	// Current parse duration
	private int parseTime = 0;

	// Current chart construction duration
	private int chartTime = 0;

	// Current unpacking time
	private int unpackingTime = 0;

	// Current chart
	private Chart chart = null;

	// Current parse results
	private List<Sign> result = new ArrayList<Sign>();

	// Current parse scores
	private List<Double> scores = new ArrayList<Double>();

	/**
	 * @return the lexTime
	 */
	public int getLexTime() {
		return lexTime;
	}

	/**
	 * @param lexTime the lexTime to set
	 */
	public void setLexTime(int lexTime) {
		this.lexTime = lexTime;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the parseTime
	 */
	public int getParseTime() {
		return parseTime;
	}

	/**
	 * @param parseTime the parseTime to set
	 */
	public void setParseTime(int parseTime) {
		this.parseTime = parseTime;
	}

	/**
	 * @return the chartTime
	 */
	public int getChartTime() {
		return chartTime;
	}

	/**
	 * @param chartTime the chartTime to set
	 */
	public void setChartTime(int chartTime) {
		this.chartTime = chartTime;
	}

	/**
	 * @return the unpackingTime
	 */
	public int getUnpackingTime() {
		return unpackingTime;
	}

	/**
	 * @param unpackingTime the unpackingTime to set
	 */
	public void setUnpackingTime(int unpackingTime) {
		this.unpackingTime = unpackingTime;
	}

	/**
	 * @return the chart
	 */
	public Chart getChart() {
		return chart;
	}

	/**
	 * @param chart the chart to set
	 */
	public void setChart(Chart chart) {
		this.chart = chart;
	}

	/**
	 * @return the result
	 */
	public List<Sign> getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(List<Sign> result) {
		this.result = result;
	}

	/**
	 * @return the scores
	 */
	public List<Double> getScores() {
		return scores;
	}

	/**
	 * @param scores the scores to set
	 */
	public void setScores(List<Double> scores) {
		this.scores = scores;
	}

}
