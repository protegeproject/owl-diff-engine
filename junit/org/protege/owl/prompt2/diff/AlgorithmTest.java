package org.protege.owl.prompt2.diff;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.protege.owl.prompt2.diff.algorithms.MatchByCode;
import org.protege.owl.prompt2.diff.algorithms.MatchById;
import org.protege.owl.prompt2.diff.algorithms.MatchStandardVocabulary;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;


public class AlgorithmTest extends TestCase {
    public static final String PROJECTS_DIRECTORY="junit/projects/";
    private OWLOntologyManager manager;
    private OWLOntology ontology1;
    private OWLOntology ontology2;
    
    private void loadOntologies(String prefix) throws OWLOntologyCreationException {
        manager = OWLManager.createOWLOntologyManager();
        ontology1 = manager.loadOntologyFromPhysicalURI(new File(PROJECTS_DIRECTORY + prefix + "-Left.owl").toURI());
        ontology2 = manager.loadOntologyFromPhysicalURI(new File(PROJECTS_DIRECTORY + prefix + "-Right.owl").toURI());
    }
    
    public void testPureCodes() throws OWLOntologyCreationException {
        JunitUtilities.printDivider();
        loadOntologies("UseCode");
        Properties parameters = new Properties();
        parameters.setProperty(MatchByCode.USE_CODE_PROPERTY, "http://www.tigraworld.com/protege/UseCode#code");
        Engine e = new Engine(manager, ontology1, ontology2, parameters);
        e.setDiffAlgorithms(new DiffAlgorithm[] { new MatchByCode(), new MatchStandardVocabulary() });
        e.run();
        OwlDiffMap diffs = e.getOwlDiffMap();
        assertTrue(diffs.getUnmatchedSourceEntities().isEmpty());
        assertTrue(diffs.getUnmatchedTargetEntities().isEmpty());
        assertTrue(diffs.getUnmatchedSourceAxioms().isEmpty());
        assertTrue(diffs.getUnmatchedTargetAxioms().isEmpty());
    }
    
    public void testPureCodesInsufficient() throws OWLOntologyCreationException {
        JunitUtilities.printDivider();
        loadOntologies("UseCodeAndName");
        Properties parameters = new Properties();
        parameters.setProperty(MatchByCode.USE_CODE_PROPERTY, "http://www.tigraworld.com/protege/UseCode#code");
        Engine e = new Engine(manager, ontology1, ontology2, parameters);
        e.setDiffAlgorithms(new DiffAlgorithm[] { new MatchByCode(), new MatchStandardVocabulary() });
        e.run();
        OwlDiffMap diffs = e.getOwlDiffMap();
        assertTrue(diffs.getUnmatchedSourceEntities().size() == 1);
        assertTrue(diffs.getUnmatchedTargetEntities().size() == 1);
        assertTrue(!diffs.getUnmatchedSourceAxioms().isEmpty());
        assertTrue(!diffs.getUnmatchedTargetAxioms().isEmpty());
    }
    
    public void testPureCodesAndName() throws OWLOntologyCreationException {
        JunitUtilities.printDivider();
        loadOntologies("UseCodeAndName");
        Properties parameters = new Properties();
        parameters.setProperty(MatchByCode.USE_CODE_PROPERTY, "http://www.tigraworld.com/protege/UseCode#code");
        Engine e = new Engine(manager, ontology1, ontology2, parameters);
        e.setDiffAlgorithms(new DiffAlgorithm[] { new MatchByCode(), new MatchById() });
        e.run();
        OwlDiffMap diffs = e.getOwlDiffMap();
        assertTrue(diffs.getUnmatchedSourceEntities().size() == 0);
        assertTrue(diffs.getUnmatchedTargetEntities().size() == 0);
        assertTrue(diffs.getUnmatchedSourceAxioms().isEmpty());
        assertTrue(diffs.getUnmatchedTargetAxioms().isEmpty());
    }
    
}
