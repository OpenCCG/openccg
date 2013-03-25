///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002-3 Jason Baldridge, Gann Bierner and 
//                      University of Edinburgh (Michael White)
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

package opennlp.ccg.lexicon;

/**
 * Any exception thrown if something wrong happens in the lexicon.
 *
 * @author      Gann Bierner
 * @author      Michael White
 * @version     $Revision: 1.3 $, $Date: 2005/10/20 17:30:30 $
 */
public class LexException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/** The message. */
    protected String msg;

    /**
     * Constructor with message.
     */    
    public LexException(String s) { msg = s; }

    /** Returns exception message. */
    public String toString() {
        return "Lexicon Exception: " + msg;
    }
}
