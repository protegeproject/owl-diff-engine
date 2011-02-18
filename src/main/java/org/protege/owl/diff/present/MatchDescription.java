package org.protege.owl.diff.present;

public class MatchDescription implements Comparable<MatchDescription> {
	public final static int NON_CRITICAL_PRIORITY    = 10;
    public final static int DEFAULT_MATCH_PRIORITY   =  5;
    public final static int PRIMARY_MATCH_PRIORITY   =  0;
    public final static int SECONDARY_MATCH_PRIORITY =  PRIMARY_MATCH_PRIORITY + 2;
    
    private String description;
    private int sequence;
    
    public MatchDescription(String description) {
        this(description, DEFAULT_MATCH_PRIORITY);
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
