package org.protege.owl.prompt2.diff.util;

import java.util.Map;
import java.util.Map.Entry;

import org.protege.owl.prompt2.diff.DiffListener;
import org.protege.owl.prompt2.diff.UnmatchedAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

public class DiffListenerAdapter implements DiffListener {

    public void addMatch(OWLEntity source, OWLEntity target) {

    }

    public void addMatchingEntities(Map<OWLEntity, OWLEntity> newMatches) {
        for (Entry<OWLEntity, OWLEntity> entry : newMatches.entrySet()) {
            addMatch(entry.getKey(), entry.getValue());
        }
    }

    public void addMatch(OWLAnonymousIndividual source, OWLAnonymousIndividual target) {

    }

    public void addMatchingAnonymousIndividuals(Map<OWLAnonymousIndividual, OWLAnonymousIndividual> newMatches) {
        for (Entry<OWLAnonymousIndividual, OWLAnonymousIndividual> entry : newMatches.entrySet()) {
            addMatch(entry.getKey(), entry.getValue());
        }
    }
    
    public void unmatchedAxiomMoved(UnmatchedAxiom unmatched) {
 
    }

    public void addMatchedAxiom(OWLAxiom axiom) {

    }

    public void addUnmatcheableAxiom(OWLAxiom axiom) {

    }

}
