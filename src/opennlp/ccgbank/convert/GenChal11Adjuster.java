///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2011 Michael White
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

package opennlp.ccgbank.convert;

import java.util.*;

/**
 * Utility class for adjusting Generation Challenges 2011 outputs.
 * Strings are lowercased, named entities and hyphenated words are split, 
 * and dollar sign and numbers are transposed. 
 */ 
public class GenChal11Adjuster {

	/** Returns the adjusted text string . */
	public String getAdjustedString(String text) {
		// lowercase and split
		String[] tokens = text.toLowerCase().split("\\s+");
		// swap dollar signs
		for (int i=0; i < tokens.length-1; i++) {
			if (tokens[i+1].equals("$")) {
				try {
					// check for preceding number token
					Double.parseDouble(tokens[i]);
					// swap, skip
					String num = tokens[i];
					tokens[i] = tokens[i+1];
					tokens[i+1] = num;
					i++;
				}
				catch (NumberFormatException e) {}
			}
		}
		// split NEs and hyphenated words
		List<String> splitTokens = new ArrayList<String>(tokens.length*2);
		for (String token : tokens) {
			String[] tokenSplits = token.replace("-"," - ").split("[_ ]");
			for (String s : tokenSplits)
				splitTokens.add(s);
		}
		// join
		StringBuffer retval = new StringBuffer();
		for (int i=0; i < splitTokens.size()-1; i++) {
			retval.append(splitTokens.get(i));
			retval.append(' ');
		}
		retval.append(splitTokens.get(splitTokens.size()-1));
		// done
		return retval.toString();
	}
}
