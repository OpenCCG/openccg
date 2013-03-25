///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2005 University of Edinburgh (Michael White)
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

package opennlp.ccg.util;

import java.util.*;

/**
 * Implements a trie with a data object at each node. Keys are assumed to be
 * canonical, and thus checked using identity (==) rather than equality. For
 * efficient allocation, all children can be added at once.
 * 
 * @author Michael White
 * @version $Revision: 1.6 $, $Date: 2011/03/20 20:11:58 $
 */
public class TrieMap<KeyType,DataType> {

	/** Interface for methods returning an interned key. */
	public interface KeyExtractor<KeyType> {
		/** Returns an interned key. */
		public KeyType getKey();
	}

	/** The data object. */
	public DataType data;

	/**
	 * The mapping to the children. If there is just one child, it's stored in a
	 * pair with its key. Otherwise, an IdentityHashMap is used.
	 */
	private Object childMap = null;

	/** The parent node. */
	private TrieMap<KeyType, DataType> parent = null;

	/** Constructor with data object. */
	public TrieMap(DataType data) {
		this.data = data;
	}

	/** Factory method, for adding empty child nodes. */
	protected TrieMap<KeyType,DataType> createNode() {
		return new TrieMap<KeyType,DataType>(null);
	}
	
	/** Adds the given child with its key. */
	@SuppressWarnings("unchecked")
	public void addChild(KeyType key, TrieMap<KeyType, DataType> child) {
		child.parent = this;
		if (childMap == null) {
			childMap = new Pair<KeyType, TrieMap<KeyType, DataType>>(key, child);
			return;
		}
		Map<KeyType, TrieMap<KeyType, DataType>> map;
		if (childMap instanceof Pair) {
			Pair<KeyType, TrieMap<KeyType, DataType>> pair = (Pair<KeyType, TrieMap<KeyType, DataType>>) childMap;
			map = new IdentityHashMap<KeyType, TrieMap<KeyType, DataType>>();
			map.put(pair.a, pair.b);
			childMap = map;
		} else {
			map = (Map<KeyType, TrieMap<KeyType, DataType>>) childMap;
		}
		map.put(key, child);
	}

	/** Adds the given children with their keys. */
	@SuppressWarnings("unchecked")
	public void addChildren(List<KeyType> keys, List<TrieMap<KeyType, DataType>> childNodes) {
		if (childMap == null && keys.size() == 1) {
			TrieMap<KeyType, DataType> child = childNodes.get(0);
			child.parent = this;
			childMap = new Pair<KeyType, TrieMap<KeyType, DataType>>(keys.get(0), child);
			return;
		}
		Map<KeyType, TrieMap<KeyType, DataType>> map;
		if (childMap == null) {
			map = new IdentityHashMap<KeyType, TrieMap<KeyType, DataType>>(keys.size());
			childMap = map;
		} else if (childMap instanceof Pair) {
			Pair<KeyType, TrieMap<KeyType, DataType>> pair = (Pair<KeyType, TrieMap<KeyType, DataType>>) childMap;
			map = new IdentityHashMap<KeyType, TrieMap<KeyType, DataType>>(keys.size() + 1);
			map.put(pair.a, pair.b);
			childMap = map;
		} else {
			map = (Map<KeyType, TrieMap<KeyType, DataType>>) childMap;
		}
		for (int i = 0; i < keys.size(); i++) {
			TrieMap<KeyType, DataType> child = childNodes.get(i);
			child.parent = this;
			map.put(keys.get(i), child);
		}
	}

	/** Gets the parent node, or null if none. */
	public TrieMap<KeyType, DataType> getParent() {
		return parent;
	}

	/** Gets the child for the given key, or null if none. */
	@SuppressWarnings("unchecked")
	public TrieMap<KeyType, DataType> getChild(KeyType key) {
		if (childMap == null) return null;
		if (childMap instanceof Pair) {
			Pair<KeyType, TrieMap<KeyType, DataType>> pair = (Pair<KeyType, TrieMap<KeyType, DataType>>) childMap;
			if (pair.a == key) return pair.b;
			else return null;
		}
		Map<KeyType, TrieMap<KeyType, DataType>> map = (Map<KeyType, TrieMap<KeyType, DataType>>) childMap;
		return map.get(key);
	}

	/** Gets the child for the given list of keys, or null if none. */
	public TrieMap<KeyType, DataType> getChildFromList(List<KeyType> keys) {
		TrieMap<KeyType, DataType> next = this;
		for (int pos = 0; pos < keys.size(); pos++) {
			next = next.getChild(keys.get(pos));
			if (next == null) return null;
		}
		return next;
	}

	/** Gets the child for the given list of keys extractors, or null if none. */
	public TrieMap<KeyType, DataType> getChildFromLazyList(List<KeyExtractor<KeyType>> keyExtractors) {
		TrieMap<KeyType, DataType> next = this;
		for (int pos = 0; pos < keyExtractors.size(); pos++) {
			next = next.getChild(keyExtractors.get(pos).getKey());
			if (next == null) return null;
		}
		return next;
	}

	/**
	 * Finds the child for the given key, adding one (with a null data object)
	 * if necessary.
	 */
	public TrieMap<KeyType, DataType> findChild(KeyType key) {
		TrieMap<KeyType, DataType> child = getChild(key);
		if (child == null) {
			child = createNode();
			addChild(key, child);
		}
		return child;
	}

	/**
	 * Finds the child for the given list of keys, adding one (with a null data
	 * object) if necessary, along with any necessary intervening parents.
	 */
	public TrieMap<KeyType, DataType> findChildFromList(List<KeyType> keys) {
		TrieMap<KeyType, DataType> next = this;
		for (int pos=0; pos < keys.size(); pos++) {
			KeyType key = keys.get(pos);
			TrieMap<KeyType, DataType> child = next.getChild(key);
			if (child == null) {
				child = createNode();
				next.addChild(key, child);
			}
			next = child;
		}
		return next;
	}

	/**
	 * Finds the child for the given list of keys, adding one (with a null data
	 * object) if necessary, along with any necessary intervening parents.
	 */
	public TrieMap<KeyType, DataType> findChildFromLazyList(List<KeyExtractor<KeyType>> keyExtractors) {
		TrieMap<KeyType, DataType> next = this;
		for (int pos=0; pos < keyExtractors.size(); pos++) {
			KeyType key = keyExtractors.get(pos).getKey();
			TrieMap<KeyType, DataType> child = next.getChild(key);
			if (child == null) {
				child = createNode();
				next.addChild(key, child);
			}
			next = child;
		}
		return next;
	}

	/**
	 * Gets the keys leading to this node. This requires a linear search at each
	 * level.
	 */
	@SuppressWarnings("unchecked")
	public List<KeyType> traceKeys() {
		ArrayList<KeyType> retval = new ArrayList<KeyType>();
		// collect keys up to root
		TrieMap<KeyType, DataType> currentNode = this;
		TrieMap<KeyType, DataType> currentParent = parent;
		while (currentParent != null) {
			if (currentParent.childMap instanceof Pair) {
				Pair<KeyType, TrieMap<KeyType, DataType>> pair = (Pair<KeyType, TrieMap<KeyType, DataType>>) currentParent.childMap;
				retval.add(pair.a);
			} else {
				Map<KeyType, TrieMap<KeyType, DataType>> map = (Map<KeyType, TrieMap<KeyType, DataType>>) currentParent.childMap;
				for (Map.Entry<KeyType, TrieMap<KeyType, DataType>> entry : map.entrySet()) {
					if (entry.getValue() == currentNode) {
						retval.add(entry.getKey());
						break;
					}
				}
			}
			currentNode = currentParent;
			currentParent = currentParent.parent;
		}
		// reverse and return
		Collections.reverse(retval);
		return retval;
	}

	/** Returns this trie map as a string, with indenting. */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		toString(sb, "");
		return sb.toString();
	}

	// appends this trie map as a string, with the given indenting level,
	// to the given string buffer
	@SuppressWarnings("unchecked")
	private void toString(StringBuffer sb, String indent) {
		sb.append("node: " + data);
		if (childMap == null)
			return;
		indent += "  ";
		if (childMap instanceof Pair) {
			Pair<KeyType, TrieMap<KeyType, DataType>> pair = (Pair<KeyType, TrieMap<KeyType, DataType>>) childMap;
			toString(sb, indent, pair.a, pair.b);
		} else {
			Map<KeyType, TrieMap<KeyType, DataType>> map = (Map<KeyType, TrieMap<KeyType, DataType>>) childMap;
			List<KeyType> keys = new ArrayList<KeyType>(map.keySet());
			Comparator<KeyType> toStringComparator = new Comparator<KeyType>() {
				public int compare(KeyType o1, KeyType o2) {
					return o1.toString().compareTo(o2.toString());
				}
			};
			Collections.sort(keys, toStringComparator);
			for (KeyType key : keys) {
				toString(sb, indent, key, map.get(key));
			}
		}
	}

	// appends the given key and child
	private void toString(StringBuffer sb, String indent, Object key, TrieMap<?, ?> child) {
		sb.append("\n").append(indent).append('[').append(key).append("] ");
		child.toString(sb, indent);
	}
}
