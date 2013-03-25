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

package opennlp.ccg.parse.postagger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import opennlp.ccg.lexicon.Word;
import opennlp.ccg.parse.tagger.io.SRILMFactoredBundleCorpusIterator;

/**
 * @author Dennis N. Mehay
 */
public class POSTagSequenceGetter {
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String usage = "\nPOSTagSequenceGetter -i <inputCorpus> -o <outputLocation>\n";
        String input = null, output = null;
        if(args == null || args.length == 0 || args[0].equals("-h")) {
            System.err.println(usage);
            System.exit(0);
        }
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-i")) { input = args[++i]; continue; }
            if(args[i].equals("-o")) { output = args[++i]; continue; }
            System.err.println("unknown command-line option: " + args[i]);
        }
        
        BufferedReader in = new BufferedReader(new FileReader(new File(input)));
        SRILMFactoredBundleCorpusIterator corp = new SRILMFactoredBundleCorpusIterator(in);
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(output)));
        
        
        for(List<Word> sent : corp) {
            out.write("<s> ");
            for(Word w : sent) {
                out.write(w.getPOS()+" ");
            }
            out.write("</s>"+System.getProperty("line.separator"));
        }
        out.close();
    }
    
}
