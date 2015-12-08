package org.protege.owl.diff;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JunitUtilities {
    private static Logger logger = LoggerFactory.getLogger(JunitUtilities.class.getName());
    
    public static final String PROJECTS_DIRECTORY="src/test/resources/";

	public static void printDivider() {
        logger.info(" ------------------------------------------------------- ");
    }
}
