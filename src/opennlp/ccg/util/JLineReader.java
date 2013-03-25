///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004 David Reitter and University of Edinburgh (Michael White) 
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
import java.util.*;

import jline.*;

/**
 * A command-line reader based on JLine.
 *
 * @author  David Reitter
 * @author  Michael White
 * @version $Revision: 1.4 $, $Date: 2009/12/21 03:27:18 $
 */
public class JLineReader extends LineReader {

	// reader for console input
    ConsoleReader reader;

    // command history, buffer    
    History history = null;
    StringWriter histbuf = null;
    
    /** Constructor with completion strings. */
    public JLineReader(String[] completions) throws IOException {
        // init reader
        reader = new ConsoleReader();
		// store commands for 'tab' argument completion 
    	List<SimpleCompletor> completors = new LinkedList<SimpleCompletor>();
    	completors.add(new SimpleCompletor(completions));
        reader.addCompletor(new ArgumentCompletor(completors));
    }
    
    /** Sets the command history. */
    public void setCommandHistory(String histStr) throws IOException {
		// initialize history with max size = 50
		history = new History();
        history.setMaxSize(50);
		if (!histStr.equals("")) {
            histStr = histStr.replaceAll("<br/>", "\n"); // using <br/> to get around XML problem in Java 1.4
			StringReader sreader = new StringReader(histStr);
			history.load(sreader); 
		}
        // set to reader's history
		reader.setHistory(history);
    }
    
    /** Gets the current command history. */
    public String getCommandHistory() throws IOException {
        if (history == null) return "";
        StringBuffer retbuf = new StringBuffer();
        List<?> commands = history.getHistoryList();
        for (Iterator<?> it = commands.iterator(); it.hasNext(); ) {
            retbuf.append(it.next().toString());
            if (it.hasNext()) retbuf.append("<br/>"); // using <br/> to get around XML problem in Java 1.4
        }
        return retbuf.toString();
    }
    
    /** Returns an input string, using the given prompt. */
    public String readLine(String prompt) throws IOException {
	    return reader.readLine(prompt);
    }
}
