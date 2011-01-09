package org.protege.owl.diff.align;

import java.util.Properties;


public interface DiffAlgorithm {
    
    void initialise(OwlDiffMap diffMap, Properties parameters);
    
    void run();
    
    void reset();
    
    /** 
     * return the priority of the algorithm as an integer from 0 to 10.
     */
    int getPriority();
    
    String getAlgorithmName();

}
