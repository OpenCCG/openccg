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
 * well as a series of feature bundles. The naming convention comes from previous versions of
 * OpenCCG.
 *
 * @author Daniel Couto-Vale
 * @version
 */
public class MorphBuilderStd implements MorphBuilder {

	private static final String[] empty = new String[0];
	private final Morph morph = new Morph();

	@Override
	public void makeItem(Element element) {
		if (element.getName().equals("entry")) {
			try {
				makeMorphItem(element);
			} catch (RuntimeException e) {
				System.err.println("Skipping morph item: " + element.getAttributeValue("word"));
				System.err.println(e.toString());
			}
		} else if (element.getName().equals("macro")) {
			try {
				makeMacroItem(element);
			} catch (RuntimeException e) {
				System.err.println("Skipping macro item: " + element.getAttributeValue("name"));
				System.err.println(e.toString());
			}
		} else {
			System.err.println("Skipping " + element.getName() + " item");
		}
	}

	@Override
	public void makeMorphItem(Element element) {
		boolean coart = false;
		String coartString = element.getAttributeValue("coart");
		if ("true".equals(coartString)) {
			coart = true;
		}

		String wordString = element.getAttributeValue("word");
		boolean strictFactors = coart; // parse with flag for strict factors
										// with coart items
		Word tokenizedWord = Grammar.theGrammar.lexicon.tokenizer.parseToken(wordString,
				strictFactors);
		Word surfaceWord = Word.createSurfaceWord(tokenizedWord);

		String stem = element.getAttributeValue("stem");
		if (stem == null)
			stem = surfaceWord.getForm();

		String POS = element.getAttributeValue("pos");
		String supertag = null; // supertag comes later from syn cat
		String semClass = element.getAttributeValue("class");

		Word word = Word.createFullWord(surfaceWord, stem, POS, supertag, semClass);

		String macrosString = element.getAttributeValue("macros");
		String[] macros = empty;
		if (macrosString != null) {
			macros = macrosString.split("\\s+");
		}

		String excludedString = element.getAttributeValue("excluded");
		String[] excluded = empty;
		if (excludedString != null) {
			excluded = excludedString.split("\\s+");
		}

		// index on first attr of coarts
		Word coartIndexingWord = null;
		if (coart) {
			String indexAttr = wordString.substring(0, wordString.indexOf("-"));
			String indexVal = surfaceWord.getVal(indexAttr);
			coartIndexingWord = Word.createWord(indexAttr, indexVal);
		}
		morph.getMorphItems().add(
				new MorphItem(surfaceWord, word, coartIndexingWord, macros, excluded, coart));
	}

	@Override
	public void makeMacroItem(Element e) {
		String name = e.getAttributeValue("name");
		if (name == null) {
			name = e.getAttributeValue("n");
		}
		@SuppressWarnings("unchecked")
		List<Element> fsEls = e.getChildren("fs");
		FeatureStructure[] featStrucs = new FeatureStructure[fsEls.size()];
		for (int i = 0; i < featStrucs.length; i++) {
			featStrucs[i] = new GFeatStruc(fsEls.get(i));
		}
		Element lfElt = e.getChild("lf");
		LF[] preds;
		if (lfElt == null) {
			preds = new LF[0];
		} else {
			@SuppressWarnings("unchecked")
			List<Element> predElts = lfElt.getChildren();
			preds = new LF[predElts.size()];
			for (int i = 0; i < predElts.size(); i++) {
				preds[i] = HyloHelper.getLF(predElts.get(i));
			}
		}
		morph.getMacroItems().add(new MacroItem(name, featStrucs, preds));
	}

	@Override
	public Morph buildMorph() {
		return morph;
	}

}
