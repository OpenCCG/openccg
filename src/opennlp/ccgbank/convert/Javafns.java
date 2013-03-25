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

//Java Program invoked by pp-head extraction routines

package opennlp.ccgbank.convert;

import java.util.ArrayList;
import java.util.List;

// NB: addRes and changeCat probably made superfluous by computeCats.xsl
public class Javafns{
	
	//Prep heads storage
	public List<String> heads = new ArrayList<String>();
	
	//Temp id of pp heads with args
	String tempId="";	

	//Insert the prep-head into a result category by string replacement
	public String addRes(String cat,String ppHead){
		
		//Xsl spl char detected and escaped
		if(ppHead.equals("$"))
			ppHead="\\$";
		
		//Head inserted into first PP category
		cat=cat.replaceFirst("pp","pp["+ppHead+"]");
		return cat;
	}
	
	//A safety hatch to elide extra heads detected - For leaf nodes
	public String elimRedun(int headCount){
		
		//Calculating redundant Headcount
		int j=0;
		
		//All heads following the last PP-head in a cat spec are redundant
		int redun=heads.size()-headCount;
		
		//Redundant heads removed
		for(j=0;j<redun;j++)
			heads.remove(heads.size()-1);
		
		return null;
		
	}
	
	//Fn to store temp id of a treenode with pp arg(s)
	public String putTempId(String x){
		
			tempId=x;	
			return tempId;
	
	}
		
	//Fn to retrieve temp id of a treenode with pp arg(s)
	public String getTempId(){
			
			return tempId;
	
	}

	//Inserts pp-heads into argument categories 
	public String changeCat(String cat){
		
		//System.out.println(cat);
		//System.out.println(heads);
		
		int i=heads.size()-1;
		
		for(;i>-1;i--){
			
			String ppHead = heads.get(i);
			
			//Escaping dollar signs for xsl
			if(ppHead.equals("$"))
				ppHead="\\$";
			
			//Simple head insertion by replacement on the string
			cat=cat.replaceFirst("pp_","pp["+ppHead+"]_");
			
		}

		return cat;
	}
	
	public String flush(){
		heads.clear();tempId="";		
		return null;
	}
	
	
	public String setHead(String head){
		heads.add(head);
		return null;
	}
	
	
	public String getHead(){
		
		String head="";
		
		if(heads.size()==0)
			head="WrongHead";
		else{
			head = heads.get(heads.size()-1);
			heads.remove(heads.size()-1);
		} 
		
		return head;
	}
	
	
	public String peekHead(){
		
		String head="WrongHead";
		
		if(heads.size()>0)
			head = heads.get(heads.size()-1);
		
		return head;
	}

	public String printCat(String cat){
		System.out.println(cat);
		return cat;
	}

}
