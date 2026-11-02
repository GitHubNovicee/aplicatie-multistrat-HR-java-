package org.hr.domain.common;

/** Entitatea ceruta nu exista in repository. */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entityName, Object id) {
        super(entityName + " with id " + id + " not found!");
    }
}
