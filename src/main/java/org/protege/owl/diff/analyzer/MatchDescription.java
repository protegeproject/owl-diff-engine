package org.protege.owl.diff.analyzer;

public class MatchDescription implements Comparable<MatchDescription> {
    public final static int MAX_SEQUENCE = 10;
    public final static int DEFAULT_SEQUENCE = 5;
    public final static int MIN_SEQUENCE = 0;
    
    private String description;
    private int sequence;
    
    public MatchDescription(String description) {
        this(description, DEFAULT_SEQUENCE);
    }
    
    public MatchDescription(String description, int sequence) {
        super();
        this.description = description;
        this.sequence = sequence;
    }

    public String getDescription() {
        return description;
    }
    
    @Override
    public boolean equals(Object obj) {
    	if (!(obj instanceof MatchDescription)) {
    		return false;
    	}
    	MatchDescription other = (MatchDescription) obj;
    	return description.equals(other.description) && sequence == other.sequence;
    }
    
    @Override
    public int hashCode() {
    	return sequence + 42 * description.hashCode();
    }
    
    public int compareTo(MatchDescription o) {
        if (sequence < o.sequence) {
            return -1;
        }
        else if (sequence > o.sequence) {
            return +1;
        }
        else {
            return description.compareTo(o.description);
        }
    }
    
    @Override
    public String toString() {
    	return description;
    }

}
