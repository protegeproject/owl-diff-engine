package org.protege.owl.diff.align;

import org.protege.owl.diff.Engine;


public interface AlignmentAlgorithm {
    
    void initialise(Engine e);
    
    void run();
    
    void reset();
    
    /** 
     * return the priority of the algorithm as an integer from 0 to 10.
     */
    int getPriority();
    
    AlignmentAggressiveness getAggressiveness();
    
    String getAlgorithmName();

}
