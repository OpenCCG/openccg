///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2005-2009 Scott Martin, Rajakrishan Rajkumar and Michael White
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

//Class which stores unary rule freqs

//This class is invoked by the RulesExtr.xsl transform

package opennlp.ccgbank.extract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import opennlp.ccgbank.extract.ExtractGrammar.ExtractionProperties;

public class RulesTally {
	
	// Frequency cutoff for including an extracted rule 
	public static int RULE_FREQ_CUTOFF = 1;
    
    // Flag for whether to keep unmatched rules in extracted grammar
    public static boolean KEEP_UNMATCHED = true;
	
	//Unary Rule Freq
	private static Map<String,Integer> ruleFreq = new HashMap<String,Integer>();
	
	//Rule Occurrence
	private static Map<String,List<String>> ruleOccur = new HashMap<String,List<String>>();
	
	//Sentence id
	private static String id="";

	/** Resets the statically held tallies. */
	public static void reset() {
		ruleFreq = new HashMap<String,Integer>();
		ruleOccur = new HashMap<String,List<String>>();
		id="";
	}

	
	//Proc which traps and stores id of each sentence
	public String storeId(String x){
		if(x.length()>0){
			id = x;
			int posEquals = x.indexOf('=');
			if (posEquals > 0) {
				id = x.substring(posEquals+1);
			}
		}
		return id;
	}
	
	//Loads freq tables, returns rule name
	public String loadTally(String res, String arg) throws FileNotFoundException{
		
		arg = arg.replaceAll("_\\d", "");
		res = res.replaceAll("_\\d", "");
		String rule = arg+"_to_"+res;
		List<String> temp;
		
		//Freq table entry opened at first instance of rule
		if (!ruleFreq.containsKey(rule)) {
			ruleFreq.put(rule,1);
			temp = new ArrayList<String>(4);
			temp.add(id);
			ruleOccur.put(rule,temp);
		}
		else {
			int freq = ruleFreq.get(rule)+1;
			ruleFreq.put(rule,freq);
			temp = ruleOccur.get(rule);
		}
		
		//First 4 instances of rules stored
		if(temp.size()<4 && !temp.contains(id)){
			temp.add(id);
			ruleOccur.put(rule,temp);
		}
		
		return rule;
	}
	
	public static void printTally(ExtractionProperties extractProps) throws FileNotFoundException {
		RulesTally.printTally(new File(extractProps.tempDir));
	}
	
	//Rule Frequencies printed to file
	public static void printTally(File directory) throws FileNotFoundException{
		
		System.out.println("Generating RuleFreq.html");
		
		//Freq Output file
		File freqFile = new File(directory, "RuleFreq.html");
		PrintWriter output=new PrintWriter(new FileOutputStream(freqFile));
		
		List<String> ruleList = FreqTally.sortTally(ruleFreq);
		
		//Printing the final ouput in html form
		output.flush();
		output.println("<html>");
		output.println("<head>");
		output.println("<title>");output.println("Unary Rule Info");output.println("</title>");
		output.println("</head>");
		output.println("<body>");
		output.flush();
		
		String ccgbankHome = System.getProperty("CCGBANK_HOME", "/home/corpora/EN/ccgbank");
		
		for (int i=0; i<ruleList.size(); i++) {
			
			String rule = ruleList.get(i);
			int freq = ruleFreq.get(rule);
			
			output.flush();
			output.println("<p>");
			output.println(i+1+" Rule: "+rule+" Freq: "+freq);
			output.println("<p>");
			output.flush();
			
			List<String> rules = ruleOccur.get(rule);
			output.flush();
			
			output.println("<ul>");
			output.println("<li>");
			output.flush();
			
			for (int j=0; j<rules.size(); j++){
				output.println("<ul>");
				id=rules.get(j);

				String[]idInfo=id.split("\\.");

				StringTokenizer st=new StringTokenizer(id,".");

				String idLink="";
				String sentNo="";
				String dir="";

				//2 courses of action depending on whether input is gold std .auto parses or C&C .auto parses
				if(idInfo.length==2){
						idLink=st.nextToken()+".html";
						sentNo="#Sentence "+st.nextToken();
						dir=id.substring(4,6);
				}
				else {
						idLink=idInfo[0];
						sentNo="#Sentence "+idInfo[0];
						dir=idInfo[0];
				}
				
				//System.out.println(idLink);
				
				output.println("<li>");
				output.println(id);
				output.println(" <a href=file://" + ccgbankHome + "/original/data/HTML/"+dir+"/"+idLink+" target="+sentNo+">");
				output.println(idLink);
				output.println("</a>");
				output.println("</li>");
				output.println("</ul>");
			}
						
			output.flush();
			output.println("</li>");
			output.println("</ul>");
			output.println("");
			output.flush();
		}
		
		output.flush();
		output.println("</body>");
		output.println("</html>");
		output.flush();
		output.close();
	}
	
	
	//Invoked by RulesExtr.xsl to check repetition of categories
	public boolean checkRuleStatus(String rule) {
		Integer freq = ruleFreq.get(rule);
		return (freq != null && freq == 1);
	}
	
	//Checks the freq of a rule
	public boolean checkRuleFreqStatus(String rule){
		int freq = ruleFreq.get(rule);
		//Freqs >= cutoff accepted
		return (freq >= RULE_FREQ_CUTOFF);
	}
    
    // returns flag
    public boolean keepUnmatched() {
        return KEEP_UNMATCHED;
    }
}
