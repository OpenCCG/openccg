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

//Class which stores info about punctuations
package opennlp.ccgbank.convert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ApposTally {

	//Operation performed
	//private String oper = "";

	//Dest dir
	//private String destDir = "";

	//Sentence id
	private String id = "";

	//Context of a comma 
	//private String cont = "";

	//Status whether comma is balanced or not
	private String balStatus = "";

	//Intervening lexical mtl
	private String lexMtl = "";

	//Cue analysis result
	//private String cueResult = "";

	//Features for appos vs conj identification
	private ArrayList<String> featInfo = new ArrayList<String>();

	//Heads of np
	private ArrayList<String> headInfo1 = new ArrayList<String>();

	//Heads of appositive
	private ArrayList<String> headInfo2 = new ArrayList<String>();

	//Cues
	private static ArrayList<String> cueList = new ArrayList<String>();

	//Proc which opens the cue file
	public void openCueFile(String fileName) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line = "";

		while ((line = br.readLine()) != null) {
			if (!cueList.contains(line))
				cueList.add(line);
			//System.out.println(line);
		}
		br.close();
	}

	//Proc which traps, stores id of each sentence in a global var 
	public String storeId(String x) {
		id = "";
		id = x;
		//System.out.println(id);
		return null;
	}

	//Proc which reinitializes all vars when a new comma is encountered
	public void flushVars() {
		balStatus = "conj";
		lexMtl = "";
		headInfo1.clear();
		headInfo2.clear();
		//cueResult = "";
		featInfo.clear();
	}

	//Proc which stores whether comma is balanced
	public void storeBalance(String status) {

		balStatus = status;

	}

	//Proc which returns balance status
	public String getBalance() {

		return balStatus;

	}

	//Proc which returns capitalized string of balance status
	public String getCaps(String x) {

		return x.toUpperCase();

	}

	//Proc which stores intervening lexical mtl
	public void storeLex(String word, String pos) {

		String info = "";

		if (pos.equals("X"))
			info = word;
		else
			info = word + "/" + pos;

		lexMtl = lexMtl + " " + info;

	}

	//Proc which prints intervening lexical mtl
	public void printLex() {

		lexMtl = id + " " + lexMtl;
		//System.out.println(lexMtl+'\n');
		lexMtl = "";
	}

	//Proc which stores head of np1
	public void storeHead(String word, String pos, int npNo) {

		String info = "";

		//Eliding the distinction b/w sing&plural nouns
		if (pos.equals("NNS"))
			pos = "NN";
		if (pos.equals("NNPS"))
			pos = "NNP";

		info = word + "/" + pos;

		if (npNo == 1)
			headInfo1.add(info);
		else
			headInfo2.add(info);

	}

	//Heuristic2: Cue based analysis
	public String cueAnalysis() {

		//String[] np = lexMtl.split("X");
		String[] sent = lexMtl.split("X");

		//Sift out possessed in genitive constr
		if (sent[0].contains("'s/POS") && headInfo1.size() > 0)
			headInfo1.remove(0);
		if (sent[1].contains("'s/POS") && headInfo2.size() > 0)
			headInfo2.remove(0);

		String res = "";

		int flag = 0;

		String np1head[] = new String[2];

		if (headInfo1.size() == 0 || headInfo2.size() == 0) {
			//System.out.println(id+": "+lexMtl+'\n');
			headInfo1.add("X1/ECK");
			np1head = (headInfo1.get(headInfo1.size() - 1)).split("/");

		} else
			np1head = (headInfo1.get(headInfo1.size() - 1)).split("/");

		//Comparing the heads of np1 & np2
		for (String x : headInfo2) {
			String[] np2head = x.split("/");
			if (np2head[1].equals(np1head[1]))
				flag = 1;
			else {
				flag = 0;
				break;
			}
		}

		//Avoid place names: ie like c
		if (flag == 1 && headInfo1.size() == 1 && headInfo2.size() == 1)
			flag = 0;

		if (flag == 1 && sent[0].contains("/CC") && !sent[0].contains(","))
			flag = 0;

		//Like nps together smacks of a conjunction
		if (flag == 0 && sent[1].contains("/CC")) {
			headInfo2.add("X2/ECK");
			String np2head[] = (headInfo2.get(0)).split("/");
			if (np2head[1].equals(np1head[1]))
				flag = 1;
		}

		//if(flag==0 && sent[1].contains(",/, and/CC"))flag=1;

		/*Stub to print a particular sentence
		 if(id.equals("ID=wsj_0012.3")){
		 System.out.println("Flag: "+flag);
		 System.out.println(headInfo1);
		 System.out.println(headInfo2);
		 }*/

		if (flag == 1)
			res = "conj";
		else
			res = "appos";

		//if(unit.contains("/CD") || unit.contains("/POS") || unit.contains("/IN") || unit.contains("/DT")|| unit.contains("PRP$") || head1==true || cue==true)featInfo.add("appos");

		return res;

	}

}
