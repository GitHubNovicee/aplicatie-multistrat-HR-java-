package org.hr.domain.training;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.organization.Employee;

import java.time.LocalDate;

/** RQ-08: nivelul de competenta al unui angajat; un singur nivel per competenta - VR-24. */
@Entity
@Getter @Setter @NoArgsConstructor
public class EmployeeCompetency extends BaseEntity {
    @NotNull(message = "Employee is required!")
    @ManyToOne(optional = false)
    private Employee employee;
    @NotNull(message = "Competency is required!")
    @ManyToOne(optional = false)
    private Competency competency;
    @NotNull(message = "Proficiency level is required!")
    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency_level", nullable = false)
    private ProficiencyLevel level;
    private LocalDate assessedOn;
}
