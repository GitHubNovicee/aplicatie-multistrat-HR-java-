package org.hr.domain.training;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.organization.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Entitate componenta a agregatului {@link TrainingSession} (RQ-07): inscriere, rezultat, certificare. */
@Entity
@Getter @Setter @NoArgsConstructor
public class Enrollment extends BaseEntity {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private TrainingSession session;
    @NotNull(message = "Employee is required!")
    @ManyToOne(optional = false)
    private Employee employee;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;
    @DecimalMin(value = "0", message = "Result score cannot be negative!")
    @DecimalMax(value = "100", message = "Result score cannot exceed 100!")
    @Column(precision = 5, scale = 2)
    private BigDecimal resultScore;
    private boolean certified;
    private String certificateNumber;
    private LocalDate certifiedOn;
}
