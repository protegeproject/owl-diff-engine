package org.protege.owl.diff.present.algorithms;

import java.util.HashSet;
import java.util.Set;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchDescription;
import org.protege.owl.diff.present.MatchedAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

public class IdentifyOrphanedAnnotations extends AbstractAnalyzerAlgorithm {
	public static final MatchDescription ORPHANED_ANNOTATION = new MatchDescription("Annotation is orphaned", MatchDescription.SECONDARY_MATCH_PRIORITY);
	private Changes changes;
	private OWLOntology sourceOntology;
	private OWLOntology targetOntology;

	@Override
	public void initialise(Engine e) {
		changes = e.getChanges();
		sourceOntology = e.getOwlDiffMap().getSourceOntology();
		targetOntology = e.getOwlDiffMap().getTargetOntology();
	}

	@Override
	public void apply() {
		for (EntityBasedDiff diff : changes.getEntityBasedDiffs()) {
			apply(diff);
		}
	}
	
	private void apply(EntityBasedDiff diff) {
		OWLEntity sourceEntity = diff.getSourceEntity();
		OWLEntity targetEntity = diff.getTargetEntity();
		if (sourceEntity != null 
				&& targetEntity != null 
				&& !sourceEntity.equals(targetEntity) 
				&& !targetOntology.containsEntityInSignature(sourceEntity.getIRI())) {
			searchForOrphanedAnnotations(diff, sourceEntity, targetEntity);
		}
	}
	
	private void searchForOrphanedAnnotations(EntityBasedDiff diff, OWLEntity sourceEntity, OWLEntity targetEntity) {
		Set<OWLAnnotationAssertionAxiom> orphans = new HashSet<OWLAnnotationAssertionAxiom>();
		for (OWLAnnotationAssertionAxiom axiom : sourceOntology.getAnnotationAssertionAxioms(sourceEntity.getIRI())) {
			if (targetOntology.containsAxiom(axiom)) {
				orphans.add(axiom);
			}
		}
		for (OWLAnnotationAssertionAxiom orphan : orphans) {
			changes.addMatch(new MatchedAxiom(orphan, orphan, ORPHANED_ANNOTATION));
		}
	}

}
