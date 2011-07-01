package org.protege.owl.diff;

import java.util.Set;

import org.protege.owl.diff.align.OwlDiffMap;
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
	},
	TARGET {
		public OWLOntology getOntology(OwlDiffMap diffMap) {
			return diffMap.getTargetOntology();
		}		

		public Set<OWLEntity> getUnmatchedEntities(OwlDiffMap diffMap) {
			return diffMap.getUnmatchedTargetEntities();
		}
	};
	
	public abstract OWLOntology getOntology(OwlDiffMap diffMap);
	public abstract Set<OWLEntity> getUnmatchedEntities(OwlDiffMap diffMap);
	
}
