package org.hr.domain.common;

/** Operatie nepermisa pentru rolul/identitatea utilizatorului (matricea de permisiuni din Sprint 1). */
public class AccessDeniedBusinessException extends RuntimeException {
    public AccessDeniedBusinessException(String message) {
        super(message);
    }
}
