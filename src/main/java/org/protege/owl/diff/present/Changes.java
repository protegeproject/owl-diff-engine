package org.protege.owl.diff.present;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeSet;

import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.util.GetAxiomSourceVisitor;
import org.protege.owl.diff.present.util.AnalyzerAlgorithmComparator;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

public class Changes {
    private Collection<OWLAxiom> unmatchedSourceAxiomsWithNoSubject = new HashSet<OWLAxiom>();
    private Collection<OWLAxiom> unmatchedTargetAxiomsWithNoSubject = new HashSet<OWLAxiom>();
    private TreeSet<EntityBasedDiff> entityBasedDiffs  = new TreeSet<EntityBasedDiff>();
    private Map<OWLEntity, EntityBasedDiff> sourceDiffMap = new HashMap<OWLEntity, EntityBasedDiff>();
    private Map<OWLEntity, EntityBasedDiff> targetDiffMap = new HashMap<OWLEntity, EntityBasedDiff>();

    private OwlDiffMap diffMap;
    private Properties parameters;
    
    public Changes(OwlDiffMap diffMap, Properties parameters) {
        this.diffMap = diffMap;
        this.parameters = parameters;
        initialiseDiffs();
    }

    public  Collection<EntityBasedDiff> getEntityBasedDiffs() {
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
            EntityBasedDiff d = new EntityBasedDiff();
            d.setSourceEntity(source);
            d.setTargetEntity(target);
            sourceDiffMap.put(source, d);
            targetDiffMap.put(target, d);
            entityBasedDiffs.add(d);
        }
        for (OWLEntity target : diffMap.getUnmatchedTargetEntities()) {
            EntityBasedDiff d = new EntityBasedDiff();
            d.setTargetEntity(target);
            targetDiffMap.put(target, d);
            entityBasedDiffs.add(d);
        }
        for (OWLAxiom axiom : diffMap.getUnmatchedSourceAxioms()) {
            Collection<OWLEntity> subjects = new GetAxiomSourceVisitor(diffMap.getSourceOntology(), diffMap.getOWLDataFactory()).getSources(axiom);
            for (OWLEntity e : subjects) {
                sourceDiffMap.get(e).addMatch(new MatchedAxiom(axiom, null, MatchedAxiom.AXIOM_DELETED));
            }
            if (subjects.isEmpty()) {
                unmatchedSourceAxiomsWithNoSubject.add(axiom);
            }
        }
        for (OWLAxiom axiom : diffMap.getUnmatchedTargetAxioms()) {
            Collection<OWLEntity> subjects = new GetAxiomSourceVisitor(diffMap.getTargetOntology(), diffMap.getOWLDataFactory()).getSources(axiom);
            for (OWLEntity e : subjects) {
                targetDiffMap.get(e).addMatch(new MatchedAxiom(null, axiom, MatchedAxiom.AXIOM_ADDED));
            }
            if (subjects.isEmpty()) {
                unmatchedTargetAxiomsWithNoSubject.add(axiom);
            }
        }
    }
}
