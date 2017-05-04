package org.protege.owl.diff;

import junit.framework.TestCase;
import org.protege.owl.diff.align.Prioritized;
import org.protege.owl.diff.align.algorithms.MatchById;
import org.protege.owl.diff.align.util.PrioritizedComparator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.testng.internal.BshMock;
import org.testng.reporters.JUnitReportReporter;

import java.io.File;

public class PrioritizedComparatorTest extends TestCase implements Prioritized {

    public PrioritizedComparatorTest(){}

    PrioritizedComparatorTest tester1;
    PrioritizedComparatorTest tester2;
    private int priority = 0;

    public int getPriority(){
        return priority;
    }

    public void setPriority(int set){
        priority = set;
    }

    private void makeTesters(){
        tester1 =  new PrioritizedComparatorTest();
        tester2 =  new PrioritizedComparatorTest();
    }

    public void testEqual (){
        makeTesters();
        JunitUtilities.printDivider();
        PrioritizedComparator comparator = new PrioritizedComparator();
        tester1.setPriority(5);
        tester2.setPriority(5);
        assertTrue(tester1.getPriority() == tester2.getPriority());
        int result = comparator.compare(tester1 , tester2);
        assertTrue(result == 0 );
    }

    public void testFirstGreater(){
        makeTesters();
        JunitUtilities.printDivider();
        PrioritizedComparator comparator = new PrioritizedComparator();
        tester1.setPriority(5);
        tester2.setPriority(1);
        int result = comparator.compare(tester1 , tester2);
        assertTrue(result == -1 );
    }

    public void testSecondGreater(){
        makeTesters();
        JunitUtilities.printDivider();
        PrioritizedComparator comparator = new PrioritizedComparator();
        tester1.setPriority(1);
        tester2.setPriority(5);
        int result = comparator.compare(tester1 , tester2);
        assertTrue(result == 1 );
    }


}
