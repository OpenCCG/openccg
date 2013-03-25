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
package opennlp.ccg.unify;

import java.util.Set;

/**
 * A feature structure containing attributes and their associated values.
 *
 * @author      Jason Baldridge
 * @author      Gann Bierner
 * @author      Michael White
 * @version     $Revision: 1.5 $, $Date: 2009/02/16 17:31:38 $
 */
public interface FeatureStructure extends Unifiable, Mutable {

    /**
     * Store a attribute/value pair
     *
     * @param attribute the attribute of the feature
     * @param value the value of the feature
     */
    public void setFeature(String attribute, Object value);


    /**
     * Get the value corresponding to an attribute.
     *
     * @param attribute the attribute of the feature
     * @return the value of the feature
     */
    public Object getValue(String attribute);
    
    /**
     * Checks to see if the feature structure contains a feature with 
     * the given attribute.
     *
     * @param attribute the attribute
     * @return if this structure contains that attribute
     */
    public boolean hasAttribute(String attribute);

    /**
     * Checks to see if an attribute has a particular value in this structure
     *
     * @param attribute the attribute
     * @param value the value
     * @return if this structure contains that attribute/value pair
     */
    public boolean attributeHasValue(String attribute, Object value);


    /**
     * The all attributes in set form
     *
     * @return the set of attributes
     */
    public Set<String> getAttributes();

    
    /**
     * The number of features in this feature structure
     *
     * @return number of features in this feature structure
     */
    public int size();

    
    /**
     * Returns whether or not this feature structure contains any features
     *
     * @return whether this feature structure contains any features
     */
    public boolean isEmpty();

    
    /**
     * Explictly clear the attribute value mappings in this feature structure
     *
     * @param b the empty value
     */
    public void clear();
    

    /**
     * Makes a deep copy of this feature structure.
     *
     * @return a copy of this feature structure
     */
    public FeatureStructure copy();


    /**
     * Computes whether this feature structure contains (is a superset
     * of) another feature structure.
     *
     * @param fs the possibly contained feature structure
     * @return if that structure is a subset of this one 
     */
    public boolean contains(FeatureStructure fs);

    /**
     * Changes this feature structure such that all its features that
     * are in another feature structure are changed to have the values of
     * the other feature structure.  This is destructive.
     *
     * @param f the feature structure to inherit from
     * @return the changed feature structure
     */
    public FeatureStructure inherit(FeatureStructure fs);

    /**
     * Determines if this feature structure is exactly the same as another.  This
     * means that for every feature, the structures have exactly the same value.
     *
     * @param f the other feature structure
     * @return if this structure is the same as the other
     */
    public boolean equals(FeatureStructure fs);

    /** Returns the index. */
    public int getIndex();
    /** Sets the index. */
    public void setIndex(int index);
    /** Returns the inheritsFrom (default unification) index. */
    public int getInheritsFrom();
    
    /**
     * Returns the supertag info for this feature structure.
     */
    public String getSupertagInfo();
    
    /**
     * Returns a TeX formatted feature structure.
     */
    public String toTeX();
}
