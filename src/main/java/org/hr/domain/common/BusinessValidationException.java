package org.hr.domain.common;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/** Incalcarea uneia sau mai multor reguli de validare (VR-xx / JavaBean Validation). */
public class BusinessValidationException extends RuntimeException {
    private final Set<String> violations;

    public BusinessValidationException(String message) {
        this(Set.of(message));
    }

    public BusinessValidationException(Set<String> violations) {
        super("Validation failed: " + String.join("; ", new TreeSet<>(violations)));
        this.violations = Collections.unmodifiableSet(new TreeSet<>(violations));
    }

    public Set<String> getViolations() {
        return violations;
    }
}
