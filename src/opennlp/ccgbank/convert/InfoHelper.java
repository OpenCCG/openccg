///////////////////////////////////////////////////////////////////////////////
//Copyright (C) 2005-2009 Scott Martin, Rajakrishan Rajkumar and Michael White
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

//Java class which helps info extr from PTB

package opennlp.ccgbank.convert;

//import opennlp.ccg.lexicon.*;

import java.util.*;
import java.io.*;

public class InfoHelper{
	
	/** CCG terminal & non-terminal nos.. */
	private int termNo=0;private int ntNo=500;
	
	/** BBN-info. */
	private static Hashtable<String,String> bbnInfo=new Hashtable<String, String>();
	private static Hashtable<String,ArrayList<String>> bbnSpans=new Hashtable<String, ArrayList<String>>();
	private static ArrayList<String>bbnClasses=new ArrayList<String>();
	
	/** Quote info. */
	private static Hashtable<String,String> quoteInfo=new Hashtable<String, String>();
	
	/** PTB aux info viz. SBJ, FN_Tag & TPC annotation **/
	private static Hashtable<String,String> sbjInfo=new Hashtable<String, String>();
	private static Hashtable<String,String> fntagInfo=new Hashtable<String, String>();
	private static Hashtable<String,String> tpcInfo=new Hashtable<String, String>();
	
	/** Treenode info. */
	private static Hashtable<String,String> treeInfo=new Hashtable<String, String>();
	
	/** Directory where aux file and BBN NE info is stored. */
	static File auxFileDirectory = null, bbnAuxDirectory = null;
	
	/** Store aux file directories. */
	public static void init(File auxDir, File bbnAuxDir) {
		InfoHelper.auxFileDirectory = auxDir;
		InfoHelper.bbnAuxDirectory = bbnAuxDir;
	}
	
	/** Read BBN NE aux file corresponding to the WSJ Section provided as argument. */
	public static void readBBNAuxfiles(String sect){
		
		String bbnAuxFile=bbnAuxDirectory+"/"+"bbn-ccg"+sect+".aux";
		
		try{
			
			if(new File(bbnAuxFile).exists()){
				BufferedReader inp= new BufferedReader(new FileReader(bbnAuxFile));
				System.out.println("Reading in BBN aux file: "+bbnAuxFile);
				String line="";
				while((line=inp.readLine())!=null ){
					if(line.length()==0)continue;
					String bbn[]=line.trim().split(" ");
					String sentId=bbn[0];
					String span=bbn[1]+","+bbn[2];
					String key=sentId+" "+span;
					String bbnData="";
					
					for(int i=3;i<bbn.length;i++)
						bbnData=bbnData+" "+bbn[i];
					bbnData=bbnData.trim();
					//Remove colons in class names which seem to offend maxent
					bbnData=bbnData.replaceAll(":","|");
					bbnInfo.put(key,bbnData);
					ArrayList<String> spanList=new ArrayList<String>();
					if(!bbnSpans.containsKey(sentId))
						bbnSpans.put(sentId,spanList);
					spanList=bbnSpans.get(sentId);
					spanList.add(span);
					bbnSpans.put(sentId,spanList);
					
				}
				inp.close();
			}
		}
		catch(IOException e){
			System.out.println("Error reading: "+bbnAuxFile);
		}
	}
	
	/** Read quotes aux file corresponding to the WSJ Section provided as argument. */
	public static void readQuoteAuxfiles(String sect){
		
		String quoteAuxFile=auxFileDirectory+"/"+"aux-quotes-"+sect+".txt";
		
		try{
			
			if(new File(quoteAuxFile).exists()){
				System.out.println("Reading in quotes aux File: "+quoteAuxFile);
				String line="";
				BufferedReader inp= new BufferedReader(new FileReader(quoteAuxFile));
				while((line=inp.readLine())!=null ){
					
					if(line.length()==0)continue;
					String quoteData[]=line.trim().split(" ");
					String sentId=quoteData[0];
					String span=quoteData[1]+","+quoteData[2];
					String key=sentId+" "+span;
					String quotedText="";
					
					for(int i=3;i<quoteData.length;i++)
						quotedText=quotedText+" "+quoteData[i];
					quotedText=quotedText.trim();
					quoteInfo.put(key,quotedText);
				}
				inp.close();
			}
		}
		catch(IOException e){
			System.out.println("Error reading: "+quoteAuxFile);
		}
	}
	
	/** Read PTB aux file (sbj, fn-tag and tpc info) corresponding to the WSJ Section provided as argument. */
	public static void readPTBAuxfiles(String sect){
		
		String ptbAuxFile=auxFileDirectory+"/"+"ptb-aux-"+sect+".txt";
		
		try{
			
			if(new File(ptbAuxFile).exists()){
				System.out.println("Reading in PTB aux file: "+ptbAuxFile);
				//Example: wsj_0098.16 SBJ VBZ_uses_31 NN_company_30 Arg0
				String line="";
				BufferedReader inp= new BufferedReader(new FileReader(ptbAuxFile));
				while((line=inp.readLine())!=null ){
					line=line.trim();
					if(line.length()==0)continue;
					String[]info=line.split(" ");
					String wsjId=info[0];
					String label=info[1];
					String head=info[2];
					String dep=info[3];
					String rel=info[4].replaceFirst("ARG","Arg");
					//Store info in appropriate hastable
					if(label.equals("SBJ")){
						String key=wsjId+" "+head;
						sbjInfo.put(key,rel);
					}
					else{
						String key=wsjId+" "+dep;
						if(label.equals("FNT")){
							fntagInfo.put(key,rel);
						}
						else if(label.equals("TPC")){
							tpcInfo.put(key,rel);
						}
					}
				}
				inp.close();
			}
		}
		catch(IOException e){
			System.out.println("Error reading: "+ptbAuxFile);
		}
	}
	
	/** Read tree aux file corresponding to the WSJ Section provided as argument. */
	public static void readTreeAuxfiles(String sect){
		
		String treeAuxFile=auxFileDirectory+"/"+"tree-aux-"+sect+".txt";
		
		try{
			
			if(new File(treeAuxFile).exists()){
				System.out.println("Reading in Tree aux file: "+treeAuxFile);
				BufferedReader inp= new BufferedReader(new FileReader(treeAuxFile));
				String line="";
				while((line=inp.readLine())!=null ){
					if(line.length()==0)continue;
					String[] treeParts=line.split(" ");
					if(treeParts.length!=3)
						continue;
					String key=treeParts[0]+" "+treeParts[1];
					String catId=treeParts[2];
					treeInfo.put(key,catId);
				}
				inp.close();
			}

		}
		catch(IOException e){
			System.out.println("Error reading: "+treeAuxFile);
		}
	}
	

	private ArrayList<String> treeCont=new ArrayList<String>();
	public boolean checkTreeInfo(String header,String ntId,int numCats){
		
		String key=header+" "+ntId;
		
		if(treeInfo.containsKey(key)){
			String treeCats=treeInfo.get(key);
			String[] x=treeCats.split(",");
			
			if(numCats==x.length)
				return true;
			else return false;
		}
		else return false;
	}
	
	/** Retrieve treenode info. */
	public String getTreeInfo(String key){
		
		String retVal="";
		treeCont=new ArrayList<String>();
		
		if(treeInfo.containsKey(key)){
			retVal=treeInfo.get(key);
			String[]temp=retVal.split(",");
			
			for(String x:temp)
				treeCont.add(x);
		}
		
		return retVal;
	}
	
	/** Procedure which gives back id of a particular cat. */
	public String getTreeId(){
		
		String retVal="";
		
		if(treeCont.size()>0){
			retVal=treeCont.get(0);
			treeCont.remove(0);
			String x[]=retVal.split("_");
			retVal=x[1];
			if(x.length==3)
				retVal="M_"+retVal;
		}
		
		return retVal;
	}
	
	/** Procedure which gives slash of combination. */
	public String getTreeSlash(){
		
		String retVal="";
		if(treeCont.size()>0){
			String slash=treeCont.get(0);
			String mode="";
			
			if(slash.length()>1)
				mode=Character.toString(slash.charAt(1));
			else if(slash.startsWith("/"))
				mode=">";
			else if(slash.startsWith("\\"))
				mode="<";
			treeCont.remove(0);
			retVal=Character.toString(slash.charAt(0));
			retVal=retVal+"_"+mode;
			
		}
		
		return retVal;
	}
	
	/** Retrieve BBN class for lexical items for use in the Leafnodes. */
	public String getBBNClass(String header,String lex,String pos,String cat,int nodeInd){
		
		String retVal="";
		String semClass="";
		
		//Check and exit if the pos is not relevant
		boolean relFlag=false;
		
		if(cat.matches("pp\\[[a-z]+\\]_~2/np_2"))
			return "";
		
		if (pos.startsWith("NN") || pos.startsWith("RB") || pos.startsWith("JJ") || pos.startsWith("VB") ||pos.equals("CD") || lex.equals("%") || pos.equals("$"))
			relFlag=true;
		
		if(!relFlag)
			return "";
		
		String sentId=header.replaceFirst("ID=","");
		ArrayList<String> spanList=new ArrayList<String>();
		
		String key=sentId+" "+Integer.toString(nodeInd)+","+Integer.toString(nodeInd);
		if(bbnInfo.containsKey(key)){
			
			String bbnData=bbnInfo.get(key);
			String info[]=bbnData.split(" ");
			
			if(info.length>=2){
				
				semClass=getCleanClass(info[0]);
				retVal=(classReplace(semClass,lex,info[1])).trim();
				if(retVal.length()>0 && !bbnClasses.contains(retVal))
					bbnClasses.add(retVal);
			}
			
			return retVal;
		}
		else if(bbnSpans.containsKey(sentId))
			spanList=bbnSpans.get(sentId);
		
		for(String span: spanList){
			
			String inds[]=span.split(",");
			if(inds.length!=2)continue;
			
			if(!inds[0].matches("[0-9]+") || !inds[1].matches("[0-9]+") || inds[0].equals("NA") || inds[1].equals("NA"))
				continue;
			
			int ind1=Integer.parseInt(inds[0]);
			int ind2=Integer.parseInt(inds[1]);
			if(nodeInd >=ind1 && nodeInd <=ind2){
				
				key=sentId+" "+span;
				
				String bbnData=bbnInfo.get(key);
				String info[]=bbnData.split(" ");
				
				if(info.length==0)
					continue;
				
				semClass=getCleanClass(info[0]);
				
				//Compile a list of acceptable classes
				boolean accClasses=false;
				
				if (!semClass.startsWith("DATE") && !semClass.startsWith("TIME") && !semClass.startsWith("ORDINAL") && !semClass.startsWith("QUANTITY") && !semClass.startsWith("PERCENT") && !semClass.startsWith("MONEY")) 
					accClasses=true;
				
				if (accClasses || pos.startsWith("NN") || pos.equals("CD") || lex.equals("%") || pos.equals("$")){
					String wordBit="";
					int relInd=nodeInd-ind1+1;
					
					if(relInd<info.length && relInd>=0)
						wordBit=info[(nodeInd-ind1+1)];
					
					retVal=classReplace(semClass,lex,wordBit);
					if(retVal.length()>0 && !bbnClasses.contains(retVal))
						bbnClasses.add(retVal);
					break;
				}
				
			}
		}
		
		return retVal;
		
	}
	
	/** Perform semantic replacement over relevant parts of the part. */
	public String classReplace(String semClass,String lex,String wordBit){
		
		String retVal="";
		
		/*CITY-based classes
		if(wordBit.equals(lex))
			retVal=semClass;
		else if(lex.contains(wordBit))
			retVal=lex.replaceFirst(wordBit,semClass);*/
		
		//Ignore CITY-based classes
		if(wordBit.equals(lex))
			retVal=semClass;
		
		return retVal;
		
	}
	
	/** Strip off label like ENAMEX,TIMEX,NUMEX. */
	public String getCleanClass(String semClass){
		
		String retVal="";
		String x[]=semClass.split("=");
		if(x.length>=2)
			retVal=x[1];
		
		return retVal;
	}
	
	/** Retrieve stored bbn-info for use in the Treenodes. */
	public String getBBNInfo(String header,String span,String words){
		String sentId=header.replaceFirst("ID=","");
		String bbnData="";
		String key=sentId+" "+span;
		
		boolean legitPhr=false;
		
		//Checking stored BBN-data with actual words
		if(bbnInfo.containsKey(key)){
			String[] ccgWords=words.split("_");
			bbnData=bbnInfo.get(key);
			String[] bbnWords=bbnData.split(" ");
			
			if(ccgWords.length>0 && bbnWords.length>1 && ccgWords.length==bbnWords.length-1){
				for(int i=0;i<ccgWords.length;i++){
					
					if(ccgWords[i].toLowerCase().contains(bbnWords[i+1].toLowerCase()))
						legitPhr=true;
					else{
						legitPhr=false;
						break;
					}
				}
			}
			else legitPhr=false;
			
			if(legitPhr){
				bbnData=bbnData+" "+span;
			}
			else bbnData="";
		}
		
		return bbnData.trim();
	}
	
	/** Get collapsed entity status. */
	private boolean collStatus=false;
	public boolean getCollapseStatus(){
		boolean retVal=collStatus;
		collStatus=false;
		return retVal;
	}
	
	private String collPhr="";
	public String collapse(String word,String choice){
		
		String retVal="";
		
		if(choice.equals("1"))
			collPhr=collPhr+"_"+word;
		else{
			retVal=collPhr.trim();
			retVal=retVal.replaceFirst("_","");
			collPhr="";
		}
		
		return retVal;
	}
	
	/** Retrieve stored quotes-info for use in the Treenodes. */
	public String getQuoteInfo(String header,int ind1,int ind2,String words){
		
		String sentId=header.replaceFirst("ID=","");
		String span1=Integer.toString(ind1)+","+Integer.toString(ind2);
		String span2=Integer.toString(ind1)+","+Integer.toString(ind2+1);
		String quotedText="";
		String retVal="";
		String key1=sentId+" "+span1;
		String key2=sentId+" "+span2;
		
		if(quoteInfo.containsKey(key1)){
			quotedText=quoteInfo.get(key1);
			String []qInfo=quotedText.split(" ");
			if(qInfo.length>0)
				retVal=span1+" "+qInfo[0];
		}
		else if(quoteInfo.containsKey(key2)){
			quotedText=quoteInfo.get(key2);
			String []qInfo=quotedText.split(" ");
			
			if(qInfo.length>0 && qInfo[qInfo.length-1].matches("\\p{Punct}")){
				retVal=span2+" "+qInfo[0]+" "+qInfo[qInfo.length-1];
			}
			
		}
		
		return retVal;
	}
	
	/** Store result cat. */
	String res="";
	public void storeRes(String str){
		this.res=str.replaceAll("\\[.*","");
	}
	
	/** Retrieve result cat. */
	public String getRes(){
		String retVal=this.res;
		return retVal;
	}
	
	/** The store of ids. */
	private Hashtable<String,Integer> idTally=new Hashtable<String, Integer>();
	public String id(String cat){
		
		String retVal="";
		
		int idNum=idTally.size()+2;
		if(!idTally.containsKey(cat))
			idTally.put(cat,idNum);
		else 
			idNum=idTally.get(cat);
		
		if(idTally.size()==1)
			retVal="first"+"_"+Integer.toString(idNum);
		else retVal="later"+"_"+Integer.toString(idNum);
		
		return retVal;
		
	}
	
	public void id(){
		idTally=new Hashtable<String, Integer>();
	}
	
	/** Retrieve terminal no. */
	public String getTermNo(){
		String tn=Integer.toString(this.termNo);
		this.termNo++;
		return tn;
	}
	
	/** Retrieve non-terminal no. */
	public String getNonTermNo(){
		String ntNo=Integer.toString(this.ntNo);
		this.ntNo++;
		return ntNo;
	}
	
	/** Get punctless index. */
	int plessInd=0;
	public String getPunctlessIndex(String word){
		boolean isCCGWord=this.isCCGWord(word);
		int retval=-1;
		if(isCCGWord){
			retval=this.plessInd;
			this.plessInd++;
		}
		return Integer.toString(retval);
	}
	
	/** Given a lexical item, ascertain whether it is a legit original CCGbank word **/
	public boolean isCCGWord(String word){
		
		boolean retval=true;
		if(word.matches("\\p{Punct}|[\\.]+|(-lrb-)|(-rrb-)|(-lcb-)|(-rcb-)|(--)|(`)|(')|(``)|('')") && !word.equals("$") && !word.equals("%")){
			retval=false;
		}
		return retval;
	}
	
	/** Init terminal nos. */
	public String initId(){
		this.termNo=0;
		this.ntNo=500;
		this.plessInd=0;
		return null;
	}
	
	/** Extract PTB SBJ,FN-TAG &  TPC annotation. */
	public String getPTBInfo(String label,String sentId,String head,String lexInd){
		
		String retval="";
		sentId=sentId.replaceFirst("ID=","");
		String key=sentId+" "+head+"_"+lexInd;
		String rel=null;
		
		if(label.equals("SBJ"))rel=sbjInfo.get(key);
		else if(label.equals("FNT"))rel=fntagInfo.get(key);
		else if(label.equals("TPC"))rel=tpcInfo.get(key);
		
		if(rel!=null){
			retval=rel;
		}
		return retval;
	}
	
	/** Print out BBN classes (for use in grammar.xml). */
	public void printBBNClasses(){
		
		try{
			
			System.out.println("Printing BBN classes used in the corpus to bbn-types.txt (for use in grammar.xml)");
			// Create a FileWriter stream to the file
			FileWriter file_writer = new FileWriter ("bbn-types.txt");
			BufferedWriter buf_writer = new BufferedWriter (file_writer);
			PrintWriter print_writer = new PrintWriter (buf_writer,true);
			print_writer.print("<tokenizer replacement-sem-classes=\"");
			
			for(int i=0;i<bbnClasses.size();i++){
				
				String bbn=bbnClasses.get(i);
				if(i==0)
					print_writer.print(bbn);
				else print_writer.print(" "+bbn);
				if(i==bbnClasses.size()-1)
					print_writer.print("\"/>");
				print_writer.flush();
			}
			print_writer.close();
		}
		catch (Exception e){
			System.err.println ("Error writing info to file");
		}
	}
	
	/** Input a string which contains a ':' and replace it by '|'. */
	public String replaceColon(String str) {
		
		String replacedStr=str.replace(":","|");
		return replacedStr;
	}	
	
}
