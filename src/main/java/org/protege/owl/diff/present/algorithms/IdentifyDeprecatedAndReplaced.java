package org.protege.owl.diff.present.algorithms;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.EntityBasedDiff.DiffType;
import org.protege.owl.diff.present.MatchedAxiom;
import org.protege.owl.diff.service.DeprecationDeferralService;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;

public class IdentifyDeprecatedAndReplaced extends AbstractAnalyzerAlgorithm {
	public final static String DEPRECATED_AND_REPLACED_DIFF_TYPE = "Deprecated and replaced";
	private OWLDataFactory factory;
	private OwlDiffMap diffMap;
	private Changes changes;
	private DeprecationDeferralService dds;
	
	public IdentifyDeprecatedAndReplaced() {
		setPriority(IdentifyDeprecatedEntity.IDENTIFY_DEPRECATED_ENTITY_PRIORITY + 1);
	}
	
	public void initialise(Engine e) {
		diffMap = e.getOwlDiffMap();
		changes = e.getChanges();
		factory = diffMap.getOWLDataFactory();
		dds = DeprecationDeferralService.get(e);
	}

	public void apply() {
		for (EntityBasedDiff diff : changes.getEntityBasedDiffs()) {
			apply(diff);
		}
	}
	
	public void apply(EntityBasedDiff diff) {
		OWLEntity created = diff.getTargetEntity();
		if (diff.getDiffType() == DiffType.CREATED 
				&& diffMap.getSourceOntology().containsEntityInSignature(created)
				&& dds.checkDeprecation(created, created)) {
			diff.setDiffTypeDescription(DEPRECATED_AND_REPLACED_DIFF_TYPE);
			OWLAxiom declaration = factory.getOWLDeclarationAxiom(created);
			OWLAxiom deprecated  = factory.getOWLAnnotationAssertionAxiom(created.getIRI(), IdentifyDeprecatedEntity.DEPRECATE_ANNOTATION);
			MatchedAxiom declarationMatch = null;
			MatchedAxiom deprecationMatch = null;
			for (MatchedAxiom match : diff.getAxiomMatches()) {
				if (match.getDescription().equals(MatchedAxiom.AXIOM_ADDED) 
						&& match.getTargetAxiom().equalsIgnoreAnnotations(declaration)) {
					declarationMatch = match;
				}
				else if (match.getDescription().equals(MatchedAxiom.AXIOM_ADDED)
						   && match.getTargetAxiom().equalsIgnoreAnnotations(deprecated)) {
					deprecationMatch = match;
				}
			}
			if (deprecationMatch != null) {
				changes.removeMatch(deprecationMatch);
				changes.addMatch(new MatchedAxiom(null, deprecationMatch.getTargetAxiom(), IdentifyDeprecatedEntity.AXIOM_IS_DEPRECATION));
			}
			if (declarationMatch != null) {
				changes.removeMatch(declarationMatch);
			}
		}
	}
	
}
