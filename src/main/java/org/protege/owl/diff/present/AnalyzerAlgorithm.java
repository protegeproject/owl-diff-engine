package org.protege.owl.diff.present;

import java.util.Properties;

import org.protege.owl.diff.align.OwlDiffMap;

public interface AnalyzerAlgorithm {
    
    void initialise(Changes analyzer, Properties parameters);

    void apply();
    
    int getPriority();
    
    void setPriority(int priority);
}
