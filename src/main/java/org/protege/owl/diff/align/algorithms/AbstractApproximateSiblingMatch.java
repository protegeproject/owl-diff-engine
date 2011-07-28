package org.protege.owl.diff.align.algorithms;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.protege.owl.diff.DifferencePosition;
import org.protege.owl.diff.align.util.CompareNames;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;

public abstract class AbstractApproximateSiblingMatch extends AbstractSiblingMatch {

	
	public abstract String getAlgorithmName();
	public abstract String getBrowserText(OWLClass cls, DifferencePosition position);
	protected abstract String getExplanation();

	
	protected void checkSiblings(Set<OWLClass> unmatchedSourceSiblings,
								 Set<OWLClass> unmatchedTargetSiblings) {
		Map<OWLEntity, OWLEntity> newMatches = new TreeMap<OWLEntity, OWLEntity>();
		for (OWLClass unmatchedSourceSibling : unmatchedSourceSiblings) {
			for (OWLClass unmatchedTargetSibling : unmatchedTargetSiblings) {
				checkMatch(newMatches, unmatchedSourceSibling, unmatchedTargetSibling);
			}
		}
		getOwlDiffMap().addMatchingEntities(newMatches, getExplanation());
	}
	
	private void checkMatch(Map<OWLEntity, OWLEntity> newMatches, OWLClass unmatchedSourceSibling, OWLClass unmatchedTargetSibling) {
		String sourceName = getBrowserText(unmatchedSourceSibling, DifferencePosition.SOURCE);
		String targetName = getBrowserText(unmatchedTargetSibling, DifferencePosition.TARGET);
		if (CompareNames.closeEnough(sourceName, targetName)) {
			newMatches.put(unmatchedSourceSibling, unmatchedTargetSibling);
		}
	}

}
