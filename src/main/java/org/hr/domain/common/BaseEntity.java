package org.hr.domain.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

/**
 * RQ-12: toate entitatile au audit (createdAt/updatedAt) si control al concurentei (version).
 * Valorile sunt gestionate de repository (CR-11): createdAt se seteaza o singura data,
 * updatedAt la fiecare modificare, version creste la fiecare salvare.
 */
@Getter @Setter
public abstract class BaseEntity implements Serializable {
    private Long id;
    private Long version = 0L;
    private Instant createdAt;
    private Instant updatedAt;

    /** Urmatorul id local pentru entitati componenta ale unui agregat. */
    protected static Long nextComponentId(Collection<? extends BaseEntity> components) {
        return components.stream()
                .map(BaseEntity::getId)
                .filter(Objects::nonNull)
                .max(Long::compare)
                .orElse(0L) + 1;
    }

    /** Ataseaza o entitate componenta la agregat (id local + audit initial). */
    protected static <C extends BaseEntity> C addComponent(Collection<C> components, C component) {
        Instant now = Instant.now();
        component.setId(nextComponentId(components));
        component.setVersion(0L);
        component.setCreatedAt(now);
        component.setUpdatedAt(now);
        components.add(component);
        return component;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity other = (BaseEntity) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : Objects.hash(getClass().getName(), id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + id;
    }
}
