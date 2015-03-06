package org.protege.owl.diff.util;


import java.util.logging.Logger;

public class StopWatch {
    private Logger logger;
    private long start;
    private long startOfLastInterval;
    
    public StopWatch(Logger logger) {
        this.logger = logger;
        start = System.currentTimeMillis();
        startOfLastInterval = System.currentTimeMillis();
    }
    
    public void measure() {
        long now = System.currentTimeMillis();
        logger.info("Took " + (now - startOfLastInterval) + "ms");
        startOfLastInterval = now;
    }
    
    public void finish() {
        measure();
        logger.info("Total time = " + (startOfLastInterval - start) + "ms");
    }
    
}
