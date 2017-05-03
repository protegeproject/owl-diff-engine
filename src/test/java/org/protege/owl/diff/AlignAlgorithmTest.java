package org.protege.owl.diff;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.protege.owl.diff.align.AlignmentAggressiveness;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.algorithms.MatchByCode;
import org.protege.owl.diff.align.algorithms.MatchById;
import org.protege.owl.diff.align.algorithms.MatchByIdFragment;
import org.protege.owl.diff.align.algorithms.MatchByRendering;
import org.protege.owl.diff.align.algorithms.MatchLoneSiblings;
import org.protege.owl.diff.align.algorithms.MatchSiblingsWithSimilarBrowserText;
import org.protege.owl.diff.align.algorithms.MatchStandardVocabulary;
import org.protege.owl.diff.align.algorithms.SuperSubClassPinch;
import org.protege.owl.diff.align.util.PrioritizedComparator;
import org.protege.owl.diff.service.CodeToEntityMapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLObject;


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
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(CodeToEntityMapper.CODE_ANNOTATION_PROPERTY, "http://www.tigraworld.com/protege/UseCode#code");
        Engine e = new Engine(ontology1, ontology2);
        e.setParameters(parameters);
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
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(CodeToEntityMapper.CODE_ANNOTATION_PROPERTY, "http://www.tigraworld.com/protege/UseCode#code");
        Engine e = new Engine(ontology1, ontology2);
        e.setParameters(parameters);
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
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(CodeToEntityMapper.CODE_ANNOTATION_PROPERTY, "http://www.tigraworld.com/protege/UseCode#code");
        Engine e = new Engine(ontology1, ontology2);
        e.setParameters(parameters);
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
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(CodeToEntityMapper.CODE_ANNOTATION_PROPERTY, 
                               "http://www.tigraworld.com/protege/ParentsAndChildren.owl#code");
        parameters.put(SuperSubClassPinch.REQUIRED_SUBCLASSES_PROPERTY, "2");
        Engine e = new Engine(ontology1, ontology2);
        e.setParameters(parameters);
        e.phase1(); // no algorithms are run here.
        OwlDiffMap diffMap = e.getOwlDiffMap();
        CountEntityMatchesListener listener = new CountEntityMatchesListener();
        diffMap.addDiffListener(listener);
        List<AlignmentAlgorithm> algorithms = new ArrayList<AlignmentAlgorithm>();
        algorithms.add(new MatchByCode());
        algorithms.add(new MatchStandardVocabulary());
        algorithms.add(new SuperSubClassPinch());
        Collections.sort(algorithms, new PrioritizedComparator());
        for (int i =0 ;i < 2; i++) {
        	for (AlignmentAlgorithm algorithm : algorithms) {
        		algorithm.initialize(e);
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
    
    public void testLoneUnmatchedSibling01() throws OWLOntologyCreationException {
        JunitUtilities.printDivider();
        loadOntologies("LoneUnmatchedSibling01");
        String ns = "http://protege.org/ontologies/LoneUnmatchedSibling.owl";
        Engine e = new Engine(ontology1, ontology2);
        e.setAlignmentAlgorithms(new MatchById(), new MatchStandardVocabulary(), new MatchLoneSiblings());
        e.phase1();
        OwlDiffMap diffs = e.getOwlDiffMap();
        assertTrue(diffs.getUnmatchedSourceEntities().isEmpty());
        assertTrue(diffs.getUnmatchedTargetEntities().isEmpty());
        OWLClass loneSourceSibling = factory.getOWLClass(IRI.create(ns + "#UnmatchedSiblingLeft"));
        OWLClass loneTargetSibling = factory.getOWLClass(IRI.create(ns + "#UnmatchedSiblingRight"));
        assertEquals(loneTargetSibling, diffs.getEntityMap().get(loneSourceSibling));
    }
    
    public void testLoneUnmatchedSibling02() throws OWLOntologyCreationException {
        JunitUtilities.printDivider();
        loadOntologies("LoneUnmatchedSibling02");
        String ns = "http://protege.org/ontologies/LoneUnmatchedSibling.owl";
        Engine e = new Engine(ontology1, ontology2);
        e.setAlignmentAlgorithms(new MatchById(), new MatchStandardVocabulary(), new SuperSubClassPinch(), new MatchLoneSiblings());
        e.phase1();
        OwlDiffMap diffs = e.getOwlDiffMap();
        assertTrue(diffs.getUnmatchedSourceEntities().isEmpty());
        assertTrue(diffs.getUnmatchedTargetEntities().isEmpty());
        OWLClass loneSourceSibling = factory.getOWLClass(IRI.create(ns + "#TertiaryLeft"));
        OWLClass loneTargetSibling = factory.getOWLClass(IRI.create(ns + "#TertiaryRight"));
        assertEquals(loneTargetSibling, diffs.getEntityMap().get(loneSourceSibling));
    }
    
    public void testLoneUnmatchedSibling03() throws OWLOntologyCreationException {
        JunitUtilities.printDivider();
        loadOntologies("LoneUnmatchedSibling02");
        Engine e = new Engine(ontology1, ontology2);
        e.setAlignmentAlgorithms(new MatchById(), new MatchStandardVocabulary(), new MatchLoneSiblings());
        e.phase1();
        OwlDiffMap diffs = e.getOwlDiffMap();
        assertEquals(diffs.getUnmatchedSourceEntities().size(), 2);
    }
    
    public void testLoneUnmatchedSibling04() throws OWLOntologyCreationException {
        JunitUtilities.printDivider();
        loadOntologies("LoneUnmatchedSibling03");
        String ns = "http://protege.org/ontologies/LoneUnmatchedSibling.owl";
        Engine e = new Engine(ontology1, ontology2);
        e.setAlignmentAlgorithms(new MatchById(), new MatchStandardVocabulary(), new SuperSubClassPinch(), new MatchLoneSiblings());
        e.phase1();
        OwlDiffMap diffs = e.getOwlDiffMap();
        assertEquals(2, diffs.getUnmatchedSourceEntities().size());
        assertEquals(2, diffs.getUnmatchedTargetEntities().size());
        OWLClass loneSourceSibling = factory.getOWLClass(IRI.create(ns + "#TertiaryLeft"));
        OWLClass loneTargetSibling = factory.getOWLClass(IRI.create(ns + "#TertiaryRight"));
        assertEquals(loneTargetSibling, diffs.getEntityMap().get(loneSourceSibling));
    }
    
    public void testLoneUnmatchedSibling05() throws OWLOntologyCreationException {
        JunitUtilities.printDivider();
        loadOntologies("LoneUnmatchedSibling03");
        Engine e = new Engine(ontology1, ontology2);
        e.setAlignmentAlgorithms(new MatchById(), new MatchStandardVocabulary(), new MatchLoneSiblings());
        e.phase1();
        OwlDiffMap diffs = e.getOwlDiffMap();
        assertEquals(4, diffs.getUnmatchedSourceEntities().size());
    }
    
    public void testMatchingIdFragments() throws OWLOntologyCreationException {
        JunitUtilities.printDivider();
        loadOntologies("MatchingIdFragments");
        Engine e = new Engine(ontology1, ontology2);
        e.setAlignmentAlgorithms(new MatchById(), new MatchStandardVocabulary(), new MatchByIdFragment());
        e.phase1();
        OwlDiffMap diffs = e.getOwlDiffMap();
        assertEquals(2, diffs.getUnmatchedSourceEntities().size());
        for (OWLEntity entity : diffs.getUnmatchedSourceEntities()) {
        	assertTrue(entity.getIRI().toString().endsWith("RefactoredNotMatcheable"));
        }
    }
    
    public void testMatchSiblingsWithSimilarBrowserText() throws OWLOntologyCreationException{
    	MatchSiblingsWithSimilarBrowserText match = new MatchSiblingsWithSimilarBrowserText();
    	loadOntologies("MatchingIdFragments");
    	Engine e = new Engine(ontology1, ontology2);
    	match.initialize(e);
    	String expected = "Match Siblings with approximately similar renderings";
    	String actual = match.getAlgorithmName();
    	AlignmentAggressiveness aggr = match.getAggressiveness();
    	assertEquals(AlignmentAggressiveness.MODERATE, aggr);
    	assertEquals(expected, actual);
    }
    
    public void testSuperSubclassPinch()throws OWLOntologyCreationException{
    	SuperSubClassPinch pinch = new SuperSubClassPinch();
    	loadOntologies("MatchingIdFragments");
    	Engine e = new Engine(ontology1, ontology2);
    	pinch.initialize(e);
    	AlignmentAggressiveness aggr = pinch.getAggressiveness();
    	assertEquals(AlignmentAggressiveness.MODERATE, aggr);
    }
    
    public void testMatchingByRendering() throws OWLOntologyCreationException {
        JunitUtilities.printDivider();
        loadOntologies("MatchingByRendering");
        Engine e = new Engine(ontology1, ontology2);
        e.setAlignmentAlgorithms( new MatchByRendering());
        for (AlignmentAlgorithm algorithm : e.getAlignmentAlgorithms()) {
        	assertFalse(algorithm.isCustom());
        	int expectedPrior = PrioritizedComparator.MAX_PRIORITY - 1;
        	assertEquals(expectedPrior,algorithm.getPriority());
        	assertEquals(AlignmentAggressiveness.MODERATE,algorithm.getAggressiveness());
        	String expectedName = "Match By Rendering";
        	assertEquals(expectedName,algorithm.getAlgorithmName());       	
        	
        }
        e.phase1();
        OwlDiffMap diffs = e.getOwlDiffMap();
        assertTrue(diffs.getUnmatchedSourceEntities().isEmpty());
        
    }
    
    
}
