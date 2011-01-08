package org.protege.owl.diff.analyzer;

import java.util.Properties;

import org.protege.owl.diff.raw.OwlDiffMap;

public interface AnalyzerAlgorithm {
    
    void initialise(Changes analyzer, Properties parameters);

    void apply();
    
    int getPriority();
    
    void setPriority(int priority);
}
