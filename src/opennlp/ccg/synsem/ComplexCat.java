///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-5 Jason Baldridge and University of Edinburgh (Michael White)
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
import opennlp.ccg.util.DisplayPrefs;
import opennlp.ccg.grammar.Grammar;
import gnu.trove.*;
import org.jdom.*;

import java.util.*;

/**
 * A non-recursive representation of complex categories.
 * 
 * @author Jason Baldridge
 * @author Michael White
 * @version $Revision: 1.15 $, $Date: 2009/06/18 23:38:57 $
 */
public final class ComplexCat extends AbstractCat {

	private static final long serialVersionUID = 1L;

	private TargetCat _target;

	private ArgStack _args;

	/** Constructor with target and single arg. */
	public ComplexCat(TargetCat target, Arg arg) {
		this(target, new ArgStack(arg));
	}

	/** Constructor with target and arg stack. */
	public ComplexCat(TargetCat target, ArgStack args) {
		this(target, args, null);
	}

	/** Constructor with target, arg stack and LF. */
	public ComplexCat(TargetCat target, ArgStack args, LF lf) {
		super(lf);
		_target = target;
		if (args.size() < 1) {
			System.out.println("WARNING!!! Creating a ComplexCat with"
					+ " empty argument stack!");
		}
		_args = args;
	}

	/** Constructor which retrieves the complex category from the XML element. */
	// also determines modifier slashes
	@SuppressWarnings("unchecked")
	public ComplexCat(Element el) {
		// call super to get LF if present
		super(el);
		// get children minus LF elt
		List<Element> info = el.getChildren();
		Element lfElt = el.getChild("lf");
		if (lfElt != null) {
			info.remove(lfElt);
		}
		// get target and args from first and rest of remaining children
		_target = (TargetCat) CatReader.getCat(info.get(0));
		_args = new ArgStack(info.subList(1, info.size()));
		// set modifier slashes
		setModifierSlashes();
	}
	
    /**Returns an XML element representing the category. */
    public Element toXml() {
    	Element retval = new Element("complexcat");
    	retval.addContent(_target.toXml());
    	_args.toXml(retval);
    	// call super to add LF if present
    	super.toXml(retval);
    	return retval;
    }
	
	// sets modifier slashes based on feat struc ids
	private void setModifierSlashes() {
		FeatureStructure targetFS = _target.getFeatureStructure(); 
		int targetIndex = targetFS.getIndex();
		if (targetIndex == 0) targetIndex = targetFS.getInheritsFrom();
		if (targetIndex == 0) return;
		for (int i=0; i < _args.size(); i++) {
			Arg arg = _args.get(i);
			if (arg instanceof BasicArg) {
				setModifierSlash(targetIndex, (BasicArg)arg);
			}
			else if (arg instanceof SetArg) {
				SetArg sArg = (SetArg) arg;
				for (int j=0; j < sArg.size(); j++) {
					setModifierSlash(targetIndex, sArg.get(j));
				}
			}
		}
	}
	
	// sets modifier slash based on the target index
	private void setModifierSlash(int targetIndex, BasicArg arg) {
		FeatureStructure argFS = arg.getCat().getTarget().getFeatureStructure();
		// check for matching ids
		if (targetIndex == argFS.getIndex() || targetIndex == argFS.getInheritsFrom())
			arg.getSlash().setModifier(true);
	}

	
	/**
	 * Returns the target category of this category.
	 */
	public TargetCat getTarget() {
		return _target;
	}

	public Arg getArg(int pos) {
		return _args.get(pos);
	}

	public Arg getOuterArg() {
		return _args.getLast();
	}

	public Category getResult() {
		return getSubResult(arity() - 1);
	}

	public Category getSubResult(int upto) {
		if (upto == 0) {
			return _target;
		} else {
			return new ComplexCat(_target, _args.subList(0, upto));
		}
	}

	public ArgStack getArgStack() {
		return _args;
	}

	public ArgStack getArgStack(int from) {
		return _args.subList(from);
	}

	public boolean containsDollarArg() {
		return _args.containsDollarArg();
	}

	public boolean containsSetArg() {
		return _args.containsSetArg();
	}

	public void add(Arg a) {
		_args.add(a);
	}

	public void add(ArgStack as) {
		_args.add(as);
	}

	public void addBeforeEnd(ArgStack as) {
		int size = _args.size();
		if (size < 1) {
			add(as);
		} else {
			_args.addAt(as, size - 1);
		}
	}

	public void addFront(ArgStack as) {
		_args.addFront(as);
	}

	public void insertFront(ArgStack as) {
		_args.insertFront(as);
	}

	public void insertEnd(ArgStack as) {
		_args.insertEnd(as);
	}

	public void set(int index, Arg c) {
		_args.set(index, c);
	}

	public void setOuterArgument(Arg c) {
		_args.setLast(c);
	}

	public int arity() {
		return _args.size();
	}

	public Category copy() {
		return new ComplexCat((TargetCat) _target.copy(), _args.copy(),
				(_lf == null) ? null : (LF) _lf.copy());
	}

	public Category shallowCopy() {
		return new ComplexCat(_target, _args, _lf);
	}

	public void deepMap(ModFcn mf) {
		super.deepMap(mf);
		_target.deepMap(mf);
		_args.deepMap(mf);
	}

	public void forall(CategoryFcn f) {
		f.forall(this);
		_target.forall(f);
		_args.forall(f);
	}

	public void unifyCheck(Object u) throws UnifyFailure {
		if (u instanceof ComplexCat) {
			ComplexCat cc = (ComplexCat) u;
			_target.unifyCheck(cc._target);
		}
	}

	/** NB: The LF does not participate in unification. */
	public Object unify(Object u, Substitution sub) throws UnifyFailure {

		if (u instanceof AtomCat && arity() == 1 & containsDollarArg()) {
			sub.makeSubstitution((Dollar) _args.get(0), new ArgStack());
			return GUnifier.unify(_target, (AtomCat) u, sub);
		} else if (u instanceof ComplexCat) {
			ComplexCat cc = (ComplexCat) u;
			ArgStack $args = _args.unify(cc._args, sub);
			Category $target = GUnifier.unify(_target, cc._target, sub);
			if ($args.size() == 0) {
				return $target;
			} else {
				return new ComplexCat((TargetCat) $target, $args);
			}
		} else {
			throw new UnifyFailure();
		}
	}

	public boolean occurs(Variable v) {
		return super.occurs(v) || _target.occurs(v) || _args.occurs(v);
	}

	// nb: not yet sure about calling setLF methods
	public Object fill(Substitution s) throws UnifyFailure {
		Category $target = (Category) _target.fill(s);
		ArgStack $args = _args.fill(s);
		LF $lf = (_lf == null) ? null : (LF) _lf.fill(s);
		if ($args.size() == 0) {
			$target.setLF($lf);
			return $target;
		}
		if ($target instanceof TargetCat) {
			return new ComplexCat((TargetCat) $target, $args, $lf);
		} else if ($target instanceof ComplexCat) {
			((ComplexCat) $target).add($args);
			$target.setLF($lf);
			return $target;
		} else {
			throw new UnifyFailure();
		}
	}

	public String toString() {
	    DisplayPrefs prefs = Grammar.theGrammar.prefs;
	    StringBuffer sb = new StringBuffer();
	    sb.append(_target.toString()).append(_args.toString());
	    if (_lf != null && prefs.showSem) {
	    	sb.append(" : ").append(_lf.toString());
	    }
	    return sb.toString();
	}

	/**
	 * Returns the interned supertag for the category.
	 */
	public String getSupertag() {
		if (_supertag != null) return _supertag;
		StringBuffer sb = new StringBuffer();
		sb.append(_target.getSupertag()).append(_args.getSupertag());
		_supertag = sb.toString().intern();
		return _supertag;
	}

	public String toTeX() {
		// Preferences prefs = Preferences.userNodeForPackage(TextCCG.class);
		// boolean showSem = prefs.getBoolean(SHOW_SEMANTICS, false);
		StringBuffer sb = new StringBuffer();
		sb.append(_target.toTeX()).append(_args.toTeX());
		return sb.toString();
	}

	/**
	 * Returns a hash code for this category ignoring the LF, using the given
	 * map from vars to ints.
	 */
	public int hashCodeNoLF(TObjectIntHashMap varMap) {
		int retval = _target.hashCodeNoLF(varMap);
		retval += _args.hashCode(varMap);
		return retval;
	}

	/**
	 * Returns whether this category equals the given object up to variable
	 * names, using the given maps from vars to ints, ignoring the LFs (if any).
	 */
	public boolean equalsNoLF(Object obj, TObjectIntHashMap varMap,
			TObjectIntHashMap varMap2) {
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		ComplexCat cc = (ComplexCat) obj;
		if (!_target.equalsNoLF(cc._target, varMap, varMap2)) {
			return false;
		}
		if (!_args.equals(cc._args, varMap, varMap2)) {
			return false;
		}
		return true;
	}
}
