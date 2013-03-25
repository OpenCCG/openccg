package nerapp;

import java.io.*;
//import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.NERClassifierCombiner;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.sequences.PlainTextDocumentReaderAndWriter;
import edu.stanford.nlp.util.CoreMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/** This tags text using the Stanford NE tagger API.
 *  <p>
 *  Usage: <code>java -cp "stanford-ner.jar:." NERApp [serializedClassifier] [fileName]</code>
 *  <p>
 *  There are no default arguments.
 *  (Created by modifying Jenny Finkel and Chris Manning's example "NERDemo.java".)
 *  <p>
 *  @author Dennis N. Mehay
 */

public class NERApp {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
      DocumentReaderAndWriter<CoreMap> readerAndWriter = new PlainTextDocumentReaderAndWriter<CoreMap>();
      String usageStr = System.getProperty("line.separator") +
                        "java -cp \"stanford-core-nlp.jar:.\" NERApp [inputFileName] [outputFileName] [classifierModelFile1] (...[classifierModelFile10])"+
                    	System.getProperty("line.separator") + System.getProperty("line.separator") +
                   	"(I.e., you can specify between one and ten classifiers whose predictions will be combined.\n"+
	                "Specify the best model first -- it will have precedence in the model combination.)" +
                        System.getProperty("line.separator");

      if (args.length < 3) {
          System.out.println(usageStr);
          System.exit(-1);
      }

      String[] classifierMods = new String[10];

      for(int j = 2; j < args.length; j++) {
	  classifierMods[j-2] = args[j].trim();
      }

      int numClassifiers = 0;
      AbstractSequenceClassifier classifier = null; //CRFClassifier.getClassifierNoExceptions(serializedClassifier);
      for(String classMod : classifierMods) { if(classMod != null) { numClassifiers++; } }
      switch (numClassifiers) {
          case 1:
	      classifier = new NERClassifierCombiner(true, false, classifierMods[0]);
	      break;
          case 2:
	      classifier = new NERClassifierCombiner(true, false, classifierMods[0], classifierMods[1]);
	      break;
          case 3:
	      classifier = new NERClassifierCombiner(true, false, classifierMods[0], classifierMods[1], classifierMods[2]);
	      break;
          case 4:
	      classifier = new NERClassifierCombiner(true, false, classifierMods[0], classifierMods[1], classifierMods[2], classifierMods[3]);
	      break;
          case 5:
	      classifier = new NERClassifierCombiner(true, false, classifierMods[0], classifierMods[1], classifierMods[2], classifierMods[3], classifierMods[4]);
	      break;
          case 6:
	      classifier = new NERClassifierCombiner(true, false, classifierMods[0], classifierMods[1], classifierMods[2], classifierMods[3], classifierMods[4],
						     classifierMods[5]);
	      break;
          case 7:
	      classifier = new NERClassifierCombiner(true, false, classifierMods[0], classifierMods[1], classifierMods[2], classifierMods[3], classifierMods[4],
						     classifierMods[5], classifierMods[6]);
	      break;
          case 8:
	      classifier = new NERClassifierCombiner(true, false, classifierMods[0], classifierMods[1], classifierMods[2], classifierMods[3], classifierMods[4],
						     classifierMods[5], classifierMods[6], classifierMods[7]);
	      break;
          case 9:
	      classifier = new NERClassifierCombiner(true, false, classifierMods[0], classifierMods[1], classifierMods[2], classifierMods[3], classifierMods[4],
						     classifierMods[5], classifierMods[6], classifierMods[7], classifierMods[8]);
	      break;
          case 10:
	      classifier = new NERClassifierCombiner(true, false, classifierMods[0], classifierMods[1], classifierMods[2], classifierMods[3], classifierMods[4],
						     classifierMods[5], classifierMods[6], classifierMods[7], classifierMods[8], classifierMods[9]);
	      break;
          default:
	      System.out.println(usageStr);
	      System.exit(-1);
      }



      Iterable<String> sents = IOUtils.readLines(args[0]);
      BufferedWriter outf = new BufferedWriter(new FileWriter(new File(args[1])));

      for (String sent : sents) {
          String[] parts = sent.split("\\s+");
          List<HasWord> wdList = new ArrayList<HasWord>(parts.length);
          for(String w : parts) {
              wdList.add(new MyWord(w));
          }
          List<CoreMap> tagging = classifier.classifySentence(wdList);
          String currNE = null;
          StringBuilder res = new StringBuilder();

          String wd, annot;
          int cursor = -1;
          for(CoreMap item : tagging) {
              cursor += 1;
              wd = item.get(CoreAnnotations.TextAnnotation.class);
              annot = item.get(CoreAnnotations.AnswerAnnotation.class);
              
              if(annot != null && annot.equals("O") || annot.equals("MISC")) {
                  annot = null;
              }

              if(currNE != null && !currNE.equals(annot)) {
                  res.append("</");  res.append(currNE); res.append(">");
                  currNE = null;
              }

              if(annot != null && !annot.equals(currNE)) {
                  currNE = annot;
                  res.append(" ");
                  res.append("<");  res.append(currNE); res.append(">");
                  res.append(wd);
              } else {
                  res.append(" ");
                  res.append(wd);
              }

          }
          if(null != currNE) {
              res.append("</");  res.append(currNE); res.append(">");
          }
          outf.write(res.toString() + System.getProperty("line.separator"));
          outf.flush();
      }
      outf.close();
    }


    public static String classifyToString(List<CoreMap> sentence, DocumentReaderAndWriter<CoreMap> readerAndWriter, AbstractSequenceClassifier classif) {
    PlainTextDocumentReaderAndWriter.OutputStyle outFormat =
      PlainTextDocumentReaderAndWriter.OutputStyle.fromShortName("inlineXML");

    DocumentReaderAndWriter<CoreMap> tmp = readerAndWriter;
    readerAndWriter = new PlainTextDocumentReaderAndWriter<CoreMap>();
    readerAndWriter.init(classif.flags);

    StringBuilder sb = new StringBuilder();
    sb.append(((PlainTextDocumentReaderAndWriter<CoreMap>) readerAndWriter).getAnswers(sentence, outFormat, true));
    return sb.toString();
  }
}

class MyWord implements HasWord {

    private String wd = null;

    public MyWord(String wd) {
        this.wd = wd;
    }

    public String word() {
        return wd;
    }

    public void setWord(String string) {
        this.wd = string;
    }

}