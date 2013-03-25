///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004 University of Edinburgh (Michael White)
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

package opennlp.ccg.util;

import java.util.*;
import java.lang.ref.*;

/**
 * A utility class for interning (canonicalizing) objects.
 * A WeakHashMap is used as the backing store, so that interned objects can
 * be garbage collected.
 * Normally, it is easier to use the globalIntern method (sharing a global 
 * backing store) than to allocate separate interners.
 * Individual interners can be constructed to use soft references to 
 * the interned objects, so that they are kept around longer than is the 
 * case with weak references (the default). 
 *
 * @author      Michael White
 * @version     $Revision: 1.5 $, $Date: 2005/10/13 20:33:49 $
 *
 */
public class Interner<T> {

    // the backing store
    private Map<T,Reference<T>> weakMap = new WeakHashMap<T,Reference<T>>();
    
    // flag for whether to use soft references 
    private boolean softRefs = false;
    
    /** Default constructor. */
    public Interner() {}

    /** Constructor with soft references flag. */
    public Interner(boolean softRefs) { this.softRefs = softRefs; }

    /** 
     * Returns a canonical version of the given object.
     * The returned object is .equals() to the given one.
     * If the given object is not equal to one already seen, 
     * then the returned object will be == to the given one.
     */
    public T intern(T obj) {
        // check if equivalent key already in map
        if (weakMap.containsKey(obj)) {
            // return existing canonical obj if so 
            Reference<T> ref = weakMap.get(obj);
            return ref.get();
        }
        // otherwise add this object to the map, wrapped in a 
        // weak/soft reference so that it can still be gc'ed
        Reference<T> ref = (softRefs) 
            ? new SoftReference<T>(obj) 
            : new WeakReference<T>(obj);
        weakMap.put(obj, ref);
        return obj;
    }
    
    /** 
     * Returns the canonical version of the given object, if any, 
     * otherwise returns null.
     */
    public T getInterned(T obj) {
        // get weak reference to canonical obj, if any
        Reference<T> ref = weakMap.get(obj);
        // return obj, if any, otherwise null
        return (ref != null) ? ref.get() : null;
    }
    
    /** Returns the number of interned objects. */
    public int size() {
        return weakMap.size();
    }
    
    
    // the global interner
    private static Interner<Object> globalInterner = null;
    
    /** 
     * Returns a canonical version of the given object using a global interner.
     * The returned object is .equals() to the given one.
     * If the given object is not equal to one already seen, 
     * then the returned object will be == to the given one.
     */
    public static Object globalIntern(Object obj) {
        if (globalInterner == null) globalInterner = new Interner<Object>();
        return globalInterner.intern(obj);
    }
    
    /** 
     * Returns the canonical version of the given object using the global interner, if any, 
     * otherwise returns null.
     */
    public static Object getGlobalInterned(Object obj) {
        if (globalInterner == null) return null;
        return globalInterner.getInterned(obj);
    }
    
    /** Returns the number of interned objects in the global interner. */
    public static int globalSize() {
        if (globalInterner == null) return 0;
        return globalInterner.size();
    }
    
    
    /** Tests the implementation. */
    public static void main(String[] args) {

        Interner<Integer> interner = new Interner<Integer>();
        int SIZE = 100000;
        Integer[] ints = new Integer[SIZE];

        System.out.println("Adding " + SIZE + " ints to interner.");
        for (int i = 0; i < SIZE; i++) {
            ints[i] = new Integer(i);
            Integer interned = interner.intern(ints[i]);
            if (interned != ints[i]) {
                System.out.println("Whoops: ints[" + i + "] not == to interned: " + interned);
                System.exit(-1);
            }
        }
        System.out.println("interner.size(): " + interner.size()); // should be SIZE
        System.out.println("Doing gc().");
        System.gc();
        System.out.println("interner.size(): " + interner.size()); // should be the same
        System.out.println();
        
        System.out.println("Now adding " + SIZE + " equivalent ints to interner.");
        for (int i = 0; i < SIZE; i++) {
            Integer intI = new Integer(i);
            Integer interned = interner.intern(intI);
            if (interned == intI) {
                System.out.println("Whoops: intI (i=" + i + ") is == to interned: " + interned);
                System.exit(-1);
            }
        }
        System.out.println("interner.size(): " + interner.size()); // should be the same
        System.out.println();
        
        System.out.println("Next adding " + SIZE + " new, unreferenced ints to interner.");
        for (int i = SIZE; i < SIZE*2; i++) {
            Integer intI = new Integer(i);
            Integer interned = interner.intern(intI);
            if (interned != intI) {
                System.out.println("Whoops: intI (i=" + i + ") not == to interned: " + interned);
                System.exit(-1);
            }
        }
        System.out.println("interner.size(): " + interner.size()); // should be larger than SIZE
        System.out.println("Doing gc().");
        System.gc();
        System.out.println("interner.size(): " + interner.size()); // should be back to SIZE
        System.out.println();
    }
}
