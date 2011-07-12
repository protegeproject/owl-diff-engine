package org.protege.owl.diff.present.algorithms;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchDescription;
import org.protege.owl.diff.present.MatchedAxiom;
import org.protege.owl.diff.present.util.PresentationAlgorithmComparator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;

public class IdentifyDeprecatedEntity extends AbstractAnalyzerAlgorithm {
	public static final int IDENTIFY_DEPRECATED_ENTITY_PRIORITY = PresentationAlgorithmComparator.DEFAULT_ALGORITHM_PRIORITY;
	public static final MatchDescription AXIOM_IS_DEPRECATION = new MatchDescription("Deprecated", MatchDescription.PRIMARY_MATCH_PRIORITY);
	public static final OWLAnnotation DEPRECATE_ANNOTATION = OWLManager.getOWLDataFactory().getOWLAnnotation(
			OWLManager.getOWLDataFactory().getOWLDeprecated(), 
			OWLManager.getOWLDataFactory().getOWLLiteral(true)
	);
	
	private Changes changes;
	
	public IdentifyDeprecatedEntity() {
		setPriority(IDENTIFY_DEPRECATED_ENTITY_PRIORITY);
	}
	
	public void initialise(Engine e) {
		changes = e.getChanges();
	}
	
	public void apply() {
		for (EntityBasedDiff diff : changes.getEntityBasedDiffs()) {
			apply(diff);
		}
	}
	
	public void apply(EntityBasedDiff diff) {
		MatchedAxiom deprecatedMatch = null;
		for (MatchedAxiom match : diff.getAxiomMatches()) {
			if (match.getDescription().equals(MatchedAxiom.AXIOM_ADDED) 
					&& match.getTargetAxiom() instanceof OWLAnnotationAssertionAxiom
					&& ((OWLAnnotationAssertionAxiom) match.getTargetAxiom()).getAnnotation().equals(DEPRECATE_ANNOTATION)) {
				deprecatedMatch = match;
			}
		}
		if (deprecatedMatch != null) {
			diff.setDiffTypeDescription("Deprecated");
			changes.removeMatch(deprecatedMatch);
			changes.addMatch(new MatchedAxiom(null, deprecatedMatch.getTargetAxiom(), AXIOM_IS_DEPRECATION));
		}
	}
}
