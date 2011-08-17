package org.protege.owl.diff.examples;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.protege.owl.diff.DifferencePosition;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.conf.Configuration;
import org.protege.owl.diff.conf.DefaultConfiguration;
import org.protege.owl.diff.service.RenderingService;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

public class ChebiCheck {
	public static final Logger LOGGER = Logger.getLogger(ChebiCheck.class);
	
	public static final OWLAnnotationProperty ALT_ID = OWLManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://purl.obolibrary.org/obo/alt_id"));
	
	public static final String ROOT_DIR = "/home/tredmond/Shared/ontologies/prompt/chebi";
	public static final String [] FILES = { "Chebi-1.42.owl", "Chebi-1.81.owl" };

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		checkAllOntologies();
	}
	
	public static void checkAllOntologies() throws OWLOntologyCreationException, IOException, InstantiationException, IllegalAccessException {
		for (int i = 0; i < FILES.length - 1; i++ ) {
			checkOntologies(i, i+1);
		}
	}
	
	public static void checkOntologies(int i, int j) throws OWLOntologyCreationException, IOException, InstantiationException, IllegalAccessException {
		OWLOntology ontology1 = openOntology(new File(ROOT_DIR, FILES[i]));
		OWLOntology ontology2 = openOntology(new File(ROOT_DIR, FILES[j]));
		Engine e = getEngine(ontology1, ontology2, true);
		e.phase1();
		lookForFalsePositives(e);
		lookForFalseNegatives(e);
	}
	
	public static Engine getEngine(OWLOntology ontology1, OWLOntology ontology2, boolean includeLoneSibling) throws IOException, InstantiationException, IllegalAccessException {
		Engine e = new Engine(ontology1, ontology2);
		AlignmentAggressiveness effort = includeLoneSibling ? AlignmentAggressiveness.AGGRESSIVE_SEARCH : AlignmentAggressiveness.MODERATE;
		Configuration configuration = new DefaultConfiguration(effort);
		configuration.configure(e);
		return e;
	}

	public static OWLOntology openOntology(File file) throws OWLOntologyCreationException {
		LOGGER.info("Loading ontology from " + file);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		return manager.loadOntologyFromOntologyDocument(file);
	}
	
	public static void lookForFalsePositives(Engine e) {
		OwlDiffMap diffs = e.getOwlDiffMap();
		int good = 0;
		int bad  = 0;
		for (Entry<OWLEntity, OWLEntity> entry : diffs.getEntityMap().entrySet()) {
			OWLEntity source = entry.getKey();
			OWLEntity target = entry.getValue();
			if (source == null || target == null) {
				continue;
			}
			if (!source.equals(target)) {
				if (verifyMatch(e, source, target)) {
					good++;
				}
				else {
					bad++;
					LOGGER.info(getRendering(e, source, DifferencePosition.SOURCE) + " -->  " + getRendering(e, target, DifferencePosition.TARGET) +
							" is a bad match. (" + diffs.getExplanation(source).getExplanation() + ")");

				}
			}
		}
		LOGGER.info(bad + " false positives out of " + (good + bad) + " total refactors found");
	}
	
	public static String getRendering(Engine e, OWLEntity entity, DifferencePosition position) {
		RenderingService renderer = RenderingService.get(e);
		return position == DifferencePosition.SOURCE ? renderer.renderSourceObject(entity) : renderer.renderTargetObject(entity);
	}
	
	public static boolean verifyMatch(Engine e, OWLEntity source, OWLEntity target) {
		OwlDiffMap diffs = e.getOwlDiffMap();
		OWLOntology ontology2 = diffs.getTargetOntology();
		for (OWLAnnotation annotation : target.getAnnotations(ontology2, ALT_ID)) {
			IRI altIri = getAltIri(annotation.getValue());
			if (altIri != null && source.getIRI().equals(altIri)) {
				return true;
			}
		}
		return false;
	}
	
	public static void lookForFalseNegatives(Engine e) {
		OwlDiffMap diffs = e.getOwlDiffMap();
		int bad = 0;
		for (OWLEntity targetEntity : diffs.getUnmatchedTargetEntities()) {
			OWLEntity sourceEntity = getMatchingSource(e, targetEntity);
			if (sourceEntity != null) {
				LOGGER.info("Difference engine missed a match: " +
						getRendering(e, sourceEntity, DifferencePosition.SOURCE) +
						" --> " + getRendering(e, targetEntity, DifferencePosition.TARGET));
				bad++;
			}
		}
		LOGGER.info(bad + " missed alignments.");
	}
	
	public static OWLEntity getMatchingSource(Engine e, OWLEntity targetEntity) {
		OWLDataFactory factory = e.getOWLDataFactory();
		OwlDiffMap diffs = e.getOwlDiffMap();
		OWLOntology ontology2 = diffs.getTargetOntology();
		for (OWLAnnotation annotation : targetEntity.getAnnotations(ontology2, ALT_ID)) {
			IRI altIri = getAltIri(annotation.getValue());
			if (altIri != null) {
				OWLEntity sourceEntity = factory.getOWLEntity(targetEntity.getEntityType(), altIri);
				if (diffs.getUnmatchedSourceEntities().contains(sourceEntity)) {
					return sourceEntity;
				}
			}
		}
		return null;
	}
	
	public static IRI getAltIri(OWLAnnotationValue value) {
		if (value instanceof OWLLiteral &&
				(((OWLLiteral) value).getDatatype().getIRI().equals(OWL2Datatype.XSD_STRING.getIRI()) ||
						((OWLLiteral) value).getDatatype().isRDFPlainLiteral())) {
			String altId = ((OWLLiteral) value).getLiteral();
			String altIri = "http://purl.obolibrary.org/obo/" + altId.replace(":", "_");
			return IRI.create(altIri);
		}
		return null;
	}
	
}
