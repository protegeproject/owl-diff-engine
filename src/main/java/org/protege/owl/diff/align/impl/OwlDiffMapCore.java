package org.protege.owl.diff.align.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.protege.owl.diff.align.AlignmentExplanation;
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
	public static final Logger LOGGER = Logger.getLogger(OwlDiffMapCore.class.getName());
	
    /*
     * Entities
     */
    private Map<OWLEntity, OWLEntity>        entityMap                  = new HashMap<OWLEntity, OWLEntity>();
    private Set<OWLEntity>                   unmatchedSourceEntities    = new HashSet<OWLEntity>(); 
    private Set<OWLEntity>                   unmatchedTargetEntities;
    private Map<OWLEntity, Set<OWLEntity>>   blockedEntityMatches       = new HashMap<OWLEntity, Set<OWLEntity>>();
    
    /*
     * Anonymous Individuals
     */
    
    private Map<OWLAnonymousIndividual, OWLAnonymousIndividual> anonymousIndividualMap = new HashMap<OWLAnonymousIndividual, OWLAnonymousIndividual>();
    private Set<OWLAnonymousIndividual>                         unmatchedSourceAnonIndividuals = new HashSet<OWLAnonymousIndividual>();
    private Set<OWLAnonymousIndividual>                         unmatchedTargetAnonIndividuals;
    
    
    private Map<OWLObject, AlignmentExplanation>  explanationMap = new HashMap<OWLObject, AlignmentExplanation>();

    
    /*
     * Axioms
     */
    private Map<OWLObject, Set<UnmatchedSourceAxiom>>     unmatchedSourceAxiomMap        = new HashMap<OWLObject, Set<UnmatchedSourceAxiom>>();
    private Set<UnmatchedSourceAxiom>                     potentialMatchingSourceAxioms  = new HashSet<UnmatchedSourceAxiom>();
    private Set<OWLAxiom>                                 unmatchedSourceAxioms;
    private Set<OWLAxiom>                                 unmatchedTargetAxioms;
    
    private Set<UnmatchedSourceAxiom>                     completedAnnnotationAssertionAxioms = new HashSet<UnmatchedSourceAxiom>();
    
    protected OwlDiffMapCore(OWLDataFactory factory,
                             OWLOntology sourceOntology, 
                             OWLOntology targetOntology) {
    	long startTime = System.currentTimeMillis();
    	
        unmatchedSourceAxioms = new HashSet<OWLAxiom>(sourceOntology.getAxioms());
        unmatchedTargetAxioms = new HashSet<OWLAxiom>(targetOntology.getAxioms());

        for (OWLAxiom axiom : unmatchedSourceAxioms) {
            UnmatchedSourceAxiomImpl unmatched = new UnmatchedSourceAxiomImpl(axiom);
            
            potentialMatchingSourceAxioms.add(unmatched);
        	for (OWLEntity entity : unmatched.getReferencedUnmatchedEntities()) {
        		Set<UnmatchedSourceAxiom> unmatchedSet = unmatchedSourceAxiomMap.get(entity);
        		if (unmatchedSet == null) {
        			unmatchedSet = new HashSet<UnmatchedSourceAxiom>();
        			unmatchedSourceAxiomMap.put(entity, unmatchedSet);
        		}
        		unmatchedSet.add(unmatched);
        	}
        	for (OWLAnonymousIndividual anonymous : unmatched.getReferencedUnmatchedAnonymousIndividuals()) {
        		Set<UnmatchedSourceAxiom> unmatchedSet = unmatchedSourceAxiomMap.get(anonymous);
        		if (unmatchedSet == null) {
        			unmatchedSet = new HashSet<UnmatchedSourceAxiom>();
        			unmatchedSourceAxiomMap.put(anonymous, unmatchedSet);
        		}
        		unmatchedSet.add(unmatched);
        	} 

            unmatchedSourceEntities.addAll(unmatched.getReferencedUnmatchedEntities());
            unmatchedSourceAnonIndividuals.addAll(unmatched.getReferencedUnmatchedAnonymousIndividuals());
        }
        
        unmatchedTargetEntities = new HashSet<OWLEntity>(targetOntology.getSignature());
        unmatchedTargetAnonIndividuals = new HashSet<OWLAnonymousIndividual>(targetOntology.getReferencedAnonymousIndividuals());        
    
        if (LOGGER.isLoggable(Level.INFO)) {
        	LOGGER.info("Initialization of core diff map structures took " + (System.currentTimeMillis() - startTime) + "ms.");
        }
    }
    
    /*
     * Getters
     */

    public Map<OWLEntity, OWLEntity> getEntityMap() {
        return Collections.unmodifiableMap(entityMap);
    }
    
    public AlignmentExplanation getExplanation(OWLEntity sourceEntity) {
    	return explanationMap.get(sourceEntity);
    }
    
    public Map<OWLAnonymousIndividual, OWLAnonymousIndividual> getAnonymousIndividualMap() {
        return Collections.unmodifiableMap(anonymousIndividualMap);
    }
    
    public AlignmentExplanation getExplanation(OWLAnonymousIndividual sourceIndividual) {
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
    public void addMatchingEntities(Map<OWLEntity, OWLEntity> newMatches, AlignmentExplanation explanation) {
    	clearBadMatches(newMatches);
        unmatchedSourceEntities.removeAll(newMatches.keySet());
        unmatchedTargetEntities.removeAll(newMatches.values());
        entityMap.putAll(newMatches);
        
        Set<UnmatchedSourceAxiom> movingSourceAxioms = new HashSet<UnmatchedSourceAxiom>();
        for (OWLEntity newEntity : newMatches.keySet()) {
        	Set<UnmatchedSourceAxiom> removedUnmatchedSourceAxioms = unmatchedSourceAxiomMap.remove(newEntity);
        	if (removedUnmatchedSourceAxioms != null) {
        		movingSourceAxioms.addAll(removedUnmatchedSourceAxioms);
        	}
            explanationMap.put(newEntity, explanation);
        }
        updateAxiomMatches(movingSourceAxioms);
        fireAddMatchingEntities(newMatches);
    }
    
    public void addMatch(OWLEntity source, OWLEntity target, AlignmentExplanation explanation) {
    	if (!goodMatch(source, target)) {
    		return;
    	}
        unmatchedSourceEntities.remove(source);
        unmatchedTargetEntities.remove(target);
        entityMap.put(source, target);
        explanationMap.put(source, explanation);
        updateAxiomMatches(unmatchedSourceAxiomMap.remove(source));
        fireAddMatch(source, target);
    }
    
    public void setMatchBlocked(OWLEntity source, OWLEntity target, boolean block) {
    	Set<OWLEntity> blockedTargets = blockedEntityMatches.get(source);
    	if (block) {
    		if (blockedTargets == null) {
    			blockedTargets = new TreeSet<OWLEntity>();
    			blockedEntityMatches.put(source, blockedTargets);
    		}
    		blockedTargets.add(target);
    	}
    	else if (blockedTargets != null) {
    		blockedTargets.remove(target);
    	}
    }
    
    public void addMatchingAnonymousIndividuals(Map<OWLAnonymousIndividual, OWLAnonymousIndividual> newMatches, AlignmentExplanation explanation) {
    	clearBadMatchesForAnonIndividuals(newMatches);
    	unmatchedSourceAnonIndividuals.removeAll(newMatches.keySet());
        unmatchedTargetAnonIndividuals.removeAll(newMatches.values());
        anonymousIndividualMap.putAll(newMatches);
        
        Set<UnmatchedSourceAxiom> movingSourceAxioms = new HashSet<UnmatchedSourceAxiom>();
        for (OWLAnonymousIndividual newMatchingIndividual : newMatches.keySet()) {
        	Set<UnmatchedSourceAxiom> removedUnmatchedSourceAxioms = unmatchedSourceAxiomMap.remove(newMatchingIndividual);
        	if (removedUnmatchedSourceAxioms != null) {
        		movingSourceAxioms.addAll(removedUnmatchedSourceAxioms);
        	}
            explanationMap.put(newMatchingIndividual, explanation);
        }
        updateAxiomMatches(movingSourceAxioms);
        fireAddMatchingAnonymousIndividuals(newMatches);
    }
    
    public void addMatch(OWLAnonymousIndividual source, OWLAnonymousIndividual target, AlignmentExplanation explanation) {
    	if (!goodMatch(source, target)) {
    		return;
    	}
        unmatchedSourceAnonIndividuals.remove(source);
        unmatchedTargetAnonIndividuals.remove(target);
        anonymousIndividualMap.put(source, target);
        explanationMap.put(source, explanation);
        updateAxiomMatches(unmatchedSourceAxiomMap.remove(source));
        fireAddMatch(source, target);
    }
    
    public void finish() {
    	for (UnmatchedSourceAxiom unmatched : completedAnnnotationAssertionAxioms) {
    		updateAxiomMatches(unmatched, true);
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
    	Set<OWLEntity> blockedTargets = blockedEntityMatches.get(source);
    	boolean notBlocked = (blockedTargets == null || !blockedTargets.contains(target));
    	boolean notMatched = unmatchedSourceEntities.contains(source) && unmatchedTargetEntities.contains(target);
    	return notBlocked && notMatched;
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

    private void updateAxiomMatches(Set<UnmatchedSourceAxiom> unmatchedAxioms) {
        if (unmatchedAxioms != null) {
            for (UnmatchedSourceAxiom unmatched : unmatchedAxioms) {
                updateAxiomMatches(unmatched, false);
            }
        }
    }

    private void updateAxiomMatches(UnmatchedSourceAxiom unmatched, boolean cleanup) {
    	unmatched.trim(this);
        if (unmatched.getReferencedUnmatchedEntities().isEmpty() &&
                unmatched.getReferencedUnmatchedAnonymousIndividuals().isEmpty()) {
        	potentialMatchingSourceAxioms.remove(unmatched);
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
        fireUnmatchedAxiomMoved(unmatched);
    }
}
