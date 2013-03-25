///////////////////////////////////////////////////////////////////////////////
//// Copyright (C) 2003-9 Gunes Erkan and Michael White
//// 
//// This library is free software; you can redistribute it and/or
//// modify it under the terms of the GNU Lesser General Public
//// License as published by the Free Software Foundation; either
//// version 2.1 of the License, or (at your option) any later version.
//// 
//// This library is distributed in the hope that it will be useful,
//// but WITHOUT ANY WARRANTY; without even the implied warranty of
//// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//// GNU Lesser General Public License for more details.
//// 
//// You should have received a copy of the GNU Lesser General Public
//// License along with this program; if not, write to the Free Software
//// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
////////////////////////////////////////////////////////////////////////////////

package opennlp.ccg.unify;

import opennlp.ccg.grammar.*;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;

/**
 * A simple type for feature values in CCG categories.
 * 
 * Note that during deserialization, the type is resolved using the current grammar.
 * 
 * @author      Gunes Erkan
 * @author 		Michael White
 * @version     $Revision: 1.8 $, $Date: 2009/07/17 04:23:30 $
 */
public class SimpleType implements Unifiable, Serializable {
    
	private static final long serialVersionUID = 7028285176993549672L;
	
	private int index;
    private String name;
    private BitSet bitset;
    private BitSet tempBitset = new BitSet();
    private transient Types types;

    public SimpleType(int i, String n, BitSet bs, Types t) {
        index = i;
        name = n;
        bitset = bs;
        types = t;
    }
	
    public int getIndex() { return index; }

    public BitSet getBitSet() { return bitset; }

    public String getName() { return name; }

    public String toString() { return name; }
    
    public void unifyCheck(Object u) throws UnifyFailure {
        if (!(u instanceof SimpleType)) {
            throw new UnifyFailure();
        }
    }

    public Object unify(Object u, Substitution sub) throws UnifyFailure {
        if (!(u instanceof SimpleType)) {
            throw new UnifyFailure();
        }
        if (this == u) return this;
        SimpleType st2 = (SimpleType) u;
        tempBitset.clear();
        tempBitset.or(bitset);
        tempBitset.and(st2.getBitSet());
        int resultTypeIndex = tempBitset.nextSetBit(0);
        if (resultTypeIndex == -1) {
        	throw new UnifyFailure();
        }
        return types.getIndexMap().get(resultTypeIndex);
    }

    public Object fill(Substitution s) throws UnifyFailure {
        return this;
    }
    
    public boolean occurs(Variable v) { return false; }
    
    public int hashCode() { return index; } 
    
    public boolean equals(Object o) {
        if (!(o instanceof SimpleType)) return false;
        if (index == ((SimpleType)o).getIndex()) return true;
        else return false;
    }
    
    /** Returns canonical version of deserialized type based on current grammar. */
    public Object readResolve() throws ObjectStreamException {
    	return Grammar.theGrammar.types.getSimpleType(name);
    }
}
