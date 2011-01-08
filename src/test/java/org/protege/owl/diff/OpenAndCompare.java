package org.protege.owl.diff;

import java.io.File;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.protege.owl.diff.analyzer.Changes;
import org.protege.owl.diff.analyzer.EntityBasedDiff;
import org.protege.owl.diff.analyzer.EntityBasedDiff.DiffType;
import org.protege.owl.diff.analyzer.algorithms.IdentifyMergedConcepts;
import org.protege.owl.diff.analyzer.algorithms.IdentifyRetiredConcepts;
import org.protege.owl.diff.raw.Engine;
import org.protege.owl.diff.raw.OwlDiffMap;
import org.protege.owl.diff.raw.algorithms.MatchByCode;
import org.protege.owl.diff.raw.algorithms.MatchById;
import org.protege.owl.diff.raw.util.StopWatch;
import org.protege.owl.diff.service.CodeToEntityMapper;
import org.protege.owl.diff.service.RetirementClassService;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class OpenAndCompare {
    private static Logger logger = Logger.getLogger(OpenAndCompare.class);

    private final static File root     = new File("/home/tredmond/Shared/ontologies/NCI/2010-11-29-cbapp-qa2");
    private final static File baseline = new File(root, "Thesaurus-101129-10.11e.owl");
    private final static File altered  = new File(root, "Thesaurus-changed-file.owl");

    // private static File f1 = new File("/home/tredmond/Shared/ontologies/NCI/Thesaurus-09.12d.owl");
    // private static File f2 = new File("/home/tredmond/Shared/ontologies/NCI/Thesaurus-10.04f.owl");
    // private static File f1 = new File("/Users/tredmond/Shared/ontologies/simple/pizza-good.owl");
    // private static File f2 = new File("/Users/tredmond/Shared/ontologies/simple/pizza.owl");

    
    /**
     * @param args
     * @throws OWLOntologyCreationException 
     */
    public static void main(String[] args) throws OWLOntologyCreationException {
        Properties p = new Properties();
        p.put(MatchByCode.ALIGN_USING_CODE_PROPERTY, "True");
        p.put(CodeToEntityMapper.CODE_ANNOTATION_PROPERTY, "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#code");
        p.put(IdentifyMergedConcepts.MERGED_INTO_ANNOTATION_PROPERTY, "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Merge_Into");
        p.put(RetirementClassService.RETIREMENT_CLASS_PROPERTY, "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Concept_Status");
        p.put(RetirementClassService.RETIREMENT_STATUS_STRING, "Retired_Concept");
        p.put(RetirementClassService.RETIREMENT_CLASS_PROPERTY, "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Retired_Concept_");
        
        StopWatch watch = new StopWatch(Logger.getLogger(OpenAndCompare.class));
        OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();
        manager1.setSilentMissingImportsHandling(true);
        logger.info("Loading " + baseline);
        OWLOntology ontology1 = manager1.loadOntologyFromOntologyDocument(baseline);
        watch.measure();
        OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
        manager2.setSilentMissingImportsHandling(true);
        logger.info("Loading " + altered);
        OWLOntology ontology2 = manager2.loadOntologyFromOntologyDocument(altered);
        watch.measure();
        logger.info("Running diff");
        Engine e = new Engine(manager1.getOWLDataFactory(), ontology1, ontology2, p);
        e.setDiffAlgorithms(new MatchByCode(), new MatchById());
        e.run();
        watch.measure();
        OwlDiffMap diffs = e.getOwlDiffMap();
        logger.info("Collecting by entity");
        Changes analyzer = new Changes(diffs, p);
        
        analyzer.setAlgorithms(new IdentifyMergedConcepts(), new IdentifyRetiredConcepts());
        analyzer.runAlgorithms();
       
        Collection<EntityBasedDiff> ediffs = analyzer.getEntityBasedDiffs();
        for (EntityBasedDiff ediff : ediffs) {
            if (ediff.getDiffType() != DiffType.EQUIVALENT) {
                logger.info(ediff.getDescription());
            }
        }
        watch.finish();
    }

}
