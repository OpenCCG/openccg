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

public class PunctHelper {

	//The largest current id
	private int globalId = 0;

	//The store of ids
	//private Hashtable idTally = new Hashtable();

	private String feat = "";

	private String pos = "";

	private String balCom = "";

	//The pos-indexRel tally
	private static Hashtable<String,ArrayList<String>> indexRel = new Hashtable<String,ArrayList<String>>();

	//Calculate & store the indexRel
	public String calcIndexRel(String cat, String pos) {

		ArrayList<String> temp = new ArrayList<String>();
		pos = pos.replaceAll("[0-9]", "");

		//System.out.println(cat);

		if (!pos.equals("PUNCT_LPAREN")) {

			cat = cat.replaceAll("/\\*punct\\[,\\]_[0-9]", "");
			cat = cat.replaceAll("/\\*punct\\[--\\]_[0-9]", "");
			cat = cat.replaceAll("/\\*punct\\[-rrb-\\]_[0-9]", "");
			cat = cat.replaceAll("/\\*punct\\[-rcb-\\]_[0-9]", "");

		}

		//System.out.println(cat);

		if (!indexRel.containsKey(pos)) {
			temp.add(cat);
			indexRel.put(pos, temp);
		}

		temp = indexRel.get(pos);
		if (!temp.contains(cat))
			temp.add(cat);

		String ind = Integer.toString(temp.indexOf(cat) + 1);

		return ind;

	}

	//Initialization before start of a new conj rule 
	public String globalInit() {
		globalId = 0;
		//idTally.clear();
		return null;
	}

	//Function invoked by invertedDirSpComma.xsl
	public String getglobalId() {
		globalId++;
		return Integer.toString(globalId);
	}

	public String setglobalId(int x) {
		globalId = x;
		return null;
	}

	public String storePOS(String x) {
		pos = x;
		return null;
	}

	public String getPOS() {
		String retVal = pos;
		return retVal;
	}

	public String initPOS() {
		pos = "";
		return null;
	}

	public String balInit() {
		balCom = "";
		return null;
	}

	public String storeBal(String x) {
		balCom = x;
		return null;
	}

	public String getBal() {
		return balCom;
	}

	public String storeFeat(String x) {
		feat = x;
		return null;
	}

	public String getFeat() {
		return feat;
	}

	public String featInit() {
		feat = "";
		return null;
	}

	public String debugPrint(String x, String y) {
		System.out.println("Debug: " + x + " at " + y);
		return null;
	}

	public String removeFeats(String cat) {
		cat = cat.replaceAll("\\[[a-zA-Z]+\\]", "");
		//System.out.println("Debug: "+cat);
		return cat;
	}

	public String purgeCat(String cat) {
		cat = cat.replaceAll("\\[[a-zA-Z]+\\]", "");
		cat = cat.replaceAll("~", "");
		cat = cat.replaceAll("_[0-9]+", "");
		//System.out.println("Debug: "+cat);
		return cat;
	}

	public String purgeCat1(String cat) {
		cat = cat.toLowerCase();
		cat = cat.replaceAll("\\[[a-zA-Z]+\\]", "");
		cat = cat.replaceAll("~", "");
		cat = cat.replaceAll("_[0-9]+", "");
		//System.out.println("Debug: "+cat);
		return cat;
	}

	//Replace pp[] by pp
	public String cleanPP(String cat) {
		cat = cat.replaceAll("pp\\[\\]", "pp");
		return cat;
	}
}
