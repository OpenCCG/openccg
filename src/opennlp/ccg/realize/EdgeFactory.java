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

import opennlp.ccg.*;
import opennlp.ccg.lexicon.*;
import opennlp.ccg.grammar.*;
import opennlp.ccg.unify.*;
import opennlp.ccg.synsem.*;
import opennlp.ccg.hylo.*;
import opennlp.ccg.util.*;

import gnu.trove.*;

import java.util.*;
import java.util.prefs.*;

/**
 * The EdgeFactory is responsible for creating edges. 
 * A single edge factory instance per realization request is assumed.
 *
 * @author      Michael White
 * @version     $Revision: 1.87 $, $Date: 2011/10/30 21:06:47 $
 */
public class EdgeFactory
{

    /** Preference key for whether to use indexing to filter edges to combine. */
    public static final String USE_INDEXING = "Use Indexing";
    
    /** Preference key for whether to (exceptionally) allow categories with no target cat index nominal to combine. */
    public static final String ALLOW_MISSING_INDEX_COMBOS = "Allow Missing Index Combos";
    
    /** Preference key for whether to use LF chunks to filter edges to combine. */
    public static final String USE_CHUNKS = "Use Chunks";
    
    /** Preference key for whether to use feature licensing; if false, 
        the simple lex feature is used for comparison purposes. */
    public static final String USE_FEATURE_LICENSING = "Use Feature Licensing";
    

    /** The grammar used to create edges. */    
    public final Grammar grammar;
    
    /** The elementary predications to be covered. */
    public final List<SatOp> preds;
    
    /** The sign scorer. */
    public final SignScorer signScorer;
    
    /** The hypertagger. */
    public final Hypertagger hypertagger;
    
    /** The initial, unmarked edges instantiated after lexical lookup. */
    public final List<Edge> initialEdges = new ArrayList<Edge>();

    /** The marked initial edges licensed by features in the other initial edges. */
    public final List<Edge> markedEdges = new ArrayList<Edge>();

    /** The licensed and instantiated purely syntactic (semantically null) edges. */
    public final List<Edge> instantiatedNoSemEdges = new ArrayList<Edge>();
    
    /** The licensed, uninstantiated purely syntactic edges. */
    public final List<Edge> noSemEdges = new ArrayList<Edge>();

    /** The rule instances, ie the type changing rules with instantiated semantics. */
    public final List<RuleInstance> ruleInstances = new ArrayList<RuleInstance>();
    
    /** The LF chunks, represented as bitsets. */
    public final List<BitSet> lfChunks = new ArrayList<BitSet>();
    
    /** The LF alts, represented as a list of lists of alts 
        (where each list of alts forms an exclusive disjunction). */
    public final List<List<Alt>> lfAlts = new ArrayList<List<Alt>>();
    
    /** The LF optional parts, represented as bitsets. */
    public final List<BitSet> lfOpts = new ArrayList<BitSet>();
    
    /** Flag indicating whether there are any LF alts or optional parts. */
    public boolean hasLfAltsOrOpts = false;
    
    // a bitset for all preds
    private final BitSet allPreds;
    
    
    // the lexicon used to create edges    
    private final Lexicon lexicon;
    
    // general rules, ie the ones with no associated semantics
    private final RuleGroup generalRules;
    
    // rule group for rules wrapped by rule instances
    private final RuleGroup ruleInstancesGroup;
    
    // rule for joining fragments
    private final FragmentJoining fragmentRule = new FragmentJoining();
    
    // helper class for licensing features
    private final FeatureLicenser featureLicenser;
    
    
    /** Set of nominals whose phrases are marked for labeling in the output (with mark=+). */
    public final Set<Nominal> labeledNominals = new HashSet<Nominal>();
    
    /** Map from nominals to ints, for indexing edges. */
    final TObjectIntHashMap nominals = new TObjectIntHashMap();
    
    // indexes the preds by their position, 
    // by mapping pred keys to a list of pred indices for that key
    private final Map<String,List<Integer>> predMap = new HashMap<String,List<Integer>>();
    
    // list of paired nominals in the input LF
    private final List<BitSet[]> pairedNominals = new ArrayList<BitSet[]>();
    
    // flag for whether there are any paired nominals
    private boolean anyPairedNominals = false;
    
    /** Set of nominals under a BoundVar relation. */
    final Set<Nominal> boundVarNominals = new HashSet<Nominal>();
    
    // list of nominals for a particular cat or pair of cats
    private final List<Object> catNominals = new ArrayList<Object>();
    
    
    /** 
     * Flag for whether to use indexing. 
     * Setting retrieved from preferences; turned off when gluing fragments. 
     */
    public boolean useIndexing = true;

    // flag for whether to (exceptionally) allow categories with no target cat index nominal to combine
    private boolean allowMissingIndexCombos = false;
    
    // flag for whether to use chunks
    private boolean useChunks = true;
    
    // flag for whether to use feature licensing
    private boolean useFeatureLicensing = true;

    /** 
     * Flag for whether to debug category instantiation (defaults to false). 
     * If true, cases of complex categories whose outermost category 
     * is not instantiated with index nominals are reported to 
     * System.err. Note that realization is more efficient if such 
     * categories can be avoided in the grammar.
     * Uncovered EPs after lex lookup are also reported to System.err.
     */ 
    public boolean debugInstantiation = false;
    
    
    /* The number of unary rule applications executed. */
    private int unaryRuleApps = 0;

    /* The number of unary rule instance applications executed. */
    private int unaryRuleInstApps = 0;

    /* The number of binary rule applications executed. */
    private int binaryRuleApps = 0;
    

    /** Flag for whether to glue fragments currently. Defaults to false. */
    public boolean gluingFragments = false;

    /** Bit vector for EPs not covered by a lexical edge or rule instance; null if none. */
    protected BitSet uncoveredEPs = null;
    
    /** Flag indicating whether any lexical or featural EPs are uncovered. */
    public boolean hasUncoveredPreds = false;
    
    /** Flag for whether to use relaxed relation matching. */  // XXX tmp switch
    protected boolean useRelaxedRelationMatching = Boolean.getBoolean("useRelaxedRelationMatching");
    
    
    /** Constructor. */
    public EdgeFactory(Grammar grammar, List<SatOp> preds, SignScorer signScorer) {
    	this(grammar, preds, signScorer, null);
    }
    
    /** Constructor with hypertagger. */
    public EdgeFactory(Grammar grammar, List<SatOp> preds, SignScorer signScorer, Hypertagger hypertagger) {
        this.grammar = grammar;
        this.preds = preds;
        this.signScorer = signScorer;
        this.hypertagger = hypertagger;
        
        lexicon = grammar.lexicon;
        generalRules = new RuleGroup(grammar);
        generalRules.borrowSupercatRuleCombos(grammar.rules);
        ruleInstancesGroup = new RuleGroup(grammar);
        ruleInstancesGroup.borrowSupercatRuleCombos(grammar.rules);
        
        allPreds = new BitSet(preds.size());
        allPreds.set(0, preds.size());
        
        Preferences prefs = Preferences.userNodeForPackage(TextCCG.class);
        useIndexing = prefs.getBoolean(USE_INDEXING, true);
	allowMissingIndexCombos = prefs.getBoolean(ALLOW_MISSING_INDEX_COMBOS,false);
        useChunks = prefs.getBoolean(USE_CHUNKS, true);
        useFeatureLicensing = prefs.getBoolean(USE_FEATURE_LICENSING, true);

        if (useFeatureLicensing) {
            featureLicenser = new FeatureLicenser(this);
        } else {
            // if feature licensing off, use simple lex feature for comparison purposes
            featureLicenser = new FeatureLicenser(
                this, 
                new LicensingFeature[] { LicensingFeature.simpleLexFeature }
            );
        }
            
        UnifyControl.startUnifySequence();
        extractLabeledNominals();
        indexPreds();
        listNominals();
        listPairedNominals();
        addBoundVarNominals();
        fillLfChunks(); 
        fillLfAlts(); fillLfOpts();
        hasLfAltsOrOpts = lfAlts.size() > 0 || lfOpts.size() > 0;
        
        if (hypertagger != null) hypertagger.mapPreds(preds);
    }

    /**
     * Adds LF optional parts for any preds not covered by a lex item or rule instance,
     * divided up by chunks, for use with fragment gluing or relaxed relation matching.
     */
    public void addLFOptsForUncoveredPreds() {
    	// see if all preds covered
    	if (uncoveredEPs == null) return;
    	// clone what's not covered by lex items and rule instances
    	BitSet opt = (BitSet) uncoveredEPs.clone();
		// otherwise add opts for what's missing
		// nb: need to split up by chunks
		for (BitSet chunk : lfChunks) {
			if (opt.intersects(chunk)) {
				BitSet optChunk = (BitSet) opt.clone();
				optChunk.and(chunk);
				if (!lfOpts.contains(optChunk)) lfOpts.add(optChunk);
				opt.andNot(optChunk);
			}
		}
		// add anything remaining
		if (!opt.isEmpty() && !lfOpts.contains(opt)) lfOpts.add(opt);
    	// ensure hasLfAltsOrOpts set
    	hasLfAltsOrOpts = true;
		// TODO deal with lf alts too (may require sorting chunks and alts by size)
    }
    
    /**
     * Adds an LF optional part for each instantiated rule instance, 
     * for use with fragment gluing.
     */
    public void addLFOptsForRuleInstances() {
    	// do each rule instance
    	for (RuleInstance ruleInstance : ruleInstances) {
    		BitSet opt = (BitSet) ruleInstance.bitset.clone();
    		lfOpts.add(opt);
    	}
    	// ensure hasLfAltsOrOpts set
    	if (lfOpts.size() > 0) hasLfAltsOrOpts = true;
    }
    
    // returns the uncovered preds, or null if none
    private BitSet uncoveredPreds() {
    	// determine what's covered by lex items and rule instances
    	BitSet retval = new BitSet(preds.size());
    	for (Edge edge : initialEdges) retval.or(edge.bitset);
    	for (Edge edge : markedEdges) retval.or(edge.bitset);
    	for (RuleInstance ruleInstance : ruleInstances) retval.or(ruleInstance.bitset);
    	// see if all preds covered
    	if (retval.equals(allPreds)) return null;
		// otherwise xor what's missing
		retval.xor(allPreds);
		// set missing lex preds flag
		for (int i = retval.nextSetBit(0); i >= 0; i = retval.nextSetBit(i+1)) {
			SatOp pred = preds.get(i);
			if (HyloHelper.isLexPred(pred) || HyloHelper.isAttrPred(pred)) {
				hasUncoveredPreds =  true; break;
			}
		}
		// return
    	return retval;
    }
    
    
    //-----------------------------------------------------------------
    // edge construction
    //

    /** Makes an edge, computing the completeness percentage, sign score, 
        and indices, and setting the most specific incomplete LF chunk (if any). */
    protected Edge makeEdge(Sign sign, BitSet bitset, List<List<Alt>> activeLfAlts) {
        BitSet indices = getIndices(sign.getCategory(), null);
        float completeness = bitset.cardinality() / (float) preds.size();
        boolean complete = (completeness == 1.0);
        double score = signScorer.score(sign, complete);
        BitSet incompleteLfChunk = getIncompleteLfChunk(bitset, activeLfAlts);
        return new Edge(sign, bitset, indices, completeness, score, activeLfAlts, incompleteLfChunk);
    }

    /** Makes an edge for the given alt sign from the given edge, after computing the sign's score. */
    protected Edge makeAltEdge(Sign altSign, Edge edge) {
        double score = signScorer.score(altSign, edge.complete());
        return new Edge(
        		altSign, edge.bitset, edge.indices, 
        		edge.completeness, score, 
        		edge.activeLfAlts, edge.incompleteLfChunk
        );
    }
    
    /** Makes an edge consisting of two joined fragments. */
    public Edge makeJoinedEdge(Edge edge1, Edge edge2) {
    	Sign sign = fragmentRule.applyRule(edge1.sign, edge2.sign);
    	BitSet bitset = (BitSet) edge1.bitset.clone();
    	bitset.or(edge2.bitset);
        float completeness = bitset.cardinality() / (float) preds.size();
        boolean complete = (completeness == 1.0);
        double score = signScorer.score(sign, complete);
        return new Edge(
        		sign, bitset, edge1.indices, 
        		completeness, score, 
        		edge1.activeLfAlts, edge1.incompleteLfChunk
        );
    }
    
    
    //-----------------------------------------------------------------
    // active alts
    //

    /** From the given LF alts, returns the active ones for the given bitset, 
        updating the bitset for any completely covered alts. 
        NB: If the given LF alts list is not the entire list, each alt is assumed to intersect. */
    private List<List<Alt>> getActiveLfAlts(List<List<Alt>> fromLfAlts, BitSet bitset) {
        if (fromLfAlts.isEmpty()) return fromLfAlts;
        boolean checkingAllAlts = (fromLfAlts == lfAlts);
        BitSet tmpBitSet = new BitSet(bitset.size());
        List<List<Alt>> retval = new ArrayList<List<Alt>>(fromLfAlts.size());
        // check each 'from' alt 
        for (List<Alt> altSet : fromLfAlts) {
            List<Alt> activeAltSet = null; // for collecting active alts
            boolean foundCoveredAlt = false;
            for (Alt alt : altSet) { 
                // if checking all alts, check intersection with alt
                if (!checkingAllAlts || alt.bitset.intersects(bitset)) {
                    // check whether alt completely covered
                    tmpBitSet.clear(); tmpBitSet.or(bitset); tmpBitSet.and(alt.bitset);
                    if (tmpBitSet.equals(alt.bitset)) {
                        foundCoveredAlt = true; break;
                    }
                    else {
                        // otherwise add to active alts
                        if (activeAltSet == null) activeAltSet = new ArrayList<Alt>(3);
                        activeAltSet.add(alt);
                    }
                }
            }
            if (foundCoveredAlt) {
                // update coverage bitset to include all alts in this set
                List<Alt> fullAltSet = lfAlts.get(altSet.get(0).altSet);
                for (Alt alt : fullAltSet) bitset.or(alt.bitset); 
            }
            else {
                // otherwise update active alts, if any
                if (activeAltSet != null) retval.add(activeAltSet);
            }
        }
        return retval;
    }
    
    /** Returns the active LF alts that result from combining the given ones, 
        or null if these are incompatible.
        For alt sets in common, the combined alts consist of the intersection 
        of these alt sets, or null if this intersection is empty.
        For alts sets not in common, the active alts are carried through 
        unchanged. */
    private List<List<Alt>> getCombinedLfAlts(List<List<Alt>> activeLfAlts1, List<List<Alt>> activeLfAlts2) {
        if (activeLfAlts1.isEmpty()) return activeLfAlts2;
        if (activeLfAlts2.isEmpty()) return activeLfAlts1;
        List<List<Alt>> retval = new ArrayList<List<Alt>>(activeLfAlts1.size() + activeLfAlts2.size());
        Iterator<List<Alt>> it1 = activeLfAlts1.iterator(); Iterator<List<Alt>> it2 = activeLfAlts2.iterator();
        List<Alt> altSet1 = it1.next(); List<Alt> altSet2 = it2.next();
        for (int i = 0; i < lfAlts.size(); i++) {
            // inc to alt set i, if not yet there (or beyond)
            if (altSet1.get(0).altSet < i && it1.hasNext()) altSet1 = it1.next(); 
            if (altSet2.get(0).altSet < i && it2.hasNext()) altSet2 = it2.next();
            // check whether only one or the other has alt set i
            if (altSet1.get(0).altSet == i && altSet2.get(0).altSet != i) retval.add(altSet1);
            else if (altSet2.get(0).altSet == i && altSet1.get(0).altSet != i) retval.add(altSet2);
            else if (altSet1.get(0).altSet == i && altSet2.get(0).altSet == i) {
                // take intersection
                List<Alt> combined = new ArrayList<Alt>(Math.min(altSet1.size(),altSet2.size()));
                for (Alt alt : altSet1) {
                    if (altSet2.contains(alt)) combined.add(alt);
                }
                // check for empty intersection, returning null 
                if (combined.isEmpty()) return null;
                // otherwise add combined list
                retval.add(combined);
            }
        }
        return retval;
    }
    
    
    //-----------------------------------------------------------------
    // misc bookkeeping
    //

    // extracts the nominal atoms marked for labeling in the output
    private void extractLabeledNominals() {
        for (Iterator<SatOp> it = preds.iterator(); it.hasNext(); ) {
        	SatOp pred = it.next();
            if (!HyloHelper.isAttrPred(pred)) continue;
            Nominal nom1 = HyloHelper.getPrincipalNominal(pred);
            if (!(nom1 instanceof NominalAtom)) continue;
            String rel = HyloHelper.getRel(pred);
            if (rel == null || !rel.equals("mark")) continue;
            labeledNominals.add(nom1);
            it.remove();
        }
    }
    
    // lists the nominals in the preds
    private void listNominals() {
    	for (SatOp pred : preds) {
            Nominal nom1 = HyloHelper.getPrincipalNominal(pred);
            Nominal nom2 = HyloHelper.getSecondaryNominal(pred);
            if (nom1 instanceof NominalAtom && !nominals.containsKey(nom1)) { 
                nominals.put(nom1, nominals.size()); 
            }
            if (nom2 instanceof NominalAtom && !nominals.containsKey(nom2)) { 
                nominals.put(nom2, nominals.size()); 
            }
        }
    }
    
    // create bitset for cat indices
    private BitSet getIndices(Category cat, Category cat2) {
        catNominals.clear();
        cat.forall(gatherIndices);
        if (cat2 != null) { cat2.forall(gatherIndices); }
        BitSet retval = new BitSet(nominals.size());
        for (Iterator<Object> it = catNominals.iterator(); it.hasNext(); ) {
            Object nom = it.next();
            int index = nominals.get(nom);
            retval.set(index);
        }
        return retval;
    }

    // check for uninstantiated outer args; if found,  
    // set the indices to allow all combos, and issue 
    // a warning if the debugInstantiation flag is set
    private void checkInstantiation(List<Edge> edges) {
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            if (noSemEdges.contains(edge)) continue; // exempt uninstantiated no sem edges
            if (outerArgUninstantiated(edge.sign.getCategory())) {
                edge.indices.set(0, nominals.size());
                if (debugInstantiation) {
                	System.err.println("Warning: outer arg uninstantiated: " + edge.sign);
                }
            }
        }
    }
        
    // returns whether the outermost arg is not instantiated
    private boolean outerArgUninstantiated(Category cat) {
        if (!(cat instanceof ComplexCat)) return false;
        Arg outer = ((ComplexCat)cat).getOuterArg();
        catNominals.clear();
        outer.forall(gatherIndices);
        return catNominals.isEmpty();
    }
    
    // gathers values of index feature in atomic cats
    private CategoryFcn gatherIndices = new CategoryFcnAdapter() {
        public void forall(Category c) {
            if (!(c instanceof AtomCat)) return;
            FeatureStructure fs = c.getFeatureStructure();
            if (fs == null) return;
            addCatNominal(fs.getValue("index"));
            addCatNominal(fs.getValue("mod-index"));
        }
    };
    
    // adds a nominal atom to catNominals
    private void addCatNominal(Object indexVal) {
        if (indexVal instanceof NominalAtom) { 
            if (!catNominals.contains(indexVal)) { catNominals.add(indexVal); }
        }
    }

    // lists the paired nominals in the input LF, as a bitset pair
    private void listPairedNominals() {
        for (int i=0; i < preds.size(); i++) {
        	SatOp pred = preds.get(i);
            if (!"tup".equals(HyloHelper.getLexPred(pred))) continue;
            Nominal tupNom = HyloHelper.getPrincipalNominal(pred);
            Nominal nom1 = null;
            Nominal nom2 = null;
            for (int j = i+1; j < preds.size(); j++) {
            	SatOp predJ = preds.get(j);
                if (!tupNom.equals(HyloHelper.getPrincipalNominal(predJ))) break;
                if ("Item1".equals(HyloHelper.getRel(predJ))) {
                    nom1 = HyloHelper.getSecondaryNominal(predJ);
                }
                if ("Item2".equals(HyloHelper.getRel(predJ))) {
                    nom2 = HyloHelper.getSecondaryNominal(predJ);
                }
            }
            if (nom1 == null || nom2 == null) {
                System.err.println("Warning, couldn't find paired nominals for tuple: " + tupNom);
                continue;
            }
            if (!(nom1 instanceof NominalAtom)) continue;
            if (!(nom2 instanceof NominalAtom)) continue;
            BitSet[] pair = new BitSet[2];
            pair[0] = new BitSet(nominals.size());
            pair[0].set(nominals.get(nom1));
            pair[1] = new BitSet(nominals.size());
            pair[1].set(nominals.get(nom2));
            pairedNominals.add(pair);
            anyPairedNominals = true;
        }
    }
    
    // adds the bound var nominals
    private void addBoundVarNominals() {
        for (int i=0; i < preds.size(); i++) {
        	SatOp pred = preds.get(i);
            String rel = HyloHelper.getRel(pred);
            if (rel == null || !rel.equals("BoundVar")) continue;
            Nominal nom2 = HyloHelper.getSecondaryNominal(pred);
            if (!(nom2 instanceof NominalAtom)) continue;
            boundVarNominals.add(nom2);
            // check if nom2 is a tuple
            for (int j = 0; j < preds.size(); j++) {
            	SatOp predJ = preds.get(j);
                if (!nom2.equals(HyloHelper.getPrincipalNominal(predJ))) continue;
                if (!"tup".equals(HyloHelper.getLexPred(predJ))) continue;
                // if so, add paired items as bound vars too
                for (int k = j+1; k < preds.size(); k++) {
                	SatOp predK = preds.get(k);
                    if (!nom2.equals(HyloHelper.getPrincipalNominal(predK))) break;
                    String relK = HyloHelper.getRel(predK);
                    if ("Item1".equals(relK) || "Item2".equals(relK)) {
                        Nominal nom2K = HyloHelper.getSecondaryNominal(predK);
                        if (!(nom2K instanceof NominalAtom)) continue;
                        boundVarNominals.add(nom2K);
                    }
                }
            }
        }
    }
    
    /**
     * Returns whether the indices for the two edges are paired in the input LF.
     */
    public boolean pairedWith(Edge edgeA, Edge edgeB) {
        for (int i = 0; i < pairedNominals.size(); i++) {
            BitSet[] pair = pairedNominals.get(i);
            if (pair[0].equals(edgeA.indices) && pair[1].equals(edgeB.indices)) {
                return true;
            }
        }
        return false;
    }
    

    // indexes the preds by their position into predMap
    private void indexPreds() {
        for (int i=0; i < preds.size(); i++) {
            String[] keys = predKeys(preds.get(i));
            for (int j=0; j < keys.length; j++) {
                List<Integer> indices = predMap.get(keys[j]);
                if (indices == null) { 
                    indices = new ArrayList<Integer>(1);
                    predMap.put(keys[j], indices);
                }
                indices.add(i);
            }
        }
    }

    // returns 0-2 keys for the given pred (where nom vars are skipped):
    // a lex pred is indexed by atom(pred)
    // a rel pred is indexed by atom<rel> and <rel>atom2
    // an attr pred is indexed by atom<rel>
    private static String[] predKeys(LF pred) {
        Nominal nom = HyloHelper.getPrincipalNominal(pred);
        String lexPred = HyloHelper.getLexPred(pred);
        String rel = HyloHelper.getRel(pred);
        Nominal nom2 = HyloHelper.getSecondaryNominal(pred);
        List<String> keys = new ArrayList<String>(2);
        if (nom instanceof NominalAtom && lexPred != null) 
            keys.add(nom.toString() + "(" + lexPred + ")");
        if (nom instanceof NominalAtom && rel != null) 
            keys.add(nom.toString() + "<" + rel + ">");
        if (nom2 instanceof NominalAtom && rel != null) 
            keys.add("<" + rel + ">" + nom2.toString());
        return (String[]) keys.toArray(new String[keys.size()]);
    }
    
    // fills in the LF chunks list with the chunks for each pred, 
    // then sorts them by specificity, from most to least
    private void fillLfChunks() {
        // for each pred, fill in chunks
        for (int i=0; i < preds.size(); i++) {
        	SatOp pred = preds.get(i);
        	TIntArrayList chunks = pred.getChunks();
            if (chunks == null) continue;
            // for each chunk that this pred is part of
            for (int j = 0; j < chunks.size(); j++) {
                int chunkId = chunks.get(j);
                // ensure chunk bitset exists
                while (lfChunks.size() < (chunkId + 1)) {
                    lfChunks.add(new BitSet(preds.size()));
                }
                // update chunk bitset
                BitSet chunk = lfChunks.get(chunkId);
                chunk.set(i);
            }
        }
        // do insertion sort, to ensure subset ordering
        List<BitSet> tmpList = new ArrayList<BitSet>(lfChunks);
        lfChunks.clear();
        allChunks: for (BitSet chunk : tmpList) {
            for (int i=0; i < lfChunks.size(); i++) {
                BitSet sortedChunk = lfChunks.get(i);
                if (subset(chunk, sortedChunk)) {
                    lfChunks.add(i, chunk); continue allChunks;
                }
            }
            lfChunks.add(chunk);
        }
    }
    
    // gets the most specific incomplete chunk for an edge, or null
    private BitSet getIncompleteLfChunk(BitSet bitset, List<List<Alt>> activeLfAlts) {
        // check each chunk
        allChunks: for (BitSet lfChunk : lfChunks) {
        	// for intersection
            if (!lfChunk.intersects(bitset)) { continue; }
            // for incomplete coverage
            if (!subset(lfChunk, bitset) && subset(bitset, lfChunk)) {
            	// and for part of all active alts
            	for (List<Alt> altSet : activeLfAlts) {
            		for (Alt alt : altSet) {
            			if (!subset(lfChunk, alt.bitset)) continue allChunks;
            		}
            	}
            	// return chunk
            	return lfChunk;
            }
        }
    	// otherwise null
    	return null;
    }

    // returns true iff bitset1 is a subset of bitset2
	private BitSet tmpBitSet = new BitSet();
	private boolean subset(BitSet bitset1, BitSet bitset2) {
		tmpBitSet.clear();
		tmpBitSet.or(bitset1);
		tmpBitSet.andNot(bitset2);
		return tmpBitSet.isEmpty();
	}
    
    // returns true iff combining the edges would complete a chunk
	private boolean completesChunk(Edge edgeA, Edge edgeB) {
		if (edgeA.incompleteLfChunk != null) {
			tmpBitSet.clear();
			tmpBitSet.or(edgeA.incompleteLfChunk);
			tmpBitSet.andNot(edgeA.bitset); tmpBitSet.andNot(edgeB.bitset);
			if (tmpBitSet.isEmpty()) return true;
		}
		if (edgeB.incompleteLfChunk != null) {
			tmpBitSet.clear();
			tmpBitSet.or(edgeB.incompleteLfChunk);
			tmpBitSet.andNot(edgeA.bitset); tmpBitSet.andNot(edgeB.bitset);
			if (tmpBitSet.isEmpty()) return true;
		}
		return false;
	}
	
    // fills in the LF alts list with the alts for each pred
    private void fillLfAlts() {
        // for each pred
        for (int i=0; i < preds.size(); i++) {
            SatOp pred = preds.get(i);
            List<Alt> alts = pred.getAlts();
            if (alts == null) continue;
            // for each alt that this pred is part of
            for (Alt alt : alts) {
                // ensure list for alt set exists
                while (lfAlts.size() < (alt.altSet + 1)) {
                    lfAlts.add(null);
                }
                List<Alt> altSet = lfAlts.get(alt.altSet);
                if (altSet == null) {
                    altSet = new ArrayList<Alt>(5);
                    lfAlts.set(alt.altSet, altSet);
                }
                // ensure list item for alt num is alt
                while (altSet.size() < (alt.numInSet + 1)) {
                    altSet.add(null);
                }
                altSet.set(alt.numInSet, alt);
                // update alt bitset
                alt.bitset.set(i);
            }
        }
    }
    
    // fills in the LF opts list with the opts for each pred
    private void fillLfOpts() {
        // for each pred
        for (int i=0; i < preds.size(); i++) {
            SatOp pred = preds.get(i);
            TIntArrayList opts = pred.getOpts();
            if (opts == null) continue;
            // for each opt that this pred is part of
            for (int j = 0; j < opts.size(); j++) {
                int optId = opts.get(j);
                // ensure opt bitset exists
                while (lfOpts.size() < (optId + 1)) {
                    lfOpts.add(new BitSet(preds.size()));
                }
                // update opt bitset
                BitSet opt = lfOpts.get(optId);
                opt.set(i);
            }
        }
    }
    
    
    // returns the list of coart rels for the pred with the given index
    // NB: assumes that preds are sorted by their principal nominals, with the lex pred first
    private List<String> getCoartRels(int predIndex) {
    	SatOp pred = preds.get(predIndex);
        Nominal nom = HyloHelper.getPrincipalNominal(pred);
        List<String> retval = null;
        for (int i = predIndex+1; i < preds.size(); i++) {
        	SatOp relPred = preds.get(i);
            if (!nom.equals(HyloHelper.getPrincipalNominal(relPred))) break;
            String rel = HyloHelper.getRel(relPred);
            if (rel != null && grammar.lexicon.isCoartRel(rel)) { 
                if (retval == null) retval = new ArrayList<String>(3);
                retval.add(rel);
            }
        }
        return retval;
    }

    
    //-----------------------------------------------------------------
    // createInitialEdges
    //

    /** 
     * Creates and returns all initial edges.
     * In particular, initializes all lexical edges that cover some of the input semantics;  
     * also initializes edges for semantically null lexical items, 
     * and initializes instances of type changing rules which 
     * introduce their own semantics.
     * If a hypertagger is in place, only the beta-best edges are returned for each EP.
     */
    public List<Edge> createInitialEdges() {

        // marked initial edges that need to be licensed
        List<Edge> markedEdgesForLicensing = new ArrayList<Edge>(); 
        
        // for each pred, create edges for signs indexed 
        // by lexical preds and by indexed rels; 
        // and similarly for type changing rules
        for (int i=0; i < preds.size(); i++) {
        	SatOp pred = preds.get(i);
            String key = HyloHelper.getLexPred(pred);
            String rel = HyloHelper.getRel(pred);
            // skip if no lex pred or indexed rel (not expected)
            if (key == null && rel == null) continue;
            // update hypertagger for beta-best lookup
            if (hypertagger != null) hypertagger.setPred(i); 
            Collection<Sign> signs = new ArrayList<Sign>();
            Collection<TypeChangingRule> typeChangingRules = new ArrayList<TypeChangingRule>();
            // add signs and rules for lex pred
            if (key != null) {
                List<String> coartRels = getCoartRels(i);
                Collection<Sign> lexPredSigns = lexicon.getSignsFromPred(key, coartRels);
                if (lexPredSigns != null) { signs.addAll(lexPredSigns); }
                Collection<TypeChangingRule> lexPredRules = grammar.rules.getRulesForPred(key);
                if (lexPredRules != null) { typeChangingRules.addAll(lexPredRules); }
            }
            // add signs and rules for indexed rel
            if (rel != null) {
                Collection<Sign> indexedRelSigns = lexicon.getSignsFromRel(rel);
                if (indexedRelSigns != null) { signs.addAll(indexedRelSigns); }
                Collection<TypeChangingRule> indexedRelRules = grammar.rules.getRulesForRel(rel);
                if (indexedRelRules != null) { typeChangingRules.addAll(indexedRelRules); }
            }
            // create initial and marked edges for each sign, updating feature map
            for (Sign sign : signs) {
                List<Edge> initialEdgesForSign = createInitialEdges(sign, i);
                if (initialEdgesForSign != null) {
                    for (Edge initialEdge : initialEdgesForSign) {
                        Category cat = initialEdge.sign.getCategory();
                        if (featureLicenser.needsLicensing(cat))
                            markedEdgesForLicensing.add(initialEdge);
                        else {
                            initialEdges.add(initialEdge);
                            featureLicenser.updateFeatureMap(cat);
                        }
                    }
                }
            }
            // create rules instances for each rule, updating feature map
            for (TypeChangingRule rule : typeChangingRules) {
                List<RuleInstance> ruleInstancesForRule = createRuleInstances(rule, i);
                if (ruleInstancesForRule != null) {
                    for (RuleInstance ruleInst : ruleInstancesForRule) { 
                        ruleInstances.add(ruleInst);
                        featureLicenser.updateFeatureMap(ruleInst.rule.getArg());
                        featureLicenser.updateFeatureMap(ruleInst.rule.getResult());
                    }
                }
            }
        }
        
        // add licensed, marked initial edges
        int prevSize;
        do { // while list size is changing
            prevSize = markedEdgesForLicensing.size();
            for (Iterator<Edge> it = markedEdgesForLicensing.iterator(); it.hasNext(); ) {
                // check each edge
                Edge edge = it.next();
                Category cat = edge.sign.getCategory();
                if (featureLicenser.isLicensed(cat)) {
                    // and add to marked edges if licensed
                    markedEdges.add(edge);
                    it.remove();
                    // updating feature map
                    featureLicenser.updateFeatureMap(cat);
                }
            }
        } while (markedEdgesForLicensing.size() != prevSize);
        
        // initialize general rules
        initGeneralRules();

        // initialize edges for semantically null lexical items        
        initNoSemEdges();
        
        // collect all initial edges
        List<Edge> retval = new ArrayList<Edge>(
            initialEdges.size() + markedEdges.size() + 
            instantiatedNoSemEdges.size() + noSemEdges.size()
        );
        retval.addAll(initialEdges);
        retval.addAll(markedEdges);
        retval.addAll(instantiatedNoSemEdges);
        retval.addAll(noSemEdges);
        
        // check instantiation of outermost cats
        checkInstantiation(retval);
        
        // set uncovered EPs
        uncoveredEPs = uncoveredPreds();
        // warn if EPs missing and debug instantiation flag set
        if (uncoveredEPs != null && debugInstantiation) {
        	System.err.println("Warning, uncovered preds after lex instantiation: " + Edge.toString(uncoveredEPs));
        }
        
        // set opts for missing relations, if apropos
        if (useRelaxedRelationMatching) addLFOptsForUncoveredPreds();
        	
        // return
        return retval;
    }
    
    // return null if LF doesn't unify with preds
    private List<Edge> createInitialEdges(Sign sign, int predIndex) {
        // get parts of sign 
        List<Word> words = sign.getWords();
        Category cat = sign.getCategory();
        // instantiate
        List<Pair<Substitution,BitSet>> instantiations = instantiate(cat, null, predIndex);
        // check for failure
        if (instantiations == null) return null;
        // otherwise fill cats and make edges
        List<Edge> retval = new ArrayList<Edge>(instantiations.size());
        for (Pair<Substitution,BitSet> inst : instantiations) {
            Substitution subst = inst.a; BitSet bitset = inst.b;
            Category filledCat = null;
            try {
                filledCat = (Category) cat.fill(subst);
            } catch (UnifyFailure uf) {
                // shouldn't happen
                throw new RuntimeException("Unable to fill cat: " + uf);
            }
            // index subcategorized semantically null words
            featureLicenser.indexSemanticallyNullWords(filledCat);
            // update lex origins for new sign
            Sign newSign = new Sign(words, filledCat);
            newSign.setOrigin();
            // and add new edge
            List<List<Alt>> activeLfAlts = getActiveLfAlts(lfAlts, bitset);
            retval.add(makeEdge(newSign, bitset, activeLfAlts));
        }
        // and return them
        return retval;
    }
    
    // return null if result LF doesn't unify with preds
    private List<RuleInstance> createRuleInstances(TypeChangingRule rule, int predIndex) {
        // get parts of rule
        Category result = rule.getResult();
        Category arg = rule.getArg();
        // instantiate
        List<Pair<Substitution,BitSet>> instantiations = instantiate(result, arg, predIndex);
        // check for failure
        if (instantiations == null) return null;
        // otherwise fill cats and make rule instances
        List<RuleInstance> retval = new ArrayList<RuleInstance>(instantiations.size());
        for (Pair<Substitution,BitSet> inst : instantiations) {
            Substitution subst = inst.a; BitSet bitset = inst.b;
            Category filledResult = null; Category filledArg = null;
            try {
                filledResult = (Category) result.fill(subst);
                filledArg = (Category) arg.fill(subst);
            } catch (UnifyFailure uf) {
                // shouldn't happen
                throw new RuntimeException("Unable to fill cat: " + uf);
            }
            // index subcategorized semantically null words
            featureLicenser.indexSemanticallyNullWords(filledArg);
            featureLicenser.indexSemanticallyNullWords(filledResult);
            // and return new rule instance
            BitSet indices = getIndices(filledResult, filledArg);
            TypeChangingRule newRule = new TypeChangingRule(filledArg, filledResult, rule.name(), rule.getFirstEP());
            ruleInstancesGroup.addRule(newRule);
            List<List<Alt>> activeLfAlts = getActiveLfAlts(lfAlts, bitset);
            RuleInstance ruleInst = new RuleInstance(newRule, bitset, indices, activeLfAlts);
            retval.add(ruleInst);
        }
        // and return them
        return retval;
    }
    

    // return null if cat LF doesn't unify with preds
    private List<Pair<Substitution,BitSet>> instantiate(Category cat, Category cat2, int predIndex) {

        // unify with indexed pred
        UnifyControl.reindex(cat, cat2); 
        List<SatOp> lfPreds = HyloHelper.getPreds(cat.getLF());
        Substitution subst = null;
        SatOp indexedPred = preds.get(predIndex);
        int lfPredIndex = -1;
        for (int i=0; i < lfPreds.size(); i++) {
            LF lfPred = lfPreds.get(i);
            subst = new SimpleSubstitution();
            try {
                Unifier.unify(lfPred, indexedPred, subst);
                lfPredIndex = i;
                break;
            } catch (UnifyFailure uf) {}
        }
        
        // if failed, return empty list
        if (lfPredIndex == -1) return null;
        
        // set indexed pred in bitset
        BitSet bitset = new BitSet(preds.size());
        bitset.set(predIndex);
        
        // unify with rest of lfPreds, extending subst/bitset
        List<SatOp> remainingPreds = new ArrayList<SatOp>(lfPreds.size());
        remainingPreds.addAll(lfPreds);
        remainingPreds.remove(lfPredIndex);
        int prevSize = -1;
        List<Pair<Substitution,BitSet>> retval = new ArrayList<Pair<Substitution,BitSet>>(3);
        List<Pair<Substitution,BitSet>> prev = new ArrayList<Pair<Substitution,BitSet>>(3);
        retval.add(new Pair<Substitution, BitSet>(subst, bitset));
        // loop until empty or no changes, in order to propagate matches
        while (!remainingPreds.isEmpty() && remainingPreds.size() != prevSize) {
            prevSize = remainingPreds.size();
            for (Iterator<SatOp> it = remainingPreds.iterator(); it.hasNext(); ) {
            	SatOp lfPred = it.next();
                try {
                    // fill index
                    lfPred = (SatOp) lfPred.fill(subst);
                } catch (UnifyFailure uf) {
                    // shouldn't happen
                    throw new RuntimeException("Unable to fill lfPred: " + uf);
                }
                // find matching pred
                String[] lfPredKeys = predKeys(lfPred);
                if (lfPredKeys.length == 0) {
                    // nb: this means the lfPred is underconstrained; 
                    //     will need to check it later!
                    continue;
                }
                List<Integer> matchingPredIndices = new ArrayList<Integer>(3);
                for (int i = 0; i < lfPredKeys.length; i++) {
                    List<Integer> indices = predMap.get(lfPredKeys[i]);
                    if (indices != null) matchingPredIndices.addAll(indices);
                }
                if (matchingPredIndices.isEmpty()) {
                	if (useRelaxedRelationMatching && HyloHelper.isRelPred(lfPred)) continue; // skip
                	else return null; // fail
                }
                // try extending each subst/bitset: 
                // first swap retval, prev, and clear retval
                List<Pair<Substitution,BitSet>> tmp = prev;
                prev = retval; retval = tmp; 
                retval.clear();
                for (Pair<Substitution,BitSet> inst : prev) {
                    Substitution s = inst.a; BitSet b = inst.b;
                    if (matchingPredIndices.size() == 1) { // reuse current instantiation
                        int matchingPredIndex = matchingPredIndices.get(0);
                        b.set(matchingPredIndex);
                        if (checkAlts(b)) {
                            try { // unify
                            	SatOp matchingPred = preds.get(matchingPredIndex);
                                Unifier.unify(lfPred, matchingPred, s);
                                retval.add(inst);
                            } catch (UnifyFailure uf) {}
                        }
                    }
                    else { // otherwise make copies
                        for (int matchingPredIndex : matchingPredIndices) {
                            Substitution s2 = new SimpleSubstitution((SimpleSubstitution)s);
                            BitSet b2 = (BitSet)b.clone();
                            b2.set(matchingPredIndex);
                            if (checkAlts(b2)) {
                                try { // unify
                                	SatOp matchingPred = preds.get(matchingPredIndex);
                                    Unifier.unify(lfPred, matchingPred, s2);
                                    Pair<Substitution,BitSet> inst2 = new Pair<Substitution, BitSet>(s2, b2);
                                    retval.add(inst2);
                                } catch (UnifyFailure uf) {}
                            }
                        }
                    }
                }
                if (retval.isEmpty()) {
                	if (useRelaxedRelationMatching && HyloHelper.isRelPred(lfPred)) {
                		retval.addAll(prev);
                		continue; // skip
                	}
                	else return null; // fail
                }
                it.remove();
            }
        }
        
        // check for no more than one (rel) pred left over
        if (remainingPreds.size() > 1) return null;
        // done
        return retval;
    }
    
    
    // returns true iff no alt exclusions are violated
    // nb: needs to check that if there any intersections 
    //     with multiple alts, then these are only in the shared part
    private boolean checkAlts(BitSet b) {
        for (List<Alt> altSet : lfAlts) {
            int intersects = 0;
            for (Alt alt : altSet) {
                if (alt.bitset.intersects(b)) intersects++;
            }
            if (intersects > 1) { // check intersections 
                for (int i = 0; i < altSet.size(); i++) {
                    Alt alt = altSet.get(i);
                    if (alt.bitset.intersects(b)) {
                        for (int j = i+1; j < altSet.size(); j++) {
                            Alt alt2 = altSet.get(j);
                            if (alt2.bitset.intersects(b)) {
                                BitSet altOnly = (BitSet) alt.bitset.clone();
                                altOnly.andNot(alt2.bitset);
                                BitSet alt2Only = (BitSet) alt2.bitset.clone();
                                alt2Only.andNot(alt.bitset);
                                if (altOnly.intersects(b) && alt2Only.intersects(b))
                                    return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    

    //-----------------------------------------------------------------
    // createNewEdges
    //

    /**
     * Returns all edges that can be created by combining the given edges, 
     * without collecting combos.
     */
    public List<Edge> createNewEdges(Edge edge, Edge next) {
        return createNewEdges(edge, next, false);
    }
     
    /**
     * Returns all edges that can be created by combining the given edges;
     * if the collectCombos flag is true, the edges are updated with collected combos, 
     * and additional alt edges are made for the remaining alternative edges for 
     * the given first edge.
     */
    public List<Edge> createNewEdges(Edge edge, Edge next, boolean collectCombos) {
        
        // check for sem overlap
        if (edge.intersects(next)) return Collections.emptyList();
        // check LF chunk constraints
        if (useChunks) {
            if (!edge.meetsLfChunkConstraints(next) || 
                !next.meetsLfChunkConstraints(edge)) return Collections.emptyList();
        }
        
        // make new edges ...
        List<Edge> newEdges = null;
        // when using indexing:
        if (useIndexing) {
            // check for intersecting indices
            if (edge.indicesIntersect(next)) {
                newEdges = createNewEdges(edge, next, collectCombos, true);
            }
            // check for PairedWith relation
            else if (anyPairedNominals && pairedWith(edge, next)) {
                newEdges = createNewEdges(edge, next, collectCombos, false);
            }
            else if (anyPairedNominals && pairedWith(next, edge)) {
                newEdges = createNewEdges(next, edge, collectCombos, false);
            }
	    // check for a missing index nominal on the target cat,
	    // which can indicate a type-raised category that needs to combine
	    // before its indices become adjacent
            else if (allowMissingIndexCombos && (edge.getIndexNominal() == null || next.getIndexNominal() == null))
	    {
                newEdges = createNewEdges(edge, next, collectCombos, true);
            }
            else { return Collections.emptyList(); }
        } 
        // otherwise try everything
        else {
            newEdges = createNewEdges(edge, next, collectCombos, true);
        }
        
        // make alt edges for rest of edge's alts, with collectCombos option
        if (collectCombos && edge.altEdges.size() > 0) {
            int numNewEdges = newEdges.size(); // get num before adding any more
            for (int i = 0; i < numNewEdges; i++) {
                Edge resultEdge = newEdges.get(i);
                Sign resultSign = resultEdge.sign;
                Category resultCat = resultSign.getCategory();
                Rule rule = resultSign.getDerivationHistory().getRule();
                Sign[] resultInputs = resultSign.getDerivationHistory().getInputs(); 
                boolean rightward = (resultInputs[0] == next.sign);
                boolean lefthead = (resultSign.getLexHead() == resultInputs[0].getLexHead());
                for (int j = 0; j < edge.altEdges.size(); j++) {
                    Edge furtherEdge = edge.altEdges.get(j);
                    if (furtherEdge == edge) continue;
                    Sign[] signs = (rightward) 
                        ? new Sign[] { next.sign, furtherEdge.sign }
                        : new Sign[] { furtherEdge.sign, next.sign };
                    Sign lexHead = (rightward == lefthead) 
                    	? next.sign.getLexHead() 
            			: furtherEdge.sign.getLexHead(); 
                	Sign altSign = Sign.createDerivedSignWithNewLF(resultCat, signs, rule, lexHead);
                    newEdges.add(makeAltEdge(altSign, resultEdge));
                }
            }
        }
        
        // check instantiation of outermost cats
        checkInstantiation(newEdges);
        
        // done
        return newEdges;
    }

    // creates edges, combining in one or both directions per flag
    private List<Edge> createNewEdges(Edge edgeA, Edge edgeB, boolean collectCombos, boolean bothDirections) {

        // get combined alts, checking compatibility        
        List<List<Alt>> combinedLfAlts = getCombinedLfAlts(edgeA.activeLfAlts, edgeB.activeLfAlts);
        if (combinedLfAlts == null) return Collections.emptyList();
        
        // check whether a chunk is completed when gluing fragments
        boolean fragCompletion = false;
        if (gluingFragments) fragCompletion = completesChunk(edgeA, edgeB);
        
        // A B combos
        List<Sign> results;
        if (gluingFragments) results = generalRules.applyGlueRule(edgeA.sign, edgeB.sign);
        else results = generalRules.applyBinaryRules(edgeA.sign, edgeB.sign);
        binaryRuleApps++; 
        int numResults = results.size();
        
        // B A combos
        List<Sign> reversedResults = Collections.emptyList();
        if (bothDirections) {
        	if (gluingFragments) reversedResults = generalRules.applyGlueRule(edgeB.sign, edgeA.sign);
        	else reversedResults = generalRules.applyBinaryRules(edgeB.sign, edgeA.sign);
            binaryRuleApps++; 
        }
        int numReversedResults = reversedResults.size();
        
        // make edges to return, updating edge combos (if apropos)
        List<Edge> retval = Collections.emptyList();
        if (numResults + numReversedResults > 0) {
            retval = new ArrayList<Edge>(numResults + numReversedResults);
            BitSet union = (BitSet) edgeA.bitset.clone();
            union.or(edgeB.bitset);
            int cardBefore = union.cardinality();
            List<List<Alt>> activeLfAlts = getActiveLfAlts(combinedLfAlts, union);
            // check for alt completion when gluing fragments
            if (gluingFragments && union.cardinality() > cardBefore) fragCompletion = true;
            for (int i = 0; i < numResults; i++) {
                Sign sign = results.get(i);
                if (fragCompletion) { ((AtomCat)sign.getCategory()).fragCompletion = true; }
                Edge resultEdge = makeEdge(sign, union, activeLfAlts); 
                retval.add(resultEdge);
                if (collectCombos) {
                    edgeA.edgeCombos.addRightwardCombo(edgeB, resultEdge);
                    edgeB.edgeCombos.addLeftwardCombo(edgeA, resultEdge);
                }
            }
            for (int i = 0; i < numReversedResults; i++) {
                Sign sign = reversedResults.get(i);
                if (fragCompletion) { ((AtomCat)sign.getCategory()).fragCompletion = true; }
                Edge resultEdge = makeEdge(sign, union, activeLfAlts); 
                retval.add(resultEdge);
                if (collectCombos) {
                    edgeB.edgeCombos.addRightwardCombo(edgeA, resultEdge);
                    edgeA.edgeCombos.addLeftwardCombo(edgeB, resultEdge);
                }
            }
        }
        
        // done
        return retval;
    }

    
    /**
     * Returns all edges that can be created by applying a unary rule 
     * to the given edge or by combining it with a purely syntactic edge, 
     * without collecting combos.
     */
    public List<Edge> createNewEdges(Edge edge) {
        return createNewEdges(edge, false);
    }
    
    /**
     * Returns all edges that can be created by applying a unary rule 
     * to the given edge, or by combining it with a purely syntactic edge,
     * or by completing a realization/chunk/alt with an optional part,  
     * while updating the given edge with collected combos, 
     * if the collectCombos flag is true.
     * When gluing fragments, only the opt completion step is done.
     */
    public List<Edge> createNewEdges(Edge edge, boolean collectCombos) {
        
        List<Edge> retval = null; // instantiate on demand
        
        if (!gluingFragments) {
	        	
	        List<Sign> genResults = generalRules.applyUnaryRules(edge.sign);
	        unaryRuleApps++;
	        // make edges for results, updating edge combos
	        if (genResults.size() > 0) {
	            if (retval == null) retval = new ArrayList<Edge>(genResults.size());
	            for (int i = 0; i < genResults.size(); i++) {
	                Sign sign = genResults.get(i);
					// check for unary rule cycle; skip result if found
	                if (sign.getDerivationHistory().containsCycle()) continue;
	                Edge resultEdge = makeEdge(sign, edge.bitset, edge.activeLfAlts); 
	                retval.add(resultEdge);
	                if (collectCombos) edge.edgeCombos.unaryResults.add(resultEdge);
	            }
	        }
	        
	        // do rule instances
	        Sign[] signs = { edge.sign };
	        for (int i = 0; i < ruleInstances.size(); i++) {
	            RuleInstance ruleInst = ruleInstances.get(i);
	            // check sem overlap
	            if (edge.intersects(ruleInst)) continue; 
	            // check for indices in common
	            if (useIndexing && !edge.indicesIntersect(ruleInst)) continue; 
	            // check LF chunk constraints
	            if (useChunks && !edge.meetsLfChunkConstraints(ruleInst)) continue;
	            // get combined alts, checking compatibility        
	            List<List<Alt>> combinedLfAlts = getCombinedLfAlts(edge.activeLfAlts, ruleInst.activeLfAlts);
	            if (combinedLfAlts == null) continue;
	        
	            // apply rule
	            List<Sign> instResults = new ArrayList<Sign>(1);
	            ruleInst.rule.applyRule(signs, instResults);
	            unaryRuleInstApps++;
	            if (instResults.size() > 0) {
	                if (retval == null) retval = new ArrayList<Edge>(instResults.size());
	                BitSet union = (BitSet) edge.bitset.clone();
	                union.or(ruleInst.bitset);
	                List<List<Alt>> activeLfAlts = getActiveLfAlts(combinedLfAlts, union);
	                for (int j = 0; j < instResults.size(); j++) {
	                    Sign sign = instResults.get(j);
	    				// check for unary rule cycle; skip result if found
	                    if (sign.getDerivationHistory().containsCycle()) continue;
	                    Edge resultEdge = makeEdge(sign, union, activeLfAlts); 
	                    retval.add(resultEdge);
	                    if (collectCombos) edge.edgeCombos.unaryResults.add(resultEdge);
	                }
	            }
	        }
        }
        
        // do opt completed edges
        if (!lfOpts.isEmpty() && !edge.complete()) {
            // get completed bitsets for each completed active alt or chunk, and for whole thing
            List<BitSet> optCompleted = new ArrayList<BitSet>(2);
            addOptCompletedBitSet(edge, allPreds, optCompleted);
            for (List<Alt> altSet : edge.activeLfAlts) {
                for (Alt alt : altSet) {
                    addOptCompletedBitSet(edge, alt.bitset, optCompleted);
                }
            }
            for (BitSet chunk : lfChunks) {
                addOptCompletedBitSet(edge, chunk, optCompleted);
            }
            // for each completed bitset, make complete edge with same sign
            for (BitSet completed : optCompleted) {
                List<List<Alt>> activeLfAlts = getActiveLfAlts(edge.activeLfAlts, completed);
                // set frag completion if apropos
                if (gluingFragments && edge.sign.getCategory() instanceof AtomCat) {
                	AtomCat ac = (AtomCat) edge.sign.getCategory();
                	if (ac.isFragment()) ac.fragCompletion = true;
                }
                Edge resultEdge = makeEdge(edge.sign, completed, activeLfAlts);
                resultEdge.optCompletes = edge;
                if (retval == null) retval = new ArrayList<Edge>(1);
                retval.add(resultEdge);
                if (collectCombos) edge.edgeCombos.optionalResults.add(resultEdge);
            }
        }

        // ensure retval instantiated        
        if (retval == null) retval = Collections.emptyList();
        
        // check instantiation of outermost cats
        if (!gluingFragments) checkInstantiation(retval);
        
        // done
        return retval;
    }

    // bitset for checking completeness
    private BitSet tmpBitSetCompleteness = new BitSet();
    
    // bitset for making retval
    private BitSet tmpBitSetRetval = new BitSet();
    
    // adds a bitset with optional parts completed within the given bitset scope 
    // to the given list, if the optional parts complete the given edge's bitset
    private void addOptCompletedBitSet(Edge edge, BitSet bitset, List<BitSet> optCompleted) {
        // check whether already complete
    	tmpBitSetRetval.clear(); tmpBitSetRetval.or(edge.bitset);
    	tmpBitSetRetval.and(bitset);
        if (tmpBitSetRetval.cardinality() == bitset.cardinality()) return;
        tmpBitSetRetval.or(edge.bitset);
        // or retval with opts when apropos
        for (BitSet opt : lfOpts) {
            if (subset(opt, bitset)) {
            	if (edge.bitset.intersects(opt)) continue; // skip if opt not entirely missing
            	tmpBitSetRetval.or(opt);
            }
        }
        // check completeness, add retval if complete (and distinct)
        tmpBitSetCompleteness.clear(); tmpBitSetCompleteness.or(bitset); 
        tmpBitSetCompleteness.and(tmpBitSetRetval);
        if (tmpBitSetCompleteness.cardinality() == bitset.cardinality()) {
            if (!optCompleted.contains(tmpBitSetRetval)) 
            	optCompleted.add((BitSet)tmpBitSetRetval.clone());
        }
    }
    
    
    /** Returns the edges that can be made by constructing alternative edges 
        from the given edge and the collected combos in its representative edge. */
    public List<Edge> createAltEdges(Edge edge, Edge repEdge) {
        // instantiate return list with right capacity
        EdgeCombos edgeCombos = repEdge.edgeCombos;
        int numResults = numResultsFromCombos(edgeCombos.rightwardCombos);
        numResults += numResultsFromCombos(edgeCombos.leftwardCombos);
        numResults += edgeCombos.unaryResults.size();
        numResults += edgeCombos.optionalResults.size();
        List<Edge> retval = new ArrayList<Edge>(numResults);
        // make alt edges
        addAltsFromCombos(edge, edgeCombos.rightwardCombos, true, retval);
        addAltsFromCombos(edge, edgeCombos.leftwardCombos, false, retval);
        addAltsFromUnaryResults(edge, edgeCombos.unaryResults, retval);
        addAltsFromOptionalResults(edge, edgeCombos.optionalResults, retval);
        // done
        return retval;
    }
    
    // returns the number of results from the given combos
    private int numResultsFromCombos(List<EdgeCombos.CatCombo> combos) {
        int retval = 0;
        for (int i = 0; i < combos.size(); i++) {
            EdgeCombos.CatCombo combo = combos.get(i);
            retval += combo.inputEdge.altEdges.size();
        }
        return retval;
    }
    
    // adds alt edges for the given edge, combos, and direction to results
    private void addAltsFromCombos(Edge edge, List<EdgeCombos.CatCombo> combos, boolean rightward, List<Edge> results) {
        for (EdgeCombos.CatCombo combo : combos) {
            Edge resultEdge = combo.resultEdge;
            Sign resultSign = resultEdge.sign;
            Category resultCat = resultSign.getCategory();
            Rule rule = resultSign.getDerivationHistory().getRule();
            Sign[] resultInputs = resultSign.getDerivationHistory().getInputs(); 
            boolean lefthead = (resultSign.getLexHead() == resultInputs[0].getLexHead());
            List<Edge> comboEdges = combo.inputEdge.altEdges;
            for (Edge comboEdge : comboEdges) {
                Sign[] signs = (rightward) 
                    ? new Sign[] { edge.sign, comboEdge.sign }
                    : new Sign[] { comboEdge.sign, edge.sign };
                Sign lexHead = (rightward == lefthead) 
                	? edge.sign.getLexHead() 
        			: comboEdge.sign.getLexHead();
                Sign altSign = Sign.createDerivedSignWithNewLF(resultCat, signs, rule, lexHead);
                results.add(makeAltEdge(altSign, resultEdge));
            }
        }
    }
    
    // adds alt edges for the given edge and unary results to results
    private void addAltsFromUnaryResults(Edge edge, List<Edge> unaryResults, List<Edge> results) {
        for (Edge resultEdge : unaryResults) {
            Sign resultSign = resultEdge.sign;
            Category resultCat = resultSign.getCategory();
            Rule rule = resultSign.getDerivationHistory().getRule();
            Sign[] signs = { edge.sign };
            Sign lexHead = edge.sign.getLexHead();
            Sign altSign = Sign.createDerivedSignWithNewLF(resultCat, signs, rule, lexHead);
            results.add(makeAltEdge(altSign, resultEdge));
        }
    }
    
    // adds alt edges for the given edge and optional results to results
    private void addAltsFromOptionalResults(Edge edge, List<Edge> optionalResults, List<Edge> results) {
        for (Edge resultEdge : optionalResults) { 
            results.add(makeAltEdge(edge.sign, resultEdge));
        }
    }
    
    
    /** Returns the number of rule applications executed. */
    public int ruleApps() {
        return 
            unaryRuleApps * generalRules.getUnaryRules().size() +
            unaryRuleInstApps +
            binaryRuleApps * generalRules.getBinaryRules().size();
    }
    

    //-----------------------------------------------------------------
    // initGeneralRules
    //

    // separates out general rules with no semantics
    // nb: could consider adding feature licensing for type changing rules with no semantics
    private void initGeneralRules() {
        // add all binary rules to general rules
        for (Rule r : grammar.rules.getBinaryRules()) {
            generalRules.addRule(r);
        }
        // add type raising rules, and type changing ones with no semantics too
        for (Rule r : grammar.rules.getUnaryRules()) {
            // skip type changing rules with semantics
            if (r instanceof TypeChangingRule) {
                TypeChangingRule rule = (TypeChangingRule) r;
                if (rule.getResult().getLF() != null) { continue; }
            }
            // otherwise add it
            generalRules.addRule(r);
        }
    }
    
        
    //-----------------------------------------------------------------
    // initNoSemEdges
    //

    // creates edges for signs flagged as having no semantics,  
    // and with appropriate licensing values in the initial edges
    private void initNoSemEdges() {
        // lookup signs by special index rel constant NO_SEM_FLAG
        lexicon.setSupertagger(null); // turn off hypertagger first
        Collection<Sign> noSemSigns = lexicon.getSignsFromRel(Lexicon.NO_SEM_FLAG);
        lexicon.setSupertagger(hypertagger); // reset hypertagger
        if (noSemSigns == null) return;
        // sets for accumulating no sem edges
        Set<Edge> instEdges = new HashSet<Edge>();
        Set<Edge> uninstEdges = new HashSet<Edge>();
        // add signs with no LF and with matching licensing values
        Set<Category> instantiatedCats = new HashSet<Category>();
        Set<Category> uninstantiatedCats = new HashSet<Category>();
        List<List<Alt>> emptyLfAlts = Collections.emptyList();
        // loop until no more no sem edges
        int numInstEdges, numUninstEdges;
        do {
        	numInstEdges = instEdges.size(); numUninstEdges = uninstEdges.size();
	        for (Sign sign : noSemSigns) {
	            Category cat = sign.getCategory();
	            // get licensed, potentially instantiated cats
	            instantiatedCats.clear();
	            uninstantiatedCats.clear();
	            featureLicenser.licenseEmptyCat(cat, instantiatedCats, uninstantiatedCats);
	            // add edges for instantiated cats to initial edges, updating
				// feature map
	            for (Category instCat : instantiatedCats) {
                    featureLicenser.updateFeatureMap(instCat);
                    featureLicenser.indexSemanticallyNullWords(instCat);
	                Sign instSign = new Sign(sign.getWords(), instCat);
	                instEdges.add(makeEdge(instSign, new BitSet(preds.size()), emptyLfAlts));
	            }
	            // add edges for uninstantiated cats to no-sem edges, updating
				// feature map
	            for (Category uninstCat : uninstantiatedCats) {
                    featureLicenser.updateFeatureMap(uninstCat);
                    featureLicenser.indexSemanticallyNullWords(uninstCat);
	                Sign uninstSign = new Sign(sign.getWords(), uninstCat);
	                Edge noSemEdge = makeEdge(uninstSign, new BitSet(preds.size()), emptyLfAlts); 
	                uninstEdges.add(noSemEdge);
	            }
	        }
        } while (numInstEdges != instEdges.size() || numUninstEdges != uninstEdges.size());
        // update no sem edge lists
        instantiatedNoSemEdges.addAll(instEdges);
        noSemEdges.addAll(uninstEdges);
    }
}

