package org.protege.owl.diff.analyzer.algorithms;

import org.protege.owl.diff.analyzer.AnalyzerAlgorithm;
import org.protege.owl.diff.analyzer.util.AnalyzerAlgorithmComparator;

public abstract class AbstractAnalyzerAlgorithm implements AnalyzerAlgorithm {
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
