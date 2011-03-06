package org.protege.owl.diff.present.algorithms;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.present.Changes;
import org.semanticweb.owlapi.model.OWLEntity;

public class IdentifyPunConversions extends AbstractAnalyzerAlgorithm {
	private Engine e;

	public void initialise(Engine e) {
		this.e = e;
	}

	public void apply() {
		for (OWLEntity deletedEntity : e.getOwlDiffMap().getUnmatchedSourceEntities()) {
			handleDeletedEntity(deletedEntity);
		}
		for (OWLEntity createdEntity : e.getOwlDiffMap().getUnmatchedTargetEntities()) {
			handleCreatedEntity(createdEntity);
		}
	}
	
	
	private void handleDeletedEntity(OWLEntity deletedEntity) {
		
	}

	private void handleCreatedEntity(OWLEntity createdEnitity) {
		
	}
}
