package org.protege.owl.diff.present.algorithms;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchDescription;
import org.protege.owl.diff.present.MatchedAxiom;
import org.protege.owl.diff.present.util.PresentationAlgorithmComparator;
import org.semanticweb.owlapi.model.OWLAxiom;

public class IdentifyAxiomAnnotationChanged extends AbstractAnalyzerAlgorithm {
	public static final MatchDescription AXIOM_ANNOTATION_CHANGED = new MatchDescription("Axiom Annotation Changed");
	
	private Changes changes;
	private Map<OWLAxiom, OWLAxiom> logicalAxiomToAddedAxiomMap = new HashMap<OWLAxiom,OWLAxiom>();

	public IdentifyAxiomAnnotationChanged() {
		setPriority(PresentationAlgorithmComparator.MAX_ALGORITHM_PRIORITY - 1);
	}

	public void initialise(Engine e) {
		changes = e.getChanges();
		for (OWLAxiom axiom : e.getOwlDiffMap().getUnmatchedTargetAxioms()) {
			logicalAxiomToAddedAxiomMap.put(axiom.getAxiomWithoutAnnotations(), axiom);
		}
	}

	public void apply() {
		for (EntityBasedDiff diff : changes.getEntityBasedDiffs()) {
			apply(diff);
		}
	}
	
	public void apply(EntityBasedDiff diff) {
		for (MatchedAxiom removedMatch : new TreeSet<MatchedAxiom>(diff.getAxiomMatches())) {
			if (removedMatch.getDescription().equals(MatchedAxiom.AXIOM_DELETED)) {
				OWLAxiom sourceAxiom = removedMatch.getSourceAxiom();
				OWLAxiom targetAxiom = logicalAxiomToAddedAxiomMap.get(sourceAxiom.getAxiomWithoutAnnotations());
				if (targetAxiom != null) {
					MatchedAxiom addedMatch = new MatchedAxiom(null, targetAxiom, MatchedAxiom.AXIOM_ADDED);
					if (changes.containsMatch(addedMatch)) {
						changes.removeMatch(removedMatch);
						changes.removeMatch(addedMatch);
						changes.addMatch(new MatchedAxiom(sourceAxiom, targetAxiom, AXIOM_ANNOTATION_CHANGED));
					}
				}
			}
		}
	}

}
