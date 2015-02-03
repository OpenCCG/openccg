///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2005 University of Edinburgh (Michael White)
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
 * An AssociateChain represents an association via a chain of associates. In the
 * minimal case, it may be just a form with no associate, what is useful for
 * morphological type ascription. It may also be a larger chain of associates
 * that goes all the way to rhetorico-semantic entities and entity classes.
 * 
 * Canonical instances are created by a factory method and stored in a trie map.
 * The associate chain representation of an association is more space-efficient
 * when dealing with a large number of symbols that are realised by a smaller
 * numer of forms.
 *
 * @author Michael White
 * @author Daniel Couto-Vale
 * @version $Revision: 1.3 $, $Date: 2009/07/17 04:23:30 $
 */
public class AssociateChain extends Word {

	private static final long serialVersionUID = 952665894357382685L;

	/** The referenced factor key or string (for the word form). */
	protected Object key;

	/** The previous node in the chain. */
	protected AssociateChain prev;

	/** Constructor. */
	protected AssociateChain(Object key, AssociateChain prev) {
		this.key = key;
		this.prev = prev;
	}

	/** Returns the surface form. */
	public String getForm() {
		return getValFromInterned(Tokenizer.WORD_ASSOCIATE);
	}

	/** Returns the pitch accent. */
	public String getPitchAccent() {
		return getValFromInterned(Tokenizer.TONE_ASSOCIATE);
	}

	/** Returns the list of extra attribute-value pairs. */
	protected List<Pair<String, String>> getFormalAttributes() {
		List<Pair<String, String>> retval = null;
		AssociateChain current = this;
		while (current != null) {
			if (current.key instanceof FactorKey) {
				FactorKey fkey = (FactorKey) current.key;
				if (!isKnownAttr(fkey.factor)) {
					if (retval == null)
						retval = new ArrayList<Pair<String, String>>(5);
					retval.add(0, new Pair<String, String>(fkey.factor, fkey.val));
				}
			}
			current = current.prev;
		}
		return retval;
	}

	/** Returns the stem. */
	public String getStem() {
		return getValFromInterned(Tokenizer.TERM_ASSOCIATE);
	}

	/** Returns the part of speech. */
	public String getPOS() {
		return getValFromInterned(Tokenizer.FUNCTIONS_ASSOCIATE);
	}

	/** Returns the supertag. */
	public String getSupertag() {
		return getValFromInterned(Tokenizer.SUPERTAG_ASSOCIATE);
	}

	/** Returns the semantic class. */
	public String getSemClass() {
		return getValFromInterned(Tokenizer.ENTITY_CLASS_ASSOCIATE);
	}

	/**
	 * Returns the value of the attribute with the given name, or null if none.
	 * The attribute names Tokenizer.WORD_ATTR, ..., Tokenizer.SEM_CLASS_ATTR
	 * may be used to retrieve the form, ..., semantic class.
	 */
	public String getFormalAttributeValue(String attr) {
		String internedAttr = attr.intern(); // use == on interned attr
		return getValFromInterned(internedAttr);
	}

	/** Returns the value of the given interned attr, or null if none. */
	protected String getValFromInterned(String attr) {
		AssociateChain current = this;
		while (current != null) {
			if (attr == Tokenizer.WORD_ASSOCIATE) {
				if (current.key instanceof String)
					return (String) current.key;
			} else if (current.key instanceof FactorKey) {
				FactorKey fkey = (FactorKey) current.key;
				if (fkey.factor == attr)
					return fkey.val;
			}
			current = current.prev;
		}
		return null;
	}

}
