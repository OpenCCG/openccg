///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2011 Michael White
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

import java.util.*;

import opennlp.ccg.synsem.*;

/** 
 * A class for tracking semantic dependencies between lexical items.
 * A lex dependency is a triple consisting of a lexical head, a relation 
 * and a lexical dependent.  Either the head or dependent can be null, in 
 * which case the dependency is considered unfilled.  To be equal, a lex 
 * dependency must have identical heads and dependents and equal relations.
 * Relations can involve chains of individual relations (concatenated with dots) 
 * when there are intervening nominals for the same lexical item.
 * 
 * @author Michael White
 * @version     $Revision: 1.2 $, $Date: 2011/08/26 21:31:52 $
 */ 
public class LexDependency {

	/** The lexical head. */
	public Sign lexHead;
	
	/** The relation. */
	public String rel;
	
	/** The lexical dependent. */
	public Sign lexDep;
	
	/** Constructor. */
	public LexDependency(Sign lexHead, String rel, Sign lexDep) {
		this.lexHead = lexHead; this.rel = rel; this.lexDep = lexDep;
	}
	
	/** Hash code. */
	public int hashCode() {
		int retval = rel.hashCode();
		if (lexHead != null) retval += 31 * lexHead.hashCode();
		if (lexDep != null) retval += 7 * lexDep.hashCode();
		return retval;
	}
	
	/** Equals. */
	public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof LexDependency)) return false;
        LexDependency dep = (LexDependency) obj;
		return lexHead == dep.lexHead && lexDep == dep.lexDep && rel.equals(dep.rel);
	}
	
	/** toString. */
	public String toString() {
		StringBuffer retval = new StringBuffer();
		if (lexHead == null) retval.append("null");
		else retval.append(lexHead.getOrthography());
		retval.append('<').append(rel).append('>');
		if (lexDep == null) retval.append("null");
		else retval.append(lexDep.getOrthography());
		return retval.toString();
	}
	
	/** Filled test: neither head nor dependent null. */
	public boolean filled() {
		return lexHead != null && lexDep != null;
	}
	
	/**
	 * Returns a list of unfilled dependencies for a list of EPs 
	 * for a lexical item.
	 */
	public static List<LexDependency> unfilledLexDeps(List<SatOp> preds) {
		List<LexDependency> retval = new ArrayList<LexDependency>(4);
		Map<Nominal,Integer> nomIndex = HyloHelper.nomIndex(preds);
		Set<Nominal> nominals = nomIndex.keySet();
		// special case for indexRels
		if (nominals.size() == 1 && !HyloHelper.isLexPred(preds.get(0))) {
			Sign lexHead = null;
			LexSemOrigin origin = preds.get(0).getOrigin();
			if (origin instanceof Sign) lexHead = (Sign) origin;
			else return retval;
			for (SatOp pred : preds) {
				String rel = HyloHelper.getRel(pred);
				if (rel != null) {
					// add unfilled dep with lex head as dep
					retval.add(new LexDependency(null, rel, lexHead));
					if (HyloHelper.isRelPred(pred)) {
						// also add unfilled dep with lex head as head
						retval.add(new LexDependency(lexHead, rel, null));
					}
				}
			}
			return retval;
		}
		// otherwise, starting with each root nominal, enumerate paths to leaf nominals
		for (Nominal root : nominals) {
			// check for root nominal
			if (!HyloHelper.isRoot(root, preds)) continue;
			// set lex head
			Sign lexHead = null;
			int rootIndex = nomIndex.get(root);
			SatOp rootPred = preds.get(rootIndex);
			if (HyloHelper.isLexPred(rootPred)) {
				LexSemOrigin origin = rootPred.getOrigin();
				if (origin instanceof Sign) lexHead = (Sign) origin;
				rootIndex++;
			}
			// start path for each rel for root nom
			for (int i=rootIndex; i < preds.size() && HyloHelper.getPrincipalNominal(preds.get(i)).equals(root); i++) {
				rootPred = preds.get(i);
				Nominal dep = HyloHelper.getSecondaryNominal(rootPred);
				if (dep == null) continue;
				String rel = HyloHelper.getRel(rootPred);
				addUnfilledLexDep(dep, lexHead, rel, preds, nomIndex, retval);
			}
		}
		return retval;
	}
	
	// recursively adds unfilled lex deps to retval for leaf nominals 
	private static void addUnfilledLexDep(Nominal dep, Sign lexHead, String rel, List<SatOp> preds, Map<Nominal,Integer> nomIndex, List<LexDependency> retval) {
		// if dep not in nom index, then just add unfilled dep for the current rel
		if (!nomIndex.containsKey(dep)) {
			retval.add(new LexDependency(lexHead, rel, null));
			return;
		}
		// otherwise continue with the preds for the current dep
		int depIndex = nomIndex.get(dep);
		SatOp depPred = preds.get(depIndex);
		// if lex head null, add unfilled dep for the current rel, 
		// then update lex head and reset rel
		if (lexHead == null) {
			Sign lexDep = null;
			LexSemOrigin origin = depPred.getOrigin();
			if (origin instanceof Sign) {
				lexDep = (Sign) origin;
				retval.add(new LexDependency(lexHead, rel, lexDep));
			}
			lexHead = lexDep;
			rel = null;
		}
		// then recurse through further rels, if any
		for (int i=depIndex; i < preds.size() && HyloHelper.getPrincipalNominal(preds.get(i)).equals(dep); i++) {
			depPred = preds.get(i);
			Nominal depdep = HyloHelper.getSecondaryNominal(depPred);
			if (depdep == null) continue;
			String relrel = (rel == null) 
				? HyloHelper.getRel(depPred) 
				: rel + "." + HyloHelper.getRel(depPred);
			addUnfilledLexDep(depdep, lexHead, relrel, preds, nomIndex, retval);
		}
	}
	
	
	/**
	 * Returns the filled lexical dependencies from those in the unfilled list 
	 * by checking the list of EPs for ones that have become filled, removing the 
	 * corresponding no longer unfilled deps.  
	 */
	public static List<LexDependency> filledLexDeps(List<LexDependency> unfilled, List<SatOp> preds) {
		List<LexDependency> retval = new ArrayList<LexDependency>(unfilled.size());
		Map<Nominal,Integer> nomIndex = HyloHelper.nomIndex(preds);
		// check each unfilled dep
		for (Iterator<LexDependency> it = unfilled.iterator(); it.hasNext(); ) {
			LexDependency udep = it.next();
			String[] rels = udep.rel.split("\\.");
			// dependent missing case
			if (udep.lexDep == null) {
				// follow rels to descendant pred
				SatOp relPred = findPred(udep.lexHead, rels[0], preds);
				SatOp descendantPred = findDescendantPred(relPred, 0, rels, preds, nomIndex);
				// check if dep filled
				Nominal depnom = HyloHelper.getSecondaryNominal(descendantPred);
				if (!nomIndex.containsKey(depnom)) continue;
				SatOp depPred = preds.get(nomIndex.get(depnom));
				if (HyloHelper.isLexPred(depPred)) {
					// remove dep from unfilled
					it.remove();
					// add filled dep, if lexical
					if (depPred.getOrigin() instanceof Sign) {
						Sign lexDep = (Sign) depPred.getOrigin();
						retval.add(new LexDependency(udep.lexHead, udep.rel, lexDep));
					}
				}
			}
			// head missing case
			else if (udep.lexHead == null) {
				// follow rels to ancestor pred
				SatOp relPred = findPred(udep.lexDep, rels[rels.length-1], preds);
				SatOp ancestorPred = findAncestorPred(relPred, rels.length-1, rels, preds);
				// check if head filled
				Nominal headnom = HyloHelper.getPrincipalNominal(ancestorPred);
				if (!nomIndex.containsKey(headnom)) continue;
				SatOp headPred = preds.get(nomIndex.get(headnom));
				if (HyloHelper.isLexPred(headPred)) {
					// remove dep from unfilled
					it.remove();
					// add filled dep, if lexical
					if (headPred.getOrigin() instanceof Sign) {
						Sign lexHead = (Sign) headPred.getOrigin();
						retval.add(new LexDependency(lexHead, udep.rel, udep.lexDep));
					}
				}
			}
		}
		return retval;
	}
	
	// returns the EP with the given origin and rel, or null if not found
	private static SatOp findPred(Sign origin, String rel, List<SatOp> preds) {
		for (SatOp pred : preds) {
			if (pred.getOrigin() != origin) continue;
			if (rel.equals(HyloHelper.getRel(pred))) return pred;
		}
		return null;
	}
	
	// returns the descendant EP for the given rels, or null if not found
	private static SatOp findDescendantPred(SatOp current, int index, String[] rels, List<SatOp> preds, Map<Nominal,Integer> nomIndex) {
		if (index == rels.length-1) return current;
		// find EP for next rel
		SatOp next = null;
		String rel = rels[++index];
		Nominal depnom = HyloHelper.getSecondaryNominal(current);
		for (int i=nomIndex.get(depnom); i < preds.size() && HyloHelper.getPrincipalNominal(preds.get(i)).equals(depnom); i++) {
			SatOp pred = preds.get(i);
			if (rel.equals(HyloHelper.getRel(pred))) {
				next = pred; break;
			}
		}
		if (next == null) return null;
		// recurse
		return findDescendantPred(next, index, rels, preds, nomIndex);
	}
	
	// returns the ancestor EP for the given rels, or null if not found
	private static SatOp findAncestorPred(SatOp current, int index, String[] rels, List<SatOp> preds) {
		if (index == 0) return current;
		// find EP for previous rel
		SatOp prev = null;
		String rel = rels[--index];
		Nominal headnom = HyloHelper.getPrincipalNominal(current);
		for (int i=0; i < preds.size(); i++) {
			SatOp pred = preds.get(i);
			if (headnom.equals(HyloHelper.getSecondaryNominal(pred)) && rel.equals(HyloHelper.getRel(pred))) {
				prev = pred; break;
			}
		}
		if (prev == null) return null;
		// recurse
		return findAncestorPred(prev, index, rels, preds);
	}
	
	
	/** 
	 * Filters the first list of dependencies to those sharing a head with a dependency in the second list.
	 */
	public static List<LexDependency> filterSameHead(List<LexDependency> deps1, List<LexDependency> deps2) {
		if (deps1.isEmpty() || deps2.isEmpty()) return Collections.emptyList();
		List<LexDependency> retval = new ArrayList<LexDependency>(deps1.size());
		for (LexDependency dep1 : deps1) {
			for (LexDependency dep2 : deps2) {
				if (dep1.lexHead == dep2.lexHead) {
					retval.add(dep1); break;
				}
			}
		}
		return retval;
	}
}
