package org.protege.owl.diff.align;

import org.semanticweb.owlapi.model.OWLObject;

public interface AlignmentExplanation {
	
	String getExplanation();
	
	boolean hasDetailedExplanation(OWLObject sourceObject);
	
	String getDetailedExplanation(OWLObject sourceObject);
}
