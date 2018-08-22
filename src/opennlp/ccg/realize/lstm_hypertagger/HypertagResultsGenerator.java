///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2018 Reid Fu
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

package opennlp.ccg.realize.lstm_hypertagger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import opennlp.ccg.realize.feat_extract.LogicalForm;
import opennlp.ccg.realize.feat_print.AnnotSeqPrinter;
import opennlp.ccg.realize.hylo_feat_extract.HyloFlatLFParser;
import opennlp.ccg.realize.linearizer.*;
import opennlp.ccg.hylo.HyloHelper;
import opennlp.ccg.hylo.SatOp;

public class HypertagResultsGenerator {
	private HyloFlatLFParser parser;
	private LinConfig config;
	private Linearizer linearizer;
	private AnnotSeqPrinter printer;
	private int portNum;
	private double maxBeta;
	private HypertagResultsProcessor processor;
	
	private static String OPENCCG_HOME = System.getProperty("user.dir");
	static {
		OPENCCG_HOME = OPENCCG_HOME.substring(0, OPENCCG_HOME.lastIndexOf("openccg/")) + "openccg/";
	}
	private static String TAGGERFLOW_HOME = OPENCCG_HOME + "src/hypertagger_lstm/";
	private String START_SERVER_COMMAND;
	private String START_CLIENT_COMMAND;
	private Process server;	
	private static Logger LOGGER = LSTMHypertaggerLogger.LOGGER;
	
	public HypertagResultsGenerator(LinConfig linConfig, int portNum, double maxBeta, HypertagResultsProcessor processor)
			throws IOException, InterruptedException {
		parser = new HyloFlatLFParser();
		this.config = linConfig;
		LSTMHypertaggerLogger.logLinConfig(config);
		this.linearizer = linConfig.getLinearizer();
		printer = new AnnotSeqPrinter();
		this.portNum = portNum;
		this.maxBeta = maxBeta;
		this.processor = processor;
		
		startServer();
	}
	public void startServer() throws IOException, InterruptedException {
		START_SERVER_COMMAND = TAGGERFLOW_HOME + "server.py";
		server = new ProcessBuilder().command("python", START_SERVER_COMMAND, "" + portNum,
				config.featOrderName(), config.childOrderName(), "" + maxBeta).start();
		System.out.println("Hypertagger server started");
		LOGGER.log(Level.INFO, "Hypertagger server started");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(server.getInputStream()));
		String line = null;
		while(line == null) {
			line = br.readLine();
			if(line == null)
				Thread.sleep(5000);
		}
		System.out.println(line);
		Thread.sleep(5000);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
		        server.destroy();
		    }
		});
	}	
	public HypertagResults getHypertagResults(List<SatOp> preds) throws IOException {
		LogicalForm sentence = parser.parse(preds);
		LSTMHypertaggerLogger.logLogicalForm(sentence);
		List<String> linOrder = linearizer.getOrder(sentence, sentence.getHead(), new HashSet<String>(), config);
		LOGGER.log(Level.INFO, "Linearization: " + linOrder.toString());
		String annotSeq = printer.sentFeatString(sentence, config.featOrder(), linOrder);
		LOGGER.log(Level.INFO, "AnnotSeq: " + annotSeq);
		
		List<String> linearization = augmentLinOrder(linOrder);
		List<Map<String,Double>> results = callHypertaggerClient(annotSeq, linearization.size());
		List<String> nomOrder = getNomOrder(preds);
		LOGGER.log(Level.INFO, "Nom order: " + nomOrder.toString());
		return new HypertagResults(nomOrder, linearization, results);
	}
	@SuppressWarnings("unused")
	private List<String> filterLinOrder(List<String> linOrder) {
		List<String> linearization = new ArrayList<>(linOrder);
		for(int i = 0;i < linearization.size();i++) {
			String nom = linearization.get(i);
			if(nom.equals("(") || nom.equals(")")) {
				linearization.remove(i);
				i--;
			}
		}
		return linearization;
	}
	private List<String> augmentLinOrder(List<String> linOrder){
		List<String> linearization = new ArrayList<>(linOrder);
		linearization.add(0, "<s>");
		linearization.add("</s>");
		return linearization;
	}
	public List<String> getNomOrder(List<SatOp> preds) {
		List<String> nomOrder = new ArrayList<>(preds.size());
		for(SatOp pred : preds) {
			String nom = HyloHelper.getPrincipalNominal(pred).getName();
			nomOrder.add(nom);
		}
		return nomOrder;
	}
	
	public List<Map<String,Double>> callHypertaggerClient(String annotSeq, int linOrderSize) throws IOException {
		START_CLIENT_COMMAND = TAGGERFLOW_HOME + "client.py";
		Process client = new ProcessBuilder("python", START_CLIENT_COMMAND, "" + portNum, "\"" + annotSeq + "\"").start();
		BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
		String line = br.readLine();
		printError(client);
		return processor.processServerResponse(line, linOrderSize);
	}
	public void printError(Process proc) throws IOException {
		BufferedReader br2 = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
		String line2 = null;
		while((line2 = br2.readLine()) != null) {
			System.out.println(line2);
		}
	}
}
