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

import static opennlp.ccg.alignment.MappingFormat.Field.A_INDEX_FIELD;
import static opennlp.ccg.alignment.MappingFormat.Field.B_INDEX_FIELD;
import static opennlp.ccg.alignment.MappingFormat.Field.STATUS_FIELD;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Represents the Moses encoding.
 * <table>
 * 	<tr><td>Field separator</td><td>-</td></tr>
 * 	<tr><td>Mapping separator</td><td>(space)</td></tr>
 * 	<tr><td>Group separator</td><td>(newline)</td></tr>
 * 	<tr><td>ID base</td><td>{@link IndexBase#ZERO}</td></tr>
 * 	<tr><td>Index base</td><td>{@link IndexBase#ZERO}</td></tr>
 * 	<tr><td>Example group</td><td>0-1 2-3 2-4-P 3-0</td></tr>
 * </table>
 * @see <a href="http://www.statmt.org/moses/?n=FactoredTraining.AlignWords">Moses Word Alignment Tutorial</a>
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class MosesEncodingScheme extends AbstractEncodingScheme {
	
	/**
	 * Creates a new instance of the Moses encoding scheme.
	 * @see Alignments#MOSES_ENCODING_SCHEME
	 */
	public MosesEncodingScheme() {
		super('-', ' ', '\n', Alignments.DEFAULT_INDEX_BASE, IndexBase.ZERO,
				new HashSet<MappingFormat.Field>(Arrays.asList(A_INDEX_FIELD, B_INDEX_FIELD, STATUS_FIELD)),
				new HashSet<MappingFormat.Field>(Arrays.asList(A_INDEX_FIELD, B_INDEX_FIELD)),
				A_INDEX_FIELD, B_INDEX_FIELD, STATUS_FIELD);
	}

}
