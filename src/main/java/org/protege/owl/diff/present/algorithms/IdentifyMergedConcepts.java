package org.protege.owl.diff.present.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.util.DiffDuplicator;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchDescription;
import org.protege.owl.diff.present.MatchedAxiom;
import org.protege.owl.diff.service.CodeToEntityMapper;
import org.protege.owl.diff.service.RetirementClassService;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

public class IdentifyMergedConcepts extends AbstractAnalyzerAlgorithm {
	public static final MatchDescription MERGE = new MatchDescription("Merge Operation", MatchDescription.MIN_SEQUENCE);
	public static final MatchDescription MERGE_AXIOM = new MatchDescription("Axiom modified by merge", MatchDescription.MIN_SEQUENCE + 1);
	public static final MatchDescription RETIRED_DUE_TO_MERGE = new MatchDescription("Retired Due to Merge", MatchDescription.MIN_SEQUENCE);

	public static final String MERGED_INTO_ANNOTATION_PROPERTY = "merged.into.annotation";
	
	private Changes changes;
	private CodeToEntityMapper mapper;
	private boolean disabled = false;
	private OWLAnnotationProperty mergedIntoProperty;
	private Map<OWLEntity, IRI> mergedFromMap = new HashMap<OWLEntity, IRI>();
	private OWLObjectDuplicator sourceToTarget;
	private OWLObjectDuplicator mergeDuplicator;
	private RetirementClassService retiredClassService;

	public IdentifyMergedConcepts() {
		setPriority(IdentifyRetiredConcepts.DEFAULT_IDENTIFY_RETIRED_CONCEPTS_PRIORITY + 1);
	}

	@Override
	public void initialise(Changes changes, Properties parameters) {
		this.changes = changes;
		OwlDiffMap diffMap = changes.getRawDiffMap();
		mapper = CodeToEntityMapper.generateCodeToEntityMap(diffMap, parameters);
		OWLDataFactory factory = diffMap.getOWLDataFactory();
		String mergedIntoPropertyName = (String) parameters.get(MERGED_INTO_ANNOTATION_PROPERTY);
		if (mergedIntoPropertyName == null) {
			disabled = true;
			return;
		}
		mergedIntoProperty = factory.getOWLAnnotationProperty(IRI.create(mergedIntoPropertyName));
		retiredClassService = RetirementClassService.getRetirementClassService(diffMap, parameters);
		Map<IRI, IRI> mergedIntoMap = new HashMap<IRI, IRI>();
		for (OWLAxiom axiom : diffMap.getTargetOntology().getReferencingAxioms(mergedIntoProperty)) {
			if (isMergedIntoAxiom(axiom)) {
				OWLAnnotationAssertionAxiom annotationAssertion = (OWLAnnotationAssertionAxiom) axiom;
				IRI retiringEntity = (IRI) annotationAssertion.getSubject();
				String code = ((OWLLiteral) annotationAssertion.getAnnotation().getValue()).getLiteral();
				Collection<OWLEntity> keptEntities = mapper.getTargetEntities(code);
				if (keptEntities != null && !keptEntities.isEmpty()) {
					OWLEntity keptEntity = keptEntities.iterator().next();
					mergedIntoMap.put(retiringEntity, keptEntity.getIRI());
					mergedFromMap.put(keptEntity, retiringEntity);
					break;
				}
			}
		}
		sourceToTarget = new DiffDuplicator(diffMap);
		mergeDuplicator = new OWLObjectDuplicator(diffMap.getOWLDataFactory(), mergedIntoMap);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void apply() {
		if (disabled) {
			return;
		}
		OwlDiffMap diffMap = changes.getRawDiffMap();
		OWLDataFactory factory = diffMap.getOWLDataFactory();
		for (Entry<OWLEntity, IRI> entry : mergedFromMap.entrySet()) {
			OWLEntity keptEntity = entry.getKey();
			OWLEntity retiringEntity = factory.getOWLEntity(keptEntity.getEntityType(), entry.getValue());
			EntityBasedDiff keptEntityDiff = changes.getTargetDiffMap().get(keptEntity);
			EntityBasedDiff retiringEntityDiff = changes.getTargetDiffMap().get(retiringEntity);
			if (retiringEntityDiff != null) {
				handleMergeAxiom(retiringEntityDiff, keptEntityDiff);
				handleRetire(retiringEntityDiff);
			}
			handleAxiomAdjustments(keptEntityDiff, keptEntityDiff);
			if (retiringEntityDiff != null) {
				handleAxiomAdjustments(retiringEntityDiff, keptEntityDiff);
			}
		}
	}
	
	private void handleAxiomAdjustments(EntityBasedDiff retiringEntityDiff, EntityBasedDiff keptEntityDiff) {
		if (retiringEntityDiff.getSourceEntity() == null) {
			return;
		}
		OWLEntity retiringEntity = retiringEntityDiff.getSourceEntity();
		for (OWLAxiom axiom : changes.getRawDiffMap().getSourceOntology().getReferencingAxioms(retiringEntity)) {
			OWLAxiom targetAxiom = mergeDuplicator.duplicateObject(sourceToTarget.duplicateObject(axiom));
			MatchedAxiom axiomAdded = new MatchedAxiom(null, targetAxiom, MatchedAxiom.AXIOM_ADDED);
			MatchedAxiom axiomRemoved = new MatchedAxiom(axiom, null, MatchedAxiom.AXIOM_DELETED);
			if (keptEntityDiff.getAxiomMatches().contains(axiomAdded)) {
				MatchedAxiom axiomMerged = new MatchedAxiom(axiom, targetAxiom, MERGE_AXIOM);
				keptEntityDiff.removeMatch(axiomAdded);
				keptEntityDiff.addMatch(axiomMerged);
				if (retiringEntityDiff.getAxiomMatches().contains(axiomRemoved)) {
					retiringEntityDiff.removeMatch(axiomRemoved);
					retiringEntityDiff.addMatch(axiomMerged);
				}
			}
		}
	}
	
	private void handleMergeAxiom(EntityBasedDiff retiringEntityDiff, EntityBasedDiff keptEntityDiff) {
        MatchedAxiom mergeDecl = null;
        for (MatchedAxiom match : retiringEntityDiff.getAxiomMatches()) {
            if (match.isFinal()) {
                continue;
            }
            if (match.getDescription().equals(MatchedAxiom.AXIOM_ADDED) 
            		&& isMergedIntoAxiom(match.getTargetAxiom())) {
            	mergeDecl = match;
            	break;
            }
        }
        if (mergeDecl != null) {
        	MatchedAxiom newMergeDecl = new MatchedAxiom(null, mergeDecl.getTargetAxiom(), MERGE);
        	newMergeDecl.setFinal(true);
            retiringEntityDiff.removeMatch(mergeDecl);
            retiringEntityDiff.addMatch(newMergeDecl);
            keptEntityDiff.addMatch(newMergeDecl);
        }
	}
		
	private void handleRetire(EntityBasedDiff diff) {
        Collection<MatchedAxiom> retiringMatches = new ArrayList<MatchedAxiom>();
        for (MatchedAxiom match : diff.getAxiomMatches()) {
            if (match.isFinal()) {
                continue;
            }
            if (match.getDescription().equals(MatchedAxiom.AXIOM_ADDED) && retiredClassService.isRetirementAxiom(match.getTargetAxiom())) {
            	retiringMatches.add(match);
            }
        }
        for (MatchedAxiom match : retiringMatches) {
            MatchedAxiom newRetired = new MatchedAxiom(null, match.getTargetAxiom(), RETIRED_DUE_TO_MERGE);
            newRetired.setFinal(true);
            diff.removeMatch(match);
            diff.addMatch(newRetired);
        }
	}
	
	private boolean isMergedIntoAxiom(OWLAxiom axiom) {
		boolean ret = false;
		if (axiom instanceof OWLAnnotationAssertionAxiom) {
			OWLAnnotationAssertionAxiom annotationAssertion = (OWLAnnotationAssertionAxiom) axiom;
			OWLAnnotation annotation = annotationAssertion.getAnnotation();
			ret = annotation.getProperty().equals(mergedIntoProperty)
				    && annotationAssertion.getSubject() instanceof IRI
					&& annotation.getValue() instanceof OWLLiteral;
		}
		return ret;
	}

}
