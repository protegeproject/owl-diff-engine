package org.protege.owl.diff.present.algorithms;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchDescription;
import org.protege.owl.diff.present.MatchedAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class IdentifyChangedSuperclass extends AbstractAnalyzerAlgorithm {
	public static final MatchDescription CHANGED_SUPERCLASS = new MatchDescription("Superclass changed");
	
	private Changes changes;

	public void initialise(Engine e) {
		changes = e.getChanges();
	}

	public void apply() {
		for (EntityBasedDiff diff : changes.getEntityBasedDiffs()) {
			match(diff);
		}
	}
	
	private void match(EntityBasedDiff diff) {
		matchAnonymousSuperClasses(diff, true);
		matchAnonymousSuperClasses(diff, false);
	}
	
	private void matchAnonymousSuperClasses(EntityBasedDiff diff, boolean anonymousOnly) {
		MatchedAxiom sourceAxiom = null;
		MatchedAxiom targetAxiom = null;
		
		for (MatchedAxiom correspondence : diff.getAxiomMatches()) {
			if (correspondence.getDescription().equals(MatchedAxiom.AXIOM_ADDED) &&
					correspondence.getTargetAxiom() instanceof OWLSubClassOfAxiom &&
					(!anonymousOnly ||
							((OWLSubClassOfAxiom) correspondence.getTargetAxiom()).getSuperClass().isAnonymous())) {
				if (targetAxiom != null) {
					return; // failed
				}
				targetAxiom = correspondence;
			}
			if (correspondence.getDescription().equals(MatchedAxiom.AXIOM_DELETED) &&
					correspondence.getSourceAxiom() instanceof OWLSubClassOfAxiom &&
					(!anonymousOnly ||
							((OWLSubClassOfAxiom) correspondence.getSourceAxiom()).getSuperClass().isAnonymous())) {
				if (sourceAxiom != null) {
					return; // failed
				}
				sourceAxiom = correspondence;
			}
		}
		if (sourceAxiom != null && targetAxiom != null) {
			MatchedAxiom newMatch = new MatchedAxiom(sourceAxiom.getSourceAxiom(), targetAxiom.getTargetAxiom(), CHANGED_SUPERCLASS);
			changes.addMatch(newMatch);
			changes.removeMatch(sourceAxiom);
			changes.removeMatch(targetAxiom);
		}
	}

}
