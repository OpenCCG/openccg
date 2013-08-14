///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-11 Jason Baldridge, Gann Bierner and 
//                       University of Edinburgh (Michael White)
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
import org.jdom.*;

import gnu.trove.*;

/**
 * The most basic CG category.  This corresponds to a category like 'np[acc]',
 * i.e. a category name and associated features.
 *
 * @author      Gann Bierner
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.18 $, $Date: 2011/07/15 03:02:53 $
 */
public final class AtomCat extends AbstractCat implements TargetCat {

    private static final long serialVersionUID = 1L;

    private String type;

    /** Flag indicating whether this cat is a fragment with completion status true (defaults to false). */
    public boolean fragCompletion = false;
    
    /** Constructor which creates an atomic category with the given type. */
    public AtomCat(String t) {
        this(t, new GFeatStruc());
    }

    /** Constructor which creates an atomic category with the given type and feature structure. */
    public AtomCat(String t, FeatureStructure fs) {
        this(t, fs, null); 
    }

    /** Constructor which creates an atomic category with the given type, feature structure and LF. */
    public AtomCat(String t, FeatureStructure fs, LF lf) {
        super(lf);
        type = t; 
        _featStruc = fs; 
    }

    /** Constructor which retrieves the atomic category from the XML element. */
    public AtomCat(Element acel) {
        // call super to get LF if present
        super(acel);
        // get type
        type = acel.getAttributeValue("type");
        if (type == null) type = acel.getAttributeValue("t"); 
        // get feature structure
        Element fsEl = acel.getChild("fs");
        if (fsEl != null) {
            _featStruc = new GFeatStruc(fsEl);
        }
        // or create empty one
        else {
            _featStruc = new GFeatStruc();
        }
    }

    /**Returns an XML element representing the category. */
    public Element toXml() {
    	Element retval = new Element("atomcat");
    	retval.setAttribute("type", type);
    	if (!_featStruc.isEmpty() || _featStruc.getIndex() > 0) {
    		if (_featStruc instanceof GFeatStruc) { // only supporting GFeatStruc per xml construction
    			retval.addContent(((GFeatStruc) _featStruc).toXml());
    		}
    	}
    	// call super to add LF if present
    	super.toXml(retval);
    	return retval;
    }

    /**
     * Returns this category as the target category.
     */
    public TargetCat getTarget() { return this; }
    
    
    public String getType() {
        return type;
    }

    
    public Category copy() {
    	AtomCat retval = new AtomCat(type, _featStruc.copy(), (_lf == null) ? null : (LF) _lf.copy());
    	retval.fragCompletion = fragCompletion;
        return retval;
    }

    public Category shallowCopy() {
        AtomCat retval = new AtomCat(type, _featStruc, _lf);
    	retval.fragCompletion = fragCompletion;
        return retval;
    }

    public void deepMap(ModFcn mf) { 
        super.deepMap(mf);
        _featStruc.deepMap(mf);
    }

    public void unifyCheck (Object u) throws UnifyFailure {
        if (u instanceof AtomCat) {
            AtomCat u_ac = (AtomCat)u;
            if (!(type.equals(u_ac.type))) {
                throw new UnifyFailure();
            }
            if (_featStruc != null && u_ac._featStruc != null) {
                _featStruc.unifyCheck(u_ac._featStruc);
            }
        } else if (!(u instanceof Variable)) {
            throw new UnifyFailure();
        }
    }

    /** NB: The LF does not participate in unification. */
    public Object unify (Object u, Substitution sub) 
        throws UnifyFailure {

        if (u instanceof AtomCat && type.equals(((AtomCat)u).type)) {
            AtomCat u_ac = (AtomCat)u;
            FeatureStructure $fs;
            if (_featStruc == null) {
                $fs = u_ac._featStruc;
            } else if (u_ac._featStruc == null) {
                $fs = _featStruc;
            } else {
                $fs = (FeatureStructure)_featStruc.unify(u_ac._featStruc, sub);
            }
            return new AtomCat(type, $fs);
        }
        else {
            throw new UnifyFailure();
        }
    }

    public Object fill (Substitution s) throws UnifyFailure {
        AtomCat $ac =
            new AtomCat(type,
                        (FeatureStructure)_featStruc.fill(s),
                        (_lf == null) ? null : (LF) _lf.fill(s));
        return $ac;
    }

    public boolean shallowEquals(Object c) {
        if (c instanceof AtomCat) {
            AtomCat ac = (AtomCat)c;
            return type.equals(ac.type);
        }
        return false;
    }
    
    public String toString() {
        DisplayPrefs prefs = Grammar.theGrammar.prefs;

        StringBuffer sb = new StringBuffer();
        sb.append(type);
        if (fragCompletion) sb.append("_c");
        
        if(_featStruc != null && prefs.showFeats)
            sb.append(_featStruc.toString());

        if (_lf != null && prefs.showSem) {
            sb.append(" : ").append(_lf.toString());
        }

        if (sb.length() == 0) return "UnknownCat";
        return sb.toString();
    }

    /**
     * Returns the interned supertag for the category.
     */
    public String getSupertag() {
		if (_supertag != null) return _supertag;
        StringBuffer sb = new StringBuffer();
        sb.append(type);
        if(_featStruc != null) sb.append(_featStruc.getSupertagInfo());
        if (sb.length() == 0) _supertag = "UnknownCat";
        else _supertag = sb.toString().intern();
		return _supertag;
    }
    
    public String toTeX() {
        DisplayPrefs prefs = Grammar.theGrammar.prefs;
        StringBuffer sb = new StringBuffer();
        sb.append(type);
        if(_featStruc != null && prefs.showFeats)
            sb.append(_featStruc.toTeX());
        if (sb.length() == 0) return "UnknownCat";
        return sb.toString();
    }

    
    /**
     * Returns a hash code for this category ignoring the LF, 
     * using the given map from vars to ints.
     */
    public int hashCodeNoLF(TObjectIntHashMap varMap) {
        int retval = type.hashCode();
        if (_featStruc != null) { 
            if (_featStruc instanceof GFeatStruc) {
                retval += ((GFeatStruc)_featStruc).hashCode(varMap);
            } else { // nb: would be nice to get rid of this case
                retval += _featStruc.hashCode(); 
            }
        }
        return retval;
    }

    /**
     * Returns whether this category equals the given object  
     * up to variable names, using the given maps from vars to ints, 
     * ignoring the LFs (if any).
     */
    public boolean equalsNoLF(Object obj, TObjectIntHashMap varMap, TObjectIntHashMap varMap2) {
        if (obj.getClass() != this.getClass()) { return false; }
        AtomCat ac = (AtomCat) obj;
        if (_featStruc != null && ac._featStruc == null) { return false; }
        if (_featStruc == null && ac._featStruc != null) { return false; }
        if (!type.equals(ac.type)) { return false; }
        if (_featStruc != null) { 
            if (_featStruc instanceof GFeatStruc) {
                if (!((GFeatStruc)_featStruc).equals(ac._featStruc, varMap, varMap2)) { return false; }
            } else { // nb: would be nice to get rid of this case
                if (!_featStruc.equals(ac._featStruc)) { return false; }
            }
        }
        return true;
    }

    /**
     * Returns whether this category is a fragment category;
     * returns true iff the type of this cat is "frag".
     */
    public boolean isFragment() { return type.equals("frag"); }
}
