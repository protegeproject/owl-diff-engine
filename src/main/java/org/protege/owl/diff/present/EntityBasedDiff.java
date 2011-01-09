package org.protege.owl.diff.present;

import java.util.Collection;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class EntityBasedDiff implements Comparable<EntityBasedDiff> {
    public enum DiffType {
        EQUIVALENT, CREATED, DELETED, RENAMED, MODIFIED;
    }
    
    private OWLEntity sourceEntity;
    private OWLEntity targetEntity;
    private Collection<MatchedAxiom> axiomMatches = new TreeSet<MatchedAxiom>();
    
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
        else if (!axiomMatches.isEmpty()) {
            return DiffType.MODIFIED;
        }
        else if (!sourceEntity.equals(targetEntity)) {
            return DiffType.RENAMED;
        }
        else {
            return DiffType.EQUIVALENT;
        }
    }

    public Collection<MatchedAxiom> getAxiomMatches() {
        return axiomMatches;
    }
    
    public void addMatch(MatchedAxiom match) {
        axiomMatches.add(match);
    }
    
    public void removeMatch(MatchedAxiom match) {
        axiomMatches.remove(match);
    }
    
    public String getDescription() {
        StringBuffer buffer = new StringBuffer();
        switch (getDiffType()) {
        case CREATED:
            buffer.append("Created ");
            buffer.append(renderObject(targetEntity));
            break;
        case DELETED:
            buffer.append("Deleted ");
            buffer.append(renderObject(sourceEntity));
            break;
        case EQUIVALENT:
            buffer.append("Unchanged ");
            buffer.append(renderObject(targetEntity));
            break;
        case RENAMED:
            buffer.append("Id changed");
            buffer.append(renderObject(sourceEntity));
            buffer.append(" -> ");
            buffer.append(renderObject(targetEntity));
            break;
        case MODIFIED:
            buffer.append("Modified ");
            buffer.append(renderObject(sourceEntity));
            buffer.append(" -> ");
            buffer.append(renderObject(targetEntity));
            break;
        default:
            throw new UnsupportedOperationException("Programmer error");
        }
        buffer.append("\n");
        for (MatchedAxiom match : axiomMatches) {
            buffer.append(match.getDescription().getDescription());
            buffer.append(" ");
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
            buffer.append("\n");
        }
        return buffer.toString();
    }
    
    protected String renderObject(OWLObject o) {
        return new ManchesterOWLSyntaxOWLObjectRendererImpl().render(o);
    }
    
    public int compareTo(EntityBasedDiff o) {
        int ret;

        if (sourceEntity != null && o.sourceEntity == null) {
            return +1;
        }
        else if (sourceEntity == null && o.sourceEntity != null) {
            return -1;
        }
        else if (sourceEntity != null && (ret = sourceEntity.compareTo(o.sourceEntity)) != 0) {
            return ret;
        }
        else if (targetEntity != null && o.targetEntity == null) {
            return +1;
        }
        else if (targetEntity == null && o.targetEntity != null) {
            return -1;
        }
        else if (targetEntity != null) {
            return targetEntity.compareTo(o.targetEntity);
        }
        else {
            return 0;
        }
    }
}
