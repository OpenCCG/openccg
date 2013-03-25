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

//Bkgrnd java class which helps with operations for debugging LFs

package opennlp.ccgbank.extract;

import java.util.*;
//import java.io.*;

public class DefaultLFHelper {

	private ArrayList<String> idTally = new ArrayList<String>();

	private Hashtable<String,Integer> freqTable = new Hashtable<String,Integer>();

	private String lfType = "ord";

	public void init() {

		lfType = "ord";
		idTally = new ArrayList<String>();
		freqTable = new Hashtable<String,Integer>();
		argCounter = 0;
	}

	public void storeCat(String cat, String id, String idType) {

		int freq = 1;

		if (id.length() > 0) {

			//System.out.println(cat+" "+id+" "+idType);

			if (!freqTable.containsKey(id))
				freqTable.put(id, freq);
			else {
				freq = freqTable.get(id) + 1;
				freqTable.put(id, freq);
			}

			if (id.equals("1") && idType.equals("inherits"))
				lfType = "mod-mod";
			else if (!lfType.equals("mod-mod") && id.equals("1")
					&& idTally.size() > 0 && idTally.get(0).equals("1"))
				lfType = "mod";

			idTally.add(id);
		}
	}

	public String getType() {
		String retVal = lfType;
		lfType = "";
		argCounter = 0;
		return retVal;
	}

	private int argCounter = 0;

	public String getArgNo(int argCount) {
		int argNo = argCount - argCounter;
		argCounter++;
		return Integer.toString(argNo);
	}

	public boolean isArg(String id) {

		//System.out.println(id);
		//System.out.println(freqTable);

		int freq = 0;
		if (freqTable.containsKey(id))
			freq = freqTable.get(id);

		boolean retVal = false;
		if (freq == 1)
			retVal = true;
		return retVal;
	}

	public String purgeCat(String cat) {
		cat = cat.replaceAll("\"", "");
		cat = cat.replaceAll("~", "");
		cat = cat.replaceAll("_[0-9]+", "");
		cat = cat.replaceAll(">", "");
		//System.out.println("Debug: "+cat);
		return cat;
	}
}
