package org.protege.owl.diff.service;

import java.util.Properties;

import org.protege.owl.diff.align.OwlDiffMap;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class RetirementClassService {

    public static final String RETIREMENT_CLASS_PROPERTY  = "retirement.class.prefix";
    public static final String RETIREMENT_STATUS_PROPERTY = "retirement.status.property";
    public static final String RETIREMENT_STATUS_STRING   = "retirement.status.string";
    
    private String retirementClassPrefix;
    private OWLAnnotationProperty retirementStatusProperty;
    private String retirementStatusString;
    
    public static RetirementClassService getRetirementClassService(OwlDiffMap diffMap, Properties parameters) {
    	RetirementClassService rcs = diffMap.getService(RetirementClassService.class);
    	if (rcs == null) {
    		rcs = new RetirementClassService(diffMap, parameters);
    		diffMap.addService(rcs);
    	}
    	return rcs;
    }
    
    private RetirementClassService(OwlDiffMap diffMap, Properties parameters) {
    	retirementClassPrefix = (String) parameters.get(RETIREMENT_CLASS_PROPERTY);
    	String retirementStatusPropertyName = (String) parameters.get(RETIREMENT_STATUS_PROPERTY);
    	retirementStatusString = (String) parameters.getProperty(RETIREMENT_STATUS_STRING);
    	if (retirementStatusPropertyName != null && retirementStatusString != null) {
    		retirementStatusProperty = diffMap.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(retirementStatusPropertyName));
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
    		ret = annotationAxiom.getAnnotation().getProperty().equals(retirementStatusProperty) 
    				&& annotationAxiom.getAnnotation().getValue() instanceof OWLLiteral
    				&& ((OWLLiteral) annotationAxiom.getAnnotation().getValue()).getLiteral().equals(retirementStatusString);
    	}
    	return ret;
    }
}
