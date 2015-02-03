package opennlp.ccg.lexicon;

import java.io.IOException;
import java.net.URL;

import opennlp.ccg.util.XmlScanner;

import org.jdom.Element;

/**
 * A loader of lexica
 * 
 * @author Daniel Couto-Vale
 */
public class LexiconLoader extends XmlScanner {

	/**
	 * Empty lexicon builder
	 */
	private final static LexiconBuilder empty = new LexiconBuilderStd();

	/**
	 * Current lexicon builder
	 */
	private LexiconBuilder builder;

	/**
	 * Makes a lexicon builder based on a url
	 * 
	 * @param url the url of the lexicon builder file
	 * @return the lexicon builder
	 */
	public final LexiconObject loadLexicon(URL url) {
		builder = empty;
		try {
			parse(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.buildLexicon();
	}

	@Override
	public final void handleRoot(Element element) {
		if (element.getName().equals("lexicon")) {
			builder = new LexiconBuilderStd();
		}
	}

	@Override
	public final void handleElement(Element element) {
		builder.makeItem(element);
	}
}
