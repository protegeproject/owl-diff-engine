package org.protege.owl.diff.align.algorithms;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

public class MatchSiblingsWithSimilarIds extends AbstractApproximateSiblingMatch {
	private ShortFormProvider shortFormProvider;

	public MatchSiblingsWithSimilarIds() {
		super();
		shortFormProvider = new SimpleShortFormProvider();
	}
	
	public String getBrowserText(OWLClass cls, boolean isSourceOntology) {
		return shortFormProvider.getShortForm(cls);
	}


	public String getAlgorithmName() {
		return "Match Siblings with approximately similar ids";
	}

}
