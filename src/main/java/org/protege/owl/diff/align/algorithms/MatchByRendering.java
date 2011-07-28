package org.protege.owl.diff.align.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.util.PrioritizedComparator;
import org.protege.owl.diff.service.CodeToEntityMapper;
import org.protege.owl.diff.service.RenderingService;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

public class MatchByRendering implements AlignmentAlgorithm {
    private Logger logger = Logger.getLogger(MatchByRendering.class);
    
    private boolean disabled = false;
    private OwlDiffMap diffMap;
    private RenderingService renderer;

    public int getPriority() {
        return PrioritizedComparator.MAX_PRIORITY - 1;
    }
    
    public AlignmentAggressiveness getAggressiveness() {
    	return AlignmentAggressiveness.MODERATE;
    }

    public void initialise(Engine e) {
        this.diffMap = e.getOwlDiffMap();
        renderer = RenderingService.get(e);
    }

    public void run() {
        if (!disabled) {
            diffMap.announce(this);
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Matching source entities with target entities");
                }
                matchEntities();
            }
            finally {
                diffMap.summarize();
            }
        }
        disabled = true;
    }
    
    public void reset() {
        disabled=false;
    }
    
    private boolean matchEntities() {
        Map<OWLEntity, OWLEntity> matchMap = new HashMap<OWLEntity, OWLEntity>();
        for (OWLEntity sourceEntity : diffMap.getUnmatchedSourceEntities()) {
            String rendering = renderer.renderSourceObject(sourceEntity);
            if (rendering == null) {
                continue;
            }
            OWLEntity potentialTargetEntity = renderer.getTargetEntityByRendering(rendering);

            if (potentialTargetEntity != null && isMatch(sourceEntity, potentialTargetEntity)) {
                matchMap.put(sourceEntity, potentialTargetEntity);
            }
        }
        if (matchMap.isEmpty()) {
            return false;
        }
        else {
            diffMap.addMatchingEntities(matchMap, "Enties with a common code value are matched.");
            return true;
        }
    }
    
    private boolean isMatch(OWLEntity entity1, OWLEntity entity2) {
    	return entity1.getEntityType() == entity2.getEntityType();
    }
    

    

    
    public String getAlgorithmName() {
        return "Match By Rendering";
    }

}
