package org.protege.owl.prompt2.diff.algorithms;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.protege.owl.prompt2.diff.DiffAlgorithm;
import org.protege.owl.prompt2.diff.OwlDiffMap;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

public class MatchById implements DiffAlgorithm {    
    private boolean disabled = false;
    private OwlDiffMap diffMap;

    public String getAlgorithmName() {
        return "Match By Id";
    }

    public int getPriority() {
        return 9;
    }

    public void initialise(OwlDiffMap diffMap, Properties parameters) {
        this.diffMap = diffMap;
    }

    public boolean run() {
        boolean progress = false;
        if (!disabled) {
            diffMap.announce(this);
            try {
                Map<OWLEntity, OWLEntity> matchingMap = new HashMap<OWLEntity, OWLEntity>();
                final OWLOntology targetOntology = diffMap.getTargetOntology();
                for (OWLEntity unmatchedSourceEntity : diffMap.getUnmatchedSourceEntities()) {
                    boolean found = unmatchedSourceEntity.accept(new OWLEntityVisitorEx<Boolean>() {

                        public Boolean visit(OWLClass sourceEntity) {
                            return targetOntology.containsClassReference(sourceEntity.getIRI());
                        }

                        public Boolean visit(OWLObjectProperty property) {
                            return targetOntology.containsObjectPropertyReference(property.getIRI());
                        }

                        public Boolean visit(OWLDataProperty property) {
                            return targetOntology.containsDataPropertyReference(property.getIRI());
                        }

                        public Boolean visit(OWLAnnotationProperty property) {
                            return targetOntology.containsAnnotationPropertyReference(property.getIRI());
                        }

                        public Boolean visit(OWLNamedIndividual individual) {
                            return targetOntology.containsIndividualReference(individual.getIRI());
                        }

                        public Boolean visit(OWLDatatype datatype) {
                            return targetOntology.containsDatatypeReference(datatype.getIRI());
                        }
                    });
                    if (found) {
                        progress = true;
                        matchingMap.put(unmatchedSourceEntity, unmatchedSourceEntity);
                    }
                }
                if (progress) {
                    diffMap.addMatchingEntities(matchingMap);
                }
            }
            finally {
                diffMap.summarize();
            }
        }
        disabled = true;
        return progress;
    }

}
