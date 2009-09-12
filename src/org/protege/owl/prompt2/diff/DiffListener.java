package org.protege.owl.prompt2.diff;

import java.util.Map;

import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

public interface DiffListener {

    void addMatchingEntities(Map<OWLEntity, OWLEntity> newMatches);
    void addMatchingAnonymousIndividuals(Map<OWLAnonymousIndividual, OWLAnonymousIndividual> newMatches);
    void addMatch(OWLEntity source, OWLEntity target);
    void addMatch(OWLAnonymousIndividual source, OWLAnonymousIndividual target);
    
    void unmatchedAxiomMoved(UnmatchedAxiom unmatched);
    void addUnmatcheableAxiom(OWLAxiom axiom);
    void addMatchedAxiom(OWLAxiom axiom);
    
    
}
