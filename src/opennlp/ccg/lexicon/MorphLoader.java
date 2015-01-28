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

import java.io.IOException;
import java.net.URL;

import org.jdom.Element;

import opennlp.ccg.util.XmlScanner;

/**
 * A factory of morph builders.
 * 
 * @author Daniel Couto-Vale
 */
public class MorphLoader extends XmlScanner {

	/**
	 * Empty morph builder
	 */
	private final static MorphBuilder empty = new MorphBuilderStd();

	/**
	 * Current morph builder
	 */
	private MorphBuilder builder;

	/**
	 * Makes a morph builder based on a url
	 * 
	 * @param url the url of the morph builder file
	 * @return the morph builder
	 */
	public final Morph loadMorph(URL url) {
		builder = empty;
		try {
			parse(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.buildMorph();
	}

	@Override
	public final void handleRoot(Element element) {
		if (element.getName().equals("morph")) {
			builder = new MorphBuilderStd();
		} else if (element.getName().equals("Associations") || element.getName().equals("Chains")) {
			builder = new MorphBuilderSfl();
		}
	}

	@Override
	public final void handleElement(Element element) {
		if (builder != empty) {
			builder.makeItem(element);
		}
	}

}
