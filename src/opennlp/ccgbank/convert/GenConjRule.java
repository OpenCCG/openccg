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

//Java class which adds brackets, stores arg1 position for inferConjRules.xsl,normConjRulesId, normTreenodeId.xsl

package opennlp.ccgbank.convert;

import java.util.ArrayList;
import java.util.Hashtable;
//import java.util.StringTokenizer;

public class GenConjRule {

	//The largest current id
	private static int globalId = 0;

	//The store of ids
	private static Hashtable<String,String> idTally = new Hashtable<String,String>();

	//Dollar status of res, arg1 & arg2
	private static ArrayList<String> dollarStatus = new ArrayList<String>();

	//Final result of dollar status calcs
	private static String ds = "No_Dollar";

	//Add brackets to complex categories
	public String addParen(String str) {

		if (str.contains("\\") || str.contains("/"))
			str = "(" + str + ")";
		return str;

	}

	//Add _conj to the result category
	public String modRes(String str) {
		//StringTokenizer st = new StringTokenizer(str, "[]");
		str = str + "_conj";
		return str;
	}

	//Procedure which cleans the unary rule result
	public String getConjRes(String x) {
		x = x.replaceAll("_conj", "");
		x = x.replaceAll("_[0-9]", "");
		//System.out.println(x);
		return x;
	}

	//Normalizes id of input category
	public String normId(String oldId, String oldInhId, String cat) {

		String newId = "";
		//int choice = 0;

		//Switch for usage between Treenode & Leafnode Id normalization 
		if (oldId.length() > 0)
			cat = cat + "_" + oldId;
		if (oldInhId.length() > 0)
			cat = cat + "_" + oldInhId;

		cat = cat.trim();

		//Normalization condition
		if (!idTally.containsKey(cat)) {
			globalId++;
			newId = Integer.toString(globalId);
			idTally.put(cat, newId);
		}

		newId = (String) idTally.get(cat);

		return newId;
	}

	//Initialization of idTally & globalId before start of a new conj rule 
	public String globalInit() {
		globalId = 0;
		idTally.clear();
		return null;
	}

	//Initialization of idTally before each of Result,arg1 & arg2 is added 
	public String localInit() {
		idTally.clear();
		return null;
	}

	//Calculation of dollarStatus before start of a new conj rule 

	//Initialization of dollarStatus before start of a new conj rule 
	public String dsInit() {
		//System.out.println(dollarStatus);
		dollarStatus.clear();
		ds = "No_Dollar";
		return null;
	}

	//Store dollar status of res, arg1 & arg2
	public String storeDollarStatus(String type) {
		type = type.trim();
		dollarStatus.add(type);
		/*System.out.println('\n');
		 System.out.println("Insertion of: "+type);
		 System.out.println(dollarStatus);
		 System.out.println('\n');*/
		return "null";
	}

	public String dsCalc() {

		//System.out.println(dollarStatus);
		if (dollarStatus.size() == 3)
			ds = "Dollar";

		/*System.out.println('\n');
		 System.out.println("Retrieval");
		 System.out.println(dollarStatus);
		 System.out.println('\n');*/

		return null;
	}

	//Get dollar status of conjunct
	public String getDollarStatus() {
		return ds;
	}

	//Function invoked by invertedDirSpComma.xsl
	public String getglobalId() {
		globalId++;
		return Integer.toString(globalId);
	}
}
