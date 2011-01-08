package org.protege.owl.diff;

import org.apache.log4j.Logger;

public class JunitUtilities {
    private static Logger logger = Logger.getLogger(JunitUtilities.class);
    
    public static final String PROJECTS_DIRECTORY="src/test/resources/";

	public static void printDivider() {
        logger.info(" ------------------------------------------------------- ");
    }
}
