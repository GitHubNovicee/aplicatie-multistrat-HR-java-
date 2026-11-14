package org.hr.domain.common;

import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

import java.time.Instant;
import java.util.logging.Logger;

/**
 * Entity listener JPA (CR-11 / RQ-12): regula de audit comuna tuturor entitatilor.
 * - @PrePersist: createdAt se seteaza o singura data, updatedAt = createdAt;
 * - @PreUpdate: updatedAt se actualizeaza la fiecare modificare efectiva;
 * - @PreRemove: stergerea fizica a unei entitati este inregistrata in jurnal.
 */
public class AuditEntityListener {
    private static final Logger logger = Logger.getLogger(AuditEntityListener.class.getName());

    @PrePersist
    public void onPrePersist(BaseEntity entity) {
        Instant now = Instant.now();
        if (entity.getCreatedAt() == null)
            entity.setCreatedAt(now);
        entity.setUpdatedAt(entity.getCreatedAt());
    }

    @PreUpdate
    public void onPreUpdate(BaseEntity entity) {
        entity.setUpdatedAt(Instant.now());
    }

    @PreRemove
    public void onPreRemove(BaseEntity entity) {
        logger.info(">> PreRemove :: " + entity);
    }
}