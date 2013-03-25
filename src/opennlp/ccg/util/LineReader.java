///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004 University of Edinburgh (Michael White) and David Reitter 
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

import java.io.*;

/**
 * Abstract command-line reader.
 *
 * @author  Michael White
 * @author  David Reitter
 * @version $Revision: 1.2 $, $Date: 2005/10/13 20:33:49 $
 */
abstract public class LineReader {

    /** Creates a default line reader (currently a JLineReader) with the given completion strings. */
    public static LineReader createLineReader(String[] completions) throws IOException {
        return new JLineReader(completions);
    }
    
    /** Sets the command history. */
    abstract public void setCommandHistory(String histStr) throws IOException;
    
    /** Gets the current command history. */
    abstract public String getCommandHistory() throws IOException;
    
    /** Returns an input string, using the given prompt. */
    abstract public String readLine(String prompt) throws IOException;
}
