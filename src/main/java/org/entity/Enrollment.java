package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "employee_id"}))
public class Enrollment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private TrainingSession session;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Employee employee;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private EnrollmentStatus status = EnrollmentStatus.ENROLLED;
    private BigDecimal resultScore;
    private boolean certified;
    private String certificateNumber;
    private LocalDate certifiedOn;
}
