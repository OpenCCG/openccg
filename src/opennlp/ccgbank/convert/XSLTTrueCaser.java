///////////////////////////////////////////////////////////////////////////////
//Copyright (C) 2011 Dennis N. Mehay
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

package opennlp.ccgbank.convert;

/**
 * A class that has a static constructor to create a TrueCaser so that XSLT (which requires such a
 * set-up) can call the TrueCaser.
 *
 * @author      Dennis N. Mehay
 *
 */

import opennlp.ccg.lexicon.TrueCaser;

public class XSLTTrueCaser {
	
	static TrueCaser tc = null;
	
	/**
	 * Static constructor that creates a true-caser. See the TrueCaser.java doc's for more info.
	 */
	public static void init(String pathToTrueCaseList) {
		XSLTTrueCaser.tc = new TrueCaser(pathToTrueCaseList, 0.5);
	}
	
	/** Function invoked from the XSLT transform trueCaser.xsl to true case words in a derivation .*/
	public String trueCase(String theWord, String neClass, String pos,String wordPosition) {
		return tc.trueCase(theWord, true, true);
	}
}