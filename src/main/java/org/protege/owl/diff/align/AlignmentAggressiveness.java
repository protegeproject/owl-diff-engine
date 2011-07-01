package org.protege.owl.diff.align;

public enum AlignmentAggressiveness {
	IGNORE_REFACTOR("Ignore refactors"),
	CONSERVATIVE("Be conservative about identifying refactor operations"),
	MODERATE("Make a reasonable attempt to find refactor operations"),
	AGGRESSIVE_SEARCH("Apply all algorithms");
	
	private String description;
	
	private AlignmentAggressiveness(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
