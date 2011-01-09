package org.protege.owl.diff.present.algorithms;

import org.protege.owl.diff.present.PresentationAlgorithm;
import org.protege.owl.diff.present.util.PresentationAlgorithmComparator;

public abstract class AbstractAnalyzerAlgorithm implements PresentationAlgorithm {
	private int priority = PresentationAlgorithmComparator.DEFAULT_PRIORITY;

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

}
