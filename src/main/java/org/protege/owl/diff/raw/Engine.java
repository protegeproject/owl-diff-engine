package org.protege.owl.diff.raw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.protege.owl.diff.raw.impl.OwlDiffMapImpl;
import org.protege.owl.diff.raw.util.DiffAlgorithmComparator;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

public class Engine {
    private Logger logger = Logger.getLogger(Engine.class);    
    private OwlDiffMap diffMap;
    private List<DiffAlgorithm> algorithms = new ArrayList<DiffAlgorithm>();
    private Properties parameters;
    
    public Engine(OWLDataFactory factory, 
                  OWLOntology ontology1, 
                  OWLOntology ontology2,
                  Properties parameters) {   
        diffMap = new OwlDiffMapImpl(factory, ontology1, ontology2);
        this.parameters = parameters;
    }


    public void run() {
        boolean progress;
        boolean finished = false;
        do {
            progress  = false;
            Collections.sort(algorithms, new DiffAlgorithmComparator());
            for (DiffAlgorithm da : algorithms) {
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
        
        for (DiffAlgorithm algorithm : algorithms) {
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
    
    public void setDiffAlgorithms(DiffAlgorithm [] algorithms) {
        this.algorithms.clear();
        for (DiffAlgorithm algorithm : algorithms) {
            algorithm.initialise(diffMap, parameters);
            this.algorithms.add(algorithm);
        }
    }
}
