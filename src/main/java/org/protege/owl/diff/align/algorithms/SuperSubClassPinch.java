package org.protege.owl.diff.align.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.AlignmentListener;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.UnmatchedSourceAxiom;
import org.protege.owl.diff.align.impl.SimpleAlignmentExplanation;
import org.protege.owl.diff.align.util.AlignmentListenerAdapter;
import org.protege.owl.diff.align.util.PrioritizedComparator;
import org.protege.owl.diff.service.RenderingService;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.search.EntitySearcher;

public class SuperSubClassPinch implements AlignmentAlgorithm {
    public static final String REQUIRED_SUBCLASSES_PROPERTY="diff.pinch.required.subclasses";
    private static Logger log = LoggerFactory.getLogger(SuperSubClassPinch.class.getName());
    
    private OwlDiffMap diffMap;
    private RenderingService renderer;
    
    private boolean disabled = false;
    private boolean firstPass = true;
    private int requiredSubclasses;
    
    private Map<OWLClass, Set<OWLClass>> superClassOf = new HashMap<OWLClass, Set<OWLClass>>();
    private Map<OWLClass, Set<OWLClass>> subClassOf   = new HashMap<OWLClass, Set<OWLClass>>();
    private Map<OWLEntity, OWLEntity> newMatches      = new  HashMap<OWLEntity, OWLEntity>();
    
    private AlignmentListener listener = new AlignmentListenerAdapter() {
    	
    	@Override
    	public void unmatchedAxiomMoved(UnmatchedSourceAxiom unmatched) {
    		addCandidateUnmatchedAxiom(unmatched);
    	}
    	
		public void addMatch(OWLEntity source, OWLEntity target) {
			superClassOf.remove(source);
			subClassOf.remove(source);
		}
		
		public void addMatchingEntities(java.util.Map<OWLEntity,OWLEntity> newMatches) {
			for (OWLEntity source : newMatches.keySet()) {
				superClassOf.remove(source);
				subClassOf.remove(source);
			}
		}
	};
   
    public String getAlgorithmName() {
        return "Super-Sub class pinch algorithm";
    }
    
	@Override
	public boolean isCustom() {
		return false;
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
        this.renderer = RenderingService.get(e);
        requiredSubclasses = 1;
        if (e.getParameters().get(REQUIRED_SUBCLASSES_PROPERTY) != null) {
            try {
                requiredSubclasses = Integer.parseInt(e.getParameters().get(REQUIRED_SUBCLASSES_PROPERTY));
            }
            catch (NumberFormatException t) {
                log.warn("Could not initialize required subclasses value" + t.getMessage());
                disabled = true;
            }
        }
    }

    public void reset() {
        superClassOf.clear();
        subClassOf.clear();
        diffMap.removeDiffListener(listener);
        diffMap = null;
        renderer = null;
    }

    public void run() {
        if (disabled) {
            return;
        }
        diffMap.announce(this);
        try {
            newMatches.clear();
            if (firstPass) {
                diffMap.addDiffListener(listener);
            	firstPass = false;
            	findCandidateUnmatchedAxioms();
            }
            searchForMatches();
            diffMap.addMatchingEntities(newMatches, new Explanation(diffMap, renderer));
        }
        finally {
            diffMap.summarize();
        }
    }
    
    /* ********************************************************************************
     * Routines for finding candidate unmatched subclass of axioms.
     */
    
    private void findCandidateUnmatchedAxioms() {
    	subClassOf.clear();
    	superClassOf.clear();
	    for (UnmatchedSourceAxiom unmatched : diffMap.getPotentialMatchingSourceAxioms()) {
	        addCandidateUnmatchedAxiom(unmatched);
	    }
	}

	private void addCandidateUnmatchedAxiom(UnmatchedSourceAxiom unmatched) {
	    if (log.isInfoEnabled()) {
	        log.info("Examining  axiom " + unmatched);
	    }
	    if (!isCandidiateUnmatchedAxiom(unmatched)) {
	    	if (log.isInfoEnabled()) {
	    		log.info("no good");
	    	}
	        return;
	    }
	    OWLSubClassOfAxiom subClassOfAxiom = (OWLSubClassOfAxiom) unmatched.getAxiom();
	    OWLClass subClass = subClassOfAxiom.getSubClass().asOWLClass();
	    OWLClass superClass = subClassOfAxiom.getSuperClass().asOWLClass();
	    if (diffMap.getUnmatchedSourceEntities().contains(subClass)) {
	        if (log.isInfoEnabled()) {
	            log.info("found super class of " + subClass);
	        }
	        addToMap(superClassOf, subClass, superClass);
	    }
	    if (diffMap.getUnmatchedSourceEntities().contains(superClass)) {
	        if (log.isInfoEnabled()) {
	            log.info("found sub class of " + superClass);
	        }
	        addToMap(subClassOf, superClass, subClass);
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
        for (OWLClass sourceClass : superClassOf.keySet()) {
            searchForMatches(sourceClass, getTargetMappedClasses(sourceClass, superClassOf), getTargetMappedClasses(sourceClass, subClassOf));
        }
    }

	private Set<OWLClass> getTargetMappedClasses(OWLClass sourceClass, Map<OWLClass, Set<OWLClass>> map) {
		Set<OWLClass>  mappedTargetClasses = new HashSet<OWLClass>();
		Set<OWLClass>  mappedSourceClasses = map.get(sourceClass);
		if (mappedSourceClasses != null) {
			for (OWLClass mappedSourceClass : mappedSourceClasses) {
				OWLClass targetClass = (OWLClass) diffMap.getEntityMap().get(mappedSourceClass);
				if (targetClass != null) {
					mappedTargetClasses.add(targetClass);
				}
			}
		}
		if (log.isInfoEnabled()) {
			log.info("" + sourceClass + " subclasses map to "  + mappedTargetClasses);
		}
		return mappedTargetClasses;
	}

	private void searchForMatches(OWLClass sourceClass, Set<OWLClass> possibleTargetSuperclasses, Set<OWLClass> possibleTargetSubclasses) {
    	for (OWLClass possibleTargetSuperClass : possibleTargetSuperclasses) {
            for (OWLClassExpression possibleTargetClass : EntitySearcher
                    .getSubClasses(possibleTargetSuperClass,
                            diffMap.getTargetOntology())) {
    			if (!possibleTargetClass.isAnonymous() 
    					&& searchForMatches(sourceClass, possibleTargetClass.asOWLClass(), possibleTargetSubclasses)){
    				return;
    			}
    		}
    	}
    }
    
    
    private boolean searchForMatches(OWLClass sourceClass, 
    		                         OWLClass potentialMatchingClass, 
    		                         Set<OWLClass> desiredTargetSubClasses) {
        int count = 0;
        for (OWLClassExpression targetSubclass : EntitySearcher.getSubClasses(
                potentialMatchingClass, diffMap.getTargetOntology())) {
            if (log.isInfoEnabled()) {
                log.info("\t" + targetSubclass);
            }
            if (desiredTargetSubClasses.contains(targetSubclass)) {
            	if (log.isInfoEnabled()) {
            		log.info("\tgood subclass");
            	}
                if (++count >= requiredSubclasses) {
                	if (log.isInfoEnabled()) {
                		log.info("match added");
                	}
                    newMatches.put(sourceClass, potentialMatchingClass);
                    return true;
                }
            }
            else {
            	if (log.isInfoEnabled()) {
            		log.info("\tbad subclass");
            	}
            }
        }
        return false;
    }
    
    private static class Explanation extends SimpleAlignmentExplanation {
    	private OwlDiffMap diffs;
    	private RenderingService renderer;
    	
    	public Explanation(OwlDiffMap diffs, RenderingService renderer) {
    		super("Aligned source and target entities that have a matching parent and child.");
    		this.diffs = diffs;
    		this.renderer = renderer;
    	}
    	
    	@Override
    	public boolean hasDetailedExplanation(OWLObject sourceObject) {
    		return sourceObject instanceof OWLClass;
    	}
    	
    	@Override
    	public String getDetailedExplanation(OWLObject sourceObject) {
    		OWLClass source = (OWLClass) sourceObject;
    		OWLClass target = (OWLClass) diffs.getEntityMap().get(source);
    		StringBuffer sb = new StringBuffer();
    		sb.append(getExplanation());
    		sb.append("\n\t");
    		sb.append(renderer.renderSourceObject(source));
    		sb.append("  -->   ");
    		sb.append(renderer.renderTargetObject(target));
    		sb.append("\n");
    		addMatchingParents(source, target, sb);
    		addMatchingChildren(source, target, sb);
    		return sb.toString();
    	}
    	
    	private void addMatchingParents(OWLClass source, OWLClass target, StringBuffer sb) {
            Collection<OWLClassExpression> targetParents = EntitySearcher
                    .getSuperClasses(target, diffs.getTargetOntology());
            for (OWLClassExpression sourceParent : EntitySearcher
                    .getSuperClasses(source, diffs.getSourceOntology())) {
    			OWLClass targetParent = (OWLClass) diffs.getEntityMap().get(sourceParent);
    			if (targetParent != null && targetParents.contains(targetParent)) {
    				sb.append("Source entity has parent\n\t");
    				sb.append(renderer.renderSourceObject(sourceParent));
    				sb.append("\nwhich maps to the following parent of the target entity\n\t");
    				sb.append(renderer.renderTargetObject(targetParent));
    				sb.append('\n');
    			}
    		}
    	}
    	
    	private void addMatchingChildren(OWLClass source, OWLClass target, StringBuffer sb) {
            Collection<OWLClassExpression> targetChildren = EntitySearcher
                    .getSubClasses(target, diffs.getTargetOntology());
            for (OWLClassExpression sourceChild : EntitySearcher.getSubClasses(
                    source, diffs.getSourceOntology())) {
    			OWLClass targetChild = (OWLClass) diffs.getEntityMap().get(sourceChild);
    			if (targetChild != null && targetChildren.contains(targetChild)) {
    				sb.append("Source entity has child\n\t");
    				sb.append(renderer.renderSourceObject(sourceChild));
    				sb.append("\nwhich maps to the following child of the target entity\n\t");
    				sb.append(renderer.renderTargetObject(targetChild));
    				sb.append('\n');
    			}
    		}
    	}
    	
    }

}
