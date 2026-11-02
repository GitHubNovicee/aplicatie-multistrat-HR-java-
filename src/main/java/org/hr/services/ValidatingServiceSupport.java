package org.hr.services;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.hr.domain.common.BusinessValidationException;

import java.util.Set;
import java.util.TreeSet;

/**
 * Suport comun pentru Domain Validation Services: ruleaza regulile JavaBean Validation
 * declarate prin adnotari pe entitati si permite adaugarea regulilor de business (VR-xx).
 */
public abstract class ValidatingServiceSupport {
    private final Validator validator;

    protected ValidatingServiceSupport(Validator validator) {
        this.validator = validator;
    }

    /** Violari JavaBean Validation (adnotari pe entitati, inclusiv in cascada: @Valid). */
    protected <E> Set<String> beanViolations(E entity) {
        Set<String> messages = new TreeSet<>();
        for (ConstraintViolation<E> violation : validator.validate(entity))
            messages.add(violation.getPropertyPath() + ": " + violation.getMessage());
        return messages;
    }

    protected void throwIfAny(Set<String> violations) {
        if (!violations.isEmpty())
            throw new BusinessValidationException(violations);
    }
}
