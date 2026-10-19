package org.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist void onCreate() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate  void onUpdate() { updatedAt = Instant.now(); }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || org.hibernate.Hibernate.getClass(this) != org.hibernate.Hibernate.getClass(o)) return false;
        BaseEntity other = (BaseEntity) o;
        return id != null && id.equals(other.id);
    }
    @Override public int hashCode() { return getClass().hashCode(); }
}
