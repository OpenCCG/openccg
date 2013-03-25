
package plugins;

import opennlp.ccg.synsem.*;
import opennlp.ccg.perceptron.*;
import opennlp.ccg.hylo.*;

public class MySynAgrFeatureExtractor extends ComposedFeatureExtractor
{
    public MySynAgrFeatureExtractor() {
        super(new SyntacticFeatureExtractor(), new EnglishAgreementExtractor());
    }
}