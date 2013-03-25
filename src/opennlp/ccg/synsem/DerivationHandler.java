///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2010 Michael White
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

import opennlp.ccg.parse.DerivationHistory;

/** 
 * A class to simplify implementing a recursive procedure on a derivation.
 * Recursion is handled within the implementation of the abstract methods 
 * by calls to <code>handleDerivation</code>, so that order of traversal 
 * can be flexibly specified.  Results may be optionally cached.
 * Note that the top step is only used with complete derivations.
 * 
 * @author 	Michael White
 * @version	$Revision: 1.1 $, $Date: 2010/02/21 16:44:59 $
 */ 
abstract public class DerivationHandler<T> {
	
	/** Top step. */
	abstract public T topStep(Sign sign);
	
	/** Lexical step. */
	abstract public T lexStep(Sign sign);

	/** Unary step. */
	abstract public T unaryStep(Sign sign, Sign headChild);
	
	/** Binary step. */
	abstract public T binaryStep(Sign sign, boolean left, Sign headChild, Sign siblingChild);
	
	/** Checks for cached value, returning null if none. Defaults to null. */
	public T checkCache(Sign sign) { return null; }

	/** Caches the result.  Default no-op. */
	public void cache(Sign sign, T result) {}
	
	/** Handles a complete derivation, invoking the top step. */
	public T handleCompleteDerivation(Sign sign) {
		return topStep(sign);
	}
	
	/** Handles a sub-derivation, checking and updating cache. */
	public T handleDerivation(Sign sign) {
		// check cache
		T retval = checkCache(sign); 
		if (retval != null) return retval;
		// lexical case
		if (sign.isLexical()) {
			retval = lexStep(sign);
			cache(sign, retval); 
			return retval; 
		}
		// recursive case
		DerivationHistory dh = sign.getDerivationHistory();
		Sign[] inputs = dh.getInputs();
		// unary case
		if (inputs.length == 1) {
			Sign headChild = inputs[0];
			retval = unaryStep(sign, headChild);
			cache(sign, retval); 
			return retval; 
		}
		// binary case
		else {
			boolean left;
			Sign headChild, siblingChild;
			if (sign.getLexHead() == inputs[0].getLexHead()) {
				left = true; headChild = inputs[0]; siblingChild = inputs[1];
			}
			else {
				left = false; headChild = inputs[1]; siblingChild = inputs[0];
			}
			retval = binaryStep(sign, left, headChild, siblingChild);
			cache(sign, retval); 
			return retval; 
		}
	}
}
