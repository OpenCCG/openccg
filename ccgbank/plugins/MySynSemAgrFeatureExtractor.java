
package plugins;

import opennlp.ccg.synsem.*;
import opennlp.ccg.perceptron.*;
import opennlp.ccg.hylo.*;

public class MySynSemAgrFeatureExtractor extends ComposedFeatureExtractor
{
    public MySynSemAgrFeatureExtractor() {
        super(new MySynSemFeatureExtractor(), new EnglishAgreementExtractor());
    }
}