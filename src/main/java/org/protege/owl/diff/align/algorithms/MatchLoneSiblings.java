package org.protege.owl.diff.align.algorithms;

import java.util.Set;

import org.protege.owl.diff.align.util.AlignmentAlgorithmComparator;
import org.semanticweb.owlapi.model.OWLClass;

public class MatchLoneSiblings extends AbstractSiblingMatch {
	
	protected void checkSiblings(Set<OWLClass> unmatchedSourceSiblings, Set<OWLClass> unmatchedTargetSiblings) {
		if (unmatchedSourceSiblings.size() == 1 && unmatchedTargetSiblings.size() == 1) {
			getOwlDiffMap().addMatch(unmatchedSourceSiblings.iterator().next(), unmatchedTargetSiblings.iterator().next());
		}
	}

	/*
	 * This is somewhat expensive to run early but I think I trust the results.
	 */
	public int getPriority() {
		return AlignmentAlgorithmComparator.MIN_PRIORITY + 2;
	}

	public String getAlgorithmName() {
		return "Match Lone Siblings";
	}

}
