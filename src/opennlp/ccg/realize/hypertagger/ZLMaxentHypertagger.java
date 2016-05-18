package opennlp.ccg.realize.hypertagger;

import static java.util.Arrays.asList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import opennlp.ccg.hylo.Nominal;
import opennlp.ccg.hylo.SatOp;
import opennlp.ccg.lexicon.DefaultTokenizer;
import opennlp.ccg.lexicon.Word;
import opennlp.ccg.parse.supertagger.io.XMLPOSDictionaryReader;
import opennlp.ccg.parse.supertagger.io.XMLWordDictionaryReader;
import opennlp.ccg.parse.supertagger.ml.STPriorModel;
import opennlp.ccg.parse.supertagger.util.STTaggerPOSDictionary;
import opennlp.ccg.parse.supertagger.util.STTaggerWordDictionary;
import opennlp.ccg.parse.tagger.util.ConfigFileProcessor;
import opennlp.ccg.parse.tagger.util.ResultSink;
import opennlp.ccg.realize.Hypertagger;
import opennlp.ccg.util.Pair;

/**
 * 
 * @author espinosa
 * This class implements the hypertagger. Instantiating a hypertagger requires several external
 * files:
 * <ol>
 * <li> A POS model
 * <li> A prior model for POS tags, and its vocab file
 * <li> A prior model for hypertags, and its vocab file
 * </ol>
 * The prior model files are optional.
 * <p/>
 * To use the hypertagger for realization, instantiate a POS tagger first, then
 * instantiate the hypertagger using that POS tagger. Example in pseudo-code:
 * <code>
 * POSPriorModel ppm = new POSPriorModel(String flmFile, String vocabFile);
 * ZLMaxentModel posMod = new ZLMaxentModel(String fileName);
 * ZLPOSTagger pt = new ZLPOSTagger(posMod, ppm);
 * ZLMaxentHypertagger ht = new ZLMaxentHypertagger(pt, File htModelPath);
 * ht.loadPriorModel(File priorModelFile, File vocabFile);
 * 
 * The tagger can also be instantiated from a config file. This is the recommended
 * method for using the tagger as part of the realizer. See the method 'ZLMaxentHypertaggerFactory'.
 * </code>
 * 
 */
public class ZLMaxentHypertagger extends TagExtractor implements Hypertagger {
	File hypertagModelFilename;
	File posModelFilename;
	public ZLMaxentModel hypertagModel; // null if extracting feats for training
	ZLMaxentModel posModel; // null if extracting feats for training
	ZLPOSTagger postagger;
	ZLMaxentModel protoHTModel;
	double protoHTBeta = 0.01; // FIXME what should this be?
	LfGraphNode currentPred;
	String LFNum;
	protected double[] betas;
	int currentBeta;
	//Flag which indicates whether gold std tags need to be ensured during tag prediction
	//Gold std tag info for perceptron training (event generation)
	private boolean goldStdTagInsert=false;
	//Nominal id to gold std supertag mapping
	private HashMap<String,String> goldPred2Tag;
	private STPriorModel priorModel;
	double priorBeta = 0.4;
	private HashMap<String, String> goldPredPOS;
	private STTaggerWordDictionary wdict;  // word (pred)-level tagging dictionary
	private STTaggerPOSDictionary posdict; // pos-level tagging dictionary
	int dictK; // frequency threshold for tagdict lookups
	public BufferedWriter tdErr;

	public class ProbIndexPair implements Comparable<Object> {
		public double prob;
		public int index;
		public ProbIndexPair(double prob, int index) {
			this.prob = prob;
			this.index = index;
		}
		public int compareTo(Object o) {
			if(prob < ((ProbIndexPair)o).prob) {
				return -1;
			}
			else if(prob == ((ProbIndexPair)o).prob) {
				return 0;
			}
			else {
				return 1;
			}
		}
	}

	/**
	 * @author espinosa
	 * This is a singleton class containing functions for extracting various features
	 * from the LF graph nodes. These
	 * feature functions are called from ZLMaxentHypertagger#getFeatures.
	 * 
	 * Some of the functions extract several features at once, to avoid unnecessary iteration.
	 * Feature template abbreviations:
	 * 
	 * FO -- fan-out, i.e. number of children
	 * PN -- predicate name
	 * RN -- parent name 
	 * CT -- type of child
	 * A1N, A2N, ... -- Arg1 name, Arg2 name, ... (by default)
	 * A1P, A2P, ... -- Arg1 POS tag, Arg2 POS tag, ... (by default)
	 * MP -- Modifier POS tag (non-arg children)
	 * PP -- parent's POS tag, if any parent
	 * CN -- name of child
	 * NA -- number of Argument children
	 * PT -- POS tag (see docs) 
	 * ZD -- det=value
	 * ZM -- mood=value
	 * ZN -- num=value
	 * ZT -- tense=value
	 * ZP -- partic=value
	 * XC -- semantic class of node, if applicable
	 * XnD -- semantic class of argument child node n, if applicable (by default)
	 * XP -- semantic class of parent node, if applicable
	 * XM -- semantic class of non-arg child node, if applicable
	 * CS -- child supertag
	 * PS -- parent supertag
	 * AS -- argument supertag
	 * MS -- modifier supertag
	 * 
	 */
	// mww: switched to configurable arg names
	void fillFeatures(LfGraphNode n, FeatureList f) {

		f.addFeatureWithProb("FO",  Integer.toString(n.getNumChildren()));

		for(String att : n.getAttribs().keySet()) {
			f.addFeatureWithProb("Z" + att.substring(0,1).toUpperCase(), n.getAttribs().get(att));
		}

		f.addFeatureWithProb("PN",  n.getPredicateName());

		if(n.getMultiParents().size() > 0) {
			for(LfGraphNode parent : n.getMultiParents() ) {
				f.addFeatureWithProb("RN", parent.getPredicateName());
				/* the line below will add the parent's best-ranked POS tag with prob=1.0 */
				f.addFeatureWithProb("PP", getPOS(parent));
				/* The code below will add the parent's best-ranked POS tag with its actual probability */
				/*
					 ArrayList<Pair<String,Double>> poslist = getBetaBestPOS(parent);
					 Pair<String,Double> pospair = poslist.get(0);
					 feats.addFeatureWithProb("PP", pospair.a, pospair.b);
				 */
				// add class name, if available
				String cls = parent.getPred().getNominal().toString();
				// string is in X:Y:Z format. Remove 'X:' leaving 'Y:Z'.
				if(cls != null && cls.indexOf(':') > 0) {
					String cfeat = cls.substring(cls.indexOf(':') + 1);
					f.addFeatureWithProb("XP", cfeat);
				}
			}
		}
		else {
			f.addFeatureWithProb("RN", "0");
		}
		int argchildren = 0;
		for(LfGraphLink lnk  : n.getChildren()) {
			f.addFeatureWithProb("CT", lnk.getLabel());
			if(lnk.getTarget() != null) {
				// how could it be null? 
				f.addFeatureWithProb("CN", lnk.getTarget().getPredicateName());
				// mww: use short arg name
				String shortArgName = argNameMap.get(lnk.getLabel());
				if (shortArgName != null) {
					// increment argchild count
					argchildren++;
					f.addFeatureWithProb(shortArgName + "N", lnk.getTarget().getPredicateName());
					f.addFeatureWithProb(shortArgName + "P", getPOS(lnk.getTarget()) );
					// add class info for arg child, if applicable
					String cls = lnk.getTarget().getPred().getNominal().toString();
					// string is in X:Y:Z format. Remove 'X:' leaving 'Y:Z'.
					if(cls != null && cls.indexOf(':') > 0) {
						String cfeat = cls.substring(cls.indexOf(':') + 1);
						// mww: for backwards compatibility
						String argNumOrName = (shortArgName.startsWith("A")) ? shortArgName.substring(1) : shortArgName;
						f.addFeatureWithProb("X" + argNumOrName + "D", cfeat);
					}
				}
				else {
					// not an argument child
					f.addFeatureWithProb("MP", getPOS(lnk.getTarget()));
					// add class info for non-arg child, if applicable
					String cls = lnk.getTarget().getPred().getNominal().toString();
					// string is in X:Y:Z format. Remove 'X:' leaving 'Y:Z'.
					if(cls != null && cls.indexOf(':') > 0) {
						String cfeat = cls.substring(cls.indexOf(':') + 1);
						f.addFeatureWithProb("XM", cfeat);
					}
				}
			}
		}
		f.addFeatureWithProb("NA", Integer.toString(argchildren));

		if(posModel == null) {
			f.addFeatureWithProb("PT", getPOS(n));
		}
		else {
			ArrayList<Pair<String,Double>> poslist = getBetaBestPOS(n);
			for(Pair<String,Double> pospair : poslist) {
				// add PT=NNP:0.7, PT=NN:0.6, etc
				f.addFeatureWithProb("PT", pospair.a, pospair.b);
				n.setPOS(pospair.a); // take the top choice to be "the pos tag", used later for prior
			}
		}
		// class name feature for node, if available
		Nominal idx = n.getPred().getNominal();
		String cls = idx.toString();
		// string is in X:Y:Z format. Remove 'X:' leaving 'Y:Z'.
		if(cls != null && cls.indexOf(':') > 0) {
			String cfeat = cls.substring(cls.indexOf(':') + 1);
			f.addFeatureWithProb("XC", cfeat);
		}
		// prior features
		if(priorModel != null) {
			Word w = Word.createWord(n.getPredicateName(), null, null, n.getPredicateName(),
					getPOS(n), null, null);
			priorModel.computePriors(w);
			List<Pair<String, Double>> tags = priorModel.getBetaBestPriors(w, priorBeta);
			for(Pair<String,Double> t : tags) {
				f.addFeatureWithProb("PR_ST", DefaultTokenizer.unescape(t.a), t.b);
			}
		}
	}

	private void fillTwoPassFeatures(LfGraphNode n, FeatureList f) {
		// get the STs from the STList
		for(Pair<String,Double> p : n.getSTList()) {
			f.addFeatureWithProb("ST", p.a, p.b);
		}
		// get parent STs
		for(LfGraphNode pl : n.getMultiParents()) {
			for(Pair<String,Double> p : pl.getSTList()) {
				f.addFeatureWithProb("STP", p.a, p.b);
			}
		}
		for(LfGraphLink cl : n.getChildren()) {
			/*
			for(Pair<String,Double> p : cl.getTarget().getSTList()) {
			*/
			LfGraphNode c = cl.getTarget();
				if(c != null) {
					for(Pair<String,Double> p : c.getSTList()) {
						f.addFeatureWithProb("STC", p.a, p.b);
					}
				}
		}
	}
	
	public ZLMaxentHypertagger() {
		super();
		betas = new double[7];
		// values determined by DNM 20 April 2008).
		betas[0] = 0.16;
		betas[1] = 0.05;
		betas[2] = 0.0058;
		betas[3] = 0.00175;
		betas[4] = 0.000625;
		betas[5] = 0.000125;
		betas[6] = 0.000058;
		currentBeta = 0;
		goldPred2Tag = new HashMap<String,String>();
		goldPredPOS = new HashMap<String, String>();
	}

	/** This constructor loads both a POS-tagging model and a hypertagging model. 
	 * 
	 * @param posModelFile The filename from which to load the POS-tagging model. If null,
	 * gold-stardard POS tags will be used (and must be stored via storeGoldStdPredInfo()).
	 * @param hyperModelFile The filename from which to load the hypertagging model
	 * @throws IOException If any model fails to load.
	 */
	public ZLMaxentHypertagger(File posModelFile, File hyperModelFile) throws IOException {
		this();
		this.posModelFilename = posModelFile;
		// load the models
		if(posModelFile != null) {
			this.posModel = new ZLMaxentModel();
			this.posModel.load(posModelFile);
			this.postagger = new ZLPOSTagger(posModel);
			postagger.setPrefixLength(4);
			postagger.setSuffixLength(4);
			postagger.argNameMap = this.argNameMap; // share the arg name map
		}
		this.hypertagModelFilename = hyperModelFile;
		this.hypertagModel = new ZLMaxentModel();
		this.hypertagModel.load(hyperModelFile);
	}
	/* POS tagger can be null. In that case, gold-standard POS tags will be used.
	 * To use in "realization mode", do the following:
	 * 1) instantiate a ZLPOSTagger as normal
	 * 2) load the POS prior model into the POS tagger
	 * 3) Instantiate the ZLMaxentHypertagger with this constructor, passing the POS tagger
	 * and the path to the trained hypertagging model file
	 * 4) load the ht-prior model into the ZLMaxentHypertagger using setPriorModel(vocab, priorfile)
	 * 5) commence tagging via setLF() and getSupertags()
	 */
	public ZLMaxentHypertagger(ZLPOSTagger ptag, File hyperModelFile) {
		this();
		this.postagger = ptag;
		this.postagger.argNameMap = this.argNameMap; // share the arg name map
		this.hypertagModelFilename = hyperModelFile;
		this.hypertagModel = new ZLMaxentModel();
		this.hypertagModel.load(hyperModelFile);
	}

	/** This constructor loads only a POS-tagging model. It's useful for extracting features for Maxent training.
	 * 
	 * @param posModelFile The filename from which to load the model.
	 * @throws IOException If the model couldn't be loaded for any reason.
	 */
	public ZLMaxentHypertagger(File posModelFile) throws IOException {
		this();
		this.posModelFilename = posModelFile;
		// load the model
		this.posModel = new ZLMaxentModel();
		this.posModel.load(posModelFile);
		this.postagger = new ZLPOSTagger(posModel);
		postagger.setPrefixLength(4);
		postagger.setSuffixLength(4);
		postagger.argNameMap = this.argNameMap; // share the arg name map
	}

	public static ZLMaxentHypertagger ZLMaxentHypertaggerFactory(String configFile) throws IOException {
		ZLMaxentHypertagger hypertagger = new ZLMaxentHypertagger();
		ZLPOSTagger postagger = null;
		String[] pathKeys = { "priormodel", "priormodelvocab", "wdict", "posdict", "maxentmodel", "posconfig" };
		Map<String,String> opts = ConfigFileProcessor.readInConfig(configFile, pathKeys);
		// load the POS model first
		String posConfig = opts.get("posconfig");
		if(posConfig != null) {
			postagger = ZLPOSTagger.ZLPOSTaggerFactory(posConfig);
		}
		hypertagger.postagger = postagger;
		// now load the prior models and/or tag dicts
		// if prior models are specified, then tagdicts are not used, or even loaded
		String priorModelPath = opts.get("priormodel");
		String wdictPath = opts.get("wdict");
		if(priorModelPath != null) {
			String vocabPath = opts.get("priormodelvocab");
			if(vocabPath == null) {
				throw new IOException("You must specify a vocab filename along with the prior model filename.");
			}
			hypertagger.loadPriorModel(new File(priorModelPath), new File(vocabPath));
		}
		else if(wdictPath != null) {
			String posdictPath = opts.get("posdict");
			if(posdictPath == null) {
				throw new IOException("You must specify both a word-level dict and a POS dict.");
			}
			XMLWordDictionaryReader wdr = new XMLWordDictionaryReader(new File(wdictPath));
			XMLPOSDictionaryReader pdr = new XMLPOSDictionaryReader(new File(posdictPath));
			STTaggerWordDictionary wdict = wdr.read();
			STTaggerPOSDictionary posdict = pdr.read();
			hypertagger.wdict = wdict;
			hypertagger.posdict = posdict;
			String kstring = opts.get("dictk");
			if(kstring != null) 
				hypertagger.dictK = Integer.parseInt(kstring);
		}
		String maxentModelPath = opts.get("maxentmodel");
		if(maxentModelPath == null) {
			System.err.println("Maxent model path must be specified with key \"maxentmodel\".");
			throw new IOException();
		}
		String protoHTModelPath = opts.get("protomodel");
		// process betas
		String betaString = opts.get("betas");
		if(betaString != null) {
			String[] bs = betaString.split("\\s+");
			double[] betaVals = new double[bs.length];
			int i;
			for(i = 0; i < bs.length; i++) {
				betaVals[i] = Double.parseDouble(bs[i]);
			}
			// can't sort descending w/o extra code and vars?
			// for now, assume betas are in correct order in configfile
			//Arrays.sort(betaVals);
			hypertagger.betas = betaVals;
		}
		hypertagger.hypertagModel = new ZLMaxentModel(maxentModelPath);
		if(protoHTModelPath != null) {
			hypertagger.protoHTModel = new ZLMaxentModel(protoHTModelPath);
			System.err.println("Two-pass model instantiated. Initializing hyperdrive.");
		}
		// mww: add argnames
		String argnames = opts.get("argnames");
		hypertagger.setArgNames(argnames);
		return hypertagger;
	}

	private String getPOS(LfGraphNode n) {
		if(postagger == null) {
			// use gold-standard POS tag
			String posTag = goldPredPOS.get(n.getPred().getNominal().getName());
			if(posTag == null) {
				// XXX this is not correct
				posTag = "UNK";
			}
			return posTag;
		}
		// else use POS model to get beta-best POS
		if(n.getPOS() == null) {
			FeatureList feats = postagger.getFeatures(n);
			//String pos = new String(postagger.getBestPOS(feats));
			// POS tags for nodes are cached in the node itself
			// so as to avoid repeated calls to the model for the same node
			ArrayList<Pair<String,Double>> pos = postagger.getBetaBestPOS(feats);
			n.setPOS(pos.get(0).a);
		}
		return n.getPOS();
	}

	private String getGoldSupertag(LfGraphNode n) {
		return goldPred2Tag.get(n.getPred().getNominal().getName());
	}

	private ArrayList<Pair<String,Double>> getBetaBestPOS(LfGraphNode n) {
		// TODO (for training): add gold-standard POS tag, if it is not in the beta-best
		// list. Or should we use beta-best POS tags in training at all?
		if(n.getPOSList() == null) {
			FeatureList feats = postagger.getFeatures(n);       
			ArrayList<Pair<String,Double>> pos = null;			
			//String pos = new String(postagger.getBestPOS(feats));
			pos = postagger.getBetaBestPOS(feats);
			n.setPOSList(pos);
		}
		return n.getPOSList();
	}

	// computes all features for the graph node, returns a feature list
	protected FeatureList getFeatures(LfGraphNode n) {
		FeatureList feats; 
		feats = n.getFeatures();
		if(feats != null) {
			return feats;
		}
		feats = new FeatureList();
		fillFeatures(n, feats);
		return feats;
	}

	/** Get all features for the current LF as a single multi-line string.
	 * Used during extraction of training data.
	 * @return All features for the current LF.
	 */
	public String getAllFeaturesAndAnswer() {
		StringBuilder output = new StringBuilder();
		for(LfGraphNode n : nomTable.values()) {
			FeatureList feats = getFeatures(n);
			String context = feats.getAllFeatures();
			//String tag = n.getPred().getOrigin().getSupertag();
			String tag = goldPred2Tag.get(n.getPred().getNominal().getName());
			output = output.append(tag + " " + context);
			output = output.append("\n");
		}
		return output.toString();
	}

	// for every node in the tree, compute its features, and store them inside the nodes
	private void assignAllFeatures() {
		for(LfGraphNode n : nomTable.values()) {
			FeatureList feats = getFeatures(n);
			n.setFeatures(feats);
			// eval the proto-model, set the node's STList to the returned outcomes/probs
			if(protoHTModel != null) {
				n.setSTList(getProtoSupertagsAndProbs(n, protoHTBeta));
			}
		}
		// now traverse the graph again, calculating the additional STS features,
		// then unioning that feature list with the cached one
		if(protoHTModel != null) {
			for(LfGraphNode n : nomTable.values()) {
				FeatureList feats = getFeatures(n); // feats guaranteed cached by first pass
				fillTwoPassFeatures(n, feats);
				n.setFeatures(feats); // necessary?
			}
		}
	}

	public ZLMaxentModel getPosModel() {
		return posModel;
	}

	public File getPosModelFilename() {
		return posModelFilename;
	}

	public void setLF(List<SatOp> preds) throws FeatureExtractionException {
		super.setLF(preds);
		if(postagger != null) {
			postagger.setLF(preds);
		}
		assignAllFeatures();
	}

	public String getLFNum() {
		return this.LFNum;
	}

	public void setLFNum(String s) {
		this.LFNum = s;
	}

	/** Set the POS tagging model to use.
	 * @param posModelFilename The filename containing the LBFGS model to use for
	 * computing simple POS tags as part of the featureset.
	 * @throws IOException when the model cannot be loaded for some reason.
	 */
	public void setPOSModelFilename(File posModelFilename) throws IOException {
		this.posModelFilename = posModelFilename;
		// load the model
		this.posModel = new ZLMaxentModel();
		this.posModel.load(posModelFilename);
		this.postagger = new ZLPOSTagger(posModel); 
	}

	public File getHypertagModelFilename() {
		return hypertagModelFilename;
	}

	/** Set the POS tagging model to use.
	 * @param posModelFilename The filename containing the LBFGS model to use for
	 * computing simple POS tags as part of the featureset.
	 * @throws IOException when the model cannot be loaded for some reason.
	 */
	public void setHypertagModelFilename(File hypertagModelFilename) throws IOException {
		this.hypertagModelFilename = hypertagModelFilename;
		// load the model
		this.hypertagModel = new ZLMaxentModel();
		this.hypertagModel.load(hypertagModelFilename);
	}

	/* methods from Hypertagger interface */

	// should probably just rename setLF to mapPreds in the first place
	public void mapPreds(List<SatOp> preds) {
		try {
			setLF(preds);
		}
		catch(FeatureExtractionException e) {
			throw (RuntimeException) new RuntimeException().initCause(e);
		}
	}

	public void setPred(int index) {
		currentPred = findNode(index);
	}

	public void resetBeta() {
		currentBeta = 0;
	}
	public void resetBetaToMax() {
		currentBeta = betas.length - 1;
	}

	public void nextBeta() {
		if(currentBeta < betas.length -1) {
			currentBeta++;
		}
	}
	public void previousBeta() {
		if(currentBeta > 0) {
			currentBeta--;
		}
	}

	public boolean hasMoreBetas() {
		if(currentBeta < betas.length - 1) {
			return true;
		}
		return false;
	}

	public boolean hasLessBetas() {
		if(currentBeta > 0 && this.hasMoreBetas()) {
			return true;
		}
		return false;
	}


	public Map<String,Double> getSupertags() {
		Set<Pair<String, Double>> tp = this.getSupertagsAndProbs(this.currentPred, this.betas[this.currentBeta]);
		if(tp == null) {
			return null;
		}
		HashMap<String,Double> tagMap = new HashMap<String,Double>(tp.size());
		for(Pair<String,Double> p : tp) {
			tagMap.put(p.a, p.b);
		}
		return tagMap;
	}

	/* this method is for use with the ResultSink class, during testing. That code requires the elements of the pairs to be swapped. */
	private List<Pair<Double,String>> getSupertagsAsList() {
		Set<Pair<String, Double>> tp = this.getSupertagsAndProbs(this.currentPred, this.betas[this.currentBeta]);
		ArrayList<Pair<Double,String>> ret = new ArrayList<Pair<Double,String>>();
		for(Pair<String, Double> p : tp) {
			ret.add(new Pair<Double,String>(p.b,p.a));
		}
		return ret;
	}
	
	// cannot be made static due to ProbIndexPair
	private ArrayList<ProbIndexPair> getModelOutcomes(FeatureList f, ZLMaxentModel m) {
		ArrayList<ProbIndexPair> probList = new ArrayList<ProbIndexPair>();
		double[] probs = m.eval(f.getAllFeaturesForMaxent(),true);
		for(int i = 0; i < probs.length; i++) {
			probList.add(new ProbIndexPair(probs[i], i));
		}
		Collections.sort(probList);
		Collections.reverse(probList);
		return probList;
	}

	private HashSet<Pair<String,Double>> betaSearch(ArrayList<ProbIndexPair> probList, double beta) {
		double maxProb = probList.get(0).prob;	
		HashSet<Pair<String,Double>> names = new HashSet<Pair<String, Double>>();
		for(int i = 0; i < probList.size(); i++) {
			if(probList.get(i).prob >= beta * maxProb) {
				names.add(new Pair<String, Double>(protoHTModel.getOutcome(probList.get(i).index), probList.get(i).prob));
			} else {
				break;
			}
		}
		return names;
	}

	public Set<Pair<String,Double>> getProtoSupertagsAndProbs(LfGraphNode n, double beta) {
		HashSet<Pair<String,Double>> names;
		ArrayList<ProbIndexPair> probList = new ArrayList<ProbIndexPair>();
		probList = getModelOutcomes(n.getFeatures(), protoHTModel);
		names = betaSearch(probList, beta);
		return names;
	}
	
	/* 'MAIN' hypertagging entry point */
	@SuppressWarnings("boxing")
	public Set<Pair<String,Double>> getSupertagsAndProbs(LfGraphNode n, double beta) {
		if(currentPred == null) {
			return null; // not handling EPs for rels or feats, so return null
		}
		double bestOCProb = 0.0, curOCProb = 0.0;
		HashSet<Pair<String,Double>> names = new HashSet<Pair<String, Double>>();
		HashSet<String> tagList = new HashSet<String>();
		ArrayList<ProbIndexPair> probList = new ArrayList<ProbIndexPair>();
		probList = getModelOutcomes(n.getFeatures(), hypertagModel);
		/* check tagdict */
		if(wdict != null) {
			// get pred name, get tags from dict at K, if none, try pos dict
			String predName = n.getPredicateName();
			Collection<String> permittedOutcomes = wdict.getEntry(predName, this.dictK);
			if(permittedOutcomes == null) {
				// back off to POS dict
				String pos = getPOS(n);
				permittedOutcomes = posdict.getEntry(pos);
				// sanity check
				if(permittedOutcomes == null) {
					System.err.println("!! No pos dict entries for " + pos);
				}
			}
			ArrayList<String> po = new ArrayList<String>();
			for(String s : permittedOutcomes) {
				po.add(DefaultTokenizer.unescape(s));
			}
			permittedOutcomes = (Collection<String>)po;
			if(permittedOutcomes != null) {
				String oc = "";
				for(ProbIndexPair p : probList) {
					oc = hypertagModel.getOutcome(p.index);
					curOCProb = p.prob;
					if(permittedOutcomes.contains(oc)) {
						if (bestOCProb == 0) {
							bestOCProb = curOCProb;
						}
						if (curOCProb >= (bestOCProb * beta)) { // Beta constraint.
							// The cut-off was met, add the outcome.
							names.add(new Pair<String,Double>(oc, p.prob));
							tagList.add(oc);
							// update max, for first selected outcome
							if (curOCProb > bestOCProb) {
								bestOCProb = curOCProb;
							}
						} else {
							// Else, since our ProbIndexPair[] is sorted by probablity, there will be no more
							// outcomes that make the (beta) cut.
							break;
						}
					}
					else {
						// the outcome was ruled out by the tagdict. make a note of it.
						String nomId = currentPred.pred.getNominal().getName();
						String gsTag = "";
						String gsPos = "";
						if(goldPred2Tag.containsKey(nomId)) {
							gsTag = goldPred2Tag.get(nomId);
							gsPos = goldPredPOS.get(nomId);
						}
						if(gsTag.equals(oc)) {
							try {
								tdErr.write(predName + "\t" + currentPred.index + "\t" + LFNum + "\t" + gsTag + "\t" + gsPos + "\n");
								tdErr.flush();
							}
							catch(Exception e) {
								throw new RuntimeException(e);
							}
						}
					}
				}
			}
		}
		else { // not using dicts
			double maxProb = probList.get(0).prob;	
			for(int i = 0; i < probList.size(); i++) {
				if(probList.get(i).prob >= beta * maxProb) {
					names.add(new Pair<String, Double>(hypertagModel.getOutcome(probList.get(i).index), probList.get(i).prob));
					tagList.add(hypertagModel.getOutcome(probList.get(i).index));
				} else {
					break;
				}
			}
		}
		String nomId=currentPred.pred.getNominal().getName();
		if(goldPred2Tag.containsKey(nomId)){
			String goldStdTag=goldPred2Tag.get(nomId);
			if(goldStdTagInsert && !tagList.contains(goldStdTag))
				names.add(new Pair<String, Double>(goldStdTag,1.0));
		}
		return names;
	}

	public double getCurrentBetaValue() {
		if(currentBeta<betas.length)
			return betas[currentBeta];
		else return 0.0;
	}

	public double[] getBetas() {
		return betas;
	}

	public void setBetas(double[] betas) {
		this.betas = betas;
	}

	public void setPOSBeta(double beta) {
		postagger.setBeta(beta);
	}
	public double getPOSBeta() {
		return postagger.getBeta();
	}

	/* (non-Javadoc)
	 * @see opennlp.ccg.realize.hypertagger.TagExtractor#storeGoldStdPredInfo(java.lang.String)
	 */
	public void storeGoldStdPredInfo(String predInfo) {
		String[] preds = predInfo.split("\\s+");
		if(preds != null) {
			for(int i = 0; i < preds.length; i++) {
				String[] info = preds[i].split(":");
				if(info.length != 4) {
					System.err.println("Malformed pred-info field, skipping (value was \"" + preds[i] + "\")\nPRED-INFO: " + predInfo + "\n");
					continue;
				}
				goldPred2Tag.put(info[0], DefaultTokenizer.unescape(info[1]));
				goldPredPOS.put(info[0], DefaultTokenizer.unescape(info[2]));
			}
		} 
	}

	public void setIncludeGold(boolean v) {
		this.goldStdTagInsert = v;
	}

	@Override
	public void loadPriorModel(File priorModelFile, File vocabFile) {
		try {
			priorModel = new STPriorModel(priorModelFile.getAbsolutePath(), vocabFile.getAbsolutePath());
		} catch (IOException e) {
			System.err.println("Unable to load prior model or vocab file");
			e.printStackTrace();
		}
	}
	
	@Override
	public void loadProtoModel(File hyperModelFile) {
		this.protoHTModel = new ZLMaxentModel(hyperModelFile.getAbsolutePath()); 
	}
	
	public String getSRILMFactors() {
		StringBuilder out = new StringBuilder();
		out.append("<s> ");
		for(LfGraphNode n : nomTable.values()) {
			if(goldPred2Tag.get(n.getPred().getNominal().getName()) == null) {
				continue; // skip has-rel for now
			}
			out.append(DefaultTokenizer.escape(n.getPredicateName()));
			out.append(":S-");
			out.append(DefaultTokenizer.escape(n.getPredicateName()));
			out.append(":P-");
			out.append(DefaultTokenizer.escape(getPOS(n)));
			out.append(":T-");
			out.append(DefaultTokenizer.escape(goldPred2Tag.get(n.getPred().getNominal().getName())));
			out.append(" ");
		}
		out.append("</s>\n");
		return out.toString();
	}

	public Word getPredAsWord(int idx) {
		LfGraphNode n = findNode(idx);
		Word w = Word.createWord(n.getPredicateName(), null, null, n.getPredicateName(),
				getPOS(n), getGoldSupertag(n), null);
		return w;
	}
	private Word getPredAsWord() {
		Word w = Word.createWord(currentPred.getPredicateName(), null, null, currentPred.getPredicateName(), getPOS(currentPred), getGoldSupertag(currentPred), null);
		return w;
	}

	@SuppressWarnings("boxing")
	public static void main(String[] args) throws IOException {
		String usage = "\nhypertagger (-i <input>) (-o <output> [defaults to <stdout>]) (-c <config file>)\n";
		if (args.length > 0 && args[0].equals("-h")) {
			System.out.println(usage);
			System.exit(0);
		}
		OptionParser o = new OptionParser();
		o.acceptsAll(asList("help", "h"), "this message");
		o.acceptsAll(asList("quiet", "q"), "print no status messages");
		OptionSpec<Double> b_s = o.acceptsAll(asList("beta", "b"), "ignore betas in config file and use this value").withRequiredArg().ofType(Double.class);
		OptionSpec<File> gr_s = o.acceptsAll(asList("g", "grammar")).withRequiredArg().ofType(File.class).describedAs("grammar filename");
		OptionSpec<File> corpusDir_s = o.acceptsAll(asList("d", "lf-dir")).withRequiredArg().ofType(File.class).describedAs("Directory to change to before searching for XML files");
		OptionSpec<File> configFile_s = o.acceptsAll(asList("c", "config")).withRequiredArg().ofType(File.class).describedAs("configfilename");
		OptionSpec<File> output_s = o.acceptsAll(asList("o", "output")).withRequiredArg().ofType(File.class).describedAs("output filename");
		OptionSpec<File> dump_s = o.acceptsAll(asList("dump-tags", "T")).withRequiredArg().ofType(File.class).describedAs("dump predicted tags to file");
		o.acceptsAll(asList("goldstd", "G"), "include gold-standard supertags in tag dump");
		OptionSet options = o.parse(args);
		File outputF = options.valueOf(output_s);
		File dumpF = options.valueOf(dump_s);
		File configFile = options.valueOf(configFile_s);
		BufferedWriter out = null;
		BufferedWriter dump = null;
		ArrayList<ResultSink> resBetas;
		boolean quiet = options.has("q");
		int lfcount = 0;
		try {
			out = (output_s.equals("stdout")) ? new BufferedWriter(new OutputStreamWriter(System.out)) : new BufferedWriter(new FileWriter(outputF));
		} 
		catch (IOException ex) {
			System.err.print("Output file " + outputF + " could not be opened.  Exiting...");
			Logger.getLogger(STPriorModel.class.getName()).log(Level.SEVERE, null, ex);
			System.exit(1);
		} 
		ZLMaxentHypertagger ht = ZLMaxentHypertagger.ZLMaxentHypertaggerFactory(configFile.getAbsolutePath());
		if(options.has("T")) {
			try {
				dump = new BufferedWriter(new FileWriter(dumpF));
			}
			catch(IOException e) {
				System.err.print("Output file " + dumpF + " could not be opened.  Exiting...");
				System.exit(1);
			}
		}
		if(options.has("b")) {
			double beta = options.valueOf(b_s);
			ht.betas = new double[1];
			ht.betas[0] = beta;
		}
		resBetas = new ArrayList<ResultSink>(ht.betas.length);
		for(int i = 0; i < ht.betas.length; i++) {
			ResultSink r = new ResultSink();
			resBetas.add(r);
		}
		ArrayList<BufferedWriter> errFiles = new ArrayList<BufferedWriter>();
		for(int i = 0; i < ht.betas.length; i++) {
			File logdir = new File("logs");
			if (!logdir.exists()) logdir.mkdirs();
			BufferedWriter b = new BufferedWriter(new FileWriter(new File("logs/tagdict.err.out." + i)));
			b.write("### beta = " + ht.betas[i] + "\n");
			errFiles.add(b);
		}
		LFLoader lfs = new LFLoader(options.valueOf(gr_s), options.valueOf(corpusDir_s), options.nonOptionArguments());
		while(lfs.hasNext()) {
			lfcount++;
			LFInfo lfi = lfs.next();
			try {
				ht.setLF(lfi.getLF());
				ht.storeGoldStdPredInfo(lfi.getFullWords());
				ht.setLFNum(lfi.getLFNum());
			} 
			catch (FeatureExtractionException e) {
				e.printStackTrace();
			}
			List<List<Pair<Double,String>>> lfTagging = new ArrayList<List<Pair<Double,String>>>();
			List<Word> gsTagging = new ArrayList<Word>();
			List<Pair<Double,String>> tags;
			Word w;
			for(int bi = 0; bi < ht.betas.length; bi++) {
				gsTagging = new ArrayList<Word>();
				lfTagging = new ArrayList<List<Pair<Double,String>>>();
				ht.setBetaIndex(bi);
				ht.tdErr = errFiles.get(bi); 
				for(int i = 0; i < ht.maxIndex(); i++) {
					ht.setPred(i);
					if(ht.currentPred == null) {
						//System.err.println("Skipping null pred " + i);
						continue;
					}
					w = ht.getPredAsWord();
					tags = ht.getSupertagsAsList();
					lfTagging.add(tags);
					gsTagging.add(w);
					if(dump != null) {
						if(options.has("G") && w.getSupertag() != null) {
							dump.write(w.getSupertag() + " ");
						}
						dump.write(w.getForm() + " ");
						for(int j = 0; j < tags.size(); j++) {
							dump.write(tags.get(j).a + " " + tags.get(j).b + " ");
						}
						dump.write("\n");
					}
				}
				resBetas.get(bi).addSent(lfTagging, gsTagging);
				if(!quiet) {
					System.err.println("LFs processed:       " + lfcount + "\r");
				}
				ht.tdErr.flush();
			}
		}
		if(dump != null) {
			dump.flush();
			dump.close();
		}
		for(int i = 0; i < ht.betas.length; i++) {
			errFiles.get(i).close();
		}
		for(int i = 0; i < ht.betas.length; i++) {
			out.write("---------------\n");
			out.write("BETA: " + ht.betas[i] + "\n");
			out.write(resBetas.get(i).report());
		}
		out.flush();
		out.close();
	}

	private void setBetaIndex(int bi) {
		this.currentBeta = bi;
	}
}