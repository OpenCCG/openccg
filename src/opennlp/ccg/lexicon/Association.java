package opennlp.ccg.lexicon;

/**
 * An association between structures of different strata/layers. In the morphological stratum/layer,
 * which is either graphological or phonological, there is a muster, which is either a form or a set
 * of form features that can be recognized in a character sequence. In the lexicogrammatical stratum
 * /layer, there is a struct, which contains grammatical functions (POS), grammatical features, and
 * a lexical term/item. In the rhetoricosemantic stratum/layer, there is a rhetoricosemantic device,
 * which is either a named entity class or a named entity.
 * 
 * @author Daniel Couto-Vale
 */
public interface Association {

	/**
	 * The morhpological muster
	 * 
	 * @return the morphological muster
	 */
	public Muster getMuster();

	/**
	 * The lexicogrammatical struct
	 * 
	 * @return the lexicogrammatical struct
	 */
	public Struct getStruct();

	/**
	 * The rhetoricosemantic device
	 * 
	 * @return the rhetoricosemantic device
	 */
	public Device getDevice();

}
