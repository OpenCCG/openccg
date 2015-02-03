package opennlp.ccg.lexicon;

import java.util.List;

import opennlp.ccg.util.Pair;
import opennlp.ccg.util.TrieMap;

public class AssociateChainFactory implements WordFactory {

	/** Trie map for canonical instances. */
	protected TrieMap<Object, AssociateChain> factorChainRoot = new TrieMap<Object, AssociateChain>(
			null);

	/** Creates a surface word with the given interned form. */
	public synchronized Word create(String form) {
		return create(factorChainRoot, Tokenizer.WORD_ASSOCIATE, form);
	}

	/**
	 * Creates a (surface or full) word with the given normalized attribute name
	 * and value. The attribute names Tokenizer.WORD_ATTR, ...,
	 * Tokenizer.SEM_CLASS_ATTR may be used for the form, ..., semantic class.
	 */
	public synchronized Word create(String attr, String val) {
		return create(factorChainRoot, attr, val);
	}

	/**
	 * Creates a (surface or full) word from the given normalized factors.
	 * Returns null if no non-null vals.
	 */
	public synchronized Word create(String form, String pitchAccent,
			List<Pair<String, String>> attrValPairs, String stem, String POS, String supertag,
			String semClass) {
		// adds non-null vals from the root, in a rough specificity order
		TrieMap<Object, AssociateChain> currentNode = factorChainRoot;
		if (POS != null)
			currentNode = findChild(currentNode, Tokenizer.FUNCTIONS_ASSOCIATE, POS);
		if (supertag != null)
			currentNode = findChild(currentNode, Tokenizer.SUPERTAG_ASSOCIATE, supertag);
		if (semClass != null)
			currentNode = findChild(currentNode, Tokenizer.ENTITY_CLASS_ASSOCIATE, semClass);
		if (stem != null)
			currentNode = findChild(currentNode, Tokenizer.TERM_ASSOCIATE, stem);
		if (form != null)
			currentNode = findChild(currentNode, Tokenizer.WORD_ASSOCIATE, form);
		if (pitchAccent != null)
			currentNode = findChild(currentNode, Tokenizer.TONE_ASSOCIATE, pitchAccent);
		if (attrValPairs != null) {
			for (int i = 0; i < attrValPairs.size(); i++) {
				Pair<String, String> p = attrValPairs.get(i);
				String attr = p.a;
				String val = p.b;
				currentNode = findChild(currentNode, attr, val);
			}
		}
		return currentNode.data;
	}

	/**
	 * Creates a word from the given node, adding the given interned attr and
	 * non-null val.
	 */
	protected Word create(TrieMap<Object, AssociateChain> currentNode, String attr, String val) {
		TrieMap<Object, AssociateChain> child = findChild(currentNode, attr, val);
		return child.data;
	}

	/** Gets or makes a child node from the given node. */
	protected TrieMap<Object, AssociateChain> findChild(
			TrieMap<Object, AssociateChain> currentNode, String attr, String val) {
		Object key = FactorKey.getKey(attr, val);
		TrieMap<Object, AssociateChain> child = currentNode.findChild(key);
		if (child.data == null) {
			AssociateChain parent = currentNode.data;
			child.data = new AssociateChain(key, parent);
		}
		return child;
	}
}
