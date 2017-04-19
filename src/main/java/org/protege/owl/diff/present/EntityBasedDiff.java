package org.protege.owl.diff.present;

import java.util.SortedSet;
import java.util.TreeSet;

import org.protege.owl.diff.present.algorithms.IdentifyRenameOperation;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;


public class EntityBasedDiff implements Comparable<EntityBasedDiff> {
    public enum DiffType {
        EQUIVALENT("Unchanged"), CREATED("Created"), DELETED("Deleted"), RENAMED("Renamed"), RENAMED_AND_MODIFIED("Renamed and Modified"), MODIFIED("Modified");
        
        private String description;
        
        private DiffType(String description) {
        	this.description = description;
        }
        
        public String getDescription() {
			return description;
		}
    }
    
    private OWLEntity sourceEntity;
    private OWLEntity targetEntity;
    private SortedSet<MatchedAxiom> axiomMatches = new TreeSet<>();
    private String diffTypeDescription;
    
    public OWLEntity getSourceEntity() {
        return sourceEntity;
    }
    
    public void setSourceEntity(OWLEntity sourceEntity) {
        this.sourceEntity = sourceEntity;
    }
    
    public OWLEntity getTargetEntity() {
        return targetEntity;
    }
    
    public void setTargetEntity(OWLEntity targetEntity) {
        this.targetEntity = targetEntity;
    }
    
    public DiffType getDiffType() {
        if (sourceEntity == null) {
            return DiffType.CREATED;
        }
        else if (targetEntity == null) {
            return DiffType.DELETED;
        }
        else if (isPureRename()) {
        	return DiffType.RENAMED;
        }
        else if (!sourceEntity.equals(targetEntity)) {
            return DiffType.RENAMED_AND_MODIFIED;
        }
        else if (!axiomMatches.isEmpty()) {
            return DiffType.MODIFIED;
        }
        else {
            return DiffType.EQUIVALENT;
        }
    }
    
    private boolean isPureRename() {
    	boolean pureRenameIdentified = 
    		axiomMatches.size() == 1 
    			&& axiomMatches.iterator().next().getDescription()
    					.equals(IdentifyRenameOperation.RENAMED_CHANGE_DESCRIPTION);
    	return !sourceEntity.equals(targetEntity) && (axiomMatches.isEmpty() || pureRenameIdentified);
    }
    
    public String getDiffTypeDescription() {
		return diffTypeDescription == null ? getDiffType().getDescription() : diffTypeDescription;
	}
    
    public void setDiffTypeDescription(String diffTypeDescription) {
		this.diffTypeDescription = diffTypeDescription;
	}

    public SortedSet<MatchedAxiom> getAxiomMatches() {
        return axiomMatches;
    }
    
    /* package */ void addMatch(MatchedAxiom match) {
        axiomMatches.add(match);
    }
    
    /* package */ void removeMatch(MatchedAxiom match) {
        axiomMatches.remove(match);
    }
    
    public String getShortDescription() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getDiffTypeDescription());
        buffer.append(" ");
        switch (getDiffType()) {
        case CREATED:
            buffer.append(renderObject(targetEntity));
            break;
        case DELETED:
            buffer.append(renderObject(sourceEntity));
            break;
        case EQUIVALENT:
            buffer.append(renderObject(targetEntity));
            break;
        case RENAMED:
            buffer.append(renderObject(sourceEntity));
            buffer.append(" -> ");
            buffer.append(renderObject(targetEntity));
            break;
        case MODIFIED:
        case RENAMED_AND_MODIFIED:
            if (!sourceEntity.getIRI().equals(targetEntity.getIRI())) {
            	buffer.append("and Renamed ");
            }
            buffer.append(renderObject(sourceEntity));
            buffer.append(" -> ");
            buffer.append(renderObject(targetEntity));
            break;
        default:
            throw new UnsupportedOperationException("Programmer error");
        }
        return buffer.toString();
    }
    
    public String getDescription() {
        StringBuilder buffer = new StringBuilder(getShortDescription());
        buffer.append("\n------------------------------------------------------\n");
        for (MatchedAxiom match : axiomMatches) {
            buffer.append(match.getDescription().getDescription());
            buffer.append(": ");
            if (match.getSourceAxiom() == null) {
                buffer.append("\t");
                buffer.append(renderObject(match.getTargetAxiom()));
            }
            else if (match.getTargetAxiom() == null) {
                buffer.append("\t");
                buffer.append(renderObject(match.getSourceAxiom()));
            }
            else {
                buffer.append("\t");
                buffer.append(renderObject(match.getSourceAxiom()));
                buffer.append("\n\t\t-->\n\t");
                buffer.append(renderObject(match.getTargetAxiom()));
            }
            buffer.append('\n');
        }
        if (!axiomMatches.isEmpty()) {
        	buffer.append("------------------------------------------------------\n");
        }
        return buffer.toString();
    }
    
    protected String renderObject(OWLObject o) {
    	if (o instanceof OWLAnnotationAssertionAxiom && ((OWLAnnotationAssertionAxiom) o).getSubject() instanceof IRI) {
    	    SimpleIRIShortFormProvider iriShortFormProvider = new SimpleIRIShortFormProvider();
    		OWLAnnotationAssertionAxiom axiom = (OWLAnnotationAssertionAxiom) o;
    		StringBuilder buffer = new StringBuilder(iriShortFormProvider.getShortForm((IRI) axiom.getSubject()));
    		buffer.append(" ");
    		buffer.append(new ManchesterOWLSyntaxOWLObjectRendererImpl().render(axiom.getAnnotation()));
    		return buffer.toString();
    	}
    	else {
    		return new ManchesterOWLSyntaxOWLObjectRendererImpl().render(o);
    	}
    }
    
    @Override
    public int compareTo(EntityBasedDiff o) {
        int ret;

        if (sourceEntity == null && o.sourceEntity == null && targetEntity == null && o.targetEntity == null) {
        	return 0;
        }
        if (sourceEntity != null && o.sourceEntity == null) {
            return +1;
        }
        else if (sourceEntity == null && o.sourceEntity != null) {
            return -1;
        }
        else if (targetEntity != null && o.targetEntity == null) {
            return +1;
        }
        else if (targetEntity == null && o.targetEntity != null) {
            return -1;
        }
        else if (sourceEntity != null && (ret = sourceEntity.compareTo(o.sourceEntity)) != 0) {
            return ret;
        }
        else if (targetEntity != null) {
            return targetEntity.compareTo(o.targetEntity);
        }
        else {
            return 0;
        }
    }
    
    @Override
    public String toString() {
    	return "[Diff: " + getShortDescription() + "]";
    }
}
