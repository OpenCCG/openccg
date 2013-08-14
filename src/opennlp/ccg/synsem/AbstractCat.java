///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-5 Jason Baldridge, Gann Bierner and 
//                      University of Edinburgh (Michael White)
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

import java.io.IOException;
import java.io.Serializable;
import gnu.trove.*;
import org.jdom.*;

import opennlp.ccg.hylo.*;
import opennlp.ccg.unify.*;

/**
 * Abstract category adapter for CCG categories.
 *
 * @author      Gann Bierner
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.22 $, $Date: 2011/05/22 03:40:55 $ 
 */
public abstract class AbstractCat implements Category, Serializable { 

	private static final long serialVersionUID = 1L;

	/** The feature structure, which should only be used with atomic categories. */
    protected FeatureStructure _featStruc;
    
    /** The logical form, which should be used only with the outermost category. */
    protected LF _lf;
    
    /** The hash code, if already computed. */
    private transient int _hashCode = -1;

    /** The hash code for the category without its LF, if already computed. */
    private transient int _hashCodeNoLF = -1;
    
    /** The mapping from vars to ints, if already computed. */
    private transient TObjectIntHashMap _varMap = null;

    /** The supertag, if already computed. */
    protected String _supertag = null;
    
    /** Default constructor. */
    public AbstractCat() {}

    /** Constructor which sets the LF. */
    public AbstractCat(LF lf) { _lf = lf; }

    /** 
     * Constructor which retrieves the LF from the XML element 
     * and flattens it to a conjunction of elementary predications
     * (or a single one). 
     */
    public AbstractCat(Element elt) {
        Element lfElt = elt.getChild("lf");
        if (lfElt != null) {
            _lf = HyloHelper.flattenLF(HyloHelper.getLF(lfElt));
        }
    }

    /**
     * Adds an XML element for the LF, if any, to the given catElt.
     * Uses {@link HyloHelper#toXml(LF)}.
     */
    public void toXml(Element catElt) {
    	if (_lf != null) catElt.addContent(HyloHelper.toXml(_lf));
    }

    // during deserialization, intern computed supertag, and ensure varmap recomputed
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    	in.defaultReadObject();
    	if (_supertag != null) _supertag = _supertag.intern();
    	_varMap = null;
    }
    
    
    /** Gets the feature structure. */
    public FeatureStructure getFeatureStructure() { 
        return _featStruc; 
    }

    /** Sets the feature structure. */
    public void setFeatureStructure(FeatureStructure fs) { 
        _featStruc = fs; 
    }
    
    /** Gets the LF. */
    public LF getLF() { return _lf; }
    
    /** Sets the LF. */
    public void setLF(LF lf) { _lf = lf; }
    

    //-----------------------------------------------------------------
    // methods from Category which should be implemented by subclasses of
    // AbstractCat
    
    public abstract String toString();
    
    /**
     * Returns the supertag for the category.
     */
    public abstract String getSupertag();
    
    /**
     * This will return a TeX formatted representation for a category.
     * If toTeX() is not implemented for this category, the normal
     * toString() method is invoked instead.
     */
    public String toTeX()
    {
        return toString();
    }
    
    public abstract Category copy();
    public abstract Category shallowCopy();
    
    public abstract Object fill (Substitution s) throws UnifyFailure;
    public abstract void unifyCheck (Object u) throws UnifyFailure;
    /** NB: The LF does not participate in unification. */
    public abstract Object unify (Object u, Substitution sub) 
        throws UnifyFailure;

    /**
     * Returns the target category of this category.
     */
    public abstract TargetCat getTarget();
    
    
    //-----------------------------------------------------------------
    // implemented methods from Category
    
    public boolean shallowEquals(Object o) { 
        return equals(o); 
    }

    public void deepMap(ModFcn mf) { 
        if (_lf != null) _lf.deepMap(mf);
        mf.modify(this);
    }

    public void forall(CategoryFcn f) { 
        f.forall(this); 
    }
    
    public boolean occurs(Variable v) {
        if (_lf == null) return false;
        return _lf.occurs(v); 
    }


    // methods to support printing of Categories
    public String prettyPrint() { return prettyPrint(""); }
    protected String prettyPrint(String pad) { return pad+toString(); }

    protected int prettyLength(String s) {
        int max=0, cur=0;
        for(int i=0; i<s.length(); i++)
            if(s.charAt(i) == '\n') {
                max = Math.max(cur, max);
                cur = 0;
            } else cur++;
        return Math.max(max, cur);
    }

    
    /** 
     * Returns a hash code for this category. 
     * The hash code handles equivalence up to variable names 
     * as long as features and predicates are in the same order.
     * The implementation calls hashCodeNoLF(varMap) and hashCode(varMap) 
     * on the LF.
     */
    public int hashCode() {
    	// NB: caching of the hash code has been turned off to avoid problems with stale values;
    	//     in principle, a check for staleness could be added
        //if (_varMap != null && _hashCode != -1) { return _hashCode; }
        _varMap = new TObjectIntHashMap();
        _hashCodeNoLF = hashCodeNoLF(_varMap);
        _hashCode = _hashCodeNoLF;
        if (_lf != null) { _hashCode += _lf.hashCode(_varMap); }
        return _hashCode;
    }
    
    /** 
     * Returns a hash code for this category ignoring the LF.
     * The hash code handles equivalence up to variable names 
     * as long as features and predicates are in the same order.
     * The implementation calls hashCodeNoLF(varMap). 
     */
    public int hashCodeNoLF() {
    	// NB: caching of the hash code has been turned off to avoid problems with stale values;
    	//     in principle, a check for staleness could be added
        //if (_varMap != null && _hashCodeNoLF != -1) { return _hashCodeNoLF; }
        _varMap = new TObjectIntHashMap();
        _hashCodeNoLF = hashCodeNoLF(_varMap);
        return _hashCodeNoLF;
    }
    
    /**
     * Returns a hash code for this category ignoring the LF, 
     * using the given map from vars to ints, 
     * to allow for equivalence up to variable names.
     */
    public abstract int hashCodeNoLF(TObjectIntHashMap varMap);

    /** 
     * Returns whether this category equals the given object. 
     * Equivalence up to variable names is handled  
     * as long as features and predicates are in the same order.
     * The implementation calls equalsNoLF(obj, varMap, varMap2).
     */
    public boolean equals(Object obj) { return equals(obj, true); }
    
    /** 
     * Returns whether this category equals the given object, 
     * ignoring the LFs (if any).
     */
    public boolean equalsNoLF(Object obj) { return equals(obj, false); }

    // checks equality, with a flag whether to check the LFs
    private boolean equals(Object obj, boolean checkLF) {
        // NB: The following line can be uncommented, in order to 
        //     turn off category equality checking; it hasn't been 
        //     put on a preferences switch, due to efficiency concerns.
        // if (true) return super.equals(obj);
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) { return false; }
        AbstractCat ac = (AbstractCat) obj;
        // ensure var maps in place
        if (checkLF) {
        	hashCode(); ac.hashCode();
        } else {
        	hashCodeNoLF(); ac.hashCodeNoLF();
        }
        // check equality wrt mappings
        if (checkLF) {
            if (_lf != null && ac._lf == null) { return false; }
            if (_lf == null && ac._lf != null) { return false; }
        }
        if (!equalsNoLF(obj, _varMap, ac._varMap)) return false;
        if (checkLF && _lf != null && !_lf.equals(ac._lf, _varMap, ac._varMap)) { return false; }
        return true;
    }

    /**
     * Returns whether this category equals the given object  
     * up to variable names, using the given maps from vars to ints, 
     * ignoring the LFs (if any).
     */
    public abstract boolean equalsNoLF(Object obj, TObjectIntHashMap varMap, TObjectIntHashMap varMap2);


    /**
     * Returns the nominal which is the value of the index feature on the 
     * target cat, or null if none.
     */
    public Nominal getIndexNominal() {
        Category target = getTarget();
        FeatureStructure fs = target.getFeatureStructure();
        if (fs != null) { 
            Object index = fs.getValue("index");
            if (index instanceof Nominal) return (Nominal) index;
        }
        return null;
    } 

    /**
     * Returns whether this category is a fragment category (false by default).
     */
    public boolean isFragment() { return false; }
}
