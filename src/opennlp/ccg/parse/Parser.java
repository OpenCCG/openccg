///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-9 Jason Baldridge, Gann Bierner and Michael White
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

import opennlp.ccg.TextCCG;
import opennlp.ccg.lexicon.*;
import opennlp.ccg.synsem.*;
import opennlp.ccg.grammar.*;
import opennlp.ccg.hylo.EPsScorer;
import opennlp.ccg.hylo.HyloHelper;
import opennlp.ccg.hylo.Nominal;
import opennlp.ccg.unify.*;
import opennlp.ccg.util.Pair;

import java.util.*;
import java.util.prefs.Preferences;

/**
 * The parser is a CKY chart parser for CCG, optionally with iterative beta-best
 * supertagging and n-best output.
 *
 * @author Jason Baldridge
 * @author Gann Bierner
 * @author Michael White
 * @version $Revision: 1.38 $, $Date: 2011/08/27 19:27:00 $
 */
public class Parser {

	/** Preference key for time limit on parsing. */
	public static final String PARSE_TIME_LIMIT = "Parse Time Limit";

	/** A constant indicating no time limit on parsing. */
	public static final int NO_TIME_LIMIT = 0;

	/** Preference key for edge limit on parsing. */
	public static final String PARSE_EDGE_LIMIT = "Parse Edge Limit";

	/** A constant indicating no edge limit on parsing. */
	public static final int NO_EDGE_LIMIT = 0;

	/**
	 * Preference key for pruning the number of signs kept per equivalence
	 * class.
	 */
	public static final String PARSE_PRUNING_VALUE = "Parse Pruning Value";

	/** Preference key for pruning the number of edges kept per cell. */
	public static final String PARSE_CELL_PRUNING_VALUE = "Parse Cell Pruning Value";

	/** A constant indicating no pruning of signs per equivalence class. */
	public static final int NO_PRUNING = 0;

	/** Preference key for whether to use lazy unpacking. */
	public static final String PARSE_LAZY_UNPACKING = "Parse Lazy Unpacking";

	/** The grammar. */
	public final Grammar grammar;

	/** The lexicon used to create edges. */
	public final Lexicon lexicon;

	/** The rules used to create edges. */
	public final RuleGroup rules;

	/** Flag for whether to show the chart for failed parses. */
	public boolean debugParse = false;

	/** The sign scorer (or null if none). */
	protected SignScorer signScorer = null;

	/** The "n" for n-best pruning. (Default is none.) */
	protected int pruneVal = -1;

	/** The cell pruning value. (Default is none.) */
	protected int cellPruneVal = -1;

	/** The lazy unpacking flag. (Default is none.) */
	protected Boolean lazyUnpacking = null;

	/** Supertagger to use. (Default is none.) */
	protected Supertagger supertagger = null;

	/**
	 * Flag for whether to use the supertagger in the most-to-least restrictive
	 * direction.
	 */
	protected boolean stMostToLeastDir = true;

	/** Time limit in milliseconds. (Default is none.) */
	protected int timeLimit = -1;

	/** Edge limit. (Default is none.) */
	protected int edgeLimit = -1;

	// flag for whether to glue fragments currently
	private boolean gluingFragments = false;

	private ParseProduct product;

	/** Constructor. */
	public Parser(Grammar grammar) {
		this.grammar = grammar;
		this.lexicon = grammar.lexicon;
		this.rules = grammar.rules;
	}

	/** Sets the sign scorer. */
	public void setSignScorer(SignScorer signScorer) {
		this.signScorer = signScorer;
	}

	/** Sets the time limit. */
	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}

	/** Sets the edge limit. */
	public void setEdgeLimit(int edgeLimit) {
		this.edgeLimit = edgeLimit;
	}

	/** Sets the n-best pruning val. */
	public void setPruneVal(int n) {
		pruneVal = n;
	}

	/** Sets the cell pruning val. */
	public void setCellPruneVal(int n) {
		cellPruneVal = n;
	}

	/** Sets the lazy unpacking flag. */
	public void setLazyUnpacking(Boolean b) {
		this.lazyUnpacking = b;
	}

	/** Sets the supertagger. */
	public void setSupertagger(Supertagger supertagger) {
		this.supertagger = supertagger;
	}

	/** Sets the supertagger most-to-least restrictive direction flag. */
	public void setSupertaggerMostToLeastRestrictiveDirection(boolean bool) {
		stMostToLeastDir = bool;
	}

	/**
	 * Parses a character sequence.
	 *
	 * @param string the character sequence
	 * @exception ParseException thrown if a parse can't be found for the entire
	 *                string
	 */
	public void parse(String string) throws ParseException {
		// tokenize
		List<Word> words = lexicon.tokenizer.tokenize(string);
		// parse words
		parse(words);
	}

	/**
	 * Parses a list of words
	 * 
	 * @param words the list of words
	 * @throws ParseException
	 */
	public final void parse(List<Word> words) throws ParseException {
		makeProduct();

		// For supertagger, parse iterative beta-best
		if (supertagger != null) {
			parseIterativeBetaBest(words);
			return;
		} else {
			parseOnce(words);
		}
	}

	/**
	 * Parses a list of words
	 * 
	 * @param words the list of words
	 * @throws ParseException
	 */
	private final void parseOnce(List<Word> words) throws ParseException {
		try {
			// init
			long lexStartTime = System.currentTimeMillis();
			UnifyControl.startUnifySequence();
			// get entries for each word
			List<SignHash> entries = new ArrayList<SignHash>(words.size());
			for (Word w : words) {
				entries.add(lexicon.getSignsFromWord(w));
			}
			product.setLexTime((int) (System.currentTimeMillis() - lexStartTime));
			// do parsing
			product.setStartTime(System.currentTimeMillis());
			product.setChart(buildChart(entries));
			parseEntries(product.getChart());
		} catch (LexException e) {
			setGiveUpTime();
			String msg = "Unable to retrieve lexical entries:\n\t" + e.toString();
			if (debugParse)
				System.out.println(msg);
			throw new ParseException(msg);
		} catch (ParseException e) {
			setGiveUpTime();
			// show chart for failed parse if apropos
			if (debugParse) {
				System.out.println(e);
				System.out.println("Chart for failed parse:");
				product.getChart().print(System.out);
			}
			// rethrow
			throw e;
		}
	}

	private final void makeProduct() {
		Preferences preferences = Preferences.userNodeForPackage(TextCCG.class);
		product = new ParseProduct();
		product.setParseLimit(makeValueToUse(preferences, timeLimit, PARSE_TIME_LIMIT,
				NO_TIME_LIMIT));
		product.setEdgeLimit(makeValueToUse(preferences, edgeLimit, PARSE_EDGE_LIMIT, NO_EDGE_LIMIT));
		product.setPruneValue(makeValueToUse(preferences, pruneVal, PARSE_PRUNING_VALUE, NO_PRUNING));
		product.setCellPruneValue(makeValueToUse(preferences, cellPruneVal,
				PARSE_CELL_PRUNING_VALUE, NO_PRUNING));
		product.setLazyUnpacking(makeValueToUse(preferences, lazyUnpacking, PARSE_LAZY_UNPACKING,
				true));
	}

	/**
	 * Helper to choose value
	 * 
	 * @param preferences the preferences
	 * @param value a given value
	 * @param valueKey the value key in the preferences
	 * @param valueDefault the value default
	 * @return the value to use
	 */
	private final Boolean makeValueToUse(Preferences preferences, Boolean value, String valueKey,
			Boolean valueDefault) {
		if (value != null) {
			return value;
		} else {
			return preferences.getBoolean(valueKey, valueDefault);
		}
	}

	/**
	 * Helper to choose value
	 * 
	 * @param preferences the preferences
	 * @param value a given value
	 * @param valueKey the value key in the preferences
	 * @param valueDefault the value default
	 * @return the value to use
	 */
	private final static int makeValueToUse(Preferences preferences, int value, String valueKey,
			int valueDefault) {
		if (value >= 0) {
			return value;
		} else {
			return preferences.getInt(valueKey, valueDefault);
		}
	}

	// iterative beta-best parsing
	private void parseIterativeBetaBest(List<Word> words) throws ParseException {
		// set supertagger in lexicon
		grammar.lexicon.setSupertagger(supertagger);
		// ensure gluing off
		gluingFragments = false;
		// reset beta
		if (stMostToLeastDir)
			supertagger.resetBeta();
		else
			supertagger.resetBetaToMax();
		// loop
		boolean done = false;
		while (!done) {
			try {
				// init
				long lexStartTime = System.currentTimeMillis();
				UnifyControl.startUnifySequence();
				// get filtered entries for each word
				List<SignHash> entries = new ArrayList<SignHash>(words.size());
				supertagger.mapWords(words);
				for (int i = 0; i < words.size(); i++) {
					supertagger.setWord(i);
					Word word = words.get(i);
					entries.add(lexicon.getSignsFromWord(word));
				}
				product.setLexTime((int) (System.currentTimeMillis() - lexStartTime));
				;
				// do parsing
				
				// set up chart
				product.setStartTime(System.currentTimeMillis());
				product.setChart(buildChart(entries));
				parseEntries(product.getChart());
				// done
				done = true;
				// reset supertagger in lexicon, turn gluing off
				grammar.lexicon.setSupertagger(null);
				gluingFragments = false;
			} catch (LexException e) {
				// continue if more betas
				if (stMostToLeastDir && supertagger.hasMoreBetas()) {
					supertagger.nextBeta();
				}
				// otherwise give up
				else {
					setGiveUpTime();
					// reset supertagger in lexicon, turn gluing off
					grammar.lexicon.setSupertagger(null);
					gluingFragments = false;
					// throw parse exception
					String msg = "Unable to retrieve lexical entries:\n\t" + e.toString();
					if (debugParse)
						System.out.println(msg);
					throw new ParseException(msg);
				}
			} catch (ParseException e) {
				// check if limits exceeded
				boolean outwith = e.getMessage() == ParseException.EDGE_LIMIT_EXCEEDED
						|| e.getMessage() == ParseException.TIME_LIMIT_EXCEEDED;
				// continue if more betas and limits not exceeded
				if (stMostToLeastDir && supertagger.hasMoreBetas() && !outwith)
					supertagger.nextBeta();
				// or if limits exceeded and moving in the opposite direction
				else if (!stMostToLeastDir && supertagger.hasLessBetas() && outwith)
					supertagger.previousBeta();
				// otherwise try glue rule, unless already on
				else if (!gluingFragments) {
					supertagger.resetBeta(); // may as well use most restrictive
												// supertagger setting with glue
												// rule
					gluingFragments = true;
				}
				// otherwise give up
				else {
					setGiveUpTime();
					// show chart for failed parse if apropos
					if (debugParse) {
						System.out.println(e);
						System.out.println("Chart for failed parse:");
						product.getChart().print(System.out);
					}
					// reset supertagger in lexicon, turn gluing off
					grammar.lexicon.setSupertagger(null);
					gluingFragments = false;
					// rethrow
					throw e;
				}
			}
		}
	}

	/** Returns the supertagger's final beta value (or 0 if none). */
	public double getSupertaggerBeta() {
		return (supertagger != null) ? supertagger.getCurrentBetaValue() : 0;
	}

	// parses from lex entries
	private void parseEntries(ChartCompleter chart) throws ParseException {
		if (signScorer != null) {
			chart.setSignScorer(signScorer);
		}
		chart.setPruneValue(product.getPruneValue());
		chart.setParseTimeLimit(product.getParseLimit());
		chart.setStartTime(product.getStartTime());
		chart.setEdgeLimit(product.getEdgeLimit());
		chart.setCellPruneValue(product.getCellPruneValue());
		// do parsing
		parse(chart.getSize());
	}

	/**
	 * Builds a chart for a particular sequence of associations.
	 * 
	 * @param entries the entries to build
	 * @return the chart
	 */
	private final ChartCompleter buildChart(List<SignHash> entries) {
		ChartCompleter chart = new ChartCompleterStd(entries.size(), rules);
		for (int i = 0; i < entries.size(); i++) {
			SignHash signHash = entries.get(i);
			for (Sign sign : signHash.getSignsSorted()) {
				Category category = sign.getCategory();
				UnifyControl.reindex(category);
				chart.annotateForm(i, i, sign);
			}
		}
		return chart;
	}

	/**
	 * Parse using the Cocke–Younger–Kasami (CKY) algorithm
	 * 
	 * @param size the size of the chart
	 * @throws ParseException
	 */
	private void parse(int size) throws ParseException {
		ChartCompleter chart = product.getChart();

		// Fill in chart
		for (int i = 0; i < size; i++) {
			chart.annotateForm(i, i);
		}

		// Combine forms
		for (int y2 = 1; y2 < size; y2++) {
			for (int x1 = y2 - 1; x1 >= 0; x1--) {
				for (int y1 = x1; y1 < y2; y1++) {
					int x2 = y1 + 1;
					int x3 = x1;
					int y3 = y2;
					chart.combineForms(x1, y1, x2, y2, x3, y3);
				}
				chart.annotateForm(x1, y2);
			}
		}
		// glue fragments if apropos
		if (gluingFragments && chart.isEmpty(0, size - 1)) {
			for (int j = 1; j < size; j++) {
				for (int i = j - 1; i >= 0; i--) {
					for (int k = i; k < j; k++) {
						chart.glueForms(i, k, k + 1, j, i, j);
					}
				}
			}
		}
		product.setChartTime((int) (System.currentTimeMillis() - product.getStartTime()));
		// extract results
		createResult(size);
		product.setParseTime((int) (System.currentTimeMillis() - product.getStartTime()));
		product.setUnpackingTime(product.getParseTime() - product.getChartTime());
	}

	// create answer ArrayList
	private void createResult(int size) throws ParseException {
		List<Sign> result = new ArrayList<Sign>();
		List<Double> scores = new ArrayList<Double>();
		ChartCompleter chart = product.getChart();
		// unpack top
		List<Edge> unpacked = product.getLazyUnpacking() ? chart.lazyUnpack(0, size - 1) : chart.unpack(
				0, size - 1);
		// add signs for unpacked edges
		for (Edge edge : unpacked) {
			result.add(edge.sign);
			scores.add(edge.score);
		}
		// check non-empty
		if (result.size() == 0) {
			throw new ParseException("Unable to parse");
		}
		product.setResult(result);
		product.setScores(scores);
	}

	// set parse time when giving up
	private void setGiveUpTime() {
		product.setChartTime((int) (System.currentTimeMillis() - product.getStartTime()));
		product.setParseTime(product.getChartTime());
		product.setUnpackingTime(0);
	}

	/**
	 * Adds the supertagger log probs to the lexical signs of the gold standard
	 * parse.
	 */
	public void addSupertaggerLogProbs(Sign gold) {
		List<Word> words = gold.getWords();
		supertagger.mapWords(words);
		addSupertaggerLogProbs(gold, gold);
		for (int i = 0; i < words.size(); i++) {
			supertagger.setWord(i);
		}
	}

	// recurses through derivation, adding lex log probs to lexical signs
	private void addSupertaggerLogProbs(Sign gold, Sign current) {
		// lookup and add log prob for lex sign
		if (current.isIndexed()) {
			supertagger.setWord(gold.wordIndex(current));
			Map<String, Double> stags = supertagger.getSupertags();
			Double lexprob = stags.get(current.getSupertag());
			if (lexprob != null) {
				current.addData(new SupertaggerAdapter.LexLogProb((float) Math.log10(lexprob)));
			}
		}
		// otherwise recurse
		else {
			Sign[] inputs = current.getDerivationHistory().getInputs();
			for (Sign s : inputs)
				addSupertaggerLogProbs(gold, s);
		}
	}

	/**
	 * Returns the oracle best sign among those in the n-best list for the given
	 * LF, using the f-score on all EPs, together with a flag indicating whether
	 * the gold LF was found (as indicated by an f-score of 1.0). NB: It would
	 * be better to return the forest oracle, but the nominal conversion would
	 * be tricky to do correctly.
	 */
	public Pair<Sign, Boolean> oracleBest(LF goldLF) {
		Sign retval = null;
		List<Sign> result = product.getResult();
		double bestF = 0.0;
		for (Sign sign : result) {
			Category cat = sign.getCategory().copy();
			Nominal index = cat.getIndexNominal();
			LF parsedLF = cat.getLF();
			if (parsedLF != null) {
				index = HyloHelper.getInstance().convertNominals(parsedLF, sign, index);
				EPsScorer.Results score = EPsScorer.score(parsedLF, goldLF);
				if (score.fscore > bestF) {
					retval = sign;
					bestF = score.fscore;
				}
			}
		}
		return new Pair<Sign, Boolean>(retval, (bestF == 1.0));
	}

	public final ParseProduct getProduct() {
		return product;
	}

}
