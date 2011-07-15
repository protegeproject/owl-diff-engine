package org.protege.owl.diff.align.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.UnmatchedSourceAxiom;
import org.protege.owl.diff.util.DiffDuplicator;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
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
    
    
    private Map<OWLObject, String>      explanationMap = new HashMap<OWLObject, String>();

    
    /*
     * Axioms
     */
    private Map<OWLObject, Set<UnmatchedSourceAxiom>> unmatchedSourceAxiomMap = new HashMap<OWLObject, Set<UnmatchedSourceAxiom>>();
    private Set<UnmatchedSourceAxiom>                 potentialMatchingSourceAxioms  = new HashSet<UnmatchedSourceAxiom>();
    private Set<OWLAxiom>                             unmatchedSourceAxioms;
    private Set<OWLAxiom>                             unmatchedTargetAxioms;
    
    private Set<UnmatchedSourceAxiom>                 completedAnnnotationAssertionAxioms = new HashSet<UnmatchedSourceAxiom>();
    
    protected OwlDiffMapCore(OWLDataFactory factory,
                             OWLOntology sourceOntology, 
                             OWLOntology targetOntology) {
        unmatchedSourceAxioms = new HashSet<OWLAxiom>(sourceOntology.getAxioms());
        unmatchedTargetAxioms = new HashSet<OWLAxiom>(targetOntology.getAxioms());
        
        for (OWLAxiom axiom : unmatchedSourceAxioms) {
            UnmatchedSourceAxiom unmatched = new UnmatchedSourceAxiom(axiom);
            insert(unmatched, false);
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
    
    public String getExplanation(OWLEntity sourceEntity) {
    	return explanationMap.get(sourceEntity);
    }
    
    public Map<OWLAnonymousIndividual, OWLAnonymousIndividual> getAnonymousIndividualMap() {
        return Collections.unmodifiableMap(anonymousIndividualMap);
    }
    
    public String getExplanation(OWLAnonymousIndividual sourceIndividual) {
    	return explanationMap.get(sourceIndividual);
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

    public Set<UnmatchedSourceAxiom> getPotentialMatchingSourceAxioms() {
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
    public void addMatchingEntities(Map<OWLEntity, OWLEntity> newMatches, String explanation) {
    	clearBadMatches(newMatches);
        unmatchedSourceEntities.removeAll(newMatches.keySet());
        unmatchedTargetEntities.removeAll(newMatches.values());
        entityMap.putAll(newMatches);
        for (OWLEntity newEntity : newMatches.keySet()) {
            updateUnmatchedAxiomsForNewMatch(newEntity);
            explanationMap.put(newEntity, explanation);
        }
        fireAddMatchingEntities(newMatches);
    }
    
    public void addMatch(OWLEntity source, OWLEntity target, String explanation) {
    	if (!goodMatch(source, target)) {
    		return;
    	}
        unmatchedSourceEntities.remove(source);
        unmatchedTargetEntities.remove(target);
        entityMap.put(source, target);
        explanationMap.put(source, explanation);
        updateUnmatchedAxiomsForNewMatch(source);
        fireAddMatch(source, target);
    }
    
    public void addMatchingAnonymousIndividuals(Map<OWLAnonymousIndividual, OWLAnonymousIndividual> newMatches, String explanation) {
    	clearBadMatchesForAnonIndividuals(newMatches);
    	unmatchedSourceAnonIndividuals.removeAll(newMatches.keySet());
        unmatchedTargetAnonIndividuals.removeAll(newMatches.values());
        anonymousIndividualMap.putAll(newMatches);
        for (OWLAnonymousIndividual newMatchingIndividual : newMatches.keySet()) {
            updateUnmatchedAxiomsForNewMatch(newMatchingIndividual);
            explanationMap.put(newMatchingIndividual, explanation);
        }
        fireAddMatchingAnonymousIndividuals(newMatches);
    }
    
    public void addMatch(OWLAnonymousIndividual source, OWLAnonymousIndividual target, String explanation) {
    	if (!goodMatch(source, target)) {
    		return;
    	}
        unmatchedSourceAnonIndividuals.remove(source);
        unmatchedTargetAnonIndividuals.remove(target);
        anonymousIndividualMap.put(source, target);
        explanationMap.put(source, explanation);
        updateUnmatchedAxiomsForNewMatch(source);
        fireAddMatch(source, target);
    }
    
    public void finish() {
    	for (UnmatchedSourceAxiom unmatched : completedAnnnotationAssertionAxioms) {
    		insert(unmatched, true);
    	}
    	// leave them in the set in case we try finish again...
    }
    
    public boolean processingDone() {
        return unmatchedSourceAxiomMap.isEmpty();
    }
    
    /*
     * Matching entity/individual utilities...
     */
    
    private void clearBadMatches(Map<OWLEntity, OWLEntity> newMatches) {
    	Set<OWLEntity> sourceEntriesToRemove = new TreeSet<OWLEntity>();
    	for (Map.Entry<OWLEntity, OWLEntity> entry : newMatches.entrySet()) {
    		OWLEntity source = entry.getKey();
    		OWLEntity target = entry.getValue();
    		if (!goodMatch(source, target)) {
    			sourceEntriesToRemove.add(source);
    		}
    	}
    	for (OWLEntity source : sourceEntriesToRemove) {
    		newMatches.remove(source);
    	}
    }
    
    private boolean goodMatch(OWLEntity source, OWLEntity target) {
    	return unmatchedSourceEntities.contains(source) && unmatchedTargetEntities.contains(target);
    }
    
    private void clearBadMatchesForAnonIndividuals(Map<OWLAnonymousIndividual, OWLAnonymousIndividual> newMatches) {
    	Set<OWLAnonymousIndividual> sourceEntriesToRemove = new TreeSet<OWLAnonymousIndividual>();
    	for (Map.Entry<OWLAnonymousIndividual, OWLAnonymousIndividual> entry : newMatches.entrySet()) {
    		OWLAnonymousIndividual source = entry.getKey();
    		OWLAnonymousIndividual target = entry.getValue();
    		if (!goodMatch(source, target)) {
    			sourceEntriesToRemove.add(source);
    		}
    	}
    	for (OWLAnonymousIndividual source : sourceEntriesToRemove) {
    		newMatches.remove(source);
    	}
    }
    
    private boolean goodMatch(OWLAnonymousIndividual source, OWLAnonymousIndividual target) {
    	return unmatchedSourceAnonIndividuals.contains(source) && unmatchedTargetAnonIndividuals.contains(target);
    }
    
    /*
     * Axiom Matching Utilitiies
     */

    private void updateUnmatchedAxiomsForNewMatch(OWLObject source) {
        Set<UnmatchedSourceAxiom> unmatchedAxioms = unmatchedSourceAxiomMap.remove(source);
        if (unmatchedAxioms != null) {
            potentialMatchingSourceAxioms.removeAll(unmatchedAxioms);
            for (UnmatchedSourceAxiom unmatched : unmatchedAxioms) {
                unmatched.trim(this);
                insert(unmatched, false);
                fireUnmatchedAxiomMoved(unmatched);
            }
        }
    }

    private void insert(UnmatchedSourceAxiom unmatched, boolean cleanup) {
        if (unmatched.getReferencedUnmatchedEntities().isEmpty() &&
                unmatched.getReferencedUnmatchedAnonymousIndividuals().isEmpty()) {
            DiffDuplicator duplicator = new DiffDuplicator(this);
            OWLAxiom potentialTargetAxiom = duplicator.duplicateObject(unmatched.getAxiom());
            if (unmatchedTargetAxioms.contains(potentialTargetAxiom)) {
                unmatchedSourceAxioms.remove(unmatched.getAxiom());
                unmatchedTargetAxioms.remove(potentialTargetAxiom);
                fireAddMatchedAxiom(potentialTargetAxiom);
            }
            else if (!cleanup && unmatched.getAxiom() instanceof OWLAnnotationAssertionAxiom) {
            	completedAnnnotationAssertionAxioms.add(unmatched);
            }
            else {
                fireAddUnmatcheableAxiom(potentialTargetAxiom);
            }
        }
        else {
            OWLObject key = unmatched.getLeadingUnmatchedReference();
            Set<UnmatchedSourceAxiom> existingUnmatched = unmatchedSourceAxiomMap.get(key);
            if (existingUnmatched == null) {
                existingUnmatched = new HashSet<UnmatchedSourceAxiom>();
                unmatchedSourceAxiomMap.put(key, existingUnmatched);
            }
            existingUnmatched.add(unmatched);
            potentialMatchingSourceAxioms.add(unmatched);
        }
    }
}
