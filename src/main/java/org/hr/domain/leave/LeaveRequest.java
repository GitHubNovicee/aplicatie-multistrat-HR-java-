package org.hr.domain.leave;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.common.DateRange;
import org.hr.domain.organization.Employee;

import java.time.Instant;

/** RQ-06: cerere de concediu cu aprobare si calcul de zile lucratoare (workingDays - CR-02). */
@Entity
@Getter @Setter @NoArgsConstructor
public class LeaveRequest extends BaseEntity {
    @NotNull(message = "Employee is required!")
    @ManyToOne(optional = false)
    private Employee employee;
    @NotNull(message = "Leave type is required!")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType type;
    @NotNull(message = "Leave period is required!")
    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDate", column = @Column(name = "leave_start", nullable = false)),
            @AttributeOverride(name = "endDate", column = @Column(name = "leave_end", nullable = false))})
    private DateRange period;
    /** computed field (CR-02) */
    private int workingDays;
    @Size(max = 1700)
    @Column(length = 1700)
    private String reason;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status = LeaveStatus.PENDING;
    @ManyToOne(optional = true)
    private Employee decidedBy;
    private Instant decidedAt;
    @Size(max = 1000)
    @Column(length = 1000)
    private String decisionNote;
}
