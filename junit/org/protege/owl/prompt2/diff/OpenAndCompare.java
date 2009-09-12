package org.protege.owl.prompt2.diff;

import java.io.File;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.protege.owl.prompt2.analyzer.ChangeAnalyzer;
import org.protege.owl.prompt2.analyzer.EntityBasedDiff;
import org.protege.owl.prompt2.analyzer.EntityBasedDiff.DiffType;
import org.protege.owl.prompt2.diff.algorithms.MatchByCode;
import org.protege.owl.prompt2.diff.algorithms.MatchById;
import org.protege.owl.prompt2.diff.util.StopWatch;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class OpenAndCompare {
    private static Logger logger = Logger.getLogger(OpenAndCompare.class);
    // private static File f1 = new File("/home/tredmond/Shared/ontologies/NCI/2009-07-08/BGT_UAT3_file.owl");
    // private static File f2 = new File("/home/tredmond/Shared/ontologies/NCI/2009-07-08/BiomedGT_baseline.owl");

    private static File f1 = new File("/home/tredmond/Shared/ontologies/NCI/Thesaurus-08.06d.owl");
    private static File f2 = new File("/home/tredmond/Shared/ontologies/NCI/Thesaurus-08.07d.owl");
    // private static File f1 = new File("/Users/tredmond/Shared/ontologies/simple/pizza-good.owl");
    // private static File f2 = new File("/Users/tredmond/Shared/ontologies/simple/pizza.owl");

    
    /**
     * @param args
     * @throws OWLOntologyCreationException 
     */
    public static void main(String[] args) throws OWLOntologyCreationException {
        Properties p = new Properties();
        p.put(MatchByCode.USE_CODE_PROPERTY, "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#code");
        
        StopWatch watch = new StopWatch(Logger.getLogger(OpenAndCompare.class));
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        manager.setSilentMissingImportsHandling(true);
        logger.info("Loading " + f1);
        OWLOntology ontology1 = manager.loadOntologyFromPhysicalURI(f1.toURI());
        watch.measure();
        logger.info("Loading " + f2);
        OWLOntology ontology2 = manager.loadOntologyFromPhysicalURI(f2.toURI());
        watch.measure();
        logger.info("Running prompt diff");
        Engine e = new Engine(manager, ontology1, ontology2, p);
        e.setDiffAlgorithms(new DiffAlgorithm[] {
           new MatchByCode(),
           new MatchById()     
        });
        e.run();
        watch.measure();
        OwlDiffMap diffs = e.getOwlDiffMap();
        logger.info("Collecting by entity");
        ChangeAnalyzer analyzer = new ChangeAnalyzer(diffs);
        Collection<EntityBasedDiff> ediffs = analyzer.getEntityBasedDiffs();
        for (EntityBasedDiff ediff : ediffs) {
            if (ediff.getDiffType() != DiffType.EQUIVALENT) {
                logger.info(ediff.getDescription());
            }
        }
        watch.finish();
    }

}
