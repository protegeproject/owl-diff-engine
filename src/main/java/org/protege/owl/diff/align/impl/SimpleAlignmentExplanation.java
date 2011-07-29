package org.protege.owl.diff.align.impl;

import org.protege.owl.diff.align.AlignmentExplanation;
import org.semanticweb.owlapi.model.OWLObject;

public class SimpleAlignmentExplanation implements AlignmentExplanation {
	private String explanation;
	
	public SimpleAlignmentExplanation(String explanation) {
		this.explanation = explanation;
	}

	@Override
	public String getExplanation() {
		return explanation;
	}
	
	@Override
	public boolean hasDetailedExplanation(OWLObject sourceObject) {
		return false;
	}

	@Override
	public String getDetailedExplanation(OWLObject sourceObject) {
		return null;
	}
	
	@Override
	public String toString() {
		return "<Why? " + explanation + ">";
	}

}
