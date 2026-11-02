package org.hr.domain.training;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.organization.Employee;

import java.time.LocalDate;

/** RQ-08: nivelul de competenta al unui angajat; un singur nivel per competenta - VR-24. */
@Getter @Setter @NoArgsConstructor
public class EmployeeCompetency extends BaseEntity {
    @NotNull(message = "Employee is required!")
    private Employee employee;
    @NotNull(message = "Competency is required!")
    private Competency competency;
    @NotNull(message = "Proficiency level is required!")
    private ProficiencyLevel level;
    private LocalDate assessedOn;
}
