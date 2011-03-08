package org.protege.owl.diff.align.util;

/*
 * Stolen shamelessly from Protege 3 Prompt.  I have no idea what it is doing but it seems to work.
 */
public class CompareNames {

	static final private char[] DELIMITERS = { '_', '-', ' ', '(', ')', '.' };

	private static double THRESHOLD = 0.95;

	private static int TOO_SMALL = 3;

	private static int TYPO_LENGTH = 2;

	public static boolean closeEnough(String str1, String str2) {
		//completely random threshold to see if two strings are similar
		if (str1.equals(str2))
			return true;
	
		String s1 = convertString(str1);
		String s2 = convertString(str2);
	
		boolean result;
	
		result = compare(s1, s2);
	
		if (!result)
			result = compare(s2, s1);
		return result;
	}

	static private String convertString(String str) {
		String result = str.toLowerCase();
		for (int i = 0; i < DELIMITERS.length; i++)
			result = removeCharacter(result, DELIMITERS[i]);
		return result;
	}

	private static String removeCharacter(String s, char c) {
		String result = new String();
		int currentIndex = 0;
		int searchIndex;

		do {
			searchIndex = s.indexOf(c, currentIndex);
			if (searchIndex != -1) {
				result = result.concat(s.substring(currentIndex, searchIndex));
				currentIndex = searchIndex + 1;
			} else
				result = result.concat(s.substring(currentIndex));
		} while (searchIndex != -1);

		return result;
	}

	private static boolean compare(String s1, String s2) {
		int endIndex = (int) Math.round(s1.length() * THRESHOLD);

		if(endIndex < 0 && endIndex < s1.length()) {
			return false;
		}
		
		String part1 = s1.substring(0, endIndex);
		if (part1.length() <= TOO_SMALL)
			return false;
		else if ((s2.indexOf(part1) != -1 && ((float) s1.length()) / ((float) s2.length()) > THRESHOLD))
			return true;
		else if (Math.abs(s1.length() - s2.length()) > TOO_SMALL)
			return false;
		else {
			int f;
			for (f = 0; f < s1.length() && f < s2.length(); f++) {
				if (s1.charAt(f) != s2.charAt(f))
					break;
			}
			if (f == s1.length() || f == s2.length())
				return false; // **should never get here actually;
			int b;
			for (b = 1; b < s1.length() && b < s2.length(); b++) {
				if (s1.charAt(s1.length() - b) != s2.charAt(s2.length() - b))
					break;
			}
			if (Math.abs((s1.length() - b) - f) < TYPO_LENGTH)
				return true;
		}
		return false;
	}
}

