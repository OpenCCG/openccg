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


/*
 * Read in the CCGbank (auto format), line by line, and transform each lexical
 * item (<L cat1 pos1 pos2 word cat2>) into an SRILM factored LM bundle format:
 * W-word:S-word:P-pos1:T:cat1, where every thing has been escaped (e.g., colons),
 * "W" stands for word form, "S" for stem, "P" for POS and "T" for super_t_ag.
 */

package opennlp.ccg.parse.tagger.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import opennlp.ccg.lexicon.DefaultTokenizer;

/**
 * @author Dennis N. Mehay
 */
public class CCGBankToSRILMFLM {
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String usage = "\nCCGBankToSRILMFLM -input <inputCorpus> -o <outputCorpus> \n";
        if (args.length > 0 && args[0].equals("-h") || args.length == 0) {
            System.out.println(usage);
            System.exit(0);
        }

        BufferedReader reader = null;
        BufferedWriter writer = null;
        String inputCorp = "train.auto", output = "train.srilm";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-i")) {inputCorp = args[++i]; continue;}
            if (args[i].equals("-o")) {output = args[++i]; continue;}
            System.out.println("Unrecognized option: " + args[i]);
        }
        reader = new BufferedReader(new FileReader(new File(inputCorp)));
        writer = new BufferedWriter(new FileWriter(new File(output)));
        String parseIDHeader = "ID=";
        Pattern p = Pattern.compile("(<L\\s+.*?>)+?");
        
        String line = reader.readLine();
        while(line != null) {
            if(line.startsWith(parseIDHeader)) {line = reader.readLine(); continue;}
            line = line.trim();
            Matcher m = p.matcher(line);
            String word = null, pos = null, cat = null;
            int cnt = 0;
            while(m.find()) {
                String toks = m.group();
                // {<L, cat1, pos1, pos2, word, cat2>}
                String[] parts = toks.split(" ");
                word = parts[4];
                pos = parts[2];
                cat = parts[1];
                if(cnt++ > 0) {
                    writer.write(" ");
                }
                writer.write("W-"+DefaultTokenizer.escape(word)+":"
                        +"S-"+DefaultTokenizer.escape(word)+":"
                        +"P-"+DefaultTokenizer.escape(pos)+":"
                        +"T-"+DefaultTokenizer.escape(cat));
            }
            writer.write(System.getProperty("line.separator"));
            line = reader.readLine();
        }
        writer.close();
        reader.close();
    }
}
