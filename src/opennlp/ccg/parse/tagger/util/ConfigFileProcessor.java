///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010 Dennis N. Mehay
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed inp the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

package opennlp.ccg.parse.tagger.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
//import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dennis N. Mehay
 */
public class ConfigFileProcessor {
    
    
    /** Read in config file as a {@code Map<String,String>}. */
    public static Map<String, String> readInConfig(String configFile) {
    	return readInConfig(configFile, null);
    }

    /**
     * Read in config file as a {@code Map<String,String>}, resolving the given path keys
     * relative to the config file if not absolute.
     */
    public static Map<String, String> readInConfig(String configFile, String pathKeys[]) {
    	Set<String> paths = Collections.emptySet();
    	if (pathKeys != null) paths = new HashSet<String>(Arrays.asList(pathKeys));
        BufferedReader cf = null;
        Map<String, String> opts = new HashMap<String, String>();
        try {
        	File infile = new File(configFile);
        	File parentDir = infile.getParentFile();
            cf = new BufferedReader(new FileReader(infile));
            
            String ln = cf.readLine();
            // map options to values.
            while (ln != null) {
                if (ln.trim().equals("") || ln.trim().startsWith("#")) {
                    ln = cf.readLine();
                    continue;
                }
                String[] parts = ln.trim().split("=");
                String key = parts[0].trim().toLowerCase();
                String val = parts[1].trim();
                // resolve path keys
                if (paths.contains(key)) {
                	File f = new File(parentDir, val);
                	if (!f.exists()) {
                		f = new File(val);
                		if (!f.exists()) throw new FileNotFoundException("Can't resolve filename: " + val);
                	}
                	val = f.getPath();
                }
                opts.put(key, val);
                ln = cf.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigFileProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConfigFileProcessor.class.getName()).log(Level.SEVERE, null, ex);        
        } finally {
            try {
                cf.close();
            } catch (IOException ex) {
                Logger.getLogger(ConfigFileProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return opts;
    }

}
