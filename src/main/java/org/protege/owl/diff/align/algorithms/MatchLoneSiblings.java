package org.protege.owl.diff.align.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.protege.owl.diff.DifferencePosition;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.impl.SimpleAlignmentExplanation;
import org.protege.owl.diff.align.util.PrioritizedComparator;
import org.protege.owl.diff.service.RenderingService;
import org.protege.owl.diff.service.SiblingService;
import org.protege.owl.diff.util.EntityComparator;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

public class MatchLoneSiblings extends AbstractSiblingMatch {

	@Override
	public void initialize(Engine e) {
		super.initialize(e);
	}
	
	protected void checkSiblings(OWLClass sourceParent, Set<OWLClass> unmatchedSourceSiblings, 
								 OWLClass targetParent, Set<OWLClass> unmatchedTargetSiblings) {
		if (unmatchedSourceSiblings.size() == 1 && unmatchedTargetSiblings.size() == 1) {
			OWLClass sourceSibling = unmatchedSourceSiblings.iterator().next();
			OWLClass targetSibling = unmatchedTargetSiblings.iterator().next();
			getOwlDiffMap().addMatch(sourceSibling, targetSibling,
					                 new Explanation(getEngine(), sourceSibling, sourceParent));
		}
	}

	/*
	 * This is somewhat expensive to run early but I think I trust the results.
	 */
	public int getPriority() {
		return PrioritizedComparator.MIN_PRIORITY;
	}
	
    public AlignmentAggressiveness getAggressiveness() {
    	return AlignmentAggressiveness.AGGRESSIVE_SEARCH;
    }

	public String getAlgorithmName() {
		return "Match Lone Siblings";
	}
	
	private static class Explanation extends SimpleAlignmentExplanation {
		private OWLClass sourceClass;
		private OWLClass sourceParent;
		private OwlDiffMap diffMap;
		private Map<OWLClass, OWLClass> targetToSourceMap;
		private RenderingService renderer;
		private SiblingService siblingService;
		
		public Explanation(Engine engine, OWLClass sourceClass, OWLClass sourceParent) {
			super("Aligned the lone unmatched siblings.");
			diffMap = engine.getOwlDiffMap();
			renderer = RenderingService.get(engine);
			siblingService = SiblingService.get(engine);
			this.sourceClass = sourceClass;
			this.sourceParent = sourceParent;
		}
		
		@Override
		public boolean hasDetailedExplanation(OWLObject sourceObject) {
			return sourceObject.equals(sourceClass);
		}
		
		@Override
		public String getDetailedExplanation(OWLObject sourceObject) {
			OWLClass targetClass = (OWLClass) diffMap.getEntityMap().get(sourceClass);
			OWLClass targetParent = (OWLClass) diffMap.getEntityMap().get(sourceParent);
			StringBuffer sb = new StringBuffer();
			addMainExplanation(sb, sourceClass, targetParent, targetClass);
			addSourceChildrenExplanation(sb, diffMap, sourceParent, (OWLClass) sourceObject);
			addTargetChildrenExplanation(sb, diffMap, targetParent, targetClass);
			return sb.toString();
		}
		
		private void addMainExplanation(StringBuffer sb, OWLClass sourceObject, OWLClass targetParent, OWLClass targetClass) {
			sb.append("I matched the source class, \n\t");
			sb.append(renderer.renderSourceObject(sourceObject));
			sb.append(",\nwith the target class\n\t");
			sb.append(renderer.renderTargetObject(targetClass));
			sb.append(",\nbecause the source class is the only unmatched child of\n\t");
			sb.append(renderer.renderSourceObject(sourceParent));
			sb.append(",\nbecause the target class is the only unmatched child of\n\t");
			sb.append(renderer.renderTargetObject(targetParent));
			sb.append(",\nand the two parents match.\n");
		}
		
		private void addSourceChildrenExplanation(StringBuffer sb, OwlDiffMap diffMap, OWLClass sourceParent, OWLClass sourceObject) {
			List<OWLClass> sourceSubclasses = new ArrayList<OWLClass>();
			for (OWLClassExpression sourceSubclass : siblingService.getSubClasses(sourceParent, DifferencePosition.SOURCE)) {
				if (!sourceSubclass.isAnonymous()) {
					sourceSubclasses.add(sourceSubclass.asOWLClass());
				}
			}
			sourceSubclasses.remove(sourceObject);
			if (sourceSubclasses.isEmpty()) {
				sb.append("The source parent has no other subclasses\n\n");
			}
			else {
				Collections.sort(sourceSubclasses, new EntityComparator(renderer, DifferencePosition.SOURCE));
				sb.append("The other children of the source parent map as follows:\n");
				for (OWLClassExpression sourceSubclass : sourceSubclasses) {
					sb.append("\t");
					sb.append(renderer.renderSourceObject(sourceSubclass.asOWLClass()));
					sb.append(" --> ");
					sb.append(renderer.renderTargetObject((OWLClass) diffMap.getEntityMap().get(sourceSubclass)));
					sb.append('\n');
				}
			}
		}

		private void addTargetChildrenExplanation(StringBuffer sb, OwlDiffMap diffMap, OWLClass targetParent, OWLClass targetClass) {
			List<OWLClass> targetSubclasses = new ArrayList<OWLClass>();
			for (OWLClassExpression targetSubclass : siblingService.getSubClasses(targetParent, DifferencePosition.TARGET)) {
				if (!targetSubclass.isAnonymous()) {
					targetSubclasses.add(targetSubclass.asOWLClass());
				}
			}
			targetSubclasses.remove(targetClass);
			if (targetSubclasses.isEmpty()) {
				sb.append("The target parent has no other subclasses.\n\n");
			}
			else {
				Collections.sort(targetSubclasses, new EntityComparator(renderer, DifferencePosition.TARGET));
				sb.append("The other children of the target parent map as follows:\n");
				for (OWLClass targetSubclass : targetSubclasses) {
					OWLClass sourceSubclass = getMatchingSourceClass(targetSubclass);
					sb.append("\t");
					sb.append(renderer.renderSourceObject(sourceSubclass));
					sb.append(" --> ");
					sb.append(renderer.renderTargetObject(targetSubclass));
					sb.append('\n');
				}
			}
		}
		
		private OWLClass getMatchingSourceClass(OWLClass targetClass) {
			if (targetToSourceMap == null) {
				targetToSourceMap = new HashMap<OWLClass, OWLClass>();
				for (Entry<OWLEntity, OWLEntity> entry : diffMap.getEntityMap().entrySet()) {
					OWLEntity source = entry.getKey();
					OWLEntity target = entry.getValue();
					if (source instanceof OWLClass) {
						targetToSourceMap.put((OWLClass) target, (OWLClass) source);
					}
				}
			}
			return targetToSourceMap.get(targetClass);
		}
		
	}



}
