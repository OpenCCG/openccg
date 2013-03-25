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

//Bkgrnd java class which helps with operations reltd to multi-word units
//As a first stab, I combine multi-word conjns like "As well as"

package opennlp.ccgbank.convert;

import java.util.*;

public class MWHelper {

	// The largest current id
	private String lex = "";

	private String pos = "";

	private String term_no = "";

	// A list of the particle terminal nos
	private ArrayList<Integer> prtTally = new ArrayList<Integer>();

	// Index of terminal nos and the lexical items they correspond to
	private Hashtable<Integer, String> prtIndex = new Hashtable<Integer, String>();

	public void initSettings() {
		prtTally = new ArrayList<Integer>();
		prtIndex = new Hashtable<Integer, String>();
	}

	// Concat lex,pos & term_nos of multi-word units
	public void concatWords(String lex, String pos, String term_no) {

		// System.out.println(lex);
		this.lex = this.lex + "_" + lex;
		this.pos = this.pos + " " + pos;
		this.term_no = this.term_no + " " + term_no;
	}

	// Retrieve stored info
	public String getInfo(int choice) {

		String retVal = "";

		switch (choice) {

		case 1:
			retVal = lex.trim().replaceFirst("_", "");
			this.lex = "";
			break;
		case 2:
			retVal = pos.trim();
			this.pos = "";
			break;
		case 3:
			retVal = term_no.trim();
			this.term_no = "";
			break;

		}

		return retVal;
	}

	// Store particle ids
	public void storePrt(String prt_term_no, String prt) {
		prtTally.add(Integer.parseInt(prt_term_no));
		prtIndex.put(Integer.parseInt(prt_term_no), prt);
	}

	public String peekPrt(int nextPrnNo) {

		String retVal = "";
		if (prtIndex.containsKey(nextPrnNo)) {
			retVal = prtIndex.get(nextPrnNo);
		}

		return retVal;
	}

	public String getPrt() {
		// System.out.println(prtTally);
		// prtTally=new ArrayList();
		Collections.sort(prtTally);
		String retVal = "";
		if (prtTally.size() > 0) {

			retVal = (prtTally.get(prtTally.size() - 1)).toString();
			// System.out.println(retVal);
			prtTally.remove(prtTally.size() - 1);
		}

		return retVal;
	}

}
