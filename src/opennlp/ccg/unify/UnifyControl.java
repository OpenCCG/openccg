///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003 Jason Baldridge and University of Edinburgh (Michael White)
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

import opennlp.ccg.synsem.*;
import opennlp.ccg.hylo.*;

import gnu.trove.*;
import java.util.*;

/**
 * Center of command for the unification process.  
 * Needs work to allow for multithreading. 
 *
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.6 $, $Date: 2005/10/20 17:30:30 $
 */
public class UnifyControl { 

    /**
     * An integer used to keep variables unique in lexical items.
     */
    private static int _varIndex = 0;

    /**
     * A function that makes variables unique.
     */
    private static ModFcn uniqueFcn = new ModFcn() {
        public void modify (Mutable m) {
            if (m instanceof Indexed && m instanceof Variable) {
                ((Indexed)m).setIndex(_varIndex);
            }
        }};


    /**
     * An integer used to keep feature structure indexes unique.
     */
    private static int _fsIndex = 1;
    private static TIntIntHashMap _reindexed = new TIntIntHashMap();

    private static CategoryFcn indexFcn = new CategoryFcnAdapter() {
        public void forall (Category c) {
            FeatureStructure fs = c.getFeatureStructure();
            if (fs != null) {
                int index = fs.getIndex();
                if (index > 0) {
                    int $index = _reindexed.get(index);
                    if ($index == 0) {
                        $index = _fsIndex++;
                        _reindexed.put(index, $index); 
                    }
                    fs.setIndex($index);
                }
            }
        }
    };

    /** Resets the uniqueness counters. */
    public static void startUnifySequence() {
        _varIndex = 0;
        _fsIndex = 1;
    }
    
    /** Sets the var and feature structure indices to unique values. */
    public static void reindex(Category cat) { 
        reindex(cat, null); 
    }

    /** Sets the var and feature structure indices to unique values. */
    public static void reindex(Category cat, Category anotherCat) {
        _reindexed.clear();
        cat.forall(indexFcn);
        cat.deepMap(uniqueFcn);
        if (cat != anotherCat && anotherCat != null) {
            anotherCat.forall(indexFcn);
            anotherCat.deepMap(uniqueFcn);
        }
        _varIndex++;
    }

    /** Returns a unique var index. */
    public static int getUniqueVarIndex() {
        return ++_varIndex;
    }

    /** Returns a unique feature structure index. */
    public static int getUniqueFeatureStructureIndex() {
        return ++_fsIndex;
    }
    
    /** Returns a copy of the given category, feature variable or feature structure, otherwise returns the same object. */
    public static Object copy(Object o) {
        if (o instanceof Category) {
            return ((Category)o).copy();
        } else if (o instanceof GFeatVar) {
            return ((GFeatVar)o).copy();
        } else if (o instanceof LF) {
            return ((LF)o).copy();
        } else if (o instanceof GFeatStruc) {
            return ((GFeatStruc)o).copy();
        } else {
            return o;
        }
    }
    
    private static CategoryFcn addIndexFcn = new CategoryFcnAdapter() {
        public void forall (Category c) {
            FeatureStructure fs = c.getFeatureStructure();
            if (fs == null) return;
            int index = fs.getIndex();
            if (index <= 0) fs.setIndex(++_fsIndex);
        }
    };

    /** Adds indices to any feature structures without one already. */
    public static void addIndices(Category cat) { cat.forall(addIndexFcn); }
    
    private static Set<String> attsToSave = new HashSet<String>();
    private static Map<String,Object> featsToSave = new HashMap<String,Object>();

    private static CategoryFcn removeAttsFcn = new CategoryFcnAdapter() {
        public void forall (Category c) {
            FeatureStructure fs = c.getFeatureStructure();
            if (fs == null) return;
            featsToSave.clear();
            for (String att: attsToSave) {
            	if (fs.hasAttribute(att)) featsToSave.put(att, fs.getValue(att));
            }
            fs.clear();
            for (String att : featsToSave.keySet()) 
            	fs.setFeature(att, featsToSave.get(att));
        }
    };

    /** Removes all features except those in given collection of attributes. */
    public static void removeFeatsExcept(Category cat, Collection<String> atts) {
    	attsToSave.clear();
    	attsToSave.addAll(atts);
    	cat.forall(removeAttsFcn);
    }

    private static CategoryFcn abstractNominalsFcn = new CategoryFcnAdapter() {
        public void forall (Category c) {
        	LF lf = c.getLF();
        	if (lf != null) c.setLF(HyloHelper.abstractNominals(lf));
            FeatureStructure fs = c.getFeatureStructure();
            if (fs == null) return;
            for (String att : fs.getAttributes()) {
            	Object val = fs.getValue(att);
            	if (val instanceof Nominal) {
            		Nominal var = HyloHelper.abstractNominal((Nominal)val);
            		if (var != val) fs.setFeature(att, var);
            	}
            }
        }
    };
    
    /** Abstracts nominal atoms, replacing them with corresponding nominal variables. */
    public static void abstractNominals(Category cat) {
    	cat.forall(abstractNominalsFcn);
    }
}

