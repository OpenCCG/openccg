///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002-3 Jason Baldridge and University of Edinburgh (Michael White)
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

package opennlp.ccg.grammar;

import opennlp.ccg.TextCCG;
import opennlp.ccg.unify.*;
import opennlp.ccg.synsem.*;

import java.util.*;
import java.util.prefs.Preferences;

import org.jdom.Element;

/**
 * Super class for composition rules.
 * 
 * @author Jason Baldridge
 * @author Michael White
 * @version $Revision: 1.10 $, $Date: 2009/12/21 03:27:18 $
 */
public abstract class AbstractCompositionRule extends AbstractApplicationRule {

	private static final long serialVersionUID = 1L;

	/** Preference key for Eisner constraints. */
    public static final String EISNER_CONSTRAINTS = "Use Eisner Constraints";
    
	/** 
	 * Flag for whether to impose Eisner's normal form constraints.
	 * The flag is initialized based on user preferences; 
	 * it must be changed for further preferences to take effect. 
	 */
	public static boolean useEisnerConstraints = initEisnerConstraints();
	
	private static boolean initEisnerConstraints() {
        Preferences prefs = Preferences.userNodeForPackage(TextCCG.class);
        return prefs.getBoolean(EISNER_CONSTRAINTS, true);
	}
	
	protected boolean _isHarmonic;

	protected Slash _argSlash;

    /** Returns an XML element representing the rule. */
    public Element toXml(String dir) {
    	Element retval = new Element("composition");
    	retval.setAttribute("dir", dir);
    	retval.setAttribute("harmonic", Boolean.toString(_isHarmonic));
    	return retval;
    }

	protected boolean eisner() { return useEisnerConstraints && _isHarmonic; }
	
	protected List<Category> apply(Category xyCat, Category yzCat) throws UnifyFailure {

		if (xyCat instanceof ComplexCat && yzCat instanceof ComplexCat) {
			List<Category> results = new ArrayList<Category>(1);
            _headCats.clear();
			ComplexCat xyCC = (ComplexCat) xyCat;
			ComplexCat yzCC = (ComplexCat) yzCat;

			Arg xyOuter = xyCC.getOuterArg();
			if (xyOuter instanceof BasicArg) {
				Slash xySlash = ((BasicArg) xyOuter).getSlash();
				xySlash.unifyCheck(_functorSlash);
				if (eisner() && xySlash.isHarmonicCompositionResult()) throw new UnifyFailure();
				Category xyOuterCat = ((BasicArg) xyOuter).getCat();

				if (xyOuterCat instanceof AtomCat) {
					// e.g. s/s Y/Z
					ArgStack zStack = yzCC.getArgStack();
					zStack.slashesUnify(_argSlash);
					Substitution sub = new GSubstitution();
					GUnifier.unify(xyOuterCat, yzCC.getTarget(), sub);
					xySlash = (Slash) xySlash.fill(sub);
					xySlash.unifyCheck(_functorSlash);
					Category outcome = createResult(xyCC.getResult(), zStack, xySlash, sub);
					appendLFs(xyCat, yzCat, outcome, sub);
					results.add(outcome);
	                _headCats.add(xySlash.isModifier() ? yzCat : xyCat); 
				} else if (xyOuterCat instanceof ComplexCat) {
					// e.g. s/(s/n) Y/Z
					Substitution sub = new GSubstitution();
					ArgStack zStack = composeComplexY((ComplexCat) xyOuterCat, xySlash, yzCC, sub);
					xySlash = (Slash) xySlash.fill(sub);
					xySlash.unifyCheck(_functorSlash);
					Category outcome = createResult(xyCC.getResult(), zStack, xySlash, sub);
					appendLFs(xyCat, yzCat, outcome, sub);
					results.add(outcome);
	                _headCats.add(xySlash.isModifier() ? yzCat : xyCat); 
				}
			} else if (xyOuter instanceof SetArg) {
				// e.g. s/{s,n} Y/Z
				Category yzTarget = yzCC.getTarget();
				SetArg xyOuterSet = (SetArg) xyOuter;
				int targetIndex = xyOuterSet.indexOf(yzTarget);
				if (targetIndex > -1) {
					Slash xySlash = xyOuterSet.get(targetIndex).getSlash();
					xySlash.unifyCheck(_functorSlash);
					if (eisner() && xySlash.isHarmonicCompositionResult()) throw new UnifyFailure();
					Substitution sub = new GSubstitution();
					GUnifier.unify(xyOuterSet.getCat(targetIndex), yzTarget, sub);
					Category result = xyCC.copy();
					((ComplexCat) result).setOuterArgument(xyOuterSet.copyWithout(targetIndex));
					ArgStack zStack = yzCC.getArgStack();
					zStack.slashesUnify(_argSlash);
					Category outcome = createResult(result, zStack, xySlash, sub);
					appendLFs(xyCat, yzCat, outcome, sub);
					results.add(outcome);
	                _headCats.add(xySlash.isModifier() ? yzCat : xyCat); 
				} else {
					boolean success = false;
					for (int i = 0; i < xyOuterSet.size(); i++) {
						BasicArg yInSet = xyOuterSet.get(i);
						if (yInSet.getCat() instanceof ComplexCat) {
							Slash xySlash = yInSet.getSlash();
							xySlash.unifyCheck(_functorSlash);
							if (eisner() && xySlash.isHarmonicCompositionResult()) throw new UnifyFailure();
							ComplexCat yCat = (ComplexCat) yInSet.getCat();
							Substitution sub = new GSubstitution();
							ArgStack zStack = composeComplexY((ComplexCat) yCat, xySlash, yzCC, sub);
							xySlash = (Slash) xySlash.fill(sub);
							xySlash.unifyCheck(_functorSlash);
							Category result = xyCC.copy();
							((ComplexCat) result).setOuterArgument(xyOuterSet.copyWithout(i));
							Category outcome = createResult(result, zStack, xySlash, sub);
							appendLFs(xyCat, yzCat, outcome, sub);
							results.add(outcome);
			                _headCats.add(xySlash.isModifier() ? yzCat : xyCat); 
							success = true;
						}
					}
					if (!success) {
						throw new UnifyFailure();
					}
				}
			} else {
				throw new UnifyFailure();
			}

			return results;
		} else {
			throw new UnifyFailure();
		}
	}

	private Category createResult(Category result, ArgStack zStack,
			Slash xySlash, Substitution sub) throws UnifyFailure {
		((GSubstitution) sub).condense();
		result = (Category) result.fill(sub);
		ArgStack newStack = zStack.fill(sub);
		if (!_isHarmonic
				&& (!xySlash.sameDirAsModality() || zStack
						.containsContrarySlash())) {
			newStack.deepMap(INERTIZER_FCN);
		}
		newStack.get(0).setSlashModifier(false);
		if (_isHarmonic && useEisnerConstraints) 
			newStack.setSlashHarmonicCompositionResult(true);
		if (result instanceof ComplexCat) {
			((ComplexCat) result).add(newStack);
		} else {
			result = new ComplexCat((TargetCat) result, newStack);
		}
		return result;
	}

	/**
	 * A function that tries to unify the value ant=+ into feature structures.
	 */
	private static ModFcn INERTIZER_FCN = new ModFcn() {
		public void modify(Mutable m) {
			if (m instanceof Slash) {
				((Slash) m).setAbility("inert");
			}
		}
	};

	private ArgStack composeComplexY(ComplexCat xyOuterCC, Slash xySlash,
			ComplexCat yzCC, Substitution sub) throws UnifyFailure {

		GUnifier.unify(xyOuterCC.getTarget(), yzCC.getTarget(), sub);
		ArgStack zStack = yzCC.getArgStack();
		if (xyOuterCC.containsDollarArg()) {
			// e.g. s$/(s$\n) s\n/n
			xyOuterCC.getArgStack().unifyPrefix(zStack, zStack.size() - 1, sub);
			zStack = zStack.subList(zStack.size() - 1);
			zStack.slashesUnify(_argSlash);
			xySlash = (Slash) xySlash.fill(sub);
			xySlash.unifyCheck(_functorSlash);
			return zStack;
		} else if (xyOuterCC.arity() == 1) {
			ArgStack yzStack = yzCC.getArgStack();
			if (!(xyOuterCC.getArg(0) instanceof BasicArg)) {
				throw new UnifyFailure();
			}
			BasicArg xyOuterOuter = (BasicArg) xyOuterCC.getArg(0);
			Arg yzStackInner = yzStack.get(0);
			if (yzStackInner instanceof SetArg) {
				// e.g. s/(s/n) s/{s,n}
				SetArg yzSetArg = (SetArg) yzStackInner;
				int iaIndex = yzSetArg.indexOf(xyOuterOuter);
				if (iaIndex == -1)
					throw new UnifyFailure();
				xyOuterOuter.unify(yzSetArg.get(iaIndex), sub);
				xySlash = (Slash) xySlash.fill(sub);
				xySlash.unifyCheck(_functorSlash);
				zStack = yzStack.copy();
				zStack.set(0, yzSetArg.copyWithout(iaIndex));
				zStack.slashesUnify(_argSlash);
				return zStack;
			} else {
				// e.g. s/(s/n) s/n/s
				if (yzStack.size() < 2) {
					throw new UnifyFailure();
				}
				xyOuterOuter.unify(yzStackInner, sub);
				zStack = yzStack.subList(1).copy();
				zStack.slashesUnify(_argSlash);
				xySlash = (Slash) xySlash.fill(sub);
				xySlash.unifyCheck(_functorSlash);
				return zStack;
			}
		} else if (xyOuterCC.arity() == 2) {
			// e.g. s\np/(s\np) s\np/(s\np)/n
			// nb: not dealing with set args
			ArgStack yzStack = yzCC.getArgStack();
			if (!(xyOuterCC.getArg(0) instanceof BasicArg)
					|| !(xyOuterCC.getArg(1) instanceof BasicArg)
					|| yzStack.size() < 3) {
				throw new UnifyFailure();
			}
			BasicArg xyOuterOuter1 = (BasicArg) xyOuterCC.getArg(0);
			BasicArg xyOuterOuter2 = (BasicArg) xyOuterCC.getArg(1);
			Arg yzStackInner1 = yzStack.get(0);
			Arg yzStackInner2 = yzStack.get(1);
			xyOuterOuter1.unify(yzStackInner1, sub);
			xyOuterOuter2.unify(yzStackInner2, sub);
			zStack = yzStack.subList(2).copy();
			zStack.slashesUnify(_argSlash);
			xySlash = (Slash) xySlash.fill(sub);
			xySlash.unifyCheck(_functorSlash);
			return zStack;
		} else {
			// nb: not dealing with xyOuterCC arity > 2
			throw new UnifyFailure();
		}
	}
}
