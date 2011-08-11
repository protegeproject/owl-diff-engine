package org.protege.owl.diff.align.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.AlignmentExplanation;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.impl.SimpleAlignmentExplanation;
import org.protege.owl.diff.align.util.PrioritizedComparator;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class MatchStandardVocabulary implements AlignmentAlgorithm {
	public static final AlignmentExplanation EXPLANATION = new SimpleAlignmentExplanation("Aligned common standard vocabulary elements.");
    private static final Set<IRI> STANDARD_OWL_IRIS;
    static {
        STANDARD_OWL_IRIS = new HashSet<IRI>();
        for (OWLRDFVocabulary vocab : OWLRDFVocabulary.values()) {
            STANDARD_OWL_IRIS.add(vocab.getIRI());
        }
        for (OWL2Datatype vocab : OWL2Datatype.values()) {
            STANDARD_OWL_IRIS.add(vocab.getIRI());
        }
    }
    
    private boolean alreadyRun = false;
    private OwlDiffMap diffs;

    public String getAlgorithmName() {
        return "Match Standard OWL Terms";
    }
    
	@Override
	public boolean isCustom() {
		return false;
	}

    public int getPriority() {
        return PrioritizedComparator.MAX_PRIORITY;
    }
    
    public AlignmentAggressiveness getAggressiveness() {
    	return AlignmentAggressiveness.IGNORE_REFACTOR;
    }

    public void initialise(Engine e) {
        diffs = e.getOwlDiffMap();
    }

    public void run() {
        if (!alreadyRun) {
            try {
                diffs.announce(this);
                Map<OWLEntity, OWLEntity> matches = new HashMap<OWLEntity, OWLEntity>();
                for (OWLEntity entity : diffs.getUnmatchedSourceEntities()) {
                    if (STANDARD_OWL_IRIS.contains(entity.getIRI())) {
                        matches.put(entity, entity);
                    }
                }
                diffs.addMatchingEntities(matches, EXPLANATION);
            }
            finally {
                diffs.summarize();
            }
            alreadyRun = true;
        }
    }
    
    public void reset() {
        alreadyRun = false;
    }

}
