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

import opennlp.ccg.unify.*;
import org.jdom.*;
import java.io.Serializable;
import java.util.*;
import gnu.trove.*;

/**
 * A category which contains an unordered set of categories.
 * 
 * @author Jason Baldridge
 * @author Michael White
 * @version $Revision: 1.11 $, $Date: 2009/12/21 02:15:44 $
 */
public final class SetArg implements Arg, Serializable {

	private static final long serialVersionUID = -7067480310511294657L;
	
	private ArgStack _args;

	@SuppressWarnings("unchecked")
	public SetArg(Element el) {
		List<Element> info = el.getChildren();
		List<Arg> args = new ArrayList<Arg>();
		for (Iterator<Element> infoIt = info.iterator(); infoIt.hasNext();) {
			Slash s = new Slash(infoIt.next());
			Category c = CatReader.getCat(infoIt.next());
			args.add(new BasicArg(s, c));
		}
		Arg[] list = new Arg[args.size()];
		args.toArray(list);
		_args = new ArgStack(list);
	}

	public SetArg(Arg[] args) {
		_args = new ArgStack(args);
	}

	public SetArg(ArgStack args) {
		_args = args;
	}

	public Element toXml() {
		Element retval = new Element("setarg");
		for (Arg arg : _args._list) {
			if (arg instanceof BasicArg) { // only supporting basic args per xml construction
				BasicArg barg = (BasicArg) arg;
				retval.addContent(barg.getSlash().toXml());
				retval.addContent(barg.getCat().toXml());
			}
		}
		return retval;
	}
	
	public Arg copy() {
		return new SetArg(_args.copy());
	}

	public void add(ArgStack as) {
		_args.add(as);
	}

	public void forall(CategoryFcn fcn) {
		_args.forall(fcn);
	}

	public Arg copyWithout(int pos) {
		if (_args.size() == 2) {
			if (pos == 0) {
				return _args.get(1);
			} else {
				return _args.get(0);
			}
		} else {
			return new SetArg(_args.copyWithout(pos));
		}
	}

	public int size() {
		return _args.size();
	}

	public BasicArg get(int pos) {
		return (BasicArg) _args.get(pos);
	}

	public Category getCat(int pos) {
		return ((BasicArg) _args.get(pos)).getCat();
	}

	public int indexOf(BasicArg a) {
		int index = -1;
		for (int i = 0; i < _args.size() && index < 0; i++) {
			try {
				a.unifySlash(((BasicArg) _args.get(i)).getSlash());
				GUnifier.unify(getCat(i), a.getCat());
				index = i;
			} catch (UnifyFailure uf) {
			}
		}
		// if (index<0) {
		// throw new UnifyFailure();
		// } else {
		// return index;
		// }
		return index;
	}

	public int indexOf(Category cat) {
		int index = -1;
		for (int i = 0; i < _args.size() && index < 0; i++) {
			try {
				GUnifier.unify(getCat(i), cat);
				index = i;
			} catch (UnifyFailure uf) {
			}
		}
		return index;
		// if (index<0) {
		// throw new UnifyFailure();
		// } else {
		// return index;
		// }
	}

	public void setSlashModifier(boolean modifier) { 
		for (int i = 0; i < _args.size(); i++) {
			BasicArg arg = get(i);
			arg.setSlashModifier(modifier);
		}
	}
	
    public void setSlashHarmonicCompositionResult(boolean harmonicResult) { 
		for (int i = 0; i < _args.size(); i++) {
			BasicArg arg = get(i);
			arg.setSlashHarmonicCompositionResult(harmonicResult);
		}
	}
    
	public boolean containsContrarySlash() {
		for (int i = 0; i < _args.size(); i++) {
			if (!((BasicArg) _args.get(i)).getSlash().sameDirAsModality()) {
				return true;
			}
		}
		return false;
	}

	public void unifySlash(Slash s) throws UnifyFailure {
		for (int i = 0; i < _args.size(); i++) {
			_args.get(i).unifySlash(s);
		}
	}

	public void unifyCheck(Object u) throws UnifyFailure {
	}

	// nb: direct unification not implemented ...
	public Object unify(Object u, Substitution sub) throws UnifyFailure {
		throw new UnifyFailure();
	}

	public Object fill(Substitution s) throws UnifyFailure {
		return new SetArg(_args.fill(s));
	}

	public void deepMap(ModFcn mf) {
		_args.deepMap(mf);
	}

	public boolean occurs(Variable v) {
		return _args.occurs(v);
	}

	public boolean equals(Object c) {
		return false;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(10);
		sb.append('{').append(_args.toString()).append('}');
		return sb.toString();
	}

	/**
	 * Returns the supertag for this arg.
	 */
	public String getSupertag() {
		StringBuffer sb = new StringBuffer();
		sb.append("{").append(_args.getSupertag()).append("}");
		return sb.toString();
	}

	/**
	 * Returns a TeX-formatted string representation for this arg.
	 */
	public String toTeX() {
		StringBuffer sb = new StringBuffer(10);
		sb.append("\\{").append(_args.toTeX()).append("\\}");
		return sb.toString();
	}

	/**
	 * Returns a hash code for this arg, using the given map from vars to ints.
	 */
	public int hashCode(TObjectIntHashMap varMap) {
		return _args.hashCode(varMap);
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
		SetArg sa = (SetArg) obj;
		return _args.equals(sa._args, varMap, varMap2);
	}
}
