///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2018 Reid Fu
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

package opennlp.ccg.realize.lstm_hypertagger.linearizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import opennlp.ccg.realize.lstm_hypertagger.feat_extract.LogicalForm;
import opennlp.ccg.realize.lstm_hypertagger.feat_extract.WordFeatures;

public class EngLinearizer extends Linearizer {
	private static final String[] CHILD_ORDER = ChildOrders.ENG_CHILD_ORDER;
	private static final String[] BEFORE_PARENT = ChildOrders.ENG_BEFORE_PARENT;
	private static final String[] AFTER_PARENT = ChildOrders.ENG_AFTER_PARENT;
	
	/** English-like linearizer that puts word ID's in following order:<br/>
	 * - Arg0<br/>
	 * - Parent<br/>
	 * - Remaining arguments in order<br/>
	 * - Other relations sorted by number of nodes in subtree */
	public List<String> order(LogicalForm lf, WordFeatures current, Set<String> visited, LinConfig config) {
		List<String> order = addCurrentToOrderAndVisited(current, visited);
		if(order == null)
			return new ArrayList<String>();
		
		addChildrenBeforeParent(lf, order, current, visited, config);
		addChildrenAfterParent(lf, order, current, visited, config);
		addRemainingChildren(lf, order, current, visited, config);
		maybeAddParens(current, config, order);
		return order;
	}
	
	// METHODS FOR ADDING CHILD ORDER CHILDREN
	/** Adds children in CHILD_ORDER, with Det, GenOwn, Arg0, and size 1 or 2 modifiers going in front of parent */
	public void addChildrenBeforeParent(LogicalForm lf, List<String> order, WordFeatures current,
			Set<String> visited, LinConfig config) {
		for(int i = BEFORE_PARENT.length-1; i >= 0; i--) {
			String rel_type = BEFORE_PARENT[i].contains(":") ? BEFORE_PARENT[i].substring(0, BEFORE_PARENT[i].indexOf(":")) : BEFORE_PARENT[i];
			String size_range = BEFORE_PARENT[i].contains(":") ? BEFORE_PARENT[i].substring(BEFORE_PARENT[i].indexOf(":") + 1) : null;
			List<WordFeatures> children = childrenWithinSizeRange(current, rel_type, size_range);
			
			if(children != null) {
				for(WordFeatures child : children) {
					List<String> childOrder = order(lf, child, visited, config);
					order.addAll(0, childOrder);
				}
			}
		}
	}
	public void addChildrenAfterParent(LogicalForm lf, List<String> order, WordFeatures current,
			Set<String> visited, LinConfig config) {
		for(int i = 0; i < AFTER_PARENT.length; i++) {
			String rel_type = AFTER_PARENT[i].contains(":") ? AFTER_PARENT[i].substring(0, AFTER_PARENT[i].indexOf(":")) : AFTER_PARENT[i];
			String size_range = AFTER_PARENT[i].contains(":") ? AFTER_PARENT[i].substring(AFTER_PARENT[i].indexOf(":") + 1) : null;
			List<WordFeatures> children = childrenWithinSizeRange(current, rel_type, size_range);
			
			if(children != null) {
				for(WordFeatures child : children) {
					List<String> childOrder = order(lf, child, visited, config);
					order.addAll(childOrder);
				}
			}
		}
	}
	public List<WordFeatures> childrenWithinSizeRange(WordFeatures current, String rel_type, String size_range) {
		List<WordFeatures> children = current.getChildren().get(rel_type);
		if(children == null)
			return null;
		if(size_range == null)
			return children;
		
		int lowBound = Integer.parseInt(size_range.substring(0, size_range.indexOf("-")));
		int highBound = Integer.parseInt(size_range.substring(size_range.indexOf("-") + 1));
		List<WordFeatures> children2 = new ArrayList<>(children);
		children2.removeIf(feats -> feats.getSubtreeSize() < lowBound || feats.getSubtreeSize() > highBound);
		return children2;
	}
	
	// METHODS FOR ADDING REMAINING CHILDREN
	/** Add remaining related words to order, sorted by subtree size. If subtree size is equal, break tie based on number of characters in predicates. */
	public void addRemainingChildren(LogicalForm lf, List<String> order, WordFeatures current,
			Set<String> visited, LinConfig config) {
		List<List<String>> remChildren = getRemainingChildren(lf, order, current, visited, config);
		Collections.sort(remChildren, new Comparator<List<String>>() {
			@Override
			public int compare(List<String> arg0, List<String> arg1) {
				if(arg0.size() - arg1.size() != 0)
					return arg0.size() - arg1.size();
				int predLen0 = getTotalPredicateLength(lf, arg0);
				int predLen1 = getTotalPredicateLength(lf, arg1);
				return predLen0 - predLen1;
			}
		});
		for(List<String> childOrder : remChildren) {
			order.addAll(childOrder);
		}
	}
	public List<List<String>> getRemainingChildren(LogicalForm lf, List<String> order, WordFeatures current,
			Set<String> visited, LinConfig config) {
		List<List<String>> remChildren = new LinkedList<>();
		List<String> childOrderList = Arrays.asList(CHILD_ORDER);
		
		for(String relation : current.getChildren().keySet()) {
			if(childOrderList.contains(relation))
				continue;
			String rangedRel = relation + ":";
			List<String> orderRangedRel = childOrderList.stream().filter(s -> s.startsWith(rangedRel)).collect(Collectors.toList());
			
			if(orderRangedRel.size() > 0) {
				String size_range = orderRangedRel.get(0);
				size_range = size_range.substring(size_range.indexOf(":") + 1);
				int lowBound = (size_range == null) ? 0 : Integer.parseInt(size_range.substring(0, size_range.indexOf("-")));
				int highBound = (size_range == null) ? 1000 : Integer.parseInt(size_range.substring(size_range.indexOf("-") + 1));
				
				for(WordFeatures childFeats : current.getChildren().get(relation)) {
					if(childFeats.getSubtreeSize() < lowBound || childFeats.getSubtreeSize() > highBound) {
						List<String> childOrder = order(lf, childFeats, visited, config);
						remChildren.add(childOrder);
					}
				}
			} else {
				for(WordFeatures childFeats : current.getChildren().get(relation)) {					
					List<String> childOrder = order(lf, childFeats, visited, config);
					remChildren.add(childOrder);
				}
			}
		}
		return remChildren;
	}
	public int getTotalPredicateLength(LogicalForm lf, List<String> wordIDs) {
		int len = 0;
		for(String wordID : wordIDs) {
			if(wordID.equals("(") || wordID.equals(")"))
				continue;
			WordFeatures feats = lf.getWordFeatures(wordID);
			String pn = feats.getUniqueFeature("PN");
			if(pn != null)
				len += pn.length();
		}
		return len;
	}
}
