///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-6 Michael White (University of Edinburgh, The Ohio State University)
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
import opennlp.ccg.util.*;

import java.util.*;

/**
 * A class implementing compaction of flattened LFs.
 *
 * @author      Michael White
 * @version     $Revision: 1.1 $, $Date: 2006/09/04 14:09:10 $
 **/
public class Compacter {

    /**
     * Returns a compacted LF from the given flattened one. 
     * A root nominal may also be given (otherwise null). 
     * Nominals with multiple parents are kept separate.
     * If there are any duplicate predications, an attempt 
     * is made to attach them in different locations.
     */
    static LF compact(LF lf, Nominal root) {
        // get preds, make copies
        List<SatOp> preds = HyloHelper.getPreds(lf);
        for (int i=0; i < preds.size(); i++) {
            SatOp pred = preds.get(i);
            preds.set(i, (SatOp) pred.copy());
        }
        
        // check for single pred
        if (preds.size() == 1) return preds.get(0);
        
        // find unique parents and multiple parents
        Map<Nominal,Nominal> parents = new HashMap<Nominal,Nominal>();
        GroupMap<Nominal,Nominal> multipleParents = new GroupMap<Nominal,Nominal>();
        for (int i = 0; i < preds.size(); i++) {
            SatOp pred = preds.get(i);
            // get principal nominal as nom1
            Nominal nom1 = HyloHelper.getPrincipalNominal(pred);
            // get secondary nominal
            Nominal nom2 = HyloHelper.getSecondaryNominal(pred);
            // skip if none or nom2 equal to root
            if (nom2 == null) continue;
            if (root != null && nom2.equals(root)) continue;
            // if nom2 already in group map, add nom1 as another parent
            if (multipleParents.containsKey(nom2)) {
                multipleParents.put(nom2, nom1);
            }
            // if nom2 already in parent map, add existing parent and nom1 to group map, 
            // record pred, then remove nom2 from parent map
            else if (parents.containsKey(nom2)) {
                multipleParents.put(nom2, parents.get(nom2));
                multipleParents.put(nom2, nom1);
                parents.remove(nom2);
            }
            // otherwise put in nom1 as parent
            else {
                parents.put(nom2, nom1);
            }
        }

        // check multiple parent nominals for cycles
        int prevSize = -1;
        List<Nominal> history = new ArrayList<Nominal>();
        while (multipleParents.size() != prevSize) {
            prevSize = multipleParents.size();
            for (Iterator<Nominal> it = multipleParents.keySet().iterator(); it.hasNext(); ) {
            	Nominal nom = it.next();
                Set<Nominal> nomParents = multipleParents.get(nom);
                for (Iterator<Nominal> it2 = nomParents.iterator(); it2.hasNext(); ) {
                	Nominal parent = it2.next();
                    history.clear();
                    history.add(nom);
                    while (parent != null && !history.contains(parent)) { 
                        history.add(parent);
                        parent = parents.get(parent);
                    }
                    // remove if cycle found
                    if (parent != null) it2.remove();
                }
                // switch to single parent if others removed
                if (nomParents.size() == 1) {
                	Nominal parent = nomParents.iterator().next();
                    parents.put(nom, parent);
                    it.remove();
                }
            }
        }
        
        // break any remaining cycles in parent relationships
        for (Iterator<Nominal> it = parents.keySet().iterator(); it.hasNext(); ) {
        	Nominal nom = it.next();
        	Nominal parent = parents.get(nom);
            history.clear();
            history.add(nom);
            while (parent != null && !history.contains(parent)) {
                history.add(parent);
                parent = parents.get(parent);
            }
            if (parent != null) { it.remove(); } 
        }
        
        // ensure sorted
        HyloHelper.sort(preds);
        
        // combine preds on same nominal
        // also: gather any duplicate preds 
        List<SatOp> combinedPreds = new ArrayList<SatOp>(preds.size());
        List<SatOp> dupPreds = new ArrayList<SatOp>(preds.size());
        SatOp currentSatOp = preds.get(0);
        Nominal currentNominal = currentSatOp.getNominal();
        combinedPreds.add(currentSatOp);
        for (int i = 1; i < preds.size(); i++) {
            SatOp satOp = preds.get(i);
            // skip if equal to previous, saving in dupPreds
            if (satOp.equals(preds.get(i-1))) {
            	dupPreds.add(satOp); continue; 
            }
            // check for different nominal
            Nominal nominal = satOp.getNominal();
            if (!nominal.equals(currentNominal)) {
                // add to combined preds, update current refs
                currentSatOp = satOp;
                currentNominal = nominal;
                combinedPreds.add(currentSatOp);
            }
            // otherwise combine
            else {
                combine(currentSatOp, satOp);
            }
        }
        
        // compact preds with unique parent
        for (int i = 0; i < combinedPreds.size(); i++) {
            SatOp satOp1 = combinedPreds.get(i);
            Nominal nom1 = satOp1.getNominal();
            if (!parents.containsValue(nom1)) continue;
            for (int j = 0; j < combinedPreds.size(); j++) {
                SatOp satOp2 = combinedPreds.get(j);
                Nominal nom2 = satOp2.getNominal();
                if (nom1.equals(nom2)) continue;
                if (!parents.containsKey(nom2)) continue;
                if (nom1.equals(parents.get(nom2))) {
                    subst(satOp1, satOp2, nom2, null);
                }
            }
        }
        
        // get root nominals, root preds, and multiple parent preds
        List<Nominal> roots = new ArrayList<Nominal>();
        List<SatOp> rootPreds = new ArrayList<SatOp>();
        List<SatOp> multipleParentPreds = new ArrayList<SatOp>();
        for (int i = 0; i < combinedPreds.size(); i++) {
            SatOp pred = combinedPreds.get(i);
            Nominal nom = pred.getNominal();
            if (!parents.containsKey(nom) && !multipleParents.containsKey(nom)) {
                roots.add(nom);
                rootPreds.add(pred);
            }
            if (multipleParents.containsKey(nom)) {
                multipleParentPreds.add(pred);
            }
        }
        
        // compact preds with multiple parents, using parent that is closest to a root
        prevSize = -1;
        while (multipleParentPreds.size() != prevSize) {
            prevSize = multipleParentPreds.size();
            // for each nominal with multiple parents
            for (Iterator<SatOp> it = multipleParentPreds.iterator(); it.hasNext(); ) {
                SatOp pred = it.next();
                Nominal nom = pred.getNominal();
                // find parent closest to root, but checking for a parent not below a root
                Set<Nominal> nomParents = multipleParents.get(nom);
                Nominal parentClosestToRoot = null;
                int closestDist = 0;
                int closestRootIndex = -1;
                for (Iterator<Nominal> it2 = nomParents.iterator(); it2.hasNext(); ) {
                    Nominal parent = it2.next();
                    int dist = 0;
                    // trace parents to top ancestor
                    Nominal topAncestor = parent;
                    while (parents.containsKey(topAncestor)) {
                        topAncestor = parents.get(topAncestor);
                        dist++;
                    }
                    // if top ancestor a root, update closest parent
                    if (roots.contains(topAncestor)) {
                        if (parentClosestToRoot == null || dist < closestDist) {
                            parentClosestToRoot = parent; 
                            closestDist = dist;
                            closestRootIndex = roots.indexOf(topAncestor);
                        }
                    }
                    // otherwise set closest dist to -1, to indicate that not all ancestors are roots
                    else { closestDist = -1; }
                }
                // check for a parent not below a root, or no closest root, and skip this nom if so
                if (closestDist == -1 || closestRootIndex == -1) { continue; }
                // otherwise compact under root pred of parent closest to root
                SatOp closestRootPred = rootPreds.get(closestRootIndex);
                subst(closestRootPred, pred, nom, parentClosestToRoot);
                // update parents map
                parents.put(nom, parentClosestToRoot);
                // and remove from iterator
                it.remove();
            }
        }
        
        // set retval to single remaining pred or conjunction of remaining ones
        LF retval;
        List<LF> retPreds = new ArrayList<LF>();
        retPreds.addAll(rootPreds);
        retPreds.addAll(multipleParentPreds);
        if (retPreds.size() == 1) { retval = retPreds.get(0); }
        else { retval = new Op(Op.CONJ, retPreds); }
        
        // tmp
        for (SatOp dup : dupPreds) {
        	Nominal nom = dup.getNominal();
        	Nominal dupParent = findDupParent(retval, dup, nom);
        	subst(retval, dup, nom, dupParent); 
        }

        // return
        return retval;
    }
    
    
    // combines two preds for the same nominal into the first pred, 
    // where either both preds are elementary, 
    // or the first is the result of an earlier combination
    private static void combine(SatOp satOp1, SatOp satOp2) {
        // get args
        LF arg1 = satOp1.getArg();
        LF arg2 = satOp2.getArg();
        // check if arg1 already conj op
        if (arg1 instanceof Op && ((Op)arg1).getName().equals(Op.CONJ)) {
            List<LF> args = ((Op)arg1).getArguments();
            args.add(arg2);
        }
        // or make it one
        else {
            List<LF> args = new ArrayList<LF>(2);
            args.add(arg1); args.add(arg2);
            satOp1.setArg(new Op(Op.CONJ, args));
        }
    }
    
    
    // substitutes the second satop into the first lf at nom2, optionally 
    // respecting the given parent constraint (if non-null)
    // returns whether the substitution has been made
    private static boolean subst(LF lf, SatOp satOp2, Nominal nom2, Nominal requiredParent) {
        return subst(lf, null, satOp2, nom2, requiredParent);
    }
    
    // recursive implementation that tracks the current parent and 
    // returns whether the substitution has been made
    private static boolean subst(LF lf, Nominal currentParent, SatOp satOp2, Nominal nom2, Nominal requiredParent) {
        // recurse to nom2, then append if requiredParent constraint met
        if (lf instanceof SatOp) {
            SatOp satOp = (SatOp) lf;
            return subst(satOp.getArg(), satOp.getNominal(), satOp2, nom2, requiredParent);
        }
        else if (lf instanceof Diamond) {
            Diamond d = (Diamond) lf;
            LF arg = d.getArg();
            // check for nom2, and that requiredParent constraint met
            if (arg.equals(nom2) && (requiredParent == null || requiredParent.equals(currentParent))) {
                // make substitution
                d.setArg(HyloHelper.append(arg, satOp2.getArg()));
                return true;
            }
            else {
                return subst(arg, currentParent, satOp2, nom2, requiredParent);
            }
        }
        else if (lf instanceof Op) {
        	Op op = (Op) lf;
            List<LF> args = op.getArguments();
            for (int i = 0; i < args.size(); i++) {
                LF arg = args.get(i);
                if (arg instanceof Nominal) {
                    // check for nom2, and that requiredParent constraint met
                    if (arg.equals(nom2) && (requiredParent == null || requiredParent.equals(currentParent))) {
                        // make substitution
                    	// nb: this (rarely used) operation doesn't nec. preserve the sort order, unfortunately
                    	op.appendArgs(satOp2.getArg());
                        return true;
                    }
                    // otherwise, set current parent and continue
                    else {
	                    currentParent = (Nominal) arg;
	                    continue;
                    }
                }
                boolean madeSubst = subst(arg, currentParent, satOp2, nom2, requiredParent);
                if (madeSubst) return true;
            }
        }
        return false;
    }
    
    
    // returns a parent nominal where the duplicate pred can be substituted 
    // if there is no equivalent pred there already; otherwise returns null
    private static Nominal findDupParent(LF lf, SatOp dup, Nominal dupNom) {
        return findDupParent(lf, null, dup, dupNom);
    }
    
    // recursive implementation that tracks the current parent
    private static Nominal findDupParent(LF lf, Nominal currentParent, SatOp dup, Nominal dupNom) {
        // recurse to dupNom, then return parent if apropos
        if (lf instanceof SatOp) {
            SatOp satOp = (SatOp) lf;
            return findDupParent(satOp.getArg(), satOp.getNominal(), dup, dupNom);
        }
        else if (lf instanceof Diamond) {
            Diamond d = (Diamond) lf;
            LF arg = d.getArg();
            // check for dupNom by itself, and return parent
            if (arg.equals(dupNom)) return currentParent;
            else return findDupParent(arg, currentParent, dup, dupNom);
        }
        else if (lf instanceof Op) {
        	Op op = (Op) lf;
            List<LF> args = op.getArguments();
            for (int i = 0; i < args.size(); i++) {
                LF arg = args.get(i);
                if (arg instanceof Nominal) {
                    // check for dupNom, and that no equiv pred constraint met
                    if (arg.equals(dupNom) && !args.contains(dup.getArg()))
                    	// return parent
                    	return currentParent;
                    // otherwise, set current parent and continue
                    else {
	                    currentParent = (Nominal) arg;
	                    continue;
                    }
                }
                Nominal retval = findDupParent(arg, currentParent, dup, dupNom);
                if (retval != null) return retval;
            }
        }
        return null;
    }
}
