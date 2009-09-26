package org.protege.owl.prompt2.diff.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.protege.owl.prompt2.diff.DiffAlgorithm;
import org.protege.owl.prompt2.diff.DiffListener;
import org.protege.owl.prompt2.diff.OwlDiffMap;
import org.protege.owl.prompt2.diff.UnmatchedAxiom;
import org.protege.owl.prompt2.diff.util.DiffAlgorithmComparator;
import org.protege.owl.prompt2.diff.util.DiffListenerAdapter;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class SuperSubClassPinch implements DiffAlgorithm {
    public static final String REQUIRED_SUBCLASSES_PROPERTY="diff.pinch.required.subclasses";
    private static Logger log = Logger.getLogger(SuperSubClassPinch.class);
    
    private OwlDiffMap diffMap;
    private DiffListener listener;
    
    private boolean firstRun = true;
    private boolean disabled = false;
    private int requiredSubclasses;
    
    private Map<OWLClass, Set<UnmatchedAxiom>> superClassOf = new HashMap<OWLClass, Set<UnmatchedAxiom>>();
    private Map<OWLClass, Set<UnmatchedAxiom>> subClassOf = new HashMap<OWLClass, Set<UnmatchedAxiom>>();
    private Map<OWLEntity, OWLEntity> newMatches = new  HashMap<OWLEntity, OWLEntity>();
   
    public String getAlgorithmName() {
        return "Super-Sub class pinch algorithm";
    }

    public int getPriority() {
        return DiffAlgorithmComparator.DEFAULT_PRIORITY;
    }

    public void initialise(OwlDiffMap diffMap, Properties parameters) {
        this.diffMap = diffMap;
        requiredSubclasses = 1;
        if (parameters.get(REQUIRED_SUBCLASSES_PROPERTY) != null) {
            try {
                requiredSubclasses = Integer.parseInt((String) parameters.get(REQUIRED_SUBCLASSES_PROPERTY));
            }
            catch (Throwable t) {
                log.warn("Could not initialize required subclasses value", t);
                disabled = true;
            }
        }
    }

    public void reset() {
        superClassOf.clear();
        subClassOf.clear();
    }

    public void run() {
        diffMap.announce(this);
        try {
            newMatches.clear();
            if (firstRun) {
                listener = new FindCandidateUnmatchedAxiomListener();
                diffMap.addDiffListener(listener);
                findCandidateUnmatchedAxioms();
                firstRun = false;
            }
            searchForMatches();
            diffMap.addMatchingEntities(newMatches);
        }
        finally {
            diffMap.summarize();
        }
    }
    
    private void searchForMatches() {
        for (Entry<OWLClass, Set<UnmatchedAxiom>> entry : superClassOf.entrySet()) {
            OWLClass sourceClass = entry.getKey();
            if (diffMap.getUnmatchedSourceEntities().contains(sourceClass) || !subClassOf.containsKey(sourceClass)) {
                continue;
            }
            Set<OWLClass>  desiredTargetSubClasses = new HashSet<OWLClass>();
            for (UnmatchedAxiom subClassAxiom : subClassOf.get(sourceClass)) {
                OWLClass sourceSubClass = ((OWLSubClassOfAxiom) subClassAxiom.getAxiom()).getSubClass().asOWLClass();
                desiredTargetSubClasses.add((OWLClass) diffMap.getEntityMap().get(sourceSubClass));
            }
            for (UnmatchedAxiom superClassAxiom : entry.getValue()) {
                if (superClassAxiom.getReferencedUnmatchedEntities().size() == 1) {
                    OWLClass sourceSuperClass = ((OWLSubClassOfAxiom) superClassAxiom.getAxiom()).getSuperClass().asOWLClass();
                    if (searchForMatches(sourceClass, sourceSuperClass, desiredTargetSubClasses)) {
                        return;
                    }
                }
            }
        }
    }
    
    private boolean searchForMatches(OWLClass sourceClass, OWLClass sourceSuperClass, Set<OWLClass> desiredTargetSubClasses) {
        OWLClass targetSuperClass = (OWLClass) diffMap.getEntityMap().get(sourceClass);
        if (targetSuperClass != null) {
            for (OWLClassExpression potentialTargetMatch  : targetSuperClass.getSubClasses(diffMap.getTargetOntology())) {
                if (!potentialTargetMatch.isAnonymous()) {
                    OWLClass potentialMatchingClass = potentialTargetMatch.asOWLClass();
                    int count = 0;
                    for (OWLClassExpression targetSubclass : potentialMatchingClass.getSubClasses(diffMap.getTargetOntology())) {
                        if (!targetSubclass.isAnonymous() && desiredTargetSubClasses.contains(targetSubclass)) {
                            if (++count >= requiredSubclasses) {
                                newMatches.put(sourceClass, potentialMatchingClass);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private void findCandidateUnmatchedAxioms() {
        for (UnmatchedAxiom unmatched : diffMap.getPotentialMatchingSourceAxioms()) {
            addCandidateUnmatchedAxiom(unmatched);
        }
    }
    
    private void addCandidateUnmatchedAxiom(UnmatchedAxiom unmatched) {
        if (!isCandidiateUnmatchedAxiom(unmatched)) {
            return;
        }
        OWLSubClassOfAxiom subClassOfAxiom = (OWLSubClassOfAxiom) unmatched.getAxiom();
        OWLClass subClass = subClassOfAxiom.getSubClass().asOWLClass();
        OWLClass superClass = subClassOfAxiom.getSuperClass().asOWLClass();
        if (diffMap.getUnmatchedSourceEntities().contains(subClass)) {
            addToMap(subClassOf, superClass, unmatched);
        }
        else {
            addToMap(superClassOf, subClass, unmatched);
        }
    }

    private boolean isCandidiateUnmatchedAxiom(UnmatchedAxiom unmatched) {
        return unmatched.getReferencedUnmatchedEntities().size() == 1 &&
                    unmatched.getAxiom() instanceof OWLSubClassOfAxiom &&
                    !((OWLSubClassOfAxiom) unmatched.getAxiom()).getSubClass().isAnonymous() &&
                    !((OWLSubClassOfAxiom) unmatched.getAxiom()).getSubClass().isAnonymous();
    }
    
    private class FindCandidateUnmatchedAxiomListener extends DiffListenerAdapter {    
        @Override
        public void unmatchedAxiomMoved(UnmatchedAxiom unmatched) {
            SuperSubClassPinch.this.addCandidateUnmatchedAxiom(unmatched);
        }
    }
    
    public static <X, Y> void addToMap(Map<X, Set<Y>>  map, X x, Y y) {
        Set<Y> ys  = map.get(x);
        if (ys == null) {
            ys = new HashSet<Y>();
            map.put(x, ys);
        }
        ys.add(y);
    }

}
