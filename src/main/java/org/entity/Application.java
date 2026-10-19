package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "application", uniqueConstraints = @UniqueConstraint(columnNames = {"candidate_id", "job_opening_id"}))
public class Application extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Candidate candidate;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private JobOpening jobOpening;
    @Column(nullable = false) private LocalDate appliedOn;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ApplicationStatus status = ApplicationStatus.RECEIVED;
}
