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
import gnu.trove.*;
import org.jdom.*;

import java.io.Serializable;
import java.util.*;

/**
 * A stack of arguments with their associated slashes.
 * 
 * @author Jason Baldridge
 * @author Michael White
 * @version $Revision: 1.11 $, $Date: 2009/11/28 03:39:27 $
 */
public class ArgStack implements Serializable {

	private static final long serialVersionUID = -4610015768742537105L;

	protected Arg[] _list;

	protected boolean _hasDollar = false;

	protected boolean _hasSet = false;

	public ArgStack() {
		_list = new Arg[0];
	}

	public ArgStack(Arg c) {
		_list = new Arg[1];
		_list[0] = c;
		if (c instanceof Dollar) {
			_hasDollar = true;
		} else if (c instanceof SetArg) {
			_hasSet = true;
		}
	}

	public ArgStack(Slash s, Category c) {
		this(new BasicArg(s, c));
	}

	public ArgStack(Arg[] list) {
		_list = list;
		checkForDollar();
		checkForSet();
	}

	public ArgStack(List<Element> info) {
		List<Arg> args = new ArrayList<Arg>();
		for (Iterator<Element> infoIt = info.iterator(); infoIt.hasNext();) {
			Element el = infoIt.next();
			String elName = el.getName();
			if (elName.equals("setarg")) {
				args.add(new SetArg(el));
				_hasSet = true;
			} else if (elName.equals("dollar")) {
				String name = el.getAttributeValue("name");
				if (name == null)
					name = el.getAttributeValue("n");
				args.add(new Dollar(name));
				_hasDollar = true;
			} else if (elName.equals("slash") || elName.equals("sl")) {
				Slash s = new Slash(el);
				Element argEl = infoIt.next();
				if (argEl.getName().equals("dollar")) {
					String name = argEl.getAttributeValue("name");
					if (name == null)
						name = argEl.getAttributeValue("n");
					args.add(new Dollar(s, name));
					_hasDollar = true;
				} else {
					args.add(new BasicArg(s, CatReader.getCat(argEl)));
				}
			} else {
				System.out.println("Invalid element for creating ArgStack: "
						+ elName);
			}
		}
		_list = new Arg[args.size()];
		args.toArray(_list);
	}

	public void toXml(Element catElt) {
		for (Arg arg: _list) {
			if (arg instanceof SetArg)
				catElt.addContent(((SetArg) arg).toXml());
			else if (arg instanceof Dollar) {
				Dollar dollar = (Dollar) arg;
				if (!dollar.getSlash().toString().equals("|."))
					catElt.addContent(dollar.getSlash().toXml());
				Element dollarElt = new Element("dollar");
				dollarElt.setAttribute("name", dollar.name());
				catElt.addContent(dollarElt);
			}
			else if (arg instanceof BasicArg) {
				BasicArg barg = (BasicArg) arg;
				catElt.addContent(barg.getSlash().toXml());
				catElt.addContent(barg.getCat().toXml());
			}
		}
	}
	
	public void addAt(Arg c, int index) {
		Arg[] $list = new Arg[_list.length + 1];
		insert(subList(0, index)._list, $list, 0);
		$list[index] = c;
		insert(subList(index)._list, $list, index + 1);
		_list = $list;
		if (c instanceof Dollar) {
			_hasDollar = true;
		} else if (c instanceof SetArg) {
			_hasSet = true;
		}
	}

	public void add(Arg c) {
		Arg[] $list = new Arg[_list.length + 1];
		int last = insert(_list, $list, 0);
		$list[last] = c;
		_list = $list;
		if (c instanceof Dollar) {
			_hasDollar = true;
		} else if (c instanceof SetArg) {
			_hasSet = true;
		}
	}

	public void addAt(ArgStack cl, int index) {
		Arg[] $list = new Arg[_list.length + cl._list.length];
		int last = insert(subList(0, index)._list, $list, 0);
		last = insert(cl._list, $list, last);
		insert(subList(index)._list, $list, last);
		_list = $list;
		if (cl.containsDollarArg()) {
			_hasDollar = true;
		} else if (cl.containsSetArg()) {
			_hasSet = true;
		}
	}

	public void add(ArgStack cl) {
		Arg[] $list = new Arg[_list.length + cl._list.length];
		int last = insert(_list, $list, 0);
		insert(cl._list, $list, last);
		_list = $list;
		if (cl.containsDollarArg()) {
			_hasDollar = true;
		} else if (cl.containsSetArg()) {
			_hasSet = true;
		}
	}

	public void addFront(Arg c) {
		Arg[] $list = new Arg[_list.length + 1];
		$list[0] = c;
		insert(_list, $list, 1);
		_list = $list;
		if (c instanceof Dollar) {
			_hasDollar = true;
		} else if (c instanceof SetArg) {
			_hasSet = true;
		}
	}

	public void addFront(ArgStack cl) {
		Arg[] $list = new Arg[_list.length + cl._list.length];
		int last = insert(cl._list, $list, 0);
		insert(_list, $list, last);
		_list = $list;
		if (cl.containsDollarArg()) {
			_hasDollar = true;
		} else if (cl.containsSetArg()) {
			_hasSet = true;
		}
	}

	public void insertFront(ArgStack cl) {
		insertAt(cl, 0);
	}

	public void insertEnd(ArgStack cl) {
		insertAt(cl, _list.length - 1);
	}

	public void insertAt(ArgStack cl, int index) {
		Arg insertInto = _list[index];
		if (insertInto instanceof BasicArg) {
			cl.add(insertInto);
			_list[index] = new SetArg(cl);
			_hasSet = true;
		} else if (insertInto instanceof SetArg) {
			((SetArg) insertInto).add(cl);
		} else {
			System.out.println("Problem inserting arg stack: " + cl);
		}
	}

	public int size() {
		return _list.length;
	}

	public boolean containsDollarArg() {
		return _hasDollar;
	}

	public boolean containsSetArg() {
		return _hasSet;
	}

	public Arg get(int i) {
		return _list[i];
	}

	public void set(int i, Arg c) {
		_list[i] = c;
		if (c instanceof Dollar) {
			_hasDollar = true;
		} else if (c instanceof SetArg) {
			_hasSet = true;
		}
	}

	public Arg getLast() {
		return _list[_list.length - 1];
	}

	public void setLast(Arg c) {
		set(_list.length - 1, c);
	}

    /** Sets the harmonic composition result of each arg's slash. */
    public void setSlashHarmonicCompositionResult(boolean harmonicResult) {
    	for (int i=0; i < _list.length; i++) {
    		_list[i].setSlashHarmonicCompositionResult(harmonicResult);
    	}
    }
    
	public ArgStack copy() {
		Arg[] $list = new Arg[_list.length];
		for (int i = 0; i < $list.length; i++) {
			$list[i] = _list[i].copy();
		}
		return new ArgStack($list);
	}

	public ArgStack copyWithout(int indexToRemove) {
		Arg[] $list = new Arg[_list.length - 1];
		if ($list.length < 1) {
			System.out.println("Removing last item from an argument stack!");
		}
		int index = 0;
		for (int i = 0; i < _list.length; i++) {
			if (i != indexToRemove) {
				$list[index++] = _list[i].copy();
			}
		}
		return new ArgStack($list);
	}

	public ArgStack subList(int from) {
		return subList(from, _list.length);
	}

	public ArgStack subList(int from, int upto) {
		Arg[] $list;
		if (upto > from) {
			$list = new Arg[upto - from];
			int index = 0;
			for (int i = from; i < upto; i++) {
				$list[index++] = _list[i];
			}
		} else {
			$list = new Arg[0];
		}
		return new ArgStack($list);
	}

	public ArgStack shallowCopy() {
		return new ArgStack(_list);
	}

	public boolean occurs(Variable v) {
		for (int i = 0; i < _list.length; i++) {
			if (_list[i].occurs(v)) {
				return true;
			}
		}
		return false;
	}

	public ArgStack fill(Substitution s) throws UnifyFailure {
		ArgStack args = new ArgStack();
		for (int i = 0; i < _list.length; i++) {
			Object value = _list[i].fill(s);
			if (value instanceof ArgStack) {
				args.add((ArgStack) value);
			} else {
				args.add((Arg) value);
			}
		}
		return args;
	}

	public void deepMap(ModFcn mf) {
		for (int i = 0; i < _list.length; i++) {
			_list[i].deepMap(mf);
		}
	}

	public boolean containsContrarySlash() {
		for (int i = 0; i < _list.length; i++) {
			if (_list[i] instanceof BasicArg
					&& !((BasicArg) _list[i]).getSlash().sameDirAsModality()) {
				return true;
			} else if (_list[i] instanceof SetArg
					&& ((SetArg) _list[i]).containsContrarySlash()) {
				return true;
			}
		}
		return false;
	}

	public void slashesUnify(Slash s) throws UnifyFailure {
		for (int i = 0; i < _list.length; i++) {
			_list[i].unifySlash(s);
		}
	}

	public int unifySuffix(ArgStack as, Substitution sub) throws UnifyFailure {

		int asIndex = as.size();
		for (int i = _list.length - 1; i >= 0; i--) {
			asIndex--;
			get(i).unify(as.get(asIndex), sub);
		}
		return asIndex;
	}

	public ArgStack unify(ArgStack as, Substitution sub) throws UnifyFailure {
		return unifyPrefix(as, as.size(), sub);
	}

	public ArgStack unifyPrefix(ArgStack as, int upto, Substitution sub)
			throws UnifyFailure {

		ArgStack $args;
		if (containsDollarArg()) {
			if (as.containsDollarArg()) {
				$args = unifyDollarWithDollar(as, upto, sub);
			} else {
				$args = unifyDollarWithNoDollar(size(), as, upto, sub);
			}
		} else if (as.containsDollarArg()) {
			$args = as.unifyDollarWithNoDollar(upto, this, size(), sub);
		} else if (size() == upto) {
			$args = unifySimple(as, upto, sub);
		} else {
			$args = unifyComplex(as, upto, sub);
			// throw new UnifyFailure();
		}
		return $args;
	}

	private ArgStack unifySimple(ArgStack as, int upto, Substitution sub)
			throws UnifyFailure {

		ArgStack $args = new ArgStack();
		for (int i = upto - 1; i >= 0; i--) {
			$args.addFront((Arg) _list[i].unify(as.get(i), sub));
		}
		return $args;
	}

	private ArgStack unifyComplex(ArgStack as, int upto, Substitution sub)
			throws UnifyFailure {

		ArgStack $args = new ArgStack();

		int aIndex = size() - 1;
		int bIndex = upto - 1;
		while (aIndex >= 0 && bIndex >= 0) {
			// while (null != aArg && null != bArg) {
			Arg aArg = get(aIndex);
			Arg bArg = as.get(bIndex);

			if ((aArg instanceof BasicArg && bArg instanceof BasicArg)
					|| (aArg instanceof SetArg && bArg instanceof SetArg)) {
				$args.addFront((Arg) aArg.unify(bArg, sub));
				aIndex--;
				bIndex--;
			} else if (aArg instanceof BasicArg && bArg instanceof SetArg) {
				int setsize = ((SetArg) bArg).size();
				if (setsize <= aIndex + 1) {
					int stop = aIndex - setsize;
					for (; aIndex > stop;) {
						aIndex--;
						if (bArg instanceof BasicArg) {
							$args.addFront((Arg) aArg.unify(bArg, sub));
						} else {
							int idInSet = ((SetArg) bArg)
									.indexOf((BasicArg) aArg);
							if (idInSet == -1)
								throw new UnifyFailure();
							$args.addFront((Arg) aArg.unify(((SetArg) bArg)
									.get(idInSet), sub));
							aArg = get(aIndex);
							bArg = ((SetArg) bArg).copyWithout(idInSet);
						}
					}
					bIndex--;
				} else {
					throw new UnifyFailure();
				}
			} else if (aArg instanceof SetArg && bArg instanceof BasicArg) {
				throw new UnifyFailure();
			} else {
				throw new UnifyFailure();
			}
		}
		if (aIndex > -1 || bIndex > -1) {
			throw new UnifyFailure();
		}
		return $args;
	}

	private ArgStack unifyDollarWithNoDollar(int uptoThis, ArgStack otherStack,
			int uptoOther, Substitution sub) throws UnifyFailure {

		if ((!(_hasSet || otherStack._hasSet) && uptoThis > uptoOther + 1)
				|| (uptoThis > 1 && uptoOther < 1)) {
			throw new UnifyFailure();
		}
		ArgStack $args = new ArgStack();
		otherStack = otherStack.subList(0, uptoOther);
		int otherIndex = uptoOther - 1;

		for (int i = uptoThis - 1; i >= 0; i--) {
			Arg argi = get(i);
			if (argi instanceof Dollar) {
				if (i > 0) {
					throw new UnifyFailure();
				} else {
					ArgStack $subArgs = otherStack.subList(0, otherIndex + 1);
					// Slash dsl = ((Dollar) argi).getSlash();
					((Dollar) argi).unify($subArgs.copy(), sub);
					otherIndex = 0;
					$args.addFront($subArgs);
				}
			} else if (argi instanceof BasicArg) {
				if (otherIndex < 0) {
					throw new UnifyFailure();
				}

				Arg otherArg = otherStack.get(otherIndex);
				if (otherArg instanceof BasicArg) {
					$args.addFront((Arg) argi.unify(otherArg, sub));
					otherIndex--;
				} else if (otherArg instanceof SetArg) {
					SetArg sa = (SetArg) otherArg;
					int id = sa.indexOf((BasicArg) argi);
					if (id == -1)
						throw new UnifyFailure();
					$args.addFront((Arg) argi.unify(sa.get(id), sub));
					otherStack.set(otherIndex, sa.copyWithout(id));
				}
			} else {
				throw new UnifyFailure();
			}
		}
		if (otherIndex > 0) {
			throw new UnifyFailure();
		}
		return $args;
	}

	private ArgStack unifyDollarWithDollar(ArgStack as, int upto,
			Substitution sub) throws UnifyFailure {

		ArgStack $args;
		if (size() == 1) {
			$args = as.subList(0, upto);
			((Dollar) get(0)).unify($args.copy(), sub);
		} else if (upto == 1) {
			$args = subList(0, size());
			((Dollar) as.get(0)).unify($args.copy(), sub);
		} else if (upto == size()) {
			$args = unifySimple(as, upto, sub);
		} else {
			throw new UnifyFailure();
		}
		return $args;
	}

	public void forall(CategoryFcn fcn) {
		for (int i = 0; i < _list.length; i++) {
			_list[i].forall(fcn);
		}
	}

	private void checkForDollar() {
		for (int i = 0; i < _list.length; i++) {
			if (_list[i] instanceof Dollar) {
				_hasDollar = true;
				return;
			}
		}
	}

	private void checkForSet() {
		for (int i = 0; i < _list.length; i++) {
			if (_list[i] instanceof SetArg) {
				_hasSet = true;
				return;
			}
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < _list.length; i++) {
			sb.append(_list[i].toString());
		}
		return sb.toString();
	}

// 	private boolean methodExists(Object o, String methodName) {
// 		java.lang.reflect.Method[] m = o.getClass().getMethods();
// 		for (int i = 0; i < m.length; i++)
// 			if (m[i].getName() == methodName) {
// 				if (m[i].getDeclaringClass().toString().startsWith("class"))
// 					return true;
// 				else
// 					return false;
// 			}
// 		return false;
// 	}

	/**
	 * Returns the supertag for this arg stack.
	 */
	public String getSupertag() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < _list.length; i++) {
			Arg arg = (Arg) _list[i];
			sb.append(arg.getSupertag());
		}
		return sb.toString();
	}

	/**
	 * Returns a TeX-formatted string representation for this arg stack.
	 */
	public String toTeX() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < _list.length; i++) {
		    sb.append(_list[i].toTeX());
		}
		return sb.toString();
	}

	/**
	 * Returns a hash code using the given map from vars to ints.
	 */
	public int hashCode(TObjectIntHashMap varMap) {
		int retval = 0;
		for (int i = 0; i < _list.length; i++) {
			retval += _list[i].hashCode(varMap);
		}
		return retval;
	}

	/**
	 * Returns whether this arg stack equals the given object up to variable
	 * names, using the given maps from vars to ints.
	 */
	public boolean equals(Object obj, TObjectIntHashMap varMap,
			TObjectIntHashMap varMap2) {
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		ArgStack as = (ArgStack) obj;
		if (_list.length != as._list.length) {
			return false;
		}
		for (int i = 0; i < _list.length; i++) {
			if (!_list[i].equals(as._list[i], varMap, varMap2)) {
				return false;
			}
		}
		return true;
	}

	protected static int insert(Arg[] a, Arg[] b, int pos) {
		for (int i = 0; i < a.length; i++) {
			b[pos++] = a[i];
		}
		return pos;
	}
}
