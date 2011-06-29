package org.protege.owl.diff.service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.protege.owl.diff.Engine;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.ShortFormProvider;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;

public class RenderingService {
	private OWLDataFactory factory;
	
	private WriterDelegate sourceWriter = new WriterDelegate();
	private ManchesterOWLSyntaxObjectRenderer sourceRenderer;
	
	private WriterDelegate targetWriter = new WriterDelegate();
	private ManchesterOWLSyntaxObjectRenderer targetRenderer;
	
	public static RenderingService get(Engine e) {
		RenderingService renderer = e.getService(RenderingService.class);
		if (renderer == null) {
			renderer = new RenderingService(e.getOWLDataFactory());
			e.addService(renderer);
		}
		return renderer;
	}
	
	private RenderingService(OWLDataFactory factory) {
		this.factory = factory;
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
	
	
	public String renderSourceObject(OWLObject o) {
		return render(o, false);
	}
	
	public String renderTargetObject(OWLObject o) {
		return render(o, true);
	}
	
	public String renderDiff(EntityBasedDiff diff) {
		StringBuffer diffDescription = new StringBuffer();
		switch (diff.getDiffType()) {
		case CREATED:
			diffDescription.append("Created ");
			diffDescription.append(renderTargetObject(diff.getTargetEntity()));
			break;
		case DELETED:
			diffDescription.append("Deleted ");
			diffDescription.append(renderSourceObject(diff.getSourceEntity()));
			break;
		case EQUIVALENT:
			break;
		case MODIFIED:
		case RENAMED:
			diffDescription.append("Modified ");
			diffDescription.append(renderSourceObject(diff.getSourceEntity()));
			break;
		}
		return diffDescription.toString();
	}

	private String render(OWLObject o, boolean isTargetOntology) {
		WriterDelegate writer = isTargetOntology ? targetWriter : sourceWriter;
		writer.reset();
		ManchesterOWLSyntaxObjectRenderer renderer = isTargetOntology ? targetRenderer : sourceRenderer;
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
	
	private IRIShortFormProvider getIRIShortFormProvider(final ShortFormProvider shortFormProvider) {
		return new IRIShortFormProvider() {
			
			@Override
			public String getShortForm(IRI uri) {
				return shortFormProvider.getShortForm(factory.getOWLClass(uri));
			}
		};
	}
	
    private static class WriterDelegate extends Writer {

        private StringWriter delegate;

        public WriterDelegate() {
		}


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
