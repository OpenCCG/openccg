package opennlp.ccg.lexicon;

import java.util.ArrayList;
import java.util.List;

import opennlp.ccg.util.XmlScanner;

import org.jdom.Element;

/**
 * A loader of lexica
 * 
 * @author Daniel Couto-Vale
 */
public class LexiconLoader extends XmlScanner {

	List<Family> families = new ArrayList<Family>();
	Element distributiveElm = null;
	Element licensingElm = null;
	Element relationSortingElm = null;

	public void handleElement(Element element) {
		// create family
		if (element.getName().equals("family")) {
			try {
				families.add(new Family(element));
			} catch (RuntimeException exc) {
				System.err.println("Skipping family: " + element.getAttributeValue("name"));
				System.err.println(exc.toString());
			}
		}
		// save distributive attributes
		else if (element.getName().equals("distributive-features"))
			distributiveElm = element;
		// save licensing features
		else if (element.getName().equals("licensing-features"))
			licensingElm = element;
		// save relation sort order
		else if (element.getName().equals("relation-sorting"))
			relationSortingElm = element;
	}
}
