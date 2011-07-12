package org.protege.owl.diff.present.algorithms;

import java.util.HashSet;
import java.util.Set;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchedAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;

public class IdentifyAnnotationRefactored extends AbstractAnalyzerAlgorithm {
	private OWLDataFactory factory;
	private Changes changes;

	public IdentifyAnnotationRefactored() {
		setPriority(IdentifyChangedAnnotation.IDENTIFY_CHANGED_ANNOTATION_PRIORITY + 1);
	}
	
	public void initialise(Engine e) {
		factory = e.getOWLDataFactory();
		changes = e.getChanges();
	}

	public void apply() {
		for (EntityBasedDiff diff : changes.getEntityBasedDiffs()) {
			if (diff.getSourceEntity() != null && diff.getTargetEntity() != null) {
				apply(diff);
			}
		}
	}
	
	private void apply(EntityBasedDiff diff) {
		Set<MatchedAxiom> possibles = new HashSet<MatchedAxiom>();
		for (MatchedAxiom deleted : diff.getAxiomMatches()) {
			if (isCandidate(diff, deleted)) {
				possibles.add(deleted);
			}
		}
		for (MatchedAxiom deleted : possibles) {
			OWLEntity targetEntity = diff.getTargetEntity();
			OWLAnnotationAssertionAxiom sourceAxiom = (OWLAnnotationAssertionAxiom) deleted.getSourceAxiom();
			OWLAnnotationAssertionAxiom targetAxiom = factory.getOWLAnnotationAssertionAxiom(targetEntity.getIRI(), sourceAxiom.getAnnotation());
			MatchedAxiom added = new MatchedAxiom(null, targetAxiom, MatchedAxiom.AXIOM_ADDED);
			if (diff.getAxiomMatches().contains(added)) {
				changes.removeMatch(deleted);
				changes.removeMatch(added);
			}
		}
	
	}
	
	private boolean isCandidate(EntityBasedDiff diff, MatchedAxiom match) {
		return match.getDescription().equals(MatchedAxiom.AXIOM_DELETED)
		         && match.getSourceAxiom() instanceof OWLAnnotationAssertionAxiom
		         && ((OWLAnnotationAssertionAxiom) match.getSourceAxiom()).getSubject().equals(diff.getSourceEntity().getIRI());
	}
}
