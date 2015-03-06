package org.protege.owl.diff.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.OwlDiffMap;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;

public class CodeToEntityMapper {
	public static final Logger LOGGER = Logger.getLogger(CodeToEntityMapper.class.getName());
	
	public static final String CODE_ANNOTATION_PROPERTY = "code.annotation.property";
	
	private OwlDiffMap                         diffMap;
	private OWLAnnotationProperty              codeProperty;
	private Map<String, Collection<OWLEntity>> targetCodeToEntityMap;
	
	public static CodeToEntityMapper get(Engine e) {
		CodeToEntityMapper mapper = e.getService(CodeToEntityMapper.class);
		if (mapper == null) {
			mapper = new CodeToEntityMapper(e.getOwlDiffMap(), e.getParameters());
			e.addService(mapper);
		}
		return mapper;
	}

	
	private CodeToEntityMapper(OwlDiffMap diffMap, Map<String, String> parameters) {
		this.diffMap = diffMap;
		String codeName = parameters.get(CODE_ANNOTATION_PROPERTY);
		if (codeName == null) {
			return;
		}
        IRI codeIri = IRI.create((String) codeName);
        codeProperty = diffMap.getOWLDataFactory().getOWLAnnotationProperty(codeIri);
        if (!diffMap.getSourceOntology().containsAnnotationPropertyInSignature(codeIri)) {
        	LOGGER.warning("Source ontology does not have selected code annotation " + codeName);
        }
        else if (!diffMap.getTargetOntology().containsAnnotationPropertyInSignature(codeIri)) {
        	LOGGER.warning("Target ontology does not have selected code annotation " + codeName);
        }

	}
	
	public boolean codeNotPresent() {
		return codeProperty == null 
		    || !diffMap.getSourceOntology().containsAnnotationPropertyInSignature(codeProperty.getIRI()) 
			|| !diffMap.getTargetOntology().containsAnnotationPropertyInSignature(codeProperty.getIRI());
	}
	
	public OWLAnnotationProperty getCodeProperty() {
		return codeProperty;
	}
	
    public String getCode(OWLOntology ontology, OWLEntity entity) {
        for (OWLAnnotation annotation : entity.getAnnotations(ontology)) {
            if (!annotation.getProperty().equals(codeProperty)) {
                continue;
            }
            OWLAnnotationValue value = annotation.getValue();
            if (value instanceof OWLLiteral) {
                return ((OWLLiteral) value).getLiteral();
            }
        }
        return null;
    }
    
    public Collection<OWLEntity> getTargetEntities(String code) {
    	Collection<OWLEntity> targetEntities = getTargetCodeToEntityMap().get(code);
    	if (targetEntities == null) {
    		return Collections.emptySet();
    	}
    	else {
    		return Collections.unmodifiableCollection(targetEntities);
    	}
    }
    
    private Map<String, Collection<OWLEntity>> getTargetCodeToEntityMap() {
    	if (targetCodeToEntityMap == null) {
    		targetCodeToEntityMap = generateCodeToEntityMap(diffMap.getTargetOntology());
    	}
    	return Collections.unmodifiableMap(targetCodeToEntityMap);
    }


    private Map<String, Collection<OWLEntity>> generateCodeToEntityMap(OWLOntology ontology) {
    	Map<String, Collection<OWLEntity>> codeToEntityMap = new HashMap<String, Collection<OWLEntity>>();
        for (OWLEntity entity : ontology.getSignature()) {
            String code = getCode(ontology, entity);
            if (code != null) {
                Collection<OWLEntity> entities = codeToEntityMap.get(code);
                if (entities == null) {
                    entities = new ArrayList<OWLEntity>();
                    codeToEntityMap.put(code, entities);
                }
                entities.add(entity);
            }
        }
        return Collections.unmodifiableMap(codeToEntityMap);
    }
}

