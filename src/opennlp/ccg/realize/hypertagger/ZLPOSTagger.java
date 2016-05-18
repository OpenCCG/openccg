package opennlp.ccg.realize.hypertagger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.ccg.hylo.Nominal;
import opennlp.ccg.lexicon.DefaultTokenizer;
import opennlp.ccg.parse.postagger.ml.POSPriorModel;
import opennlp.ccg.parse.tagger.util.ConfigFileProcessor;
import opennlp.ccg.parse.tagger.util.ResultSink;
import opennlp.ccg.util.Pair;

public class ZLPOSTagger extends TagExtractor {
	ZLMaxentModel model;
	POSPriorModel priorModel;
	int prefixLength = 4;
	int suffixLength = 4;
	double beta = 0.35;
	private HashMap<String,String> goldPred2Tag=new HashMap<String,String>();
	public class ProbIndexPair implements Comparable<ProbIndexPair> {
		public double prob;
		public int index;
		public ProbIndexPair(double prob, int index) {
			this.prob = prob;
			this.index = index;
		}
		public int compareTo(ProbIndexPair o) {
			if(prob < o.prob) {
				return -1;
			}
			else if(prob == o.prob) {
				return 0;
			}
			else {
				return 1;
			}
		}
	}

	public ZLPOSTagger() { super(); }

	public ZLPOSTagger(ZLMaxentModel model) {
		this();
		this.model = model;
		this.prefixLength = 4;
		this.suffixLength = 4;
		this.beta = 0.35; // 0.4 delivers 1.08 POStags/pred
	}
	public ZLPOSTagger(ZLMaxentModel model, POSPriorModel priorModel) {
		this();
		this.model = model;
		this.prefixLength = 4;
		this.suffixLength = 4;
		this.beta = 0.35;
		this.priorModel = priorModel;
	}
	public POSPriorModel getPriorModel() {
		return priorModel;
	}

	public void setPriorModel(POSPriorModel priorModel) {
		this.priorModel = priorModel;
	}
	/** get the features for one node
	 * 
	 * FO -- fan-out, i.e. number of children
	 * PN -- predicate name
	 * RN -- parent name 
	 * RT -- parent relation
	 * CT -- type of child
	 * CN -- name of child
	 * NA -- number of Argument children
	 * A0N, A1N, ... -- names of argument children (by default)
	 * PX -- prefix (N characters)
	 * SX -- suffix (M characters)
	 * HD -- has a digit
	 * UH -- has an uppercase character or a hyphen
	 * 
	 * @param n The graph node to extract features from
	 * @return An array of strings representing the features
	 */	
	// mww: switched to configurable arg names
	@SuppressWarnings("boxing")
	protected FeatureList getFeatures(LfGraphNode n) {
		FeatureList feats = new FeatureList();
		int argchildren = 0;
		feats.addFeature("PN", n.getPredicateName());
		feats.addFeature("FO", Integer.toString(n.getNumChildren()));
		// add name of parent, if any parent, and parent relation
		if(n.getMultiParents().size() > 0) {
			for(LfGraphNode parent : n.getMultiParents() ) {
				feats.addFeature("RN", parent.getPredicateName());
				// add class name, if available
				String cls = parent.getPred().getNominal().toString();
				// string is in X:Y:Z format. Remove 'X:' leaving 'Y:Z'.
				if(cls != null && cls.indexOf(':') > 0) {
					String cfeat = cls.substring(cls.indexOf(':') + 1);
					feats.addFeature("XP", cfeat);
				}
			}
		}
		else {
			feats.addFeature("RN", "0");
		}
		// add types of children, count up argument children
		for(LfGraphLink lnk  : n.getChildren()) {
			feats.addFeature("CT", lnk.getLabel());
			if(lnk.getTarget() != null) { 
				feats.addFeature("CN", lnk.getTarget().getPredicateName());
				// mww: use short arg name
				String shortArgName = argNameMap.get(lnk.getLabel());
				if (shortArgName != null) {
					// increment argchild count
					argchildren++;
					feats.addFeature(shortArgName + "N", lnk.getTarget().getPredicateName());
					// add class info for arg child, if applicable
					String cls = lnk.getTarget().getPred().getNominal().toString();
					// string is in X:Y:Z format. Remove 'X:' leaving 'Y:Z'.
					if(cls.indexOf(':') > 0) {
						String cfeat = cls.substring(cls.indexOf(':') + 1);
						// mww: for backwards compatibility
						String argNumOrName = (shortArgName.startsWith("A")) ? shortArgName.substring(1) : shortArgName;
						feats.addFeature("X" + argNumOrName + "D", cfeat);
					}
				}
			}
		}
		feats.addFeature("NA", Integer.toString(argchildren));
		// class name feature, if available
		Nominal idx = n.getPred().getNominal();
		String cls = idx.toString();
		// string is in X:Y:Z format. Remove 'X:' leaving 'Y:Z'.
		if(cls.indexOf(':') > 0) {
			String cfeat = cls.substring(cls.indexOf(':') + 1);
			feats.addFeature("XC", cfeat);
		}
		// compute prefix and suffix
		String predname = n.getPredicateName();
		// chop .04 part, if any
		Pattern pat = Pattern.compile("(\\w+)\\.\\d+$");
		String basePredName = new String(predname);
		Matcher mat = pat.matcher(basePredName);
		if(mat.matches()) {
			basePredName = mat.group(1);
		}
		if(basePredName.length() > prefixLength + 1) {
			String prefix = predname.substring(0, prefixLength);
			feats.addFeature("PX", prefix);
		}
		if(basePredName.length() > suffixLength + 1) {
			String suffix = basePredName.substring(basePredName.length() - suffixLength, basePredName.length());
			feats.addFeature("SX", suffix);
		}
		// check for digit and/or (upcase char and hyphen)
		if(basePredName.matches("\\d+")) {
			feats.addFeature("HD", "1");
		}
		else {
			feats.addFeature("HD", "0");
		}
		if(predname.matches("[A-Z]+") || predname.matches("-+")) {
			// XXX hack, because I don't think these PASS nodes should have a positive val
			// for this feature
			if(predname != "PASS") {
				feats.addFeature("UH", "1");
			}
			else {
				feats.addFeature("UH", "0");
			}
		}
		else {
			feats.addFeature("UH", "0");
		}
		// features from prior model, PPOS, if applicable
		if(priorModel != null) {
			List<Pair<Double,String>> priors = priorModel.getPriors(predname);
			double beta = 0.1;
      double best = priors.get(0).a;
      for(Pair<Double,String> prior : priors) {
          if(prior.a > (beta * best)) {
              // add the feature PPOS=<POSTAG>:<log-prob>
              feats.addFeatureWithProb("PPOS", prior.b, prior.a);
          } else {
              break;
          }
      }
		}
		return feats;
	}

	/** Get the features for the index'th node
	 * 
	 * @param index The index into the LF
	 * @return An array of strings representing the features
	 */
	public FeatureList getFeatures(int index) {
		for(LfGraphNode n : nomTable.values()) {
			if(n.getIndex() == index) {
				return getFeatures(n);
			}
		}
		return null; // bad index
	}
	public int getPrefixLength() {
		return prefixLength;
	}
	public void setPrefixLength(int prefixLength) {
		this.prefixLength = prefixLength;
	}
	public int getSuffixLength() {
		return suffixLength;
	}
	public void setSuffixLength(int suffixLength) {
		this.suffixLength = suffixLength;
	}
	/** Get all features for the current LF as a single multi-line string.
	 *  Used during extraction of training data.
	 * @return All features for the current LF.
	 */
	// formerly known as getAllFeaturesAndPOS()
	public String getAllFeaturesAndAnswer() {
		StringBuilder output = new StringBuilder();
		for(LfGraphNode n : nomTable.values()) {
			String feats = getFeatures(n).getAllFeatures();
			String postag = goldPred2Tag.get(n.getPred().getNominal().getName());
			output = output.append(postag + " ");
			output = output.append(feats);
			output = output.append("\n");
		}
		return output.toString();
	}
	
	public String getAllFeaturesAndPOSWithID() {
		String output = "";
		for(LfGraphNode n : nomTable.values()) {
			String feats = getFeatures(n).getAllFeatures();
			String postag = n.getPred().getOrigin().getPOS();
			output = output.concat("<" + Integer.toString(LFID) + "> " + postag + " " + feats);
			output = output.concat(postag + " ");
			output = output.concat(feats);
			output = output.concat("\n");
		}
		return output;
	}

	/* Returns an arraylist of tuples (POS, probability) */
	@SuppressWarnings({ "boxing" })
	public ArrayList<Pair<String,Double>> getBetaBestPOS(FeatureList feats) {
		ArrayList<Pair<String,Double>> poss = new ArrayList<Pair<String,Double>>();
		ArrayList<ProbIndexPair> probList = new ArrayList<ProbIndexPair>();
		double[] probs = model.eval(feats.getAllFeaturesForMaxent(),true);
		for(int i = 0; i < probs.length; i++) {
			probList.add(new ProbIndexPair(probs[i], i));
		}
		Collections.sort(probList);
		Collections.reverse(probList);
		double maxProb = probList.get(0).prob;
		for(int i = 0; i < probList.size(); i++) {
			if(probList.get(i).prob >= this.beta * maxProb) {
				poss.add(new Pair<String,Double>(model.getOutcome(probList.get(i).index), probList.get(i).prob));
			}
		}
		return poss;
	}
	@SuppressWarnings("boxing")
	public ArrayList<Pair<String,Double>> getBetaBestPOS(String[] feats) {
		ArrayList<Pair<String,Double>> poss = new ArrayList<Pair<String,Double>>();
		ArrayList<ProbIndexPair> probList = new ArrayList<ProbIndexPair>();
		double[] probs = model.eval(feats,true);
		for(int i = 0; i < probs.length; i++) {
			probList.add(new ProbIndexPair(probs[i], i));
		}
		Collections.sort(probList);
		Collections.reverse(probList);
		double maxProb = probList.get(0).prob;
		for(int i = 0; i < probList.size(); i++) {
			if(probList.get(i).prob >= this.beta * maxProb) {
				poss.add(new Pair<String,Double>(model.getOutcome(probList.get(i).index), probList.get(i).prob));
			}
		}
		return poss;
	}

//	for tagging (i.e. evaluating feature list against model)
	public String getBestPOS(FeatureList feats) {
		// feed the features to the model, and get the best guess
		// at the POS tag given those features.
		double[] probs = model.eval(feats.getAllFeaturesForMaxent(),false);
		return new String(model.getBestOutcome(probs));
	}

	public double getBeta() {
		return beta;
	}

	public void setBeta(double beta) {
		this.beta = beta;
	}

	/* (non-Javadoc)
	 * @see opennlp.ccg.realize.hypertagger.TagExtractor#storeGoldStdPredInfo(java.lang.String)
	 */
	public void storeGoldStdPredInfo(String predInfo) {
		String[] preds = predInfo.split("\\s+");
		if(preds != null) {
			for(int i = 1; i < preds.length; i++) {
				String[] info = preds[i].split(":");
				if(info.length != 4) {
					System.err.println("Malformed pred-info field, skipping (value was \"" + preds[i] + "\")");
					continue;
				}
				goldPred2Tag.put(info[0], DefaultTokenizer.unescape(info[2]));
			}
		} 
	}
	public void loadPriorModel(File priorModelFile, File vocabFile) {
		try {
			priorModel = new POSPriorModel(priorModelFile.getAbsolutePath(), vocabFile.getAbsolutePath());
		} catch (IOException e) {
			System.err.println("Unable to load prior model or vocab file");
			e.printStackTrace();
		}
	}
	@SuppressWarnings({"unused" })
	public static void main(String[] args) throws IOException {
		String usage = "\nBasicPOSTagger -c <configFile> (-i <input>) (-o <output> [defaults to <stdout>])\n"+
		"                 (-b beta value) (-m model file)\n";
		if (args.length > 0 && args[0].equals("-h")) {
			System.out.println(usage);
			System.exit(0);
		}

		//SRILMFactoredBundleCorpusIterator inp = null;
		// input is just a file full of maxent events
		// i.e., we are not really tagging an lf, just testing a model by feeding it pre-extracted
		// events and comparing with the GS tags
		BufferedReader inp = null;
		BufferedWriter out = null;
		try {
			String inputCorp = null;
			String configFile = null;
			String output = null;
			String modelFile = null;
			double beta = 0;

			boolean test = false;

			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-i")) { inputCorp = args[++i]; continue; }
				if (args[i].equals("-o")) { output = args[++i];    continue; }
				if (args[i].equals("-c")) { configFile = args[++i]; continue; }
				if (args[i].equals("-m")) { modelFile = args[++i]; continue; }
				if (args[i].equals("-b")) { beta = Double.parseDouble(args[++i]); continue; }
				System.out.println("Unrecognized option: " + args[i]);
			}

			ResultSink rs = new ResultSink(ResultSink.ResultSinkType.POSTAG);

			try {                        
				inp = new BufferedReader(new FileReader(new File(inputCorp)));                
			} catch (FileNotFoundException ex) {
				System.err.print("Input corpus " + inputCorp + " not found.  Exiting...");
				Logger.getLogger(POSPriorModel.class.getName()).log(Level.SEVERE, null, ex);
				System.exit(-1);
			}  

			try {
				out = (output.equals("<stdout>")) ? new BufferedWriter(new OutputStreamWriter(System.out)) : new BufferedWriter(new FileWriter(new File(output)));
			} catch (IOException ex) {
				System.err.print("Output file " + output + " not found.  Exiting...");
				Logger.getLogger(POSPriorModel.class.getName()).log(Level.SEVERE, null, ex);
				System.exit(-1);
			}
			//String[] pathKeys = { "maxentmodel", "priormodel", "priormodelvocab", "sequencemodel" };
			//Map<String, String> opts = ConfigFileProcessor.readInConfig(configFile, pathKeys);
			//POSPriorModel posPrior = new POSPriorModel(opts.get("priormodel"), opts.get("priormodelvocab"));
			ZLPOSTagger post = new ZLPOSTagger(new ZLMaxentModel(modelFile));
			if(beta > 0) { post.setBeta(beta); }
			//post.setPriorModel(posPrior); // unneeded?
			String line;
			int count = 0;
			int wins = 0;
			int tagsPerPred = 0;
			while(true) {
				line = inp.readLine();
				if(line == null) { break; }
				count++;
				int pos = line.indexOf(' ');
				String gs = line.substring(0,pos);
				//System.err.println("GS: " + gs);
				//System.err.println("Featline: " + line.substring(pos + 1));
				String[] feats = line.substring(pos+1).split("\\s+");
				ArrayList<Pair<String,Double>> ptags = post.getBetaBestPOS(feats);
				tagsPerPred += ptags.size();
				// now check for the win...
				for(Pair<String,Double> p : ptags) {
					if(p.a.equals(gs)) {
						wins++;
						break;
					}
				}

			}
			// for now, just print overall accuracy
			out.write("Beta: " + post.getBeta() + "\n");
			out.write("Acc: " + (double)wins / (double)count * 100.0 + "\n");
			out.write("Tags/Pred: " + (double)tagsPerPred / (double)count + "\n");
			out.flush();
		} catch(Throwable t) {
			t.printStackTrace();
		} finally {
			try {                
				inp.close();
				out.close();
			} catch (IOException ex) {
				Logger.getLogger(POSPriorModel.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public static ZLPOSTagger ZLPOSTaggerFactory(String configFile) throws IOException {
		ZLPOSTagger postagger = new ZLPOSTagger();
		String[] pathKeys = { "priormodel", "priormodelvocab", "maxentmodel"};
		Map<String,String> opts = ConfigFileProcessor.readInConfig(configFile, pathKeys);
		String priorModelFile = opts.get("priormodel");
		if(priorModelFile != null) {
			String vocabFile = opts.get("priormodelvocab");
			if(vocabFile == null) {
				throw new IOException("A vocab file must be specified.");
			}
			postagger.loadPriorModel(new File(priorModelFile), new File(vocabFile));
		}
		String modelFile = opts.get("maxentmodel");
		if(modelFile == null) {
			throw new IOException("You must specify the maxent model to use.");
		}
		postagger.model = new ZLMaxentModel(modelFile);
		String betaString = opts.get("beta");
		if(betaString != null) {
			double beta = Double.parseDouble(betaString);
			postagger.beta = beta;
		}
		return postagger;
	}
}
