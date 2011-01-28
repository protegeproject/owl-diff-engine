package org.protege.owl.diff.present;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.util.GetAxiomSourceVisitor;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

public class Changes {
    private Collection<OWLAxiom> unmatchedSourceAxiomsWithNoSubject = new HashSet<OWLAxiom>();
    private Collection<OWLAxiom> unmatchedTargetAxiomsWithNoSubject = new HashSet<OWLAxiom>();
    private SortedSet<EntityBasedDiff> entityBasedDiffs  = new TreeSet<EntityBasedDiff>();
    private Map<OWLEntity, EntityBasedDiff> sourceDiffMap = new HashMap<OWLEntity, EntityBasedDiff>();
    private Map<OWLEntity, EntityBasedDiff> targetDiffMap = new HashMap<OWLEntity, EntityBasedDiff>();
    private AxiomDescribesEntitiesDetector sourceEntitiesDetector;
    private AxiomDescribesEntitiesDetector targetEntitiesDetector;

    private OwlDiffMap diffMap;
    
    public Changes(OwlDiffMap diffMap) {
        this.diffMap = diffMap;
        sourceEntitiesDetector = new GetAxiomSourceVisitor(diffMap.getSourceOntology(), diffMap.getOWLDataFactory());
        targetEntitiesDetector = new GetAxiomSourceVisitor(diffMap.getTargetOntology(), diffMap.getOWLDataFactory());
        initialiseDiffs();
    }

    public SortedSet<EntityBasedDiff> getEntityBasedDiffs() {
        return entityBasedDiffs;
    }
    
    public Map<OWLEntity, EntityBasedDiff> getSourceDiffMap() {
		return sourceDiffMap;
	}
    
    public Map<OWLEntity, EntityBasedDiff> getTargetDiffMap() {
		return targetDiffMap;
	}
    
    public OwlDiffMap getRawDiffMap() {
		return diffMap;
	}
    
    public Collection<OWLAxiom> getUnmatchedSourceAxiomsWithNoSubject() {
        return unmatchedSourceAxiomsWithNoSubject;
    }

    public Collection<OWLAxiom> getUnmatchedTargetAxiomsWithNoSubject() {
        return unmatchedTargetAxiomsWithNoSubject;
    }
    
    public boolean containsMatch(MatchedAxiom match) {
    	if (match.getSourceAxiom() != null) {
    		Collection<OWLEntity> subjects = sourceEntitiesDetector.getSources(match.getSourceAxiom());
    		if (!subjects.isEmpty()) {
    			OWLEntity entity = subjects.iterator().next();
    			EntityBasedDiff entityDiff = sourceDiffMap.get(entity);
    			return entityDiff != null && entityDiff.getAxiomMatches().contains(match);
    		}
    	}
    	if (match.getTargetAxiom() != null) {
    		Collection<OWLEntity> subjects = sourceEntitiesDetector.getSources(match.getTargetAxiom());
    		if (!subjects.isEmpty()) {
    			OWLEntity entity = subjects.iterator().next();
    			EntityBasedDiff entityDiff = targetDiffMap.get(entity);
    			return entityDiff != null && entityDiff.getAxiomMatches().contains(match);
    		}
    	}
    	return (match.getDescription().equals(MatchedAxiom.AXIOM_DELETED) && unmatchedSourceAxiomsWithNoSubject.contains(match.getSourceAxiom())) 
    	          || (match.getDescription().equals(MatchedAxiom.AXIOM_ADDED) && unmatchedTargetAxiomsWithNoSubject.contains(match.getTargetAxiom()));
    }
    
    public void addMatch(MatchedAxiom match) {
    	if (match.getSourceAxiom() != null) {
    		OWLAxiom axiom = match.getSourceAxiom();
    		Collection<OWLEntity> subjects = sourceEntitiesDetector.getSources(axiom);
    		for (OWLEntity e : subjects) {
    			EntityBasedDiff diff = sourceDiffMap.get(e);
    			if (diff == null) {
    				diff = new EntityBasedDiff();
    				diff.setSourceEntity(e);
    				diff.setTargetEntity(e);
    				sourceDiffMap.put(e, diff);
    				targetDiffMap.put(e, diff);
    				entityBasedDiffs.add(diff);
    			}
    			diff.addMatch(match);
    		}
    		if (subjects.isEmpty()) {
    			unmatchedSourceAxiomsWithNoSubject.add(axiom);
    		}
    	}
    	if (match.getTargetAxiom() != null) {
    		OWLAxiom axiom = match.getTargetAxiom();
    		Collection<OWLEntity> subjects = targetEntitiesDetector.getSources(axiom);
    		for (OWLEntity e : subjects) {
    			EntityBasedDiff diff = targetDiffMap.get(e);
    			if (diff == null) {
    				diff = new EntityBasedDiff();
    				diff.setSourceEntity(e);
    				diff.setTargetEntity(e);
    				sourceDiffMap.put(e, diff);
    				targetDiffMap.put(e, diff);
    				entityBasedDiffs.add(diff);
    			}
    			diff.addMatch(match);
    		}
    		if (subjects.isEmpty()) {
    			unmatchedTargetAxiomsWithNoSubject.add(axiom);
    		}
    	}
    }
    
    public void removeMatch(MatchedAxiom match) {
    	if (match.getSourceAxiom() != null) {
    		OWLAxiom axiom = match.getSourceAxiom();
    		Collection<OWLEntity> subjects = sourceEntitiesDetector.getSources(axiom);
    		for (OWLEntity e : subjects) {
    			EntityBasedDiff diff = sourceDiffMap.get(e);
    			if (diff != null) {
    				diff.removeMatch(match);
    			}
    		}
    		if (subjects.isEmpty()) {
    			unmatchedSourceAxiomsWithNoSubject.remove(axiom);
    		}
    	}
    	if (match.getTargetAxiom() != null) {
    		OWLAxiom axiom = match.getTargetAxiom();
    		Collection<OWLEntity> subjects = targetEntitiesDetector.getSources(axiom);
    		for (OWLEntity e : subjects) {
    			EntityBasedDiff diff = targetDiffMap.get(e);
    			if (diff != null) {
    				diff.removeMatch(match);
    			}
    		}
    		if (subjects.isEmpty()) {
    			unmatchedTargetAxiomsWithNoSubject.remove(axiom);
    		}
    	}
    }

    private void initialiseDiffs() {
        for (OWLEntity source : diffMap.getUnmatchedSourceEntities()) {
            EntityBasedDiff d = new EntityBasedDiff();
            d.setSourceEntity(source);
            sourceDiffMap.put(source, d);
            entityBasedDiffs.add(d);
        }
        for (Entry<OWLEntity, OWLEntity> entry : diffMap.getEntityMap().entrySet()) {
            OWLEntity source = entry.getKey();
            OWLEntity target = entry.getValue();
            if (!source.equals(target)) {
            	EntityBasedDiff d = new EntityBasedDiff();
            	d.setSourceEntity(source);
            	d.setTargetEntity(target);
            	sourceDiffMap.put(source, d);
            	targetDiffMap.put(target, d);
            	entityBasedDiffs.add(d);
            }
        }
        for (OWLEntity target : diffMap.getUnmatchedTargetEntities()) {
            EntityBasedDiff d = new EntityBasedDiff();
            d.setTargetEntity(target);
            targetDiffMap.put(target, d);
            entityBasedDiffs.add(d);
        }
        for (OWLAxiom axiom : diffMap.getUnmatchedSourceAxioms()) {
        	addMatch(new MatchedAxiom(axiom, null, MatchedAxiom.AXIOM_DELETED));
        }
        for (OWLAxiom axiom : diffMap.getUnmatchedTargetAxioms()) {
        	addMatch(new MatchedAxiom(null, axiom, MatchedAxiom.AXIOM_ADDED));
        }
    }
}
