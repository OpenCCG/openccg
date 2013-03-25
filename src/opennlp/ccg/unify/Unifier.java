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
package opennlp.ccg.unify;

/**
 * A unification utility that abstracts a few basic issues such
 * Variables and not needed to pass a substitution object explictly.
 *
 * @author      Jason Baldridge
 * @version     $Revision: 1.2 $, $Date: 2004/11/11 17:50:13 $
 **/
public class Unifier {
   
    /**
     * Uses a <code>SelfCondensingSub</code> underlyingly so that it
     * is not necessary to pass a substitution object explictly.
     *
     * @param u1 the first of two Unifiables to unify
     * @param u2 the second of two Unifiables to unify
     * @return the result of unifying u1 and u2
     **/
    public static final Object unify (Object u1, Object u2) throws UnifyFailure {
        Substitution sub = new SelfCondensingSub();
        Object result =  unify(u1, u2, sub);
        if (result instanceof Unifiable) {
            result = ((Unifiable)result).fill(sub);
        }
        return result;
    }

    /**
     * Method which handles ordering to make sure that the Unifiable
     * unify() method is called on the Variable if either of the
     * arguments is a Variable.  This way, under a unification scheme
     * for a set of classes, you don't have to have each Unifiable
     * check to see if the thing it is trying to be unified with is a
     * Variable.
     *
     * @param u1 the first of two Unifiables to unify
     * @param u2 the second of two Unifiables to unify
     * @param sub the substitution object holding global unification
     * information
     * @return the result of unifying u1 and u2
     **/
    public static final Object unify (Object u1, Object u2, Substitution sub) throws UnifyFailure {
    
        // !!!!!!!!!!!!!!!!!!!!!!!! CAUTION !!!!!!!!!!!!!!!!!!!!!!!!
        // the order of this if-else statement is important, so be
        // careful before you change it!
        // !!!!!!!!!!!!!!!!!!!!!!!! CAUTION !!!!!!!!!!!!!!!!!!!!!!!!
        if (u2 instanceof Variable) {
            return ((Variable)u2).unify(u1, sub);
        } else if (u1 instanceof Unifiable) {
            return ((Unifiable)u1).unify(u2, sub);
        } else if (u2 instanceof Unifiable) {
            return ((Unifiable)u2).unify(u1, sub);
        } else if (u1.equals(u2)) {
            return u1;
        } else {
            throw new UnifyFailure();
        }
    }
}
