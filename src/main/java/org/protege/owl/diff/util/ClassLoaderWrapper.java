package org.protege.owl.diff.util;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public interface ClassLoaderWrapper {
	Enumeration<URL> getResources(String name) throws IOException;
	
	Class<?> loadClass(String name) throws ClassNotFoundException;
}
