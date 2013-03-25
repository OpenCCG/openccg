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

import java.util.*;

/**
 * Specifies how variable are to be replaced to make two objects unify.
 * 
 * @author Gann Bierner & Jason Baldridge
 * @version $Revision: 1.2 $, $Date: 2005/10/20 17:30:30 $
 */
public interface Substitution {

	/**
	 * Request the Substitution to identify a variable with an object.
	 * 
	 * @param var
	 *            the variable whose value has been determined
	 * @param o
	 *            the Object identified with the variable
	 * @return the Object identified with the variable, which has potentially
	 *         undergone further unifications as a result of making the
	 *         substitution
	 * @exception throws
	 *                UnifyFailure if the Object cannot be unified with a
	 *                previous value substituted for the Variable.
	 */
	public Object makeSubstitution(Variable var, Object u) throws UnifyFailure;

	/**
	 * Try to get the value of a variable from this Substitution. Should return
	 * null if the variable is unknown to the Substitution.
	 * 
	 * @param var
	 *            the variable whose value after unification is desired
	 * @return the Object which this variable has been unified with
	 */
	public Object getValue(Variable var);

	public Iterator<Variable> varIterator();

}
