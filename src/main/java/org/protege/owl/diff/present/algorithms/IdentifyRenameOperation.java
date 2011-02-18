package org.protege.owl.diff.present.algorithms;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchDescription;
import org.protege.owl.diff.present.MatchedAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;

public class IdentifyRenameOperation extends AbstractAnalyzerAlgorithm {
	// put this just after the primary match descriptions but before any of the secondary ones.
	public static final MatchDescription RENAMED_CHANGE_DESCRIPTION = new MatchDescription("Renamed", MatchDescription.PRIMARY_MATCH_PRIORITY + 1);
	private Changes changes;
	private OWLDataFactory factory;
	
	public void initialise(Engine engine) {
		changes  = engine.getChanges();
		factory = engine.getOWLDataFactory();
	}

	public void apply() {
		for (EntityBasedDiff diff : changes.getEntityBasedDiffs()) {
			apply(diff);
		}
	}
	
	private void apply(EntityBasedDiff diff) {
		OWLEntity sourceEntity = diff.getSourceEntity();
		OWLEntity targetEntity = diff.getTargetEntity();
		if (sourceEntity != null && targetEntity != null && !sourceEntity.equals(targetEntity)) {
			changes.addMatch(new MatchedAxiom(factory.getOWLDeclarationAxiom(sourceEntity), 
											  factory.getOWLDeclarationAxiom(targetEntity),
											  RENAMED_CHANGE_DESCRIPTION));
		}
	}

}
