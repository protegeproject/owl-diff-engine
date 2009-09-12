package org.protege.owl.prompt2.diff;

import java.util.Properties;

import org.protege.owl.prompt2.diff.impl.OwlDiffMapImpl;


public interface DiffAlgorithm {
    
    void initialise(OwlDiffMap diffMap, Properties parameters);
    
    boolean run();
    
    /** 
     * return the priority of the algorithm as an integer from 0 to 10.
     */
    int getPriority();
    
    String getAlgorithmName();

}
