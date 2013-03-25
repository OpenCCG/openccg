
package plugins;

import opennlp.ccg.synsem.*;
import opennlp.ccg.perceptron.*;
import opennlp.ccg.hylo.*;

public class MySynSemFeatureExtractor extends ComposedFeatureExtractor
{
    public MySynSemFeatureExtractor() {
        super(new SyntacticFeatureExtractor(), new LexDepFeatureExtractor());
    }
}