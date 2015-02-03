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

import org.jdom.Element;

/**
 * A builder of morph objects.
 * 
 * @author Daniel Couto-Vale
 */
public interface MorphBuilder {

	/**
	 * Makes an item out of an element.
	 * 
	 * @param element the element
	 */
	public void makeItem(Element element);

	/**
	 * Makes a morph item out of an element.
	 * 
	 * @param element the element
	 */
	public void makeMorphItem(Element element);

	/**
	 * Makes a macro item out of an element.
	 * 
	 * @param element the element
	 */
	public void makeMacroItem(Element element);

	/**
	 * Builds a morph object.
	 * 
	 * @return the morph object
	 */
	public Morph buildMorph();

}
