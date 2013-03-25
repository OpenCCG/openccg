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

package opennlp.ccgbank.extract;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

public class ExtractGrammar {

	/** Class for holding properties of desired grammar extraction. */
	public static class ExtractionProperties {

		/** Whether to use the PP head augmented corpus. */
		public boolean ppHeads = true;

		/** Source directory. */
		public String srcDir = "/scratch/ccgbank/converted";

		/** Destination directory. */
		public String destDir = "/scratch/grammars/protogrammar";

		/** Temp directory. */
		public String tempDir = "/tmp/ccgbankextract";

		/** Start section. */
		public int startSection = 0;

		/** End section. */
		public int endSection = 24;

		/** Selected file (-1 if none). */
		public int fileNum = -1;

		/** Frequency cutoff for including an extracted cat. */
		public int catFreqCutoff = 1;

		/** Frequency cutoff for including an extracted lex, cat, pos triple. */
		public int lexFreqCutoff = 1;

		/** Frequency cutoff for making a family (ie, cat & pos) open. */
		public int openFreqCutoff = 100;

		/** Rule frequency cutoff. */
		public int ruleFreqCutoff = 1;

		/** Flag for whether to skip unmatched rules. */
		public boolean skipUnmatched = false;

		/** Whether to show debug info for failed derivations. */
		public boolean debugDerivs = false;

		/** File name for text only output. */
		public String textfile = null;

		/** File name for text factors output. */
		public String factorsfile = null;

		/** File name for observed supertag-rule combos.. */
		public String combosfile = null;

		// Flag to add feats excl to the lexicon
		public boolean lexF = false;

		// Flag to adjust lfs of orig puncts ie those corrs to extant corp
		// binary rules
		public boolean origPuncts = false;

		// String taking names of macros as input. Expects a dash separated list
		public String macroSpecs = "";

		// String taking names of LF specificity condtions as input. Expects a
		// dash separated list
		// Overt wh pronouns: wh
		public String lfSpecs = "";

	}

	/** Processes args and invokes extraction steps. */
	public static void main(String args[]) throws Exception {

		List<String> arguments = Arrays.asList(args);
		ExtractionProperties extractProps = new ExtractionProperties();

		// flags for each extraction step
		boolean doLex = true;
		boolean doMorph = true;
		boolean doRules = true;
		boolean doTestbed = true;

		if (arguments.contains("-h") || arguments.contains("--help")) {
			System.out.println("usage: extractGrammar \n"
							+ "\t[-noPPs|--noPPHeads] \n"
							+ "\t[-lexF] \n"
							+ "\t[-origPuncts] \n"
							+ "\t[-s|--section sectnum] [-ss|--startSection sectnum] [-es|endSection sectnum] \n"
							+ "\t[-f|--file filenum] \n"
							+ "\t[--lexOnly|--morpOnly|--rulesOnly|--testbedOnly] [--skipLex] [--skipMorph] [--skipRules] [--skipTestbed]\n"
							+ "\t[-tmp|--tempDir tempDir] \n"
							+ "\t[-cfc|--catFreqCutoff num] \n"
							+ "\t[-lfc|--lexFreqCutoff num] \n"
							+ "\t[-ofc|--openFreqCutoff num] \n"
							+ "\t[-rfc|--ruleFreqCutoff num] \n"
							+ "\t[--skipUnmatched] \n"
							+ "\t[-dd|--debugDerivs] \n"
							+ "\t[--text textfile] [--textf factorsfile] \n"
							+ "\t[--combos combosfile] \n"
							+ "\t[srcDir] [destDir]");
			System.exit(0);
		}

		if (arguments.contains("-noPPs") || arguments.contains("--noPPHeads")) {
			extractProps.ppHeads = false;
		}

		// process args
		Iterator<String> it = arguments.iterator();

		String s;
		boolean seenSrc = false;
		while (it.hasNext()) {
			s = it.next();
			if (s.equals("-s") || s.equals("--section") || s.equals("-ss")
					|| s.equals("--startSection") || s.equals("-es")
					|| s.equals("--endSection")) {
				if (!it.hasNext()) {
					throw new IllegalArgumentException("encountered flag " + s
							+ ", but no sectnum specified");
				}
				int sectNum = Integer.parseInt(it.next());
				if (s.equals("-s") || s.equals("--section") || s.equals("-ss")
						|| s.equals("--startSection"))
					extractProps.startSection = sectNum;
				if (s.equals("-s") || s.equals("--section") || s.equals("-es")
						|| s.equals("--endSection"))
					extractProps.endSection = sectNum;
			} else if (s.equals("-f") || s.equals("--filenum")) {
				if (!it.hasNext()) {
					throw new IllegalArgumentException("encountered flag " + s
							+ ", but no filenum specified");
				}
				extractProps.fileNum = Integer.parseInt(it.next());
			} else if (s.equals("-lexF")) {
				System.out
						.println("Inserting lexicon specific feats - Punct filter placeholder feats now");
				extractProps.lexF = true;

			} else if (s.equals("-origPuncts")) {
				extractProps.origPuncts = true;

			}

			else if (s.equals("--lexOnly")) {
				doMorph = false;
				doRules = false;
				doTestbed = false;
			} else if (s.equals("--morphOnly")) {
				doLex = false;
				doRules = false;
				doTestbed = false;
			} else if (s.equals("--rulesOnly")) {
				doLex = false;
				doMorph = false;
				doTestbed = false;
			} else if (s.equals("--testbedOnly")) {
				doLex = false;
				doMorph = false;
				doRules = false;
			} else if (s.equals("--skipLex")) {
				doLex = false;
			} else if (s.equals("--skipMorph")) {
				doMorph = false;
			} else if (s.equals("--skipRules")) {
				doRules = false;
			} else if (s.equals("--skipTestbed")) {
				doTestbed = false;
			} else if (s.equals("-tmp") || s.equals("--tempDir")) {
				if (!it.hasNext()) {
					throw new IllegalArgumentException("encountered flag " + s
							+ ", but no temp dir specified");
				}
				extractProps.tempDir = it.next();
			} else if (s.equals("-cfc") || s.equals("--catFreqCutoff")) {
				if (!it.hasNext()) {
					throw new IllegalArgumentException("encountered flag " + s
							+ ", but no num specified");
				}
				int num = Integer.parseInt(it.next());
				extractProps.catFreqCutoff = num;
			} else if (s.equals("-lfc") || s.equals("--lexFreqCutoff")) {
				if (!it.hasNext()) {
					throw new IllegalArgumentException("encountered flag " + s
							+ ", but no num specified");
				}
				int num = Integer.parseInt(it.next());
				extractProps.lexFreqCutoff = num;
			} else if (s.equals("-ofc") || s.equals("--openFreqCutoff")) {
				if (!it.hasNext()) {
					throw new IllegalArgumentException("encountered flag " + s
							+ ", but no num specified");
				}
				int num = Integer.parseInt(it.next());
				extractProps.openFreqCutoff = num;
			} else if (s.equals("-rfc") || s.equals("--ruleFreqCutoff")) {
				if (!it.hasNext()) {
					throw new IllegalArgumentException("encountered flag " + s
							+ ", but no num specified");
				}
				int num = Integer.parseInt(it.next());
				extractProps.ruleFreqCutoff = num;
			} else if (s.equals("--skipUnmatched"))
				extractProps.skipUnmatched = true;
			else if (s.equals("-dd") || s.equals("--debugDerivs"))
				extractProps.debugDerivs = true;
			else if (s.equals("--text")) {
				if (!it.hasNext()) {
					throw new IllegalArgumentException("encountered flag " + s
							+ ", but no file name specified");
				}
				extractProps.textfile = it.next();
			} else if (s.equals("--textf")) {
				if (!it.hasNext()) {
					throw new IllegalArgumentException("encountered flag " + s
							+ ", but no file name specified");
				}
				extractProps.factorsfile = it.next();
			} else if (s.equals("--combos")) {
				if (!it.hasNext()) {
					throw new IllegalArgumentException("encountered flag " + s
							+ ", but no file name specified");
				}
				extractProps.combosfile = it.next();
			} else if (!seenSrc) {
				extractProps.srcDir = s;
				seenSrc = true;
			} else {
				extractProps.destDir = s;
			}
		}

		// ensure directories exist or can be made
		File tempDir = new File(extractProps.tempDir);
		if (!tempDir.exists() && !tempDir.mkdirs())
			throw new IllegalArgumentException(
					"could not create temp directory: " + extractProps.tempDir);
		File srcDir = new File(extractProps.srcDir);
		if (!srcDir.exists() || !srcDir.isDirectory())
			throw new IllegalArgumentException(
					"source directory does not exist: " + extractProps.srcDir);
		File destDir = new File(extractProps.destDir);
		if (!destDir.exists() && !destDir.mkdirs())
			throw new IllegalArgumentException(
					"could not create destination directory: "
							+ extractProps.destDir);

		// log params
		System.out.println("Extracting Grammar");
		System.out.println("Reading from: " + srcDir);
		System.out.println("Writing to: " + destDir);
		System.out.println("Temp dir: " + tempDir);
		System.out.println("Start section: " + extractProps.startSection);
		System.out.println("End section: " + extractProps.endSection);
		if (extractProps.fileNum >= 0)
			System.out.println("File: " + extractProps.fileNum);

		// do extraction steps
		if (doLex)
			LexExtract.extractLex(extractProps);
		if (doMorph)
			MorphExtract.extractMorph(extractProps);
		if (doRules)
			RulesExtract.extractRules(extractProps);

		// generate grammar.xml, if it doesn't already exist
		// nb: should eventually make schema refs relative to OPENCCG_HOME
		File gramFile = new File(destDir, "grammar.xml");
		if (!gramFile.exists()) {
			System.out.println("Generating grammar.xml");
			PrintWriter gramOut = new PrintWriter(new FileWriter(gramFile));
			gramOut.println("<?xml version=\"1.0\"?>");
			gramOut.println("<grammar name=\"proto\"");
			gramOut.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
			gramOut.println("  xsi:noNamespaceSchemaLocation=\"../grammar.xsd\"");
			gramOut.println(">");
			gramOut.println("  <lexicon file=\"lexicon.xml\"/>");
			gramOut.println("  <morphology file=\"morph.xml\"/>");
			gramOut.println("  <rules file=\"rules.xml\"/>");
			gramOut.println("</grammar>");
			gramOut.close();
		}

		// do testbed
		if (doTestbed && !doTestbed) ; // nb: just avoiding a warning here
		// TODO if (doTestbed) Testbed.createTestFiles(extractProps);
	}

	/* Returns a stream source for the given resource from the class loader. */
	public static StreamSource getSource(String resourceName) {
		ClassLoader cl = ExtractGrammar.class.getClassLoader();
		return new StreamSource(cl.getResourceAsStream(resourceName));
	}
}
