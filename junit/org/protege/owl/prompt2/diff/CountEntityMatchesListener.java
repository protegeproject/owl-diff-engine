package org.protege.owl.prompt2.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;


public class CountEntityMatchesListener implements DiffListener {
    private List<Integer> counts = new ArrayList<Integer>();
    
    public List<Integer> getCounts() {
        return counts;
    }

    public void addMatch(OWLEntity source, OWLEntity target) {
        counts.add(1);
    }

    public void addMatchingEntities(Map<OWLEntity, OWLEntity> newMatches) {
        counts.add(newMatches.size());
    }

    public void addMatch(OWLAnonymousIndividual source, OWLAnonymousIndividual target) {

    }

    public void addMatchingAnonymousIndividuals(Map<OWLAnonymousIndividual, OWLAnonymousIndividual> newMatches) {

    }

    public void addMatchedAxiom(OWLAxiom axiom) {

    }

    public void addUnmatcheableAxiom(OWLAxiom axiom) {

    }

}
