package org.protege.owl.prompt2.analyzer;

import org.semanticweb.owlapi.model.OWLAxiom;

public class MatchedAxiom implements Comparable<MatchedAxiom> {
    public static MatchDescription AXIOM_ADDED   = new MatchDescription("Added Axiom", MatchDescription.MAX_SEQUENCE);
    public static MatchDescription AXIOM_DELETED = new MatchDescription("Deleted Axiom", MatchDescription.MAX_SEQUENCE);
    
    private OWLAxiom sourceAxiom;
    private OWLAxiom targetAxiom;
    private MatchDescription description;
    private boolean isFinal = false;
    
    public MatchedAxiom(OWLAxiom sourceAxiom, OWLAxiom targetAxiom, MatchDescription description) {
        this.sourceAxiom = sourceAxiom;
        this.targetAxiom = targetAxiom;
        this.description = description;
    }
    
    public OWLAxiom getSourceAxiom() {
        return sourceAxiom;
    }
    
    public OWLAxiom getTargetAxiom() {
        return targetAxiom;
    }
    
    public MatchDescription getDescription() {
        return description;
    }
    
    public boolean isFinal() {
        return isFinal;
    }
    
    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }
    
    public int compareTo(MatchedAxiom o) {
        int ret;
        
        if ((ret = description.compareTo(o.getDescription())) != 0) {
            return ret;
        }
        if ((ret = compareAxioms(sourceAxiom, o.getSourceAxiom())) != 0) {
            return ret;
        }
        return compareAxioms(targetAxiom, o.getTargetAxiom());
    }
    
    private int compareAxioms(OWLAxiom ax1, OWLAxiom ax2) {
        if (ax1 != null && ax2 != null) {
            return ax1.compareTo(ax2);
        }
        else if (ax1 != null && ax2 == null) {
            return +1;
        }
        else if (ax1 == null && ax2 != null) {
            return -1;
        }
        else return 0;
    }
}
