package opennlp.ccg.realize.hypertagger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import static java.util.Arrays.*;
import joptsimple.*;

import opennlp.ccg.realize.hypertagger.TagExtractor;
import opennlp.ccg.synsem.LF;
import opennlp.ccg.util.Pair;

public class TagExtract {
	private static boolean quiet = false; // when true, suppress stderr messages
	@SuppressWarnings("unused")
	private static File posModelFile;
	private static File hyperModelFile;
	private static File posPriorModelFile;
	private static File htPriorModelFile;
	private static File posVocabFile;
	private static File htVocabFile;
	private static String argnames;
	private TagExtractor tex;
	private BufferedWriter output;
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static class PairComparator implements Comparator {
		@SuppressWarnings("boxing")
		public int compare(Pair<Double,Integer> p, Pair<Double,Integer> q) {
			if(p.a > q.a) {
				return 1;
			}
			if(p.a == q.a) {
				return 0;
			}
			return -1;
		}
		public int compare(Object p, Object q) {
			return this.compare((Pair<Double,Integer>) p, (Pair<Double,Integer>)q );
		}
	}
	static class XmlFilenameFilter implements FileFilter {
		public boolean accept(File f) {
			return f.getName().toLowerCase().endsWith(".xml");
		}
	}
	public TagExtract(TagExtractor t) {
		this.tex = t;
	}
	/* TODO: this method should probably be rewritten to use LFLoader and a config file */
	public static void main(String[] args) throws IOException {
		TagExtract t = null;
		//PrintStream output = System.out;
		BufferedWriter output;
		//int lfcount = 0;
		//int lfNum = 0;
		// option processing
		OptionParser o = new OptionParser();
		o.acceptsAll(asList("help", "h"), "this message");
		o.acceptsAll(asList("quiet", "q"), "print no messages");
		o.acceptsAll(asList("pos", "pos"), "extract POS features");
		OptionSpec<File> pos_s = o.acceptsAll(asList("p", "pos-model")).withRequiredArg().ofType(File.class).describedAs("POS model to use");
		OptionSpec<File> outf = o.acceptsAll(asList("o", "output")).withRequiredArg().ofType(File.class).describedAs("output file");
		OptionSpec<File> posPrior_s = o.acceptsAll(asList("P", "pos-prior")).withRequiredArg().ofType(File.class).describedAs("POS prior model to use");
		OptionSpec<File> ht_s = o.acceptsAll(asList("y", "hyper-model")).withRequiredArg().ofType(File.class).describedAs("HT model to use as input to 2-pass model (see README)");
		OptionSpec<File> htPrior_s = o.acceptsAll(asList("H", "ht-prior")).withRequiredArg().ofType(File.class).describedAs("HT prior model to use");
		OptionSpec<File> gr_s = o.acceptsAll(asList("g", "grammar")).withRequiredArg().ofType(File.class).describedAs("grammar filename");
		OptionSpec<File> ht_vocab_s = o.acceptsAll(asList("V", "ht-prior-vocab")).withRequiredArg().ofType(File.class).describedAs("HT prior vocab filename");
		OptionSpec<File> pos_vocab_s = o.acceptsAll(asList("v", "pos-prior-vocab")).withRequiredArg().ofType(File.class).describedAs("POS prior vocab filename");
		OptionSpec<File> corpusDir_s = o.acceptsAll(asList("d", "lf-dir")).withRequiredArg().ofType(File.class).describedAs("Directory to change to before searching for XML files");
		OptionSpec<String> argnames_s = o.acceptsAll(asList("an", "argnames")).withRequiredArg().describedAs("Names of argument roles in format name(:shortname)?");
		OptionSet options = o.parse(args);
		/* if -h (help) is given, print message and exit */
		if (options.has("h") || args.length == 0) {
			o.printHelpOn(System.out);
			System.out.println("See the README for additional information.");
			System.exit(0);
		}
		output = new BufferedWriter(new FileWriter(options.valueOf(outf)));
		// some of these will be nulls, depending on what the user is trying to do
		hyperModelFile = options.valueOf(ht_s);
		posModelFile = options.valueOf(pos_s);
		posPriorModelFile = options.valueOf(posPrior_s);
		posVocabFile = options.valueOf(pos_vocab_s);
		htPriorModelFile = options.valueOf(htPrior_s);
		htVocabFile = options.valueOf(ht_vocab_s);
		argnames = options.valueOf(argnames_s);
		if(options.has("q"))
			quiet = true;
		LFLoader lfs = new LFLoader(options.valueOf(gr_s), options.valueOf(corpusDir_s), options.nonOptionArguments());
		if(options.has("pos")) {
			TagExtractor tex = new ZLPOSTagger();
			// mww: set arg names
			if (argnames != null) debug("Setting arg names to " + argnames + "\n");
			tex.setArgNames(argnames); // uses default names if null
			if(posPriorModelFile != null && posVocabFile != null) {
				debug("Loading POS model priors from " + posPriorModelFile + "\n");
				debug("Loading POS model vocab from " + posVocabFile + "\n");
				tex.loadPriorModel(posPriorModelFile, posVocabFile);
			}
			debug("Extracting POS features..." + "\n");
			t = new TagExtract(tex);
		}
		else {
			// extracting hypertags
			// using GS pos tags
			TagExtractor tex = new ZLMaxentHypertagger();
			// mww: set arg names
			if (argnames != null) debug("Setting arg names to " + argnames + "\n");
			tex.setArgNames(argnames); // uses default names if null
			if(htPriorModelFile != null && htVocabFile != null) {
				debug("Loading HT model priors from " + htPriorModelFile + "\n");
				debug("Loading HT model vocab from " + htVocabFile + "\n");
				tex.loadPriorModel(htPriorModelFile,htVocabFile);
			}
			if(hyperModelFile != null) {
				debug("Loading proto-HT model from " + hyperModelFile + "\n");
				tex.loadProtoModel(hyperModelFile);
			}
			debug("Extracting hypertagger features..." + "\n");
			t = new TagExtract(tex);
		}
		t.setOutput(output);
		while(lfs.hasNext()) {
				LFInfo lfi = lfs.next();
				LF lf = lfi.getLF();
				try {
					//lfNum++;
					t.extract(lf, lfi.getFullWords());
					//lfcount++;
					//debug("LFs extracted:       " + lfcount + "\r");
				} catch (FeatureExtractionException e) {
					debug("In LF #" + lfi.getLFNum() + ":\n");
					debug(e.toString() + "\n");
				}
		}
		output.close();
		debug("\n");
	}
	private void extract(LF flatLF, String fullWords) throws FeatureExtractionException {
		tex.storeGoldStdPredInfo(fullWords);
		tex.setLF(flatLF);
		try {
			output.write(tex.getAllFeaturesAndAnswer());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private void setOutput(BufferedWriter output) {
		this.output = output;

	}
	public static void debug(String string) {
		if(!quiet) 
			System.err.print(string);
	}
}
