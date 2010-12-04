package org.protege.owl.diff.raw;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.OWLEntityCollector;

public class UnmatchedAxiom {    
    private Collection<OWLEntity> referencedUnmatchedEntities;
    private Collection<OWLAnonymousIndividual> referencedUnmatchedAnonymousIndividuals;
    private OWLAxiom axiom;
    
    public UnmatchedAxiom(OWLAxiom axiom) {
        this.axiom = axiom;
        OWLEntityCollector collector = new OWLEntityCollector();
        axiom.accept(collector);
        referencedUnmatchedEntities = collector.getObjects();
        referencedUnmatchedAnonymousIndividuals = collector.getAnonymousIndividuals();
    }

    public Collection<OWLEntity> getReferencedUnmatchedEntities() {
        return Collections.unmodifiableCollection(referencedUnmatchedEntities);
    }

    public Collection<OWLAnonymousIndividual> getReferencedUnmatchedAnonymousIndividuals() {
        return Collections.unmodifiableCollection(referencedUnmatchedAnonymousIndividuals);
    }
    
    public OWLAxiom getAxiom() {
        return axiom;
    }

    public OWLObject getLeadingUnmatchedReference() {
        if (!referencedUnmatchedEntities.isEmpty()) {
            return referencedUnmatchedEntities.iterator().next();
        }
        else {
            return referencedUnmatchedAnonymousIndividuals.iterator().next();
        }
    }
    
    public void trim(OwlDiffMap diffMap) {
        Iterator<OWLEntity> referencedIt = referencedUnmatchedEntities.iterator();
        while (referencedIt.hasNext()) {
            OWLEntity referencedEntity = referencedIt.next();
            if (!diffMap.getUnmatchedSourceEntities().contains(referencedEntity)) {
                referencedIt.remove();
            }
        }
        Iterator<OWLAnonymousIndividual> referencedIndIt = referencedUnmatchedAnonymousIndividuals.iterator();
        while (referencedIndIt.hasNext()) {
            OWLAnonymousIndividual referencedIndividual = referencedIndIt.next();
            if (!diffMap.getUnmatchedSourceAnonymousIndividuals().contains(referencedIndividual)) {
                referencedIndIt.remove();
            }
        }
    }
    
    public String toString() {
        return "{unref = " + referencedUnmatchedEntities.size() + " - " + axiom + "}";
    }
    
}
