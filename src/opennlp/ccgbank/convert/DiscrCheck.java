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



public class DiscrCheck{

		//Sentence id
		private  String id="";

	 //Proc which traps, stores id of each sentence in a global var
    public String storeId(String x){

				id="";
				id=x;
				return null;
    }

		 public void checkCatDiscr(String cat,String cat0,String lex){

				 //Comparing discrepancies between cat0 & cat1
				 //Just a check. Should be commented out in the final version
				 
				 String catA=cat0;
				 String catB=cat;
				 
				 catA=catA.toLowerCase();
				 catB=catB.replaceAll("[0-9]","");
				 catB=catB.replaceAll("_","");
				 
				 if(!catA.equals(catB) && !cat0.contains("nb"))
						 System.out.println(id+": "+cat0+"***"+cat+" - "+lex);
				 
		 }
		
}



