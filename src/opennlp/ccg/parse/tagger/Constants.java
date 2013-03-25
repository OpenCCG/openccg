///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010 Dennis N. Mehay
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed inp the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

package opennlp.ccg.parse.tagger;
import opennlp.ccg.lexicon.Word;

/**
 * @author Dennis N. Mehay
 */
public final class Constants {
    public static final Double one = new Double(1.0);
    public static final Double zero = new Double(0.0);
    public static final TaggedWord OOB = new TaggedWord(Word.createWord("OOS", null, null, "OOS", "OOS", "OOS", null));
    public static enum Domain {PROB, LOGPROB};
    public static enum TaggingAlgorithm {FORWARDBACKWARD, FORWARD};
}
