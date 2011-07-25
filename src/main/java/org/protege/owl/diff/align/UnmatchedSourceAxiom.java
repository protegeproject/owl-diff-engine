package org.protege.owl.diff.align;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

public interface UnmatchedSourceAxiom {    

	public Collection<OWLEntity> getReferencedUnmatchedEntities();
	public Collection<OWLAnonymousIndividual> getReferencedUnmatchedAnonymousIndividuals();
	
	public OWLAxiom getAxiom();
}
