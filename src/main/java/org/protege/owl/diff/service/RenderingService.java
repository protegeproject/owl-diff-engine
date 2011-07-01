package org.protege.owl.diff.service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.protege.owl.diff.DifferencePosition;
import org.protege.owl.diff.Engine;
import org.protege.owl.diff.align.OwlDiffMap;
import org.protege.owl.diff.present.EntityBasedDiff;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.ShortFormProvider;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;

public class RenderingService {
	private OWLDataFactory factory;
	private Engine e;
	private DeprecationDeferralService dds;
	
	private WriterDelegate sourceWriter = new WriterDelegate();
	private ManchesterOWLSyntaxObjectRenderer sourceRenderer;
	
	private WriterDelegate targetWriter = new WriterDelegate();
	private ManchesterOWLSyntaxObjectRenderer targetRenderer;
	
	public static RenderingService get(Engine e) {
		RenderingService renderer = e.getService(RenderingService.class);
		if (renderer == null) {
			renderer = new RenderingService(e);
			e.addService(renderer);
		}
		return renderer;
	}
	
	private RenderingService(Engine e) {
		this.e = e;
		this.factory = e.getOWLDataFactory();
		this.dds = DeprecationDeferralService.get(e);
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
		return render(o, DifferencePosition.SOURCE);
	}
	
	public String renderTargetObject(OWLObject o) {
		return render(o, DifferencePosition.TARGET);
	}
	
	public String renderDiff(EntityBasedDiff diff) {
		StringBuffer diffDescription = new StringBuffer();
		diffDescription.append(diff.getDiffTypeDescription());
		diffDescription.append(' ');
		switch (diff.getDiffType()) {
		case CREATED:
			diffDescription.append(renderTargetObject(diff.getTargetEntity()));
			break;
		case DELETED:
			diffDescription.append(renderSourceObject(diff.getSourceEntity()));
			break;
		case EQUIVALENT:
			break;
		case MODIFIED:
		case RENAMED:
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
