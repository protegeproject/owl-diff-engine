package org.protege.owl.diff.align.algorithms;

import java.util.HashMap;
import java.util.Map;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.util.AlignmentAlgorithmComparator;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

public class MatchById implements AlignmentAlgorithm {    
    private boolean disabled = false;
    private OwlDiffMap diffMap;

    public String getAlgorithmName() {
        return "Match By Id";
    }

    public int getPriority() {
        return AlignmentAlgorithmComparator.MAX_PRIORITY - 1;
    }

    public void initialise(Engine e) {
        this.diffMap = e.getOwlDiffMap();
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
