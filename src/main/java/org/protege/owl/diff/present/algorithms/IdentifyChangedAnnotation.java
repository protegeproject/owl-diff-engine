package org.protege.owl.diff.present.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.protege.owl.diff.DifferencePosition;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchDescription;
import org.protege.owl.diff.present.MatchedAxiom;
import org.protege.owl.diff.present.util.PresentationAlgorithmComparator;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

public class IdentifyChangedAnnotation extends AbstractAnalyzerAlgorithm {
	public static final int IDENTIFY_CHANGED_ANNOTATION_PRIORITY = 	PresentationAlgorithmComparator.DEFAULT_ALGORITHM_PRIORITY;
	public static final MatchDescription CHANGED_ANNOTATION = new MatchDescription("Annotation Changed");
	
	private Changes changes;

	public IdentifyChangedAnnotation() {
		// TODO Auto-generated constructor stub
	}
	
	public void initialise(Engine e) {
		changes = e.getChanges();
	}

	public void apply() {
		for (EntityBasedDiff diff : changes.getEntityBasedDiffs()) {
			match(diff);
		}
	}
	
	private void match(EntityBasedDiff diff) {
		matchAnnotations(diff);
	}
	
	private void matchAnnotations(EntityBasedDiff diff) {
		Map<OWLAnnotationProperty, Set<MatchedAxiom>> sourceAxioms = new HashMap<OWLAnnotationProperty, Set<MatchedAxiom>>();
		Map<OWLAnnotationProperty, Set<MatchedAxiom>> targetAxioms = new HashMap<OWLAnnotationProperty, Set<MatchedAxiom>>();
		
		for (MatchedAxiom correspondence : diff.getAxiomMatches()) {
			if (isCandidateMatch(diff, correspondence, DifferencePosition.SOURCE)) {
				addAnnotationAssertionMatch(sourceAxioms, correspondence, (OWLAnnotationAssertionAxiom) correspondence.getSourceAxiom());
			}
			if (isCandidateMatch(diff, correspondence, DifferencePosition.TARGET)) {
				addAnnotationAssertionMatch(targetAxioms, correspondence, (OWLAnnotationAssertionAxiom) correspondence.getTargetAxiom());
			}
		}
		for (OWLAnnotationProperty p : sourceAxioms.keySet()) {
			Set<MatchedAxiom> sourceMatches = sourceAxioms.get(p);
			Set<MatchedAxiom> targetMatches = targetAxioms.get(p);
			if (sourceMatches != null && sourceMatches.size() == 1 
					&& targetMatches != null && targetMatches.size() == 1) {
				MatchedAxiom deleted = sourceMatches.iterator().next();
				MatchedAxiom added   = targetMatches.iterator().next();
				changes.removeMatch(deleted);
				changes.removeMatch(added);
				
				MatchedAxiom changed = new MatchedAxiom(deleted.getSourceAxiom(), added.getTargetAxiom(), CHANGED_ANNOTATION);
				changes.addMatch(changed);
			}
		}
	}
	
	private boolean isCandidateMatch(EntityBasedDiff diff, MatchedAxiom match, DifferencePosition position) {
		MatchDescription expectedMatchDescription = (position == DifferencePosition.SOURCE 
				? MatchedAxiom.AXIOM_DELETED : MatchedAxiom.AXIOM_ADDED);
		OWLEntity entity = position.getEntity(diff);
		OWLAxiom axiom = position.getAxiom(match);
		return match.getDescription().equals(expectedMatchDescription)
					&& axiom instanceof OWLAnnotationAssertionAxiom
					&& entity != null
					&& ((OWLAnnotationAssertionAxiom) axiom).getSubject().equals(entity.getIRI());	
	}
	
	private boolean isIdenticalAnnotation(MatchedAxiom deleted, MatchedAxiom added) {
		return ((OWLAnnotationAssertionAxiom) deleted.getSourceAxiom()).getAnnotation()
					.equals(((OWLAnnotationAssertionAxiom) added.getTargetAxiom()).getAnnotation());
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
