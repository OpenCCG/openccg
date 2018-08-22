///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2018 Reid Fu
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

package opennlp.ccg.realize.lstm_hypertagger;
import java.io.IOException;
import java.util.logging.*;

import opennlp.ccg.realize.feat_extract.LogicalForm;
import opennlp.ccg.realize.linearizer.LinConfig;

public class LSTMHypertaggerLogger {
	public static Logger LOGGER = Logger.getLogger(LSTMHypertaggerLogger.class.getName());
	static {
		try {
			LOGGER.setLevel(Level.INFO);
			LOGGER.setUseParentHandlers(false);
			Handler fh = new FileHandler("/home/reid/projects/research/ccg/openccg/hypertagger_logs/lstm_hypertagger.log");
			LOGGER.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void logLinConfig(LinConfig config) {
		LOGGER.log(Level.INFO, "LinConfig: " + config.toString());
	}
	public static void logLogicalForm(LogicalForm lf) {
		LOGGER.log(Level.INFO, "LogicalForm object\n"
				+ "Word ID's: " + lf.getWordIds().toString() + "\n"
				+ "Roots: " + lf.getHead().getChildList().toString());
	}
}
