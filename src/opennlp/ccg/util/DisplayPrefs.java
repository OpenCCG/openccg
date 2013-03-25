///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2006 Ben Wing
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

import java.util.prefs.Preferences;

import opennlp.ccg.TextCCG;

/**
 * Simple class for holding preferences for converting an object to a
 * string or other displayable representation.
 *
 * @author  Ben Wing
 * @version $Revision: 1.5 $, $Date: 2007/06/22 01:52:21 $
 */
public class DisplayPrefs {

    /* Whether to show feature info along with each nonterminal */
    public boolean showFeats = false; 
    /* Whether to show semantic info (logical forms) */
    public boolean showSem = false;
    /* Which features to show. */
    public String featsToShow = "";
    
    /** Constructor sets initial prefs from current user prefs. */
    public DisplayPrefs() {
    	Preferences prefs = Preferences.userNodeForPackage(TextCCG.class);
    	showFeats = prefs.getBoolean(TextCCG.SHOW_FEATURES, false);
    	showSem = prefs.getBoolean(TextCCG.SHOW_SEMANTICS, false);
    	featsToShow = prefs.get(TextCCG.FEATURES_TO_SHOW, "");
    }
}