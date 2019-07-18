///////////////////////////////////////////////////////////////////////////////
//// Copyright (C) 2003-4 Gunes Erkan and University of Edinburgh (Michael White)
//// 
//// This library is free software; you can redistribute it and/or
//// modify it under the terms of the GNU Lesser General Public
//// License as published by the Free Software Foundation; either
//// version 2.1 of the License, or (at your option) any later version.
//// 
//// This library is distributed in the hope that it will be useful,
//// but WITHOUT ANY WARRANTY; without even the implied warranty of
//// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//// GNU Lesser General Public License for more details.
//// 
//// You should have received a copy of the GNU Lesser General Public
//// License along with this program; if not, write to the Free Software
//// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
////////////////////////////////////////////////////////////////////////////////

package opennlp.ccg.grammar;

import opennlp.ccg.util.*;
import opennlp.ccg.unify.*;

import org.jdom.*;
import org.jdom.input.*;

import java.io.*;
import java.net.*;
import java.util.*;

import gnu.trove.*;

/**
 * Class for constructing and holding the hierarchical simple type maps.
 *
 * @author  Gunes Erkan
 * @author  Michael White
 * @version $Revision: 1.13 $, $Date: 2009/12/21 03:27:18 $
 */
public class Types implements WithGrammar {

    public Grammar grammar;
    private final HashMap<String,SimpleType> nameToType = new HashMap<String,SimpleType>();
    private final ArrayList<SimpleType> indexToType = new ArrayList<SimpleType>();
    private int maxTypeIndex = 0;
    public static final String TOP_TYPE = "top";
    public static final String BOT_TYPE = "bottom";
	
    /** Constructor for an empty hierarchy (with just the top type). */
    public Types(){
    }

    public Types(Grammar grammar) {
        getSimpleType(TOP_TYPE);
        this.grammar = grammar;
    }

    /**
     * Constructs the type hierarchy from the given URL, for 
     * the given grammar.
     */
    @SuppressWarnings("unchecked")
	public Types(URL url, Grammar grammar) throws IOException {
        this.grammar = grammar;
        SAXBuilder builder = new SAXBuilder();
        Document doc;
        try {
            doc = builder.build(url);
        }
        catch (JDOMException exc) {
          getSimpleType(TOP_TYPE);
          throw (IOException) new IOException().initCause(exc);
        }
        List<Element> entries = doc.getRootElement().getChildren();
        readTypes(entries);
        // for debugging: print the indexToType list
        // printTypes();
    }

    public void setGrammar(Grammar grammar){
        this.grammar = grammar;
    }
    
    /** Returns the simple type with the given name, or a new one if none yet exists. */
    public SimpleType getSimpleType(String typeName) {
        SimpleType type = nameToType.get(typeName);
        if (type == null) {
            BitSet bs = new BitSet();
            bs.set(maxTypeIndex);
            SimpleType newtype = new SimpleType(maxTypeIndex, typeName, bs, this);
            nameToType.put(typeName, newtype);
            indexToType.add(newtype);
            nameToType.get(TOP_TYPE).getBitSet().set(maxTypeIndex++);
            return newtype;
        }
        else return type;
    }

    /** Returns whether there is a simple type with the given name. */
    public boolean containsSimpleType(String typeName) {
        return nameToType.containsKey(typeName);
    }
    
    /** Returns the list of types, with parents preceding children in the hierarchy. */
    public ArrayList<SimpleType> getIndexMap() {
        return indexToType;
    }


    /** Reads the rules and constructs the nameToType and indexToType maps. */
    public void readTypes(List<Element> _types) {
        
        GroupMap<String,String> hierarchy = new GroupMap<String,String>(); // map from types to all subtypes
        GroupMap<String,String> parents = new GroupMap<String,String>(); // map from types to parents
        TObjectIntHashMap depthMap = new TObjectIntHashMap(); // map from types to max depth

        // Construct the initial hierarchy of types without 
        // taking transitive closure.
        // Also store parents.
        for (int i=0; i < _types.size(); i++) {
            Element typeEl = _types.get(i);
            String typeName = typeEl.getAttributeValue("name");
            String _parents = typeEl.getAttributeValue("parents");
            hierarchy.put(typeName, BOT_TYPE);
            if (_parents == null) {
                hierarchy.put(TOP_TYPE, typeName);
                parents.put(typeName, TOP_TYPE);
            }
    		else {
    			String[] parentsArray = _parents.split("\\s+");
    			for (int j = 0; j < parentsArray.length; j++) {
    				hierarchy.put(parentsArray[j], typeName);
                    parents.put(typeName, parentsArray[j]);
    			}
    		}
    	}

        // Compute depth from parents.
        for (String type : parents.keySet()) {
            int depth = computeDepth(type, parents, type);
            depthMap.put(type, depth);
        }

    	// Compute ALL subtypes of each type and insert into the hierarchy.
    	for (String type : hierarchy.keySet()) { 
 		    hierarchy.putAll(type, findAllSubtypes(hierarchy, type));
 		}
        
    	// Assign a unique int to each type in breadth-first order.
    	// Then create the string -> SimpleType map.
    	createSimpleTypes(hierarchy, depthMap);
    }

    /** Returns the max depth of the given type, checking for cycles. */
    private static int computeDepth(String type, GroupMap<String,String> parents, String startType) {
        if (type.equals(TOP_TYPE)) return 0;
        int maxParentDepth = 0;
        Set<String> parentSet = parents.get(type);
	if (parentSet != null) {
	    for (String parent : parentSet) {
		if (parent.equals(startType)) {
		    throw new RuntimeException("Error, type hierarchy contains cycle from/to: " + startType);
		}
		int parentDepth = computeDepth(parent, parents, startType);
		maxParentDepth = Math.max(maxParentDepth, parentDepth);
	    }
	}
        return maxParentDepth + 1;
    }
    
    /** 
     * Computes the list of all sub-types of a given type (key) 
     * in depth-first order. 
     */
    private Collection<String> findAllSubtypes(GroupMap<String,String> hierarchy, String key) {
        ArrayList<String> subs = new ArrayList<String>();
        if (hierarchy.get(key) != null) {
      	    Stack<String> look = new Stack<String>();
	    for (String type : hierarchy.get(key)) {
		look.push(type);
	    }
            for (; !look.empty() ; ) {
                String new_sub = look.pop();
                subs.add(new_sub);
                if (hierarchy.get(new_sub) != null) {
                    for (String type : hierarchy.get(new_sub)) {
                        look.push(type);
                    }
                }
            }
        }
        return subs;
    }

    /** 
     * Creates the SimpleType objects and constructs the nameToType and indexToType maps. 
     */
    private void createSimpleTypes(GroupMap<String,String> hierarchy, TObjectIntHashMap depthMap) {
        
        // find max depth
        int maxDepth = 0;
        int[] depths = depthMap.getValues();
        for (int i = 0; i < depths.length; i++) {
            maxDepth = Math.max(maxDepth, depths[i]);
        }

        // add types in order of increasing depth
        ArrayList<String> typesVisited = new ArrayList<String>();
        typesVisited.add(TOP_TYPE);
        Object[] types = depthMap.keys();
        ArrayList<String> typesAtSameDepth = new ArrayList<String>();
        for (int i = 1; i <= maxDepth; i++) {
            typesAtSameDepth.clear();
            for (int j = 0; j < types.length; j++) {
                if (depthMap.get(types[j]) == i)
                    typesAtSameDepth.add((String)types[j]);
            }
            Collections.sort(typesAtSameDepth);
            typesVisited.addAll(typesAtSameDepth);
        }

        // construct the maps
        for (int i=0; i < typesVisited.size(); i++) {
            String typeName = typesVisited.get(i);
            BitSet bitset = new BitSet();
            bitset.set(i);
	    if (hierarchy.get(typeName) != null) {
		for (String type : hierarchy.get(typeName)) {
		    int indexToSet = typesVisited.indexOf(type); 
		    if (indexToSet != -1) bitset.set(indexToSet);
		}
	    }
            SimpleType st = new SimpleType(i, typeName, bitset, this);
            nameToType.put(typeName, st);
            indexToType.add(st);
        }
        maxTypeIndex = typesVisited.size();
    }
    
    /**
     * Prints the types and their subtypes to System.out.
     */
    public void printTypes() {
        System.out.println("types:");
        for (int i=0; i < indexToType.size(); i++) {
            SimpleType st = indexToType.get(i); 
            System.out.println(i + ": " + st.getName() + " subtypes: " + st.getBitSet());
        }
        System.out.println();
    }
    
    /** Tests serialization of simple types, including resolution. */
    public void debugSerialization() throws IOException, ClassNotFoundException {
        // test serialization
        SimpleType st = indexToType.get(1);
    	String filename = "tmp.ser";
    	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
    	System.out.println("Writing st: " + st.getIndex() + ": " + st + " " + st.getBitSet());
    	out.writeObject(st);
    	out.close();
    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
    	System.out.print("Reading st2: ");
    	SimpleType st2 = (SimpleType) in.readObject();
    	System.out.println(st2.getIndex() + ": " + st2 + " " + st2.getBitSet());
    	in.close();
    	// test identity (and thus readResolve)
    	System.out.println("st == st2?: " + (st == st2));
    }
}
