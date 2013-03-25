///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-11 University of Edinburgh / Michael White
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

package opennlp.ccg.realize;

import opennlp.ccg.grammar.*;
import opennlp.ccg.synsem.*;
import opennlp.ccg.hylo.*;
import opennlp.ccg.*;
import org.jdom.*;
import java.util.*;
import java.util.prefs.*;

/**
 * The realizer manages the realization process.
 * Realization options may be set for use across calls 
 * to the realizer.
 *
 * @author      Michael White
 * @version     $Revision: 1.31 $, $Date: 2011/07/19 03:40:46 $
 */
public class Realizer
{
    
    /** The grammar used for realization. */
    public final Grammar grammar; 
    
    /** Flag for whether to use depth-first search.  Defaults to false. */
    public boolean depthFirst = false; 
    
    // the chart used to realize a request
    private Chart chart = null;
    
   
    /** Constructor. */
    public Realizer(Grammar grammar) { 
        this.grammar = grammar;
    }
    
    /** Returns the chart used in the latest request, or null if none. */
    public Chart getChart() { return chart; }
    
    
    //-----------------------------------------------------------------
    // default options, for use when not given in realization request
    // nb: as the usual practice is to set these options once 
    //     for reuse across calls to the realizer, only a subset of 
    //     the options may be overridden in different calls to the 
    //     realize method
        
    /** Time limit in ms.  (Default is -1, or none.) */
    public int timeLimitMS = -1;
    
    /** Flag for whether to wait for a complete edge. (Default is false.) */
    public boolean waitForCompleteEdge = false;

    /** Sign scorer to use.  (Default is none.) */
    public SignScorer signScorer = null;
    
    /** Pruning strategy to use. (Default is none.) */
    public PruningStrategy pruningStrategy = null;
    
    /** Hypertagger to use. (Default is none.) */
    public Hypertagger hypertagger = null;
    

    //-----------------------------------------------------------------
    // get LF from doc    
    
    /**
     * Retrieves an input LF from the given XML doc, processing any 
     * LF chunks along the way.
     */
    public static LF getLfFromDoc(Document doc) {
        Element rootElt = doc.getRootElement();
        Element lfElt = (rootElt.getName().equals("lf")) ? rootElt : rootElt.getChild("lf");
        return getLfFromElt(lfElt);
    }

    /**
     * Retrieves an input LF from the given XML element, processing any 
     * LF chunks along the way.
     */
    public static LF getLfFromElt(Element lfElt) {
        HyloHelper.processChunks(lfElt);
        LF lf = HyloHelper.getLF(lfElt);
        return lf;
    }

    
    //-----------------------------------------------------------------
    // realization routines    
    
    /**
     * Realizes the input LF, 
     * returning the best edge found (or null if none).
     */
    public Edge realize(LF lf) {
        return realize(lf, this.signScorer);
    }

    /**
     * Realizes the input LF relative to the given sign scorer, 
     * returning the best edge found (or null if none).
     */
    public Edge realize(LF lf, SignScorer signScorer) {
        Preferences prefs = Preferences.userNodeForPackage(TextCCG.class);
        int timeLimitToUse = (timeLimitMS != -1) 
            ? timeLimitMS
            : prefs.getInt(Chart.TIME_LIMIT, Chart.NO_TIME_LIMIT);
        return realize(lf, signScorer, timeLimitToUse, waitForCompleteEdge);
    }
    
    /**
     * Realizes the input LF relative to given sign scorer, 
     * returning the best edge found (or null if none)
     * in the given time limit (in ms), potentially waiting 
     * longer for a complete edge according to the given flag.
     * If a hypertagger is employed, realization proceeds 
     * iteratively through the available beta-best values 
     * within the overall time or edge limit.
     */
    public Edge realize(LF lf, SignScorer signScorer, int timeLimitMS, boolean waitForCompleteEdge) {
        List<SatOp> preds = HyloHelper.flatten(lf);
        SignScorer scorerToUse = (signScorer != null) 
            ? signScorer : SignScorer.nullScorer;
        PruningStrategy strategyToUse = (pruningStrategy != null) 
            ? pruningStrategy : new NBestPruningStrategy();
        // realize iteratively with hypertagger, if present
        if (hypertagger != null) { 
        	return realizeWithHypertagger(preds, scorerToUse, strategyToUse, timeLimitMS);
        }
        // otherwise make chart, set start time
        long startTime = System.currentTimeMillis(); 
        chart = new Chart(new EdgeFactory(grammar, preds, scorerToUse), strategyToUse);
        chart.startTime = startTime; 
        chart.depthFirst = depthFirst;
        // run request
        chart.initialize();
        chart.combine(timeLimitMS, waitForCompleteEdge);
        // XXX tmp
    	// if no complete edge, try again gluing fragments
//        if (!chart.bestEdge.complete()) {
//        	System.out.println("Trying to glue fragments ...");
//        	chart.reInitForGluing();
//        	chart.combine(timeLimitMS, waitForCompleteEdge);
//        }
        // return best edge
        return chart.bestEdge;
    }
    
    // XXX tmp switch for gluing
    private boolean useGluing = Boolean.getBoolean("useGluing");
    
    // iterate through beta-best values until a complete realization is found; 
    // otherwise return the best fragment using the glue rule, or if all else 
    // fails (or not using gluing), greedy fragment joining
    private Edge realizeWithHypertagger(List<SatOp> preds, SignScorer signScorer, PruningStrategy pruningStrategy, int timeLimitMS) {
        // get start time
        long startTime = System.currentTimeMillis();
        // get edge limit
        Preferences prefs = Preferences.userNodeForPackage(TextCCG.class);
        int edgeLimit = prefs.getInt(Chart.EDGE_LIMIT, Chart.NO_EDGE_LIMIT);
    	// set supertagger in lexicon
    	grammar.lexicon.setSupertagger(hypertagger);
    	// reset beta
    	hypertagger.resetBeta();
        // loop until retval set or need to give up
        Edge retval = null;
        chart = null;
        boolean outOfBetas = false;
        boolean pastTimeLimit = false;
        boolean exceededEdgeLimit = false;
        long iterStartTime = 0, currentTime = 0;
        int iterTime = 0;
        while (retval == null && !outOfBetas && !pastTimeLimit && !exceededEdgeLimit) {
        	// instantiate chart and set start time for this iteration
            chart = new Chart(new EdgeFactory(grammar, preds, signScorer, hypertagger), pruningStrategy);
            iterStartTime = System.currentTimeMillis();
        	// do realization in packing mode to see if a complete realization 
        	// can be found with this hypertagger setting
            chart.usePacking = true; chart.collectCombos = false;
            chart.doUnpacking = false; chart.joinFragments = false;
            // run request
            chart.initialize();
            if (chart.noUncoveredPreds()) 
            	chart.combine(timeLimitMS, false);
            // check time limit
            currentTime = System.currentTimeMillis();
            iterTime = (int) (currentTime - iterStartTime);
            if (timeLimitMS != Chart.NO_TIME_LIMIT && iterTime >= timeLimitMS) {
            	pastTimeLimit = true;
//            	System.out.println("Went past time limit with ht beta: " + hypertagger.getCurrentBetaValue());
            }
            // check edge limit
            if (edgeLimit != Chart.NO_EDGE_LIMIT && chart.numEdges >= edgeLimit) {
            	exceededEdgeLimit = true;
//            	System.out.println("Exceeded edge limit with ht beta: " + hypertagger.getCurrentBetaValue());
            }
            // if complete, unpack and return best edge
            if (chart.bestEdge.complete()) {
            	chart.doUnpacking = true; chart.doUnpacking();
            	retval = chart.bestEdge;
            }
            // otherwise check beta level if still within limits
            else if (!pastTimeLimit && !exceededEdgeLimit) {
            	// progress to next beta setting, if any
            	if (hypertagger.hasMoreBetas()) {
            		hypertagger.nextBeta();
            	}
            	else {
	            	// otherwise out of betas
            		outOfBetas = true;
//	            	System.out.println("Ran out of betas with ht beta: " + hypertagger.getCurrentBetaValue());
            	}
            }
        }
        // if no result, take desperate measures with fragments
        if (retval == null) {
        	// try realization with gluing
            if (useGluing) {
//	            System.out.println("Num edges for final iteration: " + chart.numEdges);
//	            System.out.println("Trying gluing option after iterTime: " + iterTime);
	        	chart.reInitForGluing();
	        	// double time and space limits, to give gluing option some room
	        	chart.edgeLimit = edgeLimit * 2;
	        	chart.combine(timeLimitMS * 2, waitForCompleteEdge);
//	            System.out.println("Num edges after gluing: " + chart.numEdges);
	            currentTime = System.currentTimeMillis();
	            iterTime = (int) (currentTime - iterStartTime);
	            // if complete, unpack and return best edge
	            if (chart.bestEdge.complete()) {
//	                System.out.println("Unpacking in final iteration after iterTime: " + iterTime);
	            	chart.doUnpacking = true; chart.doUnpacking();
	            	retval = chart.bestEdge;
	            }
            }
            // otherwise try a final iteration in an iteration in anytime mode, possibly resorting to joining fragments 
            if (retval == null) {
//                System.out.println("Trying a final iteration in anytime mode after iterTime: " + iterTime);
            	// instantiate chart and set start time for this iteration
                chart = new Chart(new EdgeFactory(grammar, preds, signScorer, hypertagger), pruningStrategy);
                iterStartTime = System.currentTimeMillis();
                // run request
        		chart.usePacking = false; chart.joinFragments = true;
                chart.initialize();
                chart.combine(timeLimitMS, waitForCompleteEdge);
//	            System.out.println("Num edges after anytime iteration: " + chart.numEdges);
	            currentTime = System.currentTimeMillis();
	            iterTime = (int) (currentTime - iterStartTime);
//	            if (chart.bestEdge.complete()) 
//	                System.out.println("Found complete edge after iterTime: " + iterTime);
//	            else
//	                System.out.println("Resorting to joined fragments after iterTime: " + iterTime);
                // return best edge
                retval = chart.bestEdge;
            }
        }
    	// update end time
        long endTime = System.currentTimeMillis();
        chart.timeTilDone = (int) (endTime - startTime);
    	// reset supertagger in lexicon
    	grammar.lexicon.setSupertagger(null);
        // return
    	return retval;
    }
}
