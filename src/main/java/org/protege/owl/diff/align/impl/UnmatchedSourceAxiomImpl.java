package org.protege.owl.diff.align.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.UnmatchedSourceAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.OWLEntityCollector;

public class UnmatchedSourceAxiomImpl implements UnmatchedSourceAxiom {
    private Set<OWLEntity> referencedUnmatchedEntities;
    private Collection<OWLAnonymousIndividual> referencedUnmatchedAnonymousIndividuals;
    private OWLAxiom axiom;
    
    public UnmatchedSourceAxiomImpl(OWLAxiom axiom) {
        this.axiom = axiom;
        referencedUnmatchedEntities = new HashSet<OWLEntity>();
        referencedUnmatchedAnonymousIndividuals = new HashSet<OWLAnonymousIndividual>();
        OWLEntityCollector collector = new OWLEntityCollector(referencedUnmatchedEntities, referencedUnmatchedAnonymousIndividuals);
        axiom.accept(collector);
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
