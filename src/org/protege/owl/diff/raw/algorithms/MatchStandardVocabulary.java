package org.protege.owl.diff.raw.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.protege.owl.diff.raw.DiffAlgorithm;
import org.protege.owl.diff.raw.OwlDiffMap;
import org.protege.owl.diff.raw.util.DiffAlgorithmComparator;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class MatchStandardVocabulary implements DiffAlgorithm {
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

    public int getPriority() {
        return DiffAlgorithmComparator.MAX_PRIORITY;
    }

    public void initialise(OwlDiffMap diffMap, Properties parameters) {
        diffs = diffMap;
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
                diffs.addMatchingEntities(matches);
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
