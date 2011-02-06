package org.protege.owl.diff.present.algorithms;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchDescription;
import org.protege.owl.diff.present.MatchedAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;

public class IdentifyChangedDefinition extends AbstractAnalyzerAlgorithm {
	public static final MatchDescription CHANGED_DEFINITION = new MatchDescription("Definition changed");
	
	private Changes changes;

	public void initialise(Engine e) {
		changes = e.getChanges();
	}

	@Override
	public void apply() {
		for (EntityBasedDiff diff : changes.getEntityBasedDiffs()) {
			match(diff);
		}
	}
	
	private void match(EntityBasedDiff diff) {
		matchAnonymousSuperClasses(diff);
	}
	
	private void matchAnonymousSuperClasses(EntityBasedDiff diff) {
		MatchedAxiom sourceAxiom = null;
		MatchedAxiom targetAxiom = null;
		
		for (MatchedAxiom correspondence : diff.getAxiomMatches()) {
			if (correspondence.getDescription().equals(MatchedAxiom.AXIOM_ADDED) &&
					correspondence.getTargetAxiom() instanceof OWLEquivalentClassesAxiom) {
				if (targetAxiom != null) {
					return; // failed
				}
				targetAxiom = correspondence;
			}
			if (correspondence.getDescription().equals(MatchedAxiom.AXIOM_DELETED) &&
					correspondence.getSourceAxiom() instanceof OWLEquivalentClassesAxiom) {
				if (sourceAxiom != null) {
					return; // failed
				}
				sourceAxiom = correspondence;
			}
		}
		if (sourceAxiom != null && targetAxiom != null) {
			MatchedAxiom newMatch = new MatchedAxiom(sourceAxiom.getSourceAxiom(), targetAxiom.getTargetAxiom(), CHANGED_DEFINITION);
			changes.addMatch(newMatch);
			changes.removeMatch(sourceAxiom);
			changes.removeMatch(targetAxiom);
		}
	}

}
