package org.protege.owl.diff.service;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.OwlDiffMap;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public final class RetirementClassService {

    public static final String RETIREMENT_CLASS_PROPERTY  = "retirement.class.prefix";
    public static final String RETIREMENT_STATUS_PROPERTY = "retirement.status.property";
    public static final String RETIREMENT_STATUS_STRING   = "retirement.status.string";
    public static final String RETIREMENT_META_PROPERTIES = "retirement.meta.property.";
    
    private String retirementClassPrefix;
    private OWLAnnotationProperty retirementStatusProperty;
    private Set<OWLAnnotationProperty> retirementMetaProperties;
    private String retirementStatusString;
    
    private OWLDataFactory factory;
    
    public static RetirementClassService getRetirementClassService(Engine e) {
    	RetirementClassService rcs = e.getService(RetirementClassService.class);
    	if (rcs == null) {
    		rcs = new RetirementClassService(e.getOwlDiffMap(), e.getParameters());
    		e.addService(rcs);
    	}
    	return rcs;
    }
    
    private RetirementClassService(OwlDiffMap diffMap, Properties parameters) {
    	factory = diffMap.getOWLDataFactory();
    	retirementClassPrefix = (String) parameters.get(RETIREMENT_CLASS_PROPERTY);
    	String retirementStatusPropertyName = (String) parameters.get(RETIREMENT_STATUS_PROPERTY);
    	retirementStatusString = (String) parameters.getProperty(RETIREMENT_STATUS_STRING);
    	if (retirementStatusPropertyName != null && retirementStatusString != null) {
    		retirementStatusProperty = diffMap.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(retirementStatusPropertyName));
    	}
    	retirementMetaProperties = new HashSet<OWLAnnotationProperty>();
    	int i = 0;
    	while (true) {
    		String retirementMetaProperty = (String) parameters.getProperty(RETIREMENT_META_PROPERTIES + (i++));
    		if (retirementMetaProperty == null) {
    			break;
    		}
    		retirementMetaProperties.add(factory.getOWLAnnotationProperty(IRI.create(retirementMetaProperty)));
    	}
    }
    
    public boolean isDisabled() {
    	return retirementClassPrefix == null;
    }
    
    public boolean isRetirementAxiom(OWLAxiom axiom) {
    	boolean ret = false;
    	if (axiom instanceof OWLSubClassOfAxiom) {
    		OWLSubClassOfAxiom subClassAxiom = (OWLSubClassOfAxiom) axiom;
    		ret = !subClassAxiom.getSubClass().isAnonymous() && 
            		!subClassAxiom.getSuperClass().isAnonymous() &&
            	    subClassAxiom.getSuperClass().asOWLClass().getIRI().toString().startsWith(retirementClassPrefix);
    	}
    	else if (axiom instanceof OWLAnnotationAssertionAxiom && retirementStatusProperty != null) {
    		OWLAnnotationAssertionAxiom annotationAxiom = (OWLAnnotationAssertionAxiom) axiom;
    		OWLAnnotationProperty property = annotationAxiom.getAnnotation().getProperty();
    		if (property.equals(retirementStatusProperty)) {
    			ret = annotationAxiom.getAnnotation().getValue() instanceof OWLLiteral
    		             && ((OWLLiteral) annotationAxiom.getAnnotation().getValue()).getLiteral().equals(retirementStatusString);
    		}
    		else if (property.equals(factory.getOWLDeprecated())) {
    			ret = annotationAxiom.getAnnotation().getValue() instanceof OWLLiteral
	             		&& ((OWLLiteral) annotationAxiom.getAnnotation().getValue()).getLiteral().toLowerCase().equals("true");
    		}
    		else if (retirementMetaProperties.contains(property)) {
    			ret = true;
    		}
    	}
    	return ret;
    }
}
