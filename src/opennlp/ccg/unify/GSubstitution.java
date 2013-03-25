///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003 Jason Baldridge, University of Edinburgh (Michael White) 
//                    and Gunes Erkan
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

package opennlp.ccg.unify;

import gnu.trove.*;
import java.util.*;

/**
 * Implementation of Substitution interface which ensures that all
 * the categories it contains are updated as new substitutions are
 * made.
 *
 * @author      Jason Baldridge
 * @author      Michael White
 * @author      Gunes Erkan
 * @version     $Revision: 1.13 $, $Date: 2009/12/21 03:27:19 $ 
*/
public class GSubstitution extends THashMap implements Substitution {

	private static final long serialVersionUID = 1L;

	private TIntObjectHashMap _indexedObjects = new TIntObjectHashMap();
    private TIntIntHashMap _newFeatStrucIndexes = new TIntIntHashMap();

    /**
     * Request the Substitution to identify a variable with an
     * object. Automagically condenses the Substitution so that all
     * other values in this Substitution get the new value for the
     * variable if they contain it.
     *
     * @param var the variable whose value has been determined
     * @param o the Object identified with the variable
     * @return the Object identified with the variable, which has
     * potentially undergone further unifications as a result of
     * making the substitution
     * @exception throws UnifyFailure if the Object cannot be unified
     * with a previous value substituted for the Variable.
     */
    @SuppressWarnings("unchecked")
	public Object makeSubstitution(Variable var, Object u) throws UnifyFailure {
        Object val1 = getValue(var);
        if (u instanceof Variable) {
            Variable var2 = (Variable)u;
            Object val2 = getValue(var2);
            // check if var -> u already
            if (val1 != null && val1.equals(u)) return u;
            // check if u -> var already
            if (val2 != null && val2.equals(var)) return var;
            // otherwise continue 
            if (val1 != null) {
                if (val1 instanceof Unifiable && ((Unifiable)val1).occurs(var2)) {
                    throw new UnifyFailure();
                }
                if (val2 != null) {
                    u = Unifier.unify(var, val2, this);
                } else {
                    u = makeSubstitution(var2, val1);
                }
            } else if (val2 != null) {
                if (val2 instanceof Unifiable && ((Unifiable)val2).occurs(var)) {
                    throw new UnifyFailure();   
                }
                makeSubstitution(var, val2);
            } 
        } else if (val1 != null) {
            u = Unifier.unify(val1, u, this);
        }
        put(var, u);
        for (Iterator<Variable> i=keySet().iterator(); i.hasNext();) {
            Variable v = (Variable)i.next();
            Object res = getValue(v);
            if (res instanceof Unifiable) {
                res = ((Unifiable)res).fill(this);
            }
            put(v, res);
        }
        if (u instanceof Unifiable) {
            u = ((Unifiable)u).fill(this);
        }
        return u;
    }

    /**
     * Try to get the value of a variable from this Substitution.
     * Returns null if the variable is unknown to the Substitution.
     *
     * @param var the variable whose value after unification is desired
     * @return the Object which this variable has been unified with 
     */
    public Object getValue(Variable var) {
        Object val = get(var);
        if (null != val) {
            if (val instanceof Variable) {
                Object deepVal = getValue((Variable)val);
                if (null != deepVal) {
                    val = deepVal;
                }
            }
        }
        return val;
    }

    @SuppressWarnings("unchecked")
	public Iterator<Variable> varIterator() {
        return keySet().iterator();
    }

    public int makeNewIndex(int fs1Index, int fs2Index) {
        int index = UnifyControl.getUniqueFeatureStructureIndex();
        int fs1IndexUpdated = getUpdatedIndex(fs1Index);
        int fs2IndexUpdated = getUpdatedIndex(fs2Index);
        addReindex(fs1IndexUpdated, index);
        addReindex(fs2IndexUpdated, index);
        return index;
    }

    public void addReindex(int oldIndex, int newIndex) {
        // avoid creating a pointer cycle
        if (oldIndex == newIndex) return;
        if (_newFeatStrucIndexes.containsKey(newIndex)) {
            throw new RuntimeException(
                "Whoops!  Index map already contains newIndex: " + newIndex + "\n" + this
            );
        }
        _newFeatStrucIndexes.put(oldIndex, newIndex);
    }

    public int getUpdatedIndex(int oldIndex) {
        if (!_newFeatStrucIndexes.containsKey(oldIndex)) return oldIndex;
        return getUpdatedIndex(_newFeatStrucIndexes.get(oldIndex));
    }
    
    public void addIndexedObject(int index, Object o) {
        _indexedObjects.put(index, o);
    }
    
    public Object getIndexedObject(int index) {
        return _indexedObjects.get(getUpdatedIndex(index));
    }

    public void condense() throws UnifyFailure {
        int[] keys = _indexedObjects.keys();
        for (int i=0; i < keys.length; i++) {
            Object obj = _indexedObjects.get(keys[i]);
            if (obj instanceof Unifiable) {
                Object filled = ((Unifiable)obj).fill(this);
                _indexedObjects.put(keys[i], filled);
            }
        }
        // drop old indexed objects
        for (int i = 0; i < keys.length; i++) {
            if (_newFeatStrucIndexes.containsKey(keys[i])) {
                _indexedObjects.remove(keys[i]);
            }
        }
    }

    @SuppressWarnings("unchecked")
	public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("vars: \t");
        for (Iterator<Variable> keys=keySet().iterator(); keys.hasNext();) {
            Object key = keys.next();
            sb.append(key).append('=').append(get(key)).append('\t');
        }
        sb.append('\n');
        sb.append("indexes: \t");
        int indexKeys[] = _newFeatStrucIndexes.keys();
        for (int i = 0; i < indexKeys.length; i++) {
            sb.append(indexKeys[i] + "->" + _newFeatStrucIndexes.get(indexKeys[i]) + "\t");
        }
        return sb.toString();
    }
}
