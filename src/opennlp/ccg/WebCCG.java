///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2006 Ben Wing.
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

package opennlp.ccg;

import opennlp.ccg.lexicon.*;
import opennlp.ccg.grammar.*;
import opennlp.ccg.parse.*;
import opennlp.ccg.util.*;
import opennlp.ccg.synsem.*;
//import opennlp.ccg.realize.*;
import opennlp.ccg.hylo.*;
//import opennlp.ccg.ngrams.*;
//import opennlp.ccg.test.*;

//import org.jdom.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * An interface for use with a higher-level web interface.  This should
 * provide as simple an interface onto parsing as possible, with its output
 * in a format that can be easily handled by a CGI program or similar.
 * Called as
 *
 * webccg [-showall] [-showderivs] [-showsem] [-visualize FILE] GRAMMARDIR

-showall shows all parses rather than just the first one.
-showderivs shows the derivation history of each parse.
-showsem shows the logical form of each parse.
-visualize output a visualization of the parses into FILE (in PNG format).
 *
 * @author  Ben Wing
 * @version $Revision: 1.4 $, $Date: 2009/12/21 03:27:18 $
 */
public class WebCCG {
    /** Main method for tccg. */
    public static void main(String[] args) throws IOException, LexException { 
	String usage = "java opennlp.ccg.WebCCG " + 
"[-showall] [-showderivs] [-showsem] [-showfeats] [-visualize FILE] GRAMMARDIR\n" +
"\n" +
"-showall shows all parses rather than just the first one.\n" +
"-showderivs shows the derivation history of each parse.\n" +
"-showsem shows the logical form of each parse.\n" +
"-showfeats shows the features associated with each nonterminal.\n" +
"-visualize output a visualization of the parses into FILE (in TEX format).\n"+
"   to convert to an image, try this:\n" +
"   latex foo.tex; dvips foo.dvi | pstopnm | pnmtopng > foo.png\n" +
"\n" +
"Sentences to parse are read from standard input.\n";

	if (args.length > 0 && (args[0].equals("-h") || args[0].equals("-help"))) {
	    System.out.println("Usage: " + usage);
	    System.exit(0);
	}

	// args        
	//String prefsfile = null;
	boolean showall = false;
	boolean showderivs = false;
	boolean showsem = false;
	boolean showfeats = false;
	String visfile = null;
	int i;
	for (i = 0; i < args.length; i++) {
	    if (args[i].equals("-showall"))
		showall = true;
	    else if (args[i].equals("-showderivs"))
		showderivs = true;
	    else if (args[i].equals("-showsem"))
		showsem = true;
	    else if (args[i].equals("-showfeats"))
		showfeats = true;
	    else if (args[i].equals("-visualize"))
		visfile = args[++i];
	    else
		break;
	}
	if (i != args.length - 1) {
	    System.out.println("Usage: " + usage);
	    System.exit(0);
	}
	String grammarfile = args[i] + "/grammar.xml";

	// load grammar
	URL grammarURL = new File(grammarfile).toURI().toURL();
	//System.out.println("Loading grammar from URL: " + grammarURL);
	Grammar grammar = new Grammar(grammarURL);

	//if (grammar.getName() != null)
	//  System.out.println("Grammar '" + grammar.getName() + "' loaded.");

	// create parser and realizer
	Parser parser = new Parser(grammar);
	//Realizer realizer = new Realizer(grammar);

	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	while (true) {
	    String input = br.readLine();
	    if (input == null) break; // EOF
	    input = input.trim();
	    if (input.equals(""))
		continue;
	    try {
		parser.parse(input);
		List<Sign> parses = parser.getResult();
		Sign[] results = new Sign[parses.size()];
		parses.toArray(results);
		int resLength = results.length;
		System.out.print("\"" + input + "\": ");
		switch (resLength) {
		case 0: break;
		case 1: 
		    System.out.println(resLength + " parse found.\n"); 
		    break;
		default: System.out.println(resLength + " parses found.\n"); 
		}
                    
		Visualizer vis = null; 
		grammar.prefs.showSem = showsem;
		grammar.prefs.showFeats = showfeats;
		grammar.prefs.featsToShow = "";
		if (visfile != null) { 
		    vis = new Visualizer(); 
		    vis.writeHeader(visfile);
		}
		int numToShow = (showall) ? resLength : 1;
		for (i=0; i < numToShow; i++) {
		    Category cat = results[i].getCategory();
		    LF convertedLF = null;
		    if (cat.getLF() != null) {
			cat = cat.copy();
			Nominal index = cat.getIndexNominal(); 
			convertedLF = HyloHelper.compactAndConvertNominals(cat.getLF(), index, results[i]);
			cat.setLF(null);
		    }
		    String parseNum = (resLength == 1) ? "Parse: " : ("Parse "+(i+1)+": "); 
		    System.out.print(parseNum + cat.toString());
		    if (showsem && convertedLF != null) {
			System.out.println(" : ");
			System.out.println("  " + convertedLF.prettyPrint("  "));
		    }
		    else System.out.println();
		    if (showderivs) {
			System.out.println("------------------------------");
			System.out.println(results[i].getDerivationHistory());
		    }
		    if (visfile != null)
			vis.saveTeXFile(results[i], visfile);
		}
		if (visfile != null) {
		    vis.writeFooter(visfile);
		}
	    } catch(ParseException pe) {
		System.out.print("\"" + input + "\": ");
		System.out.println(pe + ".\n");
	    }
	}
    }
}
