package org.protege.owl.diff.analyzer;

import java.util.Properties;

import org.protege.owl.diff.raw.OwlDiffMap;

public interface AnalyzerAlgorithm {
    
    void initialise(OwlDiffMap diffMap, Properties parameters);

    void apply(EntityBasedDiff diff);
    
    int getPriority();
}
