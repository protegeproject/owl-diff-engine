package org.protege.owl.diff;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.protege.owl.diff.align.algorithms.MatchByCode;
import org.protege.owl.diff.align.algorithms.MatchById;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.MatchDescription;
import org.protege.owl.diff.present.MatchedAxiom;
import org.protege.owl.diff.present.EntityBasedDiff.DiffType;
import org.protege.owl.diff.present.algorithms.IdentifyMergedConcepts;
import org.protege.owl.diff.present.algorithms.IdentifyRetiredConcepts;
import org.protege.owl.diff.service.CodeToEntityMapper;
import org.protege.owl.diff.service.RetirementClassService;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class PresentationAlgorithmTest extends TestCase {
    private OWLDataFactory factory;
    private OWLOntology ontology1;
    private OWLOntology ontology2;
    
    private void loadOntologies(String prefix) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology1 = manager.loadOntologyFromOntologyDocument(new File(JunitUtilities.PROJECTS_DIRECTORY + prefix + "-Left.owl"));
        OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
        ontology2 = manager2.loadOntologyFromOntologyDocument(new File(JunitUtilities.PROJECTS_DIRECTORY + prefix + "-Right.owl"));
        factory = manager.getOWLDataFactory();
    }
    
    public void testMerge() throws OWLOntologyCreationException {
    	String ns = "http://protege.org/ontologies/Merge.owl";
    	loadOntologies("Merge");
    	Properties p = new Properties();
    	p.setProperty(CodeToEntityMapper.CODE_ANNOTATION_PROPERTY, ns + "#code");
    	p.setProperty(RetirementClassService.RETIREMENT_CLASS_PROPERTY, ns + "#Retired");
    	p.setProperty(RetirementClassService.RETIREMENT_STATUS_PROPERTY, ns + "#Status");
    	p.setProperty(RetirementClassService.RETIREMENT_STATUS_STRING, "Retired_Concept");
    	p.setProperty(IdentifyMergedConcepts.MERGED_INTO_ANNOTATION_PROPERTY, ns + "#Merge_Into");
    	Engine e = new Engine(factory, ontology1, ontology2, p);
    	e.setAlignmentAlgorithms(new MatchByCode(), new MatchById());
    	e.setPresentationAlgorithms(new IdentifyMergedConcepts());
    	e.phase1();
    	e.phase2();
    	Changes changes = e.getChanges();
    	EntityBasedDiff keptEntityDiffs = changes.getSourceDiffMap().get(factory.getOWLClass(IRI.create(ns + "#A")));
    	EntityBasedDiff retiredEntityDiffs = changes.getSourceDiffMap().get(factory.getOWLClass(IRI.create(ns + "#B")));

    	int mergeCount = 0;
    	int retiredCount = 0;
    	for (MatchedAxiom match : retiredEntityDiffs.getAxiomMatches()) {
    		MatchDescription description = match.getDescription();
    		if (description.equals(IdentifyMergedConcepts.MERGE)) {
    			mergeCount++;
    			assertTrue(keptEntityDiffs.getAxiomMatches().contains(match));
    		}
    		else if (description.equals(IdentifyMergedConcepts.RETIRED_DUE_TO_MERGE)) {
    			retiredCount++;
    		}
    	}
    	assertTrue(mergeCount == 1);
    	assertTrue(retiredCount == 2);
    	
    	int modifiedCount = 0;
    	for (MatchedAxiom match : keptEntityDiffs.getAxiomMatches()) {
    		if (match.getDescription().equals(IdentifyMergedConcepts.MERGE_AXIOM)) {
    			modifiedCount++;
    		}
    	}
    	assertTrue(modifiedCount == 1);
    	e.display();
    }
    
    /*
     * This is the same as the previous test except the retirement step is at a lower priority and
     * does not kick in.
     */
    public void testMergeWithVacuousRetire() throws OWLOntologyCreationException {
    	String ns = "http://protege.org/ontologies/Merge.owl";
    	loadOntologies("Merge");
    	
    	Properties p = new Properties();
    	p.setProperty(CodeToEntityMapper.CODE_ANNOTATION_PROPERTY, ns + "#code");
    	p.setProperty(RetirementClassService.RETIREMENT_CLASS_PROPERTY, ns + "#Retired");
    	p.setProperty(RetirementClassService.RETIREMENT_STATUS_PROPERTY, ns + "#Status");
    	p.setProperty(RetirementClassService.RETIREMENT_STATUS_STRING, "Retired_Concept");
    	p.setProperty(IdentifyMergedConcepts.MERGED_INTO_ANNOTATION_PROPERTY, ns + "#Merge_Into");
    	
    	Engine e = new Engine(factory, ontology1, ontology2, p);
    	e.setAlignmentAlgorithms(new MatchByCode(), new MatchById());
    	e.setPresentationAlgorithms(new IdentifyMergedConcepts(), new IdentifyRetiredConcepts());
    	e.phase1();
    	e.phase2();
    	Changes changes = e.getChanges();
    	EntityBasedDiff keptEntityDiffs = changes.getSourceDiffMap().get(factory.getOWLClass(IRI.create(ns + "#A")));
    	EntityBasedDiff retiredEntityDiffs = changes.getSourceDiffMap().get(factory.getOWLClass(IRI.create(ns + "#B")));

    	int mergeCount = 0;
    	int retiredCount = 0;
    	for (MatchedAxiom match : retiredEntityDiffs.getAxiomMatches()) {
    		MatchDescription description = match.getDescription();
    		if (description.equals(IdentifyMergedConcepts.MERGE)) {
    			mergeCount++;
    			assertTrue(keptEntityDiffs.getAxiomMatches().contains(match));
    		}
    		else if (description.equals(IdentifyMergedConcepts.RETIRED_DUE_TO_MERGE)) {
    			retiredCount++;
    		}
    	}
    	assertTrue(mergeCount == 1);
    	assertTrue(retiredCount == 2);
    	
    	int modifiedCount = 0;
    	for (MatchedAxiom match : keptEntityDiffs.getAxiomMatches()) {
    		if (match.getDescription().equals(IdentifyMergedConcepts.MERGE_AXIOM)) {
    			modifiedCount++;
    		}
    	}
    	assertTrue(modifiedCount == 1);
    	e.display();
    }
    
    public void testRetire() throws OWLOntologyCreationException {
    	String ns = "http://protege.org/ontologies/SimpleRetire.owl";
    	loadOntologies("SimpleRetire");
    	
    	Properties p = new Properties();
    	p.setProperty(RetirementClassService.RETIREMENT_CLASS_PROPERTY, ns + "#Retire");
    	p.setProperty(RetirementClassService.RETIREMENT_STATUS_STRING, "Retired_Concept");
    	p.setProperty(RetirementClassService.RETIREMENT_STATUS_PROPERTY, ns + "#Concept_Status");
    	p.setProperty(RetirementClassService.RETIREMENT_META_PROPERTIES + 0, ns + "#OLD_PARENT");
    	p.setProperty(RetirementClassService.RETIREMENT_META_PROPERTIES + 1, ns + "#OLD_CONTEXT");
    	
    	Engine e = new Engine(factory, ontology1, ontology2, p);
    	e.setAlignmentAlgorithms(new MatchById());
    	e.setPresentationAlgorithms(new IdentifyRetiredConcepts());
    	e.phase1();
    	e.phase2();
    	int retiredSubClassCount = 0;
    	int retiredAnnotationCount = 0;
    	int deletedDueToRetirementCount = 0;
    	for (EntityBasedDiff diff : e.getChanges().getEntityBasedDiffs()) {
    		if (diff.getDiffType().equals(DiffType.EQUIVALENT)) {
    			continue;
    		}
    		for (MatchedAxiom match : diff.getAxiomMatches()) {
    			if (match.getDescription().equals(IdentifyRetiredConcepts.RETIRED)) {
    				assertTrue(match.getSourceAxiom() == null);
    				if (match.getTargetAxiom() instanceof OWLSubClassOfAxiom) {
    					retiredSubClassCount++;
    				}
    				else if (match.getTargetAxiom() instanceof OWLAnnotationAssertionAxiom) {
    					retiredAnnotationCount++;
    				}
    			}
    			else if (match.getDescription().equals(IdentifyRetiredConcepts.DELETED_DUE_TO_RETIRE)) {
    				deletedDueToRetirementCount++;
    			}
    		}
    	}
    	assertTrue(retiredAnnotationCount == 4);
    	assertTrue(retiredSubClassCount == 1);
    	assertTrue(deletedDueToRetirementCount == 1);
    	e.display();
	}
}
