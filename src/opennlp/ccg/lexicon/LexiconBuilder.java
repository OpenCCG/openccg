package opennlp.ccg.lexicon;

import org.jdom.Element;

/**
 * A builder of lexicon objects.
 * 
 * @author Daniel Couto-Vale
 */
public interface LexiconBuilder {

	/**
	 * Makes an item out of an element
	 * 
	 * @param element the element to make an item out of
	 */
	public void makeItem(Element element);

	/**
	 * The lexicon
	 * 
	 * @return the lexicon
	 */
	public LexiconObject buildLexicon();

	public void makeRelationSorting(Element element);
	
	public void makeLicensingFeatures(Element element);
	
	public void makeDistributiveFeatures(Element element);
	
	public void makeFamilyItem(Element element);
}
