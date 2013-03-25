///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002-3 Jason Baldridge and University of Edinburgh (Michael White)
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

import java.io.Serializable;
import org.jdom.*;
import gnu.trove.*;

import opennlp.ccg.synsem.*;
import opennlp.ccg.unify.*;

/**
 * A parent class to implement reasonable default behavior for classes
 * representing data structures for hybrid logic.
 *
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.13 $, $Date: 2009/12/21 03:27:19 $
 **/
public abstract class HyloFormula implements LF, Serializable {
    
	private static final long serialVersionUID = 1L;
	
	/**
     * The LF chunks to which this LF belongs.
     */
    protected TIntArrayList chunks = null;
    
    /**
     * Sets the LF chunks to which this LF belongs.
     * LF chunks are used during realization to ensure 
     * that certain edges are semantically complete 
     * before combination is attempted with edges 
     * with semantics outside the chunk.
     * The chunks are numbered starting with 0, 
     * and null represents no chunks.
     */
    public void setChunks(TIntArrayList chunks) { this.chunks = chunks; }
    
    /**
     * Gets the LF chunks to which this LF belongs.
     */
    public TIntArrayList getChunks() { return chunks; }
    
    
    /** Returns null as the default type. */
    public SimpleType getType() { return null; }

    
    /**
     * Returns a copy of this LF.
     * (LF chunks are not copied.)
     */
    public abstract LF copy();
    
    /**
     * Applies a ModFcn to this LF and then applies it to all fields
     * which are themselves Mutables.
     *
     * @param mf a function to be applied
     */
    public void deepMap(ModFcn mf) {
        mf.modify(this);
    }

    
    /**
     * Unify this Unfiable with another Object. 
     * This default implementation will reverse the direction of unification 
     * for a variable, otherwise it fails.
     * <b>NB:</b> The implementation of unification in the hylo package is not 
     * complete; a particular limitation is that no attempt is made to unify lists of terms 
     * connected by an Op instance.
     *
     * @param o object to unify with
     * @param s Substitution containing the variable resolutions
     * @exception UnifyFailure if this Unifiable cannot be unified with 
     *            the Object
     * @return an object which represents the unification of 
     *         this Unifiable with the Object
     */
    public Object unify(Object u, Substitution s) throws UnifyFailure {
        if (u instanceof Variable) return ((Unifiable)u).unify(this, s);
        else throw new UnifyFailure(this.toString(), u.toString());
    }

    
    /**
     * Check if this Unifiable can unify with another Object.  This
     * should be implemented as a quick check to allow users of the
     * Unifiable to scan a group of Unifications to rapidly see if the
     * entire group is at least possible before descending into each
     * one with a full unification procedure.  Thus, if a call to this
     * method does not result in a UnifyFailure exception being
     * thrown, it doesn't mean that the Object can definitely be
     * unified with this Unifiable -- what is important is that when a
     * call to this method throws a UnifyFailure exception, it permits
     * one to avoid calling the unify() method on other Unifiables in
     * a group because the quick check failed on this one.
     *
     * @param o object to check for unifiability
     * @exception UnifyFailure if this Unifiable cannot be unified with 
     *            the Object
     * @return the Object o, unmodified 
     **/
    public void unifyCheck(Object u) throws UnifyFailure {}


    /**
     * Replaces any variables in this Unifiable with the values found
     * for them in the Substitution argument.
     *
     * @param s Substitution containing the variable resolutions
     * @return a copy of this Unifiable with all variables from the
     *         Substitution replaced by their values.  
     */
    public Object fill(Substitution s) throws UnifyFailure {
        return this;
    }
       
    /**
     * Returns a hash code using the given map from vars to ints.
     */
    public abstract int hashCode(TObjectIntHashMap varMap);

    /**
     * Returns whether this LF equals the given object  
     * up to variable names, using the given maps from vars to ints.
     */
    public abstract boolean equals(Object obj, TObjectIntHashMap varMap, TObjectIntHashMap varMap2);
    
    /**
     * Returns an XML representation of this LF.
     */
    public abstract Element toXml();
    
    /**
     * Returns a pretty-printed string of this LF, with the given indent.
     */
    public abstract String prettyPrint(String indent);
}
