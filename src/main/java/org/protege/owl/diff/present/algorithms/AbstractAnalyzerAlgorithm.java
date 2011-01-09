package org.protege.owl.diff.present.algorithms;

import org.protege.owl.diff.present.PresentationAlgorithm;
import org.protege.owl.diff.present.util.AnalyzerAlgorithmComparator;

public abstract class AbstractAnalyzerAlgorithm implements PresentationAlgorithm {
	private int priority = AnalyzerAlgorithmComparator.DEFAULT_PRIORITY;

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

}
