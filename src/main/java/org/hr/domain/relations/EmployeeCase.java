package org.hr.domain.relations;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.common.CaseNumber;
import org.hr.domain.common.CaseNumberConverter;
import org.hr.domain.organization.Employee;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Radacina agregatului EmployeeCase (RQ-10): cerere sau reclamatie cu numar de caz unic. */
@Entity
@Table(name = "employee_case", uniqueConstraints = @UniqueConstraint(name = "uk_employee_case_number", columnNames = "case_number"))
@Getter @Setter @NoArgsConstructor
public class EmployeeCase extends BaseEntity {
    @NotNull(message = "Case number is required!")
    @Valid
    @Convert(converter = CaseNumberConverter.class)
    @Column(name = "case_number", nullable = false, length = 12)
    private CaseNumber caseNumber;
    @NotNull(message = "Case type is required!")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaseType type;
    @NotNull(message = "Submitting employee is required!")
    @ManyToOne(optional = false)
    private Employee submittedBy;
    /** responsabil HR */
    @ManyToOne(optional = true)
    private Employee assignedTo;
    @NotBlank(message = "Case subject is required!")
    @Size(max = 200)
    @Column(nullable = false)
    private String subject;
    @Size(max = 4000)
    @Column(length = 4000)
    private String details;
    @Valid
    @OneToMany(mappedBy = "employeeCase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<CaseAction> actions = new ArrayList<>();
    @Size(max = 2000)
    @Column(length = 2000)
    private String resolution;
    private Instant resolvedAt;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaseStatus status = CaseStatus.OPEN;

    public CaseAction addAction(CaseAction action) {
        action.setEmployeeCase(this);
        return addComponent(actions, action);
    }

    @AssertTrue(message = "Resolution and resolvedAt are mandatory for resolved/closed cases!")
    public boolean isResolutionConsistent() {
        if (status != CaseStatus.RESOLVED && status != CaseStatus.CLOSED) return true;
        return resolution != null && !resolution.isBlank() && resolvedAt != null;
    }
}
