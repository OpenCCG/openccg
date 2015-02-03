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
	public static final String PARSE_SCORED_SYMBOL_LIMIT = "Parse Edge Limit";

	/** A constant indicating no edge limit on parsing. */
	public static final int NO_SCORED_SYMBOL_LIMIT = 0;

	/**
	 * Preference key for pruning the number of signs kept per equivalence
	 * class.
	 */
	public static final String PARSE_PRUNE_LIMIT = "Parse Pruning Value";

	/** Preference key for pruning the number of edges kept per cell. */
	public static final String FORM_PRUNE_LIMIT = "Parse Cell Pruning Value";

	/** A constant indicating no pruning of signs per equivalence class. */
	public static final int NO_PRUNE_LIMIT = 0;

	/** Preference key for whether to use lazy unpacking. */
	public static final String LAZY_UNPACKING = "Parse Lazy Unpacking";

	/** The grammar. */
	public final Grammar grammar;

	/** The lexicon used to create edges. */
	public final Lexicon lexicon;

	/** The rules used to create edges. */
	public final RuleGroup rules;

	/** Flag for whether to show the chart for failed parses. */
	public boolean debugParse = false;

	/** The sign scorer (or null if none). */
	protected SymbolScorer signScorer = null;

	/** The "n" for n-best pruning. (Default is none.) */
	protected int pruneVal = -1;

	/** The cell pruning value. (Default is none.) */
	protected int cellPruneVal = -1;

	/** The lazy unpacking flag. (Default is none.) */
	protected boolean lazyUnpacking = true;

	/** Supertagger to use. (Default is none.) */
	protected Supertagger supertagger = null;

	/**
	 * Flag for whether to use the supertagger in the most-to-least restrictive
	 * direction.
	 */
	protected boolean stMostToLeastDir = true;

	/**
	 * Flag for whether to glue formal fragments
	 */
	private boolean gluingFlag = false;

	/**
	 * The product of parsing
	 */
	private ParseProduct product;

	/**
	 * The chart completer config
	 */
	private ChartCompleterConfig config;

	/**
	 * @param grammar the grammar
	 */
	public Parser(Grammar grammar) {
		this.grammar = grammar;
		this.lexicon = grammar.lexicon;
		this.rules = grammar.rules;
		this.config = new ChartCompleterConfig();
	}

	/**
	 * @param symbolScorer the symbol scorer
	 */
	public final void setSymbolScorer(SymbolScorer symbolScorer) {
		config.symbolScorer = symbolScorer;
	}

	/**
	 * @param timeLimit the time limit for parsing
	 */
	public final void setTimeLimit(int timeLimit) {
		Preferences preferences = Preferences.userNodeForPackage(TextCCG.class);
		config.timeLimit = makeValueToUse(preferences, timeLimit, PARSE_TIME_LIMIT, NO_TIME_LIMIT);
	}

	/**
	 * @param scoredSymbolLimit the limit of scored symbols for parsing
	 */
	public final void setScoredSymbolLimit(int scoredSymbolLimit) {
		Preferences preferences = Preferences.userNodeForPackage(TextCCG.class);
		config.scoredSymbolLimit = makeValueToUse(preferences, scoredSymbolLimit,
				PARSE_SCORED_SYMBOL_LIMIT, NO_SCORED_SYMBOL_LIMIT);
	}

	/**
	 * @param pruneLimit the prune limit of the chart for scored symbols
	 */
	public final void setPruneLimit(int pruneLimit) {
		Preferences preferences = Preferences.userNodeForPackage(TextCCG.class);
		config.pruneLimit = makeValueToUse(preferences, pruneLimit, PARSE_PRUNE_LIMIT,
				NO_PRUNE_LIMIT);
	}

	/**
	 * @param formPruneLimit the prune limit of forms for scored symbols
	 */
	public final void setCellPruneVal(int formPruneLimit) {
		Preferences preferences = Preferences.userNodeForPackage(TextCCG.class);
		config.formPruneLimit = makeValueToUse(preferences, formPruneLimit, FORM_PRUNE_LIMIT,
				NO_PRUNE_LIMIT);
	}

	/**
	 * @param lazyUnpacking lazy unpacking
	 */
	public final void setLazyUnpacking(boolean lazyUnpacking) {
		Preferences preferences = Preferences.userNodeForPackage(TextCCG.class);
		this.lazyUnpacking = makeValueToUse(preferences, lazyUnpacking, LAZY_UNPACKING, true);
	}

	/** Sets the supertagger. */
	public final void setSupertagger(Supertagger supertagger) {
		this.supertagger = supertagger;
	}

	/** Sets the supertagger most-to-least restrictive direction flag. */
	public final void setSupertaggerMostToLeastRestrictiveDirection(boolean bool) {
		this.stMostToLeastDir = bool;
	}

	/**
	 * Parses a character sequence.
	 *
	 * @param string the character sequence
	 * @return 
	 * @exception ParseException thrown if a parse can't be found for the entire
	 *                string
	 */
	public final ParseProduct parse(String string) throws ParseException {
		List<Word> words = lexicon.tokenizer.tokenize(string);
		return parse(words);
	}

	/**
	 * Parses a list of words
	 * 
	 * @param words the list of words
	 * @return 
	 * @throws ParseException
	 */
	public final ParseProduct parse(List<Word> words) throws ParseException {
		product = new ParseProduct();

		// For supertagger, parse iterative beta-best
		if (supertagger != null) {
			parseIterativeBetaBest(words);
		} else {
			parseOnce(words);
		}
		return product;
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
			List<SymbolHash> entries = new ArrayList<SymbolHash>(words.size());
			for (Word w : words) {
				entries.add(lexicon.getSignsFromWord(w));
			}
			product.setLexTime((int) (System.currentTimeMillis() - lexStartTime));
			// do parsing
			product.setStartTime(System.currentTimeMillis());
			product.setChartCompleter(buildChartCompleter(entries));
			parseEntries(product.getChartCompleter());
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
				product.getChartCompleter().print(System.out);
			}
			// rethrow
			throw e;
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
		gluingFlag = false;
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
				List<SymbolHash> entries = new ArrayList<SymbolHash>(words.size());
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
				product.setChartCompleter(buildChartCompleter(entries));
				parseEntries(product.getChartCompleter());
				// done
				done = true;
				// reset supertagger in lexicon, turn gluing off
				grammar.lexicon.setSupertagger(null);
				gluingFlag = false;
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
					gluingFlag = false;
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
				else if (!gluingFlag) {
					supertagger.resetBeta(); // may as well use most restrictive
												// supertagger setting with glue
												// rule
					gluingFlag = true;
				}
				// otherwise give up
				else {
					setGiveUpTime();
					// show chart for failed parse if apropos
					if (debugParse) {
						System.out.println(e);
						System.out.println("Chart for failed parse:");
						product.getChartCompleter().print(System.out);
					}
					// reset supertagger in lexicon, turn gluing off
					grammar.lexicon.setSupertagger(null);
					gluingFlag = false;
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
	private void parseEntries(ChartCompleter chartCompleter) throws ParseException {
		parse(chartCompleter.getSize());
	}

	/**
	 * Builds a chart for a particular sequence of symbol hashes.
	 * 
	 * @param symbolHashes the symbol hashes to put in the chart
	 * @return the chart the chart
	 */
	private final ChartCompleter buildChartCompleter(List<SymbolHash> symbolHashes) {
		Chart chart = new SparseChart(symbolHashes.size());
		ChartCompleter chartCompleter = new ChartCompleterImp(rules, chart,
				new ChartCompleterConfig(config));
		int x1 = 0;
		int x2 = 0;
		for (SymbolHash symbolHash : symbolHashes) {
			for (Symbol symbol : symbolHash.getSignsSorted()) {
				Category category = symbol.getCategory();
				UnifyControl.reindex(category);
				chartCompleter.annotateForm(x1, x2, symbol);
			}
			x1++;
			x2++;
		}
		return chartCompleter;
	}

	/**
	 * Parse using the Cocke–Younger–Kasami (CKY) algorithm
	 * 
	 * @param size the size of the chart
	 * @throws ParseException
	 */
	private void parse(int size) throws ParseException {
		ChartCompleter chartCompleter = product.getChartCompleter();

		// Annotate index forms with unary rules
		for (int i = 0; i < size; i++) {
			chartCompleter.annotateForm(i, i);
		}

		// Combine forms and annotate combined forms with unary rules
		for (int y2 = 1; y2 < size; y2++) {
			for (int x1 = y2 - 1; x1 >= 0; x1--) {
				int z1 = x1;
				int z2 = y2;
				for (int x2 = x1; x2 < y2; x2++) {
					int y1 = x2 + 1;
					chartCompleter.combineForms(x1, x2, y1, y2, z1, z2);
				}
				// Annotate combinations with unary rules
				chartCompleter.annotateForm(x1, y2);
			}
		}

		// Glue forms
		if (gluingFlag && chartCompleter.isEmpty(0, size - 1)) {
			for (int y2 = 1; y2 < size; y2++) {
				for (int x1 = y2 - 1; x1 >= 0; x1--) {
					int z1 = x1;
					int z2 = y2;
					for (int x2 = x1; x2 < y2; x2++) {
						int y1 = x2 + 1;
						chartCompleter.glueForms(x1, x2, y1, y2, z1, z2);
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
		List<Symbol> symbols = new ArrayList<Symbol>();
		List<Double> scores = new ArrayList<Double>();
		ChartCompleter chartCompleter = product.getChartCompleter();
		// unpack top
		List<ScoredSymbol> edges = lazyUnpacking ? chartCompleter.lazyUnpack(0,
				size - 1) : chartCompleter.unpack(0, size - 1);
		// add signs for unpacked edges
		for (ScoredSymbol edge : edges) {
			symbols.add(edge.symbol);
			scores.add(edge.score);
		}
		// check non-empty
		if (symbols.size() == 0) {
			throw new ParseException("Unable to parse");
		}
		product.setSymbols(symbols);
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
	public void addSupertaggerLogProbs(Symbol gold) {
		List<Word> words = gold.getWords();
		supertagger.mapWords(words);
		addSupertaggerLogProbs(gold, gold);
		for (int i = 0; i < words.size(); i++) {
			supertagger.setWord(i);
		}
	}

	// recurses through derivation, adding lex log probs to lexical signs
	private void addSupertaggerLogProbs(Symbol gold, Symbol current) {
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
			Symbol[] inputs = current.getDerivationHistory().getInputs();
			for (Symbol s : inputs)
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
	public Pair<Symbol, Boolean> oracleBest(LF goldLF) {
		Symbol retval = null;
		List<Symbol> result = product.getSymbols();
		double bestF = 0.0;
		for (Symbol sign : result) {
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
		return new Pair<Symbol, Boolean>(retval, (bestF == 1.0));
	}

}
