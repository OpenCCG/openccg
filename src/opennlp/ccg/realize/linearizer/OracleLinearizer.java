///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2018 Reid Fu
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

package opennlp.ccg.realize.linearizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import opennlp.ccg.realize.feat_extract.LogicalForm;
import opennlp.ccg.realize.feat_extract.WordFeatures;

public class OracleLinearizer extends Linearizer {
	//TODO: Can't add parentheses in properly yet
	@Override
	public List<String> order(LogicalForm lf, WordFeatures current, Set<String> visited, LinConfig config) {
		List<String> wordIDs = new ArrayList<>(lf.getWordIds());
		// Remove all x# and h# word ID's
		for(int i = 0;i < wordIDs.size();i++) {
			if(!wordIDs.get(i).matches("w[0-9]*")) {
				wordIDs.remove(i);
				i--;
			}
		}
		
		Collections.sort(wordIDs, new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				int num0 = Integer.parseInt(arg0.substring(1));
				int num1 = Integer.parseInt(arg1.substring(1));
				return num0 - num1;
			}
		});
		return wordIDs;
	}
}
