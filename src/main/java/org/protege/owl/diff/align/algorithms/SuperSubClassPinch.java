package org.protege.owl.diff.align.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.UnmatchedAxiom;
import org.protege.owl.diff.align.util.PrioritizedComparator;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class SuperSubClassPinch implements AlignmentAlgorithm {
    public static final String REQUIRED_SUBCLASSES_PROPERTY="diff.pinch.required.subclasses";
    private static Logger log = Logger.getLogger(SuperSubClassPinch.class);
    
    private OwlDiffMap diffMap;
    
    private boolean disabled = false;
    private int requiredSubclasses;
    
    private Map<OWLClass, Set<UnmatchedAxiom>> superClassOf = new HashMap<OWLClass, Set<UnmatchedAxiom>>();
    private Map<OWLClass, Set<UnmatchedAxiom>> subClassOf = new HashMap<OWLClass, Set<UnmatchedAxiom>>();
    private Map<OWLEntity, OWLEntity> newMatches = new  HashMap<OWLEntity, OWLEntity>();
   
    public String getAlgorithmName() {
        return "Super-Sub class pinch algorithm";
    }

    /*
     * Reliable but it is a bit slow.
     */
    public int getPriority() {
        return PrioritizedComparator.MIN_PRIORITY + 2;
    }
    
    public AlignmentAggressiveness getAggressiveness() {
    	return AlignmentAggressiveness.MODERATE;
    }

    public void initialise(Engine e) {
        this.diffMap = e.getOwlDiffMap();
        requiredSubclasses = 1;
        if (e.getParameters().get(REQUIRED_SUBCLASSES_PROPERTY) != null) {
            try {
                requiredSubclasses = Integer.parseInt((String) e.getParameters().get(REQUIRED_SUBCLASSES_PROPERTY));
            }
            catch (NumberFormatException t) {
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
        if (disabled) {
            return;
        }
        diffMap.announce(this);
        try {
            newMatches.clear();
            findCandidateUnmatchedAxioms();
            searchForMatches();
            diffMap.addMatchingEntities(newMatches, "Aligned source and target entities that have a matching parent and child.");
        }
        finally {
            diffMap.summarize();
        }
    }
    
    private void searchForMatches() {
        for (Entry<OWLClass, Set<UnmatchedAxiom>> entry : superClassOf.entrySet()) {
            OWLClass sourceClass = entry.getKey();
            if (log.isDebugEnabled()) {
                log.debug("Can I match " + sourceClass + "?");
            }
            if (!diffMap.getUnmatchedSourceEntities().contains(sourceClass) || !subClassOf.containsKey(sourceClass)) {
                log.debug("no good");
                continue;
            }
            Set<OWLClass>  desiredTargetSubClasses = new HashSet<OWLClass>();
            for (UnmatchedAxiom subClassAxiom : subClassOf.get(sourceClass)) {
                OWLClass sourceSubClass = ((OWLSubClassOfAxiom) subClassAxiom.getAxiom()).getSubClass().asOWLClass();
                desiredTargetSubClasses.add((OWLClass) diffMap.getEntityMap().get(sourceSubClass));
            }
            if (log.isDebugEnabled()) {
                log.debug("" + sourceClass + " subclasses map to "  + desiredTargetSubClasses);
            }
            for (UnmatchedAxiom superClassAxiom : entry.getValue()) {
                if (superClassAxiom.getReferencedUnmatchedEntities().size() == 1) {
                    OWLClass sourceSuperClass = ((OWLSubClassOfAxiom) superClassAxiom.getAxiom()).getSuperClass().asOWLClass();
                    if (searchForMatches(sourceClass, sourceSuperClass, desiredTargetSubClasses)) {
                        subClassOf.remove(sourceClass);
                        superClassOf.remove(sourceClass);
                        return;
                    }
                }
            }
        }
    }
    
    private boolean searchForMatches(OWLClass sourceClass, OWLClass sourceSuperClass, Set<OWLClass> desiredTargetSubClasses) {
        OWLClass targetSuperClass = (OWLClass) diffMap.getEntityMap().get(sourceSuperClass);
        if (targetSuperClass != null) {
            for (OWLClassExpression potentialTargetMatch  : targetSuperClass.getSubClasses(diffMap.getTargetOntology())) {
                if (!potentialTargetMatch.isAnonymous()) {
                    OWLClass potentialMatchingClass = potentialTargetMatch.asOWLClass();
                    if (log.isDebugEnabled()) {
                        log.debug("" + sourceClass + " is a subset of " + sourceSuperClass);
                        log.debug("might map to " + potentialMatchingClass + " is a subset of " + targetSuperClass);
                        log.debug("examining subclasses  of " + potentialMatchingClass);
                    }
                    int count = 0;
                    for (OWLClassExpression targetSubclass : potentialMatchingClass.getSubClasses(diffMap.getTargetOntology())) {
                        if (log.isDebugEnabled()) {
                            log.debug("\t" + targetSubclass);
                        }
                        if (!targetSubclass.isAnonymous() && desiredTargetSubClasses.contains(targetSubclass)) {
                            log.debug("\tgood subclass");
                            if (++count >= requiredSubclasses) {
                                log.debug("match added");
                                newMatches.put(sourceClass, potentialMatchingClass);
                                return true;
                            }
                        }
                        else {
                            log.debug("\tbad subclass");
                        }
                    }
                    log.debug("didn't work out");
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
        if (log.isDebugEnabled()) {
            log.debug("Examining  axiom " + unmatched);
        }
        if (!isCandidiateUnmatchedAxiom(unmatched)) {
            log.debug("no good");
            return;
        }
        OWLSubClassOfAxiom subClassOfAxiom = (OWLSubClassOfAxiom) unmatched.getAxiom();
        OWLClass subClass = subClassOfAxiom.getSubClass().asOWLClass();
        OWLClass superClass = subClassOfAxiom.getSuperClass().asOWLClass();
        if (diffMap.getUnmatchedSourceEntities().contains(subClass)) {
            if (log.isDebugEnabled()) {
                log.debug("found super class of " + subClass);
            }
            addToMap(superClassOf, subClass, unmatched);
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("found sub class of " + superClass);
            }
            addToMap(subClassOf, superClass, unmatched);
        }
    }

    private boolean isCandidiateUnmatchedAxiom(UnmatchedAxiom unmatched) {
        if (unmatched.getAxiom() instanceof OWLSubClassOfAxiom &&
                !((OWLSubClassOfAxiom) unmatched.getAxiom()).getSubClass().isAnonymous() &&
                !((OWLSubClassOfAxiom) unmatched.getAxiom()).getSuperClass().isAnonymous()) {
            unmatched.trim(diffMap);
            return unmatched.getReferencedUnmatchedEntities().size() == 1;
        }
        return false;
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
