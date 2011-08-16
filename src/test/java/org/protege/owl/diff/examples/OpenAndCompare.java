package org.protege.owl.diff.examples;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.protege.owl.diff.DifferencePosition;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.conf.DefaultConfiguration;
import org.protege.owl.diff.service.RenderingService;
import org.protege.owl.diff.util.StopWatch;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class OpenAndCompare {
    private static Logger logger = Logger.getLogger(OpenAndCompare.class);

    private final static File root     = new File("/home/tredmond/Shared/ontologies/prompt");
    private final static File baseline = new File(root, "BiomedicalResourceOntology_v2.6_v2.6.owl");
    private final static File altered  = new File(root, "BRO_v3.2.1_v3.2.1.owl");

    // private static File f1 = new File("/home/tredmond/Shared/ontologies/NCI/Thesaurus-09.12d.owl");
    // private static File f2 = new File("/home/tredmond/Shared/ontologies/NCI/Thesaurus-10.04f.owl");
    // private static File f1 = new File("/Users/tredmond/Shared/ontologies/simple/pizza-good.owl");
    // private static File f2 = new File("/Users/tredmond/Shared/ontologies/simple/pizza.owl");

    
    /**
     * @param args
     * @throws OWLOntologyCreationException 
     * @throws IOException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public static void main(String[] args) throws OWLOntologyCreationException, InstantiationException, IllegalAccessException, IOException {
        StopWatch watch = new StopWatch(Logger.getLogger(OpenAndCompare.class));
        OWLOntologyLoaderConfiguration configuration = new OWLOntologyLoaderConfiguration();
        OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();
        logger.info("Loading " + baseline);
        OWLOntology ontology1 = manager1.loadOntologyFromOntologyDocument(new FileDocumentSource(baseline), configuration);
        watch.measure();
        OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
        logger.info("Loading " + altered);
        OWLOntology ontology2 = manager2.loadOntologyFromOntologyDocument(new FileDocumentSource(altered), configuration);
        watch.measure();
        logger.info("Running diff");
        Engine e = new Engine(ontology1, ontology2);
        new DefaultConfiguration().configure(e);
        e.phase1();
        watch.measure();
        logger.info("Calculating presentation");
        e.phase2();
        e.display();
        // displayPhase1(e);
        watch.finish();
    }
    
    public static void displayPhase1(Engine e) {
    	logger.info("-------------------displaying phase 1 results ----------------------");
    	OwlDiffMap diffs = e.getOwlDiffMap();
    	RenderingService renderer = RenderingService.get(e);
    	for (Entry<OWLEntity, OWLEntity> entry : new TreeMap<OWLEntity, OWLEntity>(diffs.getEntityMap()).entrySet()) {
    		OWLEntity source = entry.getKey();
    		OWLEntity target = entry.getValue();
    		if (!source.equals(target)) {
    			logger.info("Renamed entity with readable name " + renderer.renderSourceObject(source) + " and name\n\t" + source.getIRI() 
    					+ "\nto entity with readable name " + renderer.renderTargetObject(target) + " with name \n\t" + target.getIRI());
    		}
    	}
    	Map<OWLAxiom, DifferencePosition> axiomMap = new HashMap<OWLAxiom, DifferencePosition>();
    	for (OWLAxiom axiom : diffs.getUnmatchedSourceAxioms()) {
    		axiomMap.put(axiom, DifferencePosition.SOURCE);
    	}
    	for (OWLAxiom axiom : diffs.getUnmatchedTargetAxioms()) {
    		axiomMap.put(axiom, DifferencePosition.TARGET);
    	}
    	int counter = 0;
    	for (Entry<OWLAxiom, DifferencePosition> entry : axiomMap.entrySet()) {
    		OWLAxiom axiom = entry.getKey();
    		DifferencePosition position = entry.getValue();
    		counter++;
    		String status = (position == DifferencePosition.SOURCE ? "Deleted" : "Added");
    		String pretty = (position == DifferencePosition.SOURCE ? renderer.renderSourceObject(axiom) : renderer.renderTargetObject(axiom));
    		logger.info(status + " axiom:\n\t" + pretty + "\n");
    	}
    	logger.info(counter + " axioms deleted or added.");
    	logger.info("-------------------done----------------------");

    }

}
