package org.protege.owl.diff.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.present.PresentationAlgorithm;

public class Util {
	public static final Logger LOGGER = Logger.getLogger(Util.class);
	
    public static List<AlignmentAlgorithm> createDeclaredAlignmentAlgorithms(ClassLoader cl) throws IOException {
    	return createDeclaredAlignmentAlgorithms(wrapClassLoader(cl));
    }
    
    /*
     * we wrap the class loader so that this utility works with OSGi bundles.  Horrible, eh?
     */
    public static List<AlignmentAlgorithm> createDeclaredAlignmentAlgorithms(ClassLoaderWrapper cl) throws IOException {
    	return createDeclaredAlgorithms(cl, 
    			                        AlignmentAlgorithm.class, 
    								    "META-INF/services/org.protege.owl.diff.AlignmentAlgorithms");
    }
    
    public static List<PresentationAlgorithm> createDeclaredPresentationAlgorithms(ClassLoader cl) throws IOException {
    	return createDeclaredPresentationAlgorithms(wrapClassLoader(cl));
    }
    
    /*
     * we wrap the class loader so that this utility works with OSGi bundles.  Horrible, eh?
     */
    public static List<PresentationAlgorithm> createDeclaredPresentationAlgorithms(ClassLoaderWrapper cl) throws IOException {
    	return createDeclaredAlgorithms(cl, 
    			                        PresentationAlgorithm.class, 
    			                        "META-INF/services/org.protege.owl.diff.PresentationAlgorithms");
    }
    
    private static ClassLoaderWrapper wrapClassLoader(final ClassLoader cl) {
    	return new ClassLoaderWrapper() {
    		
    		public Enumeration<URL> getResources(String name) throws IOException {
    			return cl.getResources(name);
    		}
    		
    		public Class<?> loadClass(String name) throws ClassNotFoundException {
    			return cl.loadClass(name);
    		}
    	};
    }
	
    private static <X> List<X> createDeclaredAlgorithms(ClassLoaderWrapper cl, Class<? extends X> toImplement, String resourceName) throws IOException {
    	List<X> algorithms = new ArrayList<X>();
    	Enumeration<URL> resources = cl.getResources(resourceName);
    	while (resources.hasMoreElements()) {
    		URL url = resources.nextElement();
    		algorithms.addAll(createDeclaredAlignmentAlgorithms(cl, url, toImplement));
    	}
    	return algorithms;
    }
    
    private static <X> List<X> createDeclaredAlignmentAlgorithms(ClassLoaderWrapper cl, URL url, Class<? extends X> toImplement) throws IOException {    
    	List<X> algorithms = new ArrayList<X>();
    	
    	BufferedReader factoryReader = new BufferedReader(new InputStreamReader(url.openStream()));
    	try {
    		while (true) {
    			boolean success = false;
    			String name = factoryReader.readLine();
    			if (name == null) {
    				break;
    			}
    			if (name.startsWith("#") || name.equals("")) {
    				continue;
    			}
    			try {
    				name = name.trim();
    				Class<?> clazz = cl.loadClass(name);
    				if (toImplement.isAssignableFrom(clazz)) {
    					algorithms.add(toImplement.cast(clazz.newInstance()));
    					success = true;
    				}
    			}
    			catch (Exception e) {
    				LOGGER.warn("Exception caught", e);
    			}
    			finally {
    				if (!success) {
        				LOGGER.warn("Problems reading " + toImplement + " instances from " + cl);
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
