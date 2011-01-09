package org.protege.owl.diff.present;

import org.protege.owl.diff.Engine;

public interface PresentationAlgorithm {
    
    void initialise(Engine e);

    void apply();
    
    int getPriority();
    
    void setPriority(int priority);
}
