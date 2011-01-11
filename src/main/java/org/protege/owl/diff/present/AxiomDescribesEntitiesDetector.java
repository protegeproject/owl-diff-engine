package org.protege.owl.diff.present;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

public interface AxiomDescribesEntitiesDetector {

	Collection<OWLEntity> getSources(OWLAxiom axiom);
}
