///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2009 Dennis N. Mehay
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
package opennlp.ccg.parse.supertagger.util;

import java.util.List;
import opennlp.ccg.lexicon.DefaultTokenizer;
import opennlp.ccg.lexicon.Word;
import opennlp.ccg.util.Pair;

/**
 * @author Dennis N. Mehay
 * @version $Revision: 1.1 $, $Date: 2009/08/21 17:20:20 $
 */
public class PipedTokenizer extends DefaultTokenizer {

    public PipedTokenizer() {
        super();
    }

    @Override
    public Word parseToken(String token, boolean strictFactors) {
        // init
        String form = token;
        String stem = null;
        String POS = null;
        String pitchAccent = null;
        String supertag = null;
        String semClass = null;
        List<Pair<String,String>> attrValPairs = null;
        
        // handle pipe-separated attr-val pairs
        int pipePos = token.indexOf('|');
        String suffix = null;
        if (pipePos > 0) {
            // get word form
            form = token.substring(0, pipePos);
            // shave off word form
            suffix = token.substring(pipePos + 1);
            // get next | position
            pipePos = suffix.indexOf('|');            
            // get stem [or lemma]. could be null.
            stem = suffix.substring(0,pipePos);
            if (stem.equals("")) { stem = null;}
            // shave off stem/lemma
            suffix = suffix.substring(pipePos + 1);
            // get next | position
            pipePos = suffix.indexOf('|');
            // get POS
            POS = suffix.substring(0,pipePos);
            // shave off POS
            suffix = suffix.substring(pipePos + 1);
            // see whether there is a supertag
            if (suffix != null && !suffix.equals("")) {
                // get supertag
                supertag = suffix.trim();
            }
        } else {
            throw new RuntimeException("This file is not in the right format: \n"+
                    "form|lemma|POS|(Supertag) ... form|lemma|POS(Supertag).");
        }
        // done
        return Word.createWord(form, pitchAccent, attrValPairs, stem, POS, supertag, semClass);
    }
}
