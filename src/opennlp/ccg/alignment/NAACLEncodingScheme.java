//////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2012 Scott Martin
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

package opennlp.ccg.alignment;

import static opennlp.ccg.alignment.MappingFormat.Field.*;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Represents the NAACL shared task encoding.
 * <table>
 * 	<tr><td>Field separator</td><td>(space)</td></tr>
 * 	<tr><td>Mapping separator</td><td>(newline)</td></tr>
 * 	<tr><td>Group separator</td><td>(newline)</td></tr>
 * 	<tr><td>ID base</td><td>{@link IndexBase#ZERO}</td></tr>
 * 	<tr><td>Index base</td><td>{@link IndexBase#ONE}</td></tr>
 * 	<tr><td>Example group</td><td>
 * <pre>
 * 37 1 2 S
 * 37 3 4 S
 * 37 3 5 P
 * 37 4 1 S
 * </pre>
 * </td></tr>
 * </table>
 * @see <a href="http://www.cse.unt.edu/~rada/wpt/WordAlignment.Guidelines.txt">NAACL shared task word alignment guidelines</a>
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class NAACLEncodingScheme extends AbstractEncodingScheme {
	
	/**
	 * Creates a new instance of the NAACL encoding scheme.
	 * @see Alignments#NAACL_ENCODING_SCHEME
	 */
	public NAACLEncodingScheme() {
		super(' ', '\n', '\n', IndexBase.ZERO, IndexBase.ONE,
			Alignments.NAACL_DEFAULT_FIELDS,				
			new HashSet<MappingFormat.Field>(Arrays.asList(PHRASE_NUMBER_FIELD, A_INDEX_FIELD, B_INDEX_FIELD)),				
			PHRASE_NUMBER_FIELD, A_INDEX_FIELD, B_INDEX_FIELD, STATUS_FIELD, CONFIDENCE_FIELD);
	}

}
