/*
 * $Id: SRILMNgramModelType.java,v 1.2 2007/05/30 22:53:17 coffeeblack Exp $ 
 */
package opennlp.ccg.ngrams;


/**
 * Used by {@link SRILMNgramModel} to specify the type of language model that
 * should be used.
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @see <a href="http://www.speech.sri.com/projects/srilm/">SRILM</a>
 * @version $LastChangedRevision$
 */
public enum SRILMNgramModelType {
	/*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*
	 * It is very important that the order of these does not      *
	 * change, as {@link SRILMNgramModel#loadLM(int, String, int)}*
	 * relies on the ordinal.                                     *
	 *!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
	
	/**
	 * A &quot;standard&quot; ngram model, of the type normally created by
	 * the SRILM binary <code>ngram-count</code>.
	 */
	STANDARD,
	
	/**
	 * For ngram models based on count LMs. The Google LM format is one of
	 * these.
	 * @see <a href="http://www.ldc.upenn.edu/Catalog/CatalogEntry.jsp?catalogId=LDC2006T13">Web 1T 5-gram Version 1</a>
	 */
	COUNT;
}
