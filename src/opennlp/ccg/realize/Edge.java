///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-5 University of Edinburgh (Michael White)
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

import opennlp.ccg.synsem.*;
import opennlp.ccg.hylo.*;
import opennlp.ccg.*;
import java.util.*;
import java.util.prefs.*;
import java.text.*;

/**
 * <p>
 * An edge is a tracker for a sign, ie a sign together 
 * with bitsets representing its coverage of the 
 * input predicates and the semantic indices used, 
 * along with lists of the active LF alts.
 * It also has a completeness percentage and a score, 
 * as well as its most specific incomplete LF chunk (if any).
 * Edges are created by an EdgeFactory and managed by a Chart.
 * </p>
 * <p>
 * A representative edge is an edge that represents (stands in for) 
 * other edges with the same category during the chart generation
 * process.  A representative edge has a list of alternative edges, 
 * which are assumed to share the same category; it is considered 
 * disjunctive when there is more than one alternative.
 * Note that initially a representative edge will be in its list 
 * of alternatives, but it can be removed during pruning.
 * Finally, for the anytime search, a representative edge can  
 * maintain a collection of successful combinations, to avoid 
 * invoking the combinatory rules multiple times with the same 
 * combinations of categories.
 * </p>
 *
 * @author      Michael White
 * @version     $Revision: 1.32 $, $Date: 2010/08/10 04:10:15 $
 */
public class Edge extends Tracker
{

    /** Preference key for showing completeness. */
    public static final String SHOW_COMPLETENESS = "Show Completeness";

    /** Preference key for showing coverage bitset. */
    public static final String SHOW_BITSET = "Show Bitset";

    
    /** The sign. */
    protected Sign sign;
    
    /** The completeness percentage. */
    public final float completeness;
    
    /** The edge score. */
    public final double score;
    
    /** The most specific incomplete LF chunk (if any). */
    public final BitSet incompleteLfChunk;
    
    /** The alternative edges (none initially). */
    protected List<Edge> altEdges = null;
    
    /** The edge combos (none initially). */
    protected EdgeCombos edgeCombos = null;
    
    /** The edge, if any, that this edge is constructed from 
        by marking optional bits as completed. */
    protected Edge optCompletes = null;
    
    
    /** Constructor. */
    public Edge(Sign sign, BitSet bitset, BitSet indices, 
                float completeness, double score, 
                List<List<Alt>> activeLfAlts, BitSet incompleteLfChunk) 
    {
        super(bitset, indices, activeLfAlts);
        this.sign = sign;
        this.completeness = completeness; this.score = score; 
        this.incompleteLfChunk = incompleteLfChunk;
    }

    
    /** Returns the sign. */
    public Sign getSign() { return sign; }
    
    /** Returns whether this edge has completeness 1.0, ie, covers all the input preds. */
    public boolean complete() {
        return (completeness == 1.0);
    }
    
    /**
     * Returns the nominal which is the value of the index feature on the 
     * sign's target cat, or null if none.
     */
    public Nominal getIndexNominal() { return sign.getCategory().getIndexNominal(); }
    

    /**
     * Returns true iff this edge can combine with the given tracker 
     * without violating its LF chunk constraint (if any).
     * Specifically, returns true when this edge has no incomplete chunk 
     * or the tracker is semantically empty; otherwise, returns true 
     * iff the incomplete chunk intersects with the tracker's 
     * coverage vector.
     */
    public boolean meetsLfChunkConstraints(Tracker tracker) {
        if (incompleteLfChunk == null || tracker.bitset.isEmpty()) return true;
        return incompleteLfChunk.intersects(tracker.bitset);
    }
    

    /** Returns whether this edge is a representative. */
    public boolean isRepresentative() { return altEdges != null; }
    
    /** Returns whether this edge is disjunctive. */
    public boolean isDisjunctive() { return altEdges != null && altEdges.size() > 1; }
    
    /** Returns the list of alt edges, or the empty list if none. */
    public List<Edge> getAltEdges() {
        if (altEdges == null) return Collections.emptyList(); 
        else return altEdges;
    }
    
    /** Initializes the alt edges list with a default capacity, adding this edge. */
    public void initAltEdges() { initAltEdges(3); }
    
    /** Initializes the alt edges list with the given capacity, adding this edge. */
    public void initAltEdges(int capacity) {
        // check uninitialized
        if (altEdges != null) throw new RuntimeException("Alt edges already initialized!");
        altEdges = new ArrayList<Edge>(capacity);
        altEdges.add(this);
    }
    
    
    /** Initializes the edge combos. */
    public void initEdgeCombos() {
        // check representative status
        if (!isRepresentative()) throw new RuntimeException("Not a representative!");
        if (edgeCombos != null) throw new RuntimeException("Edge combos already initialized!");
        edgeCombos = new EdgeCombos();
    }
    
    
    /** Returns a hash code for this edge. (Alternatives are not considered.) */
    public int hashCode() {
        int retval = sign.hashCode() + 31 * bitset.hashCode() + indices.hashCode();
        retval += (int) (31000 * score);
        return retval;
    }
    
    /** 
     * Returns a hash code for this edge based on the surface words, 
     * ignoring the LF and ignoring the score. 
     */
    public int surfaceWordHashCode() {
    	return sign.surfaceWordHashCode(true) + 31 * bitset.hashCode() + indices.hashCode();
    }
    
    /** Returns whether this edge equals the given object. (Alternatives are not considered.) */
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Edge)) return false;
        Edge edge = (Edge) obj;
        return bitset.equals(edge.bitset) && indices.equals(edge.indices) && 
        	completeness == edge.completeness && score == edge.score &&
        	sign.equals(edge.sign);
    }
    
    /** 
     * Returns whether this edge equals the given object based on the surface words, 
     * ignoring the LF and ignoring the score. 
     */
    public boolean surfaceWordEquals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Edge)) return false;
        Edge edge = (Edge) obj;
        return bitset.equals(edge.bitset) && indices.equals(edge.indices) &&
               sign.surfaceWordEquals(edge.sign, true);
    }
    
    
    /**
     * Returns a string for the edge in the format
     * {completeness} [score] orthography :- category {bitset}. 
     */
    public String toString() {
        Preferences prefs = Preferences.userNodeForPackage(TextCCG.class);
        boolean showCompleteness = prefs.getBoolean(SHOW_COMPLETENESS, false);
        boolean showBitset = prefs.getBoolean(SHOW_BITSET, false);
        StringBuffer sbuf = new StringBuffer();
        //sbuf.append(indices + " ");
        if (showCompleteness) { sbuf.append("{" + nf2.format(completeness) + "} "); }
        if (score >= 0.001 || score == 0.0) {
            sbuf.append("[" + nf3.format(score) + "] ");
        }
        else {
            sbuf.append("[" + nfE.format(score) + "] ");
        }
        sbuf.append(sign.toString());
        if (showBitset) { sbuf.append(' ').append(toString(bitset)); }
        return sbuf.toString();
    }
    
    // formats to two decimal places
    private static final NumberFormat nf2 = initNF2();
    private static NumberFormat initNF2() { 
        NumberFormat f = NumberFormat.getInstance();
        f.setMinimumIntegerDigits(1);
        f.setMinimumFractionDigits(2);
        f.setMaximumFractionDigits(2);
        return f;
    }
    
    // formats to three decimal places
    private static final NumberFormat nf3 = initNF3();
    private static NumberFormat initNF3() { 
        NumberFormat f = NumberFormat.getInstance();
        f.setMinimumIntegerDigits(1);
        f.setMinimumFractionDigits(3);
        f.setMaximumFractionDigits(3);
        return f;
    }
    
    // formats to "0.##E0"
    private static final NumberFormat nfE = new DecimalFormat("0.##E0");
    
    /** Formats bitset compactly, with ranges hyphenated. */
    public static String toString(BitSet bitset) {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append('{');
        int j = 0;
        for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(j+1)) {
            if (j != 0) sbuf.append(',');
            j = bitset.nextClearBit(i);
            if (j == i+1) sbuf.append(Integer.toString(i));
            else sbuf.append(i + "-" + (j-1));
        }
        sbuf.append('}');
        return sbuf.toString();
    }
}
