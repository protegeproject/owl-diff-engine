package org.protege.owl.diff.present.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchDescription;
import org.protege.owl.diff.present.MatchedAxiom;
import org.protege.owl.diff.service.CodeToEntityMapper;
import org.protege.owl.diff.service.RetirementClassService;
import org.protege.owl.diff.util.DiffDuplicator;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

public class IdentifyMergedConcepts extends AbstractAnalyzerAlgorithm {
	public static final MatchDescription MERGE = new MatchDescription("Merge Operation", MatchDescription.PRIMARY_MATCH_PRIORITY);
	public static final MatchDescription MERGE_AXIOM = new MatchDescription("Axiom modified by merge", MatchDescription.SECONDARY_MATCH_PRIORITY);
	public static final MatchDescription RETIRED_DUE_TO_MERGE = new MatchDescription("Retired Due to Merge", MatchDescription.SECONDARY_MATCH_PRIORITY);

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

	public void initialise(Engine e) {
		this.changes = e.getChanges();
		OwlDiffMap diffMap = changes.getRawDiffMap();
		mapper = CodeToEntityMapper.generateCodeToEntityMap(e);
		OWLDataFactory factory = diffMap.getOWLDataFactory();
		String mergedIntoPropertyName = (String) e.getParameters().get(MERGED_INTO_ANNOTATION_PROPERTY);
		if (mergedIntoPropertyName == null) {
			disabled = true;
			return;
		}
		mergedIntoProperty = factory.getOWLAnnotationProperty(IRI.create(mergedIntoPropertyName));
		retiredClassService = RetirementClassService.getRetirementClassService(e);
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
				}
			}
		}
		sourceToTarget = new DiffDuplicator(diffMap);
		mergeDuplicator = new OWLObjectDuplicator(diffMap.getOWLDataFactory(), mergedIntoMap);
	}
	
	@SuppressWarnings("unchecked")
	public void apply() {
		if (disabled) {
			return;
		}
		OwlDiffMap diffMap = changes.getRawDiffMap();
		OWLDataFactory factory = diffMap.getOWLDataFactory();
		for (Entry<OWLEntity, IRI> entry : mergedFromMap.entrySet()) {
			OWLEntity keptEntity = entry.getKey();
			/*
			 * The cast to entity type is not required in java 6.  Thanks for the help apple!
			 */
			OWLEntity retiringEntity = factory.getOWLEntity((EntityType) keptEntity.getEntityType(), entry.getValue());
			EntityBasedDiff keptEntityDiff = changes.getTargetDiffMap().get(keptEntity);
			EntityBasedDiff retiringEntityDiff = changes.getTargetDiffMap().get(retiringEntity);
			if (retiringEntityDiff != null) {
				handleMergeDeclaration(retiringEntityDiff, keptEntityDiff);
				handleRetire(retiringEntityDiff);
			}
			handleAxiomAdjustments(keptEntityDiff, keptEntityDiff);
			if (retiringEntityDiff != null) {
				handleAxiomAdjustments(retiringEntityDiff, keptEntityDiff);
			}
		}
	}
	
	private void handleMergeDeclaration(EntityBasedDiff retiringEntityDiff, EntityBasedDiff keptEntityDiff) {
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
	        changes.removeMatch(mergeDecl);
	        changes.addMatch(newMergeDecl);
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
	        changes.removeMatch(match);
	        changes.addMatch(newRetired);
	    }
	}

	private void handleAxiomAdjustments(EntityBasedDiff retiringEntityDiff, EntityBasedDiff keptEntityDiff) {
		if (retiringEntityDiff.getSourceEntity() == null) {
			return;
		}
		OWLEntity retiringEntity = retiringEntityDiff.getSourceEntity();
		OWLOntology sourceOntology = changes.getRawDiffMap().getSourceOntology();
		OWLOntology targetOntology = changes.getRawDiffMap().getTargetOntology();
		for (OWLAxiom axiom : sourceOntology.getReferencingAxioms(retiringEntity)) {
			OWLAxiom targetAxiom = mergeDuplicator.duplicateObject(sourceToTarget.duplicateObject(axiom));
			MatchedAxiom axiomAdded = new MatchedAxiom(null, targetAxiom, MatchedAxiom.AXIOM_ADDED);
			MatchedAxiom axiomRemoved = new MatchedAxiom(axiom, null, MatchedAxiom.AXIOM_DELETED);
			MatchedAxiom axiomMerged = new MatchedAxiom(axiom, targetAxiom, MERGE_AXIOM);
			
			if (changes.containsMatch(axiomAdded)) {			
				changes.removeMatch(axiomAdded);
				changes.addMatch(axiomMerged);
				if (changes.containsMatch(axiomRemoved)) {
					changes.removeMatch(axiomRemoved);
				}
			}
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
