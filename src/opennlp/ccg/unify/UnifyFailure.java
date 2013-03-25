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

package opennlp.ccg.unify;

/**
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.4 $, $Date: 2009/11/28 03:39:27 $
 */
public class UnifyFailure extends Exception {

	private static final long serialVersionUID = 1L;

    /** Constructor. */
    public UnifyFailure() {}

    /** Constructor with message. */
    public UnifyFailure(String m) { super(m); }

    /** Constructor with two args that failed to unify. */
    public UnifyFailure(String arg1, String arg2) {
    	super("Unable to unify " + arg1 + " with " + arg2 + ".");
    }

    /** Returns exception message. */
    public String toString() {
    	String msg = getMessage();
        return "Unify Failure: " + (msg != null ? msg : "(no message)");
    }
}
