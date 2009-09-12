package org.protege.owl.prompt2.diff;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

public interface OwlDiffMap {

    OWLDataFactory getOWLDataFactory();
    OWLOntology getSourceOntology();
    OWLOntology getTargetOntology();

    Map<OWLEntity, OWLEntity>   getEntityMap();
    Map<OWLAnonymousIndividual, OWLAnonymousIndividual> getAnonymousIndividualMap();

    Set<OWLEntity>              getUnmatchedSourceEntities();
    Set<OWLAnonymousIndividual> getUnmatchedSourceAnonymousIndividuals();
    Set<OWLAxiom>               getUnmatchedSourceAxioms();

    Set<OWLEntity>              getUnmatchedTargetEntities();
    Set<OWLAnonymousIndividual> getUnmatchedTargetAnonymousIndividuals();
    Set<OWLAxiom>               getUnmatchedTargetAxioms();

    void addMatchingEntities(Map<OWLEntity, OWLEntity> newMatches);
    void addMatchingAnonymousIndividuals(Map<OWLAnonymousIndividual, OWLAnonymousIndividual> newMatches);
    void addMatch(OWLEntity source, OWLEntity target);
    void addMatch(OWLAnonymousIndividual source, OWLAnonymousIndividual target);


    void announce(DiffAlgorithm da);
    void summarize();

    boolean processingDone();

}
