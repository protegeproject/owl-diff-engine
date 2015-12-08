package org.protege.owl.diff.align.algorithms;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.AlignmentListener;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.UnmatchedSourceAxiom;
import org.protege.owl.diff.align.util.PrioritizedComparator;
import org.protege.owl.diff.service.DeprecationDeferralService;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

public class DeferDeprecationAlgorithm implements AlignmentAlgorithm {
	public final static Logger LOGGER = LoggerFactory.getLogger(DeferDeprecationAlgorithm.class.getName());
	
	private OwlDiffMap diffMap;
	private DeprecationDeferralService dds;
	private boolean progress;
	private AlignmentListener listener = new AlignmentListener() {
		
		public void unmatchedAxiomMoved(UnmatchedSourceAxiom unmatched) {
			;
		}
		
		
		public void addUnmatcheableAxiom(OWLAxiom axiom) {
			;
		}
		
		
		public void addMatchingEntities(Map<OWLEntity, OWLEntity> newMatches) {
			if (!newMatches.isEmpty()) {
				progress = true;
			}
		}
		
		
		public void addMatchingAnonymousIndividuals(Map<OWLAnonymousIndividual, OWLAnonymousIndividual> newMatches) {
			if (!newMatches.isEmpty()) {
				progress = true;
			}
		}
		
		
		public void addMatchedAxiom(OWLAxiom axiom) {
		}
		
		
		public void addMatch(OWLAnonymousIndividual source, OWLAnonymousIndividual target) {
			progress = true;
		}
		
		
		public void addMatch(OWLEntity source, OWLEntity target) {
			progress = true;
		}
	};
	
	public int getPriority() {
		return PrioritizedComparator.MIN_PRIORITY;
	}

	
	public void initialise(Engine e) {
		dds = DeprecationDeferralService.get(e);
		diffMap = e.getOwlDiffMap();
		progress = false;
		diffMap.addDiffListener(listener);
	}

	
	public void run() {
		if (!progress) {
			diffMap.announce(this);
			try {
				matchEntities();
			} finally {
				diffMap.summarize();
			}
		}
		progress = false;
	}
	
	private void matchEntities() {
		for (DeprecationDeferralService.DeferredMatchBean bean : dds.getDeferredMatches()) {
			OWLEntity sourceEntity = bean.getSourceEntity();
			OWLEntity targetEntity = bean.getTargetEntity();
			diffMap.setMatchBlocked(sourceEntity, targetEntity, false);
			if (diffMap.getUnmatchedSourceEntities().contains(sourceEntity) 
					&& diffMap.getUnmatchedTargetEntities().contains(targetEntity)) {
				diffMap.addMatch(sourceEntity, targetEntity, bean.getExplanation());
			}
			else if (LOGGER.isInfoEnabled()){
				LOGGER.info(sourceEntity + " was deprecated but found a better refactor operation:");
				LOGGER.info("\t" + sourceEntity + " -> " + diffMap.getEntityMap().get(sourceEntity));
			}
		}
	}

	
	public void reset() {
		dds = null;
		diffMap.removeDiffListener(listener);
	}
	
	@Override
	public boolean isCustom() {
		return true;
	}

	
	public AlignmentAggressiveness getAggressiveness() {
		return AlignmentAggressiveness.AGGRESSIVE_SEARCH;
	}

	
	public String getAlgorithmName() {
		return "Defer aligning source entity with deprecated target";
	}

}
