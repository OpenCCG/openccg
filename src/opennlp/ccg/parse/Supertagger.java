///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2009 Michael White
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

package opennlp.ccg.parse;

import opennlp.ccg.lexicon.*;
import java.util.*;

/**
 * A parsing supertagger must extend the SupertaggerAdapter interface for plugging a 
 * supertagger into the lexicon.  It must additionally support methods for 
 * calculating and caching contextual supertagging assignments, so that 
 * supertags can be retrieved just based on the current word index.
 * 
 * @author      Michael White
 * @version     $Revision: 1.3 $, $Date: 2010/12/08 15:24:26 $
 */
public interface Supertagger extends SupertaggerAdapter {
	
	/**
	 * Maps the given words to their predicted categories, 
	 * so that the beta-best categories can be returned by calls to setWord
	 * and getSupertags.
	 */
	public void mapWords(List<Word> words);
	
	/**
	 * Sets the current word to the one with the given index, 
	 * so that the beta-best categories for it can be returned by a call to 
	 * getSupertags.
	 */
	public void setWord(int index);
}
