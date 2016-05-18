package opennlp.ccg.realize.hypertagger;

import java.io.File;
import java.util.*;

import opennlp.ccg.hylo.*;
import opennlp.ccg.synsem.*;
import opennlp.ccg.util.Pair;

/** This class contains methods for extracting features from a logical form
 * 
 * @author espinosa
 *
 */
public abstract class TagExtractor {
	protected class LfGraphLink {
		String label; // eg. "genrel"
		String arg; // if applicable
		LfGraphNode target; // graph node it points to
		LfGraphNode source; // node it extends from
		public LfGraphLink(String l, String a, LfGraphNode t) {
			label = l;
			arg = a;
			target = t;
		}
		public LfGraphNode getTarget() {
			return target;
		}
		public void setTarget(LfGraphNode t) {
			target = t;
		}
		public String getLabel() {
			return label;
		}
		public LfGraphNode getSource() {
			return source;
		}
		public void setSource(LfGraphNode source) {
			this.source = source;
		}
	}
	protected class LfGraphNode {
		String data;
		String predicateName;
		int index; // from original LF
		SatOp pred; // original predicate object
		ArrayList<LfGraphLink> children;
		LfGraphLink parentLink;
		HashMap<String,String> attribs;
		protected String POS; // caches the part-of-speech tag for this node (or GS postag)
		protected String ST; // gold-standard supertag
		ArrayList<Pair<String,Double>> POSList; // not used atm
		Set<Pair<String, Double>> STList; // for 2-pass tags
		
		public Set<Pair<String, Double>> getSTList() {
			return STList;
		}
		public void setSTList(Set<Pair<String, Double>> set) {
			STList = set;
		}
		ArrayList<LfGraphNode> multiparents;
		FeatureList features;
		private String id; // e.g. "w1"
		protected FeatureList getFeatures() {
			return features;
		}
		protected void setFeatures(FeatureList features) {
			this.features = features;
		}
		public LfGraphNode(String s, int idx) {
			data = s; // this is just "w1" or whatever
			index = idx;
			children = new ArrayList<LfGraphLink>();
			attribs = new HashMap<String,String>();
			POS = null;
			multiparents = new ArrayList<LfGraphNode>();
		}
		public String getData() {
			return data;
		}
		public void setData(String s) {
			data = s;
		}
		public void addAttribute(String name, String value) {
			attribs.put(name, value);
		}
		public void addChild(LfGraphLink link) {
			children.add(link);
		}
		public ArrayList<LfGraphLink> getChildren() {
			return children;
		}
		public int getNumChildren() {
			return children.size();
		}
		public boolean isLeafNode() {
			if(children.isEmpty()) {
				return true;
			}
			else {
				return false;
			}
		}
		/*
		public boolean isLexPred() {
			return HyloHelper.isLexPred(data);
		}*/
		public LfGraphNode findNode(BitSet b) {
			// intended to be run from the root node; only searches
			// nodes below this one
			// Say we want a node whose index is between 1-4 inclusive
			// pass a bitset with those bits set. Returns the lex pred
			// node whose index is in the bitset.
			if(b.get(index)) {
				return this;
			}
			for(LfGraphLink n : children) {
				n.getTarget().findNode(b);
			}
			return null;
		}
		public HashMap<String, String> getAttribs() {
			return attribs;
		}
		public void setAttribs(HashMap<String, String> attribs) {
			this.attribs = attribs;
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
		public void setChildren(ArrayList<LfGraphLink> children) {
			this.children = children;
		}
		public String getPredicateName() {
			return predicateName;
		}
		public void setPredicateName(String predicateName) {
			this.predicateName = predicateName;
		}
		public LfGraphLink getParentLink() {
			return parentLink;
		}
		public void setParentLink(LfGraphLink parentLink) {
			this.parentLink = parentLink;
		}
		public void addMultiParent(LfGraphNode parentNode) {
			// XXX should not be necessary! fix bug & eliminate
			if(parentNode == this) {
				return;
			}
			this.multiparents.add(parentNode);
		}
		public ArrayList<LfGraphNode> getMultiParents() {
			return this.multiparents;
		}
		public SatOp getPred() {
			return pred;
		}
		protected void setPred(SatOp pred) {
			this.pred = pred;
		}
		public String getPOS() {
			return POS;
		}
		public void setPOS(String pos) {
			POS = new String(pos);
		}
		public ArrayList<Pair<String, Double>> getPOSList() {
			return POSList;
		}
		public void setPOSList(ArrayList<Pair<String,Double>> plist) {
			this.POSList = plist;
		}
		public void setID(String string) {
			this.id = string;
		}
		public String getID() {
			return this.id;
		}
	}
	public static int LFID = 0;
	/** Implements  a list of features. Keys are short strings, e.g. "CN", "FO", ...
	 * Values are arbitrary strings, but a key can have multiple values. Thus, the values are actually of type ArrayList<String>. 
	 * @author espinosa
	 *
	 */
	protected class FeatureList extends HashMap<String, ArrayList<String>> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		int numFeatures = 0;
		// deprecated
		protected void addFeature(String featureName, String value) {
			/*if(value == null) {
				return;
			}
			if(this.get(featureName) == null) {
				this.put(featureName.trim(), new ArrayList<String>());
			}
			ArrayList<String> feats = this.get(featureName);
			feats.add(value.trim());
			this.put(featureName.trim(), feats);
			numFeatures++; */
			addFeatureWithProb(featureName, value);
		}
		protected void addFeatures(String featureName, ArrayList<Pair<String,Double>> values) {
			for(Pair<String,Double> v : values) {
				this.addFeatureWithProb(featureName, v.a, v.b );
			}
		}
		// merge f's features into self
		/*
		protected void addFeatures(FeatureList f) {
			Set<Map.Entry<String,ArrayList<String>>> es = this.entrySet();
			for(Map.Entry<String,ArrayList<String>> e : es) {
				String fn = e.getKey();
				for(String fv : e.getValue()) {
					this.addFeature(fn, fv);
				}
			}
		} */
		protected void addFeatureWithProb(String featureName, String value, Double prob) {
			if(value == null) {
				return;
			}
			if(this.get(featureName) == null) {
				this.put(featureName.trim(), new ArrayList<String>());
			}
			ArrayList<String> feats = this.get(featureName);
			feats.add(value.trim() + ":" + prob.toString());
			this.put(featureName.trim(), feats);
			numFeatures++;
		}
		protected void addFeatureWithProb(String featureName, String value) {
			this.addFeatureWithProb(featureName, value, new Double(1.0));
		}
		protected ArrayList<String> getFeature(String featureName) {
			return this.get(featureName);
		}
		protected String getSingleFeature(String featureName) {
			return this.get(featureName).get(0);
		}
		protected String getAllFeatures() {
			String output = "";
			for(String k : this.keySet()) {
				for(String v : this.get(k)) {
					output = output.concat(k + "=" + v);
					output = output.concat(" ");
				}
			}
			return output;
		}
		protected String[] getAllFeaturesForMaxent() {
			String[] out = new String[this.getNumFeatures()];
			int i = 0;
			for(String k : this.keySet()) {
				for(String v : this.get(k)) {
					out[i] = k + "=" + v;
					i++;
				}
			}
			return out;
		}
		protected int getNumFeatures() {
			return numFeatures;
		}
	}
	// fields
	protected LfGraphNode lfGraph; // stores the graph structure of the LF
	protected HashMap<String,LfGraphNode> nomTable;
	protected HashMap<String,LfGraphNode> varTable;
	protected HashMap<LfGraphNode,String> lexpairs;
	protected HashMap<LfGraphNode,String> pospairs;
	protected LF lf;
	protected List<SatOp> preds;
	protected List<SatOp> flatLF;
	protected int maxIndex;
	protected Map<String,String> argNameMap; // mww: map from arg names to short arg names
	

	public TagExtractor() {	argNameMap = new HashMap<String,String>(); }
	
	public void setLF(LF lf) throws FeatureExtractionException {
		LFID++;
		// experimental
		HyloHelper.convertNominals(lf);
		setLF(HyloHelper.getPreds(lf));
	}
	
	/** This method takes an LF and extracts its features, changing the internal state
	 * of this object accordingly. Other methods such as getSupertag() can then
	 * be called to obtain the tagger's prediction.
	 * @param preds A logical form
	 * @throws FeatureExtractionException when the logical form cannot be processed and the extracted features will not be meaningful
	 */
	public void setLF(List<SatOp> preds) throws FeatureExtractionException {
		this.preds = preds;
		LfGraphNode curNode = null;
		curNode = null;
		nomTable = new HashMap<String,LfGraphNode>();
		varTable = new HashMap<String,LfGraphNode>();
		int i = 0;
		// Pass 1: find and store nominals
		for(SatOp s: preds) {
			if(s == null) {
					throw new FeatureExtractionException(); // ??? why is it sometimes null?
			}
			if(s.getArg() instanceof Proposition) {
				LfGraphNode thisNode = new LfGraphNode(s.getNominal().toString(), i);
				thisNode.setPredicateName(((Proposition)s.getArg()).getName());
				thisNode.setID(s.getNominal().toString());
				thisNode.setPred(s);
				if(lfGraph == null) {
					// i.e., the first node processed
					lfGraph = thisNode;
				}
				nomTable.put(thisNode.getID(), thisNode);
			}
			i++;
		}
		this.maxIndex = i;
		// Pass 2: traverse all other nodes, linking to nominals found in pass 1
		i = 0;
		for(SatOp s: preds) {
			if(s == null) {
				throw new FeatureExtractionException();
			}
			if(s.getArg() instanceof Proposition) {
				curNode = nomTable.get(s.getNominal().toString());
				i++;
				continue;
			}
			Diamond d = (Diamond)s.getArg();
			// not sure how this could happen, but it did
			if(d == null || (d.getArg() == null)) { throw new FeatureExtractionException(); }
			if(d.getArg() instanceof NominalVar) {
				// XXX all this is probably defunct and can safely be deleted
				// add multiparent
				// might need getName() instead of toString()
				//System.err.println("found var: str = " + d.getArg().toString());
				LfGraphNode target = nomTable.get(d.getArg().toString());
				LfGraphLink ln = new LfGraphLink(d.getMode().toString(), null, target);
				ln.setSource(curNode);
				//target.addMultiParent(ln);
			}
			else if(d.getArg() instanceof Nominal) {
				// make link labeled <mode> to nominal
				LfGraphNode target = nomTable.get(d.getArg().toString());
				LfGraphLink ln = new LfGraphLink(d.getMode().toString(), null, target);
				ln.setSource(curNode);
				if(target != null) {
					// ??? why is it sometimes null? and if it is, should an exception be thrown?
					target.setParentLink(ln);
					target.addMultiParent(curNode);
				}
				else {
					//System.err.println("TE: target was null: " + d.getArg().toString());
				}
				if(curNode == null) { throw new FeatureExtractionException();}
				curNode.addChild(ln);
			}
			else {
				// proposition
				// add attr <mode><arg> to current node
				try {
					curNode.addAttribute(d.getMode().toString(), d.getArg().toString());
				}
				catch(Exception e) {
					// ??? this null must occur because there were no attributes, but this
					// probably isn't the best way to handle it
				}
				
			}
			// don't change curNode here
			i++;
		}
	}
	
	public LF getLF() {
		return this.lf;
	}
	
	/** This method extracts features from a node in the graph and returns
	 * them as an array of strings. It's implemented slightly differently
	 * in HyperTagger and POSTagExtractor.
	 * @param n The node from which to extract the features.
	 * @return A list of features.
	 */
	protected abstract FeatureList getFeatures(LfGraphNode n);
	
	/** Get the features for the index'th node
	 * 
	 * @param index The index into the LF
	 */
	public FeatureList getFeatures(int index) {
		for(LfGraphNode n : nomTable.values()) {
			if(n.getIndex() == index) {
				return getFeatures(n);
			}
		}
		return null; // bad index
	}
	
	// these methods only make sense for training, because
	// they fetch the gold-standard supertag or POS tag.
	public String getSupertag(LfGraphNode n) {
		return lexpairs.get(n);
	}
	
	public String getSupertag(int index) {
		for(LfGraphNode n : nomTable.values()) {
			if(n.getIndex() == index) {
				return lexpairs.get(n);
			}
		}
		return null;
	}
	
	public String getPOStag(LfGraphNode n) {
		return pospairs.get(n);
	}
	
	public String getPOStag(int index) {
		for(LfGraphNode n : nomTable.values()) {
			if(n.getIndex() == index) {
				return pospairs.get(n);
			}
		}
		return null;
	}

	// return the node with the given index
	protected LfGraphNode findNode(int index) {
		for(LfGraphNode n : nomTable.values()) {
			if(n.getIndex() == index) {
				return n;
			}
		}
		return null;
	}
	protected int numNodes() {
		return nomTable.size();
	}
	protected int maxIndex() {
		return this.maxIndex;
	}
	// utility method to do the inverse of java.String.split()
	public static String join(ArrayList<String> a, String delimiter) {
		String out = new String();
		int i;
		for(i = 0; i < a.size(); i++) {
			out = out.concat(a.get(i));
			if(i != a.size() - 1) {
				out = out.concat(delimiter);
			}
		}
		return out;
	}
	/**
	 * @param predInfo
	 * @throws FeatureExtractionException
	 * This method stores gold standard predicate info, which is expected to be in the following format:
	 * predInfo :: (field<space>)+
	 * field :: wordId:supertag:POStag:predName
	 * 
	 * The supertag and postag are expected to have been escaped by Lexicon.DefaultTokenizer.escape(), and will be unescaped during storage.
	 * This method throws a runtime error if the predInfo string cannot be parsed.
	 * 
	 */
	public abstract void storeGoldStdPredInfo(String predInfo);

	public abstract String getAllFeaturesAndAnswer();

	public abstract void loadPriorModel(File priorModelFile, File vocabFile);

	public void loadProtoModel(File hyperModelFile) {
		// TODO refactor TagExtract app so this isn't needed
		return;
	} 

	// mww: sets configurable arg names 
	/**
	 * Sets the arg name map to the given names.
	 * @param argnames Space-delimited arg names in format name(:shortname)?.
	 *   Defaults to "Arg0:A0 Arg1:A1 Arg1a:A1a Arg1b:A1b Arg2:A2 Arg2a:A2a Arg2b:A2b Arg3:A3 Arg4:A4 Arg5:A5".
	 */
	protected void setArgNames(String argnames) {
		argNameMap.clear();
		// default is augmented propbank arg names
		if (argnames == null) argnames = "Arg0:A0 Arg1:A1 Arg1a:A1a Arg1b:A1b Arg2:A2 Arg2a:A2a Arg2b:A2b Arg3:A3 Arg4:A4 Arg5:A5";
		String[] nameslist = argnames.split("\\s+");
		for (String argname : nameslist) {
			String[] namepair = argname.split(":");
			if (namepair.length == 2) argNameMap.put(namepair[0], namepair[1]);
			else if (namepair.length == 1) argNameMap.put(namepair[0], namepair[1]);
		}
	}
}


