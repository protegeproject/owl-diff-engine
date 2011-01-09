package org.protege.owl.diff.present;

import java.util.Properties;

public interface PresentationAlgorithm {
    
    void initialise(Changes analyzer, Properties parameters);

    void apply();
    
    int getPriority();
    
    void setPriority(int priority);
}
