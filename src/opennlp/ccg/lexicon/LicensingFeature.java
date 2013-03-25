///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-4 University of Edinburgh (Michael White)
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

import java.util.*;

/**
 * A licensing feature is one which controls the licensing and 
 * instantiation of semantically null or marked categories 
 * in the realizer.  Defaults are handled in Lexicon.loadLicensingFeatures.
 *
 * @author      Michael White
 * @version     $Revision: 1.7 $, $Date: 2009/12/21 03:27:18 $
 */
public class LicensingFeature
{

    /**
     * The name of the licensing feature.
     */
    public final String attr;

    /**
     * The string value of the licensing feature, or null if any value will do.
     */
    public final String val;
    
    /**
     * A list of string values of other features that suffice to license 
     * categories with this feature.
     */
    public final List<String> alsoLicensedBy;
    
    /**
     * A flag indicating whether semantically null categories with the 
     * licensing feature need to be licensed.
     */
    public final boolean licenseEmptyCats;
    
    /**
     * A flag indicating whether initial categories with the 
     * licensing feature are marked and need to be licensed.
     */
    public final boolean licenseMarkedCats;
    
    /**
     * A flag indicating whether semantically empty categories with the 
     * licensing feature should be instantiated.
     */
    public final boolean instantiate;
    
    /**
     * The location of the licensing feature on the category to be licensed.
     * The value must be one of TARGET_ONLY, ARGS_ONLY or BOTH.
     */
    public final byte loc;
    
    /**
     * Location of the feature on the target category only.
     */
    public static final byte TARGET_ONLY = 1;
    
    /**
     * Location of the feature on the argument categories only.
     */
    public static final byte ARGS_ONLY = 2;
    
    /**
     * Location of the feature on either the target category 
     * or the argument categories.
     */
    public static final byte BOTH = 0;
    
    
    /** Constructor. */
    public LicensingFeature(
        String attr, String val, List<String> alsoLicensedBy, 
        boolean licenseEmptyCats, boolean licenseMarkedCats, boolean instantiate, 
        byte loc
    ) 
    {
        this.attr = attr; this.val = val;
        List<String> emptyList = Collections.emptyList();
        this.alsoLicensedBy = (alsoLicensedBy != null) ? alsoLicensedBy : emptyList;
        this.licenseEmptyCats = licenseEmptyCats; 
        this.licenseMarkedCats = licenseMarkedCats; 
        this.instantiate = instantiate; 
        this.loc = loc;
    }
    
    /** Default lex feature. */
    public static final LicensingFeature defaultLexFeature = 
        new LicensingFeature("lex", null, null, true, false, true, BOTH);
    
    /** Simple lex feature, for comparison purposes. */
    public static final LicensingFeature simpleLexFeature = 
        new LicensingFeature("lex", null, null, true, false, false, BOTH);
}
    
