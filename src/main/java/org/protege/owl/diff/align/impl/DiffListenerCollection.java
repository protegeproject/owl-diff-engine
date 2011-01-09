package org.protege.owl.diff.align.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.protege.owl.diff.align.AlignmentListener;
import org.protege.owl.diff.align.UnmatchedAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

public class DiffListenerCollection {
    private Collection<AlignmentListener> listeners = new HashSet<AlignmentListener>();
    
    public void addDiffListener(AlignmentListener listener) {
        listeners.add(listener);
    }
    
    public void removeDiffListener(AlignmentListener listener) {
        listeners.remove(listener);
    }


    protected void fireAddMatchingEntities(Map<OWLEntity, OWLEntity> newMatches) {
        for (AlignmentListener listener : listeners) {
            listener.addMatchingEntities(newMatches);
        }
    }

    protected void fireAddMatchingAnonymousIndividuals(Map<OWLAnonymousIndividual, OWLAnonymousIndividual> newMatches){
        for (AlignmentListener listener : listeners) {
            listener.addMatchingAnonymousIndividuals(newMatches);
        }
    }
    protected void fireAddMatch(OWLEntity source, OWLEntity target){
        for (AlignmentListener listener : listeners) {
            listener.addMatch(source, target);
        }
    }
    protected void fireAddMatch(OWLAnonymousIndividual source, OWLAnonymousIndividual target){
        for (AlignmentListener listener : listeners) {
            listener.addMatch(source, target);
        }
    }
    
    protected void fireUnmatchedAxiomMoved(UnmatchedAxiom unmatched) {
        for (AlignmentListener listener : listeners) {
            listener.unmatchedAxiomMoved(unmatched);
        }
    }
    
    protected void fireAddUnmatcheableAxiom(OWLAxiom axiom) {
        for (AlignmentListener listener : listeners) {
            listener.addUnmatcheableAxiom(axiom);
        }
    }

    protected void fireAddMatchedAxiom(OWLAxiom axiom) {
        for (AlignmentListener listener : listeners) {
            listener.addMatchedAxiom(axiom);
        }
    }


}
