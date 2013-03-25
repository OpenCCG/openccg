///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2009 Michael White (The Ohio State University)
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

package opennlp.ccg.hylo;

import opennlp.ccg.synsem.*;
import opennlp.ccg.util.Pair;

import java.text.NumberFormat;
import java.util.*;

/**
 * A class implementing LF scoring in terms of recall and precision 
 * of elementary predications.
 *
 * @author      Michael White
 * @version     $Revision: 1.5 $, $Date: 2010/11/15 03:21:12 $
 **/
public class EPsScorer {

	/**
	 * Class for scoring results.
	 */
	public static class Results {
		// results
		public double recall = 0.0;
		public double precision = 0.0;
		public double fscore = 0.0;
		public double depsRecall = 0.0;
		public double depsPrecision = 0.0;
		public double depsFscore = 0.0;
		public double unlabeledDepsRecall = 0.0;
		public double unlabeledDepsPrecision = 0.0;
		public double unlabeledDepsFscore = 0.0;
		// display
		public String toString() {
			return "fscore: " + nf.format(fscore) + 
				"  recall: " + nf.format(recall) + 
				"  precision: " + nf.format(precision) + 
				"  deps fscore: " + nf.format(depsFscore) + 
				"  deps recall: " + nf.format(depsRecall) + 
				"  deps precision: " + nf.format(depsPrecision) +
				"  unlabeled deps fscore: " + nf.format(unlabeledDepsFscore) + 
				"  unlabeled deps recall: " + nf.format(unlabeledDepsRecall) + 
				"  unlabeled deps precision: " + nf.format(unlabeledDepsPrecision);
		}
	    // formats to four decimal places
	    private static final NumberFormat nf = initNF();
	    private static NumberFormat initNF() { 
	        NumberFormat f = NumberFormat.getInstance();
	        f.setMinimumIntegerDigits(1);
	        f.setMinimumFractionDigits(1);
	        f.setMaximumFractionDigits(4);
	        return f;
	    }
	}

	/**
	 * Returns the results of scoring an LF against a gold LF.
	 */
	public static Results score(LF lf, LF goldLF) {
		// get EPs
		List<SatOp> eps = HyloHelper.flatten(lf);
		List<SatOp> goldEPs = HyloHelper.flatten(goldLF);
		Set<SatOp> epsSet = new HashSet<SatOp>(eps);
		Set<SatOp> goldEPsSet = new HashSet<SatOp>(goldEPs);
		// get unlabeled deps
		Set<Pair<Nominal,Nominal>> unlabeledDepsSet = new HashSet<Pair<Nominal,Nominal>>();
		Set<Pair<Nominal,Nominal>> goldUnlabeledDepsSet = new HashSet<Pair<Nominal,Nominal>>();
		for (SatOp ep : eps) {
			Pair<Nominal,Nominal> dep = getDep(ep);
			if (dep != null) unlabeledDepsSet.add(dep);
		}
		for (SatOp ep : goldEPs) {
			Pair<Nominal,Nominal> dep = getDep(ep);
			if (dep != null) goldUnlabeledDepsSet.add(dep);
		}
		// calc recall
		Results retval = new Results();
		int recalled = 0, depsRecalled = 0, unlabeledDepsRecalled = 0;
		int goldDeps = goldUnlabeledDepsSet.size();
		for (SatOp ep : goldEPs) {
			boolean isdep = HyloHelper.isRelPred(ep);
			if (epsSet.contains(ep)) {
				recalled++;
				if (isdep) depsRecalled++;
			}
			if (isdep && unlabeledDepsSet.contains(getDep(ep))) unlabeledDepsRecalled++;
		}
		retval.recall = 1.0 * recalled / goldEPs.size();
		retval.depsRecall = (goldDeps > 0) ? 1.0 * depsRecalled / goldDeps : 1.0;
		retval.unlabeledDepsRecall = (goldDeps > 0) ? 1.0 * unlabeledDepsRecalled / goldDeps : 1.0;
		// calc precision
		int precise = 0, depsPrecise = 0, unlabeledDepsPrecise = 0;
		int lfDeps = unlabeledDepsSet.size();
		for (SatOp ep : eps) {
			boolean isdep = HyloHelper.isRelPred(ep);
			if (goldEPsSet.contains(ep)) {
				precise++;
				if (isdep) depsPrecise++;
			}
			if (isdep && goldUnlabeledDepsSet.contains(getDep(ep))) unlabeledDepsPrecise++;
		}
		retval.precision = 1.0 * precise / eps.size();
		retval.depsPrecision = (lfDeps > 0) ? 1.0 * depsPrecise / lfDeps : 1.0;
		retval.unlabeledDepsPrecision = (lfDeps > 0) ? 1.0 * unlabeledDepsPrecise / lfDeps : 1.0;
		// calc f-score
		retval.fscore = fscore(retval.recall, retval.precision);
		retval.depsFscore = fscore(retval.depsRecall, retval.depsPrecision);
		retval.unlabeledDepsFscore = fscore(retval.unlabeledDepsRecall, retval.unlabeledDepsPrecision);
		// done
		return retval;
	}

	// returns an unlabeled dependency as a pair of nominals, or null if the ep is not relational
	private static Pair<Nominal,Nominal> getDep(SatOp ep) {
		if (HyloHelper.isRelPred(ep)) {
			// put nominals in canonical order, so that direction of dependency doesn't matter
			Nominal n1 = HyloHelper.getPrincipalNominal(ep);
			Nominal n2 = HyloHelper.getSecondaryNominal(ep);
			if (n1.compareTo(n2) <= 0) return new Pair<Nominal,Nominal>(n1, n2);
			else return new Pair<Nominal,Nominal>(n2, n1);
		}
		else
			return null;
	}
	
	/** Calculates f-score as balanced harmonic mean of recall and precision. */
	public static double fscore(double recall, double precision) {
		if (recall + precision == 0.0) return 0.0;
		return 2.0 * recall * precision / (recall + precision);
	}
}
