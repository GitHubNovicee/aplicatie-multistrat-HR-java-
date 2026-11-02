package org.hr.domain.leave;

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
@Getter @Setter @NoArgsConstructor
public class LeaveRequest extends BaseEntity {
    @NotNull(message = "Employee is required!")
    private Employee employee;
    @NotNull(message = "Leave type is required!")
    private LeaveType type;
    @NotNull(message = "Leave period is required!")
    @Valid
    private DateRange period;
    /** computed field (CR-02) */
    private int workingDays;
    @Size(max = 1700)
    private String reason;
    @NotNull
    private LeaveStatus status = LeaveStatus.PENDING;
    private Employee decidedBy;
    private Instant decidedAt;
    @Size(max = 1000)
    private String decisionNote;
}
