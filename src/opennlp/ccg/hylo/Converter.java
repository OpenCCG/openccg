///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-9 Michael White (University of Edinburgh, The Ohio State University)
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

import opennlp.ccg.TextCCG;
import opennlp.ccg.synsem.*;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * A class implementing conversion of nominal variables to nominal atoms.
 *
 * @author      Michael White
 * @version     $Revision: 1.7 $, $Date: 2010/08/31 16:20:43 $
 **/
public class Converter {
    
    /** Preference key for using word positions to name atoms. */
    public static final String USE_WORD_POSITIONS_FOR_ATOM_CONVERSION = "Use Word Positions To Convert Atoms";

	// map to already converted nominals 
    private Map<Nominal,Nominal> nominalMap = new HashMap<Nominal,Nominal>();
    
    // map to int for names
    private Map<String,Integer> nameMap = new HashMap<String,Integer>();
	
    // flag for whether to skip absent props
    private boolean skipAbsentProp = true;
       	
    /** Converts nominal vars to atoms, renaming them based on lexical propositions. */
	static void convertNominals(LF lf) {
		convertNominals(lf, null, null);
    }
	
	/**
	 * Converts nominal vars to atoms, renaming them based on word position, if 
	 * a root sign is given, otherwise using lexical propositions; 
	 * returns the converted nominal root. 
	 */
	static Nominal convertNominals(LF lf, Sign root, Nominal nominalRoot) {
		// check preference for naming with word positions; set root to null if false
        Preferences prefs = Preferences.userNodeForPackage(TextCCG.class);
        boolean useWordPositions = prefs.getBoolean(USE_WORD_POSITIONS_FOR_ATOM_CONVERSION, true);
		if (!useWordPositions) root = null;
        // traverse twice, skipping absent props the first time
    	Converter converter = new Converter();
    	converter.convertNoms(lf, root);
        converter.skipAbsentProp = false;
    	converter.convertNoms(lf, root);
    	// return converted nominal root, if any
    	Nominal retval = null;
    	if (nominalRoot != null) {
    		retval = converter.nominalMap.get(nominalRoot);
    	}
    	return retval;
    }

    // recurse through lf, converting nominals
    private void convertNoms(LF lf, Sign root) {
        if (lf instanceof SatOp) {
            SatOp satOp = (SatOp) lf;
            // try finding word index of lex origin in root sign
            int wordIndex = -1;
            if (root != null) {
	            LexSemOrigin origin = satOp.getOrigin();
	            if (origin instanceof Sign) {
	            	Sign lexSign = (Sign) origin;
	            	// make sure it's not dominated by another lex pred
	            	// nb: also need to check for special pred 'elem', which isn't 
	            	// dominated in sample flights grammar
	            	String lexPred = HyloHelper.getLexPred(satOp);
	            	if (lexPred != null && !lexPred.equals("elem")) {
	            		if (!lexDominated(lexPred, lexSign)) 
		            		wordIndex = root.wordIndex(lexSign);
	            	}
	            }
            }
            Nominal oldNom = satOp.getNominal();
            Proposition prop = null;
            LF arg = satOp.getArg();
            if (arg instanceof Proposition) { prop = (Proposition) arg; }
            else if (arg instanceof Op) {
                Op op = (Op) arg;
                LF first = (LF) op.getArguments().get(0);
                if (first instanceof Proposition) { prop = (Proposition) first; }
            }
            Nominal convertedNom = convertNominal(oldNom, prop, wordIndex);
            satOp.setNominal(convertedNom);
            convertNoms(arg, root);
        }
        else if (lf instanceof Diamond) {
            Diamond d = (Diamond) lf;
            LF arg = d.getArg();
            if (arg instanceof Nominal) {
                Nominal oldNom = (Nominal) arg;
                Nominal convertedNom = convertNominal(oldNom, null, -1);
                d.setArg(convertedNom);
            }
            else if (arg instanceof Op) {
                Op op = (Op) arg;
                List<LF> args = op.getArguments();
                LF first = args.get(0);
                if (first instanceof Nominal) {
                    Nominal oldNom = (Nominal) first;
                    LF second = args.get(1);
                    Proposition prop = null;
                    if (second instanceof Proposition) { prop = (Proposition) second; }
                    Nominal convertedNom = convertNominal(oldNom, prop, -1);
                    args.set(0, convertedNom);
                }
                convertNoms(arg, root);
            }
        }
        else if (lf instanceof Op) {
            List<LF> args = ((Op)lf).getArguments();
            for (int i = 0; i < args.size(); i++) {
            	convertNoms(args.get(i), root);
            }
        }
    }

    // returns a nominal atom based on the old nominal, prop and maps, 
    // which are updated accordingly; 
    // wordIndex is used instead if non-negative;
    // the skipAbsentProp flag controls whether to skip a null prop, 
    // so that a meaningful name might be created later
    private Nominal convertNominal(Nominal oldNom, Proposition prop, int wordIndex) {
        // check for an atom
        if (oldNom instanceof NominalAtom) return oldNom;
        // handle word index case
        if (wordIndex >= 0) return convertNominal(oldNom, "w" + wordIndex);
        // skip absent props according to flag
        if (prop == null && skipAbsentProp) return oldNom;
        // check if already converted, and return copy
        Nominal alreadyConvertedNom = nominalMap.get(oldNom);
        if (alreadyConvertedNom != null) {
            return (Nominal) alreadyConvertedNom.copy();
        }
        // otherwise create new atom, with name based on prop (if possible)
        String nameBase = "x";
        if (prop != null) { 
            nameBase = prop.toString().toLowerCase().substring(0,1); 
            // use "n" if not a letter
            if (!Character.isLetter(nameBase.charAt(0))) nameBase = "n";
        }
        int ext = 1;
        Integer baseCount = nameMap.get(nameBase);
        if (baseCount != null) { ext = baseCount.intValue() + 1; }
        nameMap.put(nameBase, new Integer(ext));
        String name = nameBase + ext;
        return convertNominal(oldNom, name);
    }

    // returns the converted nominal using the given name, updating the map
    private Nominal convertNominal(Nominal oldNom, String name) {
        Nominal retval = new NominalAtom(name, oldNom.getType());
        nominalMap.put(oldNom, retval);
        return retval;
    }
    
    
    //---------------------------------------------------------------------------
    // check for dominating lex pred
    //
    
    // returns true if the EP for the lexPred is dominated by another lex pred
    private static boolean lexDominated(String lexPred, Sign lexSign) {
    	Category cat = lexSign.getCategory();
    	LF lf = cat.getLF();
    	Nominal index = cat.getIndexNominal();
    	List<SatOp> preds = HyloHelper.getPreds(lf);
    	// find EP with lexPred, other lex preds
    	SatOp lexEP = null;
    	List<SatOp> otherLexPreds = new ArrayList<SatOp>();
    	for (SatOp pred : preds) {
    		if (HyloHelper.isLexPred(pred)) {
        		if (lexPred.equals(HyloHelper.getLexPred(pred))) 
        			lexEP = pred; 
        		else otherLexPreds.add(pred);
    		}
    	}
    	if (lexEP == null) { 
    		throw new RuntimeException("Couldn't find lexPred: " + lexPred); 
		}
    	// check domination
    	Nominal lexNom = HyloHelper.getPrincipalNominal(lexEP);
    	for (SatOp pred : otherLexPreds) {
    		Nominal otherNom = HyloHelper.getPrincipalNominal(pred);
    		Stack<Nominal> seen = new Stack<Nominal>();
    		seen.push(index); // don't recurse through index nominal
    		if (dominates(otherNom, lexNom, preds, seen)) return true; 
    	}
    	// otherwise false
    	return false;
    }
    
    // returns true if a dominates b in preds, using seen stack to avoid looping
    private static boolean dominates(Nominal a, Nominal b, List<SatOp> preds, Stack<Nominal> seen) {
    	// check for identity
    	if (a.equals(b)) return false;
    	// push a to seen noms
    	seen.push(a);
    	// check relations
    	for (SatOp pred : preds) {
    		if (a.equals(HyloHelper.getPrincipalNominal(pred))) {
    			Nominal c = HyloHelper.getSecondaryNominal(pred);
    			if (c == null) continue;
    			// check immed dominance
    			if (b.equals(c)) return true; // found dominance!
    			// check seen
    			if (seen.contains(c)) continue;
    			// recurse
    			if (dominates(c, b, preds, seen)) return true;
    		}
    	}
    	// otherwise not; pop a and return
    	seen.pop();
    	return false;
    }
    
    
    //---------------------------------------------------------------------------
    // convert nominal atoms back to vars
    //
    
    /** Converts nominal atoms back to vars. */
	static void convertNominalsToVars(List<SatOp> preds) {
		convertNominalsToVars(preds, null);
    }
	
	/**
	 * Converts nominal atoms back to vars, returning the converted nominal root. 
	 */
	static Nominal convertNominalsToVars(List<SatOp> preds, Nominal nominalRoot) {
		Nominal retval = null;
		for (SatOp pred : preds) {
			Nominal nom = pred._nominal;
			Nominal nv = convertNominalToVar(nom);
			if (nom.equals(nominalRoot)) retval = nv;
			pred.setNominal(nv);
			LF arg = pred.getArg();
			if (arg instanceof Diamond) {
				Diamond dArg = (Diamond) arg;
				LF arg2 = dArg.getArg();
				if (arg2 instanceof Nominal) {
					Nominal nv2 = convertNominalToVar((Nominal)arg2);
					dArg.setArg(nv2);
				}
			}
		}
		return retval;
	}
	
	// returns a nominal var with the same name as the given nominal
	static Nominal convertNominalToVar(Nominal nom) {
		return new NominalVar(nom.getName().toUpperCase(), nom.getType());
	}
}
