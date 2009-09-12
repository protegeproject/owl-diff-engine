package org.protege.owl.prompt2.analyzer.util;

import java.util.Comparator;

import org.protege.owl.prompt2.analyzer.AnalyzerAlgorithm;

public class AnalyzerAlgorithmComparator implements Comparator<AnalyzerAlgorithm> {
    public static final int MIN_PRIORITY = 1;
    public static final int DEFAULT_PRIORITY = 5;
    public static final int MAX_PRIORITY = 10;

    /*
     * ordering is the inverse of the obvious so that high priority algorithms come first.
     */
    public int compare(AnalyzerAlgorithm aa1, AnalyzerAlgorithm aa2) {
        int priority1 = getPriority(aa1);
        int priority2 = getPriority(aa2);
        if (priority1 > priority2) {
            return -1;
        }
        else if (priority1 < priority2) {
            return 1;
        }
        else {
            return 0;
        }
    }
    
    private int getPriority(AnalyzerAlgorithm aa) {
        int priority = aa.getPriority();
        if (priority < MIN_PRIORITY) {
            priority = MIN_PRIORITY;
        }
        if (priority > MAX_PRIORITY) {
            priority = MAX_PRIORITY;
        }
        return priority;
    }

}
