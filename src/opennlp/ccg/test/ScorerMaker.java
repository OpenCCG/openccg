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
package opennlp.ccg.test;

import opennlp.ccg.synsem.SignScorer;

import java.io.*;

/**
 * Interface for making custom scorers for use in cross-validation tests with the realizer.
 *
 * @author  Michael White
 * @version $Revision: 1.6 $, $Date: 2007/12/21 05:13:37 $
 */
public interface ScorerMaker {

    /**
     * Sets the context for this scorer maker.
     */
    public void setCVR(CrossValidateRealizer cvr);
    
    /**
     * Prepares a scoring model from the training data. 
     * The data can be accessed by creating a RegressionInfo 
     * object from the given training file.
     * The test data is also made available to optionally 
     * compute perplexity or other measures.
     * The model can be stored in a file in tmpDir, keyed off of foldNum.
     */
    public void prepScorer(File tmpDir, int foldNum, File trainFile, File testFile) throws IOException;
    
    /**
     * Optionally summarizes perplexity or other measures 
     * after all calls to prepScorer.
     */
    public void prepScorersSummary(File tmpDir) throws IOException;
    
    /**
     * Loads a scoring model created from the training data. 
     */
    public SignScorer loadScorer(File tmpDir, int foldNum, File trainFile) throws IOException;
}


