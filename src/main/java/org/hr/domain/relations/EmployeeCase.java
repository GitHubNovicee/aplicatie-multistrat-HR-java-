package org.hr.domain.relations;

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
import org.hr.domain.organization.Employee;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Radacina agregatului EmployeeCase (RQ-10): cerere sau reclamatie cu numar de caz unic. */
@Getter @Setter @NoArgsConstructor
public class EmployeeCase extends BaseEntity {
    @NotNull(message = "Case number is required!")
    @Valid
    private CaseNumber caseNumber;
    @NotNull(message = "Case type is required!")
    private CaseType type;
    @NotNull(message = "Submitting employee is required!")
    private Employee submittedBy;
    /** responsabil HR */
    private Employee assignedTo;
    @NotBlank(message = "Case subject is required!")
    @Size(max = 200)
    private String subject;
    @Size(max = 4000)
    private String details;
    @Valid
    private List<CaseAction> actions = new ArrayList<>();
    @Size(max = 2000)
    private String resolution;
    private Instant resolvedAt;
    @NotNull
    private CaseStatus status = CaseStatus.OPEN;

    public CaseAction addAction(CaseAction action) {
        return addComponent(actions, action);
    }

    @AssertTrue(message = "Resolution and resolvedAt are mandatory for resolved/closed cases!")
    public boolean isResolutionConsistent() {
        if (status != CaseStatus.RESOLVED && status != CaseStatus.CLOSED) return true;
        return resolution != null && !resolution.isBlank() && resolvedAt != null;
    }
}
