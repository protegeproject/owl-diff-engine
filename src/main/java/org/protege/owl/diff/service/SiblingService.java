package org.protege.owl.diff.service;

import java.util.HashSet;
import java.util.Set;

import org.protege.owl.diff.DifferencePosition;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.OwlDiffMap;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * This class provides a trivial service - it gets subclasses and superclasses.  It is here as a marker
 * for the time when we might add some inference to this class.  In particular, one might expect to see the 
 * CheeseyPizza inference and an inference that classes without a parent are subclasses of OWL thing.  Adding
 * these capabilities will make this services more expensive so getting this service needs to be deferred until
 * it is known that it is needed.
 */

public class SiblingService {
	private OwlDiffMap diffs;
	
	
	public static SiblingService get(Engine e) {
		SiblingService s = e.getService(SiblingService.class);
		if (s == null) {
			s = new SiblingService(e);
			e.addService(s);
		}
		return s;
	}
	
	private SiblingService(Engine e) {
		diffs = e.getOwlDiffMap();
	}
	
	
	public Set<OWLClass> getSourceSuperClasses(OWLClass c) {
		Set<OWLClass> superClasses = new HashSet<OWLClass>();
		for (OWLClassExpression ce : c.getSuperClasses(diffs.getSourceOntology())) {
			if (!ce.isAnonymous()) {
				superClasses.add(ce.asOWLClass());
			}
		}
		return superClasses;
	}
	
	
	public Set<OWLClass> getSubClasses(OWLClass c, DifferencePosition position) {
		Set<OWLClass> subClasses = new HashSet<OWLClass>();
		for (OWLClassExpression ce : c.getSubClasses(position.getOntology(diffs))) {
			if (!ce.isAnonymous()) {
				subClasses.add(ce.asOWLClass());
			}
		}
		return subClasses;
	}
	
}
