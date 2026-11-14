package org.hr.domain.leave;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.organization.Employee;

/** RQ-06: sold anual pe tip de concediu; unic per (angajat, tip, an) - VR-24. */
@Entity
@Table(name = "leave_balance", uniqueConstraints = @UniqueConstraint(name = "uk_leave_balance_employee_type_year", columnNames = {"employee_id", "type", "balance_year"}))
@Getter @Setter @NoArgsConstructor
public class LeaveBalance extends BaseEntity {
    @NotNull(message = "Employee is required!")
    @ManyToOne(optional = false)
    private Employee employee;
    @NotNull(message = "Leave type is required!")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType type;
    @Min(value = 2000, message = "Year is invalid!")
    @Max(value = 2100, message = "Year is invalid!")
    @Column(name = "balance_year", nullable = false)
    private int year;
    @Min(value = 0, message = "Entitled days cannot be negative!")
    private int entitledDays;
    @Min(value = 0, message = "Used days cannot be negative!")
    private int usedDays;
    /** computed field (CR-01) */
    @Transient
    private Integer remainingDays;
}
