package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
public class TrainingSession extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Course course;
    @Column(nullable = false) private LocalDateTime startsAt;
    @Column(nullable = false) private LocalDateTime endsAt;
    private String location;
    private String trainer;
    @Column(nullable = false) private int capacity;
    @ManyToOne(fetch = FetchType.LAZY) private Employee coordinator;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private SessionStatus status = SessionStatus.PLANNED;
}
