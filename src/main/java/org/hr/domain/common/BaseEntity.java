package org.hr.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

/**
 * RQ-12: toate entitatile au audit (createdAt/updatedAt) si control al concurentei (version).
 * Sprint 3: superclasa persistenta (@MappedSuperclass) a tuturor entitatilor JPA.
 * - id: generat de baza de date (IDENTITY);
 * - version: versionare optimista gestionata de JPA (@Version) - o modificare pe o versiune invechita este respinsa;
 * - createdAt/updatedAt: completate de {@link AuditEntityListener} (CR-11) prin @PrePersist/@PreUpdate.
 */
@MappedSuperclass
@EntityListeners(AuditEntityListener.class)
@Getter @Setter
public abstract class BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    /** Ataseaza o entitate componenta la agregat; id-ul, versiunea si auditul se completeaza la persistare (cascade). */
    protected static <C extends BaseEntity> C addComponent(Collection<C> components, C component) {
        components.add(component);
        return component;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Long otherId = ((BaseEntity) o).getId();
        return getId() != null && getId().equals(otherId);
    }

    /** Stabil pe tot ciclul de viata (id-ul se atribuie la persistare, deci nu poate intra in hash). */
    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }

    @Override
    public String toString() {
        return Hibernate.getClass(this).getSimpleName() + "#" + getId();
    }
}