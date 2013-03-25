///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2008 Michael White
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

package opennlp.ccg.perceptron;

import java.util.*;
import java.io.*;

import opennlp.ccg.perceptron.Alphabet.Feature;
import opennlp.ccg.util.Pair;

/**
 * A model is a vector of weights for an alphabet.
 * 
 * A model can be read from a file, which starts with the number 
 * of features on one line, followed by one line per feature pairing 
 * the feature name with its weight.
 * 
 * A new model with all zero weights can also be created from an alphabet.
 * 
 * The main routine tests the model on an event file.
 * 
 * @author Michael White
 * @version     $Revision: 1.7 $, $Date: 2009/11/09 18:54:30 $
 */
public class Model {

	/** Filter interface for adjusting feature weights when loading a model. */
	public interface FeatureFilter {
		/** Returns the modified feature weight for the given feature. */
		public double adjustedWeight(String name, double weight);
	}

	/** Flag for whether to print debugging info to System.err. */
	public boolean debug = false;
	
	// weight vector
	private double[] weights;
	
	// alphabet
	private Alphabet alphabet;

	
	/** Constructor with alphabet, for a new model with all zero weights. */
	public Model(Alphabet alphabet) {
		this.alphabet = alphabet;
		weights = new double[alphabet.size()];
		zero();
	}
	
	/** Constructor to load a model from a file. The alphabet is set to closed. */
	public Model(String filename) throws IOException {
	    this(filename, null);
	}

	/**
	 * Constructor to load a model from a file using a feature filter. The
	 * alphabet is set to closed.
	 */
	public Model(String filename, FeatureFilter filter) throws IOException {
		Reader reader = EventFile.openReader(new File(filename));
		StreamTokenizer tokenizer = EventFile.initTokenizer(reader);
		tokenizer.nextToken();
		int size = Integer.parseInt(tokenizer.sval);
		alphabet = new Alphabet(size);
		weights = new double[size];
		for (int i = 0; i < size; i++) {
			tokenizer.nextToken();
			String name = tokenizer.sval;
			tokenizer.nextToken();
			double weight = Double.parseDouble(tokenizer.sval);
			if (filter != null)
				weight = filter.adjustedWeight(name, weight);
			alphabet.add(name);
			weights[i] = weight;
		}
		reader.close();
		alphabet.setClosed(true);
	}
	
	/** Returns the size of the model. */
	public int size() { return weights.length; }
	
	/** Returns the alphabet. */
	public Alphabet getAlphabet() { return alphabet; }
	
	
	/** Returns the weight for the given index. */
	public double getWeight(int index) { return weights[index]; }
	
	/** Returns the weight for the given feature. */
	public double getWeight(String feat) { return weights[alphabet.index(feat).getIndex()]; }
	
	/** Returns the weight for the given feature. */
	public double getWeight(Alphabet.Feature f) { return weights[f.getIndex()]; }
	
	/** Sets the weight for the given index. */
	public void setWeight(int index, double weight) { weights[index] = weight; }
	
	/** Sets the weight for the given feature. */
	public void setWeight(String feat, double weight) { weights[alphabet.index(feat).getIndex()] = weight; }

	/** Sets the weight for the given feature. */
	public void setWeight(Alphabet.Feature f, double weight) { weights[f.getIndex()] = weight; }

	
	/** Returns the dot product of the weights and features. */
	public double score(FeatureVector fv) {
		double retval = 0.0;
		for (FeatureVector.Iterator it = fv.iterator(); it.hasNext(); ) {
			Feature feat = it.nextFeature();
			Float value = it.nextValue();
			Integer index = feat.getIndex();
			if (index == null) continue;
			retval += weights[index] * value;
		}
		if (debug) System.err.println("score: " + retval + " " + fv);
		return retval;
	}
	
	/** Adds the feature vector values to the weights. */
	public void add(FeatureVector fv) {
		for (FeatureVector.Iterator it = fv.iterator(); it.hasNext(); ) {
			Feature feat = it.nextFeature();
			Float value = it.nextValue();
			Integer index = feat.getIndex();
			if (index == null) continue;
			weights[index] += value;
		}
	}
	
	/** Subtracts the feature vector values from the weights. */
	public void subtract(FeatureVector fv) {
		for (FeatureVector.Iterator it = fv.iterator(); it.hasNext(); ) {
			Feature feat = it.nextFeature();
			Float value = it.nextValue();
			Integer index = feat.getIndex();
			if (index == null) continue;
			weights[index] -= value;
		}
	}

	
	/** Adds the given model's weights to this model.  The models are assumed to share the same alphabet. */
	public void add(Model model) {
		for (int i=0; i < weights.length; i++) {
			weights[i] += model.weights[i];
		}
	}
	
	/** Multiplies the weights by the given number. */
	public void multiply(double num) {
		for (int i=0; i < weights.length; i++) {
			weights[i] *= num;
		}
	}

	/** Resets the weights to zero. */
	public void zero() {
		for (int i=0; i < weights.length; i++) {
			weights[i] = 0.0;
		}
	}

	
	/** Sets this model's weights to the given model's ones, where the alphabets intersect. */
	public void set(Model model) {
		zero();
		for (int i=0; i < model.weights.length; i++) {
			Alphabet.Feature f = model.alphabet.feature(i);
			Alphabet.Feature f0 = alphabet.index(f);
			if (f0 == null || f0.getIndex() == null) continue;
			weights[f0.getIndex()] = model.weights[i];
		}
	}
	
	
	/** Returns the best event (first tied if ties). */
	public FeatureVector best(List<FeatureVector> fvs) {
		FeatureVector retval = null; double max = Double.NEGATIVE_INFINITY;
		for (FeatureVector fv : fvs) {
			double score = score(fv);
			if (score > max) { retval = fv; max = score; }
		}
		return retval;
	}
	
	/** Returns the best event (first tied if ties). */
	public EventFile.Event best(EventFile.Block block) {
		EventFile.Event retval = null; double max = Double.NEGATIVE_INFINITY;
		for (EventFile.Event event : block.events) {
			double score = score(event.features);
			if (score > max) { retval = event; max = score; }
		}
		return retval;
	}
	
	/** Returns the accuracy on the event file. */
	public double accuracy(EventFile eventFile) throws IOException {
		if (alphabet != eventFile.getAlphabet()) {
			throw new RuntimeException("Model and EventFile must share the same alphabet!");
		}
		int correct = 0; int total = 0;
		EventFile.Block block;
		while ( (block = eventFile.nextBlock()) != null ) {
			total++;
			if (best(block) == block.best()) {
				correct++;
				if (debug) System.err.println("CORRECT");
			}
			else {
				if (debug) System.err.println("WRONG; best: " + block.best());
			}
		}
		if (debug) System.err.println("correct: " + correct + " total: " + total);
		return 1.0 * correct / total;
	}
	
	
	/** Saves the model to a file, filtering out zero weights. */
	public void save(String filename) throws IOException { save(filename, 0.0); }
	
	/** Saves the model to a file, filtering out weights whose absolute value does not exceed the pruning value. */
	public void save(String filename, double minPrune) throws IOException {
		File file = new File(filename);
		PrintWriter out = EventFile.openWriter(file);
		// calc pruned size
		int size = size();
		int pruned = 0;
		for (int i=0; i < size; i++) if (Math.abs(weights[i]) <= minPrune) pruned++;
		int prunedSize = size - pruned;
		// write pruned size 
		out.println(Integer.toString(prunedSize));
		// collect unpruned weights
		List<Pair<Feature,Double>> featWeights = new ArrayList<Pair<Feature,Double>>(prunedSize);
		for (int i=0; i < size; i++) {
			if (Math.abs(weights[i]) <= minPrune) continue;
			featWeights.add(new Pair<Feature,Double>(alphabet.feature(i), weights[i]));
		}
		// sort weights by descending absolute value
		// (further sorting alphabetically may take too long)
		Collections.sort(
				featWeights, 
				new Comparator<Pair<Feature,Double>>() {
					public int compare(Pair<Feature,Double> entry1, Pair<Feature,Double> entry2) {
						double val1 = Math.abs(entry1.b); double val2 = Math.abs(entry2.b);
						if (val1 > val2) return -1;
						if (val1 < val2) return 1;
						return 0;
						//return entry1.a.name().compareTo(entry2.a.name());
					}
				}
		);
		// write sorted weights
		for (Pair<Feature,Double> fw : featWeights) {
			out.println(fw.a.name() + " " + fw.b);
		}
		out.close();
	}
	
	
	/**
	 * Loads a model from a file and tests it on the given event file.
	 */
	public static void main(String[] args) throws IOException {
		// help
		if (args.length < 2) {
			System.out.println("Usage: java perceptron.Model <modelfile> <eventfile> (-debug)");
			System.exit(0);
		}
		// args
		String modelfile = args[0];
		String eventfile = args[1];
		boolean debug = Arrays.asList(args).contains("-debug");
		// load model
		System.out.println("Loading model from: " + modelfile);
		Model model = new Model(modelfile); 
		model.debug = debug;
		System.out.println("model size: " + model.size());
		System.out.println("debug: " + debug);
		// compute accuracy
		System.out.println("Scoring events in: " + eventfile);
		EventFile eventFile = new EventFile(eventfile, model.alphabet);
		double accuracy = model.accuracy(eventFile);
		System.out.println("accuracy: " + accuracy);
	}
}
