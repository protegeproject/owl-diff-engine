package org.protege.owl.diff.align;

import org.protege.owl.diff.Engine;


public interface AlignmentAlgorithm extends Prioritized {
    
    void initialise(Engine e);
    
    void run();
    
    void reset();
    
    AlignmentAggressiveness getAggressiveness();
    
    String getAlgorithmName();

}
