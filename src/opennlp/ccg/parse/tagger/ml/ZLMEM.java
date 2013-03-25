///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2009 Dennis N. Mehay
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
package opennlp.ccg.parse.tagger.ml;
import java.io.File;
import java.util.Collection;
import opennlp.ccg.util.Pair;
import opennlp.ccg.parse.tagger.Constants;

/**
 * Decorates ZLMaxentModel, making it a MaxentModel.
 * 
 * @author Dennis N. Mehay
 * @version $Revision: 1.2 $, $Date: 2010/09/26 05:50:15 $
 */
public class ZLMEM extends ZLMaxentModel implements MaxentModel {
	
    public ZLMEM(File model) {
        super(model);
    }
    
    /**
     * @param context: A collection of String,Double pairs, representing the contextual input
     * features and their activations.
     * @return a double[] which represents a probability distribution over output classes, each
     * retrievable by its index with getOutcome(index);
     */
    public double[] eval(Collection<Pair<String, Double>> context) {
        // Have to turn a collection of pairs into a String[] of feature:activation Strings.
        // Sloppy and inefficient.  
        // TODO: A better solution would be to refactor ZLMaxentModel (DNM)
        String[] inpt = new String[context.size()];
        int index = -1;
        for(Pair<String,Double> inp : context) {
            inpt[++index] = inp.a + ":" + inp.b;
        }
        
        return super.eval(context, true, Constants.Domain.PROB);
    }
}
