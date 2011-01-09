package org.protege.owl.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protege.owl.diff.align.AlignmentListener;
import org.protege.owl.diff.align.UnmatchedAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;


public class CountEntityMatchesListener implements AlignmentListener {
    private List<Set<OWLEntity>> entityMatches = new ArrayList<Set<OWLEntity>>();
    
    public List<Set<OWLEntity>> getEntityMatches() {
        return Collections.unmodifiableList(entityMatches);
    }

    public void addMatch(OWLEntity source, OWLEntity target) {
        entityMatches.add(Collections.singleton(source));
    }

    public void addMatchingEntities(Map<OWLEntity, OWLEntity> newMatches) {
        entityMatches.add(new HashSet<OWLEntity>(newMatches.keySet()));
    }

    public void addMatch(OWLAnonymousIndividual source, OWLAnonymousIndividual target) {

    }

    public void addMatchingAnonymousIndividuals(Map<OWLAnonymousIndividual, OWLAnonymousIndividual> newMatches) {

    }
    
    public void unmatchedAxiomMoved(UnmatchedAxiom unmatched) {

    }

    public void addMatchedAxiom(OWLAxiom axiom) {

    }

    public void addUnmatcheableAxiom(OWLAxiom axiom) {

    }

}
