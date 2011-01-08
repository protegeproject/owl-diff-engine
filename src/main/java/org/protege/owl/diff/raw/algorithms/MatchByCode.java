package org.protege.owl.diff.raw.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.protege.owl.diff.raw.DiffAlgorithm;
import org.protege.owl.diff.raw.OwlDiffMap;
import org.protege.owl.diff.raw.util.DiffAlgorithmComparator;
import org.protege.owl.diff.service.CodeToEntityMapper;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

public class MatchByCode implements DiffAlgorithm {
    private Logger logger = Logger.getLogger(MatchByCode.class);
    
    private boolean disabled = false;
    private OwlDiffMap diffMap;
    private CodeToEntityMapper codeMapper;

    public int getPriority() {
        return DiffAlgorithmComparator.MAX_PRIORITY;
    }

    public void initialise(OwlDiffMap diffMap, Properties parameters) {
        this.diffMap = diffMap;
        codeMapper = CodeToEntityMapper.generateCodeToEntityMap(diffMap, parameters);
        if (codeMapper.codeNotPresent()) {
        	disabled = true;
        }
    }

    public void run() {
        if (!disabled) {
            diffMap.announce(this);
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Generating map of codes to owl entities for target ontology");
                }
                Map<String, Collection<OWLEntity>> codeToEntityMap = codeMapper.getTargetCodeToEntityMap();
                if (logger.isDebugEnabled()) {
                    logger.debug("Matching source entities with target entities");
                }
                matchEntities(codeToEntityMap);
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
    
    private boolean matchEntities(Map<String, Collection<OWLEntity>> targetCodeToEntitiesMap) {
        Map<OWLEntity, OWLEntity> matchMap = new HashMap<OWLEntity, OWLEntity>();
        OWLOntology sourceOntology = diffMap.getSourceOntology();
        for (OWLEntity sourceEntity : sourceOntology.getSignature()) {
            String code = codeMapper.getCode(sourceOntology, sourceEntity);
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
    

    

    
    public String getAlgorithmName() {
        return "Match By Code";
    }

}
