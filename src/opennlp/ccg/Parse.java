///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2010 Michael White
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

package opennlp.ccg;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import opennlp.ccg.grammar.Grammar;
import opennlp.ccg.hylo.HyloHelper;
import opennlp.ccg.hylo.Nominal;
import opennlp.ccg.lexicon.Tokenizer;
import opennlp.ccg.parse.ParseException;
import opennlp.ccg.parse.Parser;
import opennlp.ccg.parse.Supertagger;
import opennlp.ccg.parse.supertagger.WordAndPOSDictionaryLabellingStrategy;
import opennlp.ccg.synsem.Category;
import opennlp.ccg.synsem.LF;
import opennlp.ccg.synsem.Sign;
import opennlp.ccg.synsem.SignScorer;
import opennlp.ccg.test.RegressionInfo;
import opennlp.ccg.test.DerivMaker;
import opennlp.ccgbank.extract.Testbed;

/**
 * Creates a testbed file by parsing a text file.
 * Text is assumed to be tokenized, with one sentence per line.
 *
 * @author      Michael White
 * @version     $Revision: 1.2 $, $Date: 2010/10/28 02:46:32 $
 */
public class Parse {

	public static void main(String[] args) throws IOException {
		
        String usage = "Usage: java opennlp.ccg.Parse \n" + 
        	"  (-g <grammarfile>) \n" + 
        	"  -parsescorer <scorerclass> \n" +
        	"  -supertagger <supertaggerclass> | -stconfig <configfile> \n" +
	        "  (-nbestListSize <nbestListSize>) \n" +
	        "  (-includederivs) \n" +
	        "  (-includescores) \n" +
        	"  <inputfile> <outputfile>";
        
        if (args.length == 0 || args[0].equals("-h")) {
            System.out.println(usage);
            System.exit(0);
        }
        
        // args
        String grammarfile = "grammar.xml";
        String inputfile = null;
        String outputfile = null;
        String parseScorerClass = null;
        String supertaggerClass = null, stconfig = null;
        boolean includederivs = false;
        boolean includescores = false;
        int nbestListSize = 1;
        
        for (int i = 0; i < args.length; i++) {
        	if (args[i].equals("-g")) { grammarfile = args[++i]; continue; }
            if (args[i].equals("-parsescorer")) { parseScorerClass = args[++i]; continue; }
            if (args[i].equals("-supertagger")) { supertaggerClass = args[++i]; continue; }
            if (args[i].equals("-stconfig")) { stconfig = args[++i]; continue; }
            if (args[i].equals("-nbestListSize")) { nbestListSize = Integer.parseInt(args[++i]); continue; }
            if (args[i].equals("-includederivs")) { includederivs = true; continue; }
            if (args[i].equals("-includescores")) { includescores = true; continue; }
            if (inputfile == null) { inputfile = args[i]; continue; }
            outputfile = args[i];
        }
        if (nbestListSize < 1) nbestListSize = 1;

        if (inputfile == null || outputfile == null || 
        	parseScorerClass == null || (supertaggerClass == null && stconfig == null)) 
        {
            System.out.println(usage);
            System.exit(0);
        }
        
		// make test doc, sign map
		Document outDoc = new Document();
		Element outRoot = new Element("regression");
		outDoc.setRootElement(outRoot);
		Map<String,Sign> signMap = new HashMap<String,Sign>();

        // load grammar
        URL grammarURL = new File(grammarfile).toURI().toURL();
        System.out.println("Loading grammar from URL: " + grammarURL);
        Grammar grammar = new Grammar(grammarURL);
        Tokenizer tokenizer = grammar.lexicon.tokenizer;
        System.out.println();
        
        // set up parser
        Parser parser = new Parser(grammar);
        // instantiate scorer
        try {
            System.out.println("Instantiating parsing sign scorer from class: " + parseScorerClass);
            SignScorer parseScorer = (SignScorer) Class.forName(parseScorerClass).newInstance();
            parser.setSignScorer(parseScorer);
            System.out.println();
        } catch (Exception exc) {
            throw (RuntimeException) new RuntimeException().initCause(exc);
        }
        // instantiate supertagger
        try {
        	Supertagger supertagger;
        	if (supertaggerClass != null) {
                System.out.println("Instantiating supertagger from class: " + supertaggerClass);
                supertagger = (Supertagger) Class.forName(supertaggerClass).newInstance();
        	}
        	else {
        		System.out.println("Instantiating supertagger from config file: " + stconfig);
        		supertagger = WordAndPOSDictionaryLabellingStrategy.supertaggerFactory(stconfig);
        	}
            parser.setSupertagger(supertagger);
            System.out.println();
        } catch (Exception exc) {
            throw (RuntimeException) new RuntimeException().initCause(exc);
        }
        
        // loop through input
        BufferedReader in = new BufferedReader(new FileReader(inputfile));
        String line;
        Map<String,String> predInfoMap = new HashMap<String,String>();
        System.out.println("Parsing " + inputfile);
        System.out.println();
        int count = 1;
        while ((line = in.readLine()) != null) {
        	String id = "s" + count;
        	try {
        		// parse it
        		System.out.println(line);
        		parser.parse(line);
        		int numParses = Math.min(nbestListSize, parser.getResult().size());
        		for (int i=0; i < numParses; i++) {
        			Sign thisParse = parser.getResult().get(i);
        			// convert lf
        			Category cat = thisParse.getCategory();
        			LF convertedLF = null;
        			String predInfo = null;
        			if (cat.getLF() != null) {
        				// convert LF
        				LF flatLF = cat.getLF();
        				cat = cat.copy();
        				Nominal index = cat.getIndexNominal(); 
        				convertedLF = HyloHelper.compactAndConvertNominals(flatLF, index, thisParse);
        				// get pred info
        				predInfoMap.clear();
        				Testbed.extractPredInfo(flatLF, predInfoMap);
        				predInfo = Testbed.getPredInfo(predInfoMap);
        			}
        			// add test item, sign
        			Element item = RegressionInfo.makeTestItem(grammar, line, 1, convertedLF);
        			String actualID = (nbestListSize == 1) ? id : id + "-" + (i+1);
        			item.setAttribute("info", actualID);
        			item.setAttribute("test","true");
        			outRoot.addContent(item);
        			signMap.put(actualID, thisParse);
        			// Add parsed words as a separate LF element
        			Element fullWordsElt = new Element("full-words");
        			fullWordsElt.addContent(tokenizer.format(thisParse.getWords()));
        			item.addContent(fullWordsElt);
        			if (predInfo != null) {
        				Element predInfoElt = new Element("pred-info");
        				predInfoElt.setAttribute("data", predInfo);
        				item.addContent(predInfoElt);
        			}
        			if (includederivs) {
        				Element derivElt = new Element("deriv");
        				derivElt.addContent(DerivMaker.makeDeriv(thisParse));
        				item.addContent(derivElt);
        			}
        			if (includescores) {
        				String score = parser.getScores().get(i).toString();
        				item.setAttribute("score", score);
        			}
        		}
        	} catch (ParseException e) {
        		System.out.println("Unable to parse!");
        		// add test item with zero parses
        		Element item = RegressionInfo.makeTestItem(grammar, line, 0, null);
        		item.setAttribute("info", id);
        		outRoot.addContent(item);
        	}
        	count++;
        }
        System.out.println();

		// write test doc, saved signs
        System.out.println("Writing parses to " + outputfile);
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		File regressionFile = new File(outputfile);
		outputter.output(outDoc, new FileOutputStream(regressionFile));
		RegressionInfo.writeSerFile(signMap, regressionFile);
        System.out.println();
		
        // done
        in.close();
        System.out.println("Done.");
	}
}
