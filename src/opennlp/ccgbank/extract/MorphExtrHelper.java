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

//This class is invoked by MorphExtr.xsl 

package opennlp.ccgbank.extract;

import java.util.HashSet;
import java.util.Set;

public class MorphExtrHelper {
	
	private FreqTally aFreqTally = new FreqTally(); 
	private Set<String> seenLexPos = new HashSet<String>();
	
	/** Returns whether the lex, cat and pos pass the frequency cutoffs, 
	 *  and the lex-stem-pos combo is new. */
	public boolean checkFreqAndNoveltyStatus(String lex, String stem, String cat, String pos,String semClass) {
		if (!aFreqTally.checkFreqStatus(lex, cat, pos)) return false;
		String key = lex + "_" + stem + "_" + "_" + pos + "_"+ semClass;
		//String key = lex + "_" + stem + "_" + "_" + pos;
		if (seenLexPos.contains(key)) return false;
		seenLexPos.add(key);
		return true;
	}

	//Applies rules to discern whether noun is animate or not
	public String macroNamer(String macro, String semClass,String pos,String lex){

			if((semClass.startsWith("PER") && pos.startsWith("N"))||pos.equals("DT")){
					macro=macro+" "+"@anim-nom";
			}
			else if(pos.startsWith("PP") || lex.equals("those") || (pos.startsWith("PRP") && !lex.startsWith("it")))
					macro=macro+" "+"@anim-nom";
			else macro=macro+" "+"@non-anim-nom";

			macro=macro.trim();

			//Skip date time entities from animacy classification
			if(semClass.contains("STATE") || semClass.contains("NATION") || semClass.startsWith("ORG_DESC") || semClass.contains("DATE")||semClass.contains("TIME")||semClass.contains("QUANTITY")||semClass.contains("CARDINAL") || semClass.contains("PERCENT"))
					macro="";
						
			//Eliminate collective nouns
			if(lex.equals("audience") || lex.equals("band") || lex.equals("group") || lex.equals("team") || lex.equals("club") || lex.equals("congregation"))
					macro="";
					

			return macro;

	}

	//Applies rules to discern whether noun should have number agreement for the copula macro
	public String agrMacroDecider(String macro,String semClass,String pos,String lex){

			if(pos.equals("NN")){
					
					//if(lex.equals("couple") || lex.equals("following") ||lex.equals("rest") || semClass.contains("STATE") || semClass.contains("NATION") || semClass.startsWith("ORG_DESC") || semClass.contains("DATE")||semClass.contains("TIME")||semClass.contains("QUANTITY")||semClass.contains("CARDINAL") ||semClass.endsWith("'S"))
					
					if(lex.equals("couple") || semClass.startsWith("ORG_DESC") || lex.equals("following") ||lex.equals("rest") || semClass.contains("STATE") || semClass.contains("NATION") || semClass.contains("DATE")||semClass.contains("TIME")||semClass.contains("QUANTITY")||semClass.contains("CARDINAL") || semClass.contains("PERCENT") || semClass.endsWith("'S"))
							macro="";

					if(semClass.length()==0)
							macro="";
									
			}
			else{
					
			}

			//System.out.println(macro);		

			return macro;

	}

		public String whLex="";
	public void storeWHLex(String whLex){
			//System.out.println("Raja: "+whLex);
			this.whLex=whLex;
	}	
	public String getWHLex(){
			return this.whLex;
	}
}
