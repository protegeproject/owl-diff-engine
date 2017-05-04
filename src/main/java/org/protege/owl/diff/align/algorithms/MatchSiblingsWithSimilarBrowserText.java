package org.protege.owl.diff.align.algorithms;

import java.util.HashMap;
import java.util.Map;

import org.protege.owl.diff.DifferencePosition;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.align.AlignmentExplanation;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.impl.SimpleAlignmentExplanation;
import org.protege.owl.diff.service.RenderingService;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

public class MatchSiblingsWithSimilarBrowserText extends AbstractApproximateSiblingMatch {
	private RenderingService renderer;
	private Explain explanation;

	public MatchSiblingsWithSimilarBrowserText() {
		super();
	}

	public void initialize(Engine e) {
		super.initialize(e);
		renderer = RenderingService.get(e);
		explanation = new Explain(e);
	}
	
	public String getBrowserText(OWLClass cls, DifferencePosition position) {
		switch (position) {
		case SOURCE:
			return renderer.renderSourceObject(cls);
		case TARGET:
			return renderer.renderTargetObject(cls);
		default:
			throw new IllegalStateException("Shouldn't get here");	
		}
	}


	public String getAlgorithmName() {
		return "Match Siblings with approximately similar renderings";
	}
	
	protected AlignmentExplanation getExplanation() {
		return explanation;
	}
	
    public AlignmentAggressiveness getAggressiveness() {
    	return AlignmentAggressiveness.MODERATE;
    }
    
	protected boolean checkMatch(Map<OWLEntity, OWLEntity> newMatches, 
            				  OWLClass sourceParent, OWLClass unmatchedSourceSibling, 
            				  OWLClass targetParent, OWLClass unmatchedTargetSibling) {
		if (super.checkMatch(newMatches, sourceParent, unmatchedSourceSibling, targetParent, unmatchedTargetSibling)) {
			explanation.addSourceChildToParentMapping(unmatchedSourceSibling, sourceParent);
			return true;
		}
		else {
			return false;
		}
	}
	
	private static class Explain extends SimpleAlignmentExplanation {
		private OwlDiffMap diffMap;
		private RenderingService renderer;
		private Map<OWLClass, OWLClass> sourceSiblingToParentMap = new HashMap<OWLClass, OWLClass>();
		
		public Explain(Engine e) {
			super("Entities matched up because their parents matched and they have similar renderings.");
			this.diffMap = e.getOwlDiffMap();
			this.renderer = RenderingService.get(e);
		}
		
		@Override
		public boolean hasDetailedExplanation(OWLObject sourceObject) {
			return sourceSiblingToParentMap.containsKey(sourceObject);
		}
		
		@Override
		public String getDetailedExplanation(OWLObject sourceObject) {
			OWLClass targetObject = (OWLClass) diffMap.getEntityMap().get(sourceObject);
			OWLClass sourceParent = sourceSiblingToParentMap.get(sourceObject);
			OWLClass targetParent = (OWLClass) diffMap.getEntityMap().get(sourceParent);
			StringBuffer sb = new StringBuffer();
			sb.append("I matched the source class, \n\t");
			sb.append(renderer.renderSourceObject(sourceObject));
			sb.append(",\nwith the target class\n\t");
			sb.append(renderer.renderTargetObject(targetObject));
			sb.append(",\nbecause I judged their renderings to be similar and because the parent of the\n");
			sb.append("source class,\n\t");
			sb.append(renderer.renderSourceObject(sourceParent));
			sb.append(",\nmapped to a parent,\n\t");
			sb.append(renderer.renderTargetObject(targetParent));
			sb.append(",\nof the target object.");
			return sb.toString();
		}
		
		public void addSourceChildToParentMapping(OWLClass child, OWLClass parent) {
			sourceSiblingToParentMap.put(child, parent);
		}
		
	}

}
