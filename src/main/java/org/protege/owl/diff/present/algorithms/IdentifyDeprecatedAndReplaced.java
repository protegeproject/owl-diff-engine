package org.protege.owl.diff.present.algorithms;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.EntityBasedDiff.DiffType;
import org.protege.owl.diff.present.util.PresentationAlgorithmComparator;
import org.protege.owl.diff.present.MatchDescription;
import org.protege.owl.diff.present.MatchedAxiom;
import org.protege.owl.diff.service.DeprecationDeferralService;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;

public class IdentifyDeprecatedAndReplaced extends AbstractAnalyzerAlgorithm {
	public static final MatchDescription AXIOM_IS_DEPRECATION = new MatchDescription("Deprecated", MatchDescription.PRIMARY_MATCH_PRIORITY);
	
	private OWLDataFactory factory;
	private OwlDiffMap diffMap;
	private Changes changes;
	private DeprecationDeferralService dds;
	
	public IdentifyDeprecatedAndReplaced() {
		setPriority(PresentationAlgorithmComparator.MAX_ALGORITHM_PRIORITY);
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
			diff.setDiffTypeDescription("Deprecated and replaced");
			OWLAxiom declaration = factory.getOWLDeclarationAxiom(created);
			OWLAxiom deprecated  = factory.getOWLAnnotationAssertionAxiom(created.getIRI(), factory.getOWLAnnotation(factory.getOWLDeprecated(), factory.getOWLLiteral(true)));
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
				changes.addMatch(new MatchedAxiom(null, deprecationMatch.getTargetAxiom(), AXIOM_IS_DEPRECATION));
			}
			if (declarationMatch != null) {
				changes.removeMatch(declarationMatch);
			}
		}
	}
	
}
