package org.protege.owl.diff.service;

import java.util.HashSet;
import java.util.Set;

import org.protege.owl.diff.DifferencePosition;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.AlignmentExplanation;
import org.protege.owl.diff.align.algorithms.DeferDeprecationAlgorithm;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

public class DeprecationDeferralService {
	private boolean initialized = false;
	private boolean deprecationDeferralEnabled;
	
	private Engine engine;
	
	private Set<DeferredMatchBean> deferredMatches = new HashSet<DeferredMatchBean>();

	public static DeprecationDeferralService get(Engine e) {
		DeprecationDeferralService dds = e.getService(DeprecationDeferralService.class);
		if (dds == null) {
			dds = new DeprecationDeferralService(e);
			e.addService(dds);
		}
		return dds;
	}
	
	private DeprecationDeferralService(Engine e) {
		engine = e;
	}
	
	private void ensureInitialized() {
		if (!initialized) {
			deprecationDeferralEnabled = false;
			for (AlignmentAlgorithm a : engine.getAlignmentAlgorithms()) {
				if (a instanceof DeferDeprecationAlgorithm) {
					deprecationDeferralEnabled = true;
				}
			}
			initialized = true;
		}
	}
	
	public boolean checkDeprecation(OWLEntity sourceEntity, OWLEntity targetEntity) {
		ensureInitialized();
		return deprecationDeferralEnabled 
		           && !isDeprecated(sourceEntity, DifferencePosition.SOURCE) 
		           && isDeprecated(targetEntity, DifferencePosition.TARGET);
	}
	
	public boolean isDeprecated(OWLEntity e, DifferencePosition position) {
		OWLOntology ontology = position.getOntology(engine.getOwlDiffMap());
		OWLAnnotationProperty deprecated = engine.getOWLDataFactory().getOWLDeprecated();
		for (OWLOntology ont : ontology.getImportsClosure()) {
            for (OWLAnnotation annotation : EntitySearcher.getAnnotations(e, ont, deprecated)) {
				if (!(annotation.getValue() instanceof OWLLiteral)) {
					continue;
				}
				OWLLiteral value = (OWLLiteral) annotation.getValue();
				if (!value.isBoolean()) {
					continue;
				}
				if (value.parseBoolean()) {
					return true;
				}
			}
		}
		return false;
	}

	public void addMatch(OWLEntity sourceEntity, OWLEntity targetEntity, AlignmentExplanation explanation) {
		deferredMatches.add(new DeferredMatchBean(sourceEntity, targetEntity, explanation));
		engine.getOwlDiffMap().setMatchBlocked(sourceEntity, targetEntity, true);
	}

	public Set<DeferredMatchBean> getDeferredMatches() {
		try {
			return deferredMatches;
		}
		finally {
			deferredMatches = new HashSet<DeferredMatchBean>();
		}
	}
	
	/**
	 * @author tredmond
	 *
	 */
	public static class DeferredMatchBean {
		private OWLEntity sourceEntity;
		private OWLEntity targetEntity;
		private AlignmentExplanation explanation;

		public DeferredMatchBean(OWLEntity sourceEntity, OWLEntity targetEntity, AlignmentExplanation explanation) {
			this.sourceEntity = sourceEntity;
			this.targetEntity = targetEntity;
			this.explanation = explanation;
		}

		public OWLEntity getSourceEntity() {
			return sourceEntity;
		}

		public OWLEntity getTargetEntity() {
			return targetEntity;
		}

		public AlignmentExplanation getExplanation() {
			return explanation;
		}
		
	}
}
