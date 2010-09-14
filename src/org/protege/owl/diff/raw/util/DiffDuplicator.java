package org.protege.owl.diff.raw.util;

import java.util.Map;

import org.protege.owl.diff.raw.OwlDiffMap;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

public class DiffDuplicator extends OWLObjectDuplicator {
    private OwlDiffMap diffs;
    
    public DiffDuplicator(OwlDiffMap diffs) {
        super(diffs.getOWLDataFactory());
        this.diffs = diffs;
    }
    
    @Override
    public void visit(OWLClass cls) {
        handleEntity(cls);
    }
    
    @Override
    public void  visit(OWLObjectProperty property) {
        handleEntity(property);
    }
    
    @Override
    public void  visit(OWLDataProperty property) {
        handleEntity(property);
    }
    
    @Override
    public void visit(OWLAnnotationProperty property) {
        handleEntity(property);
    }
    
    @Override
    public void  visit(OWLNamedIndividual property) {
        handleEntity(property);
    }
    
    @Override
    public void  visit(OWLDatatype datatype) {
        setLastObject(datatype);
    }
    
    @Override
    public void visit(OWLAnonymousIndividual individual) {
        Map<OWLAnonymousIndividual, OWLAnonymousIndividual> individualMap = diffs.getAnonymousIndividualMap();
        if (!individualMap.containsKey(individual)) {
            throw new UnmappedEntityException("Entity map should contain all referenced anonymous individuals");
        }
        setLastObject(individualMap.get(individual));
    }
    
    public void visit(IRI iri) {
        OWLDataFactory factory = diffs.getOWLDataFactory();
        OWLClass cls = factory.getOWLClass(iri);
        Map<OWLEntity, OWLEntity> entityMap = diffs.getEntityMap();
        if (entityMap.containsKey(cls)) {
            setLastObject(entityMap.get(cls).getIRI());
            return;
        }
        OWLObjectProperty op = factory.getOWLObjectProperty(iri);
        if (entityMap.containsKey(op)) {
            setLastObject(entityMap.get(op).getIRI());
            return;
        }
        OWLDataProperty dp = factory.getOWLDataProperty(iri);
        if (entityMap.containsKey(dp)) {
            setLastObject(entityMap.get(dp).getIRI());
            return;
        }
        OWLNamedIndividual i = factory.getOWLNamedIndividual(iri);
        if (entityMap.containsKey(i)) {
            setLastObject(entityMap.get(i).getIRI());
            return;
        }
        setLastObject(iri);
    }

    private void handleEntity(OWLEntity entity) {
        Map<OWLEntity, OWLEntity> entityMap = diffs.getEntityMap();
        if (!entityMap.containsKey(entity)) {
            throw new UnmappedEntityException("Entity map should contain all referenced entities");
        }
        setLastObject(entityMap.get(entity));
    }
}
