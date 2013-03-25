///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-4 University of Edinburgh (Michael White)
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

package opennlp.ccg.realize;

import opennlp.ccg.lexicon.*;
import opennlp.ccg.unify.*;
import opennlp.ccg.synsem.*;
import opennlp.ccg.hylo.*;

import java.util.*;

/**
 * The feature licenser is a helper class for the edge factory, 
 * responsible for managing features which license the use and 
 * instantiation of semantically null or marked categories.
 *
 * @author      Michael White
 * @version     $Revision: 1.13 $, $Date: 2009/12/21 03:27:18 $
 */
public class FeatureLicenser
{

    // the edge factory for which this feature licenser is a helper
    private final EdgeFactory edgeFactory;
    
    // the licensing features     
    private final LicensingFeature[] licensingFeatures;
    
    /** Constructor. */
    public FeatureLicenser(EdgeFactory edgeFactory) { 
        this.edgeFactory = edgeFactory; 
        this.licensingFeatures = edgeFactory.grammar.lexicon.getLicensingFeatures();
    }

    /** Constructor with licensing features. */
    public FeatureLicenser(EdgeFactory edgeFactory, LicensingFeature[] licensingFeatures) { 
        this.edgeFactory = edgeFactory; 
        this.licensingFeatures = licensingFeatures;
    }

    
    //-----------------------------------------------------------------
    // semantically null word indexing

    /**
     * Adds new nominal atoms for subcategorized semantically null words.  
     * A check is made for atomic categories with a value for the 'lex' 
     * feature but with a null or uninstantiated index feature.  If found, a new nominal atom is 
     * created as the value of the index feature, and the nominal is 
     * added to the edge factory's nominals map, for indexing purposes. 
     * The same nominal is reused for repeated occurrences of a 'lex' value.
     */
    public void indexSemanticallyNullWords(Category cat) {
        cat.forall(semanticallyNullWordIndexer);
    }
    
    // counter
    private int wordCounter = 0;
    
    // 'lex' value to index map
    private Map<String, String> wordIndexMap = new HashMap<String, String>();
    
    // cat function
    private CategoryFcn semanticallyNullWordIndexer = new CategoryFcnAdapter() {
        public void forall(Category c) {
            if (!(c instanceof AtomCat)) return;
            FeatureStructure fs = c.getFeatureStructure();
            if (fs == null) return;
            if (!fs.hasAttribute("lex")) return;
            Object indexVal = fs.getValue("index"); 
            if (indexVal == null || (indexVal instanceof NominalVar)) {
                String lexVal = fs.getValue("lex").toString();
                String index = wordIndexMap.get(lexVal);
                NominalAtom nom;
                if (index == null) {
                    do {
                        index = "w" + ++wordCounter;
                        nom = new NominalAtom(index);
                    } while (edgeFactory.nominals.containsKey(nom));
                    wordIndexMap.put(lexVal, index);
                    edgeFactory.nominals.put(nom, edgeFactory.nominals.size());
                }
                else nom = new NominalAtom(index);
                fs.setFeature("index", nom);
            }
        }
    };
    

    //-----------------------------------------------------------------
    // feature map  
    //
    
    /**
     * Updates the licensed feature map with the info from the given initial category.
     */
    public void updateFeatureMap(Category cat) {
        currentFeatureMap = featureMap; 
        cat.forall(featureMapUpdater);
        currentFeatureMap = null;
    }
    
    // updates the category-specific licensed feature map
    private void updateCatFeatureMap(Category cat) {
        catFeatureMap.clear(); 
        currentFeatureMap = catFeatureMap; 
        cat.forall(featureMapUpdater);
        currentFeatureMap = null;
    }
    
    // a map from an attr name to a map from vals to sets of atomic categories 
    // containing those attr-val pairs
    private Map<String, Map<String, Set<Category>>> featureMap = new HashMap<String, Map<String, Set<Category>>>();
    
    // a feature map for a specific category to be checked
    private Map<String, Map<String, Set<Category>>> catFeatureMap = new HashMap<String, Map<String, Set<Category>>>();
    
    // working feature map
    private Map<String, Map<String, Set<Category>>> currentFeatureMap = null; 
    
    // list of all initial atom cats checked for main feature map
    private List<Category> allInitialAtomCats = new ArrayList<Category>();
    
    // feature map updater    
    private CategoryFcn featureMapUpdater = new CategoryFcnAdapter() {
        public void forall(Category c) {
            if (!(c instanceof AtomCat)) return;
            if (currentFeatureMap == featureMap) allInitialAtomCats.add(c);
            FeatureStructure fs = c.getFeatureStructure();
            if (fs == null) return;
            // for each feature
            for (int i = 0; i < licensingFeatures.length; i++) {
                String attr = licensingFeatures[i].attr; 
                Object val = fs.getValue(attr);
                if (val != null && !(val instanceof Variable)) {
                    // check for relevant value
                    String valStr = val.toString(); 
                    String fVal = licensingFeatures[i].val;
                    List<String> alsoList = licensingFeatures[i].alsoLicensedBy;
                    if (fVal != null && !fVal.equals(valStr) && !alsoList.contains(valStr)) continue;
                    // add to feature map
                    Map<String, Set<Category>> valMap = currentFeatureMap.get(attr); 
                    if (valMap == null) {
                        valMap = new HashMap<String, Set<Category>>(); 
                        currentFeatureMap.put(attr, valMap);
                    }
                    Set<Category> acSet = valMap.get(valStr);
                    if (acSet == null) {
                        acSet = new HashSet<Category>();
                        valMap.put(valStr, acSet);
                    }
                    acSet.add(c);
                }
            }
        }
    };

    
    //-----------------------------------------------------------------
    // category licensing 
    //
    
    /**
     * Returns whether the given category contains a feature indicating that 
     * it needs to be licensed.
     */
    public boolean needsLicensing(Category cat) {
        return checkLicensing(cat, true);
    }

    /**
     * Returns whether the given category is licensed according to 
     * the current feature map.
     */
    public boolean isLicensed(Category cat) {
        return checkLicensing(cat, false);
    }

    // records the licensing feature which succeeded in licensing the last cat (or null if none)
    private LicensingFeature currentLicensingFeature = null; 
    
    // checks the given category according to the given flag, 
    // updating currentLicensingFeature
    private boolean checkLicensing(Category cat, boolean needsLicensing) {
        currentLicensingFeature = null;
        boolean emptyCat = (cat.getLF() == null);
        // set up cat feature map
        updateCatFeatureMap(cat);
        Category target = getTarget(cat);
        // for each feature, look for appropriate attr-val pairs
        for (int i = 0; i < licensingFeatures.length; i++) {
            // skip when appropriate license flag not set
            if (emptyCat && !licensingFeatures[i].licenseEmptyCats) continue; 
            if (!emptyCat && !licensingFeatures[i].licenseMarkedCats) continue;
            String attr = licensingFeatures[i].attr;
            Map<String, Set<Category>> valMap = catFeatureMap.get(attr);
            if (valMap == null) continue;
            String fVal = licensingFeatures[i].val;
            Collection<String> vals;
            if (fVal != null) {
                if (!valMap.containsKey(fVal)) continue;
                vals = new ArrayList<String>(1);
                vals.add(fVal);
            } else {
                vals = valMap.keySet();
            }
            byte loc = licensingFeatures[i].loc;
            // for each attr-val pair
            for (Iterator<String> it = vals.iterator(); it.hasNext(); ) {
                String val = it.next();
                Set<Category> atomCats = valMap.get(val); 
                // check loc
                if (loc == LicensingFeature.TARGET_ONLY) {
                    if (atomCats.size() != 1) continue;
                    if (!atomCats.contains(target)) continue;
                } else if (loc == LicensingFeature.ARGS_ONLY) {
                    if (atomCats.contains(target)) continue;
                }
                // branch on needs-licensing flag
                if (needsLicensing) {
                    // found a feature needing to be licensed
                    return true; 
                }
                else {
                    // check for licensing feature in feature map
                    Map<String, Set<Category>> fmValMap = featureMap.get(attr);
                    // return false if not found
                    if (fmValMap == null) return false;
                    boolean foundLicensingVal = fmValMap.containsKey(val);
                    if (!foundLicensingVal) {
                        List<String> alsoList = licensingFeatures[i].alsoLicensedBy;
                        for (int j = 0; j < alsoList.size(); j++) {
                            if (fmValMap.containsKey(alsoList.get(j))) { 
                                foundLicensingVal = true; break;
                            }
                        }
                    }
                    if (!foundLicensingVal) return false;   
                    // otherwise record licensing feature and return true
                    currentLicensingFeature = licensingFeatures[i]; 
                    return true;
                }
            }
        }
        // otherwise false
        return false;
    }

    // returns the target cat, if complex,  otherwise 
    // just the cat itself    
    private Category getTarget(Category cat) {
        Category target = cat;
        if (cat instanceof ComplexCat) { target = ((ComplexCat)cat).getTarget(); }
        return target;
    }

    
    //-----------------------------------------------------------------
    // empty (semantically null) category licensing and instantiation  
    //
    
    // reusable simple substitution for instantiating vars on atom cats
    private SimpleSubstitution simpleSubst = new SimpleSubstitution();

    /**
     * Determines whether the given semantically null category 
     * is licensed according to the licensed feature map, and if so, returns 
     * appropriately (un-)instantiated versions of the category. 
     * The licensing features are checked in priority order.
     * NB: Instantiation is limited to the case where there is a single 
     *     value for the operative licensing feature.
     */
    public void licenseEmptyCat(Category cat, Set<Category> instantiatedCats, Set<Category> uninstantiatedCats) {
        // reindex
        UnifyControl.reindex(cat);
        // return cat uninstantiated if no licensing features found
        if (!needsLicensing(cat)) {
            uninstantiatedCats.add(cat); return; 
        }
        // return nothing if not licensed
        if (!isLicensed(cat)) return;
        // find operative licensing feature, if necessary
        if (currentLicensingFeature == null) {
            for (int i = 0; i < licensingFeatures.length; i++) {
                if (!catFeatureMap.containsKey(licensingFeatures[i].attr)) continue;
                Map<String, Set<Category>> valMap = catFeatureMap.get(licensingFeatures[i].attr);
                String fVal = licensingFeatures[i].val;
                if (fVal != null && !valMap.containsKey(fVal)) continue;
                currentLicensingFeature = licensingFeatures[i]; 
                break;
            }
            // if still not found, return cat uninstantiated
            if (currentLicensingFeature == null) {
                uninstantiatedCats.add(cat); 
                return; 
            }
        }
        // return cat uninstantiated if licensing feature does not 
        // have instantiation flag set
        if (!currentLicensingFeature.instantiate) {
            uninstantiatedCats.add(cat); return; 
        }
        // return cat uninstantiated if licensing feature has more than one val
        String attr = currentLicensingFeature.attr;
        Map<String, Set<Category>> valMap = catFeatureMap.get(attr);
        if (valMap.size() > 1) {
            uninstantiatedCats.add(cat); return; 
        }
        String val = valMap.keySet().iterator().next();
        Set<Category> atomCats = valMap.get(val);
        // for each atom cat, go ahead with instantiation ...
        for (Iterator<Category> acIt = atomCats.iterator(); acIt.hasNext(); ) {
            Category ac = acIt.next(); 
            // ensure cats with lex feature have an index var
            FeatureStructure fs = ac.getFeatureStructure();
            if (fs.hasAttribute("lex") && !fs.hasAttribute("index")) {
                fs.setFeature("index", new NominalVar("W"));
                UnifyControl.reindex(ac);
            }
            // unify with appropriate initial cats
            Collection<Category> initialCats = null; 
            if (!currentLicensingFeature.licenseEmptyCats) 
                initialCats = allInitialAtomCats;
            else { 
                Map<String, Set<Category>> fmValMap = featureMap.get(attr);
                initialCats = fmValMap.get(val);
                List<String> alsoList = currentLicensingFeature.alsoLicensedBy;
                if (alsoList.size() > 0) { 
                    if (initialCats != null) initialCats = new HashSet<Category>(initialCats);
                    else initialCats = new HashSet<Category>();
                    for (int i = 0; i < alsoList.size(); i++) {
                        Set<Category> alsoSet = fmValMap.get(alsoList.get(i));
                        if (alsoSet != null) initialCats.addAll(alsoSet);
                    }
                }
            }
            if (initialCats == null) {
                System.out.println("Warning, unable to find initial cats for feature " + attr + "=" + val); 
                uninstantiatedCats.add(cat); return; 
            }
            // for each initial cat
            for (Iterator<Category> it = initialCats.iterator(); it.hasNext(); ) {
                Category initialAC = it.next();
                // ensure index instantiated
                FeatureStructure initialFS = initialAC.getFeatureStructure();
                if (initialFS == null) continue;
                Object index = initialFS.getValue("index"); 
                if (!(index instanceof NominalAtom)) continue;
                // block instantiation with bound vars
                if (edgeFactory.boundVarNominals.contains(index)) {
                    instantiatedCats.clear();
                    uninstantiatedCats.add(cat); 
                    return; 
                }
                // try unifying index ...
                simpleSubst.clear(); 
                try {
                    Unifier.unify(ac.getFeatureStructure(), initialFS, simpleSubst);
                    // ensure substitution contains index
                    if (!simpleSubst.containsValue(index)) continue;
                    // get rid of other substitutions
                    for (Iterator<?> it2 = simpleSubst.values().iterator(); it2.hasNext(); ) {
                        if (!it2.next().equals(index)) it2.remove();
                    }
                    // instantiate
                    Category instCat = (Category) cat.fill(simpleSubst);
                    // and add instantiated cats
                    instantiatedCats.add(instCat);
                }
                catch (UnifyFailure uf) {}
            }
        }
    }
}
    
