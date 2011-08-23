package org.protege.owl.diff.examples;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.conf.Configuration;
import org.protege.owl.diff.conf.DefaultConfiguration;
import org.protege.owl.diff.service.RenderingService;
import org.protege.owl.diff.util.DiffDuplicator;
import org.protege.owl.diff.util.Util;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class InterestingChebiDifferences {
	public static final Logger LOGGER = Logger.getLogger(InterestingChebiDifferences.class);
		
	public static final boolean USE_LONE_UNMATCHED_SIBLING = true;
	
	public static final String ROOT_DIR = "/home/tredmond/Shared/ontologies/prompt/chebi";
	public static final String FILE1    = "chebi_v1.42.owl";
	public static final String FILE2    = "chebi_v1.82.obo";

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Engine e = calculateDiffs();
		searchForAnInterestingMatch(e);
	}
	
	public static Engine calculateDiffs() throws IOException, InstantiationException, IllegalAccessException, OWLOntologyCreationException {
		OWLOntology ontology1 = openOntology(new File(ROOT_DIR, FILE1));
		OWLOntology ontology2 = openOntology(new File(ROOT_DIR, FILE2));
		Engine e = getEngine(ontology1, ontology2);
		e.phase1();
		e.phase2();
		LOGGER.info(Util.getStats(e));
		return e;
	}
	
	public static Engine getEngine(OWLOntology ontology1, OWLOntology ontology2) throws IOException, InstantiationException, IllegalAccessException {
		Engine e = new Engine(ontology1, ontology2);
		AlignmentAggressiveness effort = USE_LONE_UNMATCHED_SIBLING ? AlignmentAggressiveness.AGGRESSIVE_SEARCH : AlignmentAggressiveness.MODERATE;
		Configuration configuration = new DefaultConfiguration(effort);
		configuration.configure(e);
		return e;
	}

	public static OWLOntology openOntology(File file) throws OWLOntologyCreationException {
		LOGGER.info("Loading ontology from " + file);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		return manager.loadOntologyFromOntologyDocument(file);
	}
	
	public static void searchForAnInterestingMatch(Engine e) {
		OwlDiffMap diffs = e.getOwlDiffMap();
		OWLOntology ontology1 = diffs.getSourceOntology();
		DiffDuplicator duplicator = new DiffDuplicator(diffs);
		RenderingService renderer = RenderingService.get(e);
		
		for (OWLSubClassOfAxiom sourceAxiom : ontology1.getAxioms(AxiomType.SUBCLASS_OF)) {
			if (sourceAxiom.getSuperClass().isAnonymous() && !diffs.getUnmatchedSourceAxioms().contains(sourceAxiom)) {
				OWLSubClassOfAxiom targetAxiom = duplicator.duplicateObject(sourceAxiom);
				if (!sourceAxiom.equals(targetAxiom) 
						&& !renderer.renderSourceObject(sourceAxiom).equals(renderer.renderTargetObject(targetAxiom))) {
					LOGGER.info("\nMapped\n\t" + renderer.renderSourceObject(sourceAxiom)
					                    + "\n\t\tto\n\t" + renderer.renderTargetObject(targetAxiom) + "\n");
				}
			}
		}
	}
	
}
