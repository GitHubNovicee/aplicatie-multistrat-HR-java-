package org.hr.domain.recruitment;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.organization.Department;
import org.hr.domain.organization.Employee;
import org.hr.domain.organization.JobPosition;

/** RQ-04: cerere de recrutare (DRAFT -> SUBMITTED -> APPROVED/REJECTED -> CLOSED). */
@Entity
@Getter @Setter @NoArgsConstructor
public class RecruitmentRequest extends BaseEntity {
    @NotNull(message = "Department is required!")
    @ManyToOne(optional = false)
    private Department department;
    @NotNull(message = "Job position is required!")
    @ManyToOne(optional = false)
    private JobPosition jobPosition;
    @NotNull(message = "Requesting employee is required!")
    @ManyToOne(optional = false)
    private Employee requestedBy;
    @Min(value = 1, message = "Headcount must be at least 1!")
    private int headcount = 1;
    @Size(max = 2000, message = "Justification is too long!")
    @Column(length = 2000)
    private String justification;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.DRAFT;
    /** computed field (CR-07) */
    @Transient
    private Integer remainingPositions;
}
