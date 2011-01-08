package org.protege.owl.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.protege.owl.diff.analyzer.AnalyzerAlgorithm;
import org.protege.owl.diff.analyzer.Changes;
import org.protege.owl.diff.analyzer.EntityBasedDiff;
import org.protege.owl.diff.analyzer.EntityBasedDiff.DiffType;
import org.protege.owl.diff.analyzer.util.AnalyzerAlgorithmComparator;
import org.protege.owl.diff.raw.DiffAlgorithm;
import org.protege.owl.diff.raw.OwlDiffMap;
import org.protege.owl.diff.raw.impl.OwlDiffMapImpl;
import org.protege.owl.diff.raw.util.DiffAlgorithmComparator;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

public class Engine {
    private Logger logger = Logger.getLogger(Engine.class);    
    
    private OWLDataFactory factory;
    private OWLOntology ontology1;
    private OWLOntology ontology2;
    private Properties parameters;
    
    private OwlDiffMap diffMap;
    private List<DiffAlgorithm> diffAlgorithms = new ArrayList<DiffAlgorithm>();

    private Changes changes;
    private List<AnalyzerAlgorithm> changeAlgorithms = new ArrayList<AnalyzerAlgorithm>();
    
    public Engine(OWLDataFactory factory, 
                  OWLOntology ontology1, 
                  OWLOntology ontology2,
                  Properties parameters) {
    	this.factory = factory;
    	this.ontology1 = ontology1;
    	this.ontology2 = ontology2;
        this.parameters = parameters;
    }
    
    public Properties getParameters() {
		return parameters;
	}

    public void phase1() {
    	phase1Init();
        boolean progress;
        boolean finished = false;
        do {
            progress  = false;
            for (DiffAlgorithm da : diffAlgorithms) {
                int entitiesCount = diffMap.getUnmatchedSourceEntities().size();
                int individualsCount = diffMap.getUnmatchedSourceAnonymousIndividuals().size();
                if (entitiesCount == 0 && individualsCount == 0) {
                    finished = true;
                    break;
                }
                try {
                    da.run();
                 }
                catch (Error e) {
                    logger.warn("Diff Algorithm " + da.getAlgorithmName()  + "failed (" + e + ").  Continuing...");
                }
                catch (Exception t) {
                    logger.warn("Diff Algorithm " + da.getAlgorithmName()  + "failed (" + t + ").  Continuing...");
                }
                progress = progress ||
                              (entitiesCount > diffMap.getUnmatchedSourceEntities().size()) ||
                              (individualsCount > diffMap.getUnmatchedSourceAnonymousIndividuals().size());
            }
        }
        while (progress && !finished);
        
        phase1Cleanup();
    }
    
    private void phase1Init() {
    	diffMap = new OwlDiffMapImpl(factory, ontology1, ontology2);
    	for (DiffAlgorithm algorithm : diffAlgorithms) {
    		algorithm.initialise(diffMap, parameters);
    	}
    }
    
    private void phase1Cleanup() {
        for (DiffAlgorithm algorithm : diffAlgorithms) {
            try {
                algorithm.reset();
            }
            catch (Error t) {
                logger.warn("Diff Algorithm " + algorithm.getAlgorithmName() + " wouldn't reset (" + t + ")");
            }
            catch (Exception t) {
                logger.warn("Diff Algorithm " + algorithm.getAlgorithmName() + " wouldn't reset (" + t + ")");
            }
        }
    }

    public OwlDiffMap getOwlDiffMap() {
        return diffMap;
    }
    
    public void setDiffAlgorithms(DiffAlgorithm... algorithms) {
        this.diffAlgorithms.clear();
        for (DiffAlgorithm algorithm : algorithms) {
        	this.diffAlgorithms.add(algorithm);
        }
        Collections.sort(diffAlgorithms, new DiffAlgorithmComparator());
    }
    
    public void phase2() {
    	phase2Init();
    	for (AnalyzerAlgorithm algorithm : changeAlgorithms) {
    		algorithm.apply();
    	}
    }
    
    public void phase2Init() {
    	changes = new Changes(diffMap, parameters);
    	for (AnalyzerAlgorithm algorithm : changeAlgorithms) {
    		algorithm.initialise(changes, parameters);
    	}
    }
    
    public Changes getChanges() {
		return changes;
	}
    
    public void setChangeAlgorithms(AnalyzerAlgorithm... algorithms) {
		changeAlgorithms.clear();
		for (AnalyzerAlgorithm algorithm : algorithms) {
			changeAlgorithms.add(algorithm);
		}
		Collections.sort(changeAlgorithms, new AnalyzerAlgorithmComparator());
	}
    
    public void display() {
        Collection<EntityBasedDiff> ediffs = changes.getEntityBasedDiffs();
        for (EntityBasedDiff ediff : ediffs) {
            if (ediff.getDiffType() != DiffType.EQUIVALENT) {
                logger.info(ediff.getDescription());
            }
        }
    }
}
