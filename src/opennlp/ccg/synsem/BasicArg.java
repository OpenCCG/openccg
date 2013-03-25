///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002-5 Jason Baldridge and University of Edinburgh (Michael White)
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

import java.io.Serializable;
import gnu.trove.*;

import opennlp.ccg.unify.*;

/**
 * A basic argument that contains a slash and a category.
 * 
 * @author Jason Baldridge
 * @author Michael White
 * @version $Revision: 1.9 $, $Date: 2009/11/28 03:39:27 $
 */
public final class BasicArg implements Arg, Serializable {

	private static final long serialVersionUID = -4244825501682166456L;

	private final Slash _slash;

	private final Category _cat;

	public BasicArg(Slash s, Category c) {
		_slash = s;
		_cat = c;
	}

	public Arg copy() {
		return new BasicArg(_slash.copy(), _cat.copy());
	}

	public Slash getSlash() {
		return _slash;
	}

	public void setSlashModifier(boolean modifier) { _slash.setModifier(modifier); }
	
    public void setSlashHarmonicCompositionResult(boolean harmonicResult) { _slash.setHarmonicCompositionResult(harmonicResult); }

    public Category getCat() {
		return _cat;
	}

	public boolean occurs(Variable v) {
		return _cat.occurs(v);
	}

	public Object fill(Substitution sub) throws UnifyFailure {
		return new BasicArg((Slash) _slash.fill(sub), (Category) _cat.fill(sub));
	}

	public void forall(CategoryFcn fcn) {
		_cat.forall(fcn);
	}

	public void unifySlash(Slash s) throws UnifyFailure {
		_slash.unifyCheck(s);
	}

	public void unifyCheck(Object u) throws UnifyFailure {
	}

	public Object unify(Object u, Substitution sub) throws UnifyFailure {
		if (u instanceof BasicArg) {
			return new BasicArg((Slash) _slash
					.unify(((BasicArg) u)._slash, sub), (Category) _cat.unify(
					((BasicArg) u)._cat, sub));
		} else {
			throw new UnifyFailure();
		}

	}

	public void deepMap(ModFcn mf) {
		_slash.deepMap(mf);
		_cat.deepMap(mf);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(_slash.toString());
		if (_cat instanceof ComplexCat) {
			sb.append('(').append(_cat).append(')');
		} else {
			sb.append(_cat);
		}
		return sb.toString();
	}

	/**
	 * Returns the supertag for this arg.
	 */
	public String getSupertag() {
		StringBuffer sb = new StringBuffer();
		sb.append(_slash.getSupertag());
		if (_cat instanceof ComplexCat) {
			sb.append('(').append(_cat.getSupertag()).append(')');
		} else {
			sb.append(_cat.getSupertag());
		}
		return sb.toString();
	}

	/**
	 * Returns a TeX-formatted string representation for this arg.
	 */
	public String toTeX() {
		StringBuffer sb = new StringBuffer();
		sb.append(_slash.toTeX());
		if (_cat instanceof ComplexCat) {
			sb.append('(').append(_cat.toTeX()).append(')');
		} else {
			sb.append(_cat.toTeX());
		}
		return sb.toString();
	}

	/**
	 * Returns a hash code for this, using the given map from vars to ints.
	 */
	public int hashCode(TObjectIntHashMap varMap) {
		return _slash.hashCode(varMap) + _cat.hashCodeNoLF(varMap);
	}

	/**
	 * Returns whether this arg equals the given object up to variable names,
	 * using the given maps from vars to ints.
	 */
	public boolean equals(Object obj, TObjectIntHashMap varMap,
			TObjectIntHashMap varMap2) {
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		BasicArg ba = (BasicArg) obj;
		return _slash.equals(ba._slash, varMap, varMap2)
				&& _cat.equalsNoLF(ba._cat, varMap, varMap2);
	}
}
