///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010 Dennis N. Mehay
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed inp the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

package opennlp.ccg.parse.tagger.ml;

/* A nearly literal translation of Zhang Le's pymaxent.py file
 * into Java (D.N. Mehay).
 */

import opennlp.ccg.parse.tagger.Constants.Domain;
import opennlp.ccg.parse.tagger.Constants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import opennlp.ccg.util.Pair;

public class ZLMaxentModel {
    public boolean verbose = false;
    private ItemMap predMap = null;
    private ItemMap outComeMap = null;
    ArrayList<ArrayList<Pair<Integer, Integer>>> paramsMap;
    private boolean loaded = false;
    private double[] probs;
    private int n_outcome;
    // the parameters.
    private double[] theta;
    private Double one = Constants.one;

    public ZLMaxentModel() {
    }

    public ZLMaxentModel(File model) {
        load(model);
    }

    public void load(File modelFile) {
        if (!loaded) {
            loaded = true;
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(modelFile));
                String line = br.readLine();

                if (line.contains("#")) {
                    line = br.readLine();
                }

                if (verbose) System.err.println("\nReading predicates...");
                // Read in contextual predicates.
                int numPreds = Integer.parseInt(line);
                predMap = new ItemMap();
                // read in predicates...
                for (int i = 0; i < numPreds; i++) {
                    line = br.readLine();
                    predMap.add(line);
                }

                if (verbose) System.err.println("Reading outcomes...");
                outComeMap = new ItemMap();
                // Read in outcomes (labels).
                line = br.readLine();
                int numOutcomes = Integer.parseInt(line);
                for (int j = 0; j < numOutcomes; j++) {
                    line = br.readLine();
                    outComeMap.add(line);
                }

                if (verbose) System.err.println("Reading parameters...");
                // Read parameters.                
                int numParameters = predMap.size();
                paramsMap = new ArrayList<ArrayList<Pair<Integer, Integer>>>(numParameters);
                ArrayList<Pair<Integer, Integer>> prms;
                int fid = 0;
                String ln = "";
                for (int q = 0; q < numParameters; q++) {
                    ln = br.readLine();
                    String[] lineParts = ln.split(" ");
                    prms = new ArrayList<Pair<Integer, Integer>>(Integer.parseInt(lineParts[0]));
                    Integer oid;
                    for (int p = 1; p < lineParts.length; p++) {
                        oid = Integer.valueOf(lineParts[p]); 
                        prms.add(new Pair<Integer, Integer>(oid, Integer.valueOf(fid))); 
                        fid++;
                    }
                    paramsMap.add(prms);
                }
                
                // Load theta.
                int nTheta = Integer.valueOf(br.readLine());
                if (verbose) System.err.println("Number of parameters: " + nTheta);
                theta = new double[nTheta];
                for (int z = 0; z < theta.length; z++) {
                    theta[z] = Double.parseDouble(br.readLine());
                }

                n_outcome = outComeMap.size();
                // Initialise the array for computing distribution over all labels.
                probs = new double[n_outcome];
                if (verbose) System.err.println("Number of outcomes: " + n_outcome);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    public String getBestOutcome(double[] probs) {
        double maxprob = 0.0;
        int maxidx = -1;
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > maxprob) {
                maxidx = i;
                maxprob = probs[i];
            }
        }
        return getOutcome(maxidx);
    }

    public double[] eval(Collection<Pair<String, Double>> context, boolean realValued) {
        return eval(context, realValued, Domain.PROB);
    }

    public double[] eval(Collection<Pair<String, Double>> context, boolean realValued, Domain domain) {
        // Zero out prob distribution over labels.
        for (int i = 0; i < probs.length; i++) {
            probs[i] = 0.0;
        // build up exponentiated scores.  
        }
        for (Pair<String, Double> pv : context) {
            Integer predID = predMap.id(pv.a);
            if (predID != null) {
                ArrayList<Pair<Integer, Integer>> featClassAssocs = paramsMap.get(predID.intValue());
                for (Pair<Integer, Integer> classAndAssoc : featClassAssocs) {
                    if (pv.b == one) {
                        // ln(exp(lambda * 1)) = ln(exp(lambda)^1) = ln(exp(lambda)) = lambda
                        probs[classAndAssoc.a.intValue()] += theta[classAndAssoc.b.intValue()];
                    } else {
                        // ln(exp(lambda * <act>)) = ln(exp(lambda)^<act>)
                        probs[classAndAssoc.a.intValue()] += Math.log(Math.exp(theta[classAndAssoc.b.intValue()] * pv.b.doubleValue()));
                    }
                }
            }
        }

        double sum = 0.0;
        // exponentiate the numerators for the denomenator sum.
        for (int p = 0; p < probs.length; p++) {
            sum += Math.exp(probs[p]);
        }
        sum = Math.log(sum);
        for (int q = 0; q < probs.length; q++) {
            probs[q] -= sum;
        }
        if (domain == Domain.PROB) {
            // translate back from the log domain.
            for (int q = 0; q < probs.length; q++) {
                probs[q] = Math.exp(probs[q]);
            }
        }
        return probs;
    }

    public String getOutcome(int index) {
        return outComeMap.getItem(index);
    }
}

class ItemMap {

    private Integer index;
    private Map<String, Integer> dict = new HashMap<String, Integer>();
    private Map<Integer, String> reverseDict = new HashMap<Integer, String>();

    public ItemMap() {
        index = Integer.valueOf(0);
    }

    public int add(String item) {
        if (dict.containsKey(item)) {
            return dict.get(item);
        } else {
            dict.put(item, index);
            reverseDict.put(index, item);
            index = Integer.valueOf(index.intValue() + 1);
            return index.intValue() - 1;

        }

    }

    public Integer id(String item) {
        if (dict.containsKey(item)) {
            return dict.get(item);
        } else {
            return null;
        }
    }

    public int size() {
        return dict.size();
    }

    public String getItem(int i) {
        return reverseDict.get(Integer.valueOf(i));
    }
}

class IntegerPool {

    private Integer[] _table;

    public IntegerPool(int size) {
        _table = new Integer[size];
        for (int i = 0; i < size; i++) {
            _table[i] = new Integer(i);
        }
    }

    public Integer getInt(int i) {
        if (i < _table.length && i >= 0) {
            return _table[i];
        } else {
            return new Integer(i);
        }
    }
}
