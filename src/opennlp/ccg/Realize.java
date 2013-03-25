///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-7 University of Edinburgh, Michael White
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

import opennlp.ccg.realize.*;
import opennlp.ccg.realize.hypertagger.ZLMaxentHypertagger;
import opennlp.ccg.grammar.*;
import opennlp.ccg.synsem.*;
import opennlp.ccg.ngrams.*;

import org.jdom.*;
import org.jdom.output.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.prefs.*;

/**
 * Sample front-end to the realizer, showing the intermediate steps of realization.
 *
 * @author      Michael White
 * @version     $Revision: 1.38 $, $Date: 2011/08/10 17:58:45 $
 */
public class Realize
{
    private static PrintWriter out;
    
    @SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
        
        String usage = "Usage: java opennlp.ccg.Realize (-g <grammarfile>) (-exactmatches) (-ngramorder N) <inputfile> (<outputfile>)";
        
        if (args.length > 0 && args[0].equals("-h")) {
            System.out.println(usage);
            System.exit(0);
        }
        
        // args
        String grammarfile = "grammar.xml";
        String inputfile = null;
        String outputfile = null;
        boolean exactMatches = false;
        int ngramOrder = 0;
        for (int i = 0; i < args.length; i++) {
        	if (args[i].startsWith("-D")) {
        		String prop = args[i].substring(2); int equalpos = prop.indexOf("=");
        		String key = prop.substring(0, equalpos); String val = prop.substring(equalpos+1);
        		System.setProperty(key, val); continue;
        	}
            if (args[i].equals("-g")) { grammarfile = args[++i]; continue; }
            if (args[i].equals("-exactmatches")) { exactMatches = true; continue; }
            if (args[i].equals("-ngramorder")) { ngramOrder = Integer.parseInt(args[++i]); continue; }
            if (inputfile == null) { inputfile = args[i]; continue; }
            outputfile = args[i];
        }
        if (inputfile == null) {
            System.out.println(usage);
            System.exit(0);
        }
        
        // set out accordingly
        if (outputfile != null) {
            out = new PrintWriter(new BufferedWriter(new FileWriter(outputfile)));
        }
        else {
            out = new PrintWriter(System.out); 
        }
        
        // remember, modify prefs
        Preferences prefs = Preferences.userNodeForPackage(TextCCG.class);
        boolean oldShowCompleteness = prefs.getBoolean(Edge.SHOW_COMPLETENESS, false);
        boolean oldShowBitset = prefs.getBoolean(Edge.SHOW_BITSET, false);
        prefs.putBoolean(Edge.SHOW_COMPLETENESS, true);
        prefs.putBoolean(Edge.SHOW_BITSET, true);
        
        // load grammar
        URL grammarURL = new File(grammarfile).toURI().toURL();
        out.println("Loading grammar from URL: " + grammarURL);
        Grammar grammar = new Grammar(grammarURL);

        // instantiate realizer        
        Realizer realizer = new Realizer(grammar);
        
        // get request
        out.println();
        out.println("Request:");
        out.println();
        Document doc = grammar.loadFromXml(inputfile);
        org.jdom.output.XMLOutputter outputter = new org.jdom.output.XMLOutputter(Format.getPrettyFormat()); 
        out.flush();
        outputter.output(doc, out);
        out.flush();
        
        LF lf = Realizer.getLfFromDoc(doc);
        out.println();
        out.println("** Initial run");
        out.println();
        out.println("Input LF: " + lf);
        
        // set up n-gram scorer
        SignScorer ngramScorer;
        Element root = doc.getRootElement();
        Element ngramModelElt = root.getChild("ngram-model");
        if (ngramModelElt == null) {
            // just use targets
            List<Element> targetElts = root.getChildren("target");
            String[] targets = new String[targetElts.size()];
            out.println();
            out.println("Targets:");
            for (int i=0; i < targetElts.size(); i++) {
                Element ex = (Element) targetElts.get(i);
                String target = ex.getText();
                out.println(target);
                targets[i] = target;
            }
            if (ngramOrder > 0) {
                out.println();
                out.println("Using order " + ngramOrder + " in n-gram precision.");
            	ngramScorer = new NgramPrecisionModel(targets, ngramOrder); 
            }
            else ngramScorer = new NgramPrecisionModel(targets);
            if (exactMatches) {
                out.println();
                out.println("Only counting exact matches in n-gram precision.");
                ((NgramPrecisionModel)ngramScorer).setExactMatches(exactMatches);
            }
        }
        else if (ngramModelElt.getAttributeValue("class") != null) {
            // load scorer from class
            String scorerClass = ngramModelElt.getAttributeValue("class");
            out.println();
            out.println("Instantiating scorer from class: " + scorerClass);
            ngramScorer = (SignScorer) Class.forName(scorerClass).newInstance();
        }
        else {
            // load n-gram model
            String filename = ngramModelElt.getAttributeValue("file");
            String reverseStr = ngramModelElt.getAttributeValue("reverse");
            boolean reverse = (reverseStr != null) ? reverseStr.equals("true") : false; 
            String factoredStr = ngramModelElt.getAttributeValue("factored");
            boolean factored = (factoredStr != null) ? factoredStr.equals("true") : false; 
            String semClassesStr = ngramModelElt.getAttributeValue("sem-classes");
            boolean useSemClasses = (semClassesStr != null) ? semClassesStr.equals("true") : true;
            int order = 3; // order can only be changed for standard n-gram models
            String orderStr = ngramModelElt.getAttributeValue("order"); 
            if (orderStr != null) { order = Integer.parseInt(orderStr); }
            if (ngramOrder > 0) order = ngramOrder; // preference given to command-line value
            out.println();
            String msg = "Loading ";
            if (reverse) msg += "reversed ";
            if (factored) msg += "factored ";
            msg += "n-gram model ";
            if (!factored) msg += "of order " + order + " ";
            if (useSemClasses) msg += "with semantic class replacement ";
            msg += "from: " + filename;
            out.println(msg);
            if (factored)
                ngramScorer = new FactoredNgramModelFamily(filename, useSemClasses);
            else
                ngramScorer = new StandardNgramModel(order, filename, useSemClasses);
            if (reverse) ((NgramScorer)ngramScorer).setReverse(true);
        }
        
        // set pruning strategy (if any)
        Element pruningStrategyElt = root.getChild("pruning-strategy");
        if (pruningStrategyElt != null) {
            // load pruning strategy from class
            String pruningStrategyClass = pruningStrategyElt.getAttributeValue("class");
            out.println();
            out.println("Instantiating pruning strategy from class: " + pruningStrategyClass);
            realizer.pruningStrategy = (PruningStrategy) 
                Class.forName(pruningStrategyClass).newInstance();
        }
        
        // set hypertagger (if any)
        Element htModelElt = root.getChild("ht-model");
        if (htModelElt != null) {
            String htconfig = htModelElt.getAttributeValue("config");
            if (htconfig != null) {
                out.println();
                out.println("Instantiating hypertagger from: " + htconfig);
            	realizer.hypertagger = ZLMaxentHypertagger.ZLMaxentHypertaggerFactory(htconfig);
            }
            else {
	            String htModelClass = htModelElt.getAttributeValue("class");
	            out.println();
	            out.println("Instantiating hypertagger from class: " + htModelClass);
	            realizer.hypertagger = (Hypertagger) Class.forName(htModelClass).newInstance();
            }
        }

        // run request
        realizer.realize(lf, ngramScorer);
        Chart chart = realizer.getChart();
        chart.out = out;

        out.println();
        out.println("Preds:");
        chart.printEPs();
        
        out.println();
        out.println("LF chunks:");
        chart.printLfChunks();

        out.println();
        out.println("LF alts:");
        chart.printLfAlts();

        out.println();
        out.println("LF optional parts:");
        chart.printLfOpts();

        out.println();
        out.println("Initial Edges:");
        chart.printInitialEdges();

        out.println();
        out.println("Marked Edges:");
        chart.printMarkedEdges(); 
        
        out.println();
        out.println("Instantiated Semantically Null Edges:");
        chart.printInstantiatedNoSemEdges();

        out.println();
        out.println("Uninstantiated Semantically Null Edges:");
        chart.printNoSemEdges();

        out.println();
        out.println("Rule Instances:");
        chart.printRuleInstances();

        out.println();
        out.println("All Edges:");
        chart.printEdges();

        out.println();
        out.println("Complete Edges (unsorted):");
        chart.printEdges(true);

        out.println();
        out.println("Complete Edges (sorted):");
        chart.printEdges(true, true);

        out.println();
        out.println("Best Edge:");
        chart.printBestEdge();
        
        out.println();
        out.println("Best Edge Derivation:");
        out.println(chart.bestEdge.getSign().getDerivationHistory());
        out.flush();
        
        if (chart.bestJoinedEdge != null) {
            out.println();
            out.println("Best Joined Edge:");
            chart.printBestJoinedEdge();
        
            out.println();
            out.println("Best Joined Edge Derivation:");
            out.println(chart.bestJoinedEdge.getSign().getDerivationHistory());
            out.flush();
        }


        // reset prefs
        prefs.putBoolean(Edge.SHOW_COMPLETENESS, oldShowCompleteness);
        prefs.putBoolean(Edge.SHOW_BITSET, oldShowBitset);
    }
}
