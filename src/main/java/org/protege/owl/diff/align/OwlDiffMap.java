package org.protege.owl.diff.align;

import java.util.Map;
import java.util.Set;

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
    String getExplanation(OWLEntity sourceEntity);
    Map<OWLAnonymousIndividual, OWLAnonymousIndividual> getAnonymousIndividualMap();
    String getExplanation(OWLAnonymousIndividual sourceIndividual);

    Set<OWLEntity>              getUnmatchedSourceEntities();
    Set<OWLAnonymousIndividual> getUnmatchedSourceAnonymousIndividuals();
    Set<OWLAxiom>               getUnmatchedSourceAxioms();
    Set<UnmatchedSourceAxiom>   getPotentialMatchingSourceAxioms();

    Set<OWLEntity>              getUnmatchedTargetEntities();
    Set<OWLAnonymousIndividual> getUnmatchedTargetAnonymousIndividuals();
    Set<OWLAxiom>               getUnmatchedTargetAxioms();

    void addMatchingEntities(Map<OWLEntity, OWLEntity> newMatches, String explanation);
    void addMatchingAnonymousIndividuals(Map<OWLAnonymousIndividual, OWLAnonymousIndividual> newMatches, String explanation);
    void addMatch(OWLEntity source, OWLEntity target, String explanation);
    void addMatch(OWLAnonymousIndividual source, OWLAnonymousIndividual target, String explanation);
    
    void finish();

    void addDiffListener(AlignmentListener listener);
    void removeDiffListener(AlignmentListener listener);
    
    void announce(AlignmentAlgorithm da);
    void summarize();

    boolean processingDone();

}
