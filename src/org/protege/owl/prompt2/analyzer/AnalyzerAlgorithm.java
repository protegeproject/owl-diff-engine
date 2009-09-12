package org.protege.owl.prompt2.analyzer;

import java.util.Properties;

import org.protege.owl.prompt2.diff.OwlDiffMap;

public interface AnalyzerAlgorithm {
    
    void initialise(OwlDiffMap diffMap, Properties parameters);

    void apply(EntityBasedDiff diff);
    
    int getPriority();
}
