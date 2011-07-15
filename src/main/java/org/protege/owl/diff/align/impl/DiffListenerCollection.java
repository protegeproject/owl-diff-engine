package org.protege.owl.diff.align.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.protege.owl.diff.align.AlignmentListener;
import org.protege.owl.diff.align.UnmatchedSourceAxiom;
import org.protege.owl.diff.align.util.PrioritizedComparator;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

public class DiffListenerCollection {
    private List<AlignmentListener> listeners = new ArrayList<AlignmentListener>();
    
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
    
    protected void fireUnmatchedAxiomMoved(UnmatchedSourceAxiom unmatched) {
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
