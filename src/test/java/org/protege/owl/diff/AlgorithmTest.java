package org.protege.owl.diff;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.protege.owl.diff.raw.DiffAlgorithm;
import org.protege.owl.diff.raw.Engine;
import org.protege.owl.diff.raw.OwlDiffMap;
import org.protege.owl.diff.raw.algorithms.MatchByCode;
import org.protege.owl.diff.raw.algorithms.MatchById;
import org.protege.owl.diff.raw.algorithms.MatchStandardVocabulary;
import org.protege.owl.diff.raw.algorithms.SuperSubClassPinch;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;


public class AlgorithmTest extends TestCase {
    public static final String PROJECTS_DIRECTORY="junit/ontologies/";
    private OWLDataFactory factory;
    private OWLOntology ontology1;
    private OWLOntology ontology2;
    
    private void loadOntologies(String prefix) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology1 = manager.loadOntologyFromOntologyDocument(new File(PROJECTS_DIRECTORY + prefix + "-Left.owl"));
        ontology2 = manager.loadOntologyFromOntologyDocument(new File(PROJECTS_DIRECTORY + prefix + "-Right.owl"));
        factory = manager.getOWLDataFactory();
    }
    
    public void testPureCodes() throws OWLOntologyCreationException {
        JunitUtilities.printDivider();
        loadOntologies("UseCode");
        Properties parameters = new Properties();
        parameters.setProperty(MatchByCode.USE_CODE_PROPERTY, "http://www.tigraworld.com/protege/UseCode#code");
        Engine e = new Engine(factory, ontology1, ontology2, parameters);
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
        Engine e = new Engine(factory, ontology1, ontology2, parameters);
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
        Engine e = new Engine(factory, ontology1, ontology2, parameters);
        e.setDiffAlgorithms(new DiffAlgorithm[] { new MatchByCode(), new MatchById() });
        e.run();
        OwlDiffMap diffs = e.getOwlDiffMap();
        assertTrue(diffs.getUnmatchedSourceEntities().size() == 0);
        assertTrue(diffs.getUnmatchedTargetEntities().size() == 0);
        assertTrue(diffs.getUnmatchedSourceAxioms().isEmpty());
        assertTrue(diffs.getUnmatchedTargetAxioms().isEmpty());
    }
    
    public void testParentsAndChildren() throws OWLOntologyCreationException {
        JunitUtilities.printDivider();
        loadOntologies("ParentsAndChildren");
        Properties parameters = new Properties();
        parameters.setProperty(MatchByCode.USE_CODE_PROPERTY, 
                               "http://www.tigraworld.com/protege/ParentsAndChildren.owl#code");
        parameters.setProperty(SuperSubClassPinch.REQUIRED_SUBCLASSES_PROPERTY, "2");
        Engine e = new Engine(factory, ontology1, ontology2, parameters);
        CountEntityMatchesListener listener = new CountEntityMatchesListener();
        e.getOwlDiffMap().addDiffListener(listener);
        e.setDiffAlgorithms(new DiffAlgorithm[] { 
                new MatchByCode(),
                new MatchStandardVocabulary(),
                new SuperSubClassPinch() 
        });
        
        e.run();
        
        OwlDiffMap diffs = e.getOwlDiffMap();
        assertTrue(diffs.getUnmatchedSourceEntities().size() == 0);
        assertTrue(diffs.getUnmatchedTargetEntities().size() == 0);
        assertTrue(diffs.getUnmatchedSourceAxioms().isEmpty());
        assertTrue(diffs.getUnmatchedTargetAxioms().isEmpty());
        
        List<Set<OWLEntity>> matches = listener.getEntityMatches();
        assertTrue(matches.size() == 4);
        assertTrue(matches.get(2).size() == 1);
        assertTrue(matches.get(2).iterator().next().getIRI().getFragment().equals("BL"));
        assertTrue(matches.get(3).size() == 1);
        assertTrue(matches.get(3).iterator().next().getIRI().getFragment().equals("EL"));
    }
}
