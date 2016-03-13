///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004 University of Edinburgh (Michael White)
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
package opennlp.ccg.test;

import opennlp.ccg.realize.*;
import opennlp.ccg.grammar.*;
import opennlp.ccg.synsem.*;
import opennlp.ccg.ngrams.*;

import org.jdom.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Generates initial target sentences from a list of logical forms. 
 * The input is an XML file with a list of top-level LF elements.
 * The output is a testbed file, with the number of parses just 
 * set to 1 (rather than actually being computed).
 * The best realization is determined using n-grams from an 
 * existing testbed file.
 *
 * @author      Michael White
 * @version     $Revision: 1.9 $, $Date: 2009/12/21 04:18:31 $
 */
public class GenTargets
{
    /** The grammar. */
    private Grammar grammar; 

    /** The realizer instance. */
    private Realizer realizer;
    
    /** The n-gram scorer. */
    private NgramScorer ngramScorer;
    
    /** The unique target strings. */
    private Set<String> uniqueTargets = new HashSet<String>();
    
    /** 
     * Constructor: loads grammar, instantiates realizer, and sets up n-gram scorer 
     * using targets with sem class replacement from the testbed.
     */
    private GenTargets(URL grammarURL, File regressionFile) throws IOException {

        // load grammar
        System.out.println("Loading grammar from: " + grammarURL);
        grammar = new Grammar(grammarURL);

        // set up n-gram scorer
        System.out.println("Loading target phrases from: " + regressionFile);
        RegressionInfo rinfo = new RegressionInfo(grammar, regressionFile);
        String[] targets = new String[rinfo.numberOfItems()];
        for (int i=0; i < targets.length; i++) {
            String target = rinfo.getItem(i).sentence;
            targets[i] = target;
        }
        // use targets with sem class replacement
        ngramScorer = new NgramPrecisionModel(targets, true);
        
        // instantiate realizer        
        realizer = new Realizer(grammar);
    }
    
    // does realization, adds test case
    private void realize(Element lfElt, Element outRoot) throws IOException {
        // get LF
        LF lf = Realizer.getLfFromElt(lfElt);
        
        // run request
        realizer.realize(lf, ngramScorer); 
        Chart chart = realizer.getChart();
        
        // make test item (w/o trying to figure out the correct number of parses)
        String target = chart.bestEdge.getSign().getOrthography();
        if (uniqueTargets.contains(target)) {
            System.out.println("Duplicate realization: " + target);
            return;
        }
        uniqueTargets.add(target);
        System.out.println("Best realization: " + target);
        Element testElt = RegressionInfo.makeTestItem(grammar, target, 1, lf); 
        
        // add to output
        outRoot.addContent(testElt);
        if (!chart.bestEdge.complete()) {
            System.out.println("NB: realization incomplete!");
            testElt.setAttribute("complete", "false");
        }
    }
    
    
    /** Creates generator and runs it on the given input file. */    
    @SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
        
        String usage = "Usage: java opennlp.ccg.test.GenTargets (-g <grammarfile>) (-tb <testbedfile>) <inputfile> <outputfile>";
        
        if (args.length > 0 && args[0].equals("-h")) {
            System.out.println(usage);
            System.exit(0);
        }

        // args
        String grammarfile = "grammar.xml";
        String testbedfile = "testbed.xml";
        String inputfile = null;
        String outputfile = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-g")) {
                grammarfile = args[++i]; continue; 
            }
            if (args[i].equals("-tb")) {
                testbedfile = args[++i]; continue; 
            }
            if (inputfile == null) {
                inputfile = args[i]; continue;
            }
            if (outputfile == null) {
                outputfile = args[i]; continue;
            }
        }
        if (inputfile == null || outputfile == null) {
            System.out.println(usage);
            System.exit(0);
        }

        // create Generator
        File gFile = new File(grammarfile);
        URL grammarURL = gFile.toURI().toURL();
        File tbFile = new File(testbedfile);
        if (!tbFile.exists()) {
            tbFile = new File(gFile.getParentFile(), testbedfile);
        }
        GenTargets gen = new GenTargets(grammarURL, tbFile);
        
        // load input LFs
        System.out.println("Loading LFs from: " + inputfile);
        Document doc = gen.grammar.loadFromXml(inputfile);
        
        // create output doc
        Document outDoc = new Document();
        Element outRoot = new Element("regression");
        outDoc.setRootElement(outRoot);

        // realize each one
        System.out.println("Realizing LFs ...");
        Element root = doc.getRootElement();
        List<Element> lfElts = root.getChildren("lf");
        for (int i = 0; i < lfElts.size(); i++) {
            Element lfElt = (Element) lfElts.get(i); 
            try {
                gen.realize(lfElt, outRoot);
            }
            catch (Exception exc) {
                System.out.println("Warning: unable to realize LF " + i + ": " + exc);
            }
        }
        
        // save file
        System.out.println("Saving results to: " + outputfile);
        FileOutputStream out = new FileOutputStream(outputfile); 
        gen.grammar.serializeXml(outDoc, out);
        out.close();
        
        System.out.println("Done.");
    }
}

