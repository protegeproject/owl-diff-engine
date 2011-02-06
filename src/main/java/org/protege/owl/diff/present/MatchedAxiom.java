package org.protege.owl.diff.present;

import org.semanticweb.owlapi.model.OWLAxiom;

public class MatchedAxiom implements Comparable<MatchedAxiom> {
    public static final MatchDescription AXIOM_ADDED   = new MatchDescription("Added", MatchDescription.MAX_SEQUENCE);
    public static final MatchDescription AXIOM_DELETED = new MatchDescription("Deleted", MatchDescription.MAX_SEQUENCE);
    
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
        
        ret = description.compareTo(o.getDescription());
        if (ret != 0) {
            return ret;
        }
        
        ret = compareAxioms(sourceAxiom, o.getSourceAxiom());
        if (ret != 0) {
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
        else {
        	return 0;
        }
    }
    
    @Override
    public String toString() {
    	return description.toString() + ": " + sourceAxiom + " --> " + targetAxiom;
    }
}
