package org.protege.owl.diff.align.util;

public class UnmappedEntityException extends RuntimeException {
    private static final long serialVersionUID = -4014029443231135385L;

    public UnmappedEntityException() {
    }
    
    public UnmappedEntityException(String message) {
        super(message);
    }
    
    public UnmappedEntityException(Throwable t) {
        super(t);
    }

    public UnmappedEntityException(String  message, Throwable t) {
        super(message, t);
    }
}
