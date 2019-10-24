///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-5 Jason Baldridge and University of Edinburgh (Michael White)
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
import opennlp.ccg.unify.*;
import opennlp.ccg.grammar.*;
import opennlp.ccg.lexicon.Lexicon;
import org.jdom.*;

import java.util.*;

import gnu.trove.*;

/**
 * A utility class to help with certain global operations over hybrid logic
 * terms.
 *
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.43 $, $Date: 2011/08/26 05:18:39 $
 **/
public class HyloHelper {

    //-----------------------------------------------------------------
    // XML functions
    
    /** 
     * Builds a Hylo term from the given element.
     * An "lf" element may be used to wrap one or more (implicitly conj-ed) terms.
     */
    public static LF getLF(Element e) {
        LF retval = null;
        String type = e.getName();
        if (type.equals("op")) {
            retval = new Op(e);
        } else if (type.equals("var")) {
            String name = getName(e);
            retval = new HyloVar(prefix(name), type(name));
        } else if (type.equals("nomvar")) {
            String name = getName(e); 
            boolean shared = "true".equals(e.getAttributeValue("shared"));
            retval = new NominalVar(prefix(name), type(name), shared);
        } else if (type.equals("nom")) {
            String name = getName(e);
            boolean shared = "true".equals(e.getAttributeValue("shared"));
            retval = new NominalAtom(prefix(name), type(name), shared);
        } else if (type.equals("prop")) {
            String name = getName(e);
            retval = new Proposition(name, existingType(name));
        } else if (type.equals("satop")) {
            retval = new SatOp(e);
        } else if (type.equals("box") || type.equals("b")) {
            retval = new Box(e);
        } else if (type.equals("diamond") || type.equals("d")) {
            retval = new Diamond(e);
        } else if (type.equals("mode")) {
            String name = getName(e);
            retval = new ModeLabel(name);
        } else if (type.equals("modevar")) {
            String name = getName(e);
            retval = new ModeVar(name);
        } else if (type.equals("lf")) {
            retval = getLF_FromChildren(e);
        } else {
            System.out.println("Invalid hybrid logic LF type: " + type);
        }
        // assign chunks
        if (retval != null) {
            String chunks = e.getAttributeValue("chunks");
            if (chunks != null) {
                retval.setChunks(convertChunks(chunks));
            }
        }
        // done
        return retval;
    }

    // returns the value of the attribute 'name' or 'n'
    private static String getName(Element e) { 
        String name = e.getAttributeValue("name");
        if (name == null) name = e.getAttributeValue("n");
        return name;
    }
    
    // returns the simple type with the given name, if it exists, or null if not
    private static SimpleType existingType(String name) {
        Types types = Grammar.theGrammar.types;
        if (types.containsSimpleType(name)) return types.getSimpleType(name);
        else return null;
    }
    
    /** Returns the prefix of the name, up to an optional colon. */
    protected static String prefix(String name) {
        int index = name.indexOf(":");
        if (index >= 0) return name.substring(0, index);
        else return name;
    }

    /** Returns the simple type given by the suffix of the name after the colon, or null if none. */
    protected static SimpleType type(String name) {
        int index = name.indexOf(":");
        String suffix = (index >=0 && index+1 < name.length()) ? name.substring(index+1) : null;
        if (suffix != null) return Grammar.theGrammar.types.getSimpleType(suffix);
        else return null;
    }
    
        
    /**
     * Returns a Hylo term from the children of the given element, 
     * adding an implicit CONJ op if necessary.
     */
    @SuppressWarnings("unchecked")
	public static LF getLF_FromChildren(Element e) {
        List<Element> children = e.getChildren();
        if (children.size() > 1) {
            List<LF> preds = new ArrayList<LF>(children.size());
            for (int i=0; i < children.size(); i++) {
                preds.add(getLF(children.get(i)));
            }
            Op conj = new Op(Op.CONJ, preds);
            return conj;
        }
        else return getLF(children.get(0));
    }

    /**
     * Returns an XML representation of the given LF, 
     * wrapped with an 'lf' element, 
     * removing CONJ ops that may be left implicit.
     */
    public static Element toXml(LF lf) {
        Element retval = new Element("lf");
        retval.addContent(lf.toXml());
        removeConjOps(retval);
        return retval;
    }

    
    //-----------------------------------------------------------------
    // process chunks
    
    /** 
     * Processes and removes any chunk elements.
     * Each chunk element is numbered, and all contained elements are marked 
     * as being contained by this chunk, via a "chunks" attribute.
     */
    public static void processChunks(Element e) {
        processChunks(e, null, 0);
        removeChunkElts(e);
    }
    
    // recursively processes chunks, threading count through calls
    @SuppressWarnings("unchecked")
	private static int processChunks(Element e, String chunks, int count) {
        // check for chunk
        if (e.getName().equals("chunk")) {
            // update chunks string and counter
            if (chunks == null) { chunks = "" + count; }
            else { chunks += " " + count; }
            count++;
        }
        // otherwise add chunks attr, if val non-null
        else if (chunks != null) {
            e.setAttribute("chunks", chunks);
        }
        // do children
        List<Element> children = e.getChildren();
        for (int i=0; i < children.size(); i++) {
            count = processChunks(children.get(i), chunks, count);
        }
        // return current count
        return count;
    }
    
    // converts chunk strings
    private static TIntArrayList convertChunks(String chunks) {
        String[] tokens = chunks.split("\\s+");
        TIntArrayList retval = new TIntArrayList(tokens.length);
        for (int i = 0; i < tokens.length; i++) {
            retval.add(Integer.parseInt(tokens[i]));
        }
        return retval;
    }
    
    //-----------------------------------------------------------------
    // recursively remove certain elements
    
    private static abstract class ElementTest {
        abstract boolean test(Element elt);
    }

    // recursively removes elements meeting given test
    @SuppressWarnings("unchecked")
	private static void removeElts(Element elt, ElementTest eltTest) {
        // nb: need to dump children into a new list, in order to get a list iterator 
        //     that will allow multiple adds
        List<Element> children = elt.getChildren();
        List<Element> newChildren = new ArrayList<Element>(children.size());
        newChildren.addAll(children);
        for (ListIterator<Element> li = newChildren.listIterator(); li.hasNext(); ) {
            Element nextElt = li.next();
            removeElts(nextElt, eltTest);
            if (eltTest.test(nextElt)) {
                li.remove();
                for (Iterator<Element> it = nextElt.getChildren().iterator(); it.hasNext(); ) {
                    Element childElt = it.next();
                    it.remove(); // removes childElt from nextElt's children, so it can become a child of elt
                    li.add(childElt);
                }
            }
        }
        elt.removeContent(); 
        elt.setContent(newChildren); 
    }
    
    // recursively removes conj ops
    private static void removeConjOps(Element lfElt) {
        removeElts(
            lfElt, 
            new ElementTest() {
                boolean test(Element elt) {
                    return elt.getName().equals("op") && 
                           elt.getAttributeValue("name").equals(Op.CONJ);
                }
            }
        );
    }
    
    // recursively removes chunk elements
    private static void removeChunkElts(Element lfElt) {
        removeElts(
            lfElt, 
            new ElementTest() {
                boolean test(Element elt) {
                    return elt.getName().equals("chunk");
                }
            }
        );
    }
    

    //-----------------------------------------------------------------
    // functions for elementary predications

    /**
     * Returns whether the given LF is an elementary predication, 
     * ie a lexical predication, relation predication or attribute-value predication.
     */
    public static boolean isElementaryPredication(LF lf) {
        return isLexPred(lf) || isRelPred(lf) || isAttrPred(lf);
    }
    
    /**
     * Returns whether the given elementary predication is a lexical predication, 
     * ie one of the form @x(prop).
     */
    public static boolean isLexPred(LF pred) {
        if (!(pred instanceof SatOp)) return false;
        SatOp satOp = (SatOp) pred;
        LF arg = satOp.getArg();
        return (arg instanceof Proposition);
    }

    /**
     * Returns whether the given elementary predication is a relation predication, 
     * ie one of the form @x(&lt;Rel&gt;y).
     */
    public static boolean isRelPred(LF pred) {
        if (!(pred instanceof SatOp)) return false;
        SatOp satOp = (SatOp) pred;
        LF arg = satOp.getArg();
        if (!(arg instanceof Diamond)) return false;
        Diamond d = (Diamond) arg;
        return (d.getArg() instanceof Nominal);
    }

    /**
     * Returns whether the given elementary predication is an attribute-value predication, 
     * ie one of the form @x(&lt;Rel&gt;prop).  Note that the prop is also allowed to be 
     * a HyloVar.
     */
    public static boolean isAttrPred(LF pred) {
        if (!(pred instanceof SatOp)) return false;
        SatOp satOp = (SatOp) pred;
        LF arg = satOp.getArg();
        return isAttr(arg);
    }
    
    /**
     * Returns whether the given arg is an attribute-value pair, 
     * ie one of the form &lt;Rel&gt;prop.  Note that the prop is also allowed to be 
     * a HyloVar.
     */
    public static boolean isAttr(LF arg) {
        if (!(arg instanceof Diamond)) return false;
        Diamond d = (Diamond) arg;
        LF dArg = d.getArg();
        return ( dArg instanceof Proposition || 
                 (dArg instanceof HyloVar && !(dArg instanceof NominalVar)) );
    }
    
    /**
     * Returns the name of the lexical predicate of the given elementary predication, 
     * or null, if the given LF is not a lexical predicate.
     */
    public static String getLexPred(LF lf) {
        if (!isLexPred(lf)) return null;
        LF arg = ((SatOp)lf).getArg();
        return ((Proposition)arg).toString();
    }
    
    /**
     * Returns the name of the relation of the given elementary predication, 
     * or null, if the given LF is not a relation or attribute-value predicate.
     */
    public static String getRel(LF lf) {
        if (!isRelPred(lf) && !isAttrPred(lf)) return null;
        LF arg = ((SatOp)lf).getArg();
        return ((Diamond)arg).getMode().toString();
    }
    
    /**
     * Returns the string value of the attribute-value predicate, or 
     * null if the given LF is not an attribute-value predicate or has no value. 
     */
    public static String getVal(LF lf) {
    	if (!isAttrPred(lf)) return null;
        LF arg = ((SatOp)lf).getArg();
        LF dArg = ((Diamond)arg).getArg();
        if (dArg instanceof Proposition) return ((Proposition)dArg).getName();
        return null;
    }
    
    /**
     * Returns the principal nominal the given elementary predication, 
     * or null, if the given LF is not an elementary predication.
     */
    public static Nominal getPrincipalNominal(LF lf) {
        if (!isElementaryPredication(lf)) return null;
        return ((SatOp)lf).getNominal();
    }

    /**
     * Returns the secondary nominal of the given elementary predication, 
     * or null, if the given LF is not a relation predication.
     */
    public static Nominal getSecondaryNominal(LF lf) {
        if (!isRelPred(lf)) return null;
        LF arg = ((SatOp)lf).getArg();
        return (Nominal) ((Diamond)arg).getArg(); 
    }

    
    //-----------------------------------------------------------------
    // flattening 

    /**
     * Returns a flattened, sorted list of elementary preds from the given LF 
     * as a conjunction op, or as a single LF, if there is only one. 
     * LF chunks are preserved on satops, as are alts (exclusive disjunctions) 
     * and opts (optional parts).
     * A runtime exception is thrown if the LF cannot be flattened.
     */
    @SuppressWarnings("unchecked")
	public static LF flattenLF(LF lf) {
        List<?> preds = flatten(lf);
        if (preds.size() == 1) {
            return (LF) preds.get(0);
        }
        else {
        	return new Op(Op.CONJ, (List<LF>)preds);
        }
    }
    
    /**
     * Returns a list of predications from the given LF, which is assumed to be either 
     * a conjunction of elementary predications or a single elementary predication.
     */
    public static List<SatOp> getPreds(LF lf) {
        if (lf instanceof Op && ((Op)lf).getName().equals(Op.CONJ)) {
            List<LF> args = ((Op)lf).getArguments();
            List<SatOp> retval = new ArrayList<SatOp>(args.size());
            for (LF arg : args) retval.add((SatOp)arg);
            return retval;
        }
        else { 
            List<SatOp> retval = new ArrayList<SatOp>(1);
            retval.add((SatOp)lf);
            return retval;
        }
    }
    
    /**
     * Returns the first elementary predication from the given LF, which is assumed to be either 
     * a conjunction of elementary predications or a single elementary predication; 
     * otherwise returns null.
     */
    public static SatOp getFirstPred(LF lf) {
    	if (lf instanceof SatOp) return (SatOp) lf;
        if (lf instanceof Op && ((Op)lf).getName().equals(Op.CONJ)) {
            List<LF> args = ((Op)lf).getArguments();
            return (SatOp) args.get(0);
        }
        return null;
    }
    
    /**
     * Returns a flattened, sorted list of elementary preds from the given LF 
     * as a list.
     * LF chunks are preserved on satops, as are alts (exclusive disjunctions) 
     * and opts (optional parts).
     * Chunks, alts and opts are propagated through shared nominals.
     * A runtime exception is thrown if the LF cannot be flattened.
     */
    public static List<SatOp> flatten(LF lf) { 
        List<SatOp> retval = new Flattener().flatten(lf);
        sort(retval);
        return retval;
    }
    
    /**
     * Returns the first elementary predication in the flattened LF.
     * A runtime exception is thrown if the LF cannot be flattened.
     */
    public static LF firstEP(LF lf) { 
        List<SatOp> preds = new Flattener().flatten(lf);
        return preds.get(0);
    }
    
    /**
     * Sets the origin of the elementary preds in the given LF (if any).
     */
    public static void setOrigin(LF lf, LexSemOrigin origin) {
    	if (lf == null) return;
    	if (lf instanceof SatOp) ((SatOp)lf).setOrigin(origin);
    	else if (lf instanceof Op && ((Op)lf).getName().equals(Op.CONJ)) {
            List<LF> args = ((Op)lf).getArguments();
            for (LF arg : args) {
            	if (arg instanceof SatOp) ((SatOp)arg).setOrigin(origin);
            }
        }

    }
    
    /** 
     * Returns a map from nominals to index positions for the first EP for
     * that nominal in a sorted list of elementary predications. 
     */
    public static Map<Nominal,Integer> nomIndex(List<SatOp> preds) {
    	HashMap<Nominal,Integer> retval = new HashMap<Nominal,Integer>(preds.size()/2);
    	for (int i=0; i < preds.size(); i++) {
    		SatOp pred = preds.get(i);
    		Nominal nom = pred._nominal;
    		if (!retval.containsKey(nom)) retval.put(nom, i);
    	}
    	return retval;
    }
    
    /**
     * Returns whether a nominal is a root in the list of EPs using a linear search.
     */
    public static boolean isRoot(Nominal nom, List<SatOp> preds) {
    	for (SatOp pred : preds) {
    		Nominal child = getSecondaryNominal(pred);
    		if (child != null && child.equals(nom)) return false;
    	}
    	return true;
    }
    
    
    //-----------------------------------------------------------------
    // lexical dependencies 

    /** Returns the unfilled lexical dependencies for a lexical item's LF. */
    public static List<LexDependency> getUnfilledLexDeps(LF lf) {
    	if (lf == null) return Collections.emptyList();
    	return LexDependency.unfilledLexDeps(getPreds(lf));
    }
    
	/**
	 * Returns the filled lexical dependencies from those in the unfilled list 
	 * by checking the sign's LF for ones that have become filled, removing the 
	 * corresponding no longer unfilled deps.  
	 */
	public static List<LexDependency> getFilledLexDeps(List<LexDependency> unfilled, LF lf) {
    	if (lf == null) return Collections.emptyList();
    	return LexDependency.filledLexDeps(unfilled, getPreds(lf));
    }

	/**
	 * Returns the semantic features (attribute-value preds) for the given nominal  
	 * in the given LF. 
	 */
	public static List<SatOp> getSemFeatsForHead(Nominal nominal, LF lf) {
		if (nominal == null || lf == null) return Collections.emptyList();
		List<SatOp> retval = new ArrayList<SatOp>(3);
		for (SatOp pred : getPreds(lf)) {
			if (nominal.equals(pred._nominal) && isAttrPred(pred))
				retval.add(pred);
		}
		return retval;
	}
	
	
    //-----------------------------------------------------------------
    // compacting 
    
    /** Composes compact and convertNominals. */
    public static LF compactAndConvertNominals(LF lf, Nominal root) {
        LF retval = compact(lf, root);
        convertNominals(retval);
        return retval;
    }
    
    /** Composes compact and convertNominals with a root sign, for conversion using word positions. */
    public static LF compactAndConvertNominals(LF lf, Nominal root, Sign rootSign) {
        root = convertNominals(lf, rootSign, root);
        LF retval = compact(lf, root);
        return retval;
    }
    
    /**
     * Returns a compacted LF from the given flattened one. 
     * A root nominal may also be given (otherwise null). 
     * Nominals with multiple parents are kept separate.
     * If there are any duplicate predications, an attempt 
     * is made to attach them in different locations.
     */
    public static LF compact(LF lf, Nominal root) {
    	return Compacter.compact(lf, root);
    }
    
    
    //-----------------------------------------------------------------
    // convert nominals
    
    /** Converts nominal vars to atoms, renaming them based on lexical propositions. */
    public static void convertNominals(LF lf) {
    	Converter.convertNominals(lf);
    }

	/**
	 * Converts nominal vars to atoms, renaming them based on word position, if 
	 * a root sign is given, otherwise using lexical propositions; 
	 * returns the converted nominal root. 
	 */
	public static Nominal convertNominals(LF lf, Sign root, Nominal nominalRoot) {
		return Converter.convertNominals(lf, root, nominalRoot);
	}
	
	/**
	 * Converts nominal atoms back to vars, returning the converted nominal root. 
	 * The LF is assumed to be flattened to elementary predications.
	 */
	public static Nominal convertNominalsToVars(LF lf, Nominal nominalRoot) {
		return Converter.convertNominalsToVars(getPreds(lf), nominalRoot);
	}
	

    //-----------------------------------------------------------------
    // append 

    /**
     * Returns a the conjunction of the two LFs, either 
     * as a conjunction op, or as a single LF, if one is null.
     * If either LF is itself a conj op, its elements are appended  
     * instead of the conj op itself.
     * If both LFs are null, null is returned.
     */
    public static LF append(LF lf1, LF lf2) {
        
        // set up new list
        int size = 0;
        List<LF> args1 = null;
        if (lf1 instanceof Op && ((Op)lf1).getName().equals(Op.CONJ)) {
            args1 = ((Op)lf1).getArguments();
            size += args1.size();
        } else if (lf1 != null) {
            size++;
        } 
        List<LF> args2 = null;
        if (lf2 instanceof Op && ((Op)lf2).getName().equals(Op.CONJ)) {
            args2 = ((Op)lf2).getArguments();
            size += args2.size();
        } else if (lf2 != null) {
            size++;
        }
        List<LF> combined = new ArrayList<LF>(size);
        
        // add to new list
        if (args1 != null) { 
            combined.addAll(args1);
        } else if (lf1 != null) {
            combined.add(lf1);
        }
        if (args2 != null) { 
            combined.addAll(args2);
        } else if (lf2 != null) {
            combined.add(lf2);
        }
        
        // return
        if (combined.isEmpty()) { return null; }
        else if (combined.size() == 1) { return combined.get(0); }
        else { return new Op(Op.CONJ, combined); }
    }

    
    //-----------------------------------------------------------------
    // sort 

    /**
     * Sorts the list of elementary predications in a conj op, 
     * or does nothing if the LF is not a conj op.
     */
    public static void sort(LF lf) {
        if (lf instanceof Op && ((Op)lf).getName().equals(Op.CONJ)) {
            sort(((Op)lf).getArguments());
        }
    }
    
    /**
     * Sorts a list of elementary predications.
     */
    public static void sort(List<? extends LF> preds) {
        Collections.sort(preds, predComparator);
    }

    // compares elementary predications
    private static final Comparator<LF> predComparator = new Comparator<LF>() {
        public int compare(LF lf1, LF lf2){
            // sort first on principal nominal
            int nomCompare = getPrincipalNominal(lf1).compareTo(getPrincipalNominal(lf2));
            if (nomCompare != 0) return nomCompare;
            // sort next on type of elementary predication
            int typeCompare = epType(lf1).compareTo(epType(lf2));
            if (typeCompare != 0) return typeCompare;
            // then on lex pred
            if (isLexPred(lf1)) {
                return getLexPred(lf1).compareToIgnoreCase(getLexPred(lf2));
            }
            // then rels
            String rel1 = getRel(lf1);
            String rel2 = getRel(lf2);
            Lexicon theLexicon = Grammar.theGrammar.lexicon;
            assert theLexicon != null;
            Integer rel1Index = theLexicon.getRelationSortIndex(rel1);
            Integer rel2Index = theLexicon.getRelationSortIndex(rel2);
            int relIndexCompare = rel1Index.compareTo(rel2Index);
            if (relIndexCompare != 0) return relIndexCompare;
            int relCompare = rel1.compareToIgnoreCase(rel2);
            if (relCompare != 0) return relCompare;
            // then secondary nominal
            if (isRelPred(lf1)) {
                return getSecondaryNominal(lf1).compareTo(getSecondaryNominal(lf2));
            }
            // otherwise 0
            return 0;
        }
    };
    
    // order of elementary predication type
    private static Integer epType(LF lf) {
        if (isLexPred(lf)) return LEX_PRED;
        else if (isAttrPred(lf)) return ATTR_PRED;
        else if (isRelPred(lf)) return REL_PRED;
        // shouldn't happen
        else return null;
    }
    
    private static Integer LEX_PRED = new Integer(1);
    private static Integer ATTR_PRED = new Integer(2);
    private static Integer REL_PRED = new Integer(3);

    
    //-----------------------------------------------------------------
    // check

    /**
     * Checks the list of elementary predications in a conj op 
     * for well-formedness, or does nothing if the LF is not a conj op.
     * A UnifyFailure exception is thrown if the check fails.
     * The only current check is that there is no more than one lexical 
     * predication per nominal.  
     * The list of predications is assumed to be already sorted.
     */
    public static void check(LF lf) throws UnifyFailure {
        if (lf instanceof Op && ((Op)lf).getName().equals(Op.CONJ)) {
            check(((Op)lf).getArguments());
        }
    }
    
    private static void check(List<LF> preds) throws UnifyFailure {
        for (int i = 0; i < preds.size()-1; i++) {
            LF lf1 = preds.get(i);
            LF lf2 = preds.get(i+1);
            if (isLexPred(lf1) && isLexPred(lf2) &&
                getPrincipalNominal(lf1).equals(getPrincipalNominal(lf2))) 
            {
                throw new UnifyFailure();
            }
        }
    }
}
