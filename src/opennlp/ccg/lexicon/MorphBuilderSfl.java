////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2015         Daniel Couto-Vale (RWTH Aachen) 
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
////////////////////////////////////////////////////////////////////////////////////////////////////
package opennlp.ccg.lexicon;

import java.util.List;

import opennlp.ccg.grammar.Grammar;
import opennlp.ccg.hylo.HyloHelper;
import opennlp.ccg.synsem.LF;
import opennlp.ccg.unify.FeatureStructure;
import opennlp.ccg.unify.GFeatStruc;

import org.jdom.Element;

/**
 * A morph builder that reads in a series of realizational chains (realizational associations) as
 * well as a series of feature bundles. The naming convention comes from Systemic Functional
 * Linguistics (SFL).
 *
 * @author Daniel Couto-Vale
 * @version
 */
public class MorphBuilderSfl implements MorphBuilder {

	/**
	 * An empty array of stings
	 */
	private final static String[] empty = new String[0];

	/**
	 * The morph item to build
	 */
	private final Morph morph = new Morph();

	@Override
	public void makeItem(Element element) {
		if (element.getName().equals("Chain") || element.getName().equals("Association")) {
			try {
				makeMorphItem(element);
			} catch (RuntimeException e) {
				String form = element.getAttributeValue("form");
				System.err.println("Skipping Association " + form);
				System.err.println(e.toString());
			}
		} else if (element.getName().equals("FeatureBundle")) {
			try {
				makeMacroItem(element);
			} catch (RuntimeException e) {
				String name = element.getAttributeValue("name");
				System.err.println("Skipping FeatureBundle " + name);
				System.err.println(e.toString());
			}
		} else {
			System.err.println("Skipping " + element.getName() + " item");
		}
	}

	@Override
	public void makeMorphItem(Element element) {
		String modeString = element.getAttributeValue("mode");
		boolean modal = "graphic".equals(modeString) || "phonetic".equals(modeString);
		String form = element.getAttributeValue("form");
		Word tokenizedWord = Grammar.theGrammar.lexicon.tokenizer.parseToken(form, modal);
		Word surfaceWord = Word.createSurfaceWord(tokenizedWord);
		String term = element.getAttributeValue("term");
		if (term == null) {
			term = surfaceWord.getForm();
		}
		String functions = element.getAttributeValue("functions");
		String supertag = null;
		String entityClass = element.getAttributeValue("entity-class");
		Word word = Word.createFullWord(surfaceWord, term, functions, supertag, entityClass);
		String featuresString = element.getAttributeValue("features");
		String[] features = empty;
		if (featuresString != null) {
			features = featuresString.split("\\s+");
		}
		// FIXME inform what is excluded
		String excludedString = element.getAttributeValue("excluded");
		String[] excluded = empty;
		if (excludedString != null) {
			excluded = excludedString.split("\\s+");
		}
		// FIXME use more transparent nomenclature
		Word coartIndexingWord = null;
		if (modal) {
			String indexAttribute = form.substring(0, form.indexOf("-"));
			String indexValue = surfaceWord.getVal(indexAttribute);
			coartIndexingWord = Word.createWord(indexAttribute, indexValue);
		}
		morph.getMorphItems().add(new MorphItem(surfaceWord, word, coartIndexingWord, features,
				excluded, modal));
	}

	@Override
	public void makeMacroItem(Element element) {
		String name = element.getAttributeValue("name");
		@SuppressWarnings("unchecked")
		List<Element> featuresElements = element.getChildren("Features");
		FeatureStructure[] featuress = new FeatureStructure[featuresElements.size()];
		for (int i = 0; i < featuress.length; i++) {
			featuress[i] = new GFeatStruc(featuresElements.get(i));
		}
		Element discourseElement = element.getChild("Discourse");
		LF[] entities;
		if (discourseElement == null) {
			entities = new LF[0];
		} else {
			@SuppressWarnings("unchecked")
			List<Element> entityElements = discourseElement.getChildren();
			entities = new LF[entityElements.size()];
			for (int i = 0; i < entityElements.size(); i++) {
				entities[i] = HyloHelper.getInstance().getLF(entityElements.get(i));
			}
		}
		morph.getMacroItems().add(new MacroItem(name, featuress, entities));
	}

	@Override
	public Morph buildMorph() {
		return morph;
	}

}
