package org.protege.owl.diff.conf;

import java.io.IOException;

import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.present.PresentationAlgorithm;
import org.protege.owl.diff.util.Util;

public class DefaultConfiguration extends Configuration {
	
	public DefaultConfiguration() throws IOException, InstantiationException, IllegalAccessException {
		this(AlignmentAggressiveness.MODERATE);
	}

	public DefaultConfiguration(AlignmentAggressiveness requestedEffort) throws IOException, InstantiationException, IllegalAccessException {
		ClassLoader cl = getClass().getClassLoader();
		for (Class<? extends AlignmentAlgorithm> algorithmClass : Util.createDeclaredAlignmentAlgorithms(cl)) {
			AlignmentAlgorithm algorithm = algorithmClass.newInstance();
			if (!algorithm.isCustom() && algorithm.getAggressiveness().compareTo(requestedEffort) <= 0) {
				addAlignmentAlgorithm(algorithmClass);
			}
		}
		for (Class<? extends PresentationAlgorithm> presentationClass : Util.createDeclaredPresentationAlgorithms(cl)) {
			addPresentationAlgorithm(presentationClass);
		}
	}
}
