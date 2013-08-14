///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-7 Jason Baldridge, Gann Bierner, Michael White and Gunes Erkan
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

import opennlp.ccg.synsem.LF;
import opennlp.ccg.hylo.*;
import opennlp.ccg.grammar.*;
import gnu.trove.*;
import org.jdom.*;
import java.util.*;


/**
 * A feature structure for use with CCG categories.
 *
 * @author      Jason Baldridge
 * @author      Gann Bierner
 * @author      Michael White
 * @author      Gunes Erkan
 * @version     $Revision: 1.29 $, $Date: 2011/11/11 15:30:42 $
 */
public class GFeatStruc extends HashMap<String,Object> implements FeatureStructure {

	private static final long serialVersionUID = 1L;

	boolean _empty = true;
    int _index = 0;
    int _inheritsFrom = 0;
    
    public GFeatStruc() {
        super(3);
    }

    public GFeatStruc(int i) {
        super(i);
    }

    @SuppressWarnings("unchecked")
	public GFeatStruc(Element fsEl) {
        super(fsEl.getChildren().size());
        String index = fsEl.getAttributeValue("id");
        if (index != null) {
            _index = Integer.parseInt(index);
        }
        String inheritsFrom = fsEl.getAttributeValue("inheritsFrom");
        if (inheritsFrom != null) {
            _inheritsFrom = Integer.parseInt(inheritsFrom);
        }
        List<Element> feats = fsEl.getChildren();
        if (feats.size() == 0) {
            setFeature(fsEl);
        }
        else {
            for (Iterator<Element> featIt=feats.iterator(); featIt.hasNext();) {
                setFeature((Element)featIt.next());
            }
        }
    }
    
    public Element toXml() {
    	Element retval = new Element("fs");
    	if (_index > 0) retval.setAttribute("id", Integer.toString(_index));
    	if (_inheritsFrom > 0) retval.setAttribute("inheritsFrom", Integer.toString(_inheritsFrom));
    	List<String> keys = new ArrayList<String>(keySet()); 
		Collections.sort(keys);
    	if (size() == 1 && get(keys.get(0)) instanceof SimpleType) {
    		String attr = keys.get(0); SimpleType val = (SimpleType) get(attr);
    		retval.setAttribute("attr", attr);
    		retval.setAttribute("val", val.getName());
    	}
    	else {
    		for (String attr : keys) {
    			Element featElt = new Element("feat");
    			featElt.setAttribute("attr", attr);
    			retval.addContent(featElt);
    			Object val = get(attr);
    			if (val instanceof SimpleType) 
    				featElt.setAttribute("val", ((SimpleType) val).getName());
    			else {
    				if (val instanceof GFeatVar) {
    					GFeatVar var = (GFeatVar) val;
    					Element varElt = new Element("featvar");
    					featElt.addContent(varElt);
    					String name = var.name();
    					String typeName = var.getType().getName();
    					if (!typeName.equals(Types.TOP_TYPE)) name += ":" + typeName;
    					varElt.setAttribute("name", name);
    				}
    				else if (val instanceof LF)
    					featElt.addContent(HyloHelper.toXml((LF)val));
    				else 
    					throw new RuntimeException("Unsupported feature value type in constructing XML: " + val);
    			}
    		}
    	}
    	return retval;
    }

    public void deepMap(ModFcn mf) {
        for (Iterator<String> attributes=keySet().iterator(); attributes.hasNext();) {
            Object val1 = getValue(attributes.next());
            if (val1 instanceof Mutable) {
                ((Mutable)val1).deepMap(mf);
            }
        }
        mf.modify(this);
    }

    public void setFeature(String attribute, Object val) { 
        put(attribute, val);
        _empty = false;
    }

    private void setFeature(Element e) {
        String attr = e.getAttributeValue("attr");
        if (attr == null) attr = e.getAttributeValue("a");
        if (attr == null) {
            return;
        }
        String val = e.getAttributeValue("val");
        if (val == null) val = e.getAttributeValue("v");
        Object value;
        if (val != null) {
            value = Grammar.theGrammar.types.getSimpleType(val);
        } else {
            Element valEl = (Element)e.getChildren().get(0);
            if (valEl.getName().equals("featvar") || valEl.getName().equals("fvar")) {
                String[] name = valEl.getAttributeValue("name").split(":");
                if (name[0]==null) name = valEl.getAttributeValue("n").split(":",2);
                if (name.length<2) {
                    value = new GFeatVar(name[0]);
                }
                else
                    value = new GFeatVar(name[0], Grammar.theGrammar.types.getSimpleType(name[1]));
            } else {
                value = HyloHelper.getLF((Element)e.getChildren().get(0));
            }
        }
        setFeature(attr, value);
    }
    
    public Object getValue(String attribute) {
        return get(attribute);
    }

    public boolean hasAttribute(String attribute) {
        return containsKey(attribute);
    }

    public boolean attributeHasValue(String attribute, Object val) {
        return val.equals(getValue(attribute));
    }

    public Set<String> getAttributes() {
        return keySet();
    }
    
    public void clear() { 
        clear();
        _empty = true;
    }

    /** Returns true iff this feature structure has the same index and set of attr-val pairs. */
    public boolean equals(FeatureStructure fs) {
        if (!(fs instanceof GFeatStruc)) return false;
        GFeatStruc bfs = (GFeatStruc)fs;
        
        if (_index != bfs._index) return false; 

        if (size() != bfs.size()) return false;
        Set<String> atts1 = getAttributes();
        Set<String> atts2 = bfs.getAttributes();
        if (!atts1.containsAll(atts2)) return false;
        
        for (Iterator<String> it = atts1.iterator(); it.hasNext(); ) {
            String att = it.next();
            if (!getValue(att).equals(bfs.getValue(att))) return false;
        }
        
        return true;
    }
    
    /** Returns a hash code consistent with equals. */
    public int hashCode() { return super.hashCode() + _index; }

    public FeatureStructure copy() { 
        GFeatStruc $fs = new GFeatStruc(size());
        $fs.setIndex(_index);
        $fs._inheritsFrom = _inheritsFrom;
        for (Iterator<String> i=getAttributes().iterator(); i.hasNext();) {
            String a = i.next();
            $fs.setFeature(a, UnifyControl.copy(getValue(a)));
        }
        return $fs;
    }

    public boolean contains(FeatureStructure fs) { 
        if (size() < fs.size())
            return false;
        
        Set<String> atts1 = getAttributes();
        Set<String> atts2 = fs.getAttributes();
        if (atts1.containsAll(atts2)) {
            for (Iterator<String> i2 = atts2.iterator(); i2.hasNext();) {
                String a2 = i2.next();
                boolean foundA2 = false;
                for (Iterator<String> i1 = atts1.iterator(); !foundA2 && i1.hasNext();) {
                    String a1 = i1.next();
                    if (a1.equals(a2)) {
                        if (!getValue(a1).equals(fs.getValue(a2)))
                            return false;
                        foundA2 = true;
                    }
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

    public boolean occurs(Variable v) {
        for (Iterator<?> i = values().iterator(); i.hasNext();) {
            Object $_ = i.next();
            if ($_ instanceof Unifiable && ((Unifiable)$_).occurs(v))
                return true;
        }
        return false;
    }

    public void unifyCheck(Object u) throws UnifyFailure {
        if (!(u instanceof FeatureStructure)) {
            throw new UnifyFailure();
        }
        // look for incompatible string-valued features
        FeatureStructure fs2 = (FeatureStructure)u;
        Set<String> keys1 = getAttributes();
        for (Iterator<String> i1=keys1.iterator(); i1.hasNext();) {
            String k1 = i1.next();
            Object val1 = getValue(k1);
            if (!(val1 instanceof SimpleType)) continue;
            Object val2 = fs2.getValue(k1);
            if (!(val2 instanceof SimpleType)) continue;
            ((SimpleType)val1).unifyCheck(val2);
        }
    }

    public Object unify(Object u, Substitution sub) throws UnifyFailure { 

        if (!(u instanceof FeatureStructure)) {
            throw new UnifyFailure();
        } 

        FeatureStructure fs2 = (FeatureStructure)u;
        FeatureStructure $fs = new GFeatStruc(size());
        Set<String> keys1 = getAttributes();
        Set<String> keys2 = fs2.getAttributes();
        for (Iterator<String> i1=keys1.iterator(); i1.hasNext();) {
            String k1 = i1.next();
            Object val1 = getValue(k1);
            Object val2 = fs2.getValue(k1);
            if (val2 != null) {
                $fs.setFeature(k1, Unifier.unify(val1, val2, sub));
            }
            else {
                $fs.setFeature(k1, UnifyControl.copy(val1));
            }
        }
        for (Iterator<String> i2=keys2.iterator(); i2.hasNext();) {
            String k2 = i2.next();
            if (!keys1.contains(k2))
                $fs.setFeature(k2, UnifyControl.copy(fs2.getValue(k2)));
        }

        int fs2Index = fs2.getIndex();
        int newIndex = 0;
        if (_index == 0) {
            newIndex = fs2Index;
        } else if (fs2Index == 0) {
            newIndex = _index;
        } else if (sub instanceof GSubstitution) {
            newIndex = ((GSubstitution)sub).makeNewIndex(_index, fs2Index);
        }
        $fs.setIndex(newIndex);

        if (sub instanceof GSubstitution && newIndex > 0) {
            ((GSubstitution)sub).addIndexedObject(newIndex,$fs);
        }
        return $fs;
    }

    public Object fill(Substitution sub) throws UnifyFailure {
        FeatureStructure $fs = copy();
        for (Iterator<String> i = $fs.getAttributes().iterator(); i.hasNext();) {
            String a = i.next();
            Object value = getValue(a);
            if (value instanceof Variable) {
                Object varVal = sub.getValue((Variable)value);
                if (null != varVal) {
                    $fs.setFeature(a, Unifier.unify(value,varVal,sub));
                }
            }
        }
        if (_index > 0 && sub instanceof GSubstitution) {
            FeatureStructure otherVals =
                (FeatureStructure)((GSubstitution)sub).getIndexedObject(_index);
            if (null != otherVals) {
                if (!$fs.equals(otherVals)) {
                    $fs = (FeatureStructure)$fs.unify(otherVals, sub);
                    $fs.setIndex(otherVals.getIndex());
                }
            }
        }
        return $fs;
    }

    public FeatureStructure inherit(FeatureStructure fs) { 
        FeatureStructure $fs = copy();
        for (Iterator<String> i = fs.getAttributes().iterator(); i.hasNext();) {
            String a = i.next();
            $fs.setFeature(a, UnifyControl.copy(fs.getValue(a)));
        }
        return $fs;
    }

    public int getIndex() {
        return _index;
    }

    public void setIndex(int index) {
        _index = index;
    }

    public int getInheritsFrom() {
        return _inheritsFrom;
    }

    private void addFeatureString(String attribute, StringBuffer sb) {
        Object val = getValue(attribute);
        sb.append(attribute).append('=').append(val.toString());
    }
    
    public String toString() {

        // if (_empty) return "";

        StringBuffer sb = new StringBuffer(size()*4);
 
        if (_index > 0) {
            sb.append('<'); sb.append(_index); sb.append('>');
        }
        
        if (_empty) return sb.toString();

        String featsToShow = Grammar.theGrammar.prefs.featsToShow;

        sb.append('{');

        List<String> filteredKeys = new ArrayList<String>(size());
        if (featsToShow.length() == 0) {
            filteredKeys.addAll(keySet());
        }
        else {
            for (Iterator<String> it = keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                if (featsToShow.indexOf(key) != -1) 
                    filteredKeys.add(key);
            }
        }
        String[] keys = new String[filteredKeys.size()];
        filteredKeys.toArray(keys);
        Arrays.sort(keys);
        
        for (int i=0; i < keys.length; i++) {
            addFeatureString(keys[i], sb);
            if (i < keys.length - 1) sb.append(", ");
        }
        
        sb.append('}');
        
        return sb.toString();
    }

    /**
     * Returns the supertag info for this feature structure.
     * In particular, returns the values of any non-variable 
     * features of interest, within square brackets.
     * The features of interest are configurable 
     * at the grammar level.
     */
    public String getSupertagInfo() {
        if (_empty) return "";
        StringBuffer sb = new StringBuffer();
        ArrayList<String> attrs = new ArrayList<String>(getAttributes());
        Collections.sort(attrs);
        Set<String> supertagFeatures = Grammar.theGrammar.supertagFeatures;
        for (int i = 0; i < attrs.size(); i++) {
            String attr = attrs.get(i);
            if (!supertagFeatures.contains(attr)) continue;
            Object val = getValue(attr);
            if (val instanceof Variable) continue;
            String s = val.toString();
            if (s.equals("+") || s.equals("-")) s = s + attr;
            sb.append('[').append(s).append(']');
        }
        return sb.toString();
    }
    
    private void addFeatureTeX(String attribute, StringBuffer sb) {
        Object val = getValue(attribute);
        String s = cleanText(val.toString());
        if (s.equals("+") || s.equals("-")) s = attribute + s;
        sb.append(" ").append(s);
    }
    
    // makes sure every special character is handled correctly in LaTeX
    private String cleanText(String s) {
        String str = s;
        try {
            //order matters!!
            str = str.replaceAll("\\\\", " \\\\\\backslash ");
            str = str.replaceAll("\\{", " \\\\\\{ ");
            str = str.replaceAll("\\}", " \\\\\\} ");
            str = str.replaceAll("\\$", " \\\\\\$ ");
            str = str.replaceAll("\\#", " \\\\\\# ");
            str = str.replaceAll("\\%", " \\\\\\% ");
            str = str.replaceAll("\\&", " \\\\\\& ");
            str = str.replaceAll("\\~", "  \\\\\\tilde\\{\\} ");
            str = str.replaceAll("\\_", " \\\\\\_ ");
            str = str.replaceAll("\\^", "  \\\\\\hat\\{\\} ");
        }
        catch (Exception e) {
            System.out.println("Error while evaluating RegExp: " + e.toString());
        }
        return str;
    }
    
    public String toTeX() {
        StringBuffer sb = new StringBuffer();
        if ((_index > 0)&&(_empty)) {
            sb.append(" \\subsf{ < "); sb.append(_index); sb.append(" > } ");
        }
        if (_empty) return sb.toString();
        String featsToShow = Grammar.theGrammar.prefs.featsToShow;
        sb.append(" \\subsf{ ");
        if ((_index > 0)) {
            sb.append("  < "); sb.append(_index); sb.append(" > ");
        }
        List<String> filteredKeys = new ArrayList<String>(size());
        if (featsToShow.length() == 0) {
            filteredKeys.addAll(keySet());
        }
        else {
            for (Iterator<String> it = keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                if (featsToShow.indexOf(key) != -1) 
                    filteredKeys.add(key);
            }
        }
        String[] keys = new String[filteredKeys.size()];
        filteredKeys.toArray(keys);
        Arrays.sort(keys);
        for (int i=0; i < keys.length; i++) {
            addFeatureTeX(keys[i], sb);
            if (i < keys.length - 1) sb.append(" , ");
        }
        sb.append(" } ");
        return sb.toString();
    }
    
    
    /**
     * Returns a hash code using the given map from vars to ints, 
     * to allow for equivalence up to variable names.
     */
    public int hashCode(TObjectIntHashMap varMap) {

        int retval = 0;
        
        // nb: treat index as a regular var
        if (_index != 0) {
    		// see if index already in map
    		if (varMap.containsKey(_index))
    			retval = varMap.get(_index);
    		// otherwise add it
    		else {
	    		int next = varMap.size() + 1;
	    		varMap.put(_index, next);
	    		retval = next;
    		}
        }
        // otherwise treat missing index as unique, keyed to negative identity hash
		else {
    		int next = varMap.size() + 1;
    		varMap.put(-1 * Math.abs(System.identityHashCode(this)), next);
    		retval = next;
		}
        
        if (_empty) { return retval; }

        // sort keys
        Set<String> keySet = keySet();
        String[] keys = new String[keySet.size()];
        keySet.toArray(keys);
        Arrays.sort(keys);
        
        // do each key
        for (int i=0; i<keys.length; i++) {
            retval += keys[i].hashCode();
            Object val = getValue(keys[i]);
            // use map for vars
            if (val instanceof Variable) retval += ((Variable)val).hashCode(varMap);
            // otherwise just hash code
            else retval += val.hashCode();
        }
        
        return retval;
    }
    
    /**
     * Returns whether this feature structure equals the given object  
     * up to variable names, using the given maps from vars to ints.
     */
    public boolean equals(Object obj, TObjectIntHashMap varMap, TObjectIntHashMap varMap2) {
        if (obj.getClass() != this.getClass()) { return false; }
        GFeatStruc fs = (GFeatStruc) obj;
        
        int mappedIndex = (_index != 0) ? varMap.get(_index) : varMap.get(-1 * Math.abs(System.identityHashCode(this)));
        int fsMappedIndex = (fs._index != 0) ? varMap2.get(fs._index) : varMap2.get(-1 * Math.abs(System.identityHashCode(fs)));
        if (mappedIndex != fsMappedIndex) return false;
        
        if (size() != fs.size()) return false;
        Set<String> atts1 = getAttributes();
        Set<String> atts2 = fs.getAttributes();
        if (!atts1.containsAll(atts2)) return false;
        
        for (Iterator<String> it = atts1.iterator(); it.hasNext(); ) {
            String att = it.next();
            Object val = getValue(att);
            Object val2 = fs.getValue(att);
            if (val instanceof Variable && val2 instanceof Variable) {
            	if (!((Variable)val).equals(val2, varMap, varMap2)) return false;
            }
            else {
                if (!val.equals(val2)) return false;
            }
        }
        
        return true;
    }
}
