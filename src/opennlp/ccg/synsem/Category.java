///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002-5 Jason Baldridge, Gann Bierner and 
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

import opennlp.ccg.unify.*;
import opennlp.ccg.hylo.*;
import gnu.trove.*;
import org.jdom.Element;

/**
 * A CCG category.
 *
 * @author      Jason Baldridge
 * @author      Gann Bierner
 * @author      Michael White
 * @version     $Revision: 1.11 $, $Date: 2011/05/22 03:40:55 $
 */
public interface Category extends Unifiable, Mutable, java.io.Serializable {

    /**
     * Accessor function for the feature structure associated with this category.
     *
     * @return the feature structure for this cateogory
     */    
    public FeatureStructure getFeatureStructure();
    
    /**
     * Gives this category a new feature structure.
     *
     * @param fs the new feature structure
     */    
    public void setFeatureStructure(FeatureStructure fs);

    /** Gets the LF. */
    public LF getLF();
    
    /** Sets the LF. */
    public void setLF(LF lf);
    

    /**
     * Determines if this category is equal to another on the top level.
     * It does not check sub categories.
     *
     * @param o object to check for equality
     * @return whether or not this is shallowly equal to object
     */
    public boolean shallowEquals(Object o);

    /**
     * Deep copies this category.
     *
     * @return a deep copy of this category
     */
    public Category copy();

    /** Shallow copies this category. */
    public Category shallowCopy();
    

    /**
     * Iterates through this Category applying a function to this category
     * and every subcategory.
     *
     * @param f a function to be applied
     */    
    public void forall(CategoryFcn f); //to ls

    
    /** 
     * Returns a hash code for this category. 
     * The hash code handles equivalence up to variable names 
     * as long as features and predicates are in the same order.
     */
    public int hashCode();
    
    /** 
     * Returns a hash code for this category ignoring the LF.
     */
    public int hashCodeNoLF();
    
    /**
     * Returns a hash code for this category ignoring the LF, 
     * using the given map from vars to ints, 
     * to allow for equivalence up to variable names.
     */
    public int hashCodeNoLF(TObjectIntHashMap varMap);

    
    /** 
     * Returns whether this category equals the given object. 
     * Equivalence up to variable names is handled  
     * as long as features and predicates are in the same order.
     */
    public boolean equals(Object obj);
    
    /** 
     * Returns whether this category equals the given object, 
     * ignoring the LFs (if any).
     */
    public boolean equalsNoLF(Object obj);
    
    /**
     * Returns whether this category equals the given object  
     * up to variable names, using the given maps from vars to ints, 
     * ignoring the LFs (if any).
     */
    public boolean equalsNoLF(Object obj, TObjectIntHashMap varMap, TObjectIntHashMap varMap2);
    
    
    /**
     * Returns the target category of this category.
     */
    public TargetCat getTarget();
    
    /**
     * Returns the nominal which is the value of the index feature on the 
     * target cat, or null if none.
     */
    public Nominal getIndexNominal(); 
    
    /**
     * Returns the interned supertag for the category.
     */
    public String getSupertag();
    
    /**
     * Returns whether this category is a fragment category.
     */
    public boolean isFragment();
    
    /**
     * Returns a TeX-formatted string representation for the category.
     */
    public String toTeX();
    
    /**
     * Returns an XML element representing the category.
     */
    public Element toXml();
}
