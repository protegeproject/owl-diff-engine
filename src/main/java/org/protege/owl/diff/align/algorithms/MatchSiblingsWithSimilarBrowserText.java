package org.protege.owl.diff.align.algorithms;

import org.protege.owl.diff.DifferencePosition;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.service.RenderingService;
import org.semanticweb.owlapi.model.OWLClass;

public class MatchSiblingsWithSimilarBrowserText extends AbstractApproximateSiblingMatch {
	private RenderingService renderer;

	public MatchSiblingsWithSimilarBrowserText() {
		super();
	}
	

	public void initialise(Engine e) {
		super.initialise(e);
		renderer = RenderingService.get(e);
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
	
	protected String getExplanation() {
		return "Entities matched up because their parents matched and they have similar renderings.";
	}
	
    public AlignmentAggressiveness getAggressiveness() {
    	return AlignmentAggressiveness.CONSERVATIVE;
    }

}
