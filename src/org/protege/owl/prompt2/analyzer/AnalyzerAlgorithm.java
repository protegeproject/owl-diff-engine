package org.protege.owl.prompt2.analyzer;

import java.util.Properties;

public interface AnalyzerAlgorithm {
    
    void initialise(Properties parameters);

    void apply(EntityBasedDiff diff);
    
    int getPriority();
}
