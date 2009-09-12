package org.protege.owl.prompt2.diff.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.owl.prompt2.diff.DiffAlgorithm;
import org.protege.owl.prompt2.diff.OwlDiffMap;
import org.protege.owl.prompt2.diff.util.DiffAlgorithmComparator;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLStringLiteral;
import org.semanticweb.owlapi.model.OWLTypedLiteral;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

public class MatchByCode implements DiffAlgorithm {
    private Logger logger = Logger.getLogger(MatchByCode.class);
    
    public static final String USE_CODE_PROPERTY = "diff.by.code";
    
    private boolean disabled = false;
    private OwlDiffMap diffMap;
    private OWLAnnotationProperty codeProperty;

    public int getPriority() {
        return DiffAlgorithmComparator.MAX_PRIORITY;
    }

    public void initialise(OwlDiffMap diffMap, Properties parameters) {
        this.diffMap = diffMap;
        OWLDataFactory factory = diffMap.getOWLDataFactory();
        Object codeName = parameters.get(USE_CODE_PROPERTY);
        if (codeName == null || !(codeName instanceof String)) {
            disabled = true;
        }
        else {
            IRI codeIri = IRI.create((String) codeName);
            codeProperty = factory.getOWLAnnotationProperty(codeIri);
            if (!diffMap.getSourceOntology().containsAnnotationPropertyReference(codeIri)) {
                logger.warn("Source ontology does not have selected code annotation " + codeName);
                disabled = true;
            }
            else if (!diffMap.getTargetOntology().containsAnnotationPropertyReference(codeIri)) {
                logger.warn("Target ontology does not have selected code annotation " + codeName);
                disabled = true;
            }
        }
    }

    public boolean run() {
        boolean progress = false;
        if (!disabled) {
            diffMap.announce(this);
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Generating map of codes to owl entities for target ontology");
                }
                Map<String, Collection<OWLEntity>> codeToEntityMap = generateCodeToEntityMap(diffMap.getTargetOntology());
                if (logger.isDebugEnabled()) {
                    logger.debug("Matching source entities with target entities");
                }
                progress = matchEntities(codeToEntityMap);
            }
            finally {
                diffMap.summarize();
            }
        }
        disabled = true;
        return progress;
    }
    
    private boolean matchEntities(Map<String, Collection<OWLEntity>> targetCodeToEntitiesMap) {
        Map<OWLEntity, OWLEntity> matchMap = new HashMap<OWLEntity, OWLEntity>();
        OWLOntology sourceOntology = diffMap.getSourceOntology();
        for (OWLEntity sourceEntity : sourceOntology.getReferencedEntities()) {
            String code = getCode(sourceOntology, sourceEntity);
            if (code == null) {
                continue;
            }
            Collection<OWLEntity> targetEntities = targetCodeToEntitiesMap.get(code);
            if (targetEntities == null) {
                continue;
            }
            OWLEntity matchedTargetEntity = null;
            for (OWLEntity potentialTargetEntity : targetEntities) {
                if (isMatch(sourceEntity, potentialTargetEntity)) {
                    matchedTargetEntity = potentialTargetEntity;
                    continue;
                }
            }
            if (matchedTargetEntity != null) {
                targetEntities.remove(matchedTargetEntity);
                matchMap.put(sourceEntity, matchedTargetEntity);
            }
            
        }
        if (matchMap.isEmpty()) {
            return false;
        }
        else {
            diffMap.addMatchingEntities(matchMap);
            return true;
        }
    }
    
    private boolean isMatch(OWLEntity entity1, final OWLEntity entity2) {
        return entity1.accept(new OWLEntityVisitorEx<Boolean>() {

            public Boolean visit(OWLClass cls) {
                return entity2.isOWLClass();
            }

            public Boolean visit(OWLObjectProperty property) {
                return entity2.isOWLObjectProperty();
            }

            public Boolean visit(OWLDataProperty property) {
                return entity2.isOWLDataProperty();
            }

            public Boolean visit(OWLNamedIndividual individual) {
                return entity2.isOWLNamedIndividual();
            }

            public Boolean visit(OWLDatatype datatype) {
                return entity2.isOWLDatatype();
            }

            public Boolean visit(OWLAnnotationProperty property) {
                return entity2.isOWLAnnotationProperty();
            }
            
        });
    }
    
    private Map<String, Collection<OWLEntity>> generateCodeToEntityMap(OWLOntology ontology) {
        Map<String, Collection<OWLEntity>> codeToEntity  = new HashMap<String, Collection<OWLEntity>>();
        for (OWLEntity entity : ontology.getReferencedEntities()) {
            String code = getCode(ontology, entity);
            if (code != null) {
                Collection<OWLEntity> entities = codeToEntity.get(code);
                if (entities == null) {
                    entities = new ArrayList<OWLEntity>();
                    codeToEntity.put(code, entities);
                }
                entities.add(entity);
            }
        }
        return codeToEntity;
    }
    
    private String getCode(OWLOntology ontology, OWLEntity entity) {
        for (OWLAnnotation annotation : entity.getAnnotations(ontology)) {
            if (!annotation.getProperty().equals(codeProperty)) {
                continue;
            }
            OWLAnnotationValue value = annotation.getValue();
            if (value instanceof OWLStringLiteral) {
                return ((OWLStringLiteral) value).getLiteral();
            }
            else if (value instanceof OWLTypedLiteral &&
                      ((OWLTypedLiteral) value).getDatatype().getIRI().equals(OWL2Datatype.XSD_STRING.getIRI())) {
                return ((OWLTypedLiteral) value).getLiteral();
            }
        }
        return null;
    }
    
    public String getAlgorithmName() {
        return "Match By Code";
    }

}
