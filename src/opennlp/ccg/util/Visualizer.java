///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004 Alexandros Triantafyllidis and 
//                    University of Edinburgh (Michael White)
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
package opennlp.ccg.util;

import opennlp.ccg.lexicon.*;
import opennlp.ccg.grammar.*;
import opennlp.ccg.synsem.*;

import java.io.*;
import java.util.*;

/**
 * Class for visualizing CCG derivations using latex.
 *
 * @author  Alexandros Triantafyllidis
 * @author  Michael White
 * @version $Revision: 1.9 $, $Date: 2009/12/21 03:27:18 $
 */
public class Visualizer {

    private String ruleToTeX(String rule, int indent, int length) {
        StringBuffer sb = new StringBuffer();
        int i=0;
        for (i=0; i < indent; i++) sb.append("&");
        sb.append(" \\mc{" + Integer.toString(length) + "} ");
        if(rule == null || rule.length()==1)
            sb.append("{\\hrulefill_{"+ rule  + "}} \\\\\n"); 
        else
            sb.append("{\\hrulefill_{"+ rule.substring(0,1) + "\\mathbf{" + rule.substring(1) + "}}}\\\\\n");
        return sb.toString();
    }
    
    // Gets a file name for a temporary file, e.g.:  ~tmp0
    public String getTempFileName() {
        File f1 = null;
        File f2 = null;
        int i=0;
        f1 = new File("~tmp"+Integer.toString(i)+".tex");
        f2 = new File("~tmp"+Integer.toString(i)+".div");
        while(f1.exists() || f2.exists() ) {
            i++;
            f1 = null; f2 = null;
            f1 = new File("~tmp"+Integer.toString(i)+".tex");
            f2 = new File("~tmp"+Integer.toString(i)+".div");
        }
        f1 = null; f2 = null;
        return "~tmp"+Integer.toString(i);
    }
    
    public String getTempDirName() {
        File f1 = null;
        int i=0;
        f1 = new File("tmp");
        while(f1.exists()  ) {
            i++;
            f1 = null; 
            f1 = new File("tmp"+Integer.toString(i) );
        }
        f1 = null; 
        return "tmp"+Integer.toString(i);
    }
    
    public int getTreeDepth(Sign sign, int level)
    {
        int max_depth = 0, depth = 0;
        Sign[] children = sign.getDerivationHistory().getInputs();
        if (children != null && sign.getWords().size() > 1) 
            for (int i=0; i < children.length; i++) {
                depth = getTreeDepth(children[i], level+1);
                if (depth > max_depth) max_depth = depth;
            }
        else
            max_depth = level;
        return max_depth;
    }

    private int numberOfLeaves(Sign results) {
        int totalLeaves = 0;    
        Sign[] children = results.getDerivationHistory().getInputs();
        if (children==null || results.getWords().size()==1) return 1;
        for(int i=0;i<children.length;i++)
            totalLeaves+=numberOfLeaves(children[i]);
        return totalLeaves;
    }
    
    public List<TeXSign> processSign(Sign results, int level, int identation) {
        List<TeXSign> signList = new ArrayList<TeXSign>();
        Sign[] children = results.getDerivationHistory().getInputs();
        int depth = getTreeDepth(results, 0);
        TeXSign ts = new TeXSign();
        int offset=0;
        if (children != null && results.getWords().size() > 1)
            for (int i=0; i < children.length; i++) {
                if (i > 0)
                    offset += numberOfLeaves(children[i-1]);
                signList.addAll( processSign(children[i], level + 1, identation + offset) );
            }
        ts.identation = identation;
        ts.height = depth;
        ts.sign = results;
        signList.add(ts);
        return signList;
    }

    public boolean writeFooter(String fileName){
        java.io.BufferedWriter bw = null;
        try{    
            bw = new java.io.BufferedWriter(new FileWriter(fileName,true) );  
            bw.write("\\end{document}\n");   
            bw.close();
        }
        catch(Exception e){ return false; }
        return true;
    }

    public  boolean writeHeader(String fileName) {
        java.io.BufferedWriter bw = null;
        try {
            bw = new java.io.BufferedWriter(new FileWriter(fileName) );  
            bw.write("\\documentclass{article}\n");
            bw.write("\\usepackage[margin=0.5in]{geometry}\n");
            bw.write("\\newcommand{\\deriv}[2]\n");
            bw.write("{  \\renewcommand{\\arraystretch}{.5}\n");
            bw.write("$\\begin{array}[t]{*{#1}{c}}\n");
            bw.write("     #2\n");
            bw.write("   \\end{array}$ }\n");
            bw.write("\\newcommand{\\gf}[1]{\\textsf{\\textsl{#1}}}\n");
            bw.write("\\newcommand{\\cf}[1]{\\mbox{\\ensuremath{\\cfont{#1}}}}\n");
            bw.write("\\newcommand{\\uline}[1]\n");
            bw.write("{\\mc{#1}{\\hrulefill} }\n");
            bw.write("\\newcommand{\\mc}[2]\n");
            bw.write("  {\\multicolumn{#1}{c}{#2}}\n");
            bw.write("\\newcommand{\\cfont}{\\mathsf}\n");
            bw.write("\\newcommand{\\bs}{\\backslash}\n");
            bw.write("\\newcommand{\\subsa}[1]{\\hspace{-0.75mm}_{_{#1}}}\n");
            bw.write("\\newcommand{\\subsb}[1]{\\hspace{-0.10mm}_{_{#1}}}\n");
            bw.write("\\newcommand{\\subs}[1]{\\hspace{-0.40mm}_{#1}}\n");
            bw.write("\\newcommand{\\subsf}[1]{\\hspace{-0.75mm}_{_{#1}}}\n");
            bw.write("\\newcommand{\\supsa}[1]{\\hspace{-1.75mm}^{^{#1}} }\n");
            bw.write("\\newcommand{\\supsb}[1]{\\hspace{-0.80mm}^{^{#1}}  }\n");
            bw.write("\\newcommand{\\sups}[1]{\\hspace{-0.40mm}^{#1}}\n");
	    bw.write("\\pagestyle{empty}\n");
            bw.write("\\begin{document}\n");
            bw.close();
        }
        catch(Exception e){ return false; }
        return true;
    }

    /** 
     * Shows the current derivation using YaP or xdvi.
     */
    public boolean show(String fileName) {
        String viewerName = null;
        try {
        	runCommand("latex " + fileName + ".tex");
            //Process p =
        	java.lang.Runtime.getRuntime().exec("latex " + fileName + ".tex");
            if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
                viewerName = "yap";
            else
                viewerName = "xdvi";

            System.out.println("Close " + viewerName  + " to continue ...");
            runCommand(viewerName + " " + fileName);
            // The process will wait indefinitely unless we close each of the related streams:/
            //p.getInputStream().close();
            //p.getOutputStream().close();
            //p.getErrorStream().close();
            //p.waitFor();
            //p = null;
            //p = java.lang.Runtime.getRuntime().exec(viewerName + " " + fileName);
            //p.getInputStream().close();
            //p.getOutputStream().close();
            //p.getErrorStream().close();
            //System.out.println("Close " + viewerName  + " to continue ...");
            //p.waitFor();
        } catch(Exception e) {
            System.out.println("Error invoking latex/" + viewerName + " : " + e.toString());
            return false;
        }
        return true;
    }
    
    class myFilter implements FileFilter {
        String baseFileName=null;
        public myFilter(String s) {
            baseFileName = s.toUpperCase();
        }
        public boolean accept(File f) {
            System.out.println("checking: " + f.getName());
            if(f.getName().toUpperCase().startsWith(baseFileName))
                return true;
            else
                return false;
        }
    }
    
    public class myFileNameFilter implements FilenameFilter {
        public String fn=null;
        public myFileNameFilter(String s) {
            fn=s;
        }
        public boolean accept(File dir, String name) { 
            return name.startsWith(fn);
        }
    }
    
    public boolean cleanFiles(String fileName) {
        try {
            File dir = new File(System.getProperty("user.dir")); 
            myFileNameFilter filter = new  myFileNameFilter( fileName) ;
            File[] allFiles = dir.listFiles(filter);
            for(int i=0;i<allFiles.length;i++)
                allFiles[i].delete();
        } catch (Exception e) {
            System.out.println("Error cleaning files: "+ e.toString());
            return false;
        }
        return true;
    }

    public class auxFileNameFilter implements FilenameFilter {
        public String fn=null;
        public auxFileNameFilter(String s) {
            fn=s;
        }
        public boolean accept(File dir, String name) { 
            if((name.startsWith(fn)) && (! ((name.toUpperCase().endsWith(".TEX")) || (name.toUpperCase().endsWith(".DVI")))))
                return true;
            else
                return false;
        }
    }

    public boolean cleanAuxFiles(String fileName) {
        try{
            File dir = new File(System.getProperty("user.dir")); 
            auxFileNameFilter filter = new  auxFileNameFilter( fileName) ;
            File[] allFiles = dir.listFiles(filter);
            for(int i=0;i<allFiles.length;i++)
                allFiles[i].delete();
        } catch (Exception e) {
            System.out.println("Error cleaning files: "+ e.toString());
            return false;
        }
        return true;
    }
    
    /** 
     * Writes a derivation in fileName in TeX format; 
     * returns false in case it fails.
     */
    public boolean saveTeXFile(Sign results, String fileName) {
        List<TeXSign> signList = null;
        java.io.BufferedWriter bw = null;
        try {
            int i=0, numDerivs=0;
            TeXSign texSign = null;
            bw = new java.io.BufferedWriter(new FileWriter( fileName,true) );  
            signList = sortList( processSign(results,0, 0 ) );
            numDerivs = results.getWords().size();
            Tokenizer tokenizer = Grammar.theGrammar.lexicon.tokenizer;
            bw.write("\\deriv{" + Integer.toString(numDerivs)  +  "}{\n");
            for (i=0; i < results.getWords().size(); i++) {
                if (i != 0) bw.write(" & ");
                String orth = tokenizer.getOrthography((Word)results.getWords().get(i), false);
                orth = orth.replaceAll("_", "\\\\_");
                orth = orth.replaceAll("%", "\\\\%");
                bw.write("\\gf{" + orth + "}");
            }
            bw.write(" \\\\\n\\uline{1}");
            for (i=1; i < results.getWords().size(); i++)
                bw.write(" & \\uline{1}");
            bw.write(" \\\\\n");
            texSign = (TeXSign)signList.get(0);
            bw.write("\\cf{"+  texSign.sign.getCategory().toTeX()  + "}");
            for (i=1; i < numDerivs; i++) {
                texSign = (TeXSign)signList.get(i);
                bw.write(" & \\cf{"+  texSign.sign.getCategory().toTeX()  + "}");
            }
            bw.write(" \\\\\n");    

            for (i=numDerivs; i < signList.size(); i++) {
                String ruleStr=null;
                texSign = (TeXSign)signList.get(i);
                ruleStr = ruleToTeX(texSign.sign.getDerivationHistory().getRule().name(), texSign.identation, texSign.sign.getWords().size()   );
                bw.write(ruleStr);
                for (int j=0; j < texSign.identation; j++)
                    bw.write("&");
                bw.write(" \\mc{" + texSign.sign.getWords().size() + "}{\\cf{"+ texSign.sign.getCategory().toTeX() +"}} \\\\\n");
            }
	    // Originally 1in, but that's too much when displayed onscreen
            bw.write("}\n\n\\vspace{5mm}\n\n");
            bw.close();
        } catch(Exception e) {
            System.out.println("Error while saving to TeX: " + e.toString()); 
            e.printStackTrace();
            return false; 
        }
        
        return true;
    }

    private List<TeXSign> sortList(List<TeXSign> signList) {
        for (int i=0; i < signList.size(); i++)
            for(int j=i; j < signList.size(); j++) {
                TeXSign texSign1 = signList.get(i);
                TeXSign texSign2 = signList.get(j);
                if(texSign1.height > texSign2.height) { 
                    signList.set(i,texSign2); signList.set(j, texSign1);  }
                if(texSign1.height == texSign2.height)
                if(texSign1.identation > texSign2.identation) { 
                    signList.set(i,texSign2); signList.set(j, texSign1);  }
            }
        return signList;
    }

    private class TeXSign {
        Sign sign = null;
        int identation = 0;
        int height = 0;
    }

    /**
     * Calls runCommand/2 assuming that wait=true.
     *
     * @param  cmd  The string containing the command to execute
     */
    public static void runCommand (String cmd) {
	runCommand(cmd, true);
    }

    /**
     * Run a command with the option of waiting for it to finish.
     *
     * @param  cmd  The string containing the command to execute
     * @param  wait True if the caller should wait for this thread to 
     *              finish before continuing, false otherwise.
     */
    public static void runCommand (String cmd, boolean wait) {
	try {
            //System.out.println("Running command: "+ cmd);
	    Process proc = Runtime.getRuntime().exec(cmd);

	    // This needs to be done, otherwise some processes fill up
	    // some Java buffer and make it so the spawned process
	    // doesn't complete.
            BufferedReader br = 
		new BufferedReader(new InputStreamReader(proc.getInputStream()));
            //String line = null;
            //while ( (line = br.readLine()) != null) {
            while ( (br.readLine()) != null) {
		; // just eat up the inputstream

		// Use this if you want to see the output from running
		// the command.
		//System.out.println(line);
	    }

	    if (wait) {
		try {
		    proc.waitFor();
		} catch (InterruptedException e) {
		    Thread.currentThread().interrupt();
		}
	    }
	    proc.getInputStream().close();
	    proc.getOutputStream().close();
	    proc.getErrorStream().close();
	} catch (IOException e) {
	    System.out.println("Unable to run command: "+cmd);
	}
    }

}
