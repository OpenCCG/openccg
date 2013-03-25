package plugins;

import opennlp.ccg.ngrams.*;
import opennlp.ccg.synsem.*;
import java.io.*;

public class MyNgramGenSynProduct extends SignScorerProduct
{
    public MyNgramGenSynProduct() throws IOException {
	super(new SignScorer[] { new MyGenSynScorer(), new MyNgramCombo() }); 
    }
}
