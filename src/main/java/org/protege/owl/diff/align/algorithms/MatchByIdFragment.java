package org.protege.owl.diff.align.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.util.AlignmentAlgorithmComparator;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

public class MatchByIdFragment implements AlignmentAlgorithm {
	
	private ShortFormProvider shortFormProvider;
	private OwlDiffMap diffs;
	private boolean alreadyRun = false;
	

	public void initialise(Engine e) {
		shortFormProvider = new SimpleShortFormProvider();
		diffs = e.getOwlDiffMap();
	}

	
	public void run() {
		if (alreadyRun) {
			return;
		}
		diffs.announce(this);
		try {
			Map<String, Collection<OWLEntity>> map = collectTargetEntitiesByShortForm();
			searchForMatches(map);
		}
		finally {
			diffs.summarize();
			alreadyRun = true;
		}
	}
	
	private Map<String, Collection<OWLEntity>> collectTargetEntitiesByShortForm() {
		Map<String, Collection<OWLEntity>> map = new HashMap<String, Collection<OWLEntity>>();
		for (OWLEntity targetEntity : diffs.getUnmatchedTargetEntities()) {
			String shortForm = shortFormProvider.getShortForm(targetEntity);
			Collection<OWLEntity> entities = map.get(shortForm);
			if (entities == null) {
				entities = new ArrayList<OWLEntity>();
				map.put(shortForm, entities);
			}
			entities.add(targetEntity);
		}
		return map;
	}

	private void searchForMatches(Map<String, Collection<OWLEntity>> targetEntitiesByShortForm) {
		for (OWLEntity sourceEntity : new ArrayList<OWLEntity>(diffs.getUnmatchedSourceEntities())) {
			String shortForm = shortFormProvider.getShortForm(sourceEntity);
			Collection<OWLEntity> targetEntities = targetEntitiesByShortForm.get(shortForm);
			if (targetEntities != null) {
				OWLEntity matchingTarget = findMatch(sourceEntity, targetEntities);
				if (matchingTarget != null) {
					diffs.addMatch(sourceEntity, matchingTarget);
				}
			}
		}
	}
	
	private OWLEntity findMatch(OWLEntity sourceEntity, Collection<OWLEntity> targetEntities) {
		OWLEntity match = null;
		EntityType<?> matchingType = sourceEntity.getEntityType();
		for (OWLEntity potentialMatch : targetEntities) {
			EntityType<?> targetType = potentialMatch.getEntityType();
			if (targetType == matchingType && match == null) {
				match = potentialMatch;
			}
			else if (targetType == matchingType) {
				return null;
			}
		}
		return match;
	}
	

	
	public void reset() {
		alreadyRun = false;
	}

	/*
	 * I don't entirely trust this guy to get the right answer and he is slow.
	 */
	public int getPriority() {
		return AlignmentAlgorithmComparator.MIN_PRIORITY;
	}

	
	public String getAlgorithmName() {
		return "Match Entities by IRI fragment";
	}

}
