package org.protege.owl.diff.conf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.present.PresentationAlgorithm;

public class Configuration {
	private Collection<Class<? extends AlignmentAlgorithm>> alignmentAlgorithms = new ArrayList<Class<? extends AlignmentAlgorithm>>();
	private Collection<Class<? extends PresentationAlgorithm>> presentationAlgorithms = new ArrayList<Class<? extends PresentationAlgorithm>>();
	private Map<String, String> parameters = new HashMap<String, String>();
	
	public void addAlignmentAlgorithm(Class<? extends AlignmentAlgorithm> cls) {
		alignmentAlgorithms.add(cls);
	}
	
	public void addPresentationAlgorithm(Class<? extends PresentationAlgorithm> cls) {
		presentationAlgorithms.add(cls);
	}
	
	public void put(String name, String value) {
		parameters.put(name, value);
	}
	
	public String get(String name) {
		return parameters.get(name);
	}
	
	public void configure(Engine e) throws InstantiationException, IllegalAccessException {
		AlignmentAlgorithm[] alignmentAlgorithmArray = new AlignmentAlgorithm[alignmentAlgorithms.size()];
		int index = 0;
		for (Class<? extends AlignmentAlgorithm> cls : alignmentAlgorithms) {
			alignmentAlgorithmArray[index++] = cls.newInstance();
		}
		PresentationAlgorithm[] presentationAlgorithmArray = new PresentationAlgorithm[presentationAlgorithms.size()];
		index = 0;
		for (Class<? extends PresentationAlgorithm> cls : presentationAlgorithms) {
			presentationAlgorithmArray[index++] = cls.newInstance();
		}
		e.setAlignmentAlgorithms(alignmentAlgorithmArray);
		e.setPresentationAlgorithms(presentationAlgorithmArray);
		e.setParameters(parameters);
	}
	
}
