package org.protege.owl.diff.align;

public enum AlignmentAggressiveness {
	IGNORE_REFACTOR("Ignore refactors"),
	PRETTY_CERTAIN("Only find refactors when the names are similar and there is supporting evidence"),
	MODERATE("Make a reasonable attempt to find refactor operations"),
	AGGRESSIVE_SEARCH("Apply all alignment search algorithms");
	
	private String description;
	
	private AlignmentAggressiveness(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
