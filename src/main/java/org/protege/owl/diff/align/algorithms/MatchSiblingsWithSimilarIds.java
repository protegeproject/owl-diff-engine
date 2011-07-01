package org.protege.owl.diff.align.algorithms;

import org.protege.owl.diff.DifferencePosition;
import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

public class MatchSiblingsWithSimilarIds extends AbstractApproximateSiblingMatch {
	private ShortFormProvider shortFormProvider;

	public MatchSiblingsWithSimilarIds() {
		super();
		shortFormProvider = new SimpleShortFormProvider();
	}
	
	public String getBrowserText(OWLClass cls, DifferencePosition position) {
		return shortFormProvider.getShortForm(cls);
	}


	public String getAlgorithmName() {
		return "Match Siblings with approximately similar ids";
	}
	
    public AlignmentAggressiveness getAggressiveness() {
    	return AlignmentAggressiveness.CONSERVATIVE;
    }

}
