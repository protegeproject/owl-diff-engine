package org.protege.owl.diff.util;

import java.util.Comparator;

import org.protege.owl.diff.DifferencePosition;
import org.protege.owl.diff.service.RenderingService;
import org.semanticweb.owlapi.model.OWLEntity;

public class EntityComparator implements Comparator<OWLEntity> {
	private RenderingService renderer;
	private DifferencePosition position;
	
	public EntityComparator(RenderingService renderer, DifferencePosition position) {
		this.renderer = renderer;
		this.position = position;
	}
	
	@Override
	public int compare(OWLEntity o1, OWLEntity o2) {
		String rendering1 = render(o1);
		String rendering2 = render(o2);
		return rendering1.compareTo(rendering2);
	}
	
	private String render(OWLEntity e) {
		return position == DifferencePosition.SOURCE ? 
				renderer.renderSourceObject(e) : renderer.renderTargetObject(e);
	}

}
