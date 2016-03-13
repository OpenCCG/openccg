///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004 University of Edinburgh (Michael White)
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
package opennlp.ccg.test;

import opennlp.ccg.grammar.*;
import opennlp.ccg.lexicon.*;

import org.jdom.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Utility class to update testbed files; just adds full words based on 
 * sign or parse at present.
 *
 * @author      Michael White
 * @version     $Revision: 1.5 $, $Date: 2009/12/21 04:18:31 $
 */
public class UpdateTestbed
{
    /** The grammar. */
    private Grammar grammar; 

    /** Constructor. */
    private UpdateTestbed(URL grammarURL) throws IOException {
        // load grammar
        System.out.println("Loading grammar from URL: " + grammarURL);
        grammar = new Grammar(grammarURL);
    }
    
    
    /** Adds full words for each test item, if missing, based on the sign or parse. */
    private void addFullWords(File tbFile) throws IOException {
        
        // load testbed
        System.out.println("Loading testbed from: " + tbFile);
        RegressionInfo tbInfo = new RegressionInfo(grammar, tbFile);
        
        // create output doc
        Document outDoc = new Document();
        Element outRoot = new Element("regression");
        outDoc.setRootElement(outRoot);

        // update each one
        int numItems = tbInfo.numberOfItems();
        Tokenizer tokenizer = grammar.lexicon.tokenizer;
        System.out.print("Adding full words ");
        for (int i = 0; i < numItems; i++) {
            RegressionInfo.TestItem testItem = tbInfo.getItem(i);
            if (testItem.fullWords == null) {
                List<Word> words = (testItem.sign != null) 
                	? testItem.sign.getWords() 
                	: grammar.getParsedWords(testItem.sentence);
                testItem.fullWords = tokenizer.format(words);
            }
            outRoot.addContent(RegressionInfo.makeTestItem(testItem));
            System.out.print("."); // indicate progress
        }
        System.out.println();
        
        // save file, backing up original
        File tbFileBackup = new File(tbFile.getParentFile(), tbFile.getName() + "~");
        System.out.println("Backing up testbed to: " + tbFileBackup);
        tbFile.renameTo(tbFileBackup);
        System.out.println("Saving results to: " + tbFile);
        FileOutputStream out = new FileOutputStream(tbFile); 
        grammar.serializeXml(outDoc, out);
        out.close();
    }

    
    /** Updates the given input file. */    
    public static void main(String[] args) throws IOException {
        
        String usage = "Usage: java opennlp.ccg.test.UpdateTestbed (-g <grammarfile>) (-add-full-words) (<testbedfile>)";
        
        if (args.length > 0 && args[0].equals("-h")) {
            System.out.println(usage);
            System.exit(0);
        }

        // args
        String grammarfile = "grammar.xml";
        String testbedfile = "testbed.xml";
        boolean addFullWords = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-g")) {
                grammarfile = args[++i]; continue; 
            }
            if (args[i].equals("-add-full-words")) {
                addFullWords = true; continue; 
            }
            testbedfile = args[i];
        }

        // create updater, check testbed exists
        File gFile = new File(grammarfile);
        URL grammarURL = gFile.toURI().toURL();
        File tbFile = new File(testbedfile);
        if (!tbFile.exists()) {
            tbFile = new File(gFile.getParentFile(), testbedfile);
        }
        if (!tbFile.exists()) {
            System.out.println("Unable to find testbed file: " + testbedfile);
            System.exit(-1);
        }
        UpdateTestbed updater = new UpdateTestbed(grammarURL); 
        
        // do tasks
        if (addFullWords) updater.addFullWords(tbFile);
        
        System.out.println("Done.");
    }
}

