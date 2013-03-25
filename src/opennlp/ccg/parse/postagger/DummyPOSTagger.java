///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2010 Dennis N. Mehay
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

package opennlp.ccg.parse.postagger;
import java.util.ArrayList;
import java.util.List;
import opennlp.ccg.lexicon.Word;
import opennlp.ccg.parse.tagger.TaggedWord;
import opennlp.ccg.util.Pair;
        
/**
 * A "DummyPOSTagger" simply grabs the single POS tag in the Words themselves
 * and puts them into a TaggedWord with a singleton list of probability 1.0 POS
 * tags.
 * 
 * @author Dennis N. Mehay
 */
public class DummyPOSTagger extends POSTagger {    

    public List<TaggedWord> tagSentence(List<Word> sentence) { 
        List<TaggedWord> result = new ArrayList<TaggedWord>(sentence.size());
        for(Word w : sentence) {            
            List<Pair<Double,String>> tmpTagging = new ArrayList<Pair<Double,String>>(1);
            tmpTagging.add(new Pair<Double,String>(1.0,w.getPOS()));            
            TaggedWord tmp = new TaggedWord(w);
            tmp.setPOSTagging(tmpTagging);
            result.add(tmp);            
        }
        return result; 
    }
}