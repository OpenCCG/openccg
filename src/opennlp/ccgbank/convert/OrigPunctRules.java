///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2005-2009 Scott Martin, Rajakrishan Rajkumar and Michael White
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

//Class which stores the context of punctuations

package opennlp.ccgbank.convert;

import java.util.Hashtable;

public class OrigPunctRules {

	//Sentence id
	private String id = "";

	//The largest current id
	private static int globalId = 0;

	//ccgbank section
	private String sect="";

	//Label for a punct mark
	private String label = "";

	//The store of ids
	private static Hashtable<String,String> idTally = new Hashtable<String,String>();

	//Proc which traps, stores id of each sentence in a global var
	public String storeId(String x) {
		id = "";
		sect = "";
		//System.out.println(x+" "+"Raja");
		String expId[] = x.split("_");
		id = expId[1];
		sect = id.substring(0, 2);
		if (sect.length() < 0) sect += "just avoiding a warning here"; 
		//System.out.println(sect+" "+"Raja");
		return null;
	}

	//Initialization before start of a new binary rule 
	public String initId() {
		globalId = 0;
		idTally.clear();
		return null;
	}

	//Id allotment
	public String allotId(String cat) {
		String newId = "";
		//Id allotment
		if (!idTally.containsKey(cat)) {
			globalId++;
			newId = Integer.toString(globalId);
			idTally.put(cat, newId);
		}
		newId = (String) idTally.get(cat);
		return newId;
	}

	public String storeLabel(String x) {
		label = x;
		return null;
	}

	public String getLabel() {
		return label;
	}

	public String initLabel() {
		label = "";
		return null;
	}
}
