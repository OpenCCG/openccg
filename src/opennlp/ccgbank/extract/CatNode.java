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

//Java class to store all the info associated with a category

package opennlp.ccgbank.extract;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class CatNode {
	
	// Category name, POS
	public final String cat;
	public final String pos;
	
	//Lexeme frequency
	private Map<String,Integer> lexFreq = new HashMap<String,Integer>();
	
	//Sentence id of lexemes
	private Map<String,List<String>> lexOccur = new HashMap<String,List<String>>();
	
	//Constructor to initialize the cat name
	public CatNode(String cat, String pos){
		this.cat=cat; this.pos = pos;
	}
	
	//Procedure to insert info associated with all lexemes of a category
	public void lexInsert(String lex, String id){
		//When first occurences of lexemes detected, entries opened
		if (!lexFreq.containsKey(lex)){
			lexFreq.put(lex, 1);
			List<String> idList = new ArrayList<String>(4);
			idList.add(id);
			lexOccur.put(lex, idList);
		}
		else { //Subsequent occurences of lexemes updated
			lexFreq.put(lex, lexFreq.get(lex)+1);
			List<String> idList = lexOccur.get(lex);
			//Ids of up to four sentences stored
			if(!idList.contains(id) && idList.size() < 4){
				idList.add(id);
			}
		}
	}
	
	// returns frequence of lex with this cat and pos
	public int getLexFreq(String lex) {
		Integer retval = lexFreq.get(lex);
		if (retval == null) return 0;
		else return retval;
	}
	
	//Proc which prints out the lexical info of a category
	public void printTally(PrintWriter output) {
		
		//Lexemes sorted in descending order of freq
		List<String> sortedLex = FreqTally.sortTally(lexFreq);
		
		String ccgbankHome = System.getProperty("CCGBANK_HOME", "/home/corpora/EN/ccgbank");
		
		//Sorted list processed
		for (int i=0; i<3 && i<sortedLex.size(); i++) {
			
			String lex = sortedLex.get(i);
			int freq = lexFreq.get(lex);
			
			output.println("<ul>");
			output.println("<li>");
			output.println(lex+" "+freq);
			
			//Sentence ids also printed
			List<String> temp = lexOccur.get(lex);
			for (String id: temp) {
				//System.out.println(id);
				String[]idInfo=id.split("\\.");
				//System.out.println(idInfo[0]);

				StringTokenizer st=new StringTokenizer(id,".");
				output.println("<ul>");

				String idLink="";
				String sentNo="";
				String dir="";

				//2 courses of action depending on whether input is gold std .auto parses or C&C .auto parses
				if(idInfo.length==2){
						idLink=st.nextToken()+".html";
						sentNo="#Sentence "+st.nextToken();
						dir=id.substring(4,6);
				}
				else {
						idLink=idInfo[0];
						sentNo="#Sentence "+idInfo[0];
						dir=idInfo[0];
				}
				output.println("<li>");
				output.println(id);
				output.println(" <a href=file://" + ccgbankHome + "/original/data/HTML/"+dir+"/"+idLink+" target="+sentNo+">");
				output.println(idLink);
				output.println("</a>");  
				output.println("</li>");  
				output.println("</ul>");
			}

			
			output.println("</li>");
			output.println("</ul>");
			output.println("");
			output.flush();
		}
	}
}
