package org.protege.owl.diff.align.algorithms;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.AlignmentListener;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.UnmatchedAxiom;
import org.protege.owl.diff.align.util.AlignmentAlgorithmComparator;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

public class MatchLoneSiblings implements AlignmentAlgorithm {
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
	private OwlDiffMap diffs;
	
	public void initialise(Engine e) {
		diffs = e.getOwlDiffMap();
	}

	public void run() {
		for (OWLClass sourceParent : diffs.getSourceOntology().getClassesInSignature()) {
			OWLClass targetParent = (OWLClass) diffs.getEntityMap().get(sourceParent);
			if (targetParent != null) {
				checkChildren(sourceParent, targetParent);
			}
		}
		diffs.addDiffListener(listener);
	}
	
	private void checkSiblings(OWLClass sourceSibling) {
		for (OWLClass sourceParent : getSourceSuperClasses(sourceSibling)) {
			OWLClass targetParent = (OWLClass) diffs.getEntityMap().get(sourceParent);
			checkChildren(sourceParent, targetParent);
		}
	}
	
	private void checkChildren(OWLClass sourceParent, OWLClass targetParent) {
		Set<OWLClass> sourceChildren = getSubClasses(sourceParent, true);
		Set<OWLClass> targetChildren = getSubClasses(targetParent, false);
		if (sourceChildren.size() != targetChildren.size()) {
			return;
		}
		OWLClass loneUnmatchedSourceChild = null;
		for (OWLClass checkSourceChild : sourceChildren) {
			OWLClass checkTargetChild = (OWLClass) diffs.getEntityMap().get(checkSourceChild);
			if (checkTargetChild == null && loneUnmatchedSourceChild != null) { // not a lone unmatched
				return;
			}
			else if (checkTargetChild == null) { // is this the lone matched?
				loneUnmatchedSourceChild = checkSourceChild;
			}
			else if (!targetChildren.contains(checkTargetChild)) { // some source children don't map to target children
				return;
			}
			else {
				targetChildren.remove(checkTargetChild);
			}
		}
		if (targetChildren.size() == 1) {
			diffs.addMatch(loneUnmatchedSourceChild, targetChildren.iterator().next());
		}
	}
	
	
	private Set<OWLClass> getSourceSuperClasses(OWLClass c) {
		Set<OWLClass> superClasses = new HashSet<OWLClass>();
		for (OWLClassExpression ce : c.getSuperClasses(diffs.getSourceOntology())) {
			if (!ce.isAnonymous()) {
				superClasses.add(ce.asOWLClass());
			}
		}
		return superClasses;
	}
	
	
	private Set<OWLClass> getSubClasses(OWLClass c, boolean isSourceOntology) {
		Set<OWLClass> subClasses = new HashSet<OWLClass>();
		for (OWLClassExpression ce : c.getSubClasses(getOntology(isSourceOntology))) {
			if (!ce.isAnonymous()) {
				subClasses.add(ce.asOWLClass());
			}
		}
		return subClasses;
	}
	
	private OWLOntology getOntology(boolean isSourceOntology) {
		return isSourceOntology ? diffs.getSourceOntology() : diffs.getTargetOntology();
	}

	public void reset() {

	}

	public int getPriority() {
		return AlignmentAlgorithmComparator.DEFAULT_PRIORITY;
	}

	public String getAlgorithmName() {
		return "Match Lone Siblings";
	}

}
