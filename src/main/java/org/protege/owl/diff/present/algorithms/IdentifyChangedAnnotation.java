package org.protege.owl.diff.present.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchDescription;
import org.protege.owl.diff.present.MatchedAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

public class IdentifyChangedAnnotation extends AbstractAnalyzerAlgorithm {
	public static final MatchDescription CHANGED_ANNOTATION = new MatchDescription("Annotation Changed");
	
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
		Map<OWLAnnotationProperty, Set<MatchedAxiom>> sourceAxioms = new HashMap<OWLAnnotationProperty, Set<MatchedAxiom>>();
		Map<OWLAnnotationProperty, Set<MatchedAxiom>> targetAxioms = new HashMap<OWLAnnotationProperty, Set<MatchedAxiom>>();
		
		for (MatchedAxiom correspondence : diff.getAxiomMatches()) {
			if (correspondence.getDescription().equals(MatchedAxiom.AXIOM_ADDED) &&
					correspondence.getTargetAxiom() instanceof OWLAnnotationAssertionAxiom) {
				addAnnotationAssertionMatch(targetAxioms, correspondence, (OWLAnnotationAssertionAxiom) correspondence.getTargetAxiom());
			}
			if (correspondence.getDescription().equals(MatchedAxiom.AXIOM_DELETED) &&
					correspondence.getSourceAxiom() instanceof OWLAnnotationAssertionAxiom) {
				addAnnotationAssertionMatch(sourceAxioms, correspondence, (OWLAnnotationAssertionAxiom) correspondence.getSourceAxiom());
			}
		}
		for (OWLAnnotationProperty p : sourceAxioms.keySet()) {
			Set<MatchedAxiom> sourceMatches = sourceAxioms.get(p);
			Set<MatchedAxiom> targetMatches = targetAxioms.get(p);
			if (sourceMatches != null && sourceMatches.size() == 1 
					&& targetMatches != null && targetMatches.size() == 1) {
				MatchedAxiom deleted = sourceMatches.iterator().next();
				MatchedAxiom added   = targetMatches.iterator().next();
				MatchedAxiom changed = new MatchedAxiom(deleted.getSourceAxiom(), added.getTargetAxiom(), CHANGED_ANNOTATION);
				changes.addMatch(changed);
				changes.removeMatch(deleted);
				changes.removeMatch(added);
			}
		}
	}

	
	private void addAnnotationAssertionMatch(Map<OWLAnnotationProperty, Set<MatchedAxiom>> map, 
											 MatchedAxiom match,
											 OWLAnnotationAssertionAxiom axiom) {
		OWLAnnotationProperty p = axiom.getProperty();
		Set<MatchedAxiom> matches = map.get(p);
		if (matches == null) {
			matches = new HashSet<MatchedAxiom>();
			map.put(p, matches);
		}
		matches.add(match);
	}
}
