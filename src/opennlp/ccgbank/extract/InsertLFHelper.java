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

//Helper Class for insertLF.xsl

//This class is invoked by MorphExtr.xsl 

package opennlp.ccgbank.extract;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InsertLFHelper{
	
	private List<String> featTally=new ArrayList<String>();
	
	//Flush feat tally
	public String initFeat(){
		featTally.clear();
		return null;
	}
	
	public String putFeat(String feat){
		featTally.add(feat);
		return null;
	}
	
	public String getFeat(){
		String feat="";
		
		if (featTally.size() > 0) {
			feat = featTally.get(0);
			featTally.remove(0);
		}
		else feat="xxx";
		
		return feat;
	}

	// for ensuring uniqueness of stem/rel pairs
	private Set<String> stemRelPairs = new HashSet<String>();
	
	// reset
	public String resetStemRelPairs() { stemRelPairs.clear(); return null; }
	
	// contains, updating
	public boolean containsStemRelPair(String stem, String rel) {
		String key = stem + "_" + rel;
		if (stemRelPairs.contains(key)) return true;
		stemRelPairs.add(key);
		return false;
	}
	
	private String[] rolesArray = {};
	
	// sets the roles
	public boolean setRoles(String roles) {
		rolesArray = roles.split("\\s+");
		return true;
	}
	
	// returns the nth role
	public String getRole(int n) {
		return (n < rolesArray.length) ? rolesArray[n] : "null";
	}
}
