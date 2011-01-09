package org.protege.owl.diff.align.util;

import java.util.Comparator;

import org.protege.owl.diff.align.AlignmentAlgorithm;


public class DiffAlgorithmComparator implements Comparator<AlignmentAlgorithm> {
    public static final int MIN_PRIORITY = 1;
    public static final int MAX_PRIORITY = 10;
    public static final int DEFAULT_PRIORITY = 5;

    /*
     * ordering is the inverse of the obvious so that high priority algorithms come first.
     */
    public int compare(AlignmentAlgorithm da1, AlignmentAlgorithm da2) {
        int priority1 = getPriority(da1);
        int priority2 = getPriority(da2);
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
    
    private int getPriority(AlignmentAlgorithm da) {
        int priority = da.getPriority();
        if (priority < MIN_PRIORITY) {
            priority = MIN_PRIORITY;
        }
        if (priority > MAX_PRIORITY) {
            priority = MAX_PRIORITY;
        }
        return priority;
    }

}
