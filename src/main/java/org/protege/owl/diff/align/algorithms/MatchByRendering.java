package org.protege.owl.diff.align.algorithms;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.AlignmentExplanation;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.impl.SimpleAlignmentExplanation;
import org.protege.owl.diff.align.util.PrioritizedComparator;
import org.protege.owl.diff.service.RenderingService;
import org.semanticweb.owlapi.model.OWLEntity;

public class MatchByRendering implements AlignmentAlgorithm {
	public static final AlignmentExplanation EXPLANATION = new SimpleAlignmentExplanation("Enties with a common rendering are matched.");
    private Logger logger = LoggerFactory.getLogger(MatchByRendering.class.getName());
    
    private boolean disabled = false;
    private OwlDiffMap diffMap;
    private RenderingService renderer;

	@Override
	public boolean isCustom() {
		return false;
	}
	
    public int getPriority() {
        return PrioritizedComparator.MAX_PRIORITY - 1;
    }
    
    public AlignmentAggressiveness getAggressiveness() {
    	return AlignmentAggressiveness.MODERATE;
    }

    public void initialize(Engine e) {
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
            diffMap.addMatchingEntities(matchMap, EXPLANATION);
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
