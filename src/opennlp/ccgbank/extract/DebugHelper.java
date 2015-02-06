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
import java.io.*;

public class DebugHelper {

	public static boolean init = true;

	public static ArrayList<String> unmatCats = new ArrayList<String>();

	public static int nsrCount = 0;

	public static int unmatCount = 0;

	public boolean getInit() {

		return init;

	}

	//Read in bkgrnd info
	public void readInfo() {

		try {

			BufferedReader inp = new BufferedReader(new FileReader(
					"/scratch/propgrammar/unmat.txt"));
			String line = "";

			while ((line = inp.readLine()) != null) {

				if (line.length() == 0)
					continue;

				String parts[] = line.split(" ");
				String name = parts[parts.length - 2];
				String pos = parts[parts.length - 1];
				String x[] = name.split("=");
				String y[] = pos.split("=");
				name = purgeCat(x[1]);
				pos = purgeCat(y[1]);
				String unmat = name + " " + pos;
				unmatCats.add(unmat);
				//System.out.println(name+" "+pos);		

			}
			//System.out.println(tagInfo);
			//System.out.println(tagInfo.size());
			init = false;
			inp.close();
		}

		catch (IOException e) {
			System.out.println("Error reading input file");
		}
	}

	public String purgeCat(String cat) {

		cat = cat.replaceAll("\"", "");
		cat = cat.replaceAll("~", "");
		cat = cat.replaceAll("_[0-9]+", "");
		cat = cat.replaceAll(">", "");
		//System.out.println("Debug: "+cat);
		return cat;

	}

	public void recordInfo(String sentId, String pred, String misc) {

		nsrCount++;
		if (unmatCats.contains(misc))
			unmatCount++;

	}

	public void printInfo() {
		System.out.println(unmatCats);
		System.out.println("No:of nsr LFs: " + nsrCount);
		System.out.println("No:of unmatched that a NSR LF contains: "
				+ unmatCount);
	}

}
