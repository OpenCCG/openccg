package opennlp.ccg.realize.hypertagger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ZLMaxentModel {
    private MyIntegerPool intPool = new MyIntegerPool(50000);
    private double doubMax = 1.79769E+308;
    private ItemMap predMap = new ItemMap(this.intPool);
    private ItemMap outComeMap = new ItemMap(this.intPool);
    private Map<Integer,ArrayList<ZPair<Integer, Double>>> params = new HashMap<Integer,ArrayList<ZPair<Integer,Double>>>();
    private boolean loaded = false;
    private double[] probs;
    private int n_outcome;
    
    public ZLMaxentModel() {
        
    }
    public ZLMaxentModel(String modelFilename) {
        this.load(new File(modelFilename));
    }
    
    public void load(File modelFile) {
        if(!this.loaded) {
            this.loaded = true;
            BufferedReader br = null; 
            try {
                br = new BufferedReader(new FileReader(modelFile));
                String line = br.readLine();
                
                if(line.contains("#")) {
                    //DEBUG: outf.write(line+"\n");
                    line = br.readLine();
                }
                // Read in contextual predicates.
                int numPreds = Integer.parseInt(line.trim());
                for(int i=0; i<numPreds; i++) {
                    line = br.readLine().trim();
                    this.predMap.add(line);
                }
                // Read in outcomes (labels).
                line = br.readLine();
                int numOutcomes = Integer.parseInt(line);
                for(int j=0; j<numOutcomes; j++){
                    line = br.readLine().trim();
                    this.outComeMap.add(line);
                }
                // Read parameters.
                ArrayList<ArrayList<ZPair<Integer,Integer>>> tempParamsMap = new ArrayList<ArrayList<ZPair<Integer,Integer>>>();
                int numParameters = this.predMap.size();
                ArrayList<ZPair<Integer,Integer>> prms;
                int fid = 0;
                String ln = "";
                for(int q=0; q<numParameters; q++) {
                    ln = br.readLine();
                    String[] lineParts = ln.split("\\s+");
                    prms = new ArrayList<ZPair<Integer,Integer>>();
                    Integer oid;
                    for(int p=1; p<lineParts.length; p++) {
                        oid = this.intPool.getInt(Integer.parseInt(lineParts[p]));
                        prms.add(new ZPair<Integer,Integer>(oid, this.intPool.getInt(fid)));
                        fid++;
                    }
                    tempParamsMap.add(prms);
                }
                // Load theta.
                int nTheta = Integer.parseInt(br.readLine().trim());
                double[] theta = new double[nTheta];
                for(int z=0; z<theta.length; z++) {
                    theta[z] = Double.parseDouble(br.readLine().trim());
                }
                // Set the params mapping with the oid's and the theta.
                ArrayList<ZPair<Integer,Double>> tmpParamsList;
                int index = 0;
                for(ArrayList<ZPair<Integer,Integer>> param : tempParamsMap) {
                    tmpParamsList = new ArrayList<ZPair<Integer,Double>>();
                    for(ZPair<Integer,Integer> mapping : param) {
                        //System.out.print(mapping.b.intValue()+" ");
                        tmpParamsList.add(new ZPair<Integer,Double>(mapping.a, new Double(theta[mapping.b.intValue()])));
                    }
                    this.params.put(this.intPool.getInt(index), tmpParamsList);
                    index++;
                    
                }
                
                this.n_outcome = this.outComeMap.size();
                this.probs = new double[this.n_outcome];
                // Initialise the array for computing distribution over all labels.
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }
    
    public String getBestOutcome(double[] probs) {
        double maxprob = 0.0;
        int maxidx = -1;
        for(int i = 0; i < probs.length; i++) {
            if(probs[i] > maxprob) {
                maxidx = i;
                maxprob = probs[i];
            }
        }
        return getOutcome(maxidx);
    }
    
    @SuppressWarnings("boxing")
		public double[] eval(String[] context, boolean realValued) {        
        // Zero out prob distribution over labels.
        for(int i=0; i<this.probs.length; i++)
            this.probs[i] = 0.0;
        
        // Keep building up unexponentiated, unnormalised
        // scores for each outcome (each slot in this.probs).
        ArrayList<ZPair<String,Double>> pred_value =
                new ArrayList<ZPair<String,Double>>(context.length);
        
        Double one = new Double(1.0);
        // Split up the strings into (pred,double) pairs.
        int splitPoint;
        for(int m=0; m<context.length; m++) {
            if(realValued) {
            splitPoint = context[m].lastIndexOf(":");
            pred_value.add(
                    new ZPair<String,Double>(context[m].substring(0, splitPoint),
                    Double.parseDouble(context[m].substring(splitPoint+1, context[m].length()))) );
            } else {
                pred_value.add(
                    new ZPair<String,Double>(context[m],   
                    one) );
            }
        }
        
        
        ArrayList<ZPair<Integer,Double>> featureWeights;
        //for(int j=0; j<context.length; j++) {
        for(ZPair<String,Double> pv : pred_value) {
            Integer predID = this.predMap.id(pv.a);
            if(predID!=null) {
                featureWeights = this.params.get(predID);
                for(ZPair<Integer,Double> fw : featureWeights) {
                    this.probs[fw.a.intValue()] += (fw.b.doubleValue() * pv.b.doubleValue());
                }
            }
        }
        
        double sum = 0.0;
        for(int p=0; p<this.probs.length; p++) {
            try {
                this.probs[p] = Math.exp(this.probs[p]);
            } catch(Exception e) {
                this.probs[p] = this.doubMax;
            }
            sum += this.probs[p];
        }
        for(int q=0; q<this.probs.length; q++) {
            this.probs[q] /= sum;
        }
        return this.probs;
    }
    
    public String getOutcome(int index) {
        return this.outComeMap.getItem(index);
    }
}

class ItemMap {
    private Integer index;
    private Map<String, Integer> dict = new HashMap<String, Integer>();
    private Map<Integer, String> reverseDict = new HashMap<Integer, String>();
    private MyIntegerPool intPool;
    
    public ItemMap(MyIntegerPool intPool) {
        this.intPool = intPool;
        this.index =  intPool.getInt(0);
    }
    @SuppressWarnings("boxing")
		public int add(String item) {
        if(this.dict.containsKey(item)) {
            return this.dict.get(item);
        } else {
            this.dict.put(item, index);
            this.reverseDict.put(index, item);
            this.index = this.intPool.getInt(index.intValue() + 1);
            return index.intValue()-1;
            
        }
        
    }
    
    public Integer id(String item) {
        if(this.dict.containsKey(item)) {
            return this.dict.get(item);
        } else {
            return null;
        }
    }
    
    public int size() {
        return this.dict.size();
    }
    
    public String getItem(int i) {
        return this.reverseDict.get(this.intPool.getInt(i));
    }
}


class ZPair<A,B> {
    public A a;
    public B b;
    
    public ZPair(A a, B b) {
        this.a = a;
        this.b = b;
    }
}


class MyIntegerPool {
    private Integer[] _table;
    public MyIntegerPool(int size) {
        this._table = new Integer[size];
        for(int i=0; i<size; i++) {
            this._table[i] = new Integer(i);
        }
    }
    public Integer getInt(int i) {
        if (i < this._table.length && i >= 0) {
            return this._table[i];
        } else {
            return new Integer(i);
        }
    }
}
