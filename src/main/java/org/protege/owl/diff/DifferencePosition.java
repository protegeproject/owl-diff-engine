package org.protege.owl.diff;

import java.util.Set;

import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchedAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

public enum DifferencePosition {
	SOURCE {
		public OWLOntology getOntology(OwlDiffMap diffMap) {
			return diffMap.getSourceOntology();
		}
		
		public Set<OWLEntity> getUnmatchedEntities(OwlDiffMap diffMap) {
			return diffMap.getUnmatchedSourceEntities();
		}
		
		public OWLEntity getEntity(EntityBasedDiff diff) {
			return diff.getSourceEntity();
		}
		
		public OWLAxiom getAxiom(MatchedAxiom match) {
			return match.getSourceAxiom();
		}
	},
	TARGET {
		public OWLOntology getOntology(OwlDiffMap diffMap) {
			return diffMap.getTargetOntology();
		}		

		public Set<OWLEntity> getUnmatchedEntities(OwlDiffMap diffMap) {
			return diffMap.getUnmatchedTargetEntities();
		}
		
		public OWLEntity getEntity(EntityBasedDiff diff) {
			return diff.getTargetEntity();
		}
		
		public OWLAxiom getAxiom(MatchedAxiom match) {
			return match.getTargetAxiom();
		}
	};
	
	public abstract OWLOntology getOntology(OwlDiffMap diffMap);
	public abstract Set<OWLEntity> getUnmatchedEntities(OwlDiffMap diffMap);
	
	public abstract OWLEntity getEntity(EntityBasedDiff diff);
	
	public abstract OWLAxiom  getAxiom(MatchedAxiom match);
}
