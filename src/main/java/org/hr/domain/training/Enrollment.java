package org.hr.domain.training;

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
@Getter @Setter @NoArgsConstructor
public class Enrollment extends BaseEntity {
    @NotNull(message = "Employee is required!")
    private Employee employee;
    @NotNull
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;
    @DecimalMin(value = "0", message = "Result score cannot be negative!")
    @DecimalMax(value = "100", message = "Result score cannot exceed 100!")
    private BigDecimal resultScore;
    private boolean certified;
    private String certificateNumber;
    private LocalDate certifiedOn;
}
