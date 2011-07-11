package org.protege.owl.diff.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.protege.owl.diff.align.AlignmentAlgorithm;

public class Util {
	public static final Logger LOGGER = Logger.getLogger(Util.class);
	
	
    public static List<AlignmentAlgorithm> createDeclaredAlignmentAlgorithms(ClassLoader cl) throws IOException {
    	List<AlignmentAlgorithm> algorithms = new ArrayList<AlignmentAlgorithm>();
    	BufferedReader factoryReader = new BufferedReader(new InputStreamReader(
    			cl.getResourceAsStream("META-INF/services/org.protege.owl.diff.AlignmentAlgorithms")));
    	try {
    		while (true) {
    			boolean success = false;
    			String name = factoryReader.readLine();
    			if (name == null) {
    				break;
    			}
    			try {
    				name = name.trim();
    				Class<?> clazz = cl.loadClass(name);
    				if (AlignmentAlgorithm.class.isAssignableFrom(clazz)) {
    					algorithms.add((AlignmentAlgorithm) clazz.newInstance());
    					success = true;
    				}
    			}
    			catch (Exception e) {
    				LOGGER.warn("Exception caught", e);
    			}
    			finally {
    				if (!success) {
        				LOGGER.warn("Problems reading alignment algorithm configuration from the " + cl);
    				}
    			}
    		}
    	}
    	finally {
    		factoryReader.close();
    	}
    	return algorithms;
    }
}
