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
		List<Class<? extends AlignmentAlgorithm>> algorithms = Util.createDeclaredAlignmentAlgorithms(getClass().getClassLoader());
		assertTrue(algorithms.contains(MatchById.class));
		assertTrue(algorithms.contains(MatchByCode.class));
	}
	
	public void testFindPresentationAlgorithms() throws IOException{
		List<Class<? extends PresentationAlgorithm>> algorithms = Util.createDeclaredPresentationAlgorithms(getClass().getClassLoader());
		assertTrue(algorithms.contains(IdentifyChangedAnnotation.class));
		assertTrue(algorithms.contains(IdentifyChangedSuperclass.class));
		assertTrue(algorithms.contains(IdentifyRenameOperation.class));
	}
}
