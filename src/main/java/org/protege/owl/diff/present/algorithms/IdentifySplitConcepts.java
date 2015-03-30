package org.protege.owl.diff.present.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchDescription;
import org.protege.owl.diff.present.MatchedAxiom;
import org.protege.owl.diff.service.CodeToEntityMapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

public class IdentifySplitConcepts extends AbstractAnalyzerAlgorithm {
	public static final MatchDescription SPLIT = new MatchDescription("Split Operation", MatchDescription.PRIMARY_MATCH_PRIORITY);
	public static final MatchDescription COPIED_FROM_SPLIT = new MatchDescription("Copied by split operation", MatchDescription.SECONDARY_MATCH_PRIORITY);
	public static final OWLDatatype STRING_DATATYPE = OWLManager.getOWLDataFactory().getOWLDatatype(XSDVocabulary.STRING.getIRI());
	
	public static final String SPLIT_FROM_ANNOTATION_PROPERTY = "split.from.annotation";

	private Changes changes;
	private OwlDiffMap diffMap;
	private CodeToEntityMapper mapper;
	private boolean disabled = false;
	private OWLAnnotationProperty splitFromProperty;

	public void initialise(Engine e) {
		this.changes = e.getChanges();
		this.diffMap = changes.getRawDiffMap();
		this.mapper = CodeToEntityMapper.get(e);
		OWLDataFactory factory = diffMap.getOWLDataFactory();
		String splitFromPropertyName = e.getParameters().get(SPLIT_FROM_ANNOTATION_PROPERTY);
		if (splitFromPropertyName == null) {
			disabled = true;
			return;
		}
		splitFromProperty = factory.getOWLAnnotationProperty(IRI.create(splitFromPropertyName));
	}

	
	public void apply() {
		if (disabled) {
			return;
		}
		for (EntityBasedDiff diff : changes.getEntityBasedDiffs()) { 
			apply(diff);
		}
	}
	
	private void apply(EntityBasedDiff diff) {
		IRI iriOfNewlyCreatedClass = null;
		String code = null;
		MatchedAxiom splitAxiomWithBadDescription = null;
		MatchedAxiom splitFrom = null;
		for (MatchedAxiom match : diff.getAxiomMatches()) {
			if (match.getDescription().equals(MatchedAxiom.AXIOM_ADDED) && isSplitFromAnnotationAssertion(match.getTargetAxiom())) {
				splitAxiomWithBadDescription = match;
				splitFrom = new MatchedAxiom(null, match.getTargetAxiom(), SPLIT);
				iriOfNewlyCreatedClass = (IRI) ((OWLAnnotationAssertionAxiom) match.getTargetAxiom()).getSubject();
				code = ((OWLLiteral) ((OWLAnnotationAssertionAxiom) match.getTargetAxiom()).getValue()).getLiteral();
				break;
			}
		}
		if (splitFrom != null) {
			changes.removeMatch(splitAxiomWithBadDescription);
			changes.addMatch(splitFrom);

			handleNewSplitAxioms(iriOfNewlyCreatedClass, code, diff);
		}
	}


	private boolean isSplitFromAnnotationAssertion(OWLAxiom axiom) {
		return axiom instanceof OWLAnnotationAssertionAxiom &&
		            ((OWLAnnotationAssertionAxiom) axiom).getProperty().equals(splitFromProperty) &&
					diffMap.getUnmatchedTargetAxioms().contains(axiom) &&
					((OWLAnnotationAssertionAxiom) axiom).getSubject() instanceof IRI &&
					((OWLAnnotationAssertionAxiom) axiom).getValue() instanceof OWLLiteral &&
					((OWLLiteral) ((OWLAnnotationAssertionAxiom) axiom).getValue()).getDatatype().equals(STRING_DATATYPE);
	}


	private void handleNewSplitAxioms(IRI iriOfNewlyCreatedClass, String code, EntityBasedDiff diff) {
		OWLDataFactory factory = diffMap.getOWLDataFactory();
		OWLClass newlyCreatedClass = factory.getOWLClass(iriOfNewlyCreatedClass);
		OWLClass classThatWasSplit = getClassThatWasSplit(code);
		if (classThatWasSplit != null) {
			handleNewSplitAxioms(newlyCreatedClass, classThatWasSplit, diff);
		}
	}
	
	private void handleNewSplitAxioms(OWLClass newlyCreatedClass, OWLClass classThatWasSplit, EntityBasedDiff diff) {
		OWLOntology sourceOntology = diffMap.getSourceOntology();
		Map<OWLEntity, IRI> newTargetToSplitSource = Collections.singletonMap((OWLEntity) newlyCreatedClass, classThatWasSplit.getIRI());
		OWLObjectDuplicator duplicator = new OWLObjectDuplicator(newTargetToSplitSource, diffMap.getOWLDataFactory());
		Set<OWLClassExpression> inferredParents = getInferredParents(sourceOntology, classThatWasSplit);
		for (MatchedAxiom match : new ArrayList<MatchedAxiom>(diff.getAxiomMatches())) {
			if (match.getDescription().equals(MatchedAxiom.AXIOM_ADDED) && 
					cameFromSourceOntology((OWLAxiom) duplicator.duplicateObject(match.getTargetAxiom()), sourceOntology, classThatWasSplit, inferredParents)) {
				MatchedAxiom modifiedBySplit = new MatchedAxiom(null, match.getTargetAxiom(), COPIED_FROM_SPLIT);
				changes.removeMatch(match);
				changes.addMatch(modifiedBySplit);
			}
		}
	}
	
	private boolean cameFromSourceOntology(OWLAxiom axiom, OWLOntology sourceOntology, OWLClass child, Set<OWLClassExpression> inferredParents) {
		if (sourceOntology.containsAxiom(axiom)) {
			return true;
		}
		else if (axiom instanceof OWLSubClassOfAxiom) {
			return ((OWLSubClassOfAxiom) axiom).getSubClass().equals(child) && inferredParents.contains(((OWLSubClassOfAxiom) axiom).getSuperClass());
		}
		else {
			return false;
		}
	}
	
	private Set<OWLClassExpression> getInferredParents(OWLOntology sourceOntology, OWLClass child) {
		return getInferredParents(sourceOntology, child, new TreeSet<OWLClass>());
	}

	private Set<OWLClassExpression> getInferredParents(OWLOntology sourceOntology, OWLClass child, Set<OWLClass> viewed) {
		Set<OWLClassExpression> result = new TreeSet<OWLClassExpression>();
		if (!viewed.contains(child)) {
			viewed.add(child);
			Set<OWLClassExpression> parents = new TreeSet<OWLClassExpression>();
            parents.addAll(EntitySearcher
                    .getSuperClasses(child, sourceOntology));
            parents.addAll(EntitySearcher.getEquivalentClasses(child,
                    sourceOntology));
			for (OWLClassExpression parent : parents) {
				if (parent instanceof OWLClass) {
					result.add(parent);
					result.addAll(getInferredParents(sourceOntology, (OWLClass) parent, viewed));
				}
				else if (parent instanceof OWLObjectIntersectionOf) {
					Set<OWLClassExpression> inferredParents = ((OWLObjectIntersectionOf) parent).getOperands();
					result.addAll(inferredParents);
					for (OWLClassExpression inferredParent : inferredParents) {
						if (inferredParent instanceof OWLClass) {
							result.addAll(getInferredParents(sourceOntology, (OWLClass) inferredParent, viewed));
						}
					}
				}
				else {
					result.add(parent);
				}
			}
		}
		return result;
	}

	private OWLClass getClassThatWasSplit(String code) {
		if (code == null) {
			return null;
		}
		Collection<OWLEntity> possiblySplitEntities = mapper.getTargetEntities(code);
		OWLClass classThatWasSplit = null;
		for (OWLEntity possiblySplityEntity : possiblySplitEntities) {
			if (possiblySplityEntity instanceof OWLClass) {
				if (classThatWasSplit != null) {
					return null;
				}
				else {
					classThatWasSplit = (OWLClass) possiblySplityEntity;
					EntityBasedDiff classThatWasSplitDiff =  changes.getTargetDiffMap().get(classThatWasSplit);
					if (classThatWasSplitDiff != null) {
						classThatWasSplit = (OWLClass) classThatWasSplitDiff.getSourceEntity(); // if it was created we do really want null
					}
				}
			}
		}
		return classThatWasSplit;
	}

}
