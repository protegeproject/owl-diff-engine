package org.protege.owl.diff.align.algorithms;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.AlignmentListener;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.UnmatchedAxiom;
import org.protege.owl.diff.align.util.AlignmentAlgorithmComparator;
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
	
	private AlignmentListener listener = new AlignmentListener() {
		
		public void unmatchedAxiomMoved(UnmatchedAxiom unmatched) {
		}
		
		public void addUnmatcheableAxiom(OWLAxiom axiom) {
		}
		
		public void addMatchingEntities(Map<OWLEntity, OWLEntity> newMatches) {
			for (Entry<OWLEntity, OWLEntity> entry : newMatches.entrySet()) {
				OWLEntity source = entry.getKey();
				OWLEntity target = entry.getValue();
				addMatch(source, target);
			}
		}
		
		public void addMatchingAnonymousIndividuals(Map<OWLAnonymousIndividual, OWLAnonymousIndividual> newMatches) {

		}
		
		public void addMatchedAxiom(OWLAxiom axiom) {
		}
		
		public void addMatch(OWLAnonymousIndividual source, OWLAnonymousIndividual target) {
		}
		
		public void addMatch(OWLEntity source, OWLEntity target) {
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
	
	private void checkSiblings(OWLClass sourceSibling) {
		for (OWLClass sourceParent : getSiblingService().getSourceSuperClasses(sourceSibling)) {
			OWLClass targetParent = (OWLClass) diffs.getEntityMap().get(sourceParent);
			if (targetParent != null) {
				checkChildren(sourceParent, targetParent);
			}
		}
	}
	
	private void checkChildren(OWLClass sourceParent, OWLClass targetParent) {
		Set<OWLClass> sourceChildren = getSiblingService().getSubClasses(sourceParent, true);
		Set<OWLClass> targetChildren = getSiblingService().getSubClasses(targetParent, false);
		checkSiblings(filterMatchedSiblings(sourceChildren, true), filterMatchedSiblings(targetChildren, false));
	}
	
	private Set<OWLClass> filterMatchedSiblings(Set<OWLClass> siblings, boolean isSourceOntology) {
		Set<OWLClass> unmatchedSiblings = new TreeSet<OWLClass>();
		Set<OWLEntity> unmatched = isSourceOntology ? diffs.getUnmatchedSourceEntities() : diffs.getUnmatchedTargetEntities();
		for (OWLClass sibling : siblings) {
			if (unmatched.contains(sibling)) {
				unmatchedSiblings.add(sibling);
			}
		}
		return unmatchedSiblings;
	}

	
	public void reset() {
		alreadyRun = false;
	}

	
	/*
	 * This is not reliable and it is also slow.
	 */
	public int getPriority() {
		return AlignmentAlgorithmComparator.MIN_PRIORITY;
	}

}
