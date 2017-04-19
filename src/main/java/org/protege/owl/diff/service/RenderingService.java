package org.protege.owl.diff.service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.protege.owl.diff.DifferencePosition;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;
import org.semanticweb.owlapi.util.AnnotationValueShortFormProvider;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.util.ShortFormProvider;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;

public class RenderingService {
    public static final String NO_LANGUAGE_SET = "";
    
	private OWLDataFactory factory;
	
	private Engine engine;


	//ManchesterOWLSyntaxObjectRenderer is deprecated, but i didn't find any other class to replace with it
    //I also didn't found the source of it or a good documentation to see what it does and write an other class
    //so i think it works now and we should not make big changes on it.
	
	private WriterDelegate sourceWriter = new WriterDelegate();
	private ManchesterOWLSyntaxObjectRenderer sourceRenderer;
	
	private WriterDelegate targetWriter = new WriterDelegate();
	private ManchesterOWLSyntaxObjectRenderer targetRenderer;
	
	private Map<String, OWLEntity> targetNameToEntityMap;


	//returns a new RenderingService
	public static RenderingService get(Engine e) {
		RenderingService renderer = e.getService(RenderingService.class);
		if (renderer == null) {
			renderer = new RenderingService(e);
			e.addService(renderer);
		}
		return renderer;
	}
	
	/*
	 * Pardon me - I am stealing this code from Protege 4.  Dependencies make it unclear how to share it.
	 */
	public static ShortFormProvider getDefaultShortFormProvider(OWLOntology ontology) {
		List<OWLAnnotationProperty> annotationProperties = Collections.singletonList(OWLManager.getOWLDataFactory().getRDFSLabel());
		List<String> langs = getDefaultLanguages();
		return getShortFormProvider(ontology, annotationProperties, langs);
	}
	
	public static ShortFormProvider getShortFormProvider(OWLOntology ontology, List<OWLAnnotationProperty> annotationProperties, List<String> langs) {
		Map<OWLAnnotationProperty, List<String>> preferredLanguageMap = new HashMap<>();
		for (OWLAnnotationProperty annotationProperty : annotationProperties) {
			preferredLanguageMap.put(annotationProperty, langs);
		}
		OWLOntologySetProvider ontologies = new OWLOntologyImportsClosureSetProvider(ontology.getOWLOntologyManager(), ontology);
		return new AnnotationValueShortFormProvider(annotationProperties, preferredLanguageMap, ontologies);
	}

	/*
	 * Pardon me - I am stealing this code from Protege 4.  Dependencies make it unclear how to share it.
	 */
	public static List<String> getDefaultLanguages() {

		List<String> languages = new ArrayList<String>();
		Locale locale = Locale.getDefault();
		//this if validates the language os the local of the JVM instance
		if (locale != null && locale.getLanguage() != null && !locale.getLanguage().equals("")) {
			languages.add(locale.getLanguage());
			//this if validates the country of the local of the JVM instance
			if (locale.getCountry() != null && !locale.getCountry().equals("")) {
				languages.add(locale.getLanguage() + "-" + locale.getCountry());

			}
		}
		//this add an empty string
		languages.add(NO_LANGUAGE_SET);

		//languages must conatain english
		String en = Locale.ENGLISH.getLanguage();
		if (!languages.contains(en)) {
			languages.add(en);
		}
		return languages;
	}

	private RenderingService(Engine e) {
		engine = e;
		this.factory = e.getOWLDataFactory();
	}
	
	public void setSourceShortFormProvider(ShortFormProvider sourceShortFormProvider) {
		setSourceShortFormProvider(sourceShortFormProvider, getIRIShortFormProvider(sourceShortFormProvider));
	}

	public void setSourceShortFormProvider(ShortFormProvider sourceShortFormProvider, IRIShortFormProvider sourceIRIShortFormProvider) {
		sourceRenderer = getRenderer(sourceWriter, sourceShortFormProvider, sourceIRIShortFormProvider);
	}

	public void setTargetShortFormProvider(ShortFormProvider targetShortFormProvider) {
		setTargetShortFormProvider(targetShortFormProvider, getIRIShortFormProvider(targetShortFormProvider));
	}
	
	public void setTargetShortFormProvider(ShortFormProvider targetShortFormProvider, IRIShortFormProvider targetIRIShortFormProvider) {
		targetRenderer = getRenderer(targetWriter, targetShortFormProvider, targetIRIShortFormProvider);
	}
	
	public boolean isReady() {
		return sourceRenderer != null && targetRenderer != null;
	}
	
	
	public String renderSourceObject(OWLObject o) {
		if (engine.getOwlDiffMap() == null) {
			return "";
		}
		if (sourceRenderer == null) {
			setSourceShortFormProvider(getDefaultShortFormProvider(engine.getOwlDiffMap().getSourceOntology()));
		}
		return render(o, DifferencePosition.SOURCE);
	}


	public String renderTargetObject(OWLObject o) {
		if (engine.getOwlDiffMap() == null) {
			return "";
		}
		if (targetRenderer == null) {
			setTargetShortFormProvider(getDefaultShortFormProvider(engine.getOwlDiffMap().getTargetOntology()));			
		}
		return render(o, DifferencePosition.TARGET);
	}
	
	public String renderDiff(EntityBasedDiff diff) {
		StringBuilder diffDescription = new StringBuilder();
		diffDescription.append(diff.getDiffTypeDescription());
		diffDescription.append(": ");
		switch (diff.getDiffType()) {
		case CREATED:
			diffDescription.append(renderTargetObject(diff.getTargetEntity()));
			break;
		case EQUIVALENT:
			break;
		default:
			diffDescription.append(renderSourceObject(diff.getSourceEntity()));
			break;
		}
		return diffDescription.toString();
	}

	private String render(OWLObject o, DifferencePosition position) {
		WriterDelegate writer = (position == DifferencePosition.TARGET) ? targetWriter : sourceWriter;
		writer.reset();
		ManchesterOWLSyntaxObjectRenderer renderer = (position == DifferencePosition.TARGET) ? targetRenderer : sourceRenderer;
		o.accept(renderer);
		return writer.toString();
	}

	private ManchesterOWLSyntaxObjectRenderer getRenderer(WriterDelegate writer, 
													      final ShortFormProvider shortFormProvider, 
													      final IRIShortFormProvider iriShortFormProvider) {
		return new ManchesterOWLSyntaxObjectRenderer(writer, shortFormProvider) {
			@Override
			public void visit(IRI iri) {
				write(iriShortFormProvider.getShortForm(iri));
			}
		};
	}

	// creates a new IRIShortFormProvider to the given ShortFormProvider
	private IRIShortFormProvider getIRIShortFormProvider(final ShortFormProvider shortFormProvider) {
		return new IRIShortFormProvider() {
			
			@Override
			public String getShortForm(IRI uri) {
				return shortFormProvider.getShortForm(factory.getOWLClass(uri));
			}
		};
	}


	public OWLEntity getTargetEntityByRendering(String rendering) {
		if (targetNameToEntityMap == null) {
			targetNameToEntityMap = new HashMap<>();
			Set<String> toRemove = new TreeSet<>();
			for (OWLEntity e : engine.getOwlDiffMap().getTargetOntology().getSignature()) {
				String eRendering = renderTargetObject(e);
				if (eRendering == null) {
					continue;
				}
				if (targetNameToEntityMap.get(eRendering) != null) {
					toRemove.add(eRendering);
				}
				else {
					targetNameToEntityMap.put(eRendering, e);
				}
			}
			for (String ambiguousRendering : toRemove) {
				targetNameToEntityMap.remove(ambiguousRendering);
			}
		}
		return targetNameToEntityMap.get(rendering);
	}

	//this is a class which contains a StringWriter, and cals it's methods
    //Is it really neccessary to have this class insted of using a simple StringWriter?
	private static class WriterDelegate extends Writer {

        private StringWriter delegate;
        
		private void reset() {
            delegate = new StringWriter();
        }


        @Override
		public String toString() {
            return delegate.getBuffer().toString();
        }


        @Override
		public void close() throws IOException {
            delegate.close();
        }


        @Override
		public void flush() throws IOException {
            delegate.flush();
        }


        @Override
		public void write(char cbuf[], int off, int len) throws IOException {
            delegate.write(cbuf, off, len);
        }
    }

}
