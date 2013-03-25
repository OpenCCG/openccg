package opennlp.ccg.hylo;

///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2011 Rajakrishnan Rajkumar
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

import opennlp.ccg.lexicon.Word;
import opennlp.ccg.perceptron.Alphabet;
import opennlp.ccg.perceptron.FeatureExtractor;
import opennlp.ccg.perceptron.FeatureMap;
import opennlp.ccg.perceptron.FeatureVector;
import opennlp.ccg.synsem.AtomCat;
import opennlp.ccg.synsem.Category;
import opennlp.ccg.synsem.ComplexCat;
import opennlp.ccg.synsem.Sign;
import opennlp.ccg.unify.FeatureStructure;
import opennlp.ccg.unify.SimpleType;
import opennlp.ccg.util.TrieMap;
import opennlp.ccg.hylo.LexDependency;
import java.util.*;

/**
 * Class which extracts subject verb and relative pronoun agreement features for
 * English (described in):
 * 
 * @InProceedings{rajkumar-white:2010:POSTERS, 
 * 	author = {Rajkumar, Rajakrishnan and White, Michael}, 
 * 	title = {Designing Agreement Features for Realization Ranking}, 
 * 	booktitle = {Coling 2010: Posters}, 
 * 	month = {August}, 
 * 	year = {2010}, 
 * 	address = {Beijing, China}, 
 * 	publisher = {Coling 2010 Organizing Committee}, 
 * 	pages = {1032--1040}, 
 * 	url = {http://www.aclweb.org/anthology/C10-2119} 
 * }
 * 
 * The class extracts features based on the OpenCCG HLDS specific LF rels: ArgN (subject rel), whApposRel, GenRel, First, Next
 * 
 * @author raja
 * @version $Revision: 1.11 $, $Date: 2011/11/25 18:18:33 $
 */
public class EnglishAgreementExtractor implements FeatureExtractor{

	/** Feature map wrapper, for unique retrieval from a sign's data objects. */
	public static class FeatureMapWrapper {
		public FeatureMap featureMap;
		public FeatureMapWrapper(FeatureMap featureMap) { this.featureMap = featureMap;}
	}
	
	/** Inner class to store specific properties of signs (right now for unbalanced punctuation status). */
	private class SignProps{

		//Store comma/dash unbalanced punctuation
		private String unbalPunct=null;
		
		/** Constructor to specify unbalanced punctuation. */
		public SignProps(String unbalPunct){
			this.unbalPunct=unbalPunct;
		}
		
		public String getUnbalancedPunct(){
			return unbalPunct;
		}
	}
	
	/** The alphabet. */
	protected Alphabet alphabet = null;

	/** Current feature map. */
	protected FeatureMap currentMap = null;
	
	/** Head and dependent signs (For feature extraction) .*/
	protected Sign headSign=null;
	protected Sign depSign=null;
	
	/** Error analysis related. */
	//Sentence id
	String sentId=null;
	//Instance num
	int INSTANCENUM=0;
	
	/** Subject-verb agreement feature extractors. */
	protected List<List<TrieMap.KeyExtractor<String>>> agrExtractors = new ArrayList<List<TrieMap.KeyExtractor<String>>>();
	protected List<List<TrieMap.KeyExtractor<String>>> agrConjExtractors = new ArrayList<List<TrieMap.KeyExtractor<String>>>();
	protected List<List<TrieMap.KeyExtractor<String>>> agrOfComplementExtractors = new ArrayList<List<TrieMap.KeyExtractor<String>>>();
	
	/** WH-pronoun agreement feature extractors. */
	protected List<List<TrieMap.KeyExtractor<String>>> whExtractors = new ArrayList<List<TrieMap.KeyExtractor<String>>>();
	protected List<List<TrieMap.KeyExtractor<String>>> whConjExtractors = new ArrayList<List<TrieMap.KeyExtractor<String>>>();
	
	/** Punctuation agreement feature extractors. */
	protected List<List<TrieMap.KeyExtractor<String>>> punctExtractor = new ArrayList<List<TrieMap.KeyExtractor<String>>>();
	
	/** Constructors. */
	
	//Constructor used during actual perceptron training and testing
	public EnglishAgreementExtractor() {
		
		// init lazy feature extractors
		this.init();
	}
	
	//Constructor used during error analysis using serialized signs
	public EnglishAgreementExtractor(String sentId) {
		
		//init lazy feature extractors
		this.sentId=sentId;
		this.INSTANCENUM=0;
		this.init();
	}
	
	/** Sets the alphabet. */
	public void setAlphabet(Alphabet alphabet) {
		this.alphabet = alphabet;
	}
	
	/** Initializes lazy feature extractors .*/
	public void init() {
	
		//Agreement: Simple subj-verb feature extractors
		this.agrExtractors.add(dep_word_head_word(1));
		this.agrExtractors.add(dep_word_head_pos(1));
		this.agrExtractors.add(dep_pos_head_word(1));
		this.agrExtractors.add(dep_pos_head_pos(1));

		//Agreement: Disjunct subj feature extractors
		this.agrConjExtractors.add(dep_word_head_word(2));
		this.agrConjExtractors.add(dep_word_head_pos(2));
		this.agrConjExtractors.add(dep_pos_head_word(2));
		this.agrConjExtractors.add(dep_pos_head_pos(2));
		
		//Agreement: Of-complement feature extractors
		this.agrOfComplementExtractors.add(dep_word_head_word(3));
		this.agrOfComplementExtractors.add(dep_word_head_pos(3));
		this.agrOfComplementExtractors.add(dep_pos_head_word(3));
		this.agrOfComplementExtractors.add(dep_pos_head_pos(3));
		
		//WH-pronoun: Simple relative pronoun feature extractors
		this.whExtractors.add(dep_word_head_stem(4));
		this.whExtractors.add(dep_word_head_pos(4));
		this.whExtractors.add(dep_word_head_class(4));
		
		//WH-pronoun: Conjunct/Disjunct subj feature extractors
		this.whConjExtractors.add(dep_word_head_stem(5));
		this.whConjExtractors.add(dep_word_head_pos(5));
		this.whConjExtractors.add(dep_word_head_class(5));
		
		//Unbalanced punctuation
		this.punctExtractor.add(unbal_punct());
		
	}
	
	/** Returns the features for the given sign and completeness flag. */
	public FeatureVector extractFeatures(Sign sign, boolean complete) {
		addFeatures(sign, complete);
		return getFeatureMap(sign);
	}
	
	/** Recursively adds features to the feature map for the given sign, if not already present. */
	//TODO: Lazier feature extraction involving conditional feature extractors
	protected void addFeatures(Sign sign, boolean complete) {
		// check for existing map, otherwise make one
		if (getFeatureMap(sign) != null) return;
		// lex case
		if (sign.isLexical()) {
			currentMap = new FeatureMap(0);
		}
		// non-terminal
		else {
			Sign[] inputs = sign.getDerivationHistory().getInputs();
			// first recurse
			for (Sign child : inputs) addFeatures(child, false);
			// use input maps in making current map
			if (inputs.length == 1) {
				currentMap = new FeatureMap(getFeatureMap(inputs[0]));
			}
			else if (inputs.length == 2) {
				currentMap = new FeatureMap(getFeatureMap(inputs[0]), getFeatureMap(inputs[1]));
			}

			String subjArg=null;
			
			//do each newly filled dep
			for (LexDependency dep : sign.getFilledDeps()) {
				
				this.headSign=dep.lexHead;
				this.depSign=dep.lexDep;
				//System.out.println("DEP: "+dep);
				
				//Find value of the subject feature
				if(subjArg==null){
					subjArg=getSubjectFeature(dep.lexHead.getCategory());
					//Back-off to Arg0 heuristic if subject feature not available for this verb
					if(subjArg==null)subjArg="Arg0";
				}
				
				//Subject-verb agr features
				if(subjArg.equals(dep.rel) && (dep.lexHead.getOrthography().equals("was") ||  dep.lexHead.getOrthography().equals("were") || dep.lexHead.getPOS().equals("VBZ") || dep.lexHead.getPOS().equals("VBP"))){
					
					//Simple subj-verb feats
					//Increment instance number if in error analysis mode
					if(sentId!=null)INSTANCENUM++;
					inc(agrExtractors);

					//Disjunct features
					if(dep.lexDep.getOrthography().equals("or")){
						ArrayList<String>rels=new ArrayList<String>(2);
						rels.add("First");rels.add("Next");
						Hashtable<LexDependency,Sign>cdeps=this.getLowerSiblingDeps(inputs,dep.lexDep,rels,null);
						if(cdeps!=null){
							for(Enumeration<LexDependency>e=cdeps.keys();e.hasMoreElements();){
								LexDependency cdep=e.nextElement();
								this.depSign=cdep.lexDep;
								inc(agrConjExtractors);
							}
						}
					}
					
					//Of-complement subjects (for non-numeral, non-%-sign subjs)
					String subjClass=dep.lexDep.getWords().get(0).getSemClass();
					String subjPOS=dep.lexDep.getPOS();
					if(subjClass==null)subjClass="NULL";
					if(!subjClass.equals("PERCENT") && !subjPOS.startsWith("CD")){
						ArrayList<String>rels=new ArrayList<String>(1);
						rels.add("Mod");
						ArrayList<String>depPreds=new ArrayList<String>(1);
						depPreds.add("of");
						Hashtable<LexDependency,Sign>ofComplDeps=this.getLowerSiblingDeps(inputs,dep.lexDep,rels,depPreds);
						if(ofComplDeps!=null){
							for(Enumeration<LexDependency>e1=ofComplDeps.keys();e1.hasMoreElements();){
								LexDependency ofComplDep=e1.nextElement();
								Sign[] ofComplSigns=ofComplDeps.get(ofComplDep).getDerivationHistory().getInputs();
								rels=new ArrayList<String>(1);
								rels.add("Arg1");
								Hashtable<LexDependency,Sign>ofDeps=this.getLowerSiblingDeps(ofComplSigns,ofComplDep.lexDep,rels,null);
								if(ofDeps!=null){
									for(Enumeration<LexDependency>e2=ofDeps.keys();e2.hasMoreElements();){
										LexDependency ofDep=e2.nextElement();
										this.depSign=ofDep.lexDep;
										inc(agrOfComplementExtractors);
									}
								}
							}
						}
					}
					
				}
				
				//Relative clause features
				String whPrn=dep.lexDep.getOrthography();
				if((dep.rel.equals("GenRel")||dep.rel.equals("whApposRel")) && (whPrn.equals("that")||whPrn.equals("who")||whPrn.equals("which")||whPrn.equals("whose"))){
					
					//Make sure relative clause is linked to head of the quoted NP 
					//(and not the quotation mark itself)
					Sign sib=this.getSibling(sign.getSiblingFilledDeps(),"Arg");
					if(sib!=null){
						this.headSign=sib;
					}
					
					//Simple WH-pronoun features
					//Increment instance number if in error analysis mode
					if(sentId!=null)INSTANCENUM++;
					inc(whExtractors);

					//Proximal conjunct features
					if(dep.lexDep.getPOS().equals("CC") || dep.lexDep.getOrthography().equals(",") || dep.lexDep.getOrthography().equals(";")|| dep.lexDep.getOrthography().equals("or")|| dep.lexDep.getOrthography().equals("and")){
						ArrayList<String>rels=new ArrayList<String>(1);
						rels.add("Next");
						Hashtable<LexDependency,Sign>cdeps=this.getLowerSiblingDeps(inputs,dep.lexDep,rels,null);
						if(cdeps!=null){
							for(Enumeration<LexDependency>e=cdeps.keys();e.hasMoreElements();){
								LexDependency cdep=e.nextElement();
								this.depSign=cdep.lexDep;
								inc(whConjExtractors);
							}
						}
					}
				}
			}
			
			//Punctuation feature extraction: Unbalanced sentence medial appositions are flagged
			if (sign!=null && inputs!=null) {
				
				//Pass up unbalanced punctuation indicator
				
				//Result cat of current has unbal feature
				Category target = sign.getCategory().getTarget();
				FeatureStructure fs = target.getFeatureStructure();
				String punctFeatVal=null;
				if ( (fs != null && fs.hasAttribute("unbal"))) {
					Object val = fs.getValue("unbal");
					punctFeatVal = (val instanceof SimpleType) ? ((SimpleType)val).getName() : null;
				}
				
				//Right child (binary case) or only child (unary case) has unbalanced punct feature
				SignProps childProps=(SignProps)inputs[inputs.length-1].getData(SignProps.class);
				if(childProps!=null)punctFeatVal=childProps.getUnbalancedPunct();
				
				if(punctFeatVal!=null){
					SignProps currProps=new SignProps(punctFeatVal);
					sign.addData(currProps);
				}
				
				//Extract unbalanced punctuation feature for unbalanced sentence medial punctuation
				if (inputs.length == 2) {
					//Left child has unbalanced punct feature
					SignProps lchildProps=(SignProps)inputs[0].getData(SignProps.class);
					if(lchildProps!=null && lchildProps.getUnbalancedPunct()!=null){
						Word nextWord = inputs[1].getWords().get(0);
						//Check whether right child begins with a punctuation mark; else fire feature
						if (!isPunct(nextWord)){
							inc(punctExtractor);
						}
					}
					
				}
			}
		}
		// store it
		storeFeatureMap(sign);
	}
	
	public Sign getOfComplSign(){
	
		Sign retval=null;
		
		return retval;
	}
	
	/** Stores the current feature map as a data object in the given sign. */
	protected void storeFeatureMap(Sign sign) {
		sign.addData(new FeatureMapWrapper(currentMap));
	}
	
	/** Returns the feature map for this extractor from the given sign (null if none). */
	public FeatureMap getFeatureMap(Sign sign) {
		FeatureMapWrapper fmw = (FeatureMapWrapper)sign.getData(FeatureMapWrapper.class);
		return (fmw != null) ? fmw.featureMap : null;
	}
	

	/**
	 * Increments the count of the given features, if relevant.
	 */
	protected void inc(List<List<TrieMap.KeyExtractor<String>>> extractors) {
		for (List<TrieMap.KeyExtractor<String>> lazyExtractor : extractors) {
			Alphabet.Feature f = alphabet.indexLazy(lazyExtractor);
			if (f != null)currentMap.inc(f);
		}
	}

	//------------------------------------
	// utility functions
	
	//Get value of subject feature from verb's result cat
	public String getSubjectFeature(Category cat){
	
		String retval=null;
		if (cat instanceof ComplexCat) {
			Category resCat = ((ComplexCat)cat).getResult();
			retval=this.getSubjectFeature(resCat);
		}
		else if (cat instanceof AtomCat) {
			AtomCat ac = (AtomCat) cat;
			FeatureStructure fs = ac.getFeatureStructure();
			for(String attr: fs.getAttributes()){
				if(attr.equals("sbj")){
					retval=fs.getValue(attr).toString();
					break;
				}
			}
		}
		
		return retval;
	}
	
	//checks for punct
	private boolean isPunct(Word w) {
		String pos = w.getPOS();
		boolean retval = pos.startsWith("PUNCT");
		retval = retval || pos.equals(".") || pos.equals(",") || pos.equals(";") || pos.equals(":") || pos.equals("LRB") || pos.equals("RRB");
		//if (retval) {
		//System.out.println("isPunct: " + w.getForm() + " pos: " + pos);
		//}
		return retval;
	}
	
	// Get siblings of a given head 1-step down the derivation, given the head-sibling relations and lexical preds of deps .*/
	public Hashtable<LexDependency,Sign> getLowerSiblingDeps(Sign[] inputs,Sign headSign,ArrayList<String>rels,ArrayList<String>depPreds){
		
		Hashtable<LexDependency,Sign> retval=new Hashtable<LexDependency,Sign>();
		for(Sign sign: inputs){
			if(retval.size()==rels.size())break;
			List<LexDependency>sdeps=sign.getSiblingFilledDeps();
			sdeps.addAll(sign.getFilledDeps());
			for(LexDependency sdep: sdeps){
				if(sdep.lexHead==headSign && rels.contains(sdep.rel) && !retval.containsKey(sdep)){
					if(depPreds==null || depPreds.contains(sdep.lexDep.getOrthography())){
						retval.put(sdep,sign);
					}
				}
			}
		}
		if(retval.size()==0)retval=null;
		return retval;
	}
	
	//returns sibling sign of a given head given a relation label
	private Sign getSibling(List<LexDependency> sdeps,String rel){
		
		Sign retval=null;
		if(sdeps!=null){
			for(LexDependency dep: sdeps){
				if(dep.rel.equals(rel)){
					retval=dep.lexDep;
					break;
				}
			}
		}
		
		return retval;
	}
	
	// returns acceptable paraphrases for words
	private String adjustWord(String word) {
		
		String retval=word;
		//Account for acceptable paraphrases
		if (word.equals("'ve"))
			retval="have";
		else if (word.equals("'s"))
			retval="is";
		else if (word.equals("'re"))
			retval="are";
		
		return retval;
	}
	
	//adjusts POS tags
	private String adjustPOS(String word,String pos,String semClass) {
		
		String retval=pos;
		
		if(word.equals("has"))
			retval="VBZ";
		else if (word.equals("have"))
			retval="VBP";
		else if(word.equals("one") || word.equals("1"))
			pos="CD-1";
		else if(semClass!=null && semClass.equals("PERCENT"))
			retval=semClass;
		else if(word.equals(",") || word.equals(";"))
			retval="CC";
		
		return retval;
	}
	
	//adjust sem class info
	private String adjustSemClass(String semClass) {
		String retval="UNK";
		if(semClass!=null){
			String[]temp=semClass.split("\\|");
			retval=temp[0].split(":")[0];
		}
		return retval;
	}
	
	//main prefixes (AGR=Agr; CONJ=Conjn/Disjn; WH=wh-pronoun; OF=Of-complement)
	private void add_prefix_main1(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return "AGR"; }});
	}
	private void add_prefix_main2(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return "AGRCONJ"; }});
	}
	private void add_prefix_main3(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return "AGROF"; }});
	}
	private void add_prefix_main4(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return "AGRWH"; }});
	}
	private void add_prefix_main5(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return "AGRWHCONJ"; }});
	}
	
	//instance # in error analysis mode
	private void add_instance_num(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return Integer.toString(INSTANCENUM); }});
	}
	
	//sub-prefixes (W=Word; P=POS tag; S=Stem; C=SemClass)
	private void add_prefix_sub1(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return "WW"; }});
	}
	private void add_prefix_sub2(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return "WP"; }});
	}
	private void add_prefix_sub3(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return "PW"; }});
	}
	private void add_prefix_sub4(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return "PP"; }});
	}
	private void add_prefix_sub5(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return "WS"; }});
	}
	private void add_prefix_sub6(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return "WC"; }});
	}

	//select required feature prefix
	private void add_prefix(int prefix,List<TrieMap.KeyExtractor<String>> retval) {
        switch (prefix) {
    		case 1:add_prefix_main1(retval);break;
    		case 2:add_prefix_main2(retval);break;
    		case 3:add_prefix_main3(retval);break;
    		case 4:add_prefix_main4(retval);break;
    		case 5:add_prefix_main5(retval);break;
        }
    }
	
	//	-------------------------------
	// feature extractors
	
	// dep-word-head-word
	private List<TrieMap.KeyExtractor<String>> dep_word_head_word(int prefix) {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(3);
		if(this.sentId!=null)add_instance_num(retval);
		add_prefix(prefix,retval);
		add_prefix_sub1(retval);
		add_dep_word(retval);
		add_head_word(retval);
		return retval;
	}
	
	//dep-word head-pos
	private List<TrieMap.KeyExtractor<String>> dep_word_head_pos(int prefix) {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(3);
		if(this.sentId!=null)add_instance_num(retval);
		add_prefix(prefix,retval);
		add_prefix_sub2(retval);
		add_dep_word(retval);
		add_head_pos(retval);
		return retval;
	}
	
	//dep-pos head-word
	private List<TrieMap.KeyExtractor<String>> dep_pos_head_word(int prefix) {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(3);
		if(this.sentId!=null)add_instance_num(retval);
		add_prefix(prefix,retval);
		add_prefix_sub3(retval);
		add_dep_pos(retval);
		add_head_word(retval);
		return retval;
	}
	
	//dep-pos head-pos
	private List<TrieMap.KeyExtractor<String>> dep_pos_head_pos(int prefix) {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(3);
		if(this.sentId!=null)add_instance_num(retval);
		add_prefix(prefix,retval);
		add_prefix_sub4(retval);
		add_dep_pos(retval);
		add_head_pos(retval);
		return retval;
	}

	//dep-word head-stem
	private List<TrieMap.KeyExtractor<String>> dep_word_head_stem(int prefix) {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(3);
		if(this.sentId!=null)add_instance_num(retval);
		add_prefix(prefix,retval);
		add_prefix_sub5(retval);
		add_dep_word(retval);
		add_head_stem(retval);
		return retval;
	}
	
	//dep-word head-class
	private List<TrieMap.KeyExtractor<String>> dep_word_head_class(int prefix) {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(3);
		if(this.sentId!=null)add_instance_num(retval);
		add_prefix(prefix,retval);
		add_prefix_sub6(retval);
		add_dep_word(retval);
		add_head_class(retval);
		return retval;
	}
	
	//unbalanced punctuation
	private List<TrieMap.KeyExtractor<String>> unbal_punct() {
		List<TrieMap.KeyExtractor<String>> retval = new ArrayList<TrieMap.KeyExtractor<String>>(1);
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return "$punct"; }});
		return retval;
	}
	
	//head word
	private void add_head_word(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ String word=adjustWord(headSign.getWordForm());return word; }});
	}
	
	//head stem
	private void add_head_stem(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ return headSign.getWords().get(0).getStem();}});
	}
	
	//head class
	private void add_head_class(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ String semClass=adjustSemClass(headSign.getWords().get(0).getSemClass());return semClass;}});
	}
	
	// head pos
	private void add_head_pos(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ String pos=adjustPOS(headSign.getOrthography(),headSign.getPOS(),headSign.getWords().get(0).getSemClass());return pos; }});
	}
	
	// dep word
	private void add_dep_word(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ String word=adjustWord(depSign.getWordForm());return word; }});
	}
	
	// dep pos
	private void add_dep_pos(List<TrieMap.KeyExtractor<String>> retval) {
		retval.add(new TrieMap.KeyExtractor<String>(){public String getKey(){ String pos=adjustPOS(depSign.getOrthography(),depSign.getPOS(),depSign.getWords().get(0).getSemClass());return pos; }});
	}
}
