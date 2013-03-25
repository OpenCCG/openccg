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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.ccg.lexicon.Word;
import opennlp.ccg.parse.tagger.io.PipeDelimitedFactoredBundleCorpusIterator;
import opennlp.ccg.parse.tagger.io.SRILMFactoredBundleCorpusIterator;
import opennlp.ccg.util.Pair;

/**
 * Extracts and writes out XML files containing tagging
 * dictionary stats.
 * 
 * @author Dennis N. Mehay
 * @version $Revision: 1.5 $, $Date: 2010/09/21 04:12:41 $
 */
public class TaggingDictionaryExtractor {

    /**
     * Create a new dictionary extractor, specifying the word and POS
     * dictionary files, as well as the tokenisation type (SRILM Factored bundle or
     * C&C/Moses-style pipe-delimited factored bundles.
     * @param corpus A <code>File</code> of plain-text, one sentence per line and no additional mark-up beyond
     * the <s> ... </s> for SRILM factored bundle style.
     * @param wd A <code>File</code> where the word-based tagging dictionary will be written.
     * @param posd A <code>File</code> where the POS-based tagging dictionary will be written.
     * @param tokenisationType A <code>String</code> telling us how to tokenise factors in the 
     * corpus file.
     */
    // mapping from words to a pairing of their frequencies and the lexical categories they were seen with.
    private Map<String, Pair<Integer, Set<String>>> wdmap = new HashMap<String, Pair<Integer, Set<String>>>();
    // mapping from POS tags to the lexical categories they were seen with.
    private Map<String, Set<String>> posmap = new HashMap<String, Set<String>>();
    private Iterator<List<Word>> incorp = null;
    // writers for dict files.
    private BufferedWriter wbr = null,  pbr = null;

    // how frequently a cat must occur to make it into the dictionaries.
    private int minCatFreq = 1;
    
    /**
     * Escape characters for text appearing as XML data, between tags.
     * 
     * <P>The following characters are replaced with corresponding character entities :
     * <table border='1' cellpadding='3' cellspacing='0'>
     * <tr><th> Character </th><th> Encoding </th></tr>
     * <tr><td> < </td><td> &lt; </td></tr>
     * <tr><td> > </td><td> &gt; </td></tr>
     * <tr><td> & </td><td> &amp; </td></tr>
     * <tr><td> " </td><td> &quot;</td></tr>
     * <tr><td> ' </td><td> &#039;</td></tr>
     * </table>
     * 
     * <P>Note that JSTL's {@code <c:out>} escapes the exact same set of 
     * characters as this method. <span class='highlight'>That is, {@code <c:out>}
     *  is good for escaping to produce valid XML, but not for producing safe 
     *  HTML.</span>
     */
    public static String forXML(String aText) {
    	if (aText == null) return null;
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(aText);
        char character = iterator.current();
        while (character != CharacterIterator.DONE) {
            if (character == '<') {
                result.append("&lt;");
            } else if (character == '>') {
                result.append("&gt;");
            } else if (character == '\"') {
                result.append("&quot;");
            } else if (character == '\'') {
                result.append("&#039;");
            } else if (character == '&') {
                result.append("&amp;");
            } else {
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }

    public TaggingDictionaryExtractor(File corpus, File wd, File posd, String tokenisationType) {
        this(corpus, wd, posd, tokenisationType, 10);
    }
    
    public TaggingDictionaryExtractor(File corpus, File wd, File posd, String tokenisationType, int catFreq) {
        try {
            wbr = new BufferedWriter(new FileWriter(wd));
            pbr = new BufferedWriter(new FileWriter(posd));
            minCatFreq = catFreq;
            if (tokenisationType.equalsIgnoreCase("srilm")) {
                incorp = new SRILMFactoredBundleCorpusIterator(new BufferedReader(new FileReader(corpus)));
            } else {
                incorp = new PipeDelimitedFactoredBundleCorpusIterator(new BufferedReader(new FileReader(corpus)));
            }
        } catch (IOException ex) {
            Logger.getLogger(TaggingDictionaryExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Extract the dictionaries.
     */
    @SuppressWarnings("unchecked")
	public void extract() {
        try {
            List<Word> currsent = null;
            String currForm = null, currPOS = null;
            //Set<String> currFormSTs = null, curPOSSTs = null;
            //Integer currWdCnt = null;
            Pair<Integer, Set<String>> currFormFetch = null;
            Set<String> currFormSet = null, currPOSSet = null;

            Map<String, Integer> catCount = new HashMap<String, Integer>();
            
            while (incorp.hasNext()) {
                // for every word in every sentence, update the counts, and add to the word- and POS-based
                // allowable tags.
                currsent = incorp.next();
                for (Word w : currsent) {
                    currForm = w.getForm();
                    currPOS = w.getPOS();
                    Object wfetch = wdmap.get(currForm);
                    String stag = w.getSupertag();
                    catCount.put(stag, catCount.get(stag)==null ? 1 : catCount.get(stag) + 1);
                    
                    if (wfetch == null) {
                        currFormSet = new HashSet<String>();
                        currFormSet.add(w.getSupertag());
                        wdmap.put(currForm, new Pair<Integer, Set<String>>(new Integer(1), currFormSet));
                    } else {
                        currFormFetch = ((Pair<Integer, Set<String>>) wfetch);
                        currFormSet = currFormFetch.b;
                        currFormSet.add(w.getSupertag());
                        wdmap.put(currForm,
                                new Pair<Integer, Set<String>>(new Integer(currFormFetch.a.intValue() + 1), currFormSet));
                    }

                    Object pfetch = posmap.get(currPOS);
                    if (pfetch == null) {
                        currPOSSet = new HashSet<String>();
                        currPOSSet.add(w.getSupertag());
                    } else {
                        currPOSSet = (Set<String>) pfetch;
                        currPOSSet.add(w.getSupertag());
                    }
                    
                    posmap.put(currPOS, currPOSSet);
                }
            }

            // now write out the dictionaries.
            String wrd = null;
            Pair<Integer, Set<String>> lkup = null;
            wbr.write("<?xml version=\"1.0\"?>\n");
            wbr.write("<wdict>\n");
            for (Object wdobj : wdmap.keySet()) {
                wrd = (String) wdobj;
                lkup = (Pair<Integer, Set<String>>) (wdmap.get(wdobj));
                wbr.write("\t<entry word=\"" + forXML(wrd) + "\" freq=\"" + lkup.a.intValue() + "\">\n");                
                for (String st : lkup.b) {
                    if(catCount.get(st) >= minCatFreq) {
                        wbr.write("\t\t<supertag> " + forXML(st) + " </supertag>\n");
                    }
                }
                wbr.write("\t</entry>\n");
            }
            wbr.write("</wdict>");

            String pos = null;
            Set<String> plkup = null;
            pbr.write("<?xml version=\"1.0\"?>\n");
            pbr.write("<posdict>\n");
            for (Object pobj : posmap.keySet()) {
                pos = (String) pobj;
                plkup = (Set<String>) posmap.get(pobj);
                pbr.write("\t<entry pos=\"" + forXML(pos) + "\">\n");
                for (String st : plkup) {
                    if(catCount.get(st) >= minCatFreq) {
                        pbr.write("\t\t<supertag> " + forXML(st) + " </supertag>\n");
                    }
                }
                pbr.write("\t</entry>\n");
            }
            pbr.write("</posdict>");

            // clean up.
            wbr.flush();
            wbr.close();
            pbr.flush();
            pbr.close();
        // done
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TaggingDictionaryExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
            Logger.getLogger(TaggingDictionaryExtractor.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public static void main(String[] args) throws Exception {
        String usage ="\nTaggingDictionaryExtractor -i <inputCorpus> -f <catFreqCutoff> -p <POSOutputXMLFile> -w <wordOutputXMLFile>\n\n";
        if(args.length > 0 && args[0].equals("-h")) {
            System.out.print(usage);
            System.exit(0);
        }
        
        String inputCorp = null, wOutput = null, pOutput = null;
        // how frequently must a supertag category have been seen to be included in the dictionary?
        int catFreq = 10;
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-i")) {inputCorp = args[++i]; continue;}
            if(args[i].equals("-w")) {wOutput = args[++i]; continue;}
            if(args[i].equals("-p")) {pOutput = args[++i]; continue;}
            if(args[i].equals("-f")) {catFreq = Integer.parseInt(args[++i]); continue; }
            System.err.println("Unknown command-line option: "+args[i]);
        }
        
        File in = new File(inputCorp); 
        File wout = new File(wOutput);
        File pout = new File(pOutput);
        TaggingDictionaryExtractor tde = new TaggingDictionaryExtractor(in, wout, pout, "SRILM", catFreq);
        tde.extract();
    }
}
