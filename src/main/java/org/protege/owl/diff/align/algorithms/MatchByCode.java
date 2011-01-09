package org.protege.owl.diff.align.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.align.util.AlignmentAlgorithmComparator;
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

public class MatchByCode implements AlignmentAlgorithm {
    private Logger logger = Logger.getLogger(MatchByCode.class);
    
    private boolean disabled = false;
    private OwlDiffMap diffMap;
    private CodeToEntityMapper codeMapper;

    public int getPriority() {
        return AlignmentAlgorithmComparator.MAX_PRIORITY;
    }

    public void initialise(Engine e) {
        this.diffMap = e.getOwlDiffMap();
        codeMapper = CodeToEntityMapper.generateCodeToEntityMap(e);
        if (codeMapper.codeNotPresent()) {
        	disabled = true;
        }
    }

    public void run() {
        if (!disabled) {
            diffMap.announce(this);
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Matching source entities with target entities");
                }
                matchEntities();
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
    
    private boolean matchEntities() {
        Map<OWLEntity, OWLEntity> matchMap = new HashMap<OWLEntity, OWLEntity>();
        OWLOntology sourceOntology = diffMap.getSourceOntology();
        for (OWLEntity sourceEntity : sourceOntology.getSignature()) {
            String code = codeMapper.getCode(sourceOntology, sourceEntity);
            if (code == null) {
                continue;
            }
            Collection<OWLEntity> targetEntities = codeMapper.getTargetEntities(code);
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
