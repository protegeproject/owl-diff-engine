package org.protege.owl.prompt2.diff.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.protege.owl.prompt2.diff.DiffAlgorithm;
import org.protege.owl.prompt2.diff.OwlDiffMap;
import org.protege.owl.prompt2.diff.UnmatchedAxiom;
import org.protege.owl.prompt2.diff.util.DiffAlgorithmComparator;
import org.protege.owl.prompt2.diff.util.DiffListenerAdapter;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class SuperSubClassPinch implements DiffAlgorithm {
   private OwlDiffMap diffMap;
   private Set<UnmatchedAxiom> subClassAxioms = new HashSet<UnmatchedAxiom>();
   
    public String getAlgorithmName() {
        return "Super-Sub class pinch algorithm";
    }

    public int getPriority() {
        return DiffAlgorithmComparator.DEFAULT_PRIORITY;
    }

    public void initialise(OwlDiffMap diffMap, Properties parameters) {
        this.diffMap = diffMap;
        diffMap.addDiffListener(new FindCandidatesListener());
    }

    public void reset() {
        subClassAxioms.clear();
    }

    public void run() {
        if (!subClassAxioms.isEmpty()) {
            Map<OWLEntity, OWLEntity> matches = new HashMap<OWLEntity, OWLEntity>();
            
            // ...
            subClassAxioms.clear();
            // ...
            diffMap.addMatchingEntities(matches);
        }
    }

    private class FindCandidatesListener extends DiffListenerAdapter {
        
        
        @Override
        public void unmatchedAxiomMoved(UnmatchedAxiom unmatched) {
            int unmatchedCount = unmatched.getReferencedUnmatchedEntities().size();
            if (unmatchedCount == 0) {
                subClassAxioms.remove(unmatched);
            }
            else if (unmatchedCount == 1 &&
                    unmatched.getAxiom() instanceof OWLSubClassOfAxiom &&
                    !((OWLSubClassOfAxiom) unmatched.getAxiom()).getSubClass().isAnonymous() &&
                    !((OWLSubClassOfAxiom) unmatched.getAxiom()).getSubClass().isAnonymous()) {
                subClassAxioms.add(unmatched);
            }
        }
    }

}
