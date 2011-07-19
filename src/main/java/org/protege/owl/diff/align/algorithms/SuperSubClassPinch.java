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
import org.protege.owl.diff.align.AlignmentListener;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.UnmatchedSourceAxiom;
import org.protege.owl.diff.align.util.AlignmentListenerAdapter;
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
    private boolean firstPass = true;
    private int requiredSubclasses;
    
    private Map<OWLClass, Set<UnmatchedSourceAxiom>> superClassOf = new HashMap<OWLClass, Set<UnmatchedSourceAxiom>>();
    private Map<OWLClass, Set<UnmatchedSourceAxiom>> subClassOf = new HashMap<OWLClass, Set<UnmatchedSourceAxiom>>();
    private Map<OWLEntity, OWLEntity> newMatches = new  HashMap<OWLEntity, OWLEntity>();
    
    private AlignmentListener listener = new AlignmentListenerAdapter() {
		
		public void unmatchedAxiomMoved(UnmatchedSourceAxiom unmatched) {
			addCandidateUnmatchedAxiom(unmatched);
		}
		
		@Override
		public void addMatch(OWLEntity source, OWLEntity target) {
			superClassOf.remove(source);
			subClassOf.remove(target);
		}
	};
   
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
        diffMap.removeDiffListener(listener);
    }

    public void run() {
        if (disabled) {
            return;
        }
        diffMap.announce(this);
        try {
            newMatches.clear();
            if (firstPass) {
            	findCandidateUnmatchedAxioms();
            }
            searchForMatches();
            diffMap.addMatchingEntities(newMatches, "Aligned source and target entities that have a matching parent and child.");
            diffMap.addDiffListener(listener);
        }
        finally {
            diffMap.summarize();
        }
    }
    
    /* ********************************************************************************
     * Routines for finding candidate unmatched subclass of axioms.
     */
    
    private void findCandidateUnmatchedAxioms() {
	    for (UnmatchedSourceAxiom unmatched : diffMap.getPotentialMatchingSourceAxioms()) {
	        addCandidateUnmatchedAxiom(unmatched);
	    }
	}

	private void addCandidateUnmatchedAxiom(UnmatchedSourceAxiom unmatched) {
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

	private boolean isCandidiateUnmatchedAxiom(UnmatchedSourceAxiom unmatched) {
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
	
	/*
	 * The routines that do the search for the subclass/superclass pinch.
	 */

	private void searchForMatches() {
        for (Entry<OWLClass, Set<UnmatchedSourceAxiom>> entry : superClassOf.entrySet()) {
            OWLClass sourceClass = entry.getKey();
            Set<UnmatchedSourceAxiom> unmatchedSuperClassAxioms = entry.getValue();
            if (log.isDebugEnabled()) {
                log.debug("Can I match " + sourceClass + "?");
            }
            if (!diffMap.getUnmatchedSourceEntities().contains(sourceClass) || !subClassOf.containsKey(sourceClass)) {
                log.debug("no good");
                continue;
            }
            searchForMatches(sourceClass, unmatchedSuperClassAxioms, getPossibleTargetSubclasses(sourceClass));
        }
    }
        
    private void searchForMatches(OWLClass sourceClass, Set<UnmatchedSourceAxiom> unmatchedSuperClassAxioms, Set<OWLClass> possibleTargetSubclasses) {
    	for (UnmatchedSourceAxiom superClassAxiom : unmatchedSuperClassAxioms) {
    		if (superClassAxiom.getReferencedUnmatchedEntities().size() == 1) {
    			OWLClass sourceSuperClass = ((OWLSubClassOfAxiom) superClassAxiom.getAxiom()).getSuperClass().asOWLClass();
    			if (searchForMatches(sourceClass, sourceSuperClass, possibleTargetSubclasses)) {
    				break;
    			}
    		}
    	}
    }
    
    private boolean searchForMatches(OWLClass sourceClass, OWLClass sourceSuperClass, Set<OWLClass> desiredTargetSubClasses) {
    	OWLClass targetSuperClass = (OWLClass) diffMap.getEntityMap().get(sourceSuperClass);
        if (targetSuperClass != null) {
        	if (log.isDebugEnabled()) {
        		log.debug("Trying to match " + sourceClass + " using:");
        		log.debug("\tSource superclass " + sourceSuperClass);
        		log.debug("\tTarget superclass " + targetSuperClass);
        	}
            for (OWLClassExpression potentialTargetMatch  : targetSuperClass.getSubClasses(diffMap.getTargetOntology())) {
                if (!potentialTargetMatch.isAnonymous() &&
                		diffMap.getUnmatchedTargetEntities().contains(potentialTargetMatch)) {
                    OWLClass potentialMatchingClass = potentialTargetMatch.asOWLClass();
                    if (log.isDebugEnabled()) {
                    	log.debug("Potential match " + sourceClass + " --> " + potentialMatchingClass);
                    	log.debug("Looking at the subclasses of the potential target match:");
                    }
                    if (searchForMatches(sourceClass, sourceSuperClass, potentialMatchingClass, desiredTargetSubClasses)) {
                    	return true;
                    }
                    log.debug("didn't work out");
                }
            }
        }
        return false;
    }
    
    private boolean searchForMatches(OWLClass sourceClass, OWLClass sourceSuperClass, OWLClass potentialMatchingClass, Set<OWLClass> desiredTargetSubClasses) {
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
        return false;
    }
    
    private Set<OWLClass> getPossibleTargetSubclasses(OWLClass sourceClass) {
    	Set<OWLClass>  possibleTargetSubclasses = new HashSet<OWLClass>();
    	for (UnmatchedSourceAxiom subClassAxiom : subClassOf.get(sourceClass)) {
    		OWLClass sourceSubClass = ((OWLSubClassOfAxiom) subClassAxiom.getAxiom()).getSubClass().asOWLClass();
    		OWLClass possibleTargetSubclass = (OWLClass) diffMap.getEntityMap().get(sourceSubClass);
    		if (possibleTargetSubclass != null) {
    			possibleTargetSubclasses.add(possibleTargetSubclass);
    		}
    	}
    	if (log.isDebugEnabled()) {
    		log.debug("" + sourceClass + " subclasses map to "  + possibleTargetSubclasses);
    	}
    	return possibleTargetSubclasses;
    }

}
