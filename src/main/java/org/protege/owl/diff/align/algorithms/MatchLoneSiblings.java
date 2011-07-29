package org.protege.owl.diff.align.algorithms;

import java.util.Set;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.align.AlignmentExplanation;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.util.PrioritizedComparator;
import org.protege.owl.diff.service.RenderingService;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;

public class MatchLoneSiblings extends AbstractSiblingMatch {
	private RenderingService renderer;
	
	@Override
	public void initialise(Engine e) {
		super.initialise(e);
		renderer = RenderingService.get(e);
	}
	
	protected void checkSiblings(OWLClass sourceParent, Set<OWLClass> unmatchedSourceSiblings, 
								 OWLClass targetParent, Set<OWLClass> unmatchedTargetSiblings) {
		if (unmatchedSourceSiblings.size() == 1 && unmatchedTargetSiblings.size() == 1) {
			OWLClass sourceSibling = unmatchedSourceSiblings.iterator().next();
			OWLClass targetSibling = unmatchedTargetSiblings.iterator().next();
			getOwlDiffMap().addMatch(sourceSibling, targetSibling,
					                 new Explanation(sourceSibling, sourceParent));
		}
	}

	/*
	 * This is somewhat expensive to run early but I think I trust the results.
	 */
	public int getPriority() {
		return PrioritizedComparator.MIN_PRIORITY;
	}
	
    public AlignmentAggressiveness getAggressiveness() {
    	return AlignmentAggressiveness.AGGRESSIVE_SEARCH;
    }

	public String getAlgorithmName() {
		return "Match Lone Siblings";
	}
	
	private class Explanation implements AlignmentExplanation {
		private OWLClass sourceClass;
		private OWLClass sourceParent;
		
		public Explanation(OWLClass sourceClass, OWLClass sourceParent) {
			this.sourceClass = sourceClass;
			this.sourceParent = sourceParent;
		}
		
		@Override
		public String getExplanation() {
			return "Aligned source and target entities that have a matching parent and child.";
		}
		
		@Override
		public String getDetailedExplanation(OWLObject sourceObject) {
			OwlDiffMap diffMap = getOwlDiffMap();
			OWLClass targetClass = (OWLClass) diffMap.getEntityMap().get(sourceClass);
			OWLClass targetParent = (OWLClass) diffMap.getEntityMap().get(sourceParent);
			StringBuffer sb = new StringBuffer();
			sb.append("I matched the source class, \n\t");
			sb.append(renderer.renderSourceObject(sourceObject));
			sb.append(",\nwith the target class\n\t");
			sb.append(renderer.renderTargetObject(targetClass));
			sb.append(",\nbecause the source class is the only unmatched child of\n\t");
			sb.append(renderer.renderSourceObject(sourceParent));
			sb.append(",\nbecause the target class is the only unmatched child of\n\t");
			sb.append(renderer.renderTargetObject(targetParent));
			sb.append(",\nand the two parents match.  More explanation will follow in a future version.");
			return sb.toString();
		}
	}

}
