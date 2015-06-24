///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2005-2009 Scott Martin, Rajakrishan Rajkumar and Michael White
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

//Program which creates a temp.xml file from the bareparse. temp.xml serves are the input for creating lexicon.xml & morph.xml

package opennlp.ccgbank.extract;

import java.io.*;
import java.util.*;

import opennlp.ccgbank.CCGBankTaskSources;
import opennlp.ccgbank.CCGBankTaskTestbed;
import opennlp.ccg.grammar.Grammar;
import opennlp.ccg.grammar.RuleGroup;
import opennlp.ccg.hylo.*;
import opennlp.ccg.lexicon.*;
import opennlp.ccg.parse.ParseException;
import opennlp.ccg.synsem.*;
import opennlp.ccg.test.*;
import opennlp.ccg.unify.*;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Creates test files under in 'test' dir under extracted grammar.
 */
public class Testbed {

	// the grammar
	private Grammar grammar;
	private Lexicon lexicon;
	private RuleGroup rules;

	// supertagger stand-in
	private SupertaggerStandIn supertaggerStandIn = new SupertaggerStandIn();
	
	// results of following deriv
	private Sign sign = null;

	private LF lf = null;

	private String str = "";

	private int numParses = 0;

	private String header = "";

	// Store info related to treenodes in the xml deriv
	private static ArrayList<String> treeInfo = new ArrayList<String>();

	private static boolean treeInfoFlag = false;

	// Store details of preds (nomId key: pos,stag, pos and pred name)
	private static Hashtable<String, String> predInfo = new Hashtable<String, String>();

	// supertag-rule combos
	private Set<String> combos = null;

	Set<CCGBankTaskSources> sourcesSet;

	CCGBankTaskTestbed ccgBankTaskTestbed;

	File grammarFile, targetDirectory;

	// constructor
	public Testbed(Set<CCGBankTaskSources> sourcesSet, File targetDirectory,
			CCGBankTaskTestbed testbed) throws IOException {
		grammarFile = new File(targetDirectory, "grammar.xml");

		this.grammar = new Grammar(grammarFile.toURI().toURL(), true);
		this.lexicon = grammar.lexicon;
		this.rules = grammar.rules;

		this.sourcesSet = sourcesSet;
		this.targetDirectory = targetDirectory;
		this.ccgBankTaskTestbed = testbed;
	}

	// main method for creating test files
	@SuppressWarnings("rawtypes")
	public void createTestFiles() throws IOException, JDOMException {

		ccgBankTaskTestbed.log("Creating test files:");

		// config grammar
		Tokenizer tokenizer = grammar.lexicon.tokenizer;
		grammar.prefs.showFeats = true;
		grammar.prefs.showSem = ccgBankTaskTestbed.isShowsSem();

		// ensure test dir exists
		File testDir = new File(targetDirectory, "test");
		testDir.mkdirs();
		ccgBankTaskTestbed.log("Writing test files to: " + testDir.getPath());

		// text, class-replaced text factors etc. output
		PrintWriter textPW = null;
		PrintWriter textscPW = null;
		PrintWriter factorsPW = null;
		PrintWriter combosPW = null;
		PrintWriter predsPW = null;
		PrintWriter treePW = null;
		File textFile = ccgBankTaskTestbed.getText();
		File factorsFile = ccgBankTaskTestbed.getFactors();
		File combosFile = ccgBankTaskTestbed.getCombos(); 
		File predsFile = ccgBankTaskTestbed.getPreds(); 
		File treeFile = ccgBankTaskTestbed.getTree();
		
		if (textFile != null) {
			File textscFile=new File(textFile.getParent()+"/"+textFile.getName().replaceFirst("text-","textsc-"));
			ccgBankTaskTestbed.log("Writing text to: " + textFile);
			ccgBankTaskTestbed.log("Writing class-replaced text to: " + textscFile);
            textFile.getParentFile().mkdirs(); 
			textPW = new PrintWriter(new BufferedWriter(new FileWriter(textFile)));
			textscPW = new PrintWriter(new BufferedWriter(new FileWriter(textscFile)));
		}
		if (factorsFile != null) {
			ccgBankTaskTestbed.log("Writing factors to: " + factorsFile);
            factorsFile.getParentFile().mkdirs();
			factorsPW = new PrintWriter(new BufferedWriter(new FileWriter(factorsFile)));
		}
		if (combosFile != null) {
			ccgBankTaskTestbed.log("Writing supertag-rule combos to: " + combosFile);
            combosFile.getParentFile().mkdirs(); 
			combos = new HashSet<String>();
			combosPW = new PrintWriter(new BufferedWriter(new FileWriter(combosFile)));
		}
		if (predsFile != null) {
			ccgBankTaskTestbed.log("Writing preds to: " + predsFile);
			predsFile.getParentFile().mkdirs();
			predsPW = new PrintWriter(new BufferedWriter(new FileWriter(predsFile)));
		}
		if (treeFile != null) {
			ccgBankTaskTestbed.log("Writing tree node info to: " + treeFile);
            treeFile.getParentFile().mkdirs();
			treePW = new PrintWriter(new BufferedWriter(new FileWriter(treeFile)));
			treeInfoFlag = true;
		}

		// jdom stuff
		SAXBuilder builder = new SAXBuilder();
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

		// counters
		int numWithLFs = 0;
		int numSingleRootLFs = 0;
		int numWithoutLFs = 0;

		for (CCGBankTaskSources sources : sourcesSet) {
			for (File file : sources) {
				File testSectDir = new File(testDir, file.getParentFile().getName());
				testSectDir.mkdir();

				ccgBankTaskTestbed.log("Debug Print: " + testSectDir.getAbsolutePath());

				// parse derivations
				Document inDoc = builder.build(file);
				Element inRoot = inDoc.getRootElement();

				// make test doc, sign map
				Document outDoc = new Document();
				Element outRoot = new Element("regression");
				outDoc.setRootElement(outRoot);
				Map<String,Sign> signMap = new HashMap<String,Sign>();

				// loop through derivations, making test items
				List derivElts = inRoot.getChildren();
				for (Object derivObj : derivElts) {
					Element derivElt = (Element) derivObj;
					followDeriv(derivElt);
					if (lf != null) {
						numWithLFs++;
						// check for single root
						if (lf instanceof SatOp)
							numSingleRootLFs++;
						// add test item, sign
						Element item = RegressionInfo.makeTestItem(grammar, str, numParses, lf);
						if (header == null) {
							header = "missing";
							ccgBankTaskTestbed.log("Warning: missing header in " + file);
						}
						item.setAttribute("info", header);
						if (header != null) signMap.put(header, sign);

						// Add parsed words as a separate LF element
						Element fullWordsElt = new Element("full-words");
						fullWordsElt.addContent(tokenizer.format(sign.getWords()));

						// Add info about LF lexical preds as a separate element
						Element predInfoElt = new Element("pred-info");
						String predInfoText = collectPredInfo(header);
						predInfoElt.setAttribute("data", predInfoText);

						item.addContent(fullWordsElt);
						item.addContent(predInfoElt);
						outRoot.addContent(item);

						// append to text, factors files
						if (textPW != null)
							textPW.println(str);
						
						 if (textscPW != null) {
                             textscPW.flush();
                             String textsc="";
                             //Note sem class replacement works only for NE classes spec in the grammar file
                             textsc=tokenizer.getOrthography((List<Word>)sign.getWords(),true);
                             textscPW.println(textsc);
                             textscPW.flush();
						 }

						
						if (factorsPW != null)
							factorsPW.println(tokenizer.format(sign.getWords()));
						// append new combos to combos file
						if (combosPW != null) {
							for (String combo : newCombos()) combosPW.println(combo);
						}
						// also to preds
						if (predsPW != null)
							predsPW.println(predInfoText);

						if (treePW != null) {
							for (String info : treeInfo) {
								treePW.println(info);
								treePW.flush();
							}
						}
						treeInfo = new ArrayList<String>();
					} else
						numWithoutLFs++;
				}

				// write test doc, saved signs
				File regressionFile = new File(testSectDir, file.getName());
				outputter.output(outDoc, new FileOutputStream(regressionFile));
				RegressionInfo.writeSerFile(signMap, regressionFile);
			}
		}

		// flush text, factors, combos, preds, tree files
		if (textPW != null) {
			textPW.flush();
			textPW.close();
		}
		if (factorsPW != null) {
			factorsPW.flush();
			factorsPW.close();
		}
		if (combosPW != null) {
			combosPW.flush();
			combosPW.close();
		}
		if (predsPW != null) {
			predsPW.flush();
			predsPW.close();
		}
		if (treePW != null) {
			treePW.flush();
			treePW.close();
		}

		// summary
		ccgBankTaskTestbed.log("numWithLFs: " + numWithLFs);
		ccgBankTaskTestbed.log("numSingleRootLFs: " + numSingleRootLFs);
		ccgBankTaskTestbed.log("numWithoutLFs: " + numWithoutLFs);
		ccgBankTaskTestbed.log("total: " + (numWithLFs + numWithoutLFs));
	}

	private void followDeriv(Element derivElt) {
		
		// reset
		sign = null;
		lf = null;
		str = "";
		header = derivElt.getAttributeValue("Header");
		
		// bookkeeping
		UnifyControl.startUnifySequence();
		
		try {

			Category cat = null;
			Nominal index = null;
			LF flatLF = null;

			// recurse through deriv
			SignHash signs = followDerivR(derivElt);
			// set results, using first available sign (ie some arbitrary one)
			if (!signs.isEmpty()) {
				Iterator<Sign> iter = signs.asSignSet().iterator();
				// System.out.println("Processing file no: "+header);
				// Count of single rooted LFs produced by the constrained parser
				int matchSRLF = 0;
				// Check whether any of the signs have a single rooted LF
				while (iter.hasNext()) {
					// System.out.println("Found LF");
					sign = iter.next();
					cat = sign.getCategory();
					index = cat.getIndexNominal();
					flatLF = cat.getLF();
					if (flatLF != null) {
						lf = HyloHelper.compactAndConvertNominals(flatLF, index, sign);
						// Break when the first single rooted LF is encountered
						if (lf instanceof SatOp) {
							matchSRLF++;
							// System.out.println("Single root LF found");
							break;
						}
					}
				}

				// If no single rooted LF is there, using first available sign
				// (ie some arbitrary one)
				if (matchSRLF == 0) {
					sign = signs.asSignSet().iterator().next();
					cat = sign.getCategory();
					index = cat.getIndexNominal();
					flatLF = cat.getLF();
					if (flatLF != null) lf = HyloHelper.compactAndConvertNominals(flatLF, index, sign);
				}

				if (flatLF != null) {
					extrPredInfo(flatLF, "");
				}

				numParses = signs.size();
				str = str.trim();
			}
			
		} catch (ParseException exc) {
			ccgBankTaskTestbed.log("Warning for " + header + ": " + exc.toString());
		}
	}

	// recurse through deriv, returning signs
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SignHash followDerivR(Element derivElt) throws ParseException {
		String eltName = derivElt.getName();
		// follow deriv, applying combinatory rules
		// nb: no checks made for intended deriv!
		if (eltName.equals("Treenode")) {
			String cat = derivElt.getAttributeValue("cat");
			String ntId = derivElt.getAttributeValue("nt_id");
			String simpleCat = derivElt.getAttributeValue("stag");
			List childElts = derivElt.getChildren();
			int numChildren = childElts.size();
			if (numChildren == 0)
				throw new ParseException(header
						+ ": no child elements for TreeNode for cat: " + cat);
			// if no cat element present, adjust list with an initial dummy node, 
			// to avoid code changes in what follows
			Element elt0 = (Element) childElts.get(0);
			String elt0name = elt0.getName();
			if (elt0name.equals("Treenode") || elt0name.equals("Leafnode")) {
				childElts.add(0, new Element("dummy"));
				numChildren++;
			}
			if (numChildren != 2 && numChildren != 3)
				throw new ParseException(header
						+ ": wrong number of child elements: " + numChildren
						+ " for cat: " + cat);
			Element firstInputElt = (Element) childElts.get(1);
			SignHash firstSigns = followDerivR(firstInputElt);
			SignHash retval = new SignHash();
			// unary case
			if (numChildren == 2) {
				// apply rules
				for (Sign s : firstSigns.asSignSet()) {
					List<Sign> results = rules.applyUnaryRules(s);
					for (Sign rSign : results)
						retval.insert(rSign);
				}
				// caution/warn upon failure
				if (!containsCat(retval, simpleCat)) {
					boolean noResults = retval.isEmpty();
					String inCat = firstInputElt.getAttributeValue("cat");
					String msg = "Unable to derive: " + cat + " from: " + inCat;
					if (!noResults)
						ccgBankTaskTestbed.log("Caution for " + header + ": " + msg);
					if (ccgBankTaskTestbed.isDebugDerivations()) {
						ccgBankTaskTestbed.log(header + ": derivation stymied; inputs: ");
						for (Sign s : firstSigns.asSignSet()) {
							ccgBankTaskTestbed.log(s.toString());
						}
						if (!noResults) {
							ccgBankTaskTestbed.log("Outputs: ");
							for (Sign s : retval.asSignSet())
								ccgBankTaskTestbed.log(s.toString());
						}
					}
					if (noResults)
						throw new ParseException("Derivation blocked: " + msg);
				}
			}
			// binary case
			else if (numChildren == 3) {
				Element secondInputElt = (Element) childElts.get(2);
				SignHash secondSigns = followDerivR(secondInputElt);
				// apply rules
				for (Sign sign1 : firstSigns.asSignSet()) {
					for (Sign sign2 : secondSigns.asSignSet()) {
						List<Sign> results = rules.applyBinaryRules(sign1, sign2);
						for (Sign rSign : results)
							retval.insert(rSign);
					}
				}
				// if no results, propagate one input if the other is
				// internal punct
				if (retval.isEmpty()) {
					if (isPunct(secondInputElt))
						return firstSigns;
					else if (isPunct(firstInputElt))
						return secondSigns;
				}
				// caution/warn upon failure
				if (!containsCat(retval, simpleCat)) {
					boolean noResults = retval.isEmpty();
					String inCat1 = firstInputElt.getAttributeValue("cat");
					String inCat2 = secondInputElt.getAttributeValue("cat");
					String msg = "Unable to derive: " + cat + " from: " + inCat1 + " and: " + inCat2;
					if (!noResults)
						ccgBankTaskTestbed.log("Caution for " + header + ": " + msg);
					if (ccgBankTaskTestbed.isDebugDerivations()) {
						ccgBankTaskTestbed.log(header + ": derivation stymied; first inputs: ");
						for (Sign sign1 : firstSigns.asSignSet()) {
							ccgBankTaskTestbed.log(sign1.toString());
						}
						ccgBankTaskTestbed.log("Second inputs: ");
						for (Sign sign2 : secondSigns.asSignSet()) {
							ccgBankTaskTestbed.log(sign2.toString());
						}
						if (!noResults) {
							ccgBankTaskTestbed.log("Outputs: ");
							for (Sign s : retval.asSignSet())
								ccgBankTaskTestbed.log(s.toString());
						}
					}
					if (noResults)
						throw new ParseException("Derivation blocked: " + msg);
				}
			}

			// Store cat ids of tree nodes for printing to aux files
			if (treeInfoFlag) {

				for (Sign s : retval.asSignSet()) {

					Hashtable<String, String> idConvTally = new Hashtable<String, String>();
					Hashtable<String, Integer> freqTally = new Hashtable<String, Integer>();
					ArrayList<String> fullCat = new ArrayList<String>();
					String catId = "";

					Category treeCat = s.getCategory();
					// System.out.println(header+" "+ntId+" "+treeCat);
					recurseCat(treeCat, fullCat, idConvTally, freqTally);
					/*
					 * System.out.println(freqTally);
					 * System.out.println(fullCat); System.out.println('\n');
					 */

					if (fullCat.size() > 1) {

						for (String x : fullCat) {

							String y[] = x.split("_");
							if (y.length == 1) {
								catId = catId + "," + y[0];
								continue;
							}

							int freq = freqTally.get(y[1]);
							freqTally.put(y[1], freq - 1);

							if (x.endsWith("_M") && freq <= 1)
								x = x.replaceFirst("_M", "");

							catId = catId + "," + x;
						}
						catId = catId.replaceFirst(",", "");
						treeInfo.add(header + " " + ntId + " " + catId);
					}
					/*
					 * System.out.println(idConvTally);
					 * System.out.println(fullCat); System.out.println('\n');
					 */
				}

			}

			// done
			return retval;
		}
		// lex lookup
		// nb: not always insisting on right POS, b/c hashing strategy uses
		// surface words,
		// thus doesn't distinguish lex signs based solely on POS
		// nb: might make sense to warn on lex cats with missing semantics
		else if (eltName.equals("Leafnode")) {
			try {
				String lex = derivElt.getAttributeValue("lexeme");
				Word w = lexicon.tokenizer.parseToken(lex);
				str += w.getForm() + " ";
				String cat = derivElt.getAttributeValue("cat");
				String simpleCat = derivElt.getAttributeValue("stag");
				String rel = derivElt.getAttributeValue("rel");
				String indexRel = derivElt.getAttributeValue("indexRel");
				String semClass = "";
				semClass = derivElt.getAttributeValue("class");

				String roles = derivElt.getAttributeValue("argRoles");
				String pos = derivElt.getAttributeValue("pos");
				// nb: for now, need to ignore rel for non-VB pos
				if (!pos.startsWith("VB"))
					rel = null;
				// lex lookup with required supertag
				// NB: there's no guarantee of getting the right arg roles if the word-cat pair is observed 
				lexicon.setSupertagger(supertaggerStandIn);
				supertaggerStandIn.setTag(simpleCat); 
				SignHash lexSigns = lexicon.getSignsFromWord(w);

				if (semClass == null || semClass.length() == 0)
					semClass = "NoClass";

				// add lex signs, filtered by rel, reindexed
				// also check number with matching pos, match on no class
				int matchPOS = 0;
				boolean matchNoClass = false;
				for (Iterator<Sign> it = lexSigns.asSignSet().iterator(); it.hasNext();) {
					Sign s = it.next();

					Word wTemp = s.getWords().get(0);
					String morphClass = wTemp.getSemClass();
					if (morphClass == null || morphClass.length() == 0)
						morphClass = "NoClass";

					Category lexcat = s.getCategory();
					LF lexLF = lexcat.getLF();

					// allow any class if no sem class given
					if (!(semClass.equals("NoClass") || semClass.equals(morphClass))
							|| !containsPred(lexLF, rel)
							|| !containsRoles(lexLF, roles)
							|| !containsRel(lexLF, indexRel, s)) {
						it.remove();
					}
					else {
						UnifyControl.reindex(lexcat);
						if (wTemp.getPOS().equals(pos)) {
							matchPOS++;
							if (semClass.equals("NoClass") && morphClass.equals("NoClass"))
								matchNoClass = true;
						}
					}
				}
				// filter by pos unless none match
				if (matchPOS > 0) {
					for (Iterator<Sign> it = lexSigns.asSignSet().iterator(); it.hasNext();) {
						Sign s = it.next();
						Word wTemp = s.getWords().get(0);
						if (!wTemp.getPOS().equals(pos)) {
							it.remove(); continue;
						}
						// filter by mismatched class if apropos
						if (matchNoClass) {
							String morphClass = wTemp.getSemClass();
							if (morphClass != null && morphClass.length() != 0)
								it.remove();
						}
					}
				}
				if (lexSigns.isEmpty())
					throw new LexException("No matching category " + cat + " for: " + w);
				return lexSigns;
			} catch (LexException exc) {
				// try continuing derivations without lex signs for punctuation,
				// otherwise throw parse exception
				if (isPunct(derivElt)) {
					if (ccgBankTaskTestbed.isDebugDerivations()) {
						ccgBankTaskTestbed.log(header + ": " + exc.toString());
					}
					return new SignHash();
				}
				throw new ParseException(exc.toString());
			} catch (RuntimeException exc) {
				// for other exceptions, throw parse exception
				throw new ParseException(exc.toString());
			}
		} else
			throw new RuntimeException(header + ": unrecognized element in derivation: " + eltName);
	}

	// Recurse through a CCG cat and print out the atomcats and their ids
	private static void recurseCat(Category cat, ArrayList<String> fullCat,
			Hashtable<String, String> idConvTally,
			Hashtable<String, Integer> freqTally) {

		if (cat instanceof ComplexCat) {

			ComplexCat cc = (ComplexCat) cat.copy();
			Category resCat = cc.getResult();

			recurseCat(resCat, fullCat, idConvTally, freqTally);
			int argStart = 0;

			if (resCat instanceof ComplexCat) {
				ComplexCat temp = (ComplexCat) resCat.copy();
				argStart = temp.getArgStack().size();
			}

			ArgStack argStack = cc.getArgStack(argStart);

			for (int i = 0; i < argStack.size(); i++) {
				if (argStack.get(i) instanceof BasicArg) {
					BasicArg bArg = (BasicArg) argStack.get(i);
					Category argCat = (Category) bArg.getCat();
					Slash argSlash = (Slash) bArg.getSlash();
					// System.out.println(argSlash.toString()+'\n');
					fullCat.add(argSlash.toString());
					recurseCat(argCat, fullCat, idConvTally, freqTally);
				}
			}

		} else if (cat instanceof AtomCat) {

			AtomCat ac = (AtomCat) cat.copy();
			FeatureStructure fs = ac.getFeatureStructure();

			if (fs.hasAttribute("index")) {
				String index = fs.getValue("index").toString();
				// System.out.println(index);
				String id[] = index.split(":");

				if (!idConvTally.containsKey(id[0]))
					idConvTally.put(id[0], Integer.toString(idConvTally.size() + 1));
				String numId = idConvTally.get(id[0]);
				String catId = ac.getType() + "_" + numId;

				if (!freqTally.containsKey(numId))
					freqTally.put(numId, 0);

				int freq = freqTally.get(numId);
				freqTally.put(numId, freq + 1);

				if (fs.hasAttribute("mod-index"))
					catId = catId + "_" + "M";

				// System.out.println('\n');

				fullCat.add(catId);
			}
		}
	}

	// returns whether the given LF contains the given the lexical predicate
	private static boolean containsPred(LF lf, String pred) {
		if (pred == null)
			return true;
		if (lf == null)
			return false;
		for (SatOp satOp : HyloHelper.getPreds(lf)) {
			if (HyloHelper.isLexPred(satOp)) {
				if (HyloHelper.getLexPred(satOp).equals(pred))
					return true;
			}
		}
		return false;
	}

	// roles in a given LF
	private static Set<String> rolesSet = new HashSet<String>();

	// returns whether the given LF contains the given the lexical predicate
	private static boolean containsRoles(LF lf, String roles) {
		if (roles == null)
			return true;
		if (lf == null)
			return false;
		String[] rolesArray = roles.split("\\s+");
		// get roles in LF
		rolesSet.clear();
		for (SatOp satOp : HyloHelper.getPreds(lf)) {
			if (HyloHelper.isRelPred(satOp)) {
				rolesSet.add(HyloHelper.getRel(satOp));
			}
		}
		// check presence of roles in LF
		for (String role : rolesArray) {
			if (role.equals("null") || role.equals("e"))
				continue;
			if (!rolesSet.contains(role))
				return false;
		}
		return true;
	}

	// returns whether the given LF contains the given indexRel
	private static boolean containsRel(LF lf, String indexRel, Sign sign) {

		if (indexRel == null)
			return true;
		if (lf == null)
			return false;

		indexRel = "<" + indexRel + ">";

		/*
		 * System.out.println(sign.getSupertag()+" "+sign.getPOS());
		 * System.out.println(indexRel); System.out.println(rolesSet);
		 * System.out.println(lf); System.out.println('\n');
		 */

		// check presence of that rel/feat in LF
		if (!lf.toString().contains(indexRel))
			return false;
		else
			return true;
	}

	// identifies punctuation
	private static boolean isPunct(Element elt) {
		String pos = elt.getAttributeValue("pos");
		if (pos == null)
			return false;

		return (pos.equals("|") || pos.equals(".") || pos.equals(",")
				|| pos.equals(";") || pos.equals(":") || pos.equals("LRB")
				|| pos.equals("RRB") || pos.equals("``") || pos.equals("''"));
	}

	// return whether signs contains cat; filter if so
	private static boolean containsCat(SignHash signs, String cat) {
		// special case: give free pass to cats with dollars
		if (!signs.isEmpty() && cat.indexOf('$') >= 0)
			return true;
		// check for cat
		boolean retval = false;
		for (Sign sign : signs.asSignSet()) {
			String supertag = sign.getCategory().getSupertag();
			// again, give free pass to cats with dollars
			if (supertag.indexOf('$') >= 0 || cat.equals(supertag)) {
				retval = true;
				break;
			}
		}
		// filter if found
		if (retval) {
			for (Iterator<Sign> it = signs.asSignSet().iterator(); it.hasNext();) {
				Sign sign = it.next();
				String supertag = sign.getCategory().getSupertag();
				if (supertag.indexOf('$') >= 0 || cat.equals(supertag))
					continue;
				else
					it.remove();
			}
		}
		return retval;
	}

	// returns new combos for current sign
	private List<String> newCombos() {
		List<String> retval = new ArrayList<String>();
		newCombos(sign, retval);
		return retval;
	}

	// recursively adds new combos for given sign
	private void newCombos(Sign s, List<String> retval) {
		Sign[] inputs = s.getDerivationHistory().getInputs();
		if (inputs != null) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < inputs.length; i++) {
				sb.append(inputs[i].getCategory().getSupertag()).append(' ');
			}
			sb.append(s.getDerivationHistory().getRule().name());
			String combo = sb.toString();
			if (!combos.contains(combo)) {
				retval.add(combo);
				combos.add(combo);
			}
			for (int i = 0; i < inputs.length; i++) {
				newCombos(inputs[i], retval);
			}
		}
	}

	// Extracts nom-id,pos,supertag info related to LF lexical preds
	private void extrPredInfo(LF lf, String sentId) {
		// System.out.println(sentId);
		extractPredInfo(lf, predInfo);
	}

	/**
	 * Extracts the nom id, pos, and supertag info related to LF lexical preds, 
	 * and puts it in the given map keyed off the nom id.
	 * Note that the map should be cleared for each new LF.
	 */
	public static void extractPredInfo(LF lf, Map<String,String> predInfoMap) {

		String predData = "";
		List<SatOp> preds = HyloHelper.getPreds(lf);

		for (SatOp pred : preds) {

			String lexPred = HyloHelper.getLexPred(pred);
			if (lexPred == null)
				continue;

			if (!(pred.getArg() instanceof Proposition))
				continue;

			Proposition p = (Proposition) pred.getArg();

			String lex = (p.getName()).toString();

			// Get supertag & pos tag info and store that
			String stag = pred.getOrigin().getSupertag();
			String pos = pred.getOrigin().getPOS();
			Nominal nom = pred.getNominal();
			String nomInd = nom.toString();
			String nomIndParts[] = nomInd.split(":");

			if (stag == null || pos == null || lex == null)
				continue;

			predData = escape(stag) + ":" + escape(pos) + ":" + escape(lex);
			predInfoMap.put(nomIndParts[0], predData);
		}
	}

	// Collects nom-id,pos,supertag info related to LF lexical preds for this
	// particular LF
	private static String collectPredInfo(String sentId) {

		String predData = "";

		for (Enumeration<String> e = predInfo.keys(); e.hasMoreElements();) {

			String nomId = e.nextElement();
			predData = predData + " " + nomId + ":" + predInfo.get(nomId);
		}

		predInfo = new Hashtable<String, String>();

		return predData.trim();
	}

	/**
	 * Returns the pred info string for the given pred info map (see extractPredInfo).
	 */
	public static String getPredInfo(Map<String,String> predInfoMap) {
		String predData = "";
		for (String nomId : predInfoMap.keySet()) {
			predData = predData + " " + nomId + ":" + predInfoMap.get(nomId);
		}
		return predData.trim();
	}
	
	// escapes a string using DefaultTokenizer
	private static String escape(String s) { return DefaultTokenizer.escape(s); }
	
	// stands in for a supertagger during lex lookup
	private static class SupertaggerStandIn implements SupertaggerAdapter {
		// map for a single key
		private Map<String,Double> map = new HashMap<String,Double>(2);
		public Map<String,Double> getSupertags() { return map; }
		
		// set tag
		void setTag(String tag) { map.clear(); map.put(tag, 1.0); }
		
		// dummy implementations
		public void setIncludeGold(boolean includeGold) {}
		public void resetBeta() {}
		public void resetBetaToMax() {}
		public void nextBeta() {}
		public void previousBeta() {}
		public boolean hasMoreBetas() { return false; }
		public boolean hasLessBetas() { return false; }
		public double[] getBetas() { return new double[]{1.0}; }
		public void setBetas(double[] betas) {}
		public double getCurrentBetaValue() { return 1.0; }
	}
}
