package org.protege.owl.diff;


import java.util.logging.Logger;

public class JunitUtilities {
    private static Logger logger = Logger.getLogger(JunitUtilities.class.getName());
    
    public static final String PROJECTS_DIRECTORY="src/test/resources/";

	public static void printDivider() {
        logger.info(" ------------------------------------------------------- ");
    }
}
