///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002 Jason Baldridge and Gann Bierner
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

/**
 * A set of functions that can be applied to Categories.  This is a way of
 * getting around the fact that Java doesn't have 1st class functions.
 * Thus, if a method is needed that takes a function that works on Categories,
 * pass one of these instead.
 *
 * @author      Gann Bierner
 * @version     $Revision: 1.1.1.1 $, $Date: 2003/02/28 18:02:12 $
 */
public interface CategoryFcn {
    /**
     * Converts a category to a different category
     *
     * @param c the category to change
     * @return the transformed category
     */
    public Category fcn(Category c);
    
    /**
     * Converts a category to a different category with some additional
     * information about its context.
     *
     * @param a The logical form in which the category appears
     * @param c The category to convert
     * @param i The position of the category in the logical form
     * @return the transformed category
     */
    public void fcn(Category a, Category c, int i);
    
    /**
     * Performs some destructive operation given a category
     *
     * @param c The category used for whatever purpose
     */
    public void forall(Category c);
}
