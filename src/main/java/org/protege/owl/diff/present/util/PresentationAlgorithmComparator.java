package org.protege.owl.diff.present.util;

import java.util.Comparator;

import org.protege.owl.diff.present.PresentationAlgorithm;

public class PresentationAlgorithmComparator implements Comparator<PresentationAlgorithm> {
    public static final int MIN_ALGORITHM_PRIORITY = 1;
    public static final int DEFAULT_ALGORITHM_PRIORITY = 5;
    public static final int MAX_ALGORITHM_PRIORITY = 10;

    /*
     * ordering is the inverse of the obvious so that high priority algorithms come first.
     */
    public int compare(PresentationAlgorithm aa1, PresentationAlgorithm aa2) {
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
    
    private int getPriority(PresentationAlgorithm aa) {
        int priority = aa.getPriority();
        if (priority < MIN_ALGORITHM_PRIORITY) {
            priority = MIN_ALGORITHM_PRIORITY;
        }
        if (priority > MAX_ALGORITHM_PRIORITY) {
            priority = MAX_ALGORITHM_PRIORITY;
        }
        return priority;
    }

}
