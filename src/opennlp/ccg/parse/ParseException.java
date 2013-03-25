///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002-9 Jason Baldridge, Gann Bierner and Michael White
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

package opennlp.ccg.parse;

/**
 * Any exception having to do with reading the lexicon or rules, etc.
 * 
 * @author Gann Bierner
 * @version $Revision: 1.3 $, $Date: 2009/12/20 18:54:41 $
 */
public class ParseException extends Exception {
	
	private static final long serialVersionUID = 1L;

	/** Time limit exceeded message. */
	public static final String TIME_LIMIT_EXCEEDED = "Time limit exceeded";
	
	/** Edge limit exceeded message. */
	public static final String EDGE_LIMIT_EXCEEDED = "Edge limit exceeded";

	/**
	 * Class constructor
	 * 
	 * @param s
	 *            the error message
	 */
	public ParseException(String s) {
		super(s); 
	}

	public String toString() { return getMessage(); }
}
