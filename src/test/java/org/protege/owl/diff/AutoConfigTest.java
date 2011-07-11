package org.protege.owl.diff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.protege.owl.diff.align.AlignmentAlgorithm;
import org.protege.owl.diff.align.algorithms.MatchByCode;
import org.protege.owl.diff.align.algorithms.MatchById;
import org.protege.owl.diff.present.PresentationAlgorithm;
import org.protege.owl.diff.present.algorithms.IdentifyChangedAnnotation;
import org.protege.owl.diff.present.algorithms.IdentifyChangedSuperclass;
import org.protege.owl.diff.present.algorithms.IdentifyRenameOperation;
import org.protege.owl.diff.util.Util;

public class AutoConfigTest extends TestCase {

	public void testFindAlignmentAlgorithms() throws IOException {
		List<AlignmentAlgorithm> algorithms = Util.createDeclaredAlignmentAlgorithms(getClass().getClassLoader());
		List<Class<? extends AlignmentAlgorithm>> classes = new ArrayList<Class<? extends AlignmentAlgorithm>>();
		for (AlignmentAlgorithm algorithm : algorithms) {
			classes.add(algorithm.getClass());
		}
		assertTrue(classes.contains(MatchById.class));
		assertTrue(classes.contains(MatchByCode.class));
	}
	
	public void testFindPresentationAlgorithms() throws IOException{
		List<PresentationAlgorithm> algorithms = Util.createDeclaredPresentationAlgorithms(getClass().getClassLoader());
		List<Class<? extends PresentationAlgorithm>> classes = new ArrayList<Class<? extends PresentationAlgorithm>>();
		for (PresentationAlgorithm algorithm : algorithms) {
			classes.add(algorithm.getClass());
		}
		assertTrue(classes.contains(IdentifyChangedAnnotation.class));
		assertTrue(classes.contains(IdentifyChangedSuperclass.class));
		assertTrue(classes.contains(IdentifyRenameOperation.class));
	}
}
