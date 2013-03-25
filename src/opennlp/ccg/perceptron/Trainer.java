///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2008-2013 Michael White
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

//import java.util.*;
import java.io.*;

/**
 * Trains an averaged perceptron model by iterating through an event file. 
 * 
 * The main routine trains and saves a model.  Options include saving 
 * the final, non-averaged model, and saving the averaged model after 
 * every N iterations.  There's also an option to keep the events in 
 * memory for subsequent iterations.
 * 
 * An alphabet must be supplied as input.  The main routine of the 
 * Alphabet class can be used to derive an alphabet from a training
 * file.
 * 
 * @author Michael White
 * @version     $Revision: 1.4 $, $Date: 2009/06/18 23:38:57 $
 */
public class Trainer {

	/** The training file. */
	public final String trainfile;
	
	/** The alphabet. */
	public final Alphabet alphabet;
	
	/** The number of training iterations. */
	public final int iterations;
	
	/** The model file. */
	public final String modelfile;
	
	/** The in-memory flag. */
	public final boolean inMemory;
	
	/** The current model. */
	public final Model currentModel;
	
	/** The averaged model. */
	public final Model averagedModel;
	
	/** The model for averaging over the current iteration. */
	private Model iterationModel;
	
	/** The number of iterations to use when saving every N iterations. */
	private int saveEveryNth = 0;

	
	/** Constructor. */
	public Trainer(String trainfile, String alphabetfile, int iterations, String modelfile, boolean inMemory) throws IOException {
		this.trainfile = trainfile;
		this.alphabet = new Alphabet(alphabetfile);
		this.iterations = iterations;
		this.modelfile = modelfile;
		this.inMemory = inMemory;
		this.currentModel = new Model(alphabet);
		this.averagedModel = new Model(alphabet);
		this.iterationModel = new Model(alphabet);
	}
	
	/** Initializes the model. */
	public void initModel(String initmodelfile) throws IOException {
		Model model = new Model(initmodelfile);
		currentModel.set(model);
		averagedModel.set(model);
	}
	
	
	/** Train the model, reporting accuracy of the averaged model for each iteration and the final model. */
	public void train() throws IOException {
		// open training file
		EventFile eventFile = new EventFile(trainfile, alphabet, inMemory);
		// iterate
		boolean converged = false;
		for (int i=0; i < iterations; i++) {
			System.out.println("iteration: " + i);
			// reset training file
			eventFile.reset();
			// zero iteration model
			iterationModel.zero();
			// init counters
			int updates = 0; int correct = 0; int total = 0;
			EventFile.Block block;
			// go through training file
			while ( (block = eventFile.nextBlock()) != null ) {
				total++;
				// update if wrong
				EventFile.Event modelBest = currentModel.best(block);
				EventFile.Event actualBest = block.best();
				if (modelBest != actualBest) {
					updates++;
					currentModel.add(actualBest.features);
					currentModel.subtract(modelBest.features);
				}
				// see if averaged model correct
				EventFile.Event avgModelBest = averagedModel.best(block);
				if (avgModelBest == actualBest) correct++;
				// update iteration model
				iterationModel.add(currentModel);
			}
			// divide iteration model by total to yield an average for this iteration, 
			// and divide by iteration number (+1) to yield proportion of this 
			// iteration to averaged model; combine these two steps into one call
			double denominator = 1.0 * total * (i+1);
			iterationModel.multiply(1.0 / denominator);
			// multiply averaged model by i/(i+1) for proportion of previous iterations
			if (i > 0) {
				double mult = 1.0 * i / (i+1);
				averagedModel.multiply(mult);
			}
			// updated averaged model
			averagedModel.add(iterationModel);
			// report
			System.out.println("updates: " + updates);
			System.out.println("avg model correct: " + correct + " total: " + total + " accuracy: " + (1.0 * correct / total));
			System.out.println();
			if (updates == 0) {
				System.out.println("converged");
				System.out.println();
				converged = true; break;
			}
			// save every nth
			if (saveEveryNth > 0 && i < (iterations-1) && i % saveEveryNth == 0) {
				String nthModelfile = nthFilename(modelfile, i);
				System.out.println("Saving model to " + nthModelfile);
				averagedModel.save(nthModelfile);
				System.out.println();
			}
		}
		// do one more iteration to compute accuracy if not converged
		if (!converged) {
			System.out.println("final iteration: ");
			// reset training file
			eventFile.reset();
			// init counters
			int finalCorrect = 0; int correct = 0; int total = 0;
			EventFile.Block block;
			// go through training file
			while ( (block = eventFile.nextBlock()) != null ) {
				total++;
				// see if correct
				EventFile.Event modelBest = currentModel.best(block);
				EventFile.Event avgModelBest = averagedModel.best(block);
				EventFile.Event actualBest = block.best();
				if (modelBest == actualBest) finalCorrect++;
				if (avgModelBest == actualBest) correct++;
			}
			// report
			System.out.println("final model correct: " + finalCorrect + " total: " + total + " accuracy: " + (1.0 * finalCorrect / total));
			System.out.println("avg model correct: " + correct + " total: " + total + " accuracy: " + (1.0 * correct / total));
			System.out.println();
		}
		// close training file
		eventFile.close();
	}

	
	/** Returns a filename with .N added before the extension, if any. */
	public static String nthFilename(String filename, int N) {
		int lastdot = filename.lastIndexOf('.');
		if (lastdot > 0) return filename.substring(0,lastdot) + "." + N + filename.substring(lastdot);
		else return filename + "." + N;
	}
	
	
	/**
	 * Trains an averaged perceptron model from the training file using the alphabet file and the given 
	 * number of iterations, saving the file to model file.
	 * The final (non-averaged) model can optionally be saved using the -f option, 
	 * and intermediate models can be saved every N iterations using the -n option.
	 * The -in_mem option keeps the events in memory for subsequent iterations.
	 */
	public static void main(String[] args) throws IOException {
		// help
		if (args.length < 4) {
			System.out.println(
				"Usage: java perceptron.Trainer <traineventfile> <alphabetfile> <iterations> <modelfile> " + 
				"(-i <initmodelfile>) (-f <finalmodelfile>) (-n <save-every-nth>) (-in_mem)"
			);
			System.exit(0);
		}
		// args
		String traineventfile = args[0];
		String alphabetfile = args[1];
		int iterations = Integer.parseInt(args[2]);
		String modelfile = args[3];
		String initmodelfile = null;
		String finalmodelfile = null;
		int saveEveryNth = 0;
		boolean inMemory = false;
		for (int i=4; i < args.length; i++) {
			if (args[i].equals("-i")) initmodelfile = args[++i]; 
			if (args[i].equals("-f")) finalmodelfile = args[++i]; 
			if (args[i].equals("-n")) saveEveryNth = Integer.parseInt(args[++i]); 
			if (args[i].equals("-in_mem")) inMemory = true;
		}
		// setup, train
		System.out.println("Training on " + traineventfile + " using " + alphabetfile + " for " + iterations + " iterations");
		if (initmodelfile != null) System.out.println("with " + initmodelfile + " as the initial model");
		if (inMemory) System.out.println("keeping events in memory");
		System.out.println();
		Trainer trainer = new Trainer(traineventfile, alphabetfile, iterations, modelfile, inMemory);
		if (initmodelfile != null) trainer.initModel(initmodelfile);
		trainer.saveEveryNth = saveEveryNth;
		trainer.train();
		// save model
		System.out.println("Saving model to " + modelfile);
		trainer.averagedModel.save(modelfile);
		if (finalmodelfile != null) {
			System.out.println("Saving model to " + finalmodelfile);
			trainer.currentModel.save(finalmodelfile);
		}
	}

}
