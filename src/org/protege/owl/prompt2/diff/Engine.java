package org.protege.owl.prompt2.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.protege.owl.prompt2.diff.impl.OwlDiffMapImpl;
import org.protege.owl.prompt2.diff.util.DiffAlgorithmComparator;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Engine {
    private Logger logger = Logger.getLogger(Engine.class);    
    private OwlDiffMap diffMap;
    private List<DiffAlgorithm> algorithms = new ArrayList<DiffAlgorithm>();
    private Properties parameters;
    
    public Engine(OWLOntologyManager manager, 
                  OWLOntology ontology1, 
                  OWLOntology ontology2,
                  Properties parameters) {   
        diffMap = new OwlDiffMapImpl(manager.getOWLDataFactory(), ontology1, ontology2);
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
                catch (Throwable t) {
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
            catch (Throwable t) {
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