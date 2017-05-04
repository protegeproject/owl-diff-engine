package org.protege.owl.diff;

import org.protege.owl.diff.align.util.CompareNames;

import junit.framework.TestCase;

public class UtilTests extends TestCase{

	
	public void testCompareNames(){
		String str1 = "Compare";
		String str2 = "Compare";
		assertTrue(CompareNames.closeEnough(str1, str2));
		str1 = "Comparx";
		str2 = "Compare";
		assertTrue(CompareNames.closeEnough(str1, str2));
		str1 = "Compaxx";
		str2 = "Compare";
		assertTrue(CompareNames.closeEnough(str1, str2));
		str1 = "Compxxx";
		str2 = "Compare";
		assertFalse(CompareNames.closeEnough(str1, str2));
		
	}
}
