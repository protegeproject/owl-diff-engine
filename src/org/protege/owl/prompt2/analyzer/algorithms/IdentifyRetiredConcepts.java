package org.protege.owl.prompt2.analyzer.algorithms;

import java.net.URI;
import java.util.Properties;

import org.protege.owl.prompt2.analyzer.AnalyzerAlgorithm;
import org.protege.owl.prompt2.analyzer.EntityBasedDiff;
import org.protege.owl.prompt2.analyzer.MatchDescription;
import org.protege.owl.prompt2.analyzer.MatchedAxiom;
import org.protege.owl.prompt2.analyzer.util.AnalyzerAlgorithmComparator;
import org.protege.owl.prompt2.diff.OwlDiffMap;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class IdentifyRetiredConcepts implements AnalyzerAlgorithm {
    public static MatchDescription RETIRED = new MatchDescription("Retired", MatchDescription.MIN_SEQUENCE);

    public static final String RETIREMENT_CLASS_PROPERTY = "retirement.class";
    
    private URI retirementUri;
    
    public void initialise(OwlDiffMap diffMap, Properties parameters) {
        String retirementString = (String) parameters.get(RETIREMENT_CLASS_PROPERTY);
        if (retirementString != null) {
            retirementUri = URI.create(retirementString);
        }
    }

    public void apply(EntityBasedDiff diff) {
        if (retirementUri == null) {
            return;
        }
        MatchedAxiom retiring = null;
        for (MatchedAxiom match : diff.getAxiomMatches()) {
            if (match.isFinal()) {
                continue;
            }
            if (match.getDescription() == MatchedAxiom.AXIOM_ADDED &&
                    match.getTargetAxiom() instanceof OWLSubClassOfAxiom) {
                OWLSubClassOfAxiom subClass = (OWLSubClassOfAxiom) match.getTargetAxiom();
                if (!subClass.getSubClass().isAnonymous() && 
                        !subClass.getSuperClass().isAnonymous() &&
                        subClass.getSuperClass().asOWLClass().getURI().equals(retirementUri)) {
                    retiring = match;
                    break;
                }
            }
        }
        if (retiring != null) {
            MatchedAxiom newRetired = new MatchedAxiom(null, retiring.getTargetAxiom(), RETIRED);
            newRetired.setFinal(true);
            diff.removeMatch(retiring);
            diff.addMatch(newRetired);
        }
    }

    public int getPriority() {
        return AnalyzerAlgorithmComparator.DEFAULT_PRIORITY + 2;
    }

}
