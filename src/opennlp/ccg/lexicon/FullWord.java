///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004-5 University of Edinburgh (Michael White)
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

package opennlp.ccg.lexicon;

import opennlp.ccg.util.*;

import java.util.*;

/**
 * A FullWord object is a word with all possible fields. The factory methods
 * return interned objects.
 *
 * @author Michael White
 * @version $Revision: 1.6 $, $Date: 2009/07/17 04:23:30 $
 */
public class FullWord extends WordWithPitchAccent {

	private static final long serialVersionUID = -3115687437782457735L;

	/** List of attribute-value pairs, which must be strings. */
	protected List<Pair<String, String>> attrValPairs;

	/** The stem. */
	protected String stem;

	/** The part of speech. */
	protected String POS;

	/** The supertag. */
	protected String supertag;

	/** The semantic class (optional). */
	protected String semClass;

	/** Returns the list of extra attribute-value pairs. */
	protected List<Pair<String, String>> getFormalAttributes() {
		return attrValPairs;
	}

	/** Returns the stem. */
	public String getStem() {
		return stem;
	}

	/** Returns the part of speech. */
	public String getPOS() {
		return POS;
	}

	/** Returns the supertag. */
	public String getSupertag() {
		return supertag;
	}

	/** Returns the semantic class (may be null). */
	public String getSemClass() {
		return semClass;
	}

	/** Constructor for full word. */
	protected FullWord(String form, String pitchAccent, List<Pair<String, String>> attrValPairs,
			String stem, String POS, String supertag, String semClass) {
		super(form, pitchAccent);
		this.attrValPairs = attrValPairs;
		this.stem = stem;
		this.POS = POS;
		this.supertag = supertag;
		this.semClass = semClass;
	}

}
