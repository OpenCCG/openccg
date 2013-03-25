///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004 University of Edinburgh (Michael White)
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
 * A WordWithPitchAccent object is a surface word with an optional pitch accent 
 * but no further attributes.
 *
 * @author      Michael White
 * @version     $Revision: 1.4 $, $Date: 2009/07/17 04:23:30 $
 */
public class WordWithPitchAccent extends SimpleWord {
    
	private static final long serialVersionUID = 1510997962756436949L;
	
	/** The pitch accent. */
    protected String pitchAccent;
    
    /** Returns the pitch accent. */
    public String getPitchAccent() { return pitchAccent; }
    
    /** Constructor. */
    protected WordWithPitchAccent(String form, String pitchAccent) {
        super(form); 
        this.pitchAccent = pitchAccent;
    }
}

