///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-4 Jason Baldridge and University of Edinburgh (Michael White)
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

package opennlp.ccg.hylo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import opennlp.ccg.grammar.*;
import opennlp.ccg.synsem.*;
import opennlp.ccg.unify.*;

import org.jdom.*;

/**
 * A hybrid logic nominal, an atomic formula which holds true at exactly one
 * point in a model.
 * The type is checked for compatibility during unification with nominal vars, 
 * but it is not updated, since nominal atoms are constants.
 * If no type is given, the TOP type is used for backwards compatibility.
 *
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.10 $, $Date: 2009/07/17 04:23:30 $
 **/
public class NominalAtom extends HyloAtom implements Nominal {

	private static final long serialVersionUID = -6002484920078196411L;
	
	protected boolean shared = false;
    
    public NominalAtom(String name) {
        this(name, null);
    }
    
    public NominalAtom(String name, SimpleType st) {
        this(name, st, false);
    }
    
    public NominalAtom(String name, SimpleType st, boolean shared) {
        super(name, st);
        type = (st != null) ? st : Grammar.theGrammar.types.getSimpleType(Types.TOP_TYPE);
        this.shared = shared;
    }

    public String getName() { return _name; }
    
    public boolean isShared() { return shared; }

    public void setShared(boolean shared) { this.shared = shared; }
    
    public LF copy() {
        return new NominalAtom(_name, type, shared);
    }

    /** Returns a hash code based on the atom name and type. */
    public int hashCode() { 
        return _name.hashCode() + type.hashCode();
    }

    /**
     * Returns whether this atom equals the given object based on the atom name and type.
     */
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        NominalAtom nom = (NominalAtom) obj;
        return type.equals(nom.type);
    }

    public Object unify(Object u, Substitution sub) throws UnifyFailure {
        if (equals(u)) return this;
        return super.unify(u, sub);
    }
    
    public int compareTo(Nominal nom) {
        if (nom instanceof NominalAtom) { 
            return super.compareTo((NominalAtom)nom);
        }
        int retval = _name.compareTo(nom.getName());
        if (retval == 0) { retval = -1; } // atom precedes var if names equal
        return retval;
    }
    
    public String toString() {
        String retval = _name;
        if (!type.getName().equals(Types.TOP_TYPE)) retval += ":" + type.getName();
        return retval;
    }
    
    /**
     * Returns an XML representation of this LF.
     */
    public Element toXml() {
        Element retval = new Element("nom");
        retval.setAttribute("name", toString());
        return retval;
    }
    
    /** Tests serialization. */
    public static void debugSerialization() throws IOException, ClassNotFoundException {
        // test serialization
        NominalAtom n = new NominalAtom("w1");
    	String filename = "tmp.ser";
    	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
    	System.out.println("Writing n: " + n);
    	out.writeObject(n);
    	out.close();
    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
    	System.out.print("Reading n2: ");
    	NominalAtom n2 = (NominalAtom) in.readObject();
    	System.out.println(n2);
    	in.close();
    	// test identity and equality
    	System.out.println("n == n2?: " + (n == n2));
    	System.out.println("n.equals(n2)?: " + (n.equals(n2)));
    }
}
