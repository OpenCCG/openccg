///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010 Dennis N. Mehay
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed inp the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////


package opennlp.ccg.parse.tagger.sequencescoring;

import java.util.List;

/**
 * A node in a forward-backward lattice.
 * 
 * Holds the current label, the forward score, backward score, a list of Doubles representing
 * this node's contribution to the scores of the following nodes in the next
 * step of the lattice, a list of Strings representing the Markov history of the
 * optimal sequence leading up to this node, and, finally, holds a ranked array of
 * backpointers to the n-best optimal predecessor nodes.
 * 
 * @author Dennis N. Mehay
 */
public class FBNode {    
    /** How far back of a Markov history window do we have? */
    public int markovHistSize;    
    /** The label at this node. */
    public String label;
    /** The (normalised) sum of the log-probabilites of all paths leading to this node. */
    public double forwardScore = 0.0;
    /** The (normalised) sum of the log-probabilites of all paths starting at this node. */
    public double backwardScore = 0.0;
    /** 
     * How does this node contribute to each of the nodes in the next time step in
     * the lattice?
     */    
    public List<Double> forwardContributions;    
    /** The list of the optimal Markov history. */
    public List<String> markovHist;
    /** 
     * A list of backpointers to the nodes in the previous time step (ranked in order
     * of how likely the sequence including them leading to this node is).
    */
    public List<Integer> backpointers;   
    
    /** 
     * Empty constructor.  Default Markov history of length 2.  
     * All other values are set directly in the fields as they are calculated. 
     */
    public FBNode() { this(2); }
    
    /** Constructor that only specifies Markov history size. */
    public FBNode(int markovHistSize) {
        this.markovHistSize = markovHistSize;
    }
}
