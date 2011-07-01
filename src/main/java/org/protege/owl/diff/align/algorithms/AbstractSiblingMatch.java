package org.protege.owl.diff.align.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.protege.owl.diff.DifferencePosition;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.AlignmentListener;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.UnmatchedAxiom;
import org.protege.owl.diff.align.util.PrioritizedComparator;
import org.protege.owl.diff.service.SiblingService;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;

public abstract class AbstractSiblingMatch implements AlignmentAlgorithm {
	private boolean alreadyRun = false;
	private Engine e;
	private OwlDiffMap diffs;
	private SiblingService siblingService;
	private Set<OWLClass> examinedSourceParents = new HashSet<OWLClass>();
	
	private AlignmentListener listener = new AlignmentListener() {
		
		public void unmatchedAxiomMoved(UnmatchedAxiom unmatched) {
		}
		
		public void addUnmatcheableAxiom(OWLAxiom axiom) {
		}
		
		public void addMatchingEntities(Map<OWLEntity, OWLEntity> newMatches) {
			startSearch();
			for (Entry<OWLEntity, OWLEntity> entry : newMatches.entrySet()) {
				OWLEntity source = entry.getKey();
				OWLEntity target = entry.getValue();
				addMatchInternal(source, target);
			}
		}
		
		public void addMatchingAnonymousIndividuals(Map<OWLAnonymousIndividual, OWLAnonymousIndividual> newMatches) {

		}
		
		public void addMatchedAxiom(OWLAxiom axiom) {
		}
		
		public void addMatch(OWLAnonymousIndividual source, OWLAnonymousIndividual target) {
		}
		
		public void addMatch(OWLEntity source, OWLEntity target) {
			startSearch();
			addMatchInternal(source, target);
		}
		
		private void addMatchInternal(OWLEntity source, OWLEntity target) {
			if (source instanceof OWLClass && target instanceof OWLClass) {
				checkChildren((OWLClass) source, (OWLClass) target);
				checkSiblings((OWLClass) source);
			}			
		}
	};
	
	public abstract String getAlgorithmName();
	protected abstract void checkSiblings(Set<OWLClass> unmatchedSourceSiblings, Set<OWLClass> unmatchedTargetSiblings);
	
	public void initialise(Engine e) {
		this.e = e;
		diffs = e.getOwlDiffMap();
	}


	public void run() {
		if (alreadyRun) {
			return;
		}
		diffs.announce(this);
		try {
			startSearch();
			for (OWLEntity unmatchedSource : new ArrayList<OWLEntity>(diffs.getUnmatchedSourceEntities())) {
				if (unmatchedSource instanceof OWLClass) {
					checkSiblings((OWLClass) unmatchedSource);
				}
			}
			diffs.addDiffListener(listener);
		} finally {
			diffs.summarize();
			alreadyRun = true;
		}
	}
	
	private SiblingService getSiblingService() {
		if (siblingService == null) {
			siblingService = SiblingService.get(e);
		}
		return siblingService;
	}
	
	protected OwlDiffMap getOwlDiffMap() {
		return diffs;
	}
	
	private void startSearch() {
		examinedSourceParents.clear();
	}
	
	private void checkSiblings(OWLClass sourceSibling) {
		for (OWLClass sourceParent : getSiblingService().getSourceSuperClasses(sourceSibling)) {
			OWLClass targetParent = (OWLClass) diffs.getEntityMap().get(sourceParent);
			if (targetParent != null) {
				checkChildren(sourceParent, targetParent);
			}
		}
	}
	
	private void checkChildren(OWLClass sourceParent, OWLClass targetParent) {
		if (!examinedSourceParents.contains(sourceParent)) {
			examinedSourceParents.add(sourceParent);
			Set<OWLClass> sourceChildren = getSiblingService().getSubClasses(sourceParent, DifferencePosition.SOURCE);
			Set<OWLClass> targetChildren = getSiblingService().getSubClasses(targetParent, DifferencePosition.TARGET);
			checkSiblings(filterMatchedSiblings(sourceChildren, DifferencePosition.SOURCE), 
					      filterMatchedSiblings(targetChildren, DifferencePosition.TARGET));
		}
	}
	
	private Set<OWLClass> filterMatchedSiblings(Set<OWLClass> siblings, DifferencePosition position) {
		Set<OWLClass> unmatchedSiblings = new TreeSet<OWLClass>();
		Set<OWLEntity> unmatched = position.getUnmatchedEntities(diffs);
		for (OWLClass sibling : siblings) {
			if (unmatched.contains(sibling)) {
				unmatchedSiblings.add(sibling);
			}
		}
		return unmatchedSiblings;
	}

	
	public void reset() {
		alreadyRun = false;
		examinedSourceParents.clear();
	}

	
	/*
	 * This is not reliable and it is also slow.
	 */
	public int getPriority() {
		return PrioritizedComparator.MIN_PRIORITY;
	}

}
