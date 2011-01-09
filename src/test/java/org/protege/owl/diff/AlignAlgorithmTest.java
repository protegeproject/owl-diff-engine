package org.protege.owl.diff;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.algorithms.MatchByCode;
import org.protege.owl.diff.align.algorithms.MatchById;
import org.protege.owl.diff.align.algorithms.MatchStandardVocabulary;
import org.protege.owl.diff.align.algorithms.SuperSubClassPinch;
import org.protege.owl.diff.align.impl.OwlDiffMapImpl;
import org.protege.owl.diff.align.util.AlignmentAlgorithmComparator;
import org.protege.owl.diff.service.CodeToEntityMapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;


public class AlignAlgorithmTest extends TestCase {
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
    
    public void testPureCodes() throws OWLOntologyCreationException {
        JunitUtilities.printDivider();
        loadOntologies("UseCode");
        Properties parameters = new Properties();
        parameters.setProperty(CodeToEntityMapper.CODE_ANNOTATION_PROPERTY, "http://www.tigraworld.com/protege/UseCode#code");
        Engine e = new Engine(factory, ontology1, ontology2, parameters);
        e.setAlignmentAlgorithms(new AlignmentAlgorithm[] { new MatchByCode(), new MatchStandardVocabulary() });
        e.phase1();
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
        parameters.setProperty(CodeToEntityMapper.CODE_ANNOTATION_PROPERTY, "http://www.tigraworld.com/protege/UseCode#code");
        Engine e = new Engine(factory, ontology1, ontology2, parameters);
        e.setAlignmentAlgorithms(new AlignmentAlgorithm[] { new MatchByCode(), new MatchStandardVocabulary() });
        e.phase1();
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
        parameters.setProperty(CodeToEntityMapper.CODE_ANNOTATION_PROPERTY, "http://www.tigraworld.com/protege/UseCode#code");
        Engine e = new Engine(factory, ontology1, ontology2, parameters);
        e.setAlignmentAlgorithms(new AlignmentAlgorithm[] { new MatchByCode(), new MatchById() });
        e.phase1();
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
        parameters.setProperty(CodeToEntityMapper.CODE_ANNOTATION_PROPERTY, 
                               "http://www.tigraworld.com/protege/ParentsAndChildren.owl#code");
        parameters.setProperty(SuperSubClassPinch.REQUIRED_SUBCLASSES_PROPERTY, "2");
        Engine e = new Engine(factory, ontology1, ontology2, parameters);
        e.phase1(); // no algorithms are run here.
        OwlDiffMap diffMap = e.getOwlDiffMap();
        CountEntityMatchesListener listener = new CountEntityMatchesListener();
        diffMap.addDiffListener(listener);
        List<AlignmentAlgorithm> algorithms = new ArrayList<AlignmentAlgorithm>();
        algorithms.add(new MatchByCode());
        algorithms.add(new MatchStandardVocabulary());
        algorithms.add(new SuperSubClassPinch());
        Collections.sort(algorithms, new AlignmentAlgorithmComparator());
        for (int i =0 ;i < 2; i++) {
        	for (AlignmentAlgorithm algorithm : algorithms) {
        		algorithm.initialise(e);
        		algorithm.run();
        	}
        }

        assertTrue(diffMap.getUnmatchedSourceEntities().size() == 0);
        assertTrue(diffMap.getUnmatchedTargetEntities().size() == 0);
        assertTrue(diffMap.getUnmatchedSourceAxioms().isEmpty());
        assertTrue(diffMap.getUnmatchedTargetAxioms().isEmpty());
        
        List<Set<OWLEntity>> matches = listener.getEntityMatches();
        assertTrue(matches.size() == 4);
        assertTrue(matches.get(2).size() == 1);
        assertTrue(matches.get(2).iterator().next().getIRI().getFragment().equals("BL"));
        assertTrue(matches.get(3).size() == 1);
        assertTrue(matches.get(3).iterator().next().getIRI().getFragment().equals("EL"));
    }
}
