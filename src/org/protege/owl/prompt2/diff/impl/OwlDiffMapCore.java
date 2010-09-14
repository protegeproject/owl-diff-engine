package org.protege.owl.prompt2.diff.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.protege.owl.prompt2.diff.OwlDiffMap;
import org.protege.owl.prompt2.diff.UnmatchedAxiom;
import org.protege.owl.prompt2.diff.util.DiffDuplicator;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

public abstract class OwlDiffMapCore extends DiffListenerCollection implements OwlDiffMap {
    /*
     * Entities
     */
    private Map<OWLEntity, OWLEntity>   entityMap = new HashMap<OWLEntity, OWLEntity>();
    private Set<OWLEntity>              unmatchedSourceEntities        = new HashSet<OWLEntity>(); 
    private Set<OWLEntity>              unmatchedTargetEntities;
    
    /*
     * Anonymous Individuals
     */
    
    private Map<OWLAnonymousIndividual, OWLAnonymousIndividual> anonymousIndividualMap = new HashMap<OWLAnonymousIndividual, OWLAnonymousIndividual>();
    private Set<OWLAnonymousIndividual>                         unmatchedSourceAnonIndividuals = new HashSet<OWLAnonymousIndividual>();
    private Set<OWLAnonymousIndividual>                         unmatchedTargetAnonIndividuals;
    
    
    /*
     * Axioms
     */
    private Map<OWLObject, Set<UnmatchedAxiom>> unmatchedAxiomsMap = new HashMap<OWLObject, Set<UnmatchedAxiom>>();
    private Set<UnmatchedAxiom>                 potentialMatchingSourceAxioms  = new HashSet<UnmatchedAxiom>();
    private Set<OWLAxiom>                       unmatchedSourceAxioms;
    private Set<OWLAxiom>                       unmatchedTargetAxioms;


    
    protected OwlDiffMapCore(OWLDataFactory factory,
                             OWLOntology sourceOntology, 
                             OWLOntology targetOntology) {
        unmatchedSourceAxioms = new HashSet<OWLAxiom>(sourceOntology.getAxioms());
        unmatchedTargetAxioms = new HashSet<OWLAxiom>(targetOntology.getAxioms());
        
        for (OWLAxiom axiom : unmatchedSourceAxioms) {
            UnmatchedAxiom unmatched = new UnmatchedAxiom(axiom);
            insert(unmatched);
            unmatchedSourceEntities.addAll(unmatched.getReferencedUnmatchedEntities());
            unmatchedSourceAnonIndividuals.addAll(unmatched.getReferencedUnmatchedAnonymousIndividuals());
        }
        
        unmatchedTargetEntities = new HashSet<OWLEntity>(targetOntology.getSignature());
        unmatchedTargetAnonIndividuals = new HashSet<OWLAnonymousIndividual>(targetOntology.getReferencedAnonymousIndividuals());        
    }
    
    /*
     * Getters
     */

    public Map<OWLEntity, OWLEntity> getEntityMap() {
        return Collections.unmodifiableMap(entityMap);
    }
    
    public Map<OWLAnonymousIndividual, OWLAnonymousIndividual> getAnonymousIndividualMap() {
        return Collections.unmodifiableMap(anonymousIndividualMap);
    }

    public Set<OWLEntity> getUnmatchedSourceEntities() {
        return Collections.unmodifiableSet(unmatchedSourceEntities);
    }
    
    public Set<OWLEntity> getUnmatchedTargetEntities() {
        return Collections.unmodifiableSet(unmatchedTargetEntities);
    }

    public Set<OWLAnonymousIndividual> getUnmatchedSourceAnonymousIndividuals() {
        return Collections.unmodifiableSet(unmatchedSourceAnonIndividuals);
    }

    public Set<OWLAnonymousIndividual> getUnmatchedTargetAnonymousIndividuals() {
        return Collections.unmodifiableSet(unmatchedTargetAnonIndividuals);
    }

    public Set<UnmatchedAxiom> getPotentialMatchingSourceAxioms() {
        return Collections.unmodifiableSet(potentialMatchingSourceAxioms);
    }

    public Set<OWLAxiom> getUnmatchedSourceAxioms() {
        return Collections.unmodifiableSet(unmatchedSourceAxioms);
    }

    public Set<OWLAxiom> getUnmatchedTargetAxioms() {
        return Collections.unmodifiableSet(unmatchedTargetAxioms);
    }
    
    /*
     * Match Processing methods
     */
    public void addMatchingEntities(Map<OWLEntity, OWLEntity> newMatches) {
        unmatchedSourceEntities.removeAll(newMatches.keySet());
        unmatchedTargetEntities.removeAll(newMatches.values());
        entityMap.putAll(newMatches);
        for (OWLEntity newEntity : newMatches.keySet()) {
            updateUnmatchedAxiomsForNewMatch(newEntity);
        }
        fireAddMatchingEntities(newMatches);
    }
    
    public void addMatch(OWLEntity source, OWLEntity target) {
        unmatchedSourceEntities.remove(source);
        unmatchedTargetEntities.remove(target);
        entityMap.put(source, target);
        updateUnmatchedAxiomsForNewMatch(source);
        fireAddMatch(source, target);
    }
    
    public void addMatchingAnonymousIndividuals(Map<OWLAnonymousIndividual, OWLAnonymousIndividual> newMatches) {
        unmatchedSourceAnonIndividuals.removeAll(newMatches.keySet());
        unmatchedTargetAnonIndividuals.removeAll(newMatches.values());
        anonymousIndividualMap.putAll(newMatches);
        for (OWLAnonymousIndividual newMatchingIndividual : newMatches.keySet()) {
            updateUnmatchedAxiomsForNewMatch(newMatchingIndividual);
        }
        fireAddMatchingAnonymousIndividuals(newMatches);
    }
    
    public void addMatch(OWLAnonymousIndividual source, OWLAnonymousIndividual target) {
        unmatchedSourceAnonIndividuals.remove(source);
        unmatchedTargetAnonIndividuals.remove(target);
        anonymousIndividualMap.put(source, target);
        updateUnmatchedAxiomsForNewMatch(source);
        fireAddMatch(source, target);
    }
    
    public boolean processingDone() {
        return unmatchedAxiomsMap.isEmpty();
    }
    
    /*
     * Axiom Matching Utilitiies
     */

    private void updateUnmatchedAxiomsForNewMatch(OWLObject source) {
        Set<UnmatchedAxiom> unmatchedAxioms = unmatchedAxiomsMap.remove(source);
        if (unmatchedAxioms != null) {
            potentialMatchingSourceAxioms.removeAll(unmatchedAxioms);
            for (UnmatchedAxiom unmatched : unmatchedAxioms) {
                unmatched.trim(this);
                insert(unmatched);
                fireUnmatchedAxiomMoved(unmatched);
            }
        }
    }

    private void insert(UnmatchedAxiom unmatched) {
        if (unmatched.getReferencedUnmatchedEntities().isEmpty() &&
                unmatched.getReferencedUnmatchedAnonymousIndividuals().isEmpty()) {
            DiffDuplicator duplicator = new DiffDuplicator(this);
            OWLAxiom potentialTargetAxiom = duplicator.duplicateObject(unmatched.getAxiom());
            if (unmatchedTargetAxioms.contains(potentialTargetAxiom)) {
                unmatchedSourceAxioms.remove(unmatched.getAxiom());
                unmatchedTargetAxioms.remove(potentialTargetAxiom);
                fireAddMatchedAxiom(potentialTargetAxiom);
            }
            else {
                fireAddUnmatcheableAxiom(potentialTargetAxiom);
            }
        }
        else {
            OWLObject key = unmatched.getLeadingUnmatchedReference();
            Set<UnmatchedAxiom> existingUnmatched = unmatchedAxiomsMap.get(key);
            if (existingUnmatched == null) {
                existingUnmatched = new HashSet<UnmatchedAxiom>();
                unmatchedAxiomsMap.put(key, existingUnmatched);
            }
            existingUnmatched.add(unmatched);
            potentialMatchingSourceAxioms.add(unmatched);
        }
    }
}
