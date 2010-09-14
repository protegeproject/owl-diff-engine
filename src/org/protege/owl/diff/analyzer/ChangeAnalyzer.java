package org.protege.owl.diff.analyzer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.protege.owl.diff.analyzer.util.AnalyzerAlgorithmComparator;
import org.protege.owl.diff.raw.OwlDiffMap;
import org.protege.owl.diff.raw.util.GetAxiomSourceVisitor;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

public class ChangeAnalyzer {
    private Logger logger = Logger.getLogger(ChangeAnalyzer.class);

    private Collection<OWLAxiom> unmatchedSourceAxiomsWithNoSubject = new HashSet<OWLAxiom>();
    private Collection<OWLAxiom> unmatchedTargetAxiomsWithNoSubject = new HashSet<OWLAxiom>();
    TreeSet<EntityBasedDiff> entityBasedDiffs  = new TreeSet<EntityBasedDiff>();
    TreeSet<AnalyzerAlgorithm> algorithms = new TreeSet<AnalyzerAlgorithm>(new AnalyzerAlgorithmComparator());

    private OwlDiffMap diffMap;
    private Properties parameters;
    
    public ChangeAnalyzer(OwlDiffMap diffMap, Properties parameters) {
        this.diffMap = diffMap;
        this.parameters = parameters;
        initialiseDiffs();
    }

    public  Collection<EntityBasedDiff> getEntityBasedDiffs() {
        return entityBasedDiffs;
    } 
    
    public Collection<OWLAxiom> getUnmatchedSourceAxiomsWithNoSubject() {
        return unmatchedSourceAxiomsWithNoSubject;
    }

    public Collection<OWLAxiom> getUnmatchedTargetAxiomsWithNoSubject() {
        return unmatchedTargetAxiomsWithNoSubject;
    }
    
    public void setAlgorihtms(AnalyzerAlgorithm[] algorithms) {
        for (AnalyzerAlgorithm algorithm : algorithms) {
            algorithm.initialise(diffMap, parameters);
            this.algorithms.add(algorithm);
        }
    }
    
    public void runAlgorithms() {
        for (EntityBasedDiff diff : entityBasedDiffs) {
            for (AnalyzerAlgorithm algorithm : algorithms) {
                algorithm.apply(diff);
            }
        }
    }

    private void initialiseDiffs() {
        Map<OWLEntity, EntityBasedDiff> sourceDiffMap = new HashMap<OWLEntity, EntityBasedDiff>();
        Map<OWLEntity, EntityBasedDiff> targetDiffMap = new HashMap<OWLEntity, EntityBasedDiff>();
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
