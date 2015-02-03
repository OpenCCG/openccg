package opennlp.ccg.lexicon;

/**
 * An association between structures of different strata/layers. In the
 * morphological stratum/layer, there is a muster, which is composed of a form
 * and a tone or a caps. In the lexicogrammatical stratum/layer, there is a
 * struct, which is composed of grammatical functions, grammatical features, and
 * a lexical term. In the rhetoricosemantic stratum/layer, there is a
 * rhetoricosemantic device, which is composed of a named entity and/or a named
 * entity class. Each structure of the associations is an associate. 
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
