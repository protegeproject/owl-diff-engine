package org.protege.owl.diff.raw.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.protege.owl.diff.raw.DiffAlgorithm;
import org.protege.owl.diff.raw.DiffListener;
import org.protege.owl.diff.raw.util.DiffListenerAdapter;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

public class OwlDiffMapImpl extends OwlDiffMapCore {
    private Logger logger = Logger.getLogger(OwlDiffMapImpl.class);
    
    private static final String SEPARATOR_STRING = " **************** ";
    
    private OWLDataFactory factory;
    private OWLOntology sourceOntology;
    private OWLOntology targetOntology;

    
    private int           matchedEntitiesSinceAnnounce = 0;
    private int           matchedAxiomsSinceAnnounce = 0;
    private DiffAlgorithm lastAnnouncedDiffAlgorithm;
    private long          lastAnnounceTime = -1;
    
    /*
     * Services used by more than one plugin.
     */
    private Collection<Object> services = new ArrayList<Object>();
    
    
    
    private DiffListener trackingListener = new DiffListenerAdapter() {

        public void addMatch(OWLEntity source, OWLEntity target) {
            matchedEntitiesSinceAnnounce++;
        }

        public void addMatchingEntities(Map<OWLEntity, OWLEntity> newMatches) {
            matchedEntitiesSinceAnnounce += newMatches.size();
        }

        public void addMatchedAxiom(OWLAxiom axiom) {
            matchedAxiomsSinceAnnounce++;
        }
        
    };
    
    public OwlDiffMapImpl(OWLDataFactory factory,
                          OWLOntology sourceOntology, 
                          OWLOntology targetOntology) {
        super(factory, sourceOntology, targetOntology);
        this.factory = factory;
        this.sourceOntology = sourceOntology;
        this.targetOntology = targetOntology;
        addDiffListener(trackingListener);
    }
    
    /*
     * Getters
     */
    public OWLDataFactory getOWLDataFactory() {
        return factory;
    }
    
    public OWLOntology getSourceOntology() {
        return sourceOntology;
    }

    public OWLOntology getTargetOntology() {
        return targetOntology;
    }

 
    /*
     * Services
     */
    
    public void addService(Object o) {
    	services.add(o);
    }
    
    @Override
    public <X> X getService(Class<? extends X> implementing) {
    	for (Object o : services) {
    		if (implementing.isAssignableFrom(o.getClass())) {
    			return implementing.cast(o);
    		}
    	}
    	return null;
    }


    
    /*
     * Tracking
     */
    
    public void announce(DiffAlgorithm da) {
        logger.info(SEPARATOR_STRING + da.getAlgorithmName() + SEPARATOR_STRING);
        
        lastAnnouncedDiffAlgorithm = da;
        lastAnnounceTime = System.currentTimeMillis();
        matchedAxiomsSinceAnnounce = 0;
        matchedEntitiesSinceAnnounce = 0;
    }
    
    public void summarize() {
        long took = System.currentTimeMillis() - lastAnnounceTime;
        
        logger.info("Algorithm " + lastAnnouncedDiffAlgorithm.getAlgorithmName() + " completed.");
        logger.info(lastAnnouncedDiffAlgorithm.getAlgorithmName() 
        					+ " step took "  + took + " ms.");
        if (matchedEntitiesSinceAnnounce == 0) {
            logger.info("No progress made.");
        }
        else {
            logger.info("" + matchedEntitiesSinceAnnounce + " owl entities matched up.");
            logger.info("" + matchedAxiomsSinceAnnounce + " axioms matched up.");
        }
        logger.info(SEPARATOR_STRING);
    }
    


}
