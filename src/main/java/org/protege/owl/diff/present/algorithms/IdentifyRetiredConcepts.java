package org.protege.owl.diff.present.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchDescription;
import org.protege.owl.diff.present.MatchedAxiom;
import org.protege.owl.diff.present.util.PresentationAlgorithmComparator;
import org.protege.owl.diff.service.RetirementClassService;

public class IdentifyRetiredConcepts extends AbstractAnalyzerAlgorithm {
    public static final MatchDescription RETIRED = new MatchDescription("Retired", MatchDescription.MIN_SEQUENCE);
    public static final int DEFAULT_IDENTIFY_RETIRED_CONCEPTS_PRIORITY = PresentationAlgorithmComparator.DEFAULT_PRIORITY + 2;

    private Changes changes;
    private RetirementClassService retiredClassService;
    
    public IdentifyRetiredConcepts() {
    	setPriority(DEFAULT_IDENTIFY_RETIRED_CONCEPTS_PRIORITY);
    }
    
    public void initialise(Engine e) {
    	this.changes = e.getChanges();
    	retiredClassService = RetirementClassService.getRetirementClassService(e);
    }	
    
    public void apply() {
    	for (EntityBasedDiff diff : changes.getEntityBasedDiffs()) {
    		apply(diff);
    	}
    }

    private void apply(EntityBasedDiff diff) {
        if (retiredClassService.isDisabled()) {
            return;
        }
        Collection<MatchedAxiom> retiringMatches = new ArrayList<MatchedAxiom>();
        for (MatchedAxiom match : diff.getAxiomMatches()) {
            if (match.isFinal()) {
                continue;
            }
            if (match.getDescription().equals(MatchedAxiom.AXIOM_ADDED) && retiredClassService.isRetirementAxiom(match.getTargetAxiom())) {
            	retiringMatches.add(match);
            }
        }
        for (MatchedAxiom match : retiringMatches) {
            MatchedAxiom newRetired = new MatchedAxiom(null, match.getTargetAxiom(), RETIRED);
            newRetired.setFinal(true);
            diff.removeMatch(match);
            diff.addMatch(newRetired);
        }
    }

}
