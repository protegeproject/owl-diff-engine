package org.protege.owl.diff.align.algorithms;

import java.util.HashMap;
import java.util.Map;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.AlignmentExplanation;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.impl.SimpleAlignmentExplanation;
import org.protege.owl.diff.align.util.PrioritizedComparator;
import org.protege.owl.diff.service.DeprecationDeferralService;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

public class MatchById implements AlignmentAlgorithm {
	public static final AlignmentExplanation EXPLANATION 
	                = new SimpleAlignmentExplanation("Source and targe entities aligned because they have the same IRI.");
    private boolean disabled = false;
    private OwlDiffMap diffMap;
    private DeprecationDeferralService dds;

    public String getAlgorithmName() {
        return "Match By Id";
    }

    public int getPriority() {
        return PrioritizedComparator.MAX_PRIORITY;
    }
    
    public AlignmentAggressiveness getAggressiveness() {
    	return AlignmentAggressiveness.IGNORE_REFACTOR;
    }

    public void initialise(Engine e) {
        this.diffMap = e.getOwlDiffMap();
        dds = DeprecationDeferralService.get(e);
    }

    public void run() {
        if (!disabled) {
            diffMap.announce(this);
            try {
                Map<OWLEntity, OWLEntity> matchingMap = new HashMap<OWLEntity, OWLEntity>();
                final OWLOntology targetOntology = diffMap.getTargetOntology();
                for (OWLEntity unmatchedSourceEntity : diffMap.getUnmatchedSourceEntities()) {
                    boolean found = targetOntology.containsEntityInSignature(unmatchedSourceEntity);
                    if (found) {
                    	if (dds.checkDeprecation(unmatchedSourceEntity, unmatchedSourceEntity)) {
                    		dds.addMatch(unmatchedSourceEntity, unmatchedSourceEntity, EXPLANATION);
                    	}
                    	else {
                    		matchingMap.put(unmatchedSourceEntity, unmatchedSourceEntity);
                    	}
                    }
                }
                diffMap.addMatchingEntities(matchingMap, EXPLANATION);
            }
            finally {
                diffMap.summarize();
            }
        }
        disabled = true;
    }
    
    public void reset() {
        disabled=false;
    }

}
