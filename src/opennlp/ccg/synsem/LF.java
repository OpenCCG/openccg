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

package opennlp.ccg.synsem;

import opennlp.ccg.unify.*;
import org.jdom.*;
import gnu.trove.*;

/**
 * An interface for objects which represent Logical Forms.
 *
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.10 $, $Date: 2005/11/01 22:35:35 $
 */
public interface LF extends Unifiable, Mutable {

    /**
     * Sets the LF chunks to which this LF belongs.
     * LF chunks are used during realization to ensure 
     * that certain edges are semantically complete 
     * before combination is attempted with edges 
     * with semantics outside the chunk.
     * The chunks are numbered starting with 0, 
     * and null represents no chunks.
     */
    public void setChunks(TIntArrayList chunks);
    
    /**
     * Gets the LF chunks to which this LF belongs.
     */
    public TIntArrayList getChunks();
    

    /** Returns the simple type of this LF, or null if none. */
    public SimpleType getType();
    
    
    /**
     * Returns a copy of this LF.
     * (LF chunks are not copied.)
     */
    public LF copy();

    /**
     * Returns a hash code using the given map from vars to ints.
     */
    public int hashCode(TObjectIntHashMap varMap);

    /**
     * Returns whether this LF equals the given object  
     * up to variable names, using the given maps from vars to ints.
     */
    public boolean equals(Object obj, TObjectIntHashMap varMap, TObjectIntHashMap varMap2);
    
    /**
     * Returns an XML representation of this LF.
     */
    public Element toXml();
    
    /**
     * Returns a pretty-printed string of this LF, with the given indent.
     */
    public String prettyPrint(String indent);
}
