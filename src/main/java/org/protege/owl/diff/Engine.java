package org.protege.owl.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.impl.OwlDiffMapImpl;
import org.protege.owl.diff.align.util.AlignmentAlgorithmComparator;
import org.protege.owl.diff.present.Changes;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.protege.owl.diff.present.EntityBasedDiff.DiffType;
import org.protege.owl.diff.present.PresentationAlgorithm;
import org.protege.owl.diff.present.util.PresentationAlgorithmComparator;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

public class Engine {
    private Logger logger = Logger.getLogger(Engine.class);    
    
    private OWLDataFactory factory;
    private OWLOntology ontology1;
    private OWLOntology ontology2;
    private Map<String, String> parameters;
    
    private OwlDiffMap diffMap;
    private List<AlignmentAlgorithm> diffAlgorithms = new ArrayList<AlignmentAlgorithm>();

    private Changes changes;
    private List<PresentationAlgorithm> changeAlgorithms = new ArrayList<PresentationAlgorithm>();
    
    private Collection<Object> services = new ArrayList<Object>();

    
    public Engine(OWLDataFactory factory, 
                  OWLOntology ontology1, 
                  OWLOntology ontology2) {
    	this.factory = factory;
    	this.ontology1 = ontology1;
    	this.ontology2 = ontology2;
        this.parameters = new HashMap<String, String>();
    }
    
    public OWLDataFactory getOWLDataFactory() {
    	return factory;
    }
    
    public OwlDiffMap getOwlDiffMap() {
	    return diffMap;
	}

	public Changes getChanges() {
		return changes;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}
	
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

    public void setAlignmentAlgorithms(AlignmentAlgorithm... algorithms) {
	    this.diffAlgorithms.clear();
	    for (AlignmentAlgorithm algorithm : algorithms) {
	    	this.diffAlgorithms.add(algorithm);
	    }
	    Collections.sort(diffAlgorithms, new AlignmentAlgorithmComparator());
	}

	public void setPresentationAlgorithms(PresentationAlgorithm... algorithms) {
		changeAlgorithms.clear();
		for (PresentationAlgorithm algorithm : algorithms) {
			changeAlgorithms.add(algorithm);
		}
		Collections.sort(changeAlgorithms, new PresentationAlgorithmComparator());
	}

	public void addService(Object o) {
		services.add(o);
	}

	public <X> X getService(Class<? extends X> implementing) {
		for (Object o : services) {
			if (implementing.isAssignableFrom(o.getClass())) {
				return implementing.cast(o);
			}
		}
		return null;
	}

	public void display() {
	    Collection<EntityBasedDiff> ediffs = changes.getEntityBasedDiffs();
	    for (EntityBasedDiff ediff : ediffs) {
	        if (ediff.getDiffType() != DiffType.EQUIVALENT) {
	            logger.info(ediff.getDescription());
	        }
	    }
	}

	public void phase1() {
    	phase1Init();
        boolean progress;
        boolean finished = false;
        do {
            progress  = false;
            for (AlignmentAlgorithm da : diffAlgorithms) {
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
		for (AlignmentAlgorithm algorithm : diffAlgorithms) {
			algorithm.initialise(this);
		}
	}

	private void phase1Cleanup() {
	    for (AlignmentAlgorithm algorithm : diffAlgorithms) {
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

	public void phase2() {
    	phase2Init();
    	for (PresentationAlgorithm algorithm : changeAlgorithms) {
    		algorithm.apply();
    	}
    }
    
    private void phase2Init() {
    	changes = new Changes(diffMap);
    	for (PresentationAlgorithm algorithm : changeAlgorithms) {
    		algorithm.initialise(this);
    	}
    }
    
    
}
