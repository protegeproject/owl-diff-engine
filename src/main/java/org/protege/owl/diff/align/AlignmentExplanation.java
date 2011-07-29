package org.protege.owl.diff.align;

import org.semanticweb.owlapi.model.OWLObject;

public interface AlignmentExplanation {
	String getExplanation();
	
	String getDetailedExplanation(OWLObject sourceObject);
}
