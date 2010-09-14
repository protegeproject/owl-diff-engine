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

    public void run() {
        if (!disabled) {
            diffMap.announce(this);
            try {
                Map<OWLEntity, OWLEntity> matchingMap = new HashMap<OWLEntity, OWLEntity>();
                final OWLOntology targetOntology = diffMap.getTargetOntology();
                for (OWLEntity unmatchedSourceEntity : diffMap.getUnmatchedSourceEntities()) {
                    boolean found = unmatchedSourceEntity.accept(new OWLEntityVisitorEx<Boolean>() {

                        public Boolean visit(OWLClass sourceEntity) {
                            return targetOntology.containsClassInSignature(sourceEntity.getIRI());
                        }

                        public Boolean visit(OWLObjectProperty property) {
                            return targetOntology.containsObjectPropertyInSignature(property.getIRI());
                        }

                        public Boolean visit(OWLDataProperty property) {
                            return targetOntology.containsDataPropertyInSignature(property.getIRI());
                        }

                        public Boolean visit(OWLAnnotationProperty property) {
                            return targetOntology.containsAnnotationPropertyInSignature(property.getIRI());
                        }

                        public Boolean visit(OWLNamedIndividual individual) {
                            return targetOntology.containsIndividualInSignature(individual.getIRI());
                        }

                        public Boolean visit(OWLDatatype datatype) {
                            return targetOntology.containsDatatypeInSignature(datatype.getIRI());
                        }
                    });
                    if (found) {
                        matchingMap.put(unmatchedSourceEntity, unmatchedSourceEntity);
                    }
                }
                diffMap.addMatchingEntities(matchingMap);
            }
            finally {
                diffMap.summarize();
            }
        }
        disabled = true;
    }
    
    public void reset() {
        disabled=false;
    }

}
