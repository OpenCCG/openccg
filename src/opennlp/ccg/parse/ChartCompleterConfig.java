package opennlp.ccg.parse;

import opennlp.ccg.synsem.SymbolScorer;

public class ChartCompleterConfig {

	/**
	 * The sign scorer (defaults to the null scorer).
	 */
	protected SymbolScorer symbolScorer;

	/**
	 * The prune limit "n" for n-best pruning (or 0 if none).
	 */
	protected int pruneLimit;

	/**
	 * The parse time limit (0 if none).
	 */
	protected int timeLimit;

	/**
	 * The limit of scored symbols in the chart (0 if none).
	 */
	protected int scoredSymbolLimit;

	/**
	 * The prune limit of derived symbols for a form (0 if none).
	 */
	protected int formPruneLimit;

	/**
	 * The start time.
	 */
	protected long startTime;

	/**
	 * Constructor
	 */
	public ChartCompleterConfig() {
		symbolScorer = SymbolScorer.nullScorer;
		pruneLimit = 0;
		timeLimit = 0;
		scoredSymbolLimit = 0;
		formPruneLimit = 0;
		startTime = 0;
	}

	/**
	 * Constructor
	 * 
	 * @param config the configuration to clone
	 */
	public ChartCompleterConfig(ChartCompleterConfig config) {
		symbolScorer = config.symbolScorer;
		pruneLimit = config.pruneLimit;
		timeLimit = config.timeLimit;
		scoredSymbolLimit = config.scoredSymbolLimit;
		formPruneLimit = config.formPruneLimit;
		startTime = config.startTime;
	}

}
