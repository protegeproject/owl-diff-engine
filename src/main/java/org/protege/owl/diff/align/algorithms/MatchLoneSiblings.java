package org.protege.owl.diff.align.algorithms;

import java.util.Set;

import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.align.util.PrioritizedComparator;
import org.semanticweb.owlapi.model.OWLClass;

public class MatchLoneSiblings extends AbstractSiblingMatch {
	
	protected void checkSiblings(Set<OWLClass> unmatchedSourceSiblings, Set<OWLClass> unmatchedTargetSiblings) {
		if (unmatchedSourceSiblings.size() == 1 && unmatchedTargetSiblings.size() == 1) {
			getOwlDiffMap().addMatch(unmatchedSourceSiblings.iterator().next(), unmatchedTargetSiblings.iterator().next(),
					                 "Aligned source and target entities because they were the only unmatching children of matching parents.");
		}
	}

	/*
	 * This is somewhat expensive to run early but I think I trust the results.
	 */
	public int getPriority() {
		return PrioritizedComparator.MIN_PRIORITY + 2;
	}
	
    public AlignmentAggressiveness getAggressiveness() {
    	return AlignmentAggressiveness.AGGRESSIVE_SEARCH;
    }

	public String getAlgorithmName() {
		return "Match Lone Siblings";
	}

}
