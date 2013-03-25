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

import gnu.trove.*;

/**
 * A class for performing flattening operations on LFs.
 *
 * @author      Michael White
 * @version     $Revision: 1.5 $, $Date: 2011/06/06 18:52:30 $
 **/
public class Flattener {

	// the resulting preds
	private List<SatOp> preds = new ArrayList<SatOp>();
    
	// counter for alts
	private int altCount = 0;
	
	// counter for opts
	private int optCount = 0;
	
	// root preds
	private List<SatOp> roots = new ArrayList<SatOp>();
	
	// map from preds to children from original expression (identity keys);
	// includes dummy parents to preserve structure 
	private ListMap<SatOp,SatOp> childMap = new ListMap<SatOp,SatOp>(true);
    
    // map from nominals to highest pred for that nominal from original expression
	private Map<Nominal,SatOp> nomMap = new HashMap<Nominal,SatOp>();
    
    // map from pred to depth in original expression
	private Map<Nominal,Integer> depthMap = new HashMap<Nominal,Integer>(); 
	
	// map from nominal to highest parent nominal in original expression, or null if a root
	private Map<Nominal,Nominal> parentMap = new HashMap<Nominal,Nominal>();
	
	// null nominal for use in dummy parents during flattening
	private static Nominal nullNom = new NominalAtom("null");

	// null prop for use in dummy parents during flattening
	private static Proposition nullProp = new Proposition("null");

	/**
	 * Returns a map from a nominal to its highest parent nominal in the original expression, 
	 * after flattening, or null if none.
	 */
	public Map<Nominal,Nominal> getHighestParentMap() {
		return parentMap;
	}
	
    /**
     * Recursively flattens the given LF and returns a list of elementary preds.
     * LF chunks are preserved on satops, as are alts (exclusive disjunctions) 
     * and opts (optional parts).  
     * Chunks, alts and opts are propagated through shared nominals.
     * A runtime exception is thrown if the LF cannot be flattened.
     */
    public List<SatOp> flatten(LF lf) {
        flatten(lf, null, null, 0, new Stack<Alt>(), new TIntArrayList());
        if (altCount > 0 || optCount > 0) propAltsOptsChunks();
        return preds;
    }

    // recursive flattening, with conversion of alts and opts
    private void flatten(
        LF lf, 
        Nominal currentNominal, SatOp parent, int depth, 
        Stack<Alt> alts, TIntArrayList opts
    ) {
        if (lf instanceof SatOp) {
            // flatten arg with new current nominal
            SatOp satOp = (SatOp) lf;
            currentNominal = satOp.getNominal();
            SatOp dummyParent = makeDummySatOp(currentNominal);
            addSatOp(dummyParent, parent, depth, alts, opts, lf);
            flatten(satOp.getArg(), currentNominal, dummyParent, depth, alts, opts);
        }
        else if (lf instanceof Op) {
            Op op = (Op) lf;
            SatOp dummyParent = makeDummySatOp(currentNominal);
            addSatOp(dummyParent, parent, depth, alts, opts, lf);
            if (op._name.equals(Op.XOR)) {
                // introduce new alt set; add alt for each item
                int altSet = altCount++;
                for (int i = 0; i < op._args.size(); i++) {
                    alts.push(new Alt(altSet, i));
                    LF arg = op._args.get(i);
                    flatten(arg, currentNominal, dummyParent, depth+1, alts, opts);
                    alts.pop();
                }
            }
            else if (op._name.equals(Op.OPT)) {
                // introduce new opt index for arg
                opts.add(optCount++);
                LF arg = op._args.get(0);
                flatten(arg, currentNominal, dummyParent, depth+1, alts, opts);
                opts.remove(opts.size()-1);
            }
            else {
                // otherwise just flatten each item
                for (Iterator<LF> it = op.getArguments().iterator(); it.hasNext(); ) {
                    flatten(it.next(), currentNominal, dummyParent, depth+1, alts, opts);
                }
            }
        }
        else if (lf instanceof Proposition) {
            // add SatOp for lf
            if (currentNominal == null) {
                throw new RuntimeException("No current nominal in trying to flatten " + lf);
            }
            SatOp satOp = new SatOp(currentNominal, lf);
            addSatOp(satOp, parent, depth, alts, opts, lf);
        }
        else if (lf instanceof HyloVar) {
            // just skip for now
        }
        else if (lf instanceof Diamond) {
            Diamond diamond = (Diamond) lf;
            LF arg = diamond.getArg();
            if (arg instanceof Proposition || arg instanceof Nominal || arg instanceof HyloVar) {
                // add SatOp for diamond
                SatOp satOp = new SatOp(currentNominal, lf);
                addSatOp(satOp, parent, depth, alts, opts, lf);
            }
            else if (arg instanceof Op && ((Op)arg)._name.equals(Op.CONJ)) {
                // add SatOp for diamond with first nominal arg, 
                // and flatten the rest of the args with the first nominal arg as the 
                // new current nominal
                Op argOp = (Op) arg;
                Iterator<LF> args = argOp._args.iterator();
                LF firstArg = args.next();
                if (!(firstArg instanceof Nominal)) {
                    throw new RuntimeException("First arg of diamond is not a nominal: " + firstArg);
                }
                Nominal firstNominalArg = (Nominal) firstArg;
                // add SatOp for diamond
                SatOp satOp = new SatOp(currentNominal, new Diamond(diamond.getMode(), firstNominalArg));
                addSatOp(satOp, parent, depth, alts, opts, lf);
                // flatten rest of list
                for (; args.hasNext(); ) {
                    flatten(args.next(), firstNominalArg, satOp, depth+1, alts, opts);
                }
            }
            else if (arg instanceof Op && ((Op)arg)._name.equals(Op.XOR)) {
                Op argOp = (Op) arg;
                SatOp dummyParent = makeDummySatOp(currentNominal);
                addSatOp(dummyParent, parent, depth, alts, opts, lf);
                // as before, process xor by introducing new alt set and adding alt for each disjunct; 
                // this time, also assume each disjunct is a conj op or nominal, and add a diamond satop 
                // to the disjunct nominal
                int altSet = altCount++;
                for (int i = 0; i < argOp._args.size(); i++) {
                    alts.push(new Alt(altSet, i));
                    LF disjunct = argOp._args.get(i);
                    if (!(disjunct instanceof Op && ((Op)disjunct)._name.equals(Op.CONJ)) && !(disjunct instanceof Nominal)) {
                        throw new RuntimeException("Disjunct of diamond is not a conj op or nominal: " + disjunct);
                    }
                    // conj op case
                    if (disjunct instanceof Op) {
                        Op disjunctOp = (Op) disjunct;
                        Iterator<LF> args = disjunctOp._args.iterator();
                        LF firstArg = args.next();
                        if (!(firstArg instanceof Nominal)) {
                            throw new RuntimeException("First arg of conj op under xor op is not a nominal: " + firstArg);
                        }
                        // add SatOp for diamond
                        Nominal disjunctNominal = (Nominal) firstArg;
                        SatOp satOp = new SatOp(currentNominal, new Diamond(diamond.getMode(), disjunctNominal));
                        addSatOp(satOp, dummyParent, depth+1, alts, opts, lf);
                        // flatten rest of list
                        for (; args.hasNext(); ) {
                            flatten(args.next(), disjunctNominal, satOp, depth+2, alts, opts);
                        }
                    }
                    // nominal case
                    else {
                        // just add SatOp for diamond
                        Nominal disjunctNominal = (Nominal) disjunct;
                        SatOp satOp = new SatOp(currentNominal, new Diamond(diamond.getMode(), disjunctNominal));
                        addSatOp(satOp, dummyParent, depth+1, alts, opts, lf);
                    }
                    alts.pop();
                }
            }
            else { 
                throw new RuntimeException("Arg of diamond is not a proposition, nominal or list: " + arg);
            }
        }
        else throw new RuntimeException("Unable to flatten " + lf);
    }

    // makes a dummy satop for the given nominal, if any; otherwise uses nullNom
    private static SatOp makeDummySatOp(Nominal nom) {
        return new SatOp((nom != null) ? nom : nullNom, nullProp);
    }

    // handles new preds
    private void addSatOp(SatOp satOp, SatOp parent, int depth, Stack<Alt> alts, TIntArrayList opts, LF lf) {
    	// add non-dummy satops to result
        if (satOp._arg != nullProp) preds.add(satOp);
        // update roots, maps
        if (parent == null) roots.add(satOp);
        else childMap.put(parent, satOp);
        Nominal nom = satOp._nominal;
        if (!nom.isShared()) {
	        if (!nomMap.containsKey(nom) || depth < depthMap.get(nom)) {
	    		nomMap.put(nom, satOp);
	    		depthMap.put(nom, depth);
	    		parentMap.put(nom, (parent != null && parent._nominal != nullNom) ? parent._nominal : null);
	        }
        }
        // set alts, opts, chunks
        if (!alts.empty()) satOp.alts = new ArrayList<Alt>(alts);
        if (opts.size() > 0) satOp.opts = new TIntArrayList(opts.toNativeArray());
        satOp.setChunks(lf.getChunks());
    }
    
    // propagates alts, opts and chunks down from roots
    private void propAltsOptsChunks() {
        // propagate for each root nom
        List<Alt> alts = Collections.emptyList(); 
        TIntArrayList opts = new TIntArrayList(0);
        TIntArrayList chunks = new TIntArrayList(0);
        for (SatOp root : roots) {
        	propAltsOptsChunks(root, alts, opts, chunks);
        }
    }
    
    // prop alts, opts & chunks, recursing through preds in child map and shared nom refs in nomMap
	private void propAltsOptsChunks(SatOp satOp, List<Alt> alts, TIntArrayList opts, TIntArrayList chunks) { 
        // prop alts and opts
        if (!alts.isEmpty()) {
            if (satOp.alts == null) satOp.alts = new ArrayList<Alt>(3);
            for (Alt alt : alts) {
                if (!satOp.alts.contains(alt)) satOp.alts.add(alt);
            }
            Collections.sort(satOp.alts); 
        }
        if (!opts.isEmpty()) {
            if (satOp.opts == null) satOp.opts = new TIntArrayList(3);
            for (int i=0; i < opts.size(); i++) {
                int opt = opts.get(i);
                if (!satOp.opts.contains(opt)) satOp.opts.add(opt);
            }
            satOp.opts.sort();
        }
        if (!chunks.isEmpty()) {
            if (satOp.chunks == null) satOp.chunks = new TIntArrayList(3);
            for (int i=0; i < chunks.size(); i++) {
                int chunk = chunks.get(i);
                if (!satOp.chunks.contains(chunk)) satOp.chunks.add(chunk);
            }
            satOp.chunks.sort();
        }
        // gather alts, opts & chunks for recursion
        List<Alt> alts2 = (satOp.alts != null) ? satOp.alts : alts;
        TIntArrayList opts2 = (satOp.opts != null) ? satOp.opts : opts;
        TIntArrayList chunks2 = (satOp.chunks != null) ? satOp.chunks : chunks;
        // recurse through children, if any
        List<SatOp> children = childMap.get(satOp);
        if (children != null) {
            for (SatOp child : children) 
            	propAltsOptsChunks(child, alts2, opts2, chunks2);
        }
        // recurse through shared nominals, if apropos
        Nominal nom = satOp._nominal;
        if (nom.isShared()) {
            SatOp nomPred = nomMap.get(nom); 
            if (nomPred != null) 
            	propAltsOptsChunks(nomPred, alts2, opts2, chunks2);
        }
        Nominal nom2 = HyloHelper.getSecondaryNominal(satOp);
        if (nom2 != null && nom2.isShared()) {
            SatOp nom2Pred = nomMap.get(nom2); 
            if (nom2Pred != null) 
            	propAltsOptsChunks(nom2Pred, alts2, opts2, chunks2);
        }
    }
}
